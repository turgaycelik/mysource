package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;

import java.sql.Timestamp;
import java.util.Date;

@Internal
public interface DateTimeConverter
{
    public String getString(Date value);

    public Timestamp getTimestamp(String stringValue) throws FieldValidationException;
}
