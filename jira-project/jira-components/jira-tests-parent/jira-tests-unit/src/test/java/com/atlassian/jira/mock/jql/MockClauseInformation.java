package com.atlassian.jira.mock.jql;

import java.util.Collections;
import java.util.Set;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.query.operator.Operator;

/**
 * @since v4.0
 */
public class MockClauseInformation implements ClauseInformation
{
    private final ClauseNames clauseNames;
    private final String indexField;
    private final String fieldId;
    private final Set<Operator> supportedOperators;
    private final JiraDataType supportedType;

    public MockClauseInformation(final ClauseNames clauseNames)
    {
        this.clauseNames = clauseNames;
        this.indexField = null;
        this.fieldId = null;
        this.supportedOperators = Collections.emptySet();
        this.supportedType = JiraDataTypes.ALL;
    }

    public MockClauseInformation(final ClauseNames clauseNames, final String indexField, final String fieldId)
    {
        this.clauseNames = clauseNames;
        this.indexField = indexField;
        this.fieldId = fieldId;
        this.supportedOperators = Collections.emptySet();
        this.supportedType = JiraDataTypes.ALL;
    }

    public MockClauseInformation(final ClauseNames clauseNames, final String indexField, final String fieldId, final Set<Operator> supportedOperators)
    {
        this.clauseNames = clauseNames;
        this.indexField = indexField;
        this.fieldId = fieldId;
        this.supportedOperators = supportedOperators;
        this.supportedType = JiraDataTypes.ALL;
    }

    public MockClauseInformation(final ClauseNames clauseNames, final String indexField, final String fieldId,
            final Set<Operator> supportedOperators, final JiraDataType supportedType)
    {
        this.clauseNames = clauseNames;
        this.indexField = indexField;
        this.fieldId = fieldId;
        this.supportedOperators = supportedOperators;
        this.supportedType = supportedType;
    }

    public ClauseNames getJqlClauseNames()
    {
        return clauseNames;
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
        return supportedType;
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

        final MockClauseInformation that = (MockClauseInformation) o;

        if (clauseNames != null ? !clauseNames.equals(that.clauseNames) : that.clauseNames != null)
        {
            return false;
        }
        if (fieldId != null ? !fieldId.equals(that.fieldId) : that.fieldId != null)
        {
            return false;
        }
        if (indexField != null ? !indexField.equals(that.indexField) : that.indexField != null)
        {
            return false;
        }
        if (supportedOperators != null ? !supportedOperators.equals(that.supportedOperators) : that.supportedOperators != null)
        {
            return false;
        }
        if (supportedType != null ? !supportedType.equals(that.supportedType) : that.supportedType != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = clauseNames != null ? clauseNames.hashCode() : 0;
        result = 31 * result + (indexField != null ? indexField.hashCode() : 0);
        result = 31 * result + (fieldId != null ? fieldId.hashCode() : 0);
        result = 31 * result + (supportedOperators != null ? supportedOperators.hashCode() : 0);
        result = 31 * result + (supportedType != null ? supportedType.hashCode() : 0);
        return result;
    }
}
