package com.atlassian.jira.issue.search.constants;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.query.operator.Operator;

import java.util.Set;

/**
 * Searching constants for the "Issue Parent" JQL clause.
 *
 * @since v4.0
 */
public final class IssueParentConstants implements ClauseInformation
{
    private final static IssueParentConstants INSTANCE = new IssueParentConstants();

    private final ClauseNames names;
    private final String indexField;
    private final Set<Operator> supportedOperators;

    private IssueParentConstants()
    {
        this.names = new ClauseNames("parent");
        this.indexField = DocumentConstants.ISSUE_PARENTTASK;
        this.supportedOperators = OperatorClasses.EQUALITY_OPERATORS;
    }

    public ClauseNames getJqlClauseNames()
    {
        return names;
    }

    public String getIndexField()
    {
        return indexField;
    }

    // there is no Issue Parent field
    public String getFieldId()
    {
        return null;
    }

    public Set<Operator> getSupportedOperators()
    {
        return supportedOperators;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.ISSUE;
    }

    public static IssueParentConstants getInstance()
    {
        return INSTANCE;
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

        final IssueParentConstants that = (IssueParentConstants) o;

        if (!indexField.equals(that.indexField))
        {
            return false;
        }
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
        result = 31 * result + indexField.hashCode();
        result = 31 * result + supportedOperators.hashCode();
        return result;
    }
}
