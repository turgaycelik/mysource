package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import org.apache.log4j.Logger;

/**
 * Condition to determine whether an issue is a subtask
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class IsSubTaskCondition extends AbstractIssueCondition
{
    private static final Logger log = Logger.getLogger(IsSubTaskCondition.class);

    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
    {
        return issue.isSubTask();
    }

}