package com.atlassian.jira.sharing;

import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.util.dbc.Assertions;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Simple Bean implementation of {@link SharePermission}.
 * 
 * @since v3.13
 */

public class SharePermissionImpl implements SharePermission
{
    private final Long id;
    private final ShareType.Name type;
    private final String param1;
    private final String param2;

    public SharePermissionImpl(final Long id, final ShareType.Name type, final String param1, final String param2)
    {
        Assertions.notNull("type", type);
        if ((param1 == null) && (param2 != null))
        {
            throw new IllegalArgumentException("param2 can not be null when param1 is not.");
        }

        this.id = id;
        this.type = type;
        this.param1 = param1;
        this.param2 = param2;
    }

    /**
     * A package level constructor to allow for special cases, around project roles.
     * 
     * @param type - the share type
     * @param param2 - the value of param2
     */
    SharePermissionImpl(final ShareType.Name type, final String param2)
    {
        Assertions.notNull("type", type);
        Assertions.notNull("param2", param2);

        id = null;
        this.type = type;
        param1 = null;
        this.param2 = param2;
    }

    public SharePermissionImpl(final ShareType.Name type, final String param1, final String param2)
    {
        this(null, type, param1, param2);
    }

    public Long getId()
    {
        return id;
    }

    public ShareType.Name getType()
    {
        return type;
    }

    public String getParam1()
    {
        return param1;
    }

    public String getParam2()
    {
        return param2;
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

        final SharePermissionImpl that = (SharePermissionImpl) o;

        if (!type.equals(that.type))
        {
            return false;
        }
        if (param1 != null ? !param1.equals(that.param1) : that.param1 != null)
        {
            return false;
        }
        if (param2 != null ? !param2.equals(that.param2) : that.param2 != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = type.hashCode();
        result = 31 * result + (param1 != null ? param1.hashCode() : 0);
        result = 31 * result + (param2 != null ? param2.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
