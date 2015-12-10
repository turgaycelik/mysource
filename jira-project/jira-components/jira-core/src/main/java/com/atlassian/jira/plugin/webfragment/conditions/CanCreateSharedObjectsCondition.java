package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * Checks if the logged in user has the rights to create shared objects.
 *
 * @since v4.0
 */
public class CanCreateSharedObjectsCondition extends AbstractJiraCondition
{
    public boolean shouldDisplay(final User user, final JiraHelper jiraHelper)
    {
        return getPermissionManager().hasPermission(Permissions.CREATE_SHARED_OBJECTS, user);
    }

    public PermissionManager getPermissionManager()
    {
        return ComponentAccessor.getPermissionManager();
    }
}