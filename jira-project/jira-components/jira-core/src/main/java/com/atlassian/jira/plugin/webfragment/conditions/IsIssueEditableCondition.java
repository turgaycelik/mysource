package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import org.apache.log4j.Logger;

/**
 * Condition to check if the issue is currently editable
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class IsIssueEditableCondition extends AbstractIssueCondition
{
    private static final Logger log = Logger.getLogger(IsIssueEditableCondition.class);
    private final IssueManager issueManager;


    public IsIssueEditableCondition(IssueManager issueManager)
    {
        this.issueManager = issueManager;
    }

    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
    {
        return issueManager.isEditable(issue);
    }

}