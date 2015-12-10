package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestUserIsSysAdminCondition
{
    @Test
    public void testAnonymousUserIsNotSystemAdmin() throws Exception
    {
        final MockControl mockControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mock = (PermissionManager) mockControl.getMock();
        mockControl.expectAndReturn(mock.hasPermission(Permissions.SYSTEM_ADMIN, (User) null), false);
        mockControl.replay();
        assertFalse(new UserIsSysAdminCondition(mock).shouldDisplay(null, null));
        mockControl.verify();
    }

    @Test
    public void testAnonymousUserIsSystemAdmin() throws Exception
    {
        final MockControl mockControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mock = (PermissionManager) mockControl.getMock();
        mockControl.expectAndReturn(mock.hasPermission(Permissions.SYSTEM_ADMIN, (User) null), true);
        mockControl.replay();
        assertTrue(new UserIsSysAdminCondition(mock).shouldDisplay(null, null));
        mockControl.verify();
    }

    @Test
    public void testUserIsNotSystemAdmin() throws Exception
    {
        final User user = new MockUser("name");
        final MockControl mockControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mock = (PermissionManager) mockControl.getMock();
        mockControl.expectAndReturn(mock.hasPermission(Permissions.SYSTEM_ADMIN, user), false);
        mockControl.replay();
        assertFalse(new UserIsSysAdminCondition(mock).shouldDisplay(user, null));
        mockControl.verify();
    }

    @Test
    public void testUserIsSystemAdmin() throws Exception
    {
        final User user = new MockUser("name");
        final MockControl mockControl = MockControl.createControl(PermissionManager.class);
        final PermissionManager mock = (PermissionManager) mockControl.getMock();
        mockControl.expectAndReturn(mock.hasPermission(Permissions.SYSTEM_ADMIN, user), true);
        mockControl.replay();
        assertTrue(new UserIsSysAdminCondition(mock).shouldDisplay(user, null));
        mockControl.verify();
    }

}
