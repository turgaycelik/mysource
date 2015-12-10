package com.atlassian.jira.jql.context;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0
 */
public class IssueTypeContextImpl implements IssueTypeContext
{
    private final String issueTypeId;

    public IssueTypeContextImpl(final String issueTypeId)
    {
        this.issueTypeId = notNull("issueTypeId", issueTypeId);
    }

    public String getIssueTypeId()
    {
        return issueTypeId;
    }

    public boolean isAll()
    {
        return false;
    }

    @Override
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

        final IssueTypeContextImpl that = (IssueTypeContextImpl) o;

        if (!issueTypeId.equals(that.issueTypeId))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return issueTypeId.hashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("issueTypeId", issueTypeId).
                toString();
    }
}
