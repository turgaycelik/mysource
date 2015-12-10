package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Checks if this user has the global admin permission.  This was previously
 * implemented in jira-core but has been moved into the API for 6.1.
 *
 * @since 6.1
 */
@PublicApi
public class UserIsAdminCondition extends AbstractWebCondition
{
    private final PermissionManager permissionManager;

    @Internal
    public UserIsAdminCondition(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    public boolean shouldDisplay(ApplicationUser user, JiraHelper jiraHelper)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, user);
    }
}
