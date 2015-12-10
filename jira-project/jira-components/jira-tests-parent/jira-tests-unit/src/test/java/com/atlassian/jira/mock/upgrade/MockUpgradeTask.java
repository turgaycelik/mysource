package com.atlassian.jira.mock.upgrade;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.upgrade.UpgradeTask;

public class MockUpgradeTask implements UpgradeTask
{
    private final String version;
    private final String shortDescription;
    private final boolean reindexRequired;

    public MockUpgradeTask(final String version, final String shortDescription, boolean reindexRequired)
    {
        this.version = version;
        this.shortDescription = shortDescription;
        this.reindexRequired = reindexRequired;
    }

    public String getBuildNumber()
    {
        return version;
    }

    public String getShortDescription()
    {
        return shortDescription;
    }

    public void doUpgrade(boolean setupMode)
    {}

    public Collection<String> getErrors()
    {
        return Collections.emptyList();
    }

    @Override
    public String toString()
    {
        return "version: " + version + ", shortDesctription: " + shortDescription;
    }

    public String getClassName()
    {
        return "MockUpgradeTask" + version;
    }

    @Override
    public boolean isReindexRequired()
    {
        return reindexRequired;
    }
}
