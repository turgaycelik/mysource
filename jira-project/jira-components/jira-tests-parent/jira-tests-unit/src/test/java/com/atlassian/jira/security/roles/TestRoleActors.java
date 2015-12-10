/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.security.roles;

import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestRoleActors
{
    @Test
    public void testDefaultRoleActorsAddRoleActor()
    {
        ProjectRole role = new ProjectRoleImpl("name", "description");
        RoleActor mockRoleActor1 = new MockRoleActor(GroupRoleActorFactory.TYPE, "group1");
        assertEquals(mockRoleActor1, new MockRoleActor(GroupRoleActorFactory.TYPE, "group1"));
        DefaultRoleActors roleActors = new DefaultRoleActorsImpl(role.getId(), mockRoleActor1);
        assertNotNull(roleActors.getRoleActors());
        assertEquals(1, roleActors.getRoleActors().size());
        assertTrue(roleActors.getRoleActors().contains(new MockRoleActor(GroupRoleActorFactory.TYPE, "group1")));
        MockRoleActor mockRoleActor2 = new MockRoleActor(UserRoleActorFactory.TYPE, "user1");
        roleActors = roleActors.addRoleActor(mockRoleActor2);
        assertNotNull(roleActors.getRoleActors());
        assertEquals(2, roleActors.getRoleActors().size());
        assertTrue(roleActors.getRoleActors().contains(new MockRoleActor(GroupRoleActorFactory.TYPE, "group1")));
        assertTrue(roleActors.getRoleActors().contains(new MockRoleActor(UserRoleActorFactory.TYPE, "user1")));
    }
}
