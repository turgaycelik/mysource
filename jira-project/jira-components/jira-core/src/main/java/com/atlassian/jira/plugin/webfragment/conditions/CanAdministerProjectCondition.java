package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.web.Condition;
import com.google.common.annotations.VisibleForTesting;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Checks that the current user is a project admin for the passed in Project, Version or Component
 *
 * @since v5.0
 */
public class CanAdministerProjectCondition implements Condition
{
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authContext;
    private final ProjectManager projectManager;

    public CanAdministerProjectCondition(PermissionManager permissionManager, JiraAuthenticationContext authContext, ProjectManager projectManager)
    {
        this.permissionManager = permissionManager;
        this.authContext = authContext;
        this.projectManager = projectManager;
    }

    @Override
    public void init(Map<String, String> params)
    {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        final User user = authContext.getLoggedInUser();
        final Project project = getProject(context);

        if (project == null)
        {
            return false;
        }

        final HttpServletRequest request = getRequest();
        if (request == null)
        {
            return hasPermission(user, project);
        }

        final String name = user == null ? "" : user.getName();
        final String cacheKey = String.format("atl.jira.permission.request.cache:%s:%s:%s",
                Permissions.PROJECT_ADMIN, name, project.getKey());

        final Object cachedPermission = request.getAttribute(cacheKey);
        if (cachedPermission == null || !(cachedPermission instanceof Boolean))
        {
            final boolean permission = hasPermission(user, project);
            request.setAttribute(cacheKey, permission);
            return permission;
        }
        else
        {
            return (Boolean) cachedPermission;
        }
    }

    @VisibleForTesting
    HttpServletRequest getRequest()
    {
        return ExecutingHttpRequest.get();
    }

    private boolean hasPermission(User user, Project project)
    {
        try
        {
            return permissionManager.hasPermission(Permissions.ADMINISTER, user)
                    || permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user);

        }
        catch (Exception e)
        {
            return false;
        }

    }

    private Project getProject(Map<String, Object> context)
    {
        if (context.containsKey("project"))
        {
            return (Project) context.get("project");
        }
        if (context.containsKey("issue"))
        {
            return ((Issue) context.get("issue")).getProjectObject();
        }
        if (context.containsKey("helper"))
        {
            JiraHelper helper = (JiraHelper) context.get("helper");
            if (helper.getProjectObject() != null)
            {
                return helper.getProjectObject();
            }

        }
        if (context.containsKey("version"))
        {
            return ((Version) context.get("version")).getProjectObject();
        }
        if (context.containsKey("component"))
        {
            final Long projectId = ((ProjectComponent) context.get("component")).getProjectId();
            return projectManager.getProjectObj(projectId);
        }

        return null;
    }
}
