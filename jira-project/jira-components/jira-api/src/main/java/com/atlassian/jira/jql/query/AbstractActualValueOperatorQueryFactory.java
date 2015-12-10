package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.IndexValueConverter;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Base class for Indexed Value clauses query factories
 *
 * @since v4.0
 */
abstract class AbstractActualValueOperatorQueryFactory
{
    private final IndexValueConverter indexValueConverter;

    public AbstractActualValueOperatorQueryFactory(final IndexValueConverter indexValueConverter)
    {
        this.indexValueConverter = notNull("indexValueConverter", indexValueConverter);
    }

    /**
     * @param rawValues the raw values to convert
     * @return a list of index values in String form; never null, but may contain null values if empty literals were passed in.
     */
    List<String> getIndexValues(List<QueryLiteral> rawValues)
    {
        List<String> values = new ArrayList<String>();
        for (QueryLiteral rawValue : rawValues)
        {
            if (rawValue.isEmpty())
            {
                values.add(null);
            }
            else
            {
                final String indexValue = indexValueConverter.convertToIndexValue(rawValue);
                if (indexValue != null)
                {
                    values.add(indexValue);
                }
            }
        }
        return values;
    }
}