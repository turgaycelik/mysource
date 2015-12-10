package com.atlassian.jira.web.bean;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * This bean class allows for "concise" permission checks to be made.  Its therefore useful in
 * jsp/velocity contexts for check that the user has permission to see something and hence
 * the UI can be adjusted appropriately.
 *
 * @since v3.12
 */
public class PermissionCheckBean
{
    private JiraAuthenticationContext authenticationContext;
    private PermissionManager permissionManager;

    public PermissionCheckBean(JiraAuthenticationContext authContext, PermissionManager permissionManager)
    {
        if (authContext == null)
        {
            throw new IllegalArgumentException("JiraAuthenticationContext must not be null");
        }
        if (permissionManager == null)
        {
            throw new IllegalArgumentException("PermissionManager must not be null");
        }
        this.authenticationContext = authContext;
        this.permissionManager = permissionManager;
    }

    public PermissionCheckBean()
    {
        this(ComponentAccessor.getJiraAuthenticationContext(), ComponentAccessor.getPermissionManager());
    }

    /**
     * Returns true if the user has permission to {@link Permissions#BROWSE} the issue
     *
     * @param issue the issue in question to be checked
     * @return true if the user has permission to {@link Permissions#BROWSE} the issue.
     * @throws IllegalArgumentException if the issue object is nhull
     */
    public boolean isIssueVisible(Issue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("The issue must not be null!");
        }
        return permissionManager.hasPermission(Permissions.BROWSE, issue, authenticationContext.getLoggedInUser());
    }

    /**
     * Tells whether the configured user (if any) has admin permission.
     * @return true only if the user is an administrator.
     */
    public boolean isAdmin()
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser());
    }
}
