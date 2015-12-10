package com.atlassian.jira.issue.search.constants;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.query.operator.Operator;

import java.util.Set;

/**
 * Constants for issue key.
 *
 * @since v4.0
 */
public class IssueKeyConstants implements ClauseInformation
{
    private final static IssueKeyConstants INSTANCE = new IssueKeyConstants();

    private final ClauseNames names;
    private final String fieldId;
    private final String indexField;
    private Set<Operator> supportedOperators;
    private boolean supportsHistorySearches;

    private IssueKeyConstants()
    {
        this.names = new ClauseNames("key", "issue", "issuekey", "id");
        this.fieldId = IssueFieldConstants.ISSUE_KEY;
        this.indexField = DocumentConstants.ISSUE_KEY_FOLDED;
        this.supportedOperators = OperatorClasses.EQUALITY_AND_RELATIONAL;
        this.supportsHistorySearches = false;
    }

    public ClauseNames getJqlClauseNames()
    {
        return names;
    }

    public String getIndexField()
    {
        return indexField;
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public Set<Operator> getSupportedOperators()
    {
        return supportedOperators;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.ISSUE;
    }

    public static IssueKeyConstants getInstance()
    {
        return INSTANCE;
    }

    public String getKeyIndexOrderField()
    {
        return DocumentConstants.ISSUE_KEY_NUM_PART_RANGE;
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

        final IssueKeyConstants that = (IssueKeyConstants) o;

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
