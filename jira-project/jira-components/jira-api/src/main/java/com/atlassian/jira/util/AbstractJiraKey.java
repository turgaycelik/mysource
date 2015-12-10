package com.atlassian.jira.util;

/**
* @since v6.1
*/
abstract class AbstractJiraKey implements JiraKey
{
    private final String projectKey;
    private final long issueNumber;

    AbstractJiraKey(final String projectKey, final long issueNumber)
    {
        this.projectKey = projectKey;
        this.issueNumber = issueNumber;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public long getIssueNumber()
    {
        return issueNumber;
    }
}
