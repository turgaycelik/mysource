package com.atlassian.jira.datetime;

import javax.annotation.concurrent.Immutable;
import java.util.Date;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.StringEscapeUtils.escapeXml;

/**
 * Aggregates the most commonly-used date formats in a single instance, for use within Velocity templates. All strings
 * returned by this class are in the system time zone, and are either HTML- or XML-escaped (as appropriate).
 *
 * @since v5.0
 */
@Immutable
public class DateVelocityUtils
{
    private final DateTimeFormatter dateTimeFormatter;

    public DateVelocityUtils(DateTimeFormatter dateTimeFormatter)
    {
        this.dateTimeFormatter = dateTimeFormatter != null ? dateTimeFormatter.forLoggedInUser().withSystemZone() : null;
    }

    /**
     * Returns a string representation of the given date in DateTimeStyle#RELATIVE_WITHOUT_TIME format.
     *
     * @param date a date/time to format
     * @return an string containing the given date/time (HTML-escaped)
     */
    public String formatRelative(Date date)
    {
        return escapeHtml(dateTimeFormatter.withStyle(DateTimeStyle.RELATIVE_WITHOUT_TIME).format(date));
    }

    /**
     * Returns a string representation of the given date in DateTimeStyle#RSS_RFC822_DATE_TIME format.
     *
     * @param date a date/time to format
     * @return an string containing the given date/time (XML-escaped)
     */
    public String formatRSS(Date date)
    {
        return escapeXml(dateTimeFormatter.withStyle(DateTimeStyle.RSS_RFC822_DATE_TIME).format(date));
    }
}
