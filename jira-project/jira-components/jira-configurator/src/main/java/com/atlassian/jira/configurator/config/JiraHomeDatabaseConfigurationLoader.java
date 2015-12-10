package com.atlassian.jira.configurator.config;

import com.atlassian.jira.config.database.AbstractJiraHomeDatabaseConfigurationLoader;

/**
 * Simple DatabaseConfigurationLoader for load and saving DB config in jira-home
 *
 * @since v4.4
 */
public class JiraHomeDatabaseConfigurationLoader extends AbstractJiraHomeDatabaseConfigurationLoader
{
    private final String jiraHome;

    public JiraHomeDatabaseConfigurationLoader(String jiraHome)
    {
        this.jiraHome = jiraHome;
    }

    @Override
    protected String getJiraHome()
    {
        return jiraHome;
    }

    @Override
    protected void logInfo(String message)
    {
        System.out.println(message);
    }
}
