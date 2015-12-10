package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.membership.MembershipType;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.util.cache.WeakInterner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.atlassian.crowd.model.membership.MembershipType.GROUP_GROUP;
import static com.atlassian.crowd.model.membership.MembershipType.GROUP_USER;
import static com.atlassian.jira.crowd.embedded.ofbiz.PrimitiveMap.builder;
import static com.atlassian.jira.util.cache.WeakInterner.newWeakInterner;
import static org.ofbiz.core.entity.EntityUtil.getOnly;


public class OfBizInternalMembershipDao implements InternalMembershipDao
{
    private static final Logger LOG = Logger.getLogger(OfBizInternalMembershipDao.class);
    private static final ImmutableList<String> FIELD_LIST_LOWERCHILDNAME = ImmutableList.of(MembershipEntity.LOWER_CHILD_NAME);
    private static final ImmutableList<String> FIELD_LIST_LOWERPARENTNAME = ImmutableList.of(MembershipEntity.LOWER_PARENT_NAME);

    private final OfBizDelegator ofBizDelegator;

    private WeakInterner<String> myInterner = newWeakInterner();

    // The groups that a user belongs to
    private final Cache<MembershipKey, Set<String>> parentsCache;
    // The users that belongs to a group
    private final Cache<MembershipKey, Set<String>> childrenCache;

    public OfBizInternalMembershipDao(final OfBizDelegator ofBizDelegator, final CacheManager cacheManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        parentsCache = cacheManager.getCache(OfBizInternalMembershipDao.class.getName() + ".parentsCache",
                new ParentsLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).flushable().build());
        childrenCache = cacheManager.getCache(OfBizInternalMembershipDao.class.getName() + ".childrenCache",
                new ChildrenLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).flushable().build());
    }

    public boolean isUserDirectMember(final long directoryId, final String userName, final String groupName)
    {
        return isDirectMember(directoryId, GROUP_USER, groupName, userName);
    }

    public boolean isGroupDirectMember(final long directoryId, final String childGroup, final String parentGroup)
    {
        return isDirectMember(directoryId, GROUP_GROUP, parentGroup, childGroup);
    }

    private boolean isDirectMember(final long directoryId, final MembershipType membershipType, final String parentName, final String childName)
    {
        // We look in the parent cache because typically results in a much shorter list than the looking via the children cache.
        Set<String> parents = parentsCache.get(MembershipKey.getKey(directoryId, childName, membershipType));
        // Look first for the case we have.  Then try for the lower case.
        // This is an optimisation that should work in most JIRA instances where group and user names are really all lower case.
        return parents.contains(parentName) || parents.contains(toLowerCase(parentName));
    }

    public void addUserToGroup(final long directoryId, final UserOrGroupStub user, final UserOrGroupStub group) throws MembershipAlreadyExistsException
    {
        if (isDirectMember(directoryId, GROUP_USER, group.getName(), user.getName()))
        {
            throw new MembershipAlreadyExistsException(directoryId, user.getName(), group.getName());
        }

        try
        {
            createMembership(directoryId, GROUP_USER, group, user);
        }
        catch (DataAccessException e)
        {
            // If we have a race we could end up with a duplicate key exception, exposed here.
            if (isDirectMember(directoryId, GROUP_USER, group.getName(), user.getName()))
            {
                throw new MembershipAlreadyExistsException(directoryId, user.getName(), group.getName());
            }
            throw e;
        }
        invalidateChildrenCacheEntry(directoryId, group.getName(), MembershipType.GROUP_USER);
        invalidateParentsCacheEntry(directoryId, user.getName(), MembershipType.GROUP_USER);
    }

    public BatchResult<String> addAllUsersToGroup(long directoryId, Collection<UserOrGroupStub> users, UserOrGroupStub group)
    {
        // We get the list of children this way because of a seen performance problem when doing initial full
        // synchronizations of Remote Directories, rather than using the isDirectMemberOf methods which cause cache thrashing.
        List<String> currentUsers = getChildrenOfGroupFromCache(directoryId, group.getName(), MembershipType.GROUP_USER);

        BatchResult<String> result = new BatchResult<String>(users.size());
        boolean groupIsDirty = false;

        for (UserOrGroupStub user : users)
        {
            try
            {
                if (currentUsers.contains(user.getName()))
                {
                    result.addFailure(user.getName());
                }
                else
                {
                    createMembership(directoryId, GROUP_USER, group, user);
                    invalidateParentsCacheEntry(directoryId, user.getName(), MembershipType.GROUP_USER);
                    groupIsDirty = true;
                }
                result.addSuccess(user.getName());
            }
            catch (DataAccessException e)
            {
                // If we come across any database errors we want to continue processing all other users
                result.addFailure(user.getName());
            }
        }

        if (groupIsDirty)
        {
            invalidateChildrenCacheEntry(directoryId, group.getName(), MembershipType.GROUP_USER);
        }
        return result;
    }

    private void createMembership(final long directoryId, final MembershipType membershipType, final UserOrGroupStub parent, final UserOrGroupStub child)
    {
        ofBizDelegator.createValue("Membership", builder()
                .put("directoryId", directoryId)
                .put("childId", child.getId())
                .put("childName", child.getName())
                .put("lowerChildName", child.getLowerName())
                .put("parentId", parent.getId())
                .put("parentName", parent.getName())
                .put("lowerParentName", parent.getLowerName())
                .put("membershipType", membershipType.name())
                .build());
    }

    public void addGroupToGroup(final long directoryId, final UserOrGroupStub child, final UserOrGroupStub parent)
    {
        if (!isDirectMember(directoryId, GROUP_GROUP, parent.getName(), child.getName()))
        {
            createMembership(directoryId, GROUP_GROUP, parent, child);
            invalidateChildrenCacheEntry(directoryId, parent.getName(), MembershipType.GROUP_GROUP);
            invalidateParentsCacheEntry(directoryId, child.getName(), MembershipType.GROUP_GROUP);
        }
    }

    public void removeUserFromGroup(final long directoryId, final UserOrGroupStub user, final UserOrGroupStub group)
            throws MembershipNotFoundException
    {
        removeMembership(directoryId, GROUP_USER, group, user);
        invalidateChildrenCacheEntry(directoryId, group.getName(), MembershipType.GROUP_USER);
        invalidateParentsCacheEntry(directoryId, user.getName(), MembershipType.GROUP_USER);
    }

    private void removeMembership(final long directoryId, final MembershipType membershipType, final UserOrGroupStub parent, final UserOrGroupStub child)
            throws MembershipNotFoundException
    {
        final GenericValue membershipGenericValue = getOnly(ofBizDelegator.findByAnd("Membership", builder()
                .put("directoryId", directoryId)
                .put("childId", child.getId())
                .put("parentId", parent.getId())
                .put("membershipType", membershipType.name())
                .build()));

        if (membershipGenericValue == null)
        {
            throw new MembershipNotFoundException(child.getName(), parent.getName());
        }
        ofBizDelegator.removeValue(membershipGenericValue);
    }

    public void removeGroupFromGroup(final long directoryId, final UserOrGroupStub childGroup, final UserOrGroupStub parentGroup)
            throws MembershipNotFoundException
    {
        removeMembership(directoryId, GROUP_GROUP, parentGroup, childGroup);
        invalidateChildrenCacheEntry(directoryId, parentGroup.getName(), MembershipType.GROUP_GROUP);
        invalidateParentsCacheEntry(directoryId, childGroup.getName(), MembershipType.GROUP_GROUP);
    }

    public void removeAllMembersFromGroup(final Group group)
    {
        ofBizDelegator.removeByAnd(MembershipEntity.ENTITY, builder()
                .put(MembershipEntity.DIRECTORY_ID, group.getDirectoryId())
                .put(MembershipEntity.PARENT_NAME, group.getName())
                .build());
        removeGroupParentFromAllCaches(group.getDirectoryId(), group.getName());
    }

    public void removeAllGroupMemberships(final Group group)
    {
        ofBizDelegator.removeByAnd(MembershipEntity.ENTITY, builder()
                .put(MembershipEntity.DIRECTORY_ID, group.getDirectoryId())
                .put(MembershipEntity.MEMBERSHIP_TYPE, GROUP_GROUP.name())
                .put(MembershipEntity.CHILD_NAME, group.getName())
                .build());
        removeGroupChildFromAllCaches(group.getDirectoryId(), group.getName());
    }

    @Override
    public void removeAllUserMemberships(final User user)
    {
        removeAllUserMemberships(user.getDirectoryId(), user.getName());
    }

    @Override
    public void removeAllUserMemberships(final long directoryId, final String username)
    {
        ofBizDelegator.removeByAnd(MembershipEntity.ENTITY, builder()
                .put(MembershipEntity.DIRECTORY_ID, directoryId)
                .put(MembershipEntity.MEMBERSHIP_TYPE, GROUP_USER.name())
                .put(MembershipEntity.CHILD_NAME, username)
                .build());
        removeUserFromAllCaches(directoryId, username);
    }

    public <T> List<String> search(final long directoryId, final MembershipQuery<T> query)
    {
        // Optimisation to use the cache if we can.  This is possible if there are no bounding restrictions
        if (canUseCacheSearch(query))
        {
            return searchCache(directoryId, query);
        }

        final PrimitiveMap.Builder filter = builder();
        filter.put("directoryId", directoryId);
        if (query.isFindChildren())
        {
            filter.putCaseInsensitive("lowerParentName", query.getEntityNameToMatch());
        }
        else
        {
            filter.putCaseInsensitive("lowerChildName", query.getEntityNameToMatch());
        }

        if (query.getEntityToReturn().equals(EntityDescriptor.user()) || query.getEntityToMatch().equals(EntityDescriptor.user()))
        {
            filter.put("membershipType", GROUP_USER.name());
        }
        else
        {
            filter.put("membershipType", GROUP_GROUP.name());
        }

        final List<GenericValue> memberships = findMemberships(filter.build());
        final List<String> entityNames = new ArrayList<String>(memberships.size());
        for (GenericValue membership : memberships)
        {
            entityNames.add(query.isFindChildren() ? membership.getString("childName") : membership.getString("parentName"));
        }

        return entityNames;
    }

    /**
     * Search the cache to satisfy ths query.
     * We assume the caller has established this is valid to do.
     * @param directoryId Directory
     * @param query  Query
     * @return List of results
     */
    private List<String> searchCache(final long directoryId, final MembershipQuery<?> query)
    {
        MembershipType type;
        if (query.getEntityToReturn().equals(EntityDescriptor.user()) || query.getEntityToMatch().equals(EntityDescriptor.user()))
        {
            type = MembershipType.GROUP_USER;
        }
        else
        {
            type = MembershipType.GROUP_GROUP;
        }
        if (query.isFindChildren())
        {
            // This is get members of group
            return getChildrenOfGroupFromCache(directoryId, query.getEntityNameToMatch(), type);
        }
        else
        {
            // This is get groups a user is a member of
            return getParentsForMemberFromCache(directoryId, query.getEntityNameToMatch(), type);
        }

    }

    /**
     * Find the groups a user is a member of from the cache.
     *
     * @param directoryId  The directory Id
     * @param childName  The child name (either a user or a sub-group)
     * @param type The kind of membership to check
     * @return A list of lower-case Group names
     */
    private List<String> getParentsForMemberFromCache(final long directoryId, final String childName, final MembershipType type)
    {
        // Try with the case we have been given.
        final Set<String> parents = parentsCache.get(MembershipKey.getKey(directoryId, childName, type));
        if (parents == null)
        {
            return new ArrayList<String>(0);
        }

        final List<String> groupNames = new ArrayList<String>(parents.size());
        for (String parent : parents)
        {
            groupNames.add(parent);
        }
        return groupNames;
    }

    /**
     * Get the Members of a Group from the cache.
     * @param directoryId The directory Id
     * @param groupName The group name
     * @param type The kind of membership to check
     * @return A list of lower-case member names.
     */
    private List<String> getChildrenOfGroupFromCache(final long directoryId, final String groupName, final MembershipType type)
    {
        final Set<String> children = childrenCache.get(MembershipKey.getKey(directoryId, groupName, type));
        if (children == null)
        {
            return new ArrayList<String>(0);
        }

        final List<String> childNames = new ArrayList<String>(children.size());
        for (String child : children)
        {
            childNames.add(child);
        }
        return childNames;
    }

    /**
     * Test if this query can be satisfied from the cache.
     * @param query Query
     * @return True if can be satisfied from the cache
     */
    private static boolean canUseCacheSearch(final MembershipQuery<?> query)
    {
        // We can use the cache iff there are no limits on the search
        return query.getStartIndex() == 0 && query.getMaxResults() == EntityQuery.ALL_RESULTS;
    }

    private List<GenericValue> findMemberships(final Map<String, Object> filter)
    {
        return ofBizDelegator.findByAnd("Membership", filter);
    }

    private Set<String> findChildren(final long directoryId, final MembershipType membershipType, final String parent)
    {
        EntityExpr directoryCondition = new EntityExpr(MembershipEntity.DIRECTORY_ID, EntityOperator.EQUALS, directoryId);
        EntityExpr parentCondition = new EntityExpr(MembershipEntity.LOWER_PARENT_NAME, EntityOperator.EQUALS, parent);
        EntityExpr typeCondition = new EntityExpr(MembershipEntity.MEMBERSHIP_TYPE, EntityOperator.EQUALS, membershipType.name());

        EntityCondition condition = new EntityConditionList(ImmutableList.of(directoryCondition, parentCondition, typeCondition), EntityOperator.AND);

        OfBizListIterator memberships = ofBizDelegator.findListIteratorByCondition(MembershipEntity.ENTITY, condition, null, FIELD_LIST_LOWERCHILDNAME, null, null);
        try
        {
            // The large size hint is to prevent multiple rebuilds of the collection for large result sets.  The
            // collection is ephemeral, so the memory it temporarily wastes shouldn't matter.
            Set<String> children =  new HashSet<String>(4096);
            GenericValue membership = memberships.next();
            while (membership != null)
            {
                final String child = membership.getString(MembershipEntity.LOWER_CHILD_NAME);
                children.add(myInterner.intern(child));
                membership = memberships.next();
            }
            return ImmutableSet.copyOf(children);
        }
        finally
        {
            memberships.close();
        }
    }

    private Set<String> findParents(final long directoryId, final MembershipType membershipType, final String child)
    {
        EntityExpr directoryCondition = new EntityExpr(MembershipEntity.DIRECTORY_ID, EntityOperator.EQUALS, directoryId);
        EntityExpr childCondition = new EntityExpr(MembershipEntity.LOWER_CHILD_NAME, EntityOperator.EQUALS, child);
        EntityExpr typeCondition = new EntityExpr(MembershipEntity.MEMBERSHIP_TYPE, EntityOperator.EQUALS, membershipType.name());

        EntityCondition condition = new EntityConditionList(ImmutableList.of(directoryCondition, childCondition, typeCondition), EntityOperator.AND);

        OfBizListIterator memberships = ofBizDelegator.findListIteratorByCondition(MembershipEntity.ENTITY, condition, null, FIELD_LIST_LOWERPARENTNAME, null, null);
        try
        {
            // The collection is ephemeral, so the memory it temporarily wastes shouldn't matter, but it would
            // be unusual to have a lot of parents unless using making very heavy use of nested groups, anyway.
            Set<String> parents =  new HashSet<String>(64);
            GenericValue membership = memberships.next();
            while (membership != null)
            {
                final String parent = membership.getString(MembershipEntity.LOWER_PARENT_NAME);
                parents.add(myInterner.intern(parent));
                membership = memberships.next();
            }
            return ImmutableSet.copyOf(parents);
        }
        finally
        {
            memberships.close();
        }
    }

    /**
     * Invoked by {@link OfBizCacheFlushingManager} to ensure caches are being flushed in the right order on
     * {@link XMLRestoreFinishedEvent}
     */
    public void flushCache()
    {
        parentsCache.removeAll();
        childrenCache.removeAll();
    }

    private void invalidateChildrenCacheEntry(final Long directoryId, final String parentName, final MembershipType type)
    {
        MembershipKey key = MembershipKey.getKey(directoryId, parentName, type);
        childrenCache.remove(key);
    }

    private void invalidateParentsCacheEntry(final Long directoryId, final String childName, final MembershipType type)
    {
        MembershipKey key = MembershipKey.getKey(directoryId, childName, type);
        parentsCache.remove(key);
    }

    private void removeUserFromAllCaches(final Long directoryId, final String userName)
    {
        List<String> groups = getParentsForMemberFromCache(directoryId, userName, MembershipType.GROUP_USER);
        for (String groupName : groups)
        {
            invalidateChildrenCacheEntry(directoryId, groupName, MembershipType.GROUP_USER);
            invalidateParentsCacheEntry(directoryId, userName, MembershipType.GROUP_USER);
        }
    }

    private void removeGroupParentFromAllCaches(final Long directoryId, final String groupName)
    {
        List<String> users = getChildrenOfGroupFromCache(directoryId, groupName, MembershipType.GROUP_USER);
        for (String userName : users)
        {
            invalidateChildrenCacheEntry(directoryId, groupName, MembershipType.GROUP_USER);
            invalidateParentsCacheEntry(directoryId, userName, MembershipType.GROUP_USER);
        }
        List<String> childGroups = getChildrenOfGroupFromCache(directoryId, groupName, MembershipType.GROUP_GROUP);
        for (String childName : childGroups)
        {
            invalidateChildrenCacheEntry(directoryId, groupName, MembershipType.GROUP_GROUP);
            invalidateParentsCacheEntry(directoryId, childName, MembershipType.GROUP_GROUP);
        }
    }

    private void removeGroupChildFromAllCaches(final Long directoryId, final String groupName)
    {
        List<String> parentGroups = getParentsForMemberFromCache(directoryId, groupName, MembershipType.GROUP_GROUP);
        for (String parentName : parentGroups)
        {
            invalidateChildrenCacheEntry(directoryId, parentName, MembershipType.GROUP_GROUP);
            invalidateParentsCacheEntry(directoryId, groupName, MembershipType.GROUP_GROUP);
        }
    }

    private class ParentsLoader implements CacheLoader<MembershipKey, Set<String>>
    {
        @Override
        public Set<String> load(@Nonnull final MembershipKey key)
        {
            return findParents(key.getDirectoryId(), key.getType(), key.getName());
        }
    }

    private class ChildrenLoader implements CacheLoader<MembershipKey, Set<String>>
    {
        @Override
        public Set<String> load(@Nonnull final MembershipKey key)
        {
            return findChildren(key.getDirectoryId(), key.getType(), key.getName());
        }
    }
}
