package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Checks if the user is an admin or can see atleast one project with this {@link AbstractPermissionCondition#permission}
 */
public class UserIsAdminOrHasVisibleProjectsCondition extends AbstractPermissionCondition
{
    public UserIsAdminOrHasVisibleProjectsCondition(PermissionManager permissionManager)
    {
        super(permissionManager);
    }

    public boolean shouldDisplay(ApplicationUser user, JiraHelper jiraHelper)
    {
        try
        {
            return permissionManager.hasProjects(permission, user) || permissionManager.hasPermission(Permissions.ADMINISTER, user);
        }
        catch (Exception e)
        {
            return false;
        }
    }
}