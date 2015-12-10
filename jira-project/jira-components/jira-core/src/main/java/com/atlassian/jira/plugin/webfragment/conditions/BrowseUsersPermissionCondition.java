package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractJiraCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * Only display a web-fragment if a User has permission to browse/pick users.
 *
 * @since v5.0
 */
public class BrowseUsersPermissionCondition extends AbstractJiraCondition
{
    private PermissionManager permissionManager;

    public BrowseUsersPermissionCondition(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    @Override
    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return permissionManager.hasPermission(Permissions.USER_PICKER, user);
    }
}
