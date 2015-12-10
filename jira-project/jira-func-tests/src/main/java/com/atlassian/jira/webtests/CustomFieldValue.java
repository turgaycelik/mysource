package com.atlassian.jira.webtests;

public class CustomFieldValue
{
    private String cfId;
    private String cfType;
    private String cfValue;

    public CustomFieldValue(String cfId, String cfType, String cfValue)
    {
        this.cfId = cfId;
        this.cfType = cfType;
        this.cfValue = cfValue;
    }

    public String getCfId()
    {
        return cfId;
    }

    public String getCfType()
    {
        return cfType;
    }

    public String getCfValue()
    {
        return cfValue;
    }
}
