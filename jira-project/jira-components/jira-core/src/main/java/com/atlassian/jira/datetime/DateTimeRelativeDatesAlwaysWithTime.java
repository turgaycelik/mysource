package com.atlassian.jira.datetime;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneResolver;

/**
 *  A {@link DateTimeFormatStrategy} which returns a relative date (e.g. Last Wednesday 10:00 AM) if the date is not older than one week, otherwise it returns
 *  the date (e.g. 17/April 10:00 AM). It will always add the time when formatting a date.
 *
 * @since v4.4
 */
class DateTimeRelativeDatesAlwaysWithTime extends AbstractDateTimeRelativeDatesFormatter
{
    public DateTimeRelativeDatesAlwaysWithTime(DateTimeFormatterServiceProvider serviceProvider, ApplicationProperties applicationProperties, TimeZoneResolver timeZoneInfoResolver, JiraAuthenticationContext jiraAuthenticationContext, JodaFormatterSupplier jodaFormatterSupplier, Clock clock)
    {
        super(serviceProvider, applicationProperties, timeZoneInfoResolver, jiraAuthenticationContext, new DateTimeTimeFormatter(serviceProvider, jodaFormatterSupplier), new DateTimeCompleteFormatter(serviceProvider, jodaFormatterSupplier), clock);
    }

    @Override
    public DateTimeStyle style()
    {
        return DateTimeStyle.RELATIVE_ALWAYS_WITH_TIME;
    }
}
