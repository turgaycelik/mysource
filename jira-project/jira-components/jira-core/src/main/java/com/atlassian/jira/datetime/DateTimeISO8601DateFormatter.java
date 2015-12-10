package com.atlassian.jira.datetime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.Locale;

/**
 *
 * @since v4.4
 */
class DateTimeISO8601DateFormatter implements DateTimeFormatStrategy
{
    private final JodaFormatterSupplier cache;
    public static String ISO8601_DATE_PATTERN = "yyyy-MM-dd";

    public DateTimeISO8601DateFormatter(JodaFormatterSupplier cache)
    {
        this.cache = cache;
    }

    @Override
    public String format(DateTime dateTime, Locale locale)
    {
        DateTimeFormatter formatter = cache.get(new JodaFormatterSupplier.Key(ISO8601_DATE_PATTERN, locale));
        return formatter.print(dateTime);
    }

    @Override
    public Date parse(String text, DateTimeZone timeZone, Locale locale)
    {
        DateTimeFormatter formatter = cache.get(new JodaFormatterSupplier.Key(ISO8601_DATE_PATTERN, locale)).withZone(timeZone);
        return formatter.parseDateTime(text).toDate();
    }

    @Override
    public DateTimeStyle style()
    {
        return DateTimeStyle.ISO_8601_DATE;
    }

    @Override
    public String pattern()
    {
        return ISO8601_DATE_PATTERN;
    }
}
