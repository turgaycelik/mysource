package com.atlassian.jira.datetime;

import com.atlassian.jira.config.properties.APKeys;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Formats a DateTime as a complete date & time.
 *
 * @see DateTimeStyle#COMPLETE
 * @see APKeys#JIRA_LF_DATE_COMPLETE
 * @since 4.4
 */
@ThreadSafe
class DateTimeCompleteFormatter extends JodaDateTimeFormatter
{
    public DateTimeCompleteFormatter(DateTimeFormatterServiceProvider serviceProvider, JodaFormatterSupplier jodaFormatterSupplier)
    {
        super(serviceProvider, jodaFormatterSupplier, APKeys.JIRA_LF_DATE_COMPLETE, DateTimeStyle.COMPLETE);
    }
}
