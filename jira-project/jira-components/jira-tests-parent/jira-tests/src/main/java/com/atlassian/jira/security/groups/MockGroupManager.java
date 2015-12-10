package com.atlassian.jira.security.groups;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MultiMap;
import com.atlassian.jira.util.collect.MultiMaps;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * @since v4.3
 */
public class MockGroupManager implements GroupManager
{
    private Map<String, Group> groupMap = new HashMap<String, Group>();
    private MultiMap<String, User, Set<User>> membershipMap = MultiMaps.createSetMultiMap();
    private MultiMap<String, Group, Set<Group>> userToGroups = MultiMaps.createSetMultiMap();

    @Override
    public Collection<Group> getAllGroups()
    {
        return groupMap.values();
    }

    public boolean groupExists(final String groupname)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Group createGroup(String groupName)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Group getGroup(final String groupname)
    {
        return groupMap.get(groupname);
    }

    @Override
    public Group getGroupEvenWhenUnknown(String groupName)
    {
        if (groupMap.containsKey(groupName))
        {
            return groupMap.get(groupName);
        }
        return new ImmutableGroup(groupName);
    }

    @Override
    public Group getGroupObject(String groupName)
    {
        return groupMap.get(groupName);
    }

    @Override
    public boolean isUserInGroup(final String username, final String groupname)
    {
        final Set<User> members = membershipMap.get(groupname);
        return members != null && Collections2.transform(members, new Function<User, String>()
        {
            @Override
            public String apply(@Nullable final User input)
            {
                return input.getName();
            }
        }).contains(username);
    }

    @Override
    public boolean isUserInGroup(final User user, final Group group)
    {
        final Set<User> members = membershipMap.get(group.getName());
        return members != null && Collections2.transform(members, new Function<User, String>()
        {
            @Override
            public String apply(@Nullable final User input)
            {
                return input.getName();
            }
        }).contains(user.getName());
    }

    public Collection<User> getUsersInGroup(final String groupName)
    {
        return membershipMap.get(groupName);
    }

    @Override
    public Collection<User> getUsersInGroup(Group group)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<String> getUserNamesInGroup(Group group)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<String> getUserNamesInGroup(String groupName)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<User> getDirectUsersInGroup(Group group)
    {
        return getUsersInGroup(group);
    }

    public Collection<Group> getGroupsForUser(final String userName)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Collection<Group> getGroupsForUser(final User user)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Collection<String> getGroupNamesForUser(final String userName)
    {
        final Set<Group> groups = userToGroups.get(userName);
        if (groups == null)
        {
            return Collections.emptySet();
        }
        return ImmutableList.copyOf(Ordering.natural().sortedCopy(Collections2.transform(groups, new Function<Group, String>()
        {
            @Override
            public String apply(@Nullable final Group input)
            {
                return input.getName();
            }
        })));
    }

    public Collection<String> getGroupNamesForUser(final User user)
    {
        return getGroupNamesForUser(user.getName());
    }

    @Override
    public Collection<String> getGroupNamesForUser(final ApplicationUser user)
    {
        return getGroupNamesForUser(user.getName());
    }

    @Override
    public void addUserToGroup(User user, Group group)
    {
        groupMap.put(group.getName(), group);
        membershipMap.putSingle(group.getName(), user);
        userToGroups.putSingle(user.getName(), group);
    }

    public void addGroup(String groupName)
    {
        groupMap.put(groupName, new ImmutableGroup(groupName));
    }

    public void addMember(String groupName, String userName)
    {
        Group group = new MockGroup(groupName);
        User user = new MockUser(userName);
        addUserToGroup(user, group);
    }
}
