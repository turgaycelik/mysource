package com.atlassian.jira.jql.util;

import com.atlassian.jira.jql.operand.QueryLiteral;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Simplest index value converter that just uses the toString() implementation.
 *
 * @since v4.0
 */
public final class SimpleIndexValueConverter implements IndexValueConverter
{
    private final boolean lowerCase;

    public SimpleIndexValueConverter(final boolean lowerCase)
    {
        this.lowerCase = lowerCase;
    }

    public String convertToIndexValue(final QueryLiteral rawValue)
    {
        notNull("rawValue", rawValue);

        if (rawValue.isEmpty())
        {
            return null;
        }
        
        final String str = rawValue.asString();
        if (lowerCase)
        {
            return str.toLowerCase();
        }
        else
        {
            return str;
        }
    }
}
