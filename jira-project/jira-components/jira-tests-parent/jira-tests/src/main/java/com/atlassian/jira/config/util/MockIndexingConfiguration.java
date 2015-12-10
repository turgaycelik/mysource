package com.atlassian.jira.config.util;

public class MockIndexingConfiguration implements IndexingConfiguration
{
    private boolean indexingEnabled = true;
    private int issuesToForceOptimize = 4000;
    private int maxReindexes = 1000;

    public void disableIndex()
    {
        indexingEnabled = false;
    }

    public void enableIndex()
    {
        indexingEnabled = true;
    }

    public int getIndexLockWaitTime()
    {
        return 3000;
    }

    public int getIssuesToForceOptimize()
    {
        return issuesToForceOptimize;
    }

    public MockIndexingConfiguration issuesToForceOptimize(int issuesToForceOptimize)
    {
        this.issuesToForceOptimize = issuesToForceOptimize;
        return this;
    }

    public int getMaxReindexes()
    {
        return maxReindexes;
    }

    public MockIndexingConfiguration maxReindexes(int maxReindexes)
    {
        this.maxReindexes = maxReindexes;
        return this;
    }

    public boolean isIndexAvailable()
    {
        return indexingEnabled;
    }
}
