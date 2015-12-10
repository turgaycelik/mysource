package com.atlassian.jira.pageobjects.config;

/**
 * Component providing config information about JIRA.
 *
 * @since v4.4
 */
public interface JiraConfigProvider
{
    /**
     * JIRA Home directory path.
     *
     * @return JIRA Home directory path
     */
    String jiraHomePath();

    /**
     * Checks if JIRA instance is set up.
     *
     * @return <code>true</code>, if JIRA is set up
     */
    boolean isSetUp();
}
