package com.atlassian.jira.rest.internal;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserUtil;

import java.util.TimeZone;

import static org.mockito.Mockito.when;

public class PermissionHelper
{
    private PermissionManager permissionManager;

    private UserUtil userUtil;

    private TimeZoneManager timeZoneManager;

    private JiraAuthenticationContext authContext;

    public PermissionHelper(final PermissionManager permissionManager, final UserUtil userUtil, JiraAuthenticationContext authContext)
    {
        this.permissionManager = permissionManager;
        this.userUtil = userUtil;
        this.authContext = authContext;
    }

    public PermissionHelper(final PermissionManager permissionManager, final UserUtil userUtil, JiraAuthenticationContext authContext, final TimeZoneManager timeZoneManager)
    {
        this.permissionManager = permissionManager;
        this.userUtil = userUtil;
        this.authContext = authContext;
        this.timeZoneManager = timeZoneManager;
    }

    public void setTimeZoneManager(final TimeZoneManager timeZoneManager)
    {
        this.timeZoneManager = timeZoneManager;
    }

    public MockApplicationUser configureJiraUser(final String username, final Permission permission)
    {
        final String key = "key-" + username;
        final MockApplicationUser user = new MockApplicationUser(key, username);

        final boolean isAdmin = Permission.ADMIN.equals(permission) || Permission.SYSADMIN.equals(permission);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(isAdmin);

        if (timeZoneManager != null)
        {
            when(timeZoneManager.getTimeZoneforUser(user.getDirectoryUser())).thenReturn(TimeZone.getTimeZone("Europe/Warsaw"));
        }

        final boolean isSysadmin = Permission.SYSADMIN.equals(permission);
        when(permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)).thenReturn(isSysadmin);

        when(userUtil.getUserByKey(key)).thenReturn(user);
        when(userUtil.getUserByName(username)).thenReturn(user);

        return user;
    }

    public MockApplicationUser configureCurrentLoggedJiraUser(final String username, final PermissionHelper.Permission permission)
    {
        final MockApplicationUser loggedUser = configureJiraUser(username, permission);
        when(authContext.getUser()).thenReturn(loggedUser);

        return loggedUser;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static enum Permission
    {
        NOT_ADMIN,
        ADMIN,
        SYSADMIN
    }
}
