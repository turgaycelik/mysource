package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.util.OutlookDate;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

public class DateTimeConverterImpl implements DateTimeConverter
{
    private final DateTimeFormatter dateTimeFormatter;

    public DateTimeConverterImpl(DateTimeFormatter dateTimeFormatter)
    {
        this.dateTimeFormatter = dateTimeFormatter != null ? dateTimeFormatter.forLoggedInUser().withStyle(DateTimeStyle.COMPLETE) : null;
    }

    public String getString(Date value)
    {
        if (value == null)
        {
            return "";
        }

        return dateTimeFormatter.format(value);
    }

    public Timestamp getTimestamp(String stringValue) throws FieldValidationException
    {
        if (stringValue == null)
        {
            return null;
        }

        try
        {
            return new Timestamp(dateTimeFormatter.parse(stringValue).getTime());
        }
        catch (IllegalArgumentException pe)
        {
            throw new FieldValidationException("Invalid date / time format.  Expected " + dateTimeFormatter.getFormatHint());
        }
    }
}
