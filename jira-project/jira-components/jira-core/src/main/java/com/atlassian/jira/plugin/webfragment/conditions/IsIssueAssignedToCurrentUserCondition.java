package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Condition to checkif the current user is the assignee of the current issue
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class IsIssueAssignedToCurrentUserCondition extends AbstractIssueWebCondition
{
    public boolean shouldDisplay(ApplicationUser user, Issue issue, JiraHelper jiraHelper)
    {
        return user != null && user.getKey().equals(issue.getAssigneeId());
    }
}