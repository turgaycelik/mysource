package com.atlassian.jira.datetime;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import javax.annotation.concurrent.ThreadSafe;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Formats a DateTime as a complete date & time relative to now.
 *
 * @see DateTimeStyle#RELATIVE
 * @since 4.4
 */
@ThreadSafe
class DateTimeRelativeFormatter implements DateTimeFormatStrategy
{
    private final DateTimeFormatterServiceProvider serviceProvider;
    private final JodaFormatterSupplier jodaFormatterSupplier;
    private final ApplicationProperties applicationProperties;
    private final Clock clock;
    private final DateTimeTimeFormatter timeFormatter;
    private final DateTimeCompleteFormatter completeFormatter;

    public DateTimeRelativeFormatter(DateTimeFormatterServiceProvider serviceProvider, JodaFormatterSupplier jodaFormatterSupplier, ApplicationProperties applicationProperties, Clock clock)
    {
        this.serviceProvider = serviceProvider;
        this.jodaFormatterSupplier = jodaFormatterSupplier;
        this.applicationProperties = applicationProperties;
        this.clock = clock;
        timeFormatter = new DateTimeTimeFormatter(serviceProvider, jodaFormatterSupplier);
        completeFormatter = new DateTimeCompleteFormatter(serviceProvider, jodaFormatterSupplier);
    }

    @Override
    public String format(DateTime dateTime, Locale locale)
    {
        DateTime now = new DateTime(clock.getCurrentDate(), dateTime.getZone());

        // fall back to complete dates if relative dates are disabled
        if (!isRelativeDateFormattingEnabled())
        {
            return completeFormatter.format(dateTime, locale);
        }

        // calculate the # of days between the start of each day. this should work with funky DST edge cases
        DateTime formatStartOfDay = new LocalDate(dateTime).toDateTimeAtStartOfDay();
        DateTime todayStartOfDay = new LocalDate(now).toDateTimeAtStartOfDay();
        int days = Days.daysBetween(formatStartOfDay, todayStartOfDay).getDays();

        //In the future
        if (days < 0)
        {
           return completeFormatter.format(dateTime, locale);
        }

        // today
        if (days < 1)
        {
            String todayFmt = serviceProvider.getUnescapedText("common.concepts.today");

            return new MessageFormat(todayFmt).format(new Object[] { timeFormatter.format(dateTime, locale) });
        }

        // yesterday
        if (days < 2)
        {
            String yesterdayFmt = serviceProvider.getUnescapedText("common.concepts.yesterday");

            return new MessageFormat(yesterdayFmt).format(new Object[] { timeFormatter.format(dateTime, locale) });
        }

        // last week
        if (days < 7)
        {
            String pattern = serviceProvider.getDefaultBackedString(APKeys.JIRA_LF_DATE_DAY);

            return jodaFormatterSupplier.get(new JodaFormatterSupplier.Key(pattern, locale)).print(dateTime);
        }

        // complete format
        return completeFormatter.format(dateTime, locale);
    }

    @Override
    public Date parse(String text, DateTimeZone timeZone, Locale locale)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public DateTimeStyle style()
    {
        return DateTimeStyle.RELATIVE;
    }

    @Override
    public String pattern()
    {
        return completeFormatter.pattern();
    }

    protected boolean isRelativeDateFormattingEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_LF_DATE_RELATIVE);
    }
}
