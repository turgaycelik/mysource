package com.atlassian.jira.startup;

/**
 * Implementations of this interface will be able to find a configured jira.home directory in one particular way.
 *
 * @since v4.0
 */
public interface JiraHomePathLocator
{
    static final class Property
    {
        public static final String JIRA_HOME = "jira.home";
    }

    /**
     * Returns the jira.home path configured via this locator method, or null if none is configured.
     * @return the jira.home path configured via this locator method, or null if none is configured.
     */
    String getJiraHome();

    /**
     * Returns a user-friendly and readable name for this locator to make support's life easier.
     * @return a user-friendly and readable name for this locator to make support's life easier
     */
    String getDisplayName();
}
