package com.atlassian.jira.external.beans;

/**
 * Represents a Group (OSGroup) object.
 *
 * @since v3.13
 */
public class ExternalGroup
{
    private String name;

    public ExternalGroup(final String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }
}
