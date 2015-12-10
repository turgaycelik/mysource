package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Condition to check whether the issue has any sub task types available.
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class HasSubTasksAvailableCondition extends AbstractIssueCondition
{
    private static final Logger log = Logger.getLogger(SubTasksEnabledCondition.class);
    private final FieldManager fieldManager;

    public HasSubTasksAvailableCondition(FieldManager fieldManager)
    {
        this.fieldManager = fieldManager;
    }

    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
    {
        final Collection<Option> availableSubTaskIssueTypes = fieldManager.getIssueTypeField().getOptionsForIssue(issue, true);
        return (availableSubTaskIssueTypes != null) && !availableSubTaskIssueTypes.isEmpty();
    }

}