package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Checks if a project is selected (in {@link JiraHelper}) and if the user has the {@link #permission} for that project
 */
public class HasProjectPermissionCondition extends AbstractPermissionCondition
{
    public HasProjectPermissionCondition(PermissionManager permissionManager)
    {
        super(permissionManager);
    }

    public boolean shouldDisplay(ApplicationUser user, JiraHelper jiraHelper)
    {
        final Project project = jiraHelper.getProjectObject();
        return project != null && permissionManager.hasPermission(permission, project, user);
    }
}
