package com.atlassian.jira.issue.operation;

public final class ScreenableSingleIssueOperationImpl extends IssueOperationImpl implements ScreenableIssueOperation
{
    private final Long id;

    public ScreenableSingleIssueOperationImpl(Long id, String name, String description)
    {
        super(name, description);
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof IssueOperation)) return false;

        final ScreenableIssueOperation issueOperation = (ScreenableIssueOperation) o;

        if (getDescriptionKey() != null ? !getDescriptionKey().equals(issueOperation.getDescriptionKey()) : issueOperation.getDescriptionKey() != null) return false;
        if (id != null ? !id.equals(issueOperation.getId()) : issueOperation.getId() != null) return false;
        if (getNameKey() != null ? !getNameKey().equals(issueOperation.getNameKey()) : issueOperation.getNameKey() != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 29 * result + (getNameKey() != null ? getNameKey().hashCode() : 0);
        result = 29 * result + (getDescriptionKey() != null ? getDescriptionKey().hashCode() : 0);
        return result;
    }
}
