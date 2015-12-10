package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.customfields.searchers.NumberRangeSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;

/**
 * An implementation of the {@link com.atlassian.jira.issue.customfields.CustomFieldValueProvider}
 * for retreiving number range values.
 *
 * @since v4.0
 */
public final class NumberRangeCustomFieldValueProvider implements CustomFieldValueProvider
{
    ///CLOVER:OFF
    public Object getStringValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
    {
        return customField.getCustomFieldValues(fieldValuesHolder);
    }

    public Object getValue(CustomField customField, FieldValuesHolder fieldValuesHolder)
    {
        return getStringValue(customField, fieldValuesHolder);
    }

    public String getGreaterThanKey()
    {
        return NumberRangeSearcher.GREATER_THAN_PARAM;
    }

    public String getLessThanKey()
    {
        return NumberRangeSearcher.GREATER_THAN_PARAM;
    }
    ///CLOVER:ON
}
