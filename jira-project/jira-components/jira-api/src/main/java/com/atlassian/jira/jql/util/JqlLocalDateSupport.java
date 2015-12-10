package com.atlassian.jira.jql.util;

import com.atlassian.jira.datetime.LocalDate;

import java.util.Date;

/**
 * Interface that helps with {@link LocalDate} parsing and validation in JQL.
 *
 * @since v4.4
 * @see JqlDateSupport
 */
public interface JqlLocalDateSupport
{
    /**
     * Try to parse the passed date string using the formats that JQL understands.
     * It will consider the user's time zone when parsing the date string.
     *
     * @param dateString the string to parse. Cannot be empty or null.
     * @return the parsed date.
     * @throws IllegalArgumentException if the passed dateString is blank or null.
     */
    LocalDate convertToLocalDate(String dateString);

    /**
     * Try to parse the passed date long.
     *
     * @param dateLong the string to parse. Cannot be empty or null.
     * @return the parsed date.
     * @throws IllegalArgumentException if the passed dateString is blank or null.
     */
    LocalDate convertToLocalDate(Long dateLong);

    /**
     * Converts a date into the index-friendly format.
     *
     * @param date the date
     * @return a string representing the date, ready for comparison to indexed values.
     */
    String getIndexedValue(LocalDate date);

    /**
     * Check to see if the passed string is a valid date according to JQL.
     *
     * @param dateString the string to check cannot be null.
     * @return true if the date is valid; false otherwise.
     * @throws IllegalArgumentException if the passed dateString is blank or null
     */
    boolean validate(String dateString);

    /**
     * Return a string representation of the passed date. This method should just convert the date into its parseable
     * String representation.
     *
     * @param date the date to convert. Cannot be null.
     * @return return the passed date as a string.
     * @throws IllegalArgumentException if the passed date is null.
     */
    String getLocalDateString(LocalDate date);

    /**
     * Converts a LocalDate to a Date using the systems time zone.
     *
     * @param date the date to convert. Cannot be null.
     * @return return the Date for the local date.
     *
     * @throws IllegalArgumentException if the passed date is null.
     */
    Date convertToDate(LocalDate date);
}
