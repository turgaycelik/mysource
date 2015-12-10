package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

/**
 * Checks if this user is logged in
 */
public class UserLoggedInCondition extends AbstractJiraCondition
{
    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return user != null;
    }
}
