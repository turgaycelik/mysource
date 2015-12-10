package com.atlassian.jira.issue.search.constants;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.query.operator.Operator;

import java.util.Set;

/**
 * Searching constants for the "IssueKey" JQL clause.
 *
 * @since v4.0
 */
public final class IssueIdConstants implements ClauseInformation
{
    private final static IssueIdConstants INSTANCE = new IssueIdConstants();

    private final ClauseNames names;
    private final String fieldId;
    private final String indexField;
    private final Set<Operator> supportedOperators;

    private IssueIdConstants()
    {
        this.names = new ClauseNames("key", "issue", "issuekey", "id");
        this.fieldId = DocumentConstants.ISSUE_ID;
        this.indexField = DocumentConstants.ISSUE_ID;
        this.supportedOperators = OperatorClasses.EQUALITY_AND_RELATIONAL;
    }

    public ClauseNames getJqlClauseNames()
    {
        return names;
    }

    public String getIndexField()
    {
        return this.indexField;
    }

    public String getFieldId()
    {
        return this.fieldId;
    }

    public Set<Operator> getSupportedOperators()
    {
        return supportedOperators;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.ISSUE;
    }

    public static IssueIdConstants getInstance()
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

        final IssueIdConstants that = (IssueIdConstants) o;

        if (!fieldId.equals(that.fieldId))
        {
            return false;
        }
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
        result = 31 * result + fieldId.hashCode();
        result = 31 * result + indexField.hashCode();
        result = 31 * result + supportedOperators.hashCode();
        return result;
    }
}
