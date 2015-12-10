package com.atlassian.jira.issue.priority;

import com.atlassian.jira.issue.MockIssueConstant;

/**
 * @since v3.13
 */
public class MockPriority extends MockIssueConstant implements Priority
{
    private String statusColor;

    public MockPriority(String id, String name)
    {
        this(id, name, null);
    }

    public MockPriority(String id, String name, String statusColor)
    {
        super(id, name);
        this.statusColor = statusColor;
    }

    public String getStatusColor()
    {
        return statusColor;
    }

    public void setStatusColor(final String statusColor)
    {
        this.statusColor = statusColor;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final MockPriority that = (MockPriority) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (getId() != null ? getId().hashCode() : 0);
    }
}
