package com.atlassian.jira.issue.operation;

public class IssueOperationImpl implements SingleIssueOperation
{
    private final String name;
    private final String description;

    public IssueOperationImpl(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public String getNameKey()
    {
        return name;
    }

    public String getDescriptionKey()
    {
        return description;
    }
}
