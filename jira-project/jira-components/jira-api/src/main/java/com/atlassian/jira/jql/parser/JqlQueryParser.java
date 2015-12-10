package com.atlassian.jira.jql.parser;

import com.atlassian.query.Query;

/**
 * Used to parse some JQL into its {@link com.atlassian.query.Query} representation.
 *
 * @since v4.0
 */
public interface JqlQueryParser
{
    /**
     * Parse the passed JQL string into its SearchQuery representation.
     *
     * @param jqlQuery the JQL string to parse. Must not be <code>null</code> or blank.
     * @return the Query representation of the passed jql string. Never null.
     * @throws JqlParseException if an error occurs while parsing the query.
     * @throws IllegalArgumentException if jqlQuery  is <code>null</code> or blank.
     */
    Query parseQuery(String jqlQuery) throws JqlParseException;

    /**
     * Determines whether or not the passed string is a valid JQL field name.
     *
     * @param fieldName the field name to check.
     * @return true if the passed string is a valid field name or false otherwise.
     */
    boolean isValidFieldName(final String fieldName);

    /**
     * Determines whether or not the passed string is a valid JQL function argument.
     *
     * @param argument the function argument to check.
     * @return true if the passed function argument is valid or false otherwise.
     */
    boolean isValidFunctionArgument(final String argument);

    /**
     * Determines whether or not the passed string is a valid JQL function name.
     *
     * @param functionName the function name to check.
     * @return true if the passed function name is valid or false otherwise.
     */
    boolean isValidFunctionName(final String functionName);

    /**
     * Determines whether or not the passed string is a valid JQL value.
     *
     * @param value the value to check.
     * @return true if the passed value is valid or false otherwise.
     */
    boolean isValidValue(final String value);
}
