package com.atlassian.jira.util;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;

import java.util.Date;

/**
 * Default implementation for formatting and parsing dates in JIRA.
 *
 * @since v4.4
 */
public class DateFieldFormatImpl implements DateFieldFormat
{
    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter datePickerFormatter;

    public DateFieldFormatImpl(DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        DateTimeFormatter systemTimeZoneFormatter = dateTimeFormatterFactory.formatter().forLoggedInUser().withSystemZone();

        dateFormatter = systemTimeZoneFormatter.withStyle(DateTimeStyle.DATE);
        datePickerFormatter = systemTimeZoneFormatter.withStyle(DateTimeStyle.DATE_PICKER);
    }

    @Override
    public String format(Date date)
    {
        return this.dateFormatter.format(date);
    }

    @Override
    public String formatDatePicker(Date date)
    {
        return this.datePickerFormatter.format(date);
    }

    @Override
    public Date parseDatePicker(String text) throws IllegalArgumentException
    {
        return datePickerFormatter.parse(text);
    }

    @Override
    public boolean isParseable(String releaseDate)
    {
        try
        {
            parseDatePicker(releaseDate);
            return true;
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    @Override
    public String getFormatHint()
    {
        return datePickerFormatter.getFormatHint();
    }
}
