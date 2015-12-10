package com.atlassian.jira.external.beans;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ExternalCustomFieldValue
{
    // @TODO refactor Remote RPC objects to use this

    public ExternalCustomFieldValue()
    {
    }

    public ExternalCustomFieldValue(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    private String customfieldId;
    private String key;

    String value;

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getCustomfieldId()
    {
        return customfieldId;
    }

    public void setCustomfieldId(String customfieldId)
    {
        this.customfieldId = customfieldId;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
