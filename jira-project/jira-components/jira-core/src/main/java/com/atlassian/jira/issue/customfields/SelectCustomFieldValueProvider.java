package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the {@link com.atlassian.jira.issue.customfields.CustomFieldValueProvider} for the {@link com.atlassian.jira.issue.customfields.searchers.MultiSelectSearcher}
 *
 * @since v4.0
 */
public final class SelectCustomFieldValueProvider implements CustomFieldValueProvider
{
    ///CLOVER:OFF

    public Object getStringValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
    {
        CustomFieldParams customFieldParams = customField.getCustomFieldValues(fieldValuesHolder);
        return customFieldParams.getValuesForNullKey();
    }

    public Object getValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
    {
        CustomFieldType customFieldType = customField.getCustomFieldType();
        final CustomFieldParams customFieldParams = customField.getCustomFieldValues(fieldValuesHolder);
        return customFieldType.getValueFromCustomFieldParams(customFieldParams);
    }
    ///CLOVER:ON
}