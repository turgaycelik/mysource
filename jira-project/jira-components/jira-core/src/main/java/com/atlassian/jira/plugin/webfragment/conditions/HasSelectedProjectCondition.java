package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

/**
 * Checks if a project is selected
 * <p>
 * The project must be set within {@link JiraHelper}
 */
public class HasSelectedProjectCondition extends AbstractJiraCondition
{
    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return jiraHelper.getProject() != null;
    }
}
