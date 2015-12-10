package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.Group;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @since v4.3
 */
public class MockGroup implements Group
{
    private String name;

    public MockGroup(final String name)
    {
        this.name = name;
    }

    public boolean isActive()
    {
        return false;
    }

    public String getDescription()
    {
        return null;
    }

    public Long getDirectoryId()
    {
        return null;
    }

    public String getName()
    {
        return name;
    }

    public int compareTo(final Group o)
    {
        return name.compareTo(o.getName());
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("name", name).
                toString();
    }
}
