package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;

/**
 * Default implementation of the {@link com.atlassian.jira.issue.customfields.CustomFieldValueProvider}
 *
 * @since v4.0
 */
public final class DefaultCustomFieldValueProvider implements CustomFieldValueProvider
{
    public Object getStringValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
    {
        CustomFieldParams customFieldParams = customField.getCustomFieldValues(fieldValuesHolder);
        return customField.getCustomFieldType().getStringValueFromCustomFieldParams(customFieldParams);
    }

    public Object getValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
    {
        CustomFieldParams customFieldParams = customField.getCustomFieldValues(fieldValuesHolder);
        return customField.getCustomFieldType().getValueFromCustomFieldParams(customFieldParams);
    }
}
