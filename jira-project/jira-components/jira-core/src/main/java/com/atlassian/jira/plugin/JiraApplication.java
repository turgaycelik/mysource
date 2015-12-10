package com.atlassian.jira.plugin;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.Application;
import com.google.common.base.Objects;

final class JiraApplication implements Application
{
    private final String key;
    private final BuildUtilsInfo buildUtilsInfo;

    JiraApplication(String key, BuildUtilsInfo buildUtilsInfo)
    {
        this.key = Assertions.notNull(key);
        this.buildUtilsInfo = Assertions.notNull(buildUtilsInfo);
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public String getVersion()
    {
        return buildUtilsInfo.getVersion();
    }

    @Override
    public String getBuildNumber()
    {
        return buildUtilsInfo.getCurrentBuildNumber();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final JiraApplication that = (JiraApplication) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(key);
    }
}
