package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Checks if the user has the global permission: {@link AbstractPermissionCondition#permission}
 */
public class JiraGlobalPermissionCondition extends AbstractPermissionCondition
{
    public JiraGlobalPermissionCondition(PermissionManager permissionManager)
    {
        super(permissionManager);
    }

    public boolean shouldDisplay(ApplicationUser user, JiraHelper jiraHelper)
    {
        return permissionManager.hasPermission(permission, user);
    }
}
