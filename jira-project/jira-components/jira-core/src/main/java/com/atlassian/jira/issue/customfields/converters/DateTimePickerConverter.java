package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.sql.Timestamp;
import java.util.Date;

public class DateTimePickerConverter implements DateConverter, DateTimeConverter
{
    private final DateTimeFormatter dateTimePickerFormatter;

    public DateTimePickerConverter(DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        this.dateTimePickerFormatter = dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.DATE_TIME_PICKER).forLoggedInUser();
    }

    public String getString(Date value)
    {
        if (value == null)
        {
            return "";
        }

        return dateTimePickerFormatter.format(value);
    }

    public Timestamp getTimestamp(String stringValue) throws FieldValidationException
    {
        if (stringValue == null)
            return null;
        
        try
        {
            Date date = dateTimePickerFormatter.parse(stringValue);
            final long time = date.getTime();
           
            return new Timestamp(time);
        }
        catch (IllegalArgumentException e)
        {
            throw new FieldValidationException("Invalid date / time format. Expected " + dateTimePickerFormatter.getFormatHint());
        }
    }
}
