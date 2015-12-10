package com.atlassian.jira.jql.util;

import com.atlassian.jira.jql.operand.QueryLiteral;

/**
 * A simple interface for converting {@link com.atlassian.jira.jql.operand.QueryLiteral}s to their index representation.
 *
 * @since v4.0
 */
public interface IndexValueConverter
{
    /**
     * @param rawValue the query literal to convert
     * @return the string of the index representation, null if the conversion fails
     */
    String convertToIndexValue(QueryLiteral rawValue);
}
