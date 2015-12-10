package com.atlassian.jira.util;

/**
 * @since v6.1
 */
public interface JiraKey
{
    public static final JiraKey INVALID_JIRA_KEY = new InvalidJiraKey();

    public String getProjectKey();
    public long getIssueNumber();

    /**
     * Validates issue key. See {@link JiraKeyUtils#validIssueKey(String)}
     * @return true if the issue key started with a valid project key and ended with a number
     */
    public boolean isValid();
}
