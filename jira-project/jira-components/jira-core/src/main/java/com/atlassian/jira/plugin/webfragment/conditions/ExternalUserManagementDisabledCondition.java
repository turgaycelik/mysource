package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

/**
 * Condition returns true if {@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_USER_EXTERNALMGT}
 * is disabled.
 *
 * @since v3.12
 */
public class ExternalUserManagementDisabledCondition extends AbstractJiraCondition
{
    private final ApplicationProperties applicationProperties;


    public ExternalUserManagementDisabledCondition(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return !applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }
}
