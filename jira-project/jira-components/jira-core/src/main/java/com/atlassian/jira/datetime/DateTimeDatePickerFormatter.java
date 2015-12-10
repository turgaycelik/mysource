package com.atlassian.jira.datetime;

import com.atlassian.jira.config.properties.APKeys;
import org.joda.time.DateTimeZone;

import java.util.Date;
import java.util.Locale;

/**
 * Formatter for date picker format.
 *
 * @since 4.4
 */
class DateTimeDatePickerFormatter extends JodaDateTimeFormatter
{
    private final DateTimePickerFormatter dateTimePickerFormatter;

    protected DateTimeDatePickerFormatter(DateTimeFormatterServiceProvider serviceProvider, JodaFormatterSupplier cache)
    {
        super(serviceProvider, cache, APKeys.JIRA_DATE_PICKER_JAVA_FORMAT, DateTimeStyle.DATE_PICKER);
        dateTimePickerFormatter = new DateTimePickerFormatter(serviceProvider, cache);
    }

    @Override
    public Date parse(String text, DateTimeZone timeZone, Locale locale)
    {
        try
        {
            return super.parse(text, timeZone, locale);
        }
        catch (IllegalArgumentException e)
        {
            // accept a date/time as well (for backward compatibility with OutlookDate)
            return dateTimePickerFormatter.parse(text, timeZone, locale);
        }
    }
}
