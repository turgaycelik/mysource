package com.atlassian.jira.security.roles;

import java.util.SortedSet;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

public class MockGroupRoleActor extends MockRoleActor
{
    private final Group group;

    public MockGroupRoleActor(Long projectRoleId, Long projectId, Group group)
    {
        super(projectRoleId, projectId, group.getName(), GroupRoleActorFactory.TYPE);
        this.group = group;
    }

    @Override
    public SortedSet<User> getUsers()
    {
        UserUtil userUtil = ComponentAccessor.getUserUtil();
        return ImmutableSortedSet.copyOf(userUtil.getAllUsersInGroups(ImmutableSet.of(group)));
    }

    @Override
    public boolean contains(User user)
    {
        CrowdService crowdService = ComponentAccessor.getCrowdService();
        return crowdService.isUserMemberOfGroup(user, group);
    }

    @Override
    public boolean contains(ApplicationUser user)
    {
        return contains(user.getDirectoryUser());
    }
}