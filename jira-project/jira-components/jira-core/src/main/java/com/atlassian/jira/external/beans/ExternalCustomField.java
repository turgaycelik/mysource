package com.atlassian.jira.external.beans;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents a custom field object and its configuration. At the moment this is only really used when importing
 * from other JIRA instances.
 *
 * @since v3.13
 */
public class ExternalCustomField
{
    private final String id;
    private final String name;
    private final String typeKey;

    public ExternalCustomField(final String id, final String name, final String typeKey)
    {
        if ((id == null) || (name == null) || (typeKey == null))
        {
            throw new IllegalArgumentException("Can not construct an ExternalCustomField with null arguments.");
        }
        this.id = id;
        this.name = name;
        this.typeKey = typeKey;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getTypeKey()
    {
        return typeKey;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final ExternalCustomField that = (ExternalCustomField) o;

        if (!id.equals(that.id))
        {
            return false;
        }
        if (!name.equals(that.name))
        {
            return false;
        }
        if (!typeKey.equals(that.typeKey))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + typeKey.hashCode();
        return result;
    }
}
