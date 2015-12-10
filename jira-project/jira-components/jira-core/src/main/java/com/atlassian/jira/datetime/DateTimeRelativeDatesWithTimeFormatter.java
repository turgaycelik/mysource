package com.atlassian.jira.datetime;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneResolver;

/**
 *  A {@link DateTimeFormatStrategy} which returns a relative date (e.g. Last Wednesday 10:00 AM) if the date is not older than one week, otherwise it returns
 *  the date (e.g. 17/April). Only relative dates will contain the time.
 * @since v4.4
 */
class DateTimeRelativeDatesWithTimeFormatter extends AbstractDateTimeRelativeDatesFormatter
{
    public DateTimeRelativeDatesWithTimeFormatter(DateTimeFormatterServiceProvider serviceProvider, ApplicationProperties applicationProperties, TimeZoneResolver timeZoneInfoResolver, JiraAuthenticationContext jiraAuthenticationContext, JodaFormatterSupplier jodaFormatterSupplier, Clock clock)
    {
        super(serviceProvider, applicationProperties, timeZoneInfoResolver, jiraAuthenticationContext, new DateTimeTimeFormatter(serviceProvider, jodaFormatterSupplier), new DateTimeDateFormatter(serviceProvider, jodaFormatterSupplier), clock);
    }

    @Override
    public DateTimeStyle style()
    {
        return DateTimeStyle.RELATIVE_WITH_TIME_ONLY;
    }
}
