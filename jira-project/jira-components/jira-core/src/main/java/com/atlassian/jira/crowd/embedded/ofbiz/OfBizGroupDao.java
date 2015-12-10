package com.atlassian.jira.crowd.embedded.ofbiz;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettings;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.embedded.spi.GroupDao;
import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.model.group.DelegatingGroupWithAttributes;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Functions;
import com.atlassian.jira.util.Visitor;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.atlassian.jira.crowd.embedded.ofbiz.GroupEntity.ENTITY;
import static com.atlassian.jira.crowd.embedded.ofbiz.PrimitiveMap.of;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.singletonList;

public class OfBizGroupDao implements GroupDao
{
    /** The EHCache settings are really coming from ehcache.xml, but we need to set unflushable() here .*/
    @VisibleForTesting
    static final CacheSettings GROUP_CACHE_SETTINGS = new CacheSettingsBuilder().unflushable().replicateViaCopy().build();

    private static final String LOAD_GROUP_CACHE_LOCK = OfBizGroupDao.class.getName() + ".loadGroupCacheLock";

    private final OfBizDelegator ofBizDelegator;
    private final DirectoryDao directoryDao;
    private final InternalMembershipDao membershipDao;
    private final CacheManager cacheManager;
    private final ClusterLockService clusterLockService;

    // This cache is fully populated and does NOT use the (preferred) lazy loading pattern. But we lazy ref it so we can get
    // the very heavy load out of the constructor.  It is plausible that this reference should have shared invalidation,
    // but normally we only reset it on a data import, which we don't allow in an operating cluster for now anyway.
    private final GroupCache groupCache = new GroupCache();

    public OfBizGroupDao(final OfBizDelegator ofBizDelegator, final DirectoryDao directoryDao,
            final InternalMembershipDao membershipDao, final CacheManager cacheManager,
            final ClusterLockService clusterLockService)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.membershipDao = membershipDao;
        this.directoryDao = directoryDao;
        this.cacheManager = cacheManager;
        this.clusterLockService = clusterLockService;
    }

    @Nonnull
    @Override
    public InternalDirectoryGroup findByName(final long directoryId, final String name) throws GroupNotFoundException
    {
        return findOfBizGroup(directoryId, name);
    }

    @Nonnull
    OfBizGroup findOfBizGroup(final long directoryId, final String name) throws GroupNotFoundException
    {
        final OfBizGroup group = findByNameOrNull(directoryId, name);
        if (group == null)
        {
            // Because the SPI says we should do this.
            throw new GroupNotFoundException(name);
        }
        return group;
    }


    /**
     * Tries to find the group by name and returns {@code null} if not found.
     * Just like the public method should have done in the first place!
     *
     * @param directoryId Directory ID
     * @param name the group name
     * @return the group, or {@code null} if the group does not exist
     */
    @Nullable
    OfBizGroup findByNameOrNull(final long directoryId, final String name)
    {
        return groupCache.getCaseInsensitive(directoryId, name);
    }

    @Override
    public GroupWithAttributes findByNameWithAttributes(final long directoryId, final String name)
            throws GroupNotFoundException
    {
        final OfBizGroup group = findOfBizGroup(directoryId, name);
        final List<GenericValue> attributes = Select.columns(OfBizAttributesBuilder.SUPPORTED_FIELDS)
                .from(GroupAttributeEntity.ENTITY)
                .whereEqual(GroupAttributeEntity.DIRECTORY_ID, directoryId)
                .andEqual(GroupAttributeEntity.GROUP_ID, group.getId())
                .runWith(ofBizDelegator)
                .asList();
        return new DelegatingGroupWithAttributes(group, OfBizAttributesBuilder.toAttributes(attributes));
    }




    @Override
    public Group add(final Group group)
    {
        return add(group, false);
    }

    @Override
    public Group addLocal(final Group group)
    {
        return add(group, true);
    }

    private Group add(final Group group, boolean local)
    {
        // Create the new Group in the DB
        final Timestamp currentTimestamp = getCurrentTimestamp();
        final Map<String, Object> groupData = GroupEntity.getData(group, currentTimestamp, currentTimestamp, local);
        final GenericValue gvGroup = ofBizDelegator.createValue(GroupEntity.ENTITY, groupData);
        final OfBizGroup ofBizGroup = OfBizGroup.from(gvGroup);
        groupCache.put(ofBizGroup);
        return ofBizGroup;
    }

    @Override
    public BatchResult<Group> addAll(Set<? extends Group> groups) throws DirectoryNotFoundException
    {
        BatchResult<Group> results = new BatchResult<Group>(groups.size());
        for (Group group : groups)
        {
            try
            {
                final Group addedGroup = add(group);
                results.addSuccess(addedGroup);
            }
            catch (DataAccessException e)
            {
                // Try to catch problems so that at least the *other* groups will be added
                results.addFailure(group);
            }
        }
        return results;
    }

    @Override
    public Group update(final Group group) throws GroupNotFoundException
    {
        // Get the latest GenericValue from the DB
        final GenericValue groupGenericValue = findGroupGenericValue(group);
        // Update the relevant values
        groupGenericValue.set(GroupEntity.ACTIVE, BooleanUtils.toInteger(group.isActive()));
        groupGenericValue.set(GroupEntity.UPDATED_DATE, getCurrentTimestamp());
        groupGenericValue.set(GroupEntity.DESCRIPTION, group.getDescription());
        groupGenericValue.set(GroupEntity.LOWER_DESCRIPTION, toLowerCaseAllowNull(group.getDescription()));
        // Save to DB
        storeGroup(groupGenericValue);
        // Convert GenericValue to an object
        OfBizGroup ofBizGroup = OfBizGroup.from(groupGenericValue);
        // Shove it in the cache
        groupCache.put(ofBizGroup);
        return ofBizGroup;
    }

    private void storeGroup(final GenericValue groupGenericValue)
    {
        ofBizDelegator.store(groupGenericValue);
    }

    @Override
    public Group rename(final Group group, final String newName)
    {
        throw new UnsupportedOperationException("Renaming groups is not supported!");
    }

    @Override
    public void storeAttributes(final Group group, final Map<String, Set<String>> attributes)
            throws GroupNotFoundException
    {
        for (final Map.Entry<String, Set<String>> attribute : notNull(attributes).entrySet())
        {
            // remove attributes before adding new ones.
            // Duplicate key values are allowed, but we always add as a complete set under the key.
            removeAttribute(group, attribute.getKey());
            if ((attribute.getValue() != null) && !attribute.getValue().isEmpty())
            {
                storeAttributeValues(group, attribute.getKey(), attribute.getValue());
            }
        }
    }

    private void storeAttributeValues(final Group group, final String name, final Set<String> values)
            throws GroupNotFoundException
    {
        for (final String value : values)
        {
            if (StringUtils.isNotEmpty(value))
            {
                storeAttributeValue(group, name, value);
            }
        }
    }

    private void storeAttributeValue(final Group group, final String name, final String value)
            throws GroupNotFoundException
    {
        final GenericValue groupGenericValue = findGroupGenericValue(group);
        ofBizDelegator.createValue(GroupAttributeEntity.ENTITY, GroupAttributeEntity.getData(group.getDirectoryId(),
                groupGenericValue.getLong(GroupEntity.ID), name, value));
    }


    @Override
    public void removeAttribute(final Group group, final String attributeName) throws GroupNotFoundException
    {
        notNull(attributeName);
        final GenericValue gv = findGroupGenericValue(group);
        ofBizDelegator.removeByAnd(GroupAttributeEntity.ENTITY, of(GroupAttributeEntity.GROUP_ID, gv.getLong(GroupEntity.ID),
                GroupAttributeEntity.NAME, attributeName));
    }

    @Override
    public void remove(final Group group) throws GroupNotFoundException
    {
        final GenericValue groupGenericValue = findGroupGenericValue(group);
        // remove memberships
        membershipDao.removeAllMembersFromGroup(group);
        membershipDao.removeAllGroupMemberships(group);

        ofBizDelegator.removeByAnd(GroupAttributeEntity.ENTITY, of(GroupAttributeEntity.GROUP_ID, groupGenericValue.getLong(GroupEntity.ID)));
        ofBizDelegator.removeValue(groupGenericValue);
        groupCache.remove(DirectoryEntityKey.getKeyLowerCase(group));
    }

    @Override
    public <T> List<T> search(final long directoryId, final EntityQuery<T> query)
    {
        final SearchRestriction searchRestriction = query.getSearchRestriction();
        final EntityCondition baseCondition = new GroupEntityConditionFactory().getEntityConditionFor(searchRestriction);
        if (baseCondition == null)
        {
            // The search restriction is a NullRestriction (or null) then we can just throw back the whole of the groups cache.
            return (getAllGroupsFromCache(directoryId, query.getReturnType()));
        }
        final EntityExpr directoryCondition = new EntityExpr(GroupEntity.DIRECTORY_ID, EntityOperator.EQUALS, directoryId);
        final EntityCondition entityCondition;
        final List<EntityCondition> entityConditions = new ArrayList<EntityCondition>(2);
        entityConditions.add(baseCondition);
        entityConditions.add(directoryCondition);
        entityCondition = new EntityConditionList(entityConditions, EntityOperator.AND);

        List<GenericValue> results;
        results = ofBizDelegator.findByCondition(ENTITY, entityCondition, null, singletonList(GroupEntity.NAME));

        List<T> typedResults = new ArrayList<T>(results.size());
        final Function<GenericValue, T> valueFunction = getTransformer(query.getReturnType());
        for (GenericValue result : results)
        {
            typedResults.add(valueFunction.get(result));
        }

        return typedResults;
    }

    @SuppressWarnings ({ "unchecked" })
    private <T> List<T> getAllGroupsFromCache(final long directoryId, final Class<T> returnType)
    {
        if (returnType.isAssignableFrom(OfBizGroup.class))
        {
            return (List<T>)groupCache.getAllInDirectory(directoryId);
        }
        if (returnType.isAssignableFrom(String.class))
        {
            return (List<T>)groupCache.getAllNamesInDirectory(directoryId);
        }
        throw new IllegalArgumentException("Class type for return values ('" + returnType + "') is not 'String' or 'Group'");
    }

    private static <T> Function<GenericValue, T> getTransformer(final Class<T> returnType)
    {
        //noinspection unchecked
        return (Function<GenericValue, T>) (returnType.equals(String.class) ? TO_GROUPNAME_FUNCTION : TO_GROUP_FUNCTION);
    }

    private static final Function<GenericValue, String> TO_GROUPNAME_FUNCTION = new Function<GenericValue, String>()
    {
        public String get(final GenericValue gv)
        {
            return gv.getString(GroupEntity.NAME);
        }
    };

    private static final Function<GenericValue, OfBizGroup> TO_GROUP_FUNCTION = new Function<GenericValue, OfBizGroup>()
    {
        public OfBizGroup get(final GenericValue gv)
        {
            return OfBizGroup.from(gv);
        }
    };

    @Override
    public BatchResult<String> removeAllGroups(long directoryId, Set<String> groupNames)
    {
        BatchResult<String> results = new BatchResult<String>(groupNames.size());
        for (String groupName : groupNames)
        {
            try
            {
                remove(findByName(directoryId, groupName));
                results.addSuccess(groupName);
            }
            catch (GroupNotFoundException e)
            {
                results.addFailure(groupName);
            }
        }
        return results;
    }

    /**
     * Invoked by {@link OfBizCacheFlushingManager} to ensure caches are being flushed in the right order on
     * {@link XMLRestoreFinishedEvent}
     */
    public void flushCache()
    {
        groupCache.refresh();
    }

    private GenericValue findGroupGenericValue(final Group group) throws GroupNotFoundException
    {
        return findGroupGenericValue(group.getDirectoryId(), group.getName());
    }

    // Note: These are relatively expensive as it fetches *all* fields.
    // Only use them to get fresh info for an update request, not to load these more generally.
    private GenericValue findGroupGenericValue(final long directoryId, final String name) throws GroupNotFoundException
    {
        notNull("name", name);
        final GenericValue groupGenericValue = Select.from(GroupEntity.ENTITY)
                .whereEqual(GroupEntity.DIRECTORY_ID, directoryId)
                .andEqual(GroupEntity.LOWER_NAME, toLowerCase(name))
                .runWith(ofBizDelegator)
                .singleValue();
        if (groupGenericValue == null)
        {
            throw new GroupNotFoundException(name);
        }
        return groupGenericValue;
    }

    @Nullable
    private static String toLowerCaseAllowNull(final String value)
    {
        return (value != null) ? toLowerCase(value) : null;
    }

    private static Timestamp getCurrentTimestamp()
    {
        return new Timestamp(System.currentTimeMillis());
    }



    class GroupCache extends UserOrGroupCache<OfBizGroup>
    {
        GroupCache()
        {
            super(GroupEntity.ENTITY);
        }

        @Override
        Lock getLock()
        {
            return clusterLockService.getLockForName(LOAD_GROUP_CACHE_LOCK);
        }

        @Override
        Cache<DirectoryEntityKey,OfBizGroup> createCache()
        {
            return cacheManager.getCache(OfBizGroupDao.class.getName() + ".groupCache", null, GROUP_CACHE_SETTINGS);
        }

        @Override
        long countAllUsingDatabase()
        {
            // Count by directory to make sure that what we count matches what we would actually visit.
            // We would only get a discrepancy if there are garbage groups with an invalid directory ID, but
            // better safe than sorry...
            long count = 0L;
            for (Directory directory : directoryDao.findAll())
            {
                count += Select.id()
                        .from(GroupEntity.ENTITY)
                        .whereEqual(GroupEntity.DIRECTORY_ID, directory.getId())
                        .runWith(ofBizDelegator)
                        .count();
            }
            return count;
        }

        @Override
        void visitAllUsingDatabase(final Visitor<OfBizGroup> visitor)
        {
            final Visitor<GenericValue> gvVisitor = Functions.mappedVisitor(TO_GROUP_FUNCTION, visitor);
            for (Directory directory : directoryDao.findAll())
            {
                Select.columns(OfBizGroup.SUPPORTED_FIELDS)
                        .from(GroupEntity.ENTITY)
                        .whereEqual(GroupEntity.DIRECTORY_ID, directory.getId())
                        .runWith(ofBizDelegator)
                        .visitWith(gvVisitor);
            }
        }

    }
}
