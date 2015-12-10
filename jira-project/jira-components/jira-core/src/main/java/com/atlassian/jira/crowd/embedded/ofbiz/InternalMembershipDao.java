package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.util.BatchResult;

/**
 * This interface is used by OfBizDelegatingMembershipDao to avoid circular dependencies with the User and Group DAOs.
 *
 *
 */
public interface InternalMembershipDao
{
    boolean isUserDirectMember(long directoryId, String userName, String groupName);

    boolean isGroupDirectMember(long directoryId, String childGroup, String parentGroup);

    void addUserToGroup(long directoryId, UserOrGroupStub user, UserOrGroupStub group) throws MembershipAlreadyExistsException;

    BatchResult<String> addAllUsersToGroup(long directoryId, Collection<UserOrGroupStub> users, UserOrGroupStub group);

    void addGroupToGroup(long directoryId, UserOrGroupStub child, UserOrGroupStub parent);

    void removeAllMembersFromGroup(Group group);

    void removeAllGroupMemberships(Group group);

    void removeAllUserMemberships(User user);

    void removeAllUserMemberships(long directoryId, String username);

    void removeUserFromGroup(long directoryId, UserOrGroupStub user, UserOrGroupStub group) throws MembershipNotFoundException;

    void removeGroupFromGroup(long directoryId, UserOrGroupStub childGroup, UserOrGroupStub parentGroup)
            throws MembershipNotFoundException;

    <T> List<String> search(long directoryId, MembershipQuery<T> query);

    void flushCache();
}
