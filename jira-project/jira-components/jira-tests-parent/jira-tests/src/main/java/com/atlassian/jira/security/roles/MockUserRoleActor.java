package com.atlassian.jira.security.roles;

import java.util.Collections;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

import com.google.common.collect.ImmutableSet;

public class MockUserRoleActor extends MockRoleActor
{
    private final User user;

    public MockUserRoleActor(Long projectRoleId, Long projectId, ApplicationUser user)
    {
        super(projectRoleId, projectId, user.getName(), UserRoleActorFactory.TYPE);
        this.user = user.getDirectoryUser();
    }

    public Set<User> getUsers()
    {
        return ImmutableSet.of(user);
    }

    public boolean contains(ApplicationUser user)
    {
        return ApplicationUsers.from(this.user).getKey().equals(user.getKey());
    }
}