package com.atlassian.jira.security.type;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;

import org.ofbiz.core.entity.GenericValue;

public class CurrentAssignee extends SimpleIssueFieldSecurityType
{
    public static final String DESC = "assignee";

    private JiraAuthenticationContext authenticationContext;

    public CurrentAssignee(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public String getDisplayName()
    {
        return authenticationContext.getI18nHelper().getText("admin.permission.types.current.assignee");
    }

    public String getType()
    {
        return DESC;
    }

    public void doValidation(String key, Map<String,String> parameters, JiraServiceContext jiraServiceContext)
    {
        // No specific validation
    }

    protected String getFieldName(String parameter)
    {
        // Parameter not used
        return DocumentConstants.ISSUE_ASSIGNEE;
    }

    /**
     * This should return two different values depending on where it is called from. If we are creating an Issue we want
     * to return FALSE but otherwise TRUE
     */
    @Override
    protected boolean hasProjectPermission(User user, boolean issueCreation, GenericValue project)
    {
        return !issueCreation;
    }

    /**
     * Originally this method was intended to return false while issue creation, otherwise true.
     * Due to JRA-31720 after check for issue creation it returns true only if given user
     * has at least one issue assigned in given project.
     */
    @Override
    protected boolean hasProjectPermission(User user, boolean issueCreation, Project project)
    {
        // If we are creating an Issue we want to return FALSE but otherwise TRUE
        return !issueCreation;
    }

    protected String getField()
    {
        return DESC;
    }

    @Override
    protected String getFieldValue(Issue issue)
    {
        return issue.getAssigneeId();
    }

    public Set<User> getUsers(PermissionContext ctx, String ignored)
    {
        if (ctx.getIssue() != null && ctx.getIssue().getAssignee() != null)
        {
            return Collections.singleton(ctx.getIssue().getAssignee());
        }
        return Collections.emptySet();
    }
}
