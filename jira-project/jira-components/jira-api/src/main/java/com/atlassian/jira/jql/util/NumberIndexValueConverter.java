package com.atlassian.jira.jql.util;

import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.jql.operand.QueryLiteral;

/**
 * Converts a query literal into a number index representation.
 *
 * @since v4.0
 */
public class NumberIndexValueConverter implements IndexValueConverter
{
    private final DoubleConverter doubleConverter;

    public NumberIndexValueConverter(DoubleConverter doubleConverter)
    {
        this.doubleConverter = doubleConverter;
    }

    public String convertToIndexValue(final QueryLiteral rawValue)
    {
        if (rawValue.isEmpty())
        {
            return null;
        }
        
        try
        {
            return doubleConverter.getStringForLucene(rawValue.asString());
        }
        catch (FieldValidationException e)
        {
            return null;
        }
    }
}
