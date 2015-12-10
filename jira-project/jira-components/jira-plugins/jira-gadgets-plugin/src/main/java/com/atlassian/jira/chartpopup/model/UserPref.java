package com.atlassian.jira.chartpopup.model;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * UserPreference key value pair DTO used for REST calls.
 */
@XmlRootElement
public class UserPref
{
    @XmlElement
    private final String key;

    @XmlElement
    private final String value;

    @SuppressWarnings({"UnusedDeclaration", "unused"})
    private UserPref()
    {
        key = null;
        value = null;
    }

    public UserPref(final String key, final String value)
    {
        this.key = key;
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }
}
