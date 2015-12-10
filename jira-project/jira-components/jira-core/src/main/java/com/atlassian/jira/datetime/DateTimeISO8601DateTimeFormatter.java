package com.atlassian.jira.datetime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.Locale;

/**
 * @since v4.4
 */
class DateTimeISO8601DateTimeFormatter implements DateTimeFormatStrategy
{
    /**
     * This format stores the local time plus offset from UTC.
     */
    public static String ISO8601_LOCAL_TIME = "yyyy-MM-dd'T'HH:mm:ssZ";

    /**
     * This format contains the time in UTC, with a literal Z at the end.
     */
    public static String ISO8601_UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private final JodaFormatterSupplier cache;

    public DateTimeISO8601DateTimeFormatter(JodaFormatterSupplier cache)
    {
        this.cache = cache;
    }

    @Override
    public String format(DateTime dateTime, Locale locale)
    {
        DateTimeFormatter formatter = cache.get(new JodaFormatterSupplier.Key(ISO8601_LOCAL_TIME, Locale.US));
        return formatter.print(dateTime);
    }

    @Override
    public Date parse(String text, DateTimeZone timeZone, Locale locale)
    {
        try
        {
            // try to use "local time" format first. this is the preferred
            DateTimeFormatter formatter = cache.get(new JodaFormatterSupplier.Key(ISO8601_LOCAL_TIME, Locale.US)).withZone(timeZone);

            return formatter.parseDateTime(text).toDate();
        }
        catch (IllegalArgumentException e)
        {
            // also try to support UTC format
            DateTimeFormatter formatter = cache.get(new JodaFormatterSupplier.Key(ISO8601_UTC, Locale.US)).withZone(DateTimeZone.UTC);

            return formatter.parseDateTime(text).toDate();
        }
    }

    @Override
    public DateTimeStyle style()
    {
        return DateTimeStyle.ISO_8601_DATE_TIME;
    }

    @Override
    public String pattern()
    {
        return ISO8601_LOCAL_TIME;
    }
}
