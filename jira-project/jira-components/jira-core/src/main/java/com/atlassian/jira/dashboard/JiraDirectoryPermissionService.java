package com.atlassian.jira.dashboard;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gadgets.directory.spi.DirectoryPermissionService;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;

import javax.annotation.Nullable;

/**
 * Used to determine if a user can modify the gadgets directory.
 *
 * @since v4.3
 */
public class JiraDirectoryPermissionService implements DirectoryPermissionService
{
    private final PermissionManager permissionManager;
    private final UserManager userManager;

    public JiraDirectoryPermissionService(final PermissionManager permissionManager, final UserManager userManager)
    {
        this.permissionManager = permissionManager;
        this.userManager = userManager;
    }

    @Override
    public boolean canConfigureDirectory(@Nullable String username)
    {
        final User user = userManager.getUserObject(username);
        return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }
}
