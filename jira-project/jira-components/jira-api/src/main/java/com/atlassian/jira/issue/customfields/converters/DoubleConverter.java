package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;

@Internal
public interface DoubleConverter
{
    String getString(Double value);

    String getStringForLucene(Double value);

    String getStringForLucene(String value);

    String getDisplayDoubleFromLucene(String luceneValue);

    Double getDouble(String stringValue) throws FieldValidationException;

    String getStringForChangelog(Double value);
}
