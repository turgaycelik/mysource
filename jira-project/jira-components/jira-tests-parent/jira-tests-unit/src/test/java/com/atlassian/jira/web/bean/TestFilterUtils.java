/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserUtil;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFilterUtils
{

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private UserUtil userUtil;

    @Mock
    @AvailableInContainer
    private GroupManager groupManager;

    @Mock
    @AvailableInContainer
    private PermissionManager permissionManager;

    @Mock
    private User user;

    @Before
    public void setUp()
    {
        when(user.getName()).thenReturn("bob");
    }

    @Test
    public void usersShouldSeeTheirOwnGroups() throws Exception
    {
        // given:
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        // when:
        FilterUtils.getGroups(user);

        // then:
        // 1. All grups were not accessed:
        Mockito.verifyZeroInteractions(groupManager);
        // 2. Users' groups were queried:
        Mockito.verify(userUtil).getGroupNamesForUser("bob");
        Mockito.verifyNoMoreInteractions(userUtil);
    }

    @Test
    public void adminsShouldSeeAllGroups() throws Exception
    {
        // given:
        final Group group = mock(Group.class);
        when(group.getName()).thenReturn("someGroup");
        final List<Group> groups = ImmutableList.of(group);

        when(groupManager.getAllGroups()).thenReturn(groups);

        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        // when:
        final Collection<String> result = FilterUtils.getGroups(user);

        // then:
        assertEquals(Collections.singletonList("someGroup"), result);
    }
}
