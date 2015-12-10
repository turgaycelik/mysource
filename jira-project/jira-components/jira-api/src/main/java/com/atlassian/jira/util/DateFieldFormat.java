package com.atlassian.jira.util;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Date;

/**
 * <p>
 * Helper for handling JIRA date fields. Dates in JIRA are displayed as day/month/year and are intepreted according to
 * the date picker format. Even though we use java.util.Date instances internally, only the year/month/day are relevant
 * in a date field. Dates are used in a few places throughout JIRA:
 * </p>
 * <ul>
 *     <li>the issue due date</li>
 *     <li>the date custom field</li>
 *     <li>the project version release date</li>
 * </ul>
 * <p>
 * The Date instances that this class produces are always to be interpreted in {@link java.util.TimeZone#getDefault()
 * the system time zone}.
 * </p>
 *
 * @see com.atlassian.jira.datetime.DateTimeStyle#DATE
 * @see com.atlassian.jira.datetime.DateTimeStyle#DATE_PICKER
 * @since v4.4
 */
@ThreadSafe
public interface DateFieldFormat
{
    /**
     * Formats a date using the JIRA date format.
     *
     * @param date a Date
     * @return a String containing a formatted date
     */
    String format(Date date);

    /**
     * Formats the given date for usage in a date picker.
     *
     * @param date a Date
     * @return a String containing a formatted date
     */
    String formatDatePicker(Date date);

    /**
     * Returns a date that is obtained by parsing the given text. The string representation of the date is assumed to be
     * in the system time zone.
     *
     * @param text a String representation of a date
     * @return a Date
     * @throws IllegalArgumentException if the text cannot be parsed
     */
    Date parseDatePicker(String text) throws IllegalArgumentException;

    /**
     * Returns a boolean indicating whether the given text contains a valid date.
     *
     * @param text a String representation of a date
     * @return a boolean indicating whether the given text contains a valid date
     */
    boolean isParseable(String text);

    /**
     * Returns a string containing the format that this class is capable of parsing.
     *
     * @return a String containing the format hint
     */
    String getFormatHint();
}
