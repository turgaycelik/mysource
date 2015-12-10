package com.atlassian.jira.crowd.embedded;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.dao.permission.InternalUserPermissionDAO;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.manager.directory.DirectoryManagerGeneric;
import com.atlassian.crowd.manager.directory.DirectorySynchroniser;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManager;
import com.atlassian.crowd.manager.directory.monitor.poller.DirectoryPollerManager;
import com.atlassian.crowd.manager.lock.DirectoryLockManager;
import com.atlassian.crowd.manager.permission.PermissionManager;
import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.Entity;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.search.util.SearchResultsUtil;
import com.atlassian.event.api.EventPublisher;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.atlassian.crowd.search.EntityDescriptor.group;
import static com.atlassian.crowd.search.builder.QueryBuilder.queryFor;
import static com.atlassian.crowd.search.util.SearchResultsUtil.constrainResults;
import static java.util.Arrays.asList;

/**
 * Overrides the crowd implementation of {@code DirectoryManager} as a workaround for CWD-4028.
 * <p>
 * The crowd-supplied implementation passes around {@code directoryId} and frequently uses the {@code private}
 * method {@code getDirectoryImplementation(long)} to obtain the corresponding {@code RemoteDirectory}, generally
 * to make a single call on it, throw it away, and get a new one.  If it were a trivial getter, then this would
 * not matter, but it isn't and the costs add up, particularly for large customers.
 * </p>
 * <p>
 * This overrides the implementation of the searching functions to stop them from doing this.  Repeated calls to
 * {@code getDirectoryImplementation(long)} are factored out to a single call up front, and the {@code RemoteDirectory}
 * is passed around instead of the {@code directoryId}.
 * </p>
 *
 * @since v6.3.6
 */
public class JiraDirectoryManager extends DirectoryManagerGeneric
{
    private final DirectoryInstanceLoader directoryInstanceLoader;

    public JiraDirectoryManager(final DirectoryDao directoryDao, final ApplicationDAO applicationDAO,
            final EventPublisher eventPublisher, final PermissionManager permissionManager,
            final DirectoryInstanceLoader directoryInstanceLoader, final DirectorySynchroniser directorySynchroniser,
            final DirectoryPollerManager directoryPollerManager, final DirectoryLockManager directoryLockManager,
            final SynchronisationStatusManager synchronisationStatusManager,
            final InternalUserPermissionDAO userPermissionDAO)
    {
        super(directoryDao, applicationDAO, eventPublisher, permissionManager, directoryInstanceLoader,
                directorySynchroniser, directoryPollerManager, directoryLockManager, synchronisationStatusManager,
                userPermissionDAO);
        this.directoryInstanceLoader = directoryInstanceLoader;
    }

    private RemoteDirectory getImplementation(long directoryId) throws DirectoryInstantiationException, DirectoryNotFoundException
    {
        return directoryInstanceLoader.getDirectory(findDirectoryById(directoryId));
    }



    // Like findGroupById, except that it acts on the RemoteDirectory and returns null instead of
    // throwing GroupNotFoundException.
    @Nullable
    private static Group findGroupOrNull(final RemoteDirectory remoteDirectory, final String groupName)
            throws OperationFailedException
    {
        try
        {
            return remoteDirectory.findGroupByName(groupName);
        }
        catch (GroupNotFoundException gnfe)
        {
            return null;
        }
    }



    private static boolean isUserDirectGroupMember(final RemoteDirectory remoteDirectory, final String username,
            final String groupName) throws OperationFailedException, DirectoryNotFoundException
    {
        return remoteDirectory.isUserDirectGroupMember(username, groupName);
    }


    private static boolean isGroupDirectGroupMember(final RemoteDirectory remoteDirectory, final String childGroup, final String parentGroup)
            throws OperationFailedException, DirectoryNotFoundException
    {
        return remoteDirectory.supportsNestedGroups()
                && !childGroup.equals(parentGroup)
                && remoteDirectory.isGroupDirectGroupMember(childGroup, parentGroup);
    }


    @Override
    public <T> List<T> searchDirectGroupRelationships(final long directoryId, final MembershipQuery<T> query)
            throws OperationFailedException, DirectoryNotFoundException
    {
        return searchDirectGroupRelationships(getImplementation(directoryId), query);
    }

    private static <T> List<T> searchDirectGroupRelationships(final RemoteDirectory remoteDirectory, final MembershipQuery<T> query)
            throws OperationFailedException, DirectoryNotFoundException
    {
        if (isNestedGroupQuery(query) && !remoteDirectory.supportsNestedGroups())
        {
            return Collections.emptyList();
        }

        if (isLegacyQuery(query))
        {
            return Collections.emptyList();
        }

        return remoteDirectory.searchGroupRelationships(query);
    }

    private static <T> boolean isNestedGroupQuery(MembershipQuery<T> query)
    {
        return query.getEntityToMatch().getEntityType() == Entity.GROUP
                && query.getEntityToReturn().getEntityType() == Entity.GROUP;
    }

    private static <T> boolean isLegacyQuery(MembershipQuery<T> query)
    {
        return isLegacyRole(query.getEntityToMatch()) || isLegacyRole(query.getEntityToReturn());
    }

    private static boolean isLegacyRole(EntityDescriptor entity)
    {
        return entity.getEntityType() == Entity.GROUP && entity.getGroupType() == GroupType.LEGACY_ROLE;
    }


    @Override
    public boolean isUserNestedGroupMember(final long directoryId, final String username, final String groupName)
            throws OperationFailedException, DirectoryNotFoundException
    {
        final RemoteDirectory remoteDirectory = getImplementation(directoryId);
        if (remoteDirectory.supportsNestedGroups())
        {
            return isUserNestedGroupMember(remoteDirectory, username, groupName, new HashSet<String>(64));
        }
        else
        {
            return isUserDirectGroupMember(remoteDirectory, username, groupName);
        }
    }

    private static boolean isUserNestedGroupMember(final RemoteDirectory remoteDirectory, final String username,
            final String groupName, final Set<String> visitedGroups) throws OperationFailedException, DirectoryNotFoundException
    {
        if (!visitedGroups.add(toLowerCase(groupName)))
        {
            // Refuse to re-enter a cyclic group relationship
            return false;
        }

        // first check if the user is a direct member
        // if he's not a direct member, then check if he's a nested member of any of the subgroups (depth first)
        return isUserDirectGroupMember(remoteDirectory, username, groupName) ||
                isUserChildGroupMember(remoteDirectory, username, groupName, visitedGroups);
    }

    private static boolean isUserChildGroupMember(final RemoteDirectory remoteDirectory, final String username,
            final String groupName, final Set<String> visitedGroups) throws OperationFailedException, DirectoryNotFoundException
    {
        final List<Group> subGroups = searchDirectGroupRelationships(remoteDirectory,
                queryFor(Group.class, group())
                        .childrenOf(group())
                        .withName(groupName)
                        .returningAtMost(EntityQuery.ALL_RESULTS));

        for (Group childGroup : subGroups)
        {
            if (isUserNestedGroupMember(remoteDirectory, username, childGroup.getName(), visitedGroups))
            {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isGroupNestedGroupMember(final long directoryId, final String childGroup, final String parentGroup)
            throws OperationFailedException, DirectoryNotFoundException
    {
        return isGroupNestedGroupMember(getImplementation(directoryId), childGroup, parentGroup);
    }

    private static boolean isGroupNestedGroupMember(final RemoteDirectory remoteDirectory, final String childGroup,
            final String parentGroup) throws OperationFailedException, DirectoryNotFoundException
    {
        if (childGroup.equals(parentGroup))
        {
            return false;
        }

        if (remoteDirectory.supportsNestedGroups())
        {
            return isGroupNestedGroupMember(remoteDirectory, childGroup, parentGroup, new HashSet<String>(64));
        }

        return isGroupDirectGroupMember(remoteDirectory, childGroup, parentGroup);
    }

    private static boolean isGroupNestedGroupMember(final RemoteDirectory remoteDirectory, final String childGroupName,
            final String parentGroupName, final Set<String> visitedGroups) throws OperationFailedException, DirectoryNotFoundException
    {
        if (!visitedGroups.add(toLowerCase(parentGroupName)))
        {
            // cycled around and still haven't been able to prove membership
            return false;
        }

        // first check if the child group is a direct member
        return isGroupDirectGroupMember(remoteDirectory, childGroupName, parentGroupName)
                || isGroupChildGroupMember(remoteDirectory, childGroupName, parentGroupName, visitedGroups);
    }

    private static boolean isGroupChildGroupMember(final RemoteDirectory remoteDirectory, final String childGroupName,
            final String parentGroupName, final Set<String> visitedGroups) throws OperationFailedException, DirectoryNotFoundException
    {
        // if it's not a direct member, then check if it's a nested member of any of the subgroups (depth first)
        List<Group> subGroups = searchDirectGroupRelationships(remoteDirectory, queryFor(Group.class, group())
                .childrenOf(group())
                .withName(parentGroupName)
                .returningAtMost(EntityQuery.ALL_RESULTS));

        for (Group childGroup : subGroups)
        {
            if (isGroupNestedGroupMember(remoteDirectory, childGroupName, childGroup.getName(), visitedGroups))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> searchNestedGroupRelationships(final long directoryId, final MembershipQuery<T> query)
            throws OperationFailedException, DirectoryNotFoundException
    {
        return searchNestedGroupRelationships(getImplementation(directoryId), query);
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> searchNestedGroupRelationships(final RemoteDirectory remoteDirectory,
            final MembershipQuery<T> query) throws OperationFailedException, DirectoryNotFoundException
    {
        if (!remoteDirectory.supportsNestedGroups())
        {
            return searchDirectGroupRelationships(remoteDirectory, query);
        }

        List<? extends DirectoryEntity> relations = getRelatedDirectoryEntities(remoteDirectory, query);
        relations = constrainResults(relations, query.getStartIndex(), query.getMaxResults());

        //noinspection ObjectEquality
        if (query.getReturnType() == String.class) // as name
        {
            return (List<T>) SearchResultsUtil.convertEntitiesToNames(relations);
        }

        return (List<T>)relations;
    }


    private static <T> List<? extends DirectoryEntity> getRelatedDirectoryEntities(final RemoteDirectory remoteDirectory,
            final MembershipQuery<T> query) throws OperationFailedException, DirectoryNotFoundException
    {
        int totalResults = query.getStartIndex() + query.getMaxResults();
        if (query.getMaxResults() == EntityQuery.ALL_RESULTS)
        {
            totalResults = EntityQuery.ALL_RESULTS;
        }

        if (query.isFindChildren())
        {
            return getChildDirectoryEntities(remoteDirectory, query, totalResults);
        }
        return getParentDirectoryEntities(remoteDirectory, query, totalResults);
    }

    private static <T> List<? extends DirectoryEntity> getParentDirectoryEntities(final RemoteDirectory remoteDirectory,
            final MembershipQuery<T> query, final int totalResults) throws OperationFailedException, DirectoryNotFoundException
    {
        List<? extends DirectoryEntity> relations;// find memberships
        if (query.getEntityToReturn().getEntityType() != Entity.GROUP)
        {
            throw new IllegalArgumentException("You can only find the GROUP memberships of USER or GROUP");
        }

        if (query.getEntityToMatch().getEntityType() == Entity.USER)
        {
            // query is to find GROUP memberships of USER
            return findNestedGroupMembershipsOfUser(remoteDirectory, query.getEntityNameToMatch(), query.getEntityToReturn().getGroupType(), totalResults);
        }

        if (query.getEntityToMatch().getEntityType() == Entity.GROUP)
        {
            // query is to find GROUP memberships of GROUP
            return findNestedGroupMembershipsOfGroup(remoteDirectory, query.getEntityNameToMatch(),
                    query.getEntityToReturn().getGroupType(), totalResults);
        }

        throw new IllegalArgumentException("You can only find the GROUP memberships of USER or GROUP");
    }



    private static <T> List<? extends DirectoryEntity> getChildDirectoryEntities(final RemoteDirectory remoteDirectory,
            final MembershipQuery<T> query, final int totalResults) throws OperationFailedException, DirectoryNotFoundException
    {
        if (query.getEntityToMatch().getEntityType() != Entity.GROUP)
        {
            throw new IllegalArgumentException("You can only find the GROUP or USER members of a GROUP");
        }

        if (query.getEntityToReturn().getEntityType() == Entity.USER)
        {
            // query is to find USER members of GROUP
            return findNestedUserMembersOfGroup(remoteDirectory, query.getEntityNameToMatch(), query.getEntityToMatch().getGroupType(), totalResults);
        }

        if (query.getEntityToReturn().getEntityType() == Entity.GROUP)
        {
            // query is to find GROUP members of GROUP
            return findNestedGroupMembersOfGroup(remoteDirectory, query.getEntityNameToMatch(), query.getEntityToMatch().getGroupType(), totalResults);
        }

        throw new IllegalArgumentException("You can only find the GROUP or USER members of a GROUP");
    }



    private static List<Group> findNestedGroupMembershipsOfGroup(final RemoteDirectory remoteDirectory,
            final String groupName, final GroupType groupType, final int maxResults)
            throws OperationFailedException, DirectoryNotFoundException
    {
        final Group group = findGroupOrNull(remoteDirectory, groupName);
        if (group == null)
        {
            return Collections.emptyList();
        }

        return findNestedGroupMembershipsIncludingGroups(remoteDirectory, asList(group), groupType, maxResults, false);
    }

    private static List<Group> findNestedGroupMembershipsIncludingGroups(final RemoteDirectory remoteDirectory,
            final List<Group> groups, final GroupType groupType, final int maxResults, boolean includeOriginal)
            throws OperationFailedException, DirectoryNotFoundException
    {
        final Queue<Group> groupsToVisit = new LinkedList<Group>();
        Set<Group> nestedParents = new LinkedHashSet<Group>(64);

        groupsToVisit.addAll(groups);

        // Should the original groups be included in the results?
        int totalResults = maxResults;
        if (maxResults != EntityQuery.ALL_RESULTS && !includeOriginal)
        {
            totalResults = maxResults + groups.size();
        }

        // now find the nested parents of the direct group memberships (similar to findNestedGroupMembershipsOfGroup)
        // keep iterating while there are more groups to explore AND (we are searching for everything OR we haven't found enough results)
        while (!groupsToVisit.isEmpty() && (totalResults == EntityQuery.ALL_RESULTS || nestedParents.size() < totalResults))
        {
            Group groupToVisit = groupsToVisit.remove();

            // avoid cycles
            if (nestedParents.add(groupToVisit))
            {
                // find direct parent groups
                List<Group> directParents = searchDirectGroupRelationships(remoteDirectory,
                        queryFor(Group.class, group(groupType))
                                .parentsOf(group(groupType))
                                .withName(groupToVisit.getName())
                                .returningAtMost(maxResults) );

                // visit them later
                groupsToVisit.addAll(directParents);
            }
        }

        if (!includeOriginal)
        {
            nestedParents.removeAll(groups);
        }
        return new ArrayList<Group>(nestedParents);
    }

    private static List<Group> findNestedGroupMembershipsOfUser(final RemoteDirectory remoteDirectory,
            final String username, GroupType groupType, final int maxResults)
            throws OperationFailedException, DirectoryNotFoundException
    {
        List<Group> directGroupMemberships = searchDirectGroupRelationships(remoteDirectory,
                queryFor(Group.class, group(groupType))
                        .parentsOf(EntityDescriptor.user())
                        .withName(username)
                        .returningAtMost(maxResults));

        return findNestedGroupMembershipsIncludingGroups(remoteDirectory, directGroupMemberships, groupType, maxResults, true);
    }

    private static List<Group> findNestedGroupMembersOfGroup(final RemoteDirectory remoteDirectory, final String groupName,
            final GroupType groupType, final int maxResults) throws OperationFailedException, DirectoryNotFoundException
    {
        final Group group = findGroupOrNull(remoteDirectory, groupName);
        if (group == null)
        {
            return Collections.emptyList();
        }

        final Queue<Group> groupsToVisit = new LinkedList<Group>();
        final Set<Group> nestedMembers = new LinkedHashSet<Group>(64);

        groupsToVisit.add(group);

        // keep iterating while there are more groups to explore AND (we are searching for everything OR we haven't found enough results)
        while (!groupsToVisit.isEmpty() && (maxResults == EntityQuery.ALL_RESULTS || nestedMembers.size() < maxResults + 1))
        {
            Group groupToVisit = groupsToVisit.remove();

            // avoid cycles
            if (nestedMembers.add(groupToVisit))
            {
                // find direct subgroups
                List<Group> directMembers = searchDirectGroupRelationships(remoteDirectory,
                        queryFor(Group.class, group(groupType))
                                .childrenOf(group(groupType))
                                .withName(groupToVisit.getName())
                                .returningAtMost(maxResults) );

                // visit them later
                groupsToVisit.addAll(directMembers);
            }
        }

        // remove the original group we are finding the members of (this will be in the nested members set to prevent cycles)
        nestedMembers.remove(group);

        return new ArrayList<Group>(nestedMembers);
    }

    private static List<User> findNestedUserMembersOfGroup(final RemoteDirectory remoteDirectory, final String groupName,
            final GroupType groupType, final int maxResults) throws OperationFailedException, DirectoryNotFoundException
    {
        final Group group = findGroupOrNull(remoteDirectory, groupName);
        if (group == null)
        {
            return Collections.emptyList();
        }

        final Queue<Group> groupsToVisit = new LinkedList<Group>();
        final Set<Group> nestedGroupMembers = new LinkedHashSet<Group>(64);
        final Set<User> nestedUserMembers = new LinkedHashSet<User>(64);

        groupsToVisit.add(group);

        // keep iterating while there are more groups to explore AND (we are searching for everything OR we haven't found enough results)
        while (!groupsToVisit.isEmpty() && (maxResults == EntityQuery.ALL_RESULTS || nestedUserMembers.size() < maxResults))
        {
            final Group groupToVisit = groupsToVisit.remove();
            final List<User> directUserMembers = searchDirectGroupRelationships(remoteDirectory,
                    queryFor(User.class, EntityDescriptor.user())
                            .childrenOf(group(groupType))
                            .withName(groupToVisit.getName())
                            .returningAtMost(maxResults) );
            nestedUserMembers.addAll(directUserMembers);

            // avoid cycles
            if (nestedGroupMembers.add(groupToVisit))
            {
                // find direct subgroups
                List<Group> directGroupMembers = searchDirectGroupRelationships(remoteDirectory,
                        queryFor(Group.class, group(groupType))
                                .childrenOf(group(groupType))
                                .withName(groupToVisit.getName())
                                .returningAtMost(EntityQuery.ALL_RESULTS) );

                // visit them later
                groupsToVisit.addAll(directGroupMembers);
            }
        }

        return new ArrayList<User>(nestedUserMembers);
    }
}
