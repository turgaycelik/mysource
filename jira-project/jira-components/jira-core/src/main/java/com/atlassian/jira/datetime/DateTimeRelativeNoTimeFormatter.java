package com.atlassian.jira.datetime;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneResolver;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;
import java.util.Locale;

/**
 *  A {@link DateTimeFormatStrategy} which returns a relative date (e.g. Last Wednesday) if the date is not older than one week, otherwise it returns
 *  the date (e.g. 17/April). No time information is added when formatting the date.
 *
 * @since v4.4
 */
class DateTimeRelativeNoTimeFormatter extends AbstractDateTimeRelativeDatesFormatter
{
    public DateTimeRelativeNoTimeFormatter(DateTimeFormatterServiceProvider serviceProvider, ApplicationProperties applicationProperties, TimeZoneResolver timeZoneInfoResolver, JiraAuthenticationContext jiraAuthenticationContext, JodaFormatterSupplier jodaFormatterSupplier, Clock clock)
    {
        super(serviceProvider, applicationProperties, timeZoneInfoResolver, jiraAuthenticationContext, createNoDateFormatter(), new DateTimeDateFormatter(serviceProvider, jodaFormatterSupplier), clock);
    }

    public static DateTimeFormatStrategy createNoDateFormatter()
    {
        return new DateTimeFormatStrategy()
        {
            @Override
            public String format(DateTime dateTime, Locale locale)
            {
                return "";
            }

            @Override
            public Date parse(String text, DateTimeZone timeZone, Locale locale)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public DateTimeStyle style()
            {
                return null;
            }

            @Override
            public String pattern()
            {
                return null;
            }
        };
    }

    @Override
    public DateTimeStyle style()
    {
        return DateTimeStyle.RELATIVE_WITHOUT_TIME;
    }
}
