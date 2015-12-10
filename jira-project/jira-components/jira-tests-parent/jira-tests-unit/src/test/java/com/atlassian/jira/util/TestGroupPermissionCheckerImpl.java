package com.atlassian.jira.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;

public class TestGroupPermissionCheckerImpl
{
    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    PermissionManager permissionManagerMock;

    @Mock
    GroupManager groupManagerMock;

    @Mock
    User userMock;

    @Mock
    Group groupMock;

    GroupPermissionCheckerImpl groupPermissionChecker;

    @Before
    public void setUp()
    {
        groupPermissionChecker = new GroupPermissionCheckerImpl(permissionManagerMock, groupManagerMock);
    }

    @Test
    public void testHasViewPermissionWhenAdmin()
    {
        when(permissionManagerMock.hasPermission(Permissions.ADMINISTER, userMock)).thenReturn(Boolean.TRUE);

        assertTrue(groupPermissionChecker.hasViewGroupPermission(groupMock.getName(), userMock));

    }

    @Test
    public void testHasViewPermissionNullUser()
    {
        when(permissionManagerMock.hasPermission(Permissions.ADMINISTER, (User) null)).thenReturn(Boolean.FALSE);

        assertFalse(groupPermissionChecker.hasViewGroupPermission(groupMock.getName(), null));

    }

    @Test
    public void testHasViewPermissionNotInGroup()
    {
        when(permissionManagerMock.hasPermission(Permissions.ADMINISTER, userMock)).thenReturn(Boolean.FALSE);

        assertFalse(groupPermissionChecker.hasViewGroupPermission(groupMock.getName(), userMock));

    }

    @Test
    public void testHasViewPermissionInGroup()
    {
        when(permissionManagerMock.hasPermission(Permissions.ADMINISTER, userMock)).thenReturn(Boolean.FALSE);

        when(groupMock.getName()).thenReturn("gname");

        when(groupManagerMock.getGroup("gname")).thenReturn(groupMock);
        when(groupManagerMock.isUserInGroup(userMock, groupMock)).thenReturn(Boolean.TRUE);

        assertTrue(groupPermissionChecker.hasViewGroupPermission(groupMock.getName(), userMock));

    }
}
