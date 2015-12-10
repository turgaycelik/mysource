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

import static com.atlassian.jira.security.Permissions.ASSIGNABLE_USER;
import static com.google.common.collect.Sets.newHashSet;

public class CurrentAssigneeHasAssignablePermission extends SimpleIssueFieldSecurityType
{
    private JiraAuthenticationContext authenticationContext;

    public CurrentAssigneeHasAssignablePermission(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.authenticationContext = jiraAuthenticationContext;
    }

    public String getDisplayName()
    {
        return authenticationContext.getI18nHelper().getText("admin.permission.types.current.assignee.has.assignable.perm");
    }

    public String getType()
    {
        return "assigneeassignable";
    }

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
    {
        // No specific validation
    }

    protected String getFieldName(String parameter)
    {
        // Parameter not used
        return DocumentConstants.ISSUE_ASSIGNEE;
    }

    /**
     * Is valid for all permissions except "Assignable".
     * <p/>
     * Because we rely on the permissions for the "Assignable" function, then not only does it not make
     * sense to add this role to "Assignable", it would actually cause an infinite loop. see JRA-13315
     * </p>
     *
     * @param permissionKey permission key.
     * @return false for Permissions.ASSIGNABLE_USER, true otherwise.
     */
    public boolean isValidForPermission(ProjectPermissionKey permissionKey)
    {
        return !ProjectPermissions.ASSIGNABLE_USER.equals(permissionKey);
    }

    @Override
    protected boolean hasProjectPermission(com.atlassian.crowd.embedded.api.User user, boolean issueCreation, GenericValue project)
    {
        return getPermissionManager().hasPermission(ASSIGNABLE_USER, project, user, issueCreation);
    }

    @Override
    protected boolean hasProjectPermission(User user, boolean issueCreation, Project project)
    {
        return getPermissionManager().hasPermission(ASSIGNABLE_USER, project, user, issueCreation);
    }

    @VisibleForTesting
    PermissionManager getPermissionManager()
    {
        return ComponentAccessor.getPermissionManager();
    }

    protected String getField()
    {
        return "assignee";
    }

    @Override
    public Set<User> getUsers(final PermissionContext ctx, final String ignored)
    {
        final Issue issue = ctx.getIssue();
        if (issue != null && issue.getAssignee() != null && getPermissionManager().hasPermission(ASSIGNABLE_USER, issue.getAssignee()))
        {
            return newHashSet(issue.getAssignee());
        }
        return newHashSet();
    }

    @Override
    protected String getFieldValue(Issue issue)
    {
        return issue.getAssigneeId();
    }
}
