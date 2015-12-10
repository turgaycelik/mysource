package com.atlassian.jira.issue.search.constants;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import com.atlassian.query.operator.Operator;
import net.jcip.annotations.ThreadSafe;

import java.util.Set;

/**
 * Search constants for time tracking field clauses in JQL e.g. current estimate, original estimate, time spent
 *
 * @since v4.0
 */
@ThreadSafe
public final class DefaultClauseInformation implements ClauseInformation
{
    private final String indexField;
    private final ClauseNames jqlClauseNames;
    private final String fieldId;
    private final Set<Operator> supportedOperators;
    private final JiraDataType supportedType;

    public DefaultClauseInformation(final String indexField, final ClauseNames names,
            final String fieldId, final Set<Operator> supportedOperators,
            final JiraDataType supportedType)
    {
        this.supportedType = notNull("supportedType", supportedType);
        this.indexField = notBlank("indexField", indexField);
        this.jqlClauseNames = notNull("names", names);
        this.fieldId = fieldId;
        this.supportedOperators = notNull("supportedOperators", supportedOperators);
    }

    public DefaultClauseInformation(final String indexField, final String jqlClauseName,
            final String fieldId, Set<Operator> supportedOperators,
            final JiraDataType supportedType)
    {
        this(indexField, new ClauseNames(notBlank("jqlClauseNames", jqlClauseName)), fieldId, supportedOperators, supportedType);
    }

    public String getIndexField()
    {
        return indexField;
    }

    public ClauseNames getJqlClauseNames()
    {
        return jqlClauseNames;
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public Set<Operator> getSupportedOperators()
    {
        return this.supportedOperators;
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

        final DefaultClauseInformation that = (DefaultClauseInformation) o;

        if (fieldId != null ? !fieldId.equals(that.fieldId) : that.fieldId != null)
        {
            return false;
        }
        if (!indexField.equals(that.indexField))
        {
            return false;
        }
        if (!jqlClauseNames.equals(that.jqlClauseNames))
        {
            return false;
        }
        if (!supportedOperators.equals(that.supportedOperators))
        {
            return false;
        }
        if (!supportedType.equals(that.supportedType))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = indexField.hashCode();
        result = 31 * result + jqlClauseNames.hashCode();
        result = 31 * result + (fieldId != null ? fieldId.hashCode() : 0);
        result = 31 * result + supportedOperators.hashCode();
        result = 31 * result + supportedType.hashCode();
        return result;
    }
}
