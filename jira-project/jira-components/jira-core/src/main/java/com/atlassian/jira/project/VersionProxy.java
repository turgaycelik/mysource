/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.jira.project.version.Version;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * To contain a key / value pair because we want to manipulate the version strings.
 */
public class VersionProxy
{
    private long key;
    private String value;

    public VersionProxy(Version version)
    {
        this.key = version.getId().longValue();
        this.value = version.getName();
    }

    public VersionProxy(long key, String value)
    {
        this.key = key;
        this.value = value;
    }

    public long getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }

    ///CLOVER:OFF
    public String toString()
    {
        return new ToStringBuilder(this).append("key", key).append("value", value).toString();
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof VersionProxy))
            return false;

        final VersionProxy versionProxy = (VersionProxy) o;

        if (key != versionProxy.key)
            return false;
        if (value != null ? !value.equals(versionProxy.value) : versionProxy.value != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (int) (key ^ (key >>> 32));
        result = 29 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
