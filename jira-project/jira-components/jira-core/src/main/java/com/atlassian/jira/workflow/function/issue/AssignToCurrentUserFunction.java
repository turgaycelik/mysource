package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.Map;

/**
 * Assigns the issue to the current user.
 *
 * @since 3.12
 */
public class AssignToCurrentUserFunction extends AbstractJiraFunctionProvider
{
    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        final MutableIssue issue = getIssue(transientVars);
        final ApplicationUser currentUser = getCallerUser(transientVars, args);
        final PermissionManager permissionManager = getPermissionManager();
        //Check that there's as a logged in user and that the user has both the assign issue and assignable
        // user permission.
        if (currentUser != null &&
            permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, issue, currentUser))
        {
            issue.setAssignee(ApplicationUsers.toDirectoryUser(currentUser));
        }
    }

    PermissionManager getPermissionManager()
    {
        return ComponentAccessor.getPermissionManager();
    }
}
