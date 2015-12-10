package com.atlassian.jira.config;

import com.atlassian.jira.issue.issuetype.IssueType;

public class IssueTypeProxy
{
    private final String key;
    private final String value;

    public IssueTypeProxy(IssueType issueType)
    {
        if (issueType == null)
        {
            throw new IllegalArgumentException("IssueType cannot be null.");
        }
        else if (!"IssueType".equals(issueType.getGenericValue().getEntityName()))
        {
            throw new IllegalArgumentException("Entity passed must be an IssueType");
        }

        key = issueType.getId();
        value = "- " + issueType.getNameTranslation();
    }

    public IssueTypeProxy(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }
}
