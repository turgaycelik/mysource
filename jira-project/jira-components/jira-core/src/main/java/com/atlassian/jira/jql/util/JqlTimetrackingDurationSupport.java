package com.atlassian.jira.jql.util;

/**
 * Interface for assisting in the parsing of duration values
 *
 * @since v4.0
 */
public interface JqlTimetrackingDurationSupport extends IndexValueConverter
{
    /**
     * Check to see if the passed string is a valid duration according to JQL.
     *
     *
     * @param durationString the string to check; cannot be null.
     * @return true if the date is valid; false otherwise.
     * @throws IllegalArgumentException if the passed durationString is blank or null
     */
    boolean validate(String durationString);
}
