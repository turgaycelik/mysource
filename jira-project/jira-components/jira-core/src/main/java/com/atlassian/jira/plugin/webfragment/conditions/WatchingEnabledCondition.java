package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

/**
 * Returns true if watching is enabled.
 *
 * @since v3.12
 */
public class WatchingEnabledCondition extends AbstractJiraCondition
{

    private final ApplicationProperties applicationProperties;

    public WatchingEnabledCondition(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING);
    }
}
