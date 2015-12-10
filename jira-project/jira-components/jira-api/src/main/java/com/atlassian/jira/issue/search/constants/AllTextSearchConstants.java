package com.atlassian.jira.issue.search.constants;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.query.operator.Operator;

import java.util.Collections;
import java.util.Set;

/**
 * All Text is strange because it does not have a fieldId or an index id.
 *
 * @since v4.0
 */
public class AllTextSearchConstants implements ClauseInformation
{
    private final static AllTextSearchConstants INSTANCE = new AllTextSearchConstants();

    private final ClauseNames ALL_TEXT;
    private final Set<Operator> supportedOperators;

    private AllTextSearchConstants()
    {
        this.ALL_TEXT = new ClauseNames("text");
        this.supportedOperators = Collections.singleton(Operator.LIKE);
    }

    public ClauseNames getJqlClauseNames()
    {
        return ALL_TEXT;
    }

    // This makes this implementation strange since it has no associated index field, instead it indicates searching across all text fields
    public String getIndexField()
    {
        return null;
    }

    // This makes this implementation strange since it has no associated field
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
        return JiraDataTypes.TEXT;
    }

    public static AllTextSearchConstants getInstance()
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

        final AllTextSearchConstants that = (AllTextSearchConstants) o;

        if (!ALL_TEXT.equals(that.ALL_TEXT))
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
        int result = ALL_TEXT.hashCode();
        result = 31 * result + supportedOperators.hashCode();
        return result;
    }
}
