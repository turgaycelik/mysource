package com.atlassian.jira.datetime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.Locale;

/**
 * A date time formatter, which uses the RFC822 format.
 * This formatter is primarily used in the XML / RSS view.
 *
 * @since v4.4
 */
class DateTimeRFC822DateTimeFormatter implements DateTimeFormatStrategy
{
    private final JodaFormatterSupplier cache;
    public static String RSS_RFC822_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";

    public DateTimeRFC822DateTimeFormatter(JodaFormatterSupplier cache)
    {
        this.cache = cache;
    }

    @Override
    public String format(DateTime dateTime, Locale locale)
    {
        DateTimeFormatter formatter = cache.get(new JodaFormatterSupplier.Key(RSS_RFC822_FORMAT, locale));
        return formatter.print(dateTime);
    }

    @Override
    public Date parse(String text, DateTimeZone timeZone, Locale locale) throws UnsupportedOperationException
    {
        DateTimeFormatter formatter = cache.get(new JodaFormatterSupplier.Key(RSS_RFC822_FORMAT, locale)).withZone(timeZone);
        return formatter.parseDateTime(text).toDate();
    }

    @Override
    public DateTimeStyle style()
    {
        return DateTimeStyle.RSS_RFC822_DATE_TIME;
    }

    @Override
    public String pattern()
    {
        return RSS_RFC822_FORMAT;
    }
}
