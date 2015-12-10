package com.atlassian.jira.jql.util;

import java.util.Date;
import java.util.TimeZone;

/**
 * Interface that helps with date parsing and validation in JQL.
 *
 * @since v4.0
 */
public interface JqlDateSupport
{
    /**
     * Try to parse the passed date string using the formats that JQL understands. It will consider the user's time zone
     * when parsing the date string.
     *
     * @param dateString the string to parse. Cannot be null.
     * @return the parsed date or null if it cant be parsed.  You did call {@link #validate(String)} right?
     * @throws IllegalArgumentException if the passed dateString is null.
     */
    Date convertToDate(String dateString);

    /**
     * Try to parse the passed date string using the formats that JQL understands. It will use the passed time zone
     * when parsing the date string.
     *
     * @param dateString the string to parse. Cannot be null.
     * @param timeZone time zone to use when parsing.
     * @return the parsed date or null if it cant be parsed.  You did call {@link #validate(String)} right?
     * @throws IllegalArgumentException if the passed dateString is null.
     */
    Date convertToDate(String dateString, TimeZone timeZone);

    /**
     * Try to parse the passed in date string using the formats that JQL understands. It will consider the user's time
     * zone when parsing the date string.
     * <p/>
     * It will eamine the single input string and use the implied precision to create the range
     * <p/>
     * If you provide only a year/month/day it will have a precision of 24 hours, ie from the start of the day to the
     * end of the day
     * <p/>
     * If you supply year/month/day hour/minute, it will have a precision of 1 minute, ie from the start of the minute
     * to the end of the minute.
     *
     * @param dateString the string to parse. Cannot be null.
     * @return the parsed datetime as a range using the implied precision.  Or null if the date cant be parsed. You did
     *         call {@link #validate(String)} right?
     * @throws IllegalArgumentException if the passed dateString is null.
     */
    DateRange convertToDateRangeWithImpliedPrecision(String dateString);

    /**
     * Converts the long to a date.
     *
     * @param dateLong the long to give back a date for . Cannot be null.
     * @return the parsed date.
     * @throws IllegalArgumentException if the passed in Long is null
     */
    Date convertToDate(Long dateLong);

    /**
     * Converts the long to a date range where both values equal each other.
     *
     * @param dateLong the long to give back a date for . Cannot be null.
     * @return the parsed date twice.  Mostly for symmetry to calling code since JQL can have both long and string
     *         representations of values
     * @throws IllegalArgumentException if the passed in Long is null
     */
    DateRange convertToDateRange(Long dateLong);

    /**
     * Converts a date into the index-friendly format.
     *
     * @param date the date
     * @return a string representing the date, ready for comparison to indexed values.
     */
    String getIndexedValue(Date date);

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
     * String representation. The user's time zone will be used when formatting the date string.
     *
     * @param date the date to convert. Cannot be null.
     * @return return the passed date as a string.
     * @throws IllegalArgumentException if the passed date is null.
     */
    String getDateString(Date date);

    /**
     * Return a string representation of the passed date. This method should just convert the date into its parseable
     * String representation. The passed time zone will be used when formatting the date string.
     *
     * @param date the date to convert. Cannot be null.
     * @param timeZone time zone to use. Cannot be null.
     * @return return the passed date as a string.
     * @throws IllegalArgumentException if the passed date is null.
     */
    String getDateString(Date date, TimeZone timeZone);

    /**
     * Returns a boolean value indicating whether the passed date string representation has duration format, e.g., 4d 1h, -1w.
     *
     * @param dateString the string to parse.
     * @return true if the passed date string has duration format, false otherwise.
     */
    boolean isDuration(String dateString);
}
