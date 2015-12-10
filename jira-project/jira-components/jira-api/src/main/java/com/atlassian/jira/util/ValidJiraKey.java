package com.atlassian.jira.util;

/**
* @since v6.1
*/
class ValidJiraKey extends AbstractJiraKey
{
    ValidJiraKey(final String projectKey, final long issueNumber)
    {
        super(projectKey, issueNumber);
    }

    ValidJiraKey(NotValidatedJiraKey jiraKey)
    {
        super(jiraKey.getProjectKey(), jiraKey.getIssueNumber());
    }

    public boolean isValid()
    {
        return true;
    }
}
