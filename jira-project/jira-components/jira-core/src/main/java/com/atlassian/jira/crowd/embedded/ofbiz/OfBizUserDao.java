package com.atlassian.jira.crowd.embedded.ofbiz;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettings;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.model.membership.MembershipType;
import com.atlassian.crowd.model.user.DelegatingUserWithAttributes;
import com.atlassian.crowd.model.user.TimestampedUser;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.UserDeleteVeto;
import com.atlassian.jira.user.util.UserKeyStore;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Visitor;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.atlassian.jira.crowd.embedded.ofbiz.DirectoryEntityKey.getKeyLowerCase;
import static com.atlassian.jira.crowd.embedded.ofbiz.PrimitiveMap.of;
import static com.atlassian.jira.util.Functions.mappedVisitor;


/**
 * Implementation of the user DAO that works with OfBiz
 */
public class OfBizUserDao implements UserDao
{
    private static final List<String> ORDER_BY_ATTRIBUTE_ID = ImmutableList.of("id");

    /**
     * The EHCache settings are really coming from ehcache.xml, but we need to set unflushable() here .
     */
    @VisibleForTesting
    static final CacheSettings USER_CACHE_SETTINGS = new CacheSettingsBuilder().unflushable().replicateViaCopy().build();

    private static final String LOAD_USER_CACHE_LOCK = OfBizUserDao.class.getName() + ".loadUserCacheLock";

    private static final String DELETED_EXTERNALLY_SUFFIX = " [X]";

    final OfBizDelegator ofBizDelegator;
    final DirectoryDao directoryDao;
    private final InternalMembershipDao membershipDao;
    private final UserKeyStore userKeyStore;
    private final ClusterLockService clusterLockService;
    final CacheManager cacheManager;
    private final UserDeleteVeto userDeleteVeto;

    private final UserCache userCache = new UserCache();

    /**
     * Lazy cache of attributes.
     */
    private final Cache<AttributeKey, Attributes> userAttributesCache;

    public OfBizUserDao(final OfBizDelegator ofBizDelegator, final DirectoryDao directoryDao,
            final InternalMembershipDao membershipDao, final UserKeyStore userKeyStore,
            final UserDeleteVeto userDeleteVeto, final CacheManager cacheManager,
            final ClusterLockService clusterLockService)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.directoryDao = directoryDao;
        this.membershipDao = membershipDao;
        this.userKeyStore = userKeyStore;
        this.userDeleteVeto = userDeleteVeto;
        this.cacheManager = cacheManager;
        this.clusterLockService = clusterLockService;

        this.userAttributesCache = this.cacheManager.getCache(OfBizUserDao.class.getName() + ".userAttributesCache",
                new UserAttributesCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).flushable().build());

    }

    @Nonnull
    @Override
    public TimestampedUser findByName(final long directoryId, final String userName) throws UserNotFoundException
    {
        return findOfBizUser(directoryId, userName);
    }

    @Nonnull
    OfBizUser findOfBizUser(final long directoryId, final String userName) throws UserNotFoundException
    {
        final OfBizUser user = findByNameOrNull(directoryId, userName);
        if (user == null)
        {
            // Because the SPI says we should do this.
            throw new UserNotFoundException(userName);
        }
        return user;
    }

    /**
     * Tries to find the user by name and returns null if not found.
     * Just like the public method should have done in the first place!
     *
     * @param directoryId Directory ID
     * @param userName the username
     * @return the user, or {@code null} if the user does not exist
     */
    @Nullable
    OfBizUser findByNameOrNull(final long directoryId, final String userName)
    {
        return userCache.getCaseInsensitive(directoryId, userName);
    }

    @Override
    @Nonnull
    public TimestampedUser findByExternalId(final long directoryId, final String externalId)
            throws UserNotFoundException
    {
        final GenericValue user = Select.columns(OfBizUser.SUPPORTED_FIELDS)
                .from(UserEntity.ENTITY)
                .whereEqual(UserEntity.DIRECTORY_ID, directoryId)
                .andEqual(UserEntity.EXTERNAL_ID, externalId)
                .runWith(ofBizDelegator)
                .singleValue();
        if (user == null)
        {
            // Because the SPI says we should do this.
            throw new UserNotFoundException(externalId);
        }
        return OfBizUser.from(user);
    }

    @Override
    @Nonnull
    public UserWithAttributes findByNameWithAttributes(final long directoryId, final String userName)
            throws UserNotFoundException
    {
        final OfBizUser user = findOfBizUser(directoryId, userName);
        final Attributes attributes = userAttributesCache.get(new AttributeKey(directoryId, user.getId()));
        return new DelegatingUserWithAttributes(user, attributes);
    }

    @Nullable
    @Override
    public PasswordCredential getCredential(final long directoryId, final String userName) throws UserNotFoundException
    {
        // Leave it a generic value so we can distinguish the 0 rows case (UserNotFoundException)
        // from the null credential case (return null)
        final GenericValue credentials = Select.columns(UserEntity.CREDENTIAL)
                .from(UserEntity.ENTITY)
                .whereEqual(UserEntity.DIRECTORY_ID, directoryId)
                .andEqual(UserEntity.LOWER_USER_NAME, toLowerCase(userName))
                .runWith(ofBizDelegator)
                .singleValue();
        if (credentials == null)
        {
            throw new UserNotFoundException(userName);
        }
        final String storedCredential = credentials.getString(UserEntity.CREDENTIAL);
        return (storedCredential != null) ? new PasswordCredential(storedCredential, true) : null;
    }

    @Override
    public List<PasswordCredential> getCredentialHistory(final long directoryId, final String userName)
            throws UserNotFoundException
    {
        throw new UnsupportedOperationException("JIRA does not store User Credential History");
    }

    @Override
    public User add(User user, PasswordCredential credential) throws UserAlreadyExistsException
    {
        if (credential != null)
        {
            Validate.isTrue(credential.isEncryptedCredential(), "credential must be encrypted");
        }

        // Check not a duplicate
        throwIfUserAlreadyExists(user);

        // Create a unique key for this new User (added for rename user)
        userKeyStore.ensureUniqueKeyForNewUser(user.getName());

        final Timestamp currentTimestamp = getCurrentTimestamp();
        final Map<String, Object> userData = UserEntity.getData(user, credential, currentTimestamp, currentTimestamp);
        try
        {
            final GenericValue gvUser = ofBizDelegator.createValue(UserEntity.ENTITY, userData);
            final OfBizUser newUser = OfBizUser.from(gvUser);
            userCache.put(newUser);
            return newUser;
        }
        catch (DataAccessException ex)
        {
            // Maybe this is a unique constraint violation due to a race condition?
            throwIfUserAlreadyExists(user);
            throw ex;
        }

    }

    @Nonnull
    @Override
    public BatchResult<User> addAll(Set<UserTemplateWithCredentialAndAttributes> users)
    {
        final BatchResult<User> results = new BatchResult<User>(users.size());
        for (UserTemplateWithCredentialAndAttributes user : users)
        {
            try
            {
                final User addedUser = add(user, user.getCredential());
                results.addSuccess(addedUser);
            }
            catch (UserAlreadyExistsException e)
            {
                results.addFailure(user);
            }
            catch (IllegalArgumentException e)
            {
                results.addFailure(user);
            }
            catch (DataAccessException e)
            {
                // We want to try to catch as many failures as possible so that all the *other* users will
                // still get added
                results.addFailure(user);
            }
        }
        return results;
    }

    private void throwIfUserAlreadyExistsByField(final long directoryId, final String fieldName, final String fieldValue)
            throws UserAlreadyExistsException
    {
        final String existingUserName = Select.stringColumn(UserEntity.USER_NAME)
                .from(UserEntity.ENTITY)
                .whereEqual(UserEntity.DIRECTORY_ID, directoryId)
                .andEqual(fieldName, fieldValue)
                .runWith(ofBizDelegator)
                .singleValue();
        if (existingUserName != null)
        {
            throw new UserAlreadyExistsException(directoryId, existingUserName);
        }
    }

    private void throwIfUserAlreadyExists(final User user) throws UserAlreadyExistsException
    {
        final long directoryId = user.getDirectoryId();
        final String externalId = user.getExternalId();
        if (StringUtils.isNotEmpty(externalId))
        {
            throwIfUserAlreadyExistsByField(directoryId, UserEntity.EXTERNAL_ID, externalId);
        }
        throwIfUserAlreadyExistsByField(directoryId, UserEntity.LOWER_USER_NAME, toLowerCase(user.getName()));
    }

    @Override
    public void storeAttributes(final User user, final Map<String, Set<String>> attributes) throws UserNotFoundException
    {
        // Need the user's id, which is only in the database and not in the cache.
        final Long userId = findUserId(user);

        for (final Map.Entry<String, Set<String>> attribute : Assertions.notNull(attributes).entrySet())
        {
            updateAttribute(userId, user, attribute);
        }
        // Clear the cache for this user
        userAttributesCache.remove(new AttributeKey(user.getDirectoryId(), userId));
    }

    private void updateAttribute(Long userId, User user, Map.Entry<String, Set<String>> attribute)
            throws UserNotFoundException
    {
        // This method is structured to stop churning rows in the database which basically breaks (performance wise)
        // postgresql over time.
        // This method is not properly thread safe.

        // In general we know there is only one value for each attribute even though > 1 value per attribute is supported.

        final List<GenericValue> oldAttributes = getAttributeValues(userId, attribute.getKey(), ORDER_BY_ATTRIBUTE_ID);
        // We need to make sure the old values are in a consistent order or we can end up with bad race conditions
        // where different threads try to update a value another thread has deleted.
        int oldSize = oldAttributes.size();
        int newSize = attribute.getValue().size();
        int commonSize = Math.min(newSize, oldSize);

        Iterator<String> newValues = attribute.getValue().iterator();
        // First handle the overlap
        for (int i = 0; i < commonSize; i++)
        {
            GenericValue attributeGv = oldAttributes.get(i);
            String oldValue = attributeGv.getString(UserAttributeEntity.VALUE);
            String newValue = newValues.next();
            if (StringUtils.isNotEmpty(newValue))
            {
                if (!newValue.equals(oldValue))
                {
                    attributeGv.set(UserAttributeEntity.VALUE, newValue);
                    attributeGv.set(UserAttributeEntity.LOWER_VALUE, IdentifierUtils.toLowerCase(newValue));
                    ofBizDelegator.store(attributeGv);
                }
            }
            else
            {
                ofBizDelegator.removeValue(attributeGv);
            }
        }
        // Handle any additional new values [ if newSize <= oldSize this is a no-op ]
        for (int i = commonSize; i < newSize; i++)
        {
            String newValue = newValues.next();
            if (StringUtils.isNotEmpty(newValue))
            {
                storeAttributeValue(user.getDirectoryId(), userId, attribute.getKey(), newValue);
            }
        }
        // Handle any leftover old values [ if oldSize <= newSize this is a no-op ]
        for (int i = commonSize; i < oldSize; i++)
        {
            GenericValue attributeGv = oldAttributes.get(i);
            ofBizDelegator.removeValue(attributeGv);
        }
    }

    private void storeAttributeValue(final Long directoryId, final Long userId, final String name, final String value)
            throws UserNotFoundException
    {
        ofBizDelegator.createValue(UserAttributeEntity.ENTITY, UserAttributeEntity.getData(directoryId,
                userId, name, value));
    }


    @Override
    public User update(final User user) throws UserNotFoundException
    {
        final GenericValue oldUserGenericValue = findUserGenericValue(user.getDirectoryId(), user.getName());
        final GenericValue newUserGenericValue = UserEntity.setData(user, (GenericValue) oldUserGenericValue.clone());
        if (userDeletedExternally(oldUserGenericValue))
        {
            newUserGenericValue.set(UserEntity.DELETED_EXTERNALLY, 0);
            newUserGenericValue.set(UserEntity.ACTIVE, 1);
        }

        // make sure name doesn't change, you need to call rename for that
        newUserGenericValue.set(UserEntity.USER_NAME, oldUserGenericValue.getString(UserEntity.USER_NAME));
        newUserGenericValue.set(UserEntity.UPDATED_DATE, getCurrentTimestamp());

        final OfBizUser newUser = OfBizUser.from(storeUser(newUserGenericValue));
        userCache.put(newUser);
        return newUser;
    }

    @Override
    public void updateCredential(final User user, final PasswordCredential credential, final int credentialHistory)
            throws UserNotFoundException
    {
        Validate.isTrue(credential.isEncryptedCredential(), "credential must be encrypted");
        final GenericValue storeGenericValue = findUserGenericValue(user.getDirectoryId(), user.getName());
        storeGenericValue.set(UserEntity.CREDENTIAL, credential.getCredential());
        storeUser(storeGenericValue);
    }

    private GenericValue storeUser(final GenericValue userGenericValue)
    {
        ofBizDelegator.store(userGenericValue);
        return userGenericValue;
    }

    @Override
    public User rename(final User oldUser, final String newName) throws UserNotFoundException
    {
        final long directoryId = oldUser.getDirectoryId();
        final boolean updateAppUserMapping;

        // We need to check if there is an existing mapping to this new username
        if (IdentifierUtils.equalsInLowerCase(newName, oldUser.getName()))
        {
            // case-only rename eg "mark" -> "Mark"
            updateAppUserMapping = false;
        }
        else if (userKeyStore.getKeyForUsername(newName) == null)
        {
            // simple case where no-one else has the new name already
            updateAppUserMapping = true;
        }
        else
        {
            // A mapping already exists for the new username.
            // We still want to update cwd_user, but how to update app_user depends on where this user already exists...
            // See https://extranet.atlassian.com/display/JIRADEV/Detect+rename+user+in+LDAP+Scenarios
            final boolean userWillBeShadowed = userIsShadowed(newName, directoryId);
            final boolean userAlreadyExistsInThisDirectory = userExists(newName, directoryId);
            if (userWillBeShadowed)
            {
                // User will be shadowed, so we don't need to update mappings in app_user
                updateAppUserMapping = false;
                if (userAlreadyExistsInThisDirectory)
                {
                    // The user already exists here, but is shadowed. We just delete this copy to get them out of the way
                    remove(findOfBizUser(directoryId, newName));
                }
            }
            else
            {
                // User will not be shadowed so we still want to update our userkey mapping...
                updateAppUserMapping = true;
                // ... but first we need to bump the old app-user mapping out of our way
                final String newNameWithQualifier = handleAppUserEviction(newName);
                if (userAlreadyExistsInThisDirectory)
                {
                    // also update the cwd_user table for this directory
                    updateUsernameInCwdTables(directoryId, newName, newNameWithQualifier);
                }
            }
        }

        // Update the cwd_user and cwd_membership tables
        OfBizUser newUser = updateUsernameInCwdTables(directoryId, oldUser.getName(), newName);

        if (updateAppUserMapping)
        {
            final boolean userIsCurrentlyShadowed = userIsShadowed(oldUser.getName(), directoryId);
            if (userIsCurrentlyShadowed)
            {
                // Another user is the owner of the userkey.
                // Create a new key for the now unshadowed user instead of moving the current key.
                userKeyStore.ensureUniqueKeyForNewUser(newName);
            }
            else
            {
                // This user actually owns that userkey - lets rename it
                userKeyStore.renameUser(oldUser.getName(), newName);
                // But maybe we just unshadowed a user. Even a user in an inactive directory can come back to life and need a userkey
                if (userExistsInAnyDirectory(oldUser.getName()))
                {
                    // Whoops - they just lost the userkey mapping. Ensure that they have one
                    userKeyStore.ensureUniqueKeyForNewUser(oldUser.getName());
                }
            }
        }

        return newUser;
    }

    /**
     * We check if the cache is initialized or not.
     * This is an important cache to see if jira is available or not for the users to login.
     *
     * @return true if the cache is initialized, false if not.
     */
    public boolean isCacheInitialized()
    {
        return userCache.isCacheInitialized();
    }

    /**
     * Will update the username in the cwd_user table as well as fixing the cwd_membership records.
     *
     * @param directoryId The directory ID
     * @param oldName old username
     * @param newName new username
     * @return the new User object
     * @throws UserNotFoundException if old user does not exist in cwd_user
     */
    private OfBizUser updateUsernameInCwdTables(final long directoryId, final String oldName, final String newName)
            throws UserNotFoundException
    {
        final GenericValue userGenericValue = findUserGenericValue(directoryId, oldName);
        userGenericValue.set(UserEntity.USER_NAME, newName);
        userGenericValue.set(UserEntity.LOWER_USER_NAME, IdentifierUtils.toLowerCase(newName));
        userGenericValue.set(UserEntity.UPDATED_DATE, getCurrentTimestamp());

        OfBizUser newUser = OfBizUser.from(storeUser(userGenericValue));

        userCache.remove(directoryId, oldName);
        userCache.put(newUser);

        updateGroupMembership(directoryId, oldName, newName);
        return newUser;
    }

    protected void updateGroupMembership(final long directoryId, final String oldName, final String newName)
    {
        final Map<String, Object> whereClause = FieldMap.build(
                MembershipEntity.DIRECTORY_ID, directoryId)
                .add(MembershipEntity.LOWER_CHILD_NAME, IdentifierUtils.toLowerCase(oldName))
                .add(MembershipEntity.MEMBERSHIP_TYPE, MembershipType.GROUP_USER.name());
        final List<GenericValue> oldMemberships = ofBizDelegator.findByAnd(MembershipEntity.ENTITY, whereClause);
        membershipDao.removeAllUserMemberships(directoryId, oldName);
        for (GenericValue oldMembership : oldMemberships)
        {
            final UserOrGroupStub userStub = new SimpleUserOrGroupStub(oldMembership.getLong(MembershipEntity.CHILD_ID), directoryId, newName);
            final UserOrGroupStub groupStub = new SimpleUserOrGroupStub(oldMembership.getLong(MembershipEntity.PARENT_ID), directoryId, oldMembership.getString(MembershipEntity.PARENT_NAME));
            try
            {
                membershipDao.addUserToGroup(directoryId, userStub, groupStub);
            }
            catch (MembershipAlreadyExistsException e)
            {
                //at this moment we do not care that membership already exists,
            }
        }
    }

    /**
     * Returns true if the given username exists in any directory, including inactive directories.
     *
     * @param username the user
     * @return true if the given username exists in any directory, including inactive directories.
     */
    private boolean userExistsInAnyDirectory(final String username)
    {
        return Select.id()
                .from(UserEntity.ENTITY)
                .whereEqual(UserEntity.LOWER_USER_NAME, toLowerCase(username))
                .runWith(ofBizDelegator)
                .count() > 0;
    }

    private boolean userIsShadowed(final String name, final long directoryId)
    {
        for (Directory directory : directoryDao.findAll())
        {
            if (directory.getId() == directoryId)
            {
                return false;
            }
            if (findByNameOrNull(directory.getId(), name) != null)
            {
                return true;
            }
        }
        // We should not get here unless we are operating on an inactive directory.
        return false;
    }

    /**
     * This method takes an existing username (eg 'fred') and bumps the mapping in the app-user table to a related
     * "temporary" name (eg 'fred#1').
     *
     * @param fromUsername the current username
     * @return the name that the old user was bumped to.
     */
    private String handleAppUserEviction(String fromUsername)
    {
        int count = 1;
        String toUsername = fromUsername + "#1";
        while (userKeyStore.getKeyForUsername(toUsername) != null)
        {
            if (count == Integer.MAX_VALUE)
            {
                // Realistically, this should never happen...
                throw new IllegalStateException("App user user eviction namespace exhausted");
            }
            toUsername = fromUsername + '#' + (++count);
        }

        // Found an available username.  Evict the deleted user!
        userKeyStore.renameUser(fromUsername, toUsername);
        return toUsername;
    }

    @Override
    public BatchResult<String> removeAllUsers(long directoryId, Set<String> userNames)
    {
        final BatchResult<String> results = new BatchResult<String>(userNames.size());
        for (String userName : userNames)
        {
            try
            {
                remove(findOfBizUser(directoryId, userName));
                results.addSuccess(userName);
            }
            catch (UserNotFoundException e)
            {
                results.addFailure(userName);
            }
        }
        return results;
    }

    @Override
    public Set<String> getAllExternalIds(final long directoryId) throws DirectoryNotFoundException
    {
        // ensure the directory exists
        directoryDao.findById(directoryId);

        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        userCache.visitAllInDirectory(directoryId, new Visitor<OfBizUser>()
        {
            @Override
            public void visit(final OfBizUser user)
            {
                if (user.getExternalId() != null)
                {
                    builder.add(user.getExternalId());
                }
            }
        });
        return builder.build();
    }

    @Override
    public long getUserCount(final long directoryId) throws DirectoryNotFoundException
    {
        // ensure the directory exists
        directoryDao.findById(directoryId);
        return Select.countFrom(UserEntity.ENTITY).whereEqual(UserEntity.DIRECTORY_ID, directoryId).runWith(ofBizDelegator).singleValue();
    }

    @Override
    public void removeAttribute(final User user, final String attributeName) throws UserNotFoundException
    {
        // Need the user's id, which is only in the database and not in the cache.
        final GenericValue userGenericValue = findUserGenericValue(user.getDirectoryId(), user.getName());
        Long userId = userGenericValue.getLong(UserEntity.USER_ID);
        ofBizDelegator.removeByAnd(UserAttributeEntity.ENTITY, of(UserAttributeEntity.USER_ID, userGenericValue.getLong(UserEntity.USER_ID),
                UserAttributeEntity.NAME, attributeName));
        userAttributesCache.remove(new AttributeKey(user.getDirectoryId(), userId));
    }

    private List<GenericValue> getAttributeValues(Long userId, final String attributeName, List<String> orderBy)
            throws UserNotFoundException
    {
        return ofBizDelegator.findByAnd(UserAttributeEntity.ENTITY, of(
                        UserAttributeEntity.USER_ID, userId,
                        UserAttributeEntity.NAME, attributeName),
                orderBy
        );
    }

    @Override
    public void remove(final User user) throws UserNotFoundException
    {
        // Check if we should allow this user to be deleted or if we want to disable them and keep their details
        if (!allowDeleteUser(user))
        {
            disableUserDeletedExternally(user);
            return;
        }

        final Long userId = findUserId(user);

        // remove memberships
        membershipDao.removeAllUserMemberships(user);

        // Remove all attributes
        ofBizDelegator.removeByAnd(UserAttributeEntity.ENTITY, of(UserAttributeEntity.USER_ID, userId));

        final int rows = Delete.from(UserEntity.ENTITY)
                .whereEqual(UserEntity.DIRECTORY_ID, user.getDirectoryId())
                .andEqual(UserEntity.LOWER_USER_NAME, getLowerUserName(user))
                .execute(ofBizDelegator);
        if (rows == 0)
        {
            throw new UserNotFoundException(user.getName());
        }

        // Remove User
        userCache.remove(getKeyLowerCase(user));
        userAttributesCache.remove(new AttributeKey(user.getDirectoryId(), userId));
    }

    private boolean allowDeleteUser(final User user)
    {
        // First check if this user exists in another active directory - if so then removing this account is the right behaviour
        return userExistsInAnotherActiveDirectory(user) || userDeleteVeto.allowDeleteUser(user);
    }

    private boolean userExistsInAnotherActiveDirectory(final User user)
    {
        for (Directory directory : directoryDao.findAll())
        {
            long directoryId = directory.getId();
            if (directory.isActive() && directoryId != user.getDirectoryId()
                    && userCache.getCaseInsensitive(directoryId, user.getName()) != null)
            {
                return true;
            }
        }
        return false;
    }

    private void disableUserDeletedExternally(final User user)
    {
        final GenericValue userGenericValue;
        try
        {
            userGenericValue = UserEntity.setData(user, findUserGenericValue(user.getDirectoryId(), user.getName()));
        }
        catch (UserNotFoundException e)
        {
            // Should never happen, but would be nothing we can do to recover anyway...
            return;
        }

        // check if they have already been disabled due to external delete.
        if (userDeletedExternally(userGenericValue))
        {
            // We don't want to add multiple " [X]" suffices to the end of the display name, nor continually alter the UPDATED_DATE
            return;
        }

        userGenericValue.set(UserEntity.UPDATED_DATE, getCurrentTimestamp());
        userGenericValue.set(UserEntity.ACTIVE, 0);
        userGenericValue.set(UserEntity.DELETED_EXTERNALLY, 1);
        // Update the Display Name to indicate that they are removed - this is also a temporary workaround to problem
        // with resurrecting user with exact same details - hopefully that behaviour will be eventually changed in emb crowd.

        // Check the suffix is not already applied, just to be extra-careful
        String displayName = user.getDisplayName();
        if (!displayName.endsWith(DELETED_EXTERNALLY_SUFFIX))
        {
            userGenericValue.set(UserEntity.DISPLAY_NAME, user.getDisplayName() + DELETED_EXTERNALLY_SUFFIX);
        }

        OfBizUser newUser = OfBizUser.from(storeUser(userGenericValue));
        userCache.put(newUser);
    }

    private boolean userDeletedExternally(final GenericValue userGenericValue)
    {
        final Integer i = userGenericValue.getInteger(UserEntity.DELETED_EXTERNALLY);
        return i != null && i != 0;
    }

    @Override
    @SuppressWarnings ("unchecked")
    public <T> List<T> search(final long directoryId, final EntityQuery<T> query)
    {
        final SearchRestriction searchRestriction = query.getSearchRestriction();
        final EntityCondition baseCondition = new UserEntityConditionFactory(ofBizDelegator).getEntityConditionFor(searchRestriction);
        final EntityExpr directoryCondition = new EntityExpr(UserEntity.DIRECTORY_ID, EntityOperator.EQUALS, directoryId);
        final EntityCondition entityCondition;

        if (baseCondition == null)
        {
            return getAllUsersFromCache(directoryId, query.getReturnType());
        }
        else
        {
            final List<EntityCondition> entityConditions = new ArrayList<EntityCondition>(2);
            entityConditions.add(baseCondition);
            entityConditions.add(directoryCondition);
            entityCondition = new EntityConditionList(entityConditions, EntityOperator.AND);
        }
        final List<GenericValue> results = ofBizDelegator.findByCondition(
                UserEntity.ENTITY, entityCondition, null, Collections.singletonList(UserEntity.LOWER_USER_NAME));

        final List<T> typedResults = new ArrayList<T>(results.size());
        final Function<GenericValue, T> valueFunction = (Function<GenericValue, T>) (query.getReturnType().equals(String.class) ? TO_USERNAME_FUNCTION : TO_USER_FUNCTION);
        for (GenericValue result : results)
        {
            typedResults.add(valueFunction.get(result));
        }

        return typedResults;
    }

    @SuppressWarnings({ "unchecked" })
    private <T> List<T> getAllUsersFromCache(final long directoryId, final Class<T> returnType)
    {
        if (returnType.isAssignableFrom(OfBizUser.class))
        {
            return (List<T>)userCache.getAllInDirectory(directoryId);
        }
        if (returnType.isAssignableFrom(String.class))
        {
            return (List<T>)userCache.getAllNamesInDirectory(directoryId);
        }
        throw new IllegalArgumentException("Class type for return values ('" + returnType + "') is not 'String' or 'User'");
    }

    private static final Function<GenericValue, String> TO_USERNAME_FUNCTION = new Function<GenericValue, String>()
    {
        public String get(final GenericValue gvUser)
        {
            return gvUser.getString(UserEntity.USER_NAME);
        }
    };

    private static final Function<GenericValue, OfBizUser> TO_USER_FUNCTION = new Function<GenericValue, OfBizUser>()
    {
        public OfBizUser get(final GenericValue gvUser)
        {
            return OfBizUser.from(gvUser);
        }
    };

    /**
     * Invoked by {@link OfBizCacheFlushingManager} to ensure caches are being flushed in the right order on {@link
     * XMLRestoreFinishedEvent}
     */
    public void flushCache()
    {
        userCache.refresh();
        userAttributesCache.removeAll();
    }

    // Note: This is relatively expensive as it fetches *all* fields.  Only use it to get fresh info for an update
    // request, not to load these more generally.
    @Nonnull
    private GenericValue findUserGenericValue(long directoryId, String userName) throws UserNotFoundException
    {
        final GenericValue userGenericValue = Select.from(UserEntity.ENTITY)
                .whereEqual(UserEntity.DIRECTORY_ID, directoryId)
                .andEqual(UserEntity.LOWER_USER_NAME, toLowerCase(userName))
                .runWith(ofBizDelegator)
                .singleValue();
        if (userGenericValue == null)
        {
            throw new UserNotFoundException(userName);
        }
        return userGenericValue;
    }

    @Nonnull
    private Long findUserId(final User user) throws UserNotFoundException
    {
        final Long id = Select.id()
                .from(UserEntity.ENTITY)
                .whereEqual(UserEntity.DIRECTORY_ID, user.getDirectoryId())
                .andEqual(UserEntity.LOWER_USER_NAME, getLowerUserName(user))
                .runWith(ofBizDelegator)
                .singleValue();
        if (id == null)
        {
            throw new UserNotFoundException(user.getName());
        }
        return id;
    }

    private boolean userExists(final String name, final long directoryId)
    {
        return findByNameOrNull(directoryId, name) != null;
    }

    @SuppressWarnings ("CastToConcreteClass")  // Necessary to take advantage of OfBizUser-only cached lowercase form
    private static String getLowerUserName(User user)
    {
        if (user instanceof OfBizUser)
        {
            return ((OfBizUser) user).getLowerName();
        }
        return toLowerCase(user.getName());
    }

    private static Timestamp getCurrentTimestamp()
    {
        return new Timestamp(System.currentTimeMillis());
    }

    private class UserAttributesCacheLoader implements CacheLoader<AttributeKey, Attributes>
    {
        @Override
        public Attributes load(@Nonnull final AttributeKey key)
        {
            final List<GenericValue> attributes = Select.columns(OfBizAttributesBuilder.SUPPORTED_FIELDS)
                    .from(UserAttributeEntity.ENTITY)
                    .whereEqual(UserAttributeEntity.DIRECTORY_ID, key.getDirectoryId())
                    .andEqual(UserAttributeEntity.USER_ID, key.getUserId())
                    .runWith(ofBizDelegator)
                    .asList();
            return OfBizAttributesBuilder.toAttributes(attributes);
        }
    }



    class UserCache extends UserOrGroupCache<OfBizUser>
    {
        UserCache()
        {
            super(UserEntity.ENTITY);
        }

        @Override
        Lock getLock()
        {
            return clusterLockService.getLockForName(LOAD_USER_CACHE_LOCK);
        }

        @Override
        Cache<DirectoryEntityKey, OfBizUser> createCache()
        {
            return cacheManager.getCache(OfBizUserDao.class.getName() + ".userCache", null, USER_CACHE_SETTINGS);
        }

        @Override
        long countAllUsingDatabase()
        {
            // Count by directory to make sure that what we count matches what we would actually visit.
            // We would only get a discrepancy if there are garbage users with an invalid directory ID, but
            // better safe than sorry...
            long count = 0L;
            for (Directory directory : directoryDao.findAll())
            {
                count += Select.id()
                        .from(UserEntity.ENTITY)
                        .whereEqual(UserEntity.DIRECTORY_ID, directory.getId())
                        .runWith(ofBizDelegator)
                        .count();
            }
            return count;
        }

        @Override
        void visitAllUsingDatabase(final Visitor<OfBizUser> visitor)
        {
            final Visitor<GenericValue> gvVisitor = mappedVisitor(TO_USER_FUNCTION, visitor);
            for (Directory directory : directoryDao.findAll())
            {
                Select.columns(OfBizUser.SUPPORTED_FIELDS)
                        .from(UserEntity.ENTITY)
                        .whereEqual(UserEntity.DIRECTORY_ID, directory.getId())
                        .runWith(ofBizDelegator)
                        .visitWith(gvVisitor);
            }
        }
    }

}
