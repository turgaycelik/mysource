package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import org.apache.log4j.Logger;

/**
 * Condition to determine whether linking is enabled
 *
 * @since v4.1
 */
public class LinkingEnabledCondition extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(LinkingEnabledCondition.class);
    private final ApplicationProperties applicationProperties;

    public LinkingEnabledCondition(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
    }

}