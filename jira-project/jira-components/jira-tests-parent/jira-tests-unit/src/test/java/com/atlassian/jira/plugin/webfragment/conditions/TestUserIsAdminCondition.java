package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestUserIsAdminCondition
{
    @Mock private ApplicationUser user;
    @Mock private PermissionManager permissionManager;
    private UserIsAdminCondition condition;

    @Test
    public void testAllowsDisplayWhenHasAdministerPermission()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        condition = new UserIsAdminCondition(permissionManager);
        assertTrue(condition.shouldDisplay(user, null));
    }

    @Test
    public void testDeniesDisplayWhenNoAdministerPermission()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        condition = new UserIsAdminCondition(permissionManager);
        assertFalse(condition.shouldDisplay(user, null));
    }
}
