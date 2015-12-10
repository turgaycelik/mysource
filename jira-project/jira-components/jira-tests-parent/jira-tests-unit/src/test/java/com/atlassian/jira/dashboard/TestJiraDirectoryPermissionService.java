package com.atlassian.jira.dashboard;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;

import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestJiraDirectoryPermissionService
{
    @Test
    public void testCanConfigureDirectory() throws Exception
    {
        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        final UserManager mockUserManager = createMock(UserManager.class);
        final User admin = createMock(User.class);
        final User fred = createMock(User.class);
        expect(mockUserManager.getUserObject(null)).andReturn(null);
        expect(mockUserManager.getUserObject("admin")).andReturn(admin);
        expect(mockUserManager.getUserObject("fred")).andReturn(fred);

        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, (User) null)).andReturn(false);
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, admin)).andReturn(true);
        expect(mockPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, fred)).andReturn(false);
        replay(mockUserManager, mockPermissionManager);

        JiraDirectoryPermissionService permissionService = new JiraDirectoryPermissionService(mockPermissionManager, mockUserManager);
        assertFalse(permissionService.canConfigureDirectory(null));
        assertTrue(permissionService.canConfigureDirectory("admin"));
        assertFalse(permissionService.canConfigureDirectory("fred"));

        verify(mockUserManager, mockPermissionManager);
    }
}
