package com.atlassian.jira.issue.fields.rest;

import com.atlassian.annotations.PublicApi;

import java.util.Collection;

/**
 * This class is used to describe the data that this field supports
 *
 * @since v5.0
 */
@PublicApi
public class FieldTypeInfo
{
    private final Collection<?> allowedValues;
    private final String autoCompleteUrl;

    public FieldTypeInfo(Collection<?> allowedValues,
            String autoCompleteUrl)
    {
        this.allowedValues = allowedValues;
        this.autoCompleteUrl = autoCompleteUrl;
    }

    public Collection<?> getAllowedValues()
    {
        return allowedValues;
    }

    public String getAutoCompleteUrl()
    {
        return autoCompleteUrl;
    }
}
