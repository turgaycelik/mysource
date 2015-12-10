package com.atlassian.jira.startup;

/**
 * Represents a generic failed StartupCheck.
 *
 * @since v4.0
 */
public class FailedStartupCheck implements StartupCheck
{
    private final String name;
    private final String faultDescription;

    public FailedStartupCheck(final String name, final String faultDescription)
    {
        this.name = name;
        this.faultDescription = faultDescription;
    }

    public String getName()
    {
        return name;
    }

    public boolean isOk()
    {
        return false;
    }

    public String getFaultDescription()
    {
        return faultDescription;
    }

    public String getHTMLFaultDescription()
    {
        return faultDescription;
    }

    @Override
    public void stop()
    {
    }

    @Override
    public String toString()
    {
        return name;
    }
}
