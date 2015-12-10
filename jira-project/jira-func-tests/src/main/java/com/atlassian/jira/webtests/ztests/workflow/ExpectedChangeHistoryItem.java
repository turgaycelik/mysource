package com.atlassian.jira.webtests.ztests.workflow;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @since v3.13
 */
public class ExpectedChangeHistoryItem
{
    private String fieldName;
    private String oldValue;
    private String newValue;

    public ExpectedChangeHistoryItem(String fieldName, String oldValue, String newValue)
    {
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getOldValue()
    {
        return oldValue;
    }

    public String getNewValue()
    {
        return newValue;
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
