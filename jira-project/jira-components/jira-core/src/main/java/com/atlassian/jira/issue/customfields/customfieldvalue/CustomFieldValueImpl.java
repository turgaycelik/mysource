package com.atlassian.jira.issue.customfields.customfieldvalue;



public class CustomFieldValueImpl implements CustomFieldValue
{
    private String value;
    private Long parentKey;


    public CustomFieldValueImpl(String value, Long parentKey)
    {
        this.value = value;
        this.parentKey = parentKey;
    }

    public String getValue()
    {
        return value;
    }

    public Long getParentKey()
    {
        return parentKey;
    }

    public String toString()
    {
        return getValue();
    }
}
