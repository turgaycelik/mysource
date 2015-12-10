package com.atlassian.jira.issue.index;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.history.DateRangeBuilder;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple class to represent a supported field in a change history search.  for example for status,
 * this would be represented by new IndexedChangeHistoryField("status",...);
 *
 * @since v4.3
 */
@PublicApi
public class IndexedChangeHistoryField
{
    private final String fieldName;
    private final DateRangeBuilder dateRangeBuilder;

     public IndexedChangeHistoryField(String fieldName, DateRangeBuilder dateRangeBuilder)
    {
        this.fieldName = fieldName;
        this.dateRangeBuilder = dateRangeBuilder;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public DateRangeBuilder getDateRangeBuilder()
    {
        return dateRangeBuilder;
    }

    @Override
    public boolean equals(Object obj)
    {
         if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        IndexedChangeHistoryField rhs=(IndexedChangeHistoryField)obj;
        return new EqualsBuilder()
                .append(fieldName,rhs.fieldName)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(11,29)
                .append(fieldName)
                .toHashCode();
    }

    public Set<Operator> getSupportedOperators(Set<Operator> operators)
    {
        final Set<Operator> supportedOperators = new HashSet<Operator>();
        supportedOperators.addAll(operators);
        supportedOperators.addAll(OperatorClasses.CHANGE_HISTORY_OPERATORS);
        return Collections.unmodifiableSet(supportedOperators);
    }
}
