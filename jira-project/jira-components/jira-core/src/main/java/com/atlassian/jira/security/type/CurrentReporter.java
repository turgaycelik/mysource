/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CurrentReporter extends SimpleIssueFieldSecurityType
{
    public static final String DESC = "reporter";
    private JiraAuthenticationContext jiraAuthenticationContext;

    public CurrentReporter(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.permission.types.reporter");
    }

    public String getType()
    {
        return DESC;
    }

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
    {
        // No specific validation
    }

    protected String getFieldName(String parameter)
    {
        // Parameter not used
        return DocumentConstants.ISSUE_AUTHOR;
    }

    @Override
    protected boolean hasProjectPermission(com.atlassian.crowd.embedded.api.User user, boolean issueCreation, GenericValue project)
    {
        return true;
    }

    @Override
    protected boolean hasProjectPermission(User user, boolean issueCreation, Project project)
    {
        return true;
    }

    protected String getField()
    {
        return DESC;
    }

    @Override
    protected String getFieldValue(Issue issue)
    {
        return issue.getReporterId();
    }

    public Set<User> getUsers(PermissionContext ctx, String ignored)
    {
        if (ctx.getIssue() != null && ctx.getIssue().getReporter() != null)
        {
            return Collections.singleton(ctx.getIssue().getReporter());
        }
        return Collections.emptySet();
    }
}
