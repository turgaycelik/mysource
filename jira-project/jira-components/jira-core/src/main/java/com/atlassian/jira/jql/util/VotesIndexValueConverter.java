package com.atlassian.jira.jql.util;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.InjectableComponent;
import org.apache.lucene.document.NumberTools;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Converts a query literal into the votes index representation. Must be
 * positive number, otherwise null is returned.
 *
 * @since v4.0
 */
@InjectableComponent
public class VotesIndexValueConverter implements IndexValueConverter
{
    public String convertToIndexValue(final QueryLiteral rawValue)
    {
        notNull("rawValue", rawValue);
        if (rawValue.isEmpty())
        {
            return null;
        }
        else if (rawValue.getLongValue() != null)
        {
            return convertLong(rawValue.getLongValue());
        }
        else
        {
            return convertString(rawValue.getStringValue());
        }
    }

    private String convertString(final String stringValue)
    {
        try
        {
            return convertLong(Long.parseLong(stringValue));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private String convertLong(final Long longValue)
    {
        if (longValue >= 0)
        {
            return NumberTools.longToString(longValue);
        }
        else
        {
            return null;
        }
    }
}
