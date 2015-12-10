package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

/**
 * Checks if we're in admin mode
 */
public class IsAdminModeCondition extends AbstractJiraCondition
{
    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return jiraHelper.getRequest().getAttribute("jira.admin.mode") != null;
    }
}
