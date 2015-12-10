package com.atlassian.jira.external.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

public class ExternalUser
{
    // @TODO refactor Remote RPC objects to use this

    String id;
    String key;
    String name;
    String fullname;
    String email;
    String password;
    String passwordHash;
    Map<String,String> userPropertyMap = new HashMap<String,String>();

    public ExternalUser()
    {
    }

    public ExternalUser(String name, String fullname)
    {
        this.name = name;
        this.fullname = fullname;
    }

    public ExternalUser(String name, String fullname, String email)
    {
        this.name = name;
        this.fullname = fullname;
        this.email = email;
    }

    public ExternalUser(String name, String fullname, String email, String password)
    {
        this.name = name;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
    }

    public ExternalUser(String key, String name, String fullname, String email, String password)
    {
        this.key = key;
        this.name = name;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
    }

    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Getters and Setters
    //------------------------------------------------------------------------------------------------------------------

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(final String key)
    {
        this.key = key;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPasswordHash()
    {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash)
    {
        this.passwordHash = passwordHash;
    }

    public Map<String,String> getUserPropertyMap()
    {
        return userPropertyMap;
    }

    public void setUserProperty(String propertyName, String value)
    {
        userPropertyMap.put(propertyName, value);
    }
}
