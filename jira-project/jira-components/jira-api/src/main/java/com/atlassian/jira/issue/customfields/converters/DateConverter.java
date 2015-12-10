package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Note that this interface is not provided to PICO/Spring directly - it is a super-interface that can be used for extending classes.
 */
@Internal
public interface DateConverter
{
    String getString(Date value);

    Timestamp getTimestamp(String stringValue) throws FieldValidationException;

}
