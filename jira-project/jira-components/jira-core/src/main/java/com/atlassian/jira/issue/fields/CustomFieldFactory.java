package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.CustomField;
import org.ofbiz.core.entity.GenericValue;

/**
 * Factory responsible of instantiating {@link com.atlassian.jira.issue.fields.CustomField} objects.
 */
@PublicApi
public interface CustomFieldFactory
{
    /**
     * Creates a new instance of {@link com.atlassian.jira.issue.fields.CustomField}.
     * @param genericValue A {@link GenericValue} that represents the custom field.
     * @return A new instance of {@link com.atlassian.jira.issue.fields.CustomField}, created from the given generic value.
     */
    CustomField create(final GenericValue genericValue);

    /**
     * Creates a new instance of {@link CustomField}, which is a copy of the given one.
     * @param customField The {@link CustomField} to copy from.
     * @return A new instance of {@link CustomField}, copied from the given one.
     */
    CustomField copyOf(final CustomField customField);
}
