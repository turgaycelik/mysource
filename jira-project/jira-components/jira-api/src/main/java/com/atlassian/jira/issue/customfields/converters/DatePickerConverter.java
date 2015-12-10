package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;

import java.sql.Timestamp;
import java.util.Date;
import java.util.GregorianCalendar;

@Internal
public interface DatePickerConverter extends DateConverter
{
    /**
     * This date is used as a placeholder in the Default Value of a Date Custom Field, to indicate that that we should
     * return the current date when getDefault() is called.
     */
    public static final Date USE_NOW_DATE = new Timestamp((new GregorianCalendar(1, 0, 1).getTime()).getTime());
    
    public String getString(final Date value);

    public Timestamp getTimestamp(final String stringValue) throws FieldValidationException;
}
