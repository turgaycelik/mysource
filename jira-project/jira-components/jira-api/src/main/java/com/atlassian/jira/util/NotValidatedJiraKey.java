package com.atlassian.jira.util;

/**
* @since v6.1
*/
class NotValidatedJiraKey extends AbstractJiraKey
{

    private final String key;

    NotValidatedJiraKey(final String projectKey, final long issueNumber, final String key)
    {
        super(projectKey, issueNumber);
        this.key = key;
    }

    public boolean isValid()
    {
        return JiraKeyUtils.validIssueKey(key);
    }
}
