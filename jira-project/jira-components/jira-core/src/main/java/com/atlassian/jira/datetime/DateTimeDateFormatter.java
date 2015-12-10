package com.atlassian.jira.datetime;

import com.atlassian.jira.config.properties.APKeys;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Formats a DateTime as a date.
 *
 * @see DateTimeStyle#DATE
 * @since 4.4
 */
@ThreadSafe
class DateTimeDateFormatter extends JodaDateTimeFormatter
{
    public DateTimeDateFormatter(DateTimeFormatterServiceProvider serviceProvider, JodaFormatterSupplier cache)
    {
        super(serviceProvider, cache, APKeys.JIRA_LF_DATE_DMY, DateTimeStyle.DATE);
    }
}
