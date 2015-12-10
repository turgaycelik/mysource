package com.atlassian.jira.upgrade;

/**
 * Simple implementation of {@link com.atlassian.jira.upgrade.BuildVersionRegistry.BuildVersion}
 *
 * @since v4.1
 */
public final class BuildVersionImpl implements BuildVersionRegistry.BuildVersion
{
    private final String buildNumber;
    private final String version;

    public BuildVersionImpl(final String buildNumber, final String version)
    {
        this.buildNumber = com.atlassian.jira.util.dbc.Assertions.notNull("buildNumber", buildNumber);
        this.version = com.atlassian.jira.util.dbc.Assertions.notNull("version", version);
    }

    public String getBuildNumber()
    {
        return buildNumber;
    }

    public String getVersion()
    {
        return version;
    }

    @Override
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

        final BuildVersionImpl that = (BuildVersionImpl) o;

        if (buildNumber != null ? !buildNumber.equals(that.buildNumber) : that.buildNumber != null)
        {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = buildNumber != null ? buildNumber.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
