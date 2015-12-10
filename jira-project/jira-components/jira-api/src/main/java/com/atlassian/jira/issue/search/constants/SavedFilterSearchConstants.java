package com.atlassian.jira.issue.search.constants;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.query.operator.Operator;

import java.util.Set;

/**
 * Saved filter is strange so it does not have a fieldId or an index id.
 *
 * @since v4.0
 */
public class SavedFilterSearchConstants implements ClauseInformation
{
    private final static SavedFilterSearchConstants INSTANCE = new SavedFilterSearchConstants();

    private final ClauseNames SAVED_FILTER;
    private final Set<Operator> supportedOperators;

    private SavedFilterSearchConstants()
    {
        this.SAVED_FILTER = new ClauseNames("filter", "savedfilter", "searchrequest", "request");
        this.supportedOperators = OperatorClasses.EQUALITY_OPERATORS;
    }

    public ClauseNames getJqlClauseNames()
    {
        return SAVED_FILTER;
    }

    // This makes this implementation strange since if has no associated index field, instead it indicates another search
    public String getIndexField()
    {
        return null;
    }

    // This makes this implementation strange since if has no associated field
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
        return JiraDataTypes.SAVED_FILTER;
    }

    public static SavedFilterSearchConstants getInstance()
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

        final SavedFilterSearchConstants that = (SavedFilterSearchConstants) o;

        if (!SAVED_FILTER.equals(that.SAVED_FILTER))
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
        int result = SAVED_FILTER.hashCode();
        result = 31 * result + supportedOperators.hashCode();
        return result;
    }
}
