package com.atlassian.jira.jql.util;

import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;

import java.util.Set;

/**
 * A utility code to help dealing with JQL strings. 
 *
 * @since v4.0
 */
public interface JqlStringSupport
{
    /**
     * Encode the passed string value into a safe JQL value if necessary. The value will not be encoded if it is
     * already safe.
     *
     * @param value the value to encode.
     * @return the encoded string.
     */
    String encodeStringValue(String value);

    /**
     * Encode the passed string value into a safe JQL value if necessary. The value will not be encoded if it is
     * already safe. This is different to {@link #encodeStringValue(String)} since it will not add quotes around
     * long values.
     *
     * @param value the value to encode.
     * @return the encoded string.
     */
    String encodeValue(String value);

    /**
     * Encode the passed string into a safe JQL function argument. The value will not be encoded if it is already safe.
     *
     * @param argument the string to encode.
     * @return the encoded string.
     */
    String encodeFunctionArgument(String argument);

    /**
     * Encode the passed string into a safe JQL function name. This value will not be encoded if it is not already safe.
     *
     * @param functionName the string to encode.
     * @return the encoded string.
     */
    String encodeFunctionName(String functionName);

    /**
     * Encode the passed string into a safe JQL field name. This value will not be encoded if it is not already safe.
     *
     * @param fieldName the string to encode.
     * @return the encoded string.
     */
    String encodeFieldName(String fieldName);

    /**
     * Generates a JQL string representation for the passed query. The JQL string is always generated, that is, {@link com.atlassian.query.Query#getQueryString()}
     * is completely ignored if it exists. The returned JQL is automatically escaped as necessary.
     *
     * @param query the query. Cannot be null.
     * @return the generated JQL string representation of the passed query.
     */
    String generateJqlString(Query query);

    /**
     * Generates a JQL string representation for the passed clause. The returned JQL is automatically escaped as necessary.
     *
     * @param clause the clause. Cannot be null.
     * @return the generated JQL string representation of the passed clause.
     */
    String generateJqlString(Clause clause);

    /**
     * @return all the reserved words for the JQL language.
     */
    Set<String> getJqlReservedWords();
}
