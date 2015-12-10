package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

/**
 * Returns true if voting is enabled.
 *
 * @since v3.12
 */
public class VotingEnabledCondition extends AbstractJiraCondition
{
    private final ApplicationProperties applicationProperties;


    public VotingEnabledCondition(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING);
    }
}
