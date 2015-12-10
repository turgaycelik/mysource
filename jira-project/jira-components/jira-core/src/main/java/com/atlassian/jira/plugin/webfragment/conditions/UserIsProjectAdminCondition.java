package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.SessionKeys;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * Checks that the current user is a project admin for at least one project.
 */
public class UserIsProjectAdminCondition extends AbstractJiraCondition
{
    private final PermissionManager permissionManager;

    public UserIsProjectAdminCondition(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    public boolean shouldDisplay(@Nullable User user, @Nullable JiraHelper jiraHelper)
    {
        final HttpServletRequest request = getRequest(jiraHelper);
        if (request == null)
        {
            try
            {
                return permissionManager.hasProjects(Permissions.PROJECT_ADMIN, user);
            }
            catch (Exception e)
            {
                return false;
            }
        }

        try
        {
            Boolean isProjectAdmin = (Boolean) request.getSession().getAttribute(SessionKeys.USER_PROJECT_ADMIN);
            if (isProjectAdmin == null)
            {
                if (user != null)
                {
                    isProjectAdmin = permissionManager.hasProjects(Permissions.PROJECT_ADMIN, user) ? Boolean.TRUE : Boolean.FALSE;
                    request.getSession().setAttribute(SessionKeys.USER_PROJECT_ADMIN, isProjectAdmin);
                }
            }

            return isProjectAdmin != null && isProjectAdmin;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Nullable
    private HttpServletRequest getRequest(@Nullable final JiraHelper jiraHelper)
    {
        return jiraHelper != null ? jiraHelper.getRequest() : null;
    }
}
