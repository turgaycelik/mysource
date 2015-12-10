/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;

import com.google.common.annotations.VisibleForTesting;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.security.Permissions.CREATE_ISSUE;
import static com.google.common.collect.Sets.newHashSet;

public class CurrentReporterHasCreatePermission extends SimpleIssueFieldSecurityType
{
    private JiraAuthenticationContext jiraAuthenticationContext;

    public CurrentReporterHasCreatePermission(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.permission.types.current.reporter.has.create.perm");
    }

    public String getType()
    {
        return "reportercreate";
    }

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
    {
        // No specific validation
    }

    /**
     * Is valid for all permissions except "Create Issue".
     * <p/>
     * Because we rely on the permissions for the "Create Issue" function, then not only does it not make sense to add
     * this role to "Create Issue", it would actually cause an infinite loop. see JRA-13315
     * </p>
     *
     * @param permissionKey permission key.
     * @return false for Permissions.CREATE_ISSUE, true otherwise.
     */
    public boolean isValidForPermission(ProjectPermissionKey permissionKey)
    {
        return !ProjectPermissions.CREATE_ISSUES.equals(permissionKey);
    }

    protected String getFieldName(String parameter)
    {
        // Parameter not used
        return DocumentConstants.ISSUE_AUTHOR;
    }

    @Override
    protected boolean hasProjectPermission(User user, boolean issueCreation, GenericValue gvProject)
    {
        return getPermissionManager().hasPermission(CREATE_ISSUE, gvProject, user, issueCreation);
    }

    @Override
    protected boolean hasProjectPermission(User user, boolean issueCreation, Project project)
    {
        return getPermissionManager().hasPermission(CREATE_ISSUE, project, user, issueCreation);
    }

    @VisibleForTesting
    PermissionManager getPermissionManager()
    {
        return ComponentAccessor.getPermissionManager();
    }


    protected String getField()
    {
        return "reporter";
    }

    @Override
    public Set<User> getUsers(final PermissionContext ctx, final String ignored)
    {
        final Issue issue = ctx.getIssue();
        if (issue != null && issue.getReporter() != null && getPermissionManager().hasPermission(CREATE_ISSUE, issue.getReporter()))
        {
            return newHashSet(issue.getReporter());
        }
        return newHashSet();
    }

    @Override
    protected String getFieldValue(Issue issue)
    {
        return issue.getReporterId();
    }
}
