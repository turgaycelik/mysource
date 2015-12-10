package com.atlassian.jira.datetime;

import com.atlassian.jira.config.properties.APKeys;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Formats a DateTime as a time.
 *
 * @see DateTimeStyle#TIME
 * @since 4.4
 */
@ThreadSafe
class DateTimeTimeFormatter extends JodaDateTimeFormatter
{
    public DateTimeTimeFormatter(DateTimeFormatterServiceProvider serviceProvider, JodaFormatterSupplier cache)
    {
        super(serviceProvider, cache, APKeys.JIRA_LF_DATE_TIME, DateTimeStyle.TIME);
    }
}
