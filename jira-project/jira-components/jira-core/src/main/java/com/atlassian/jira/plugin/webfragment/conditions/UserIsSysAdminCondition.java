package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * Will return shouldDisplay of true if the user has the {@link Permissions#SYSTEM_ADMIN} global permission.
 *
 * @since v3.12
 */
public class UserIsSysAdminCondition extends AbstractJiraCondition
{
    private final PermissionManager permissionManager;

    public UserIsSysAdminCondition(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    /**
     * Returns true if the user is logged in and has system admin rights.
     *
     * @param user       current user
     * @param jiraHelper JIRA helper - not used
     * @return true if user has the {@link com.atlassian.jira.security.Permissions#SYSTEM_ADMIN} permission.
     */
    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }
}
