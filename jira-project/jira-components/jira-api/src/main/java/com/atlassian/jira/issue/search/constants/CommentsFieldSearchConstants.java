package com.atlassian.jira.issue.search.constants;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.query.operator.Operator;

import java.util.EnumSet;
import java.util.Set;

/**
 * The search constants for the comments system field. The comments are unique as they are stored in a different
 * index to issues.
 *
 * @since v4.0
 */
public final class CommentsFieldSearchConstants implements ClauseInformation
{
    private final static CommentsFieldSearchConstants instance = new CommentsFieldSearchConstants();

    private final ClauseNames names;
    private final Set<Operator> supportedOperators;

    private CommentsFieldSearchConstants()
    {
        this.names = new ClauseNames(IssueFieldConstants.COMMENT);
        this.supportedOperators = EnumSet.of(Operator.LIKE, Operator.NOT_LIKE);
    }

    public ClauseNames getJqlClauseNames()
    {
        return this.names;
    }

    public String getIndexField()
    {
        return DocumentConstants.COMMENT_ID;
    }

    public String getUrlParameter()
    {
        return "body";
    }

    public String getFieldId()
    {
        return IssueFieldConstants.COMMENT;
    }

    public Set<Operator> getSupportedOperators()
    {
        return this.supportedOperators;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.TEXT;
    }

    static CommentsFieldSearchConstants getInstance()
    {
        return instance;
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

        final CommentsFieldSearchConstants that = (CommentsFieldSearchConstants) o;

        if (!names.equals(that.names))
        {
            return false;
        }
        if (!supportedOperators.equals(that.supportedOperators))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = names.hashCode();
        result = 31 * result + supportedOperators.hashCode();
        return result;
    }
}
