package com.atlassian.jira.datetime;

import com.atlassian.jira.config.properties.APKeys;

/**
 * Formatter for DateTimeStyle#DATE_TIME_PICKER
 *
 * @since 4.4
 */
class DateTimePickerFormatter extends JodaDateTimeFormatter
{
    public DateTimePickerFormatter(DateTimeFormatterServiceProvider serviceProvider, JodaFormatterSupplier cache)
    {
        super(serviceProvider, cache, APKeys.JIRA_DATE_TIME_PICKER_JAVA_FORMAT, DateTimeStyle.DATE_TIME_PICKER);
    }
}
