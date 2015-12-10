package com.atlassian.jira.imports.project.mapper;

import com.atlassian.jira.util.dbc.Assertions;

/**
 * Used to hold a Id and key of a value that exists in either a backup JIRA or a live version of JIRA.
 */
public class IdKeyPair
{
    private final String id;
    private final String key;

    public IdKeyPair(final String id, final String key)
    {
        Assertions.notNull("id", id);
        this.id = id;
        this.key = key;
    }

    /**
     * Holds the string representation of the database id of the value, must not be null.
     * @return string representing the id of the value.
     */
    public String getId()
    {
        return id;
    }

    /**
     * Holds the string representation of the descriptive portion of the value (e.g. HSP-1 for an issue, or Bug for
     * an issue type). This value can be null.
     * @return descriptive representation of the value (key).
     */
    public String getKey()
    {
        return key;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final IdKeyPair idKeyPair = (IdKeyPair) o;

        if (id != null ? !id.equals(idKeyPair.id) : idKeyPair.id != null)
        {
            return false;
        }
        if (key != null ? !key.equals(idKeyPair.key) : idKeyPair.key != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
