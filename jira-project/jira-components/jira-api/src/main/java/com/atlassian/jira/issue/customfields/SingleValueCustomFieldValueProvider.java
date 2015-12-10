package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;

import java.util.List;

/**
 * An implementation of the {@link CustomFieldValueProvider}
 * for retreiving single values. If a holder contains a non-empty lsit
 * the first value is returned.
 *
 * @since v4.0
 */
public final class SingleValueCustomFieldValueProvider implements CustomFieldValueProvider
{
    public Object getStringValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
    {
        return getValue(customField, fieldValuesHolder);
    }

    public Object getValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
    {
        final CustomFieldParams customFieldParams = customField.getCustomFieldValues(fieldValuesHolder);
        final Object obj = customField.getCustomFieldType().getStringValueFromCustomFieldParams(customFieldParams);
        if (obj instanceof List)
        {
            if (!((List) obj).isEmpty())
            {
                return ((List) obj).get(0);
            }
            else
            {
                return null;
            }
        }
        return obj;
    }
}
