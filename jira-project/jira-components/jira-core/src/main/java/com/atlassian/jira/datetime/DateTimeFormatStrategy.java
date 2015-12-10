package com.atlassian.jira.datetime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Date;
import java.util.Locale;

/**
 * SPI for DateTimeStyle formatter implementations. There should be one of these for each enum value in {@link
 * DateTimeStyle}.
 * <p/>
 * Implementations must be thread-safe.
 *
 * @see DateTimeStyle
 * @since 4.4
 */
@ThreadSafe
interface DateTimeFormatStrategy
{
    /**
     * Formats the given date using the style returned by {@link #style()}.
     *
     * @param dateTime the DateTime to format. The formatted date will be in the time zone specified by the value of
     * this parameter's {@link org.joda.time.DateTime#getZone()} method.
     * @param locale a Locale to use when formatting the date
     * @return a formatted DateTime
     */
    String format(DateTime dateTime, Locale locale);

    /**
     * Parses the given date text using the style returned by {@link #style()} (optional operation).
     *
     * @param text a String containing a date/time
     * @param timeZone the time zone to use for parsing the text
     * @param locale the locale to use for parsing the text
     * @return a new Date
     * @throws UnsupportedOperationException if this strategy does not support parsing
     */
    Date parse(String text, DateTimeZone timeZone, Locale locale) throws UnsupportedOperationException;

    /**
     * Returns the DateTimeStyle that this formatter implements.
     *
     * @return a DateTimeStyle
     */
    DateTimeStyle style();

    /**
     * Returns a pattern that can be shown to a user, in order to describe a valid date. This does not have to be a
     * valid pattern in Java.
     *
     * @return a String containing a pattern
     */
    String pattern();
}
