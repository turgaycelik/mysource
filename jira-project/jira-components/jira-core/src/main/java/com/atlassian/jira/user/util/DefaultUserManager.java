package com.atlassian.jira.user.util;

import com.atlassian.applinks.api.auth.oauth.ConsumerTokenService;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.CrowdException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.manager.directory.DirectoryPermissionException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.crowd.embedded.ofbiz.UserOrGroupStub;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.any;

public class DefaultUserManager implements UserManager
{
    private static final Logger log = Logger.getLogger(DefaultUserManager.class);
    private static final UserQuery<User> QUERY_ALL_USERS =
            new UserQuery<User>(User.class, NullRestrictionImpl.INSTANCE, 0, UserQuery.ALL_RESULTS);

    private final CrowdService crowdService;
    private final CrowdDirectoryService crowdDirectoryService;
    private final DirectoryManager directoryManager;
    private final UserKeyStore userKeyStore;
    private final ApplicationManager applicationManager;
    private final ApplicationProperties applicationProperties;

    public DefaultUserManager(final CrowdService crowdService, final CrowdDirectoryService crowdDirectoryService,
                              final DirectoryManager directoryManager, final UserKeyStore userKeyStore,
                              final ApplicationManager applicationManager,
                              final ApplicationProperties applicationProperties)
    {
        this.crowdService = crowdService;
        this.crowdDirectoryService = crowdDirectoryService;
        this.directoryManager = directoryManager;
        this.userKeyStore = userKeyStore;
        this.applicationManager = applicationManager;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public int getTotalUserCount()
    {
        return getAllUsersFromCrowd().size();
    }

    @Override
    @Nonnull
    public Collection<User> getUsers()
    {
        return getAllUsersFromCrowd();
    }

    @Override
    @Nonnull
    public Collection<ApplicationUser> getAllApplicationUsers()
    {
        Collection<User> crowdUsers = getAllUsersFromCrowd();
        List<ApplicationUser> users = new ArrayList<ApplicationUser>(crowdUsers.size());
        for (User user : crowdUsers)
        {
            users.add(new DelegatingApplicationUser(userKeyStore.getKeyForUsername(user.getName()), user));
        }
        return users;
    }

    @Nonnull
    private List<User> getAllUsersFromDirectory(@Nonnull final Directory directory,
                                                final boolean reportDirectoryNotFound)
    {
        try
        {
            return directoryManager.searchUsers(directory.getId(), QUERY_ALL_USERS);
        }
        catch (DirectoryNotFoundException e)
        {
            if (reportDirectoryNotFound)
            {
                throw new OperationFailedException(e);
            }
            return ImmutableList.of();
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            throw new OperationFailedException(e);
        }
    }

    @Nonnull
    private Collection<User> getAllUsersFromCrowd()
    {
        long startTime = System.currentTimeMillis();

        // Going through Crowd Service is a bit slow because the current implementation in ApplicationService
        // Will sort the results for paging even if we ask for ALL_RESULTS
        // We therefore have an optimised version here because JIRA loves calling get all users.

        final List<Directory> userDirectories = Lists.newArrayList(crowdDirectoryService.findAllDirectories());

        final Collection<User> allUsers;
        if (userDirectories.size() > 1)
        {
            // Remove shadowed users
            final Map<String, User> uniqueSetOfUsers = new HashMap<String, User>();
            // First directory takes precedence on the second one and so on.
            // Since map overrides values we need to reverse the order in the list first.
            Collections.reverse(userDirectories);
            for (Directory directory : userDirectories)
            {
                if (directory.isActive())
                {
                    for (User user : getAllUsersFromDirectory(directory, false))
                    {
                        String lowercaseUsername =
                                (user instanceof UserOrGroupStub) ? ((UserOrGroupStub) user).getLowerName() :
                                        IdentifierUtils.toLowerCase(user.getName());
                        uniqueSetOfUsers.put(lowercaseUsername, user);
                    }
                }
            }
            allUsers = uniqueSetOfUsers.values();
        }
        else if (userDirectories.size() == 1)
        {
            // No removal of shadowed users is required
            allUsers = getAllUsersFromDirectory(userDirectories.get(0), true);
        }
        else
        {
            allUsers = Collections.emptyList();
        }

        if (log.isDebugEnabled())
        {
            log.info("Found " + allUsers.size() + " users in " + (System.currentTimeMillis() - startTime) + "ms.");
        }
        return allUsers;
    }

    @Override
    @Nonnull
    public Set<User> getAllUsers()
    {
        return Sets.newHashSet(getAllUsersFromCrowd());
    }

    private User getCrowdUser(final String userName)
    {
        // Make sure that null userName is handled in a uniform way by all OSUser implementations - eg see JRA-15821, CWD-1275 
        if (userName == null)
        {
            return null;
        }
        return crowdService.getUser(userName);
    }

    @Override
    public User getUser(final String userName)
    {
        return getUserObject(userName);
    }

    @Override
    public User getUserObject(@Nullable String userName)
    {
        return getCrowdUser(userName);
    }

    @Override
    public User findUserInDirectory(String userName, Long directoryId)
    {
        try
        {
            return directoryManager.findUserByName(directoryId, userName);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new IllegalArgumentException(e);
        }
        catch (UserNotFoundException e)
        {
            return null;
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            throw new OperationFailedException(e);
        }
    }

    @Override
    public User getUserEvenWhenUnknown(final String userName)
    {
        if (userName == null)
        {
            return null;
        }
        final User user = getCrowdUser(userName);
        return (user != null) ? user : unknownUser(userName);
    }

    @Override
    public ApplicationUser getUserByKey(final String key)
    {
        String username = userKeyStore.getUsernameForKey(key);
        if (username == null)
        {
            return null;
        }
        User user = getCrowdUser(username);
        if (user == null)
        {
            return null;
        }
        return new DelegatingApplicationUser(key, user);
    }

    @Override
    public ApplicationUser getUserByName(final String username)
    {
        User user = getCrowdUser(username);
        if (user == null)
        {
            return null;
        }

        final String key = userKeyStore.getKeyForUsername(username);
        if (key == null)
        {
            // Maybe we should be nice and create the mapping instead?
            throw new IllegalStateException("User '" + username + "' exists but has no unique key mapping.");
        }
        return new DelegatingApplicationUser(key, user);
    }

    @Override
    public ApplicationUser getUserByKeyEvenWhenUnknown(@Nullable String userKey)
    {
        if (userKey == null)
        {
            return null;
        }

        final String userName = userKeyStore.getUsernameForKey(userKey);
        if (userName == null)
        {
            // We have to make something up for the username, which could collide
            // with a user that actually exists, but what better answer is there?
            return unknownApplicationUser(userKey, userKey);
        }

        final User user = getUser(userName);
        if (user == null)
        {
            // Deleted user
            return unknownApplicationUser(userKey, userName);
        }

        return new DelegatingApplicationUser(userKey, user);
    }

    @Override
    public ApplicationUser getUserByNameEvenWhenUnknown(@Nullable String userName)
    {
        if (userName == null)
        {
            return null;
        }

        final User user = getCrowdUser(userName);
        String userKey = userKeyStore.getKeyForUsername(userName);

        if (user == null)
        {
            if (userKey == null)
            {
                // We have to make something up for the userKey, which could collide
                // with a user that actually exists, but what better answer is there?
                userKey = IdentifierUtils.toLowerCase(userName);
            }
            return unknownApplicationUser(userKey, userName);
        }

        if (userKey == null)
        {
            // Maybe we should be nice and create the mapping instead?
            throw new IllegalStateException("User '" + userName + "' exists but has no unique key mapping.");
        }

        return new DelegatingApplicationUser(userKey, user);
    }


    @Override
    public boolean canUpdateUser(User user)
    {
        if (user == null)
        {
            return false;
        }
        // Check if the directory allows user modification
        return userDirectoryAllowsUpdateUser(user);
    }

    @Override
    public boolean canUpdateUser(@Nonnull ApplicationUser user)
    {
        return userDirectoryAllowsUpdateUser(user.getDirectoryUser());
    }

    @Override
    public boolean userCanUpdateOwnDetails(@Nonnull final ApplicationUser user)
    {
        return canUpdateUser(user) && !applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }

    public boolean canRenameUser(ApplicationUser user)
    {
        return userDirectoryAllowsRenameUser(user.getDirectoryUser())
                && isJaacsUnusedOrRenameAllowedAnyway();
    }

    @VisibleForTesting
    boolean isJaacsUnusedOrRenameAllowedAnyway()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_USER_CROWD_ALLOW_RENAME)
                || !any(applicationManager.findAll(), new IsExternalApplication());
    }

    @Override
    public void updateUser(User user)
    {
        try
        {
            final ImmutableUser.Builder builder = ImmutableUser.newUser(user);
            builder.emailAddress(StringUtils.trim(user.getEmailAddress()));
            crowdService.updateUser(builder.toUser());
            // Might have changed the active/inactive flag.
            // We clear on EVERY add to group, so may as well clear here without checking current status.
            ComponentAccessor.getUserUtil().clearActiveUserCount();
        }
        catch (InvalidUserException ex)
        {
            // This occurs when the passed User does not have the expected DirectoryId
            throw new OperationFailedException(ex);
        }
        catch (OperationNotPermittedException ex)
        {
            // Permission Violation
            throw new OperationFailedException(ex);
        }
    }


    // JRADEV-18718  If a deleted user is in the way, then get rid of them.
    // To do this, we'll keep adding #1, #2, #3, #4, etc. until we find a username
    // that isn't already taken and rename the deleted user to that to get
    // them out of the way of the username's new owner.
    private void handleDeletedUserEviction(String fromUsername)
    {
        if (userKeyStore.getKeyForUsername(fromUsername) == null)
        {
            // No collision, so nothing to do.
            return;
        }

        int count = 1;
        String toUsername = fromUsername + "#1";
        while (userKeyStore.getKeyForUsername(toUsername) != null)
        {
            if (count == Integer.MAX_VALUE)
            {
                // Realistically, this should never happen...
                throw new IllegalStateException("Deleted user eviction namespace exhausted");
            }
            toUsername = fromUsername + '#' + (++count);
        }

        // Found an available username.  Evict the deleted user!
        userKeyStore.renameUser(fromUsername, toUsername);
    }

    private void handleRenamedUser(ApplicationUser user)
    {
        final String newUsername = IdentifierUtils.toLowerCase(user.getUsername());
        final String oldUsername = userKeyStore.getUsernameForKey(user.getKey());

        // If the username didn't change, then we don't need to do anything special, here.
        if (newUsername.equals(oldUsername))
        {
            return;
        }

        // Make sure the desired username doesn't already belong to some other user
        if (getCrowdUser(newUsername) != null)
        {
            throw new IllegalArgumentException(
                    "Cannot rename: user with username '" + newUsername + "' already exists.");
        }

        // JRADEV-18718  If a deleted user is in the way, then get rid of them
        handleDeletedUserEviction(newUsername);

        try
        {
            directoryManager.renameUser(user.getDirectoryId(), oldUsername, user.getUsername());

            // JRADEV-19614  If revealing a previously shadowed user, we'd better make sure they get a key!
            if (crowdService.getUser(oldUsername) != null)
            {
                userKeyStore.ensureUniqueKeyForNewUser(oldUsername);
            }

            clearConsumerTokens(oldUsername);
        }
        catch (CrowdException ex)
        {
            throw new OperationFailedException(ex);
        }
        catch (DirectoryPermissionException ex)
        {
            throw new OperationFailedException(ex);
        }
    }

    private void clearConsumerTokens(final String username)
    {
        // Delete any OAuth Consumer tokens for this user
        final ConsumerTokenService consumerTokenService =
                ComponentAccessor.getOSGiComponentInstanceOfType(ConsumerTokenService.class);
        if (consumerTokenService != null)
        {
            consumerTokenService.removeAllTokensForUsername(username);
        }
        else if (log.isDebugEnabled())
        {
            log.debug("Unable to clear consumer tokens for '" + username +
                    "' because the service could not be located.  Maybe applinks is offline?");
        }
    }

    @Override
    public void updateUser(ApplicationUser user)
    {
        handleRenamedUser(user);

        try
        {
            crowdService.updateUser(user.getDirectoryUser());
            // Might have changed the active/inactive flag.
            // We clear on EVERY add to group, so may as well clear here without checking current status.
            ComponentAccessor.getUserUtil().clearActiveUserCount();
        }
        catch (InvalidUserException ex)
        {
            // This occurs when the passed User does not have the expected DirectoryId
            throw new OperationFailedException(ex);
        }
        catch (OperationNotPermittedException ex)
        {
            // Permission Violation
            throw new OperationFailedException(ex);
        }
    }

    @Override
    public boolean canUpdateUserPassword(User user)
    {
        // If we can't update the user we can't update the password
        if (!userDirectoryAllowsUpdateUser(user))
        {
            return false;
        }
        final Directory directory = crowdDirectoryService.findDirectoryById(user.getDirectoryId());
        return canDirectoryUpdateUserPassword(directory);
    }

    private boolean userDirectoryAllowsUpdateUser(User user)
    {
        if (user == null)
        {
            return false;
        }
        Directory directory = crowdDirectoryService.findDirectoryById(user.getDirectoryId());
        if (directory == null)
        {
            return false;
        }
        return directory.getAllowedOperations().contains(OperationType.UPDATE_USER);
    }

    private boolean userDirectoryAllowsRenameUser(User user)
    {
        if (user == null)
        {
            return false;
        }
        Directory directory = crowdDirectoryService.findDirectoryById(user.getDirectoryId());
        if (directory == null)
        {
            return false;
        }
        if (!directory.getAllowedOperations().contains(OperationType.UPDATE_USER))
        {
            return false;
        }
        final DirectoryType directoryType = directory.getType();
        return directoryType == DirectoryType.INTERNAL || directoryType == DirectoryType.DELEGATING;
    }


    @Override
    public boolean canUpdateGroupMembershipForUser(User user)
    {
        if (user == null)
        {
            return false;
        }
        Directory directory = crowdDirectoryService.findDirectoryById(user.getDirectoryId());
        if (directory == null)
        {
            return false;
        }
        return directory.getAllowedOperations().contains(OperationType.UPDATE_GROUP);
    }

    @Override
    public Collection<Group> getGroups()
    {
        final Query<Group> query =
                new GroupQuery<Group>(Group.class, GroupType.GROUP, NullRestrictionImpl.INSTANCE, 0, -1);
        Iterable<Group> crowdGroups = crowdService.search(query);
        // Hope for a quick conversion:
        if (crowdGroups instanceof Collection)
        {
            return (Collection<Group>) crowdGroups;
        }
        Set<Group> groups = new LinkedHashSet<Group>();
        for (Group group : crowdGroups)
        {
            groups.add(group);
        }
        return groups;
    }

    @Override
    public Set<Group> getAllGroups()
    {
        Collection<Group> groups = getGroups();
        if (groups instanceof Set)
        {
            return (Set<Group>) groups;
        }
        return new LinkedHashSet<Group>(groups);
    }

    private Group getCrowdGroup(final String groupName)
    {
        // Make sure that null groupName is handled in a uniform way by all OSUser implementations - eg see JRA-15821, CWD-1275
        if (groupName == null)
        {
            return null;
        }
        return crowdService.getGroup(groupName);
    }

    @Override
    public Group getGroup(final String groupName)
    {
        return getCrowdGroup(groupName);
    }

    @Override
    public Group getGroupObject(@Nullable String groupName)
    {
        return getCrowdGroup(groupName);
    }

    @Override
    @Nonnull
    public List<Directory> getWritableDirectories()
    {
        final List<Directory> allDirectories = crowdDirectoryService.findAllDirectories();
        List<Directory> writableDirectories = new ArrayList<Directory>(allDirectories.size());
        for (Directory directory : allDirectories)
        {
            if (directory.getAllowedOperations().contains(OperationType.CREATE_USER) && directory.isActive())
            {
                writableDirectories.add(directory);
            }
        }
        return writableDirectories;
    }

    @Override
    public boolean hasWritableDirectory()
    {
        return getWritableDirectories().size() > 0;
    }

    @Override
    public boolean hasPasswordWritableDirectory()
    {
        final List<Directory> writableDirectories = getWritableDirectories();
        for (Directory directory : writableDirectories)
        {
            if (canDirectoryUpdateUserPassword(directory))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasGroupWritableDirectory()
    {
        final List<Directory> allDirectories = crowdDirectoryService.findAllDirectories();
        for (Directory directory : allDirectories)
        {
            if (directory.isActive() && directory.getAllowedOperations().contains(OperationType.CREATE_GROUP))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Directory getDirectory(Long directoryId)
    {
        return crowdDirectoryService.findDirectoryById(directoryId);
    }

    @Override
    public boolean isUserExisting(ApplicationUser user)
    {
        return user != null && user.getDirectoryId() != -1;
    }

    @Override
    public boolean canDirectoryUpdateUserPassword(final Directory directory)
    {
        if (directory == null)
        {
            return false;
        }
        // For Delegated LDAP directories, we can never modify the password
        if (directory.getType() == DirectoryType.DELEGATING)
        {
            return false;
        }
        return directory.getAllowedOperations().contains(OperationType.UPDATE_USER);
    }

    @Nonnull
    public UserState getUserState(@Nullable User user)
    {
        if (user == null)
        {
            return UserState.INVALID_USER;
        }
        return getUserState(notNull("user", user).getName(), user.getDirectoryId());
    }

    @Nonnull
    public UserState getUserState(@Nullable ApplicationUser user)
    {
        if (user == null)
        {
            return UserState.INVALID_USER;
        }
        return getUserState(notNull("user", user).getUsername(), user.getDirectoryId());
    }

    @Nonnull
    public UserState getUserState(@Nonnull String username, long queryDirectoryId)
    {
        notNull("username", username);
        if (queryDirectoryId == -1L)
        {
            return UserState.INVALID_USER;
        }

        boolean foundQuery = false;
        boolean foundOther = false;
        for (Directory directory : crowdDirectoryService.findAllDirectories())
        {
            if (queryDirectoryId == directory.getId())
            {
                if (!isUserInDirectory(username, directory))
                {
                    return UserState.INVALID_USER;
                }
                if (foundOther)
                {
                    return UserState.SHADOW_USER;
                }
                foundQuery = true;
            }
            else if (!foundOther && isUserInDirectory(username, directory))
            {
                if (foundQuery)
                {
                    return UserState.NORMAL_USER_WITH_SHADOW;
                }
                foundOther = true;
            }
        }

        return foundQuery ? UserState.NORMAL_USER : UserState.INVALID_USER;
    }

    private boolean isUserInDirectory(String userName, Directory directory)
    {
        return directory.isActive() && findUserInDirectory(userName, directory.getId()) != null;
    }

    private User unknownUser(String userNameOrKey)
    {
        return new ImmutableUser(-1, userNameOrKey, userNameOrKey, "?", false);
    }

    private ApplicationUser unknownApplicationUser(String userKey, String userName)
    {
        return new DelegatingApplicationUser(userKey, unknownUser(userName));
    }

    static class IsExternalApplication implements Predicate<Application>
    {
        @Override
        public boolean apply(final Application input)
        {
            // "crowd-embedded" itself is the only "permanent" Application
            return !input.isPermanent();
        }
    }
}
