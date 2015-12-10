package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.UserIssueHistoryManager;

/**
 * Checks if there are any history issue's
 */
public class UserHasIssueHistoryCondition extends AbstractJiraCondition
{
    final private UserIssueHistoryManager userHistoryManager;

    public UserHasIssueHistoryCondition(UserIssueHistoryManager userHistoryManager)
    {
        this.userHistoryManager = userHistoryManager;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return userHistoryManager.hasIssueHistory(user);
    }
}
