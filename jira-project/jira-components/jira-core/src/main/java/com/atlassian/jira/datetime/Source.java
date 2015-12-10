package com.atlassian.jira.datetime;

/**
 * Strategy for calculating Locale / TimeZone at execution time
 */
interface Source<T>
{
    /**
     * Returns a boolean indicating whether this source overrides the value.
     *
     * @return a boolean
     */
    boolean isOverride();

    /**
     * Returns the value to use (never returns null).
     *
     * @return a T
     */
    T get();
}
