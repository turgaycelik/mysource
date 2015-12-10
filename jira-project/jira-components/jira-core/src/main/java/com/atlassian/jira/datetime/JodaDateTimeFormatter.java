package com.atlassian.jira.datetime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.Locale;

/**
 * Base class for formatter strategies that just delegate to JodaTime's formatter.
 *
 * @since 4.4
 */
class JodaDateTimeFormatter implements DateTimeFormatStrategy
{
    private final DateTimeFormatterServiceProvider serviceProvider;
    private final JodaFormatterSupplier cache;
    private final String patternPropertyName;
    private final DateTimeStyle style;

    protected JodaDateTimeFormatter(DateTimeFormatterServiceProvider serviceProvider, JodaFormatterSupplier cache, String patternPropertyName, DateTimeStyle style)
    {
        this.serviceProvider = serviceProvider;
        this.cache = cache;
        this.patternPropertyName = patternPropertyName;
        this.style = style;
    }

    @Override
    public String format(DateTime dateTime, Locale locale)
    {
        DateTimeFormatter formatter = cache.get(new JodaFormatterSupplier.Key(pattern(), locale));

        return formatter.print(dateTime);
    }

    @Override
    public Date parse(String text, DateTimeZone timeZone, Locale locale)
    {
        DateTimeFormatter formatter = cache.get(new JodaFormatterSupplier.Key(pattern(), locale)).withZone(timeZone);

        return formatter.parseDateTime(text).toDate();
    }

    @Override
    public DateTimeStyle style()
    {
        return style;
    }

    @Override
    public String pattern()
    {
        return serviceProvider.getDefaultBackedString(patternPropertyName);
    }
}
