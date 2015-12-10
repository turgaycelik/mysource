package com.atlassian.jira.datetime;

import javax.annotation.concurrent.Immutable;
import java.util.Date;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.StringEscapeUtils.escapeXml;

/**
 * Aggregates the most commonly-used date/time formats in a single instance, for use within Velocity templates. All
 * strings returned by this class are in the user's time zone, and are either HTML- or XML-escaped (as appropriate).
 *
 * @since v5.0
 */
@Immutable
public class DateTimeVelocityUtils
{
    private final DateTimeFormatter dateTimeFormatter;

    /**
     * Creates a new DateTimeFormats instance that will format date/time instances in the time zone of the passed-in
     * DateTimeFormatter.
     *
     * @param dateTimeFormatter a DateTimeFormatter
     */
    public DateTimeVelocityUtils(DateTimeFormatter dateTimeFormatter)
    {
        this.dateTimeFormatter = dateTimeFormatter != null ? dateTimeFormatter.forLoggedInUser() : null;
    }

    /**
     * Returns a string representation of the given date/time in DateTimeStyle#COMPLETE format.
     *
     * @param date a date/time to format
     * @return an string containing the given date/time (HTML-escaped)
     */
    public String formatDMYHMS(Date date)
    {
        return escapeHtml(dateTimeFormatter.forLoggedInUser().withStyle(DateTimeStyle.COMPLETE).format(date));
    }

    /**
     * Returns a string representation of the given date/time in DateTimeStyle#DATE format.
     *
     * @param date a date/time to format
     * @return an string containing the given date/time (HTML-escaped)
     */
    public String formatDMY(Date date)
    {
        return escapeHtml(dateTimeFormatter.forLoggedInUser().withSystemZone().withStyle(DateTimeStyle.DATE).format(date));
    }

    /**
     * Returns a string representation of the given date/time in DateTimeStyle#TIME format.
     *
     * @param date a date/time to format
     * @return an string containing the given date/time (HTML-escaped)
     */
    public String formatTime(Date date)
    {
        return escapeHtml(dateTimeFormatter.forLoggedInUser().withStyle(DateTimeStyle.TIME).format(date));
    }

    /**
     * Returns a string representation of the given date/time in DateTimeStyle#RSS_RFC822_DATE_TIME format.
     *
     * @param date a date/time to format
     * @return an string containing the given date/time (XML-escaped)
     */
    public String formatRSS(Date date)
    {
        return escapeXml(dateTimeFormatter.forLoggedInUser().withStyle(DateTimeStyle.RSS_RFC822_DATE_TIME).format(date));
    }
}
