package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.Date;

public class DatePickerConverterImpl implements DatePickerConverter
{
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final DateFieldFormat dateFieldFormat;

    public DatePickerConverterImpl(JiraAuthenticationContext jiraAuthenticationContext, DateFieldFormat dateFieldFormat)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.dateFieldFormat = dateFieldFormat;
    }

    public String getString(final Date value)
    {
        if (value == null)
        {
            return "";
        }

        return dateFieldFormat.formatDatePicker(value);
    }

    public Timestamp getTimestamp(final String stringValue) throws FieldValidationException
    {
        if (StringUtils.isEmpty(stringValue))
        { return null; }
        try
        {
            final Date date = dateFieldFormat.parseDatePicker(stringValue);
            final long time = date.getTime();

            return new Timestamp(time);
        }
        catch (IllegalArgumentException e)
        {
            final I18nHelper i18nBean = jiraAuthenticationContext.getI18nHelper();
            throw new FieldValidationException(i18nBean.getText("fields.validation.data.format", dateFieldFormat.getFormatHint()));
        }
    }
}
