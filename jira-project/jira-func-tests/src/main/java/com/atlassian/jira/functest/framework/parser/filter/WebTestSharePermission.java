package com.atlassian.jira.functest.framework.parser.filter;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Simple SharePermission proxy for the Functional tests.
 *
 * @since v3.13
 */

public class WebTestSharePermission
{
    public static final String GLOBAL_TYPE = "global";
    public static final String GROUP_TYPE = "group";
    public static final String PROJECT_TYPE = "project";

    private final Long id;
    private final String type;
    private final String param1;
    private final String param2;

    public WebTestSharePermission(final Long id, final String type, final String param1, final String param2)
    {
        this.id = id;
        this.type = type;
        this.param1 = param1;
        this.param2 = param2;
    }

    public WebTestSharePermission(final String type, final String param1, final String param2)
    {
        this(null, type, param1, param2);
    }

    public Long getId()
    {
        return id;
    }

    public String getType()
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
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final WebTestSharePermission that = (WebTestSharePermission) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
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
        if (type != null ? !type.equals(that.type) : that.type != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (param1 != null ? param1.hashCode() : 0);
        result = 31 * result + (param2 != null ? param2.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
