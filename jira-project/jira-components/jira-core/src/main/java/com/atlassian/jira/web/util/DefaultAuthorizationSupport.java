package com.atlassian.jira.web.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.ofbiz.core.entity.GenericValue;

/**
 * Implementation of the authorization checks defined in {@link AuthorizationSupport}
 *
 * @since v4.3
 */
public class DefaultAuthorizationSupport implements AuthorizationSupport
{
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public DefaultAuthorizationSupport(final PermissionManager permissionManager, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public boolean isHasPermission(String permName)
    {
        return hasPermission(Permissions.getType(permName));
    }

    @Override
    public boolean isHasPermission(int permissionsId)
    {
        return hasPermission(permissionsId);
    }

    @Override
    public boolean hasPermission(int permissionsId)
    {
        return permissionManager.hasPermission(permissionsId, jiraAuthenticationContext.getUser());
    }

    @Override
    public boolean isHasIssuePermission(String permName, GenericValue issue)
    {
        return isHasIssuePermission(Permissions.getType(permName), issue);
    }

    @Override
    public boolean isHasIssuePermission(int permissionsId, GenericValue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("isHasIssuePermission can not be passed a null issue");
        }
        if (!"Issue".equals(issue.getEntityName()))
        {
            throw new IllegalArgumentException("isHasIssuePermission can only take an Issue: " + issue.getEntityName() + " is not.");
        }
        return permissionManager.hasPermission(permissionsId, issue, jiraAuthenticationContext.getLoggedInUser());
    }

    @Override
    public boolean hasIssuePermission(int permissionsId, Issue issue)
    {
        return permissionManager.hasPermission(permissionsId, issue, jiraAuthenticationContext.getLoggedInUser());
    }

    @Override
    public boolean isHasProjectPermission(String permName, GenericValue project)
    {
        return isHasProjectPermission(Permissions.getType(permName), project);
    }

    @Override
    public boolean isHasProjectPermission(int permissionsId, GenericValue project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("isHasProjectPermission can not be passed a null project");
        }
        if (!"Project".equals(project.getEntityName()))
        {
            throw new IllegalArgumentException("isHasProjectPermission can only take a Project: " + project.getEntityName() + " is not.");
        }
        return permissionManager.hasPermission(permissionsId, project, jiraAuthenticationContext.getLoggedInUser());
    }

    @Override
    public boolean hasProjectPermission(int permissionsId, Project project)
    {
        return permissionManager.hasPermission(permissionsId, project, jiraAuthenticationContext.getUser());
    }

    @Override
    @Deprecated
    public boolean isHasPermission(String permName, GenericValue entity)
    {
        // log.warn("@deprecated Please use either isHasIssuePermission or isHasProjectPermission\n" + "Called from " + this.getClass().getName());
        return permissionManager.hasPermission(Permissions.getType(permName), entity, jiraAuthenticationContext.getLoggedInUser());
    }

}