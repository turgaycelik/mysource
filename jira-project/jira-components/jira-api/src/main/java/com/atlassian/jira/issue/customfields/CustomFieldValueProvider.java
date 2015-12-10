package com.atlassian.jira.issue.customfields;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;

/**
 * Provides access to custom field values for the purpose of rendering with custom field searchers.
 *
 * @since v4.0
 */
@PublicSpi
public interface CustomFieldValueProvider
{
    /**
     * Provides a string value representation of the value specified for this searcher. The result is used to populate
     * the velocity context with the variable "value".
     *
     * @param customField the custom field that is using this searcher.
     * @param fieldValuesHolder contains values populated by the populate methods of the input transformer.
     * @return a string value representation of the value specified for this searcher.
     */
    Object getStringValue(CustomField customField, FieldValuesHolder fieldValuesHolder);

    /**
     * Provides an object value representation of the value specified for this searcher. The result is used to populate
     * the velocity context with the variable "valueObject".
     *
     * @param customField the custom field that is using this searcher.
     * @param fieldValuesHolder contains values populated by the populate methods of the input transformer.
     * @return an object value representation of the value specified for this searcher.
     */
    Object getValue(CustomField customField, FieldValuesHolder fieldValuesHolder);
}
