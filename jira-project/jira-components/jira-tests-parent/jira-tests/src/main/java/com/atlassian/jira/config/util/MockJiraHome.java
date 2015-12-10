package com.atlassian.jira.config.util;

import javax.annotation.Nonnull;

import java.io.File;

public class MockJiraHome extends AbstractJiraHome
{
    private String localHomePath;
    private String sharedHomePath;

    public MockJiraHome()
    {
        this("/jira_home/");
    }

    public MockJiraHome(final String homePath)
    {
        this.sharedHomePath = homePath;
        this.localHomePath = homePath;
    }

    public MockJiraHome(final String localHomePath, final String sharedHomePath)
    {
        this.sharedHomePath = sharedHomePath;
        this.localHomePath = localHomePath;
    }


    @Nonnull
    public File getHome()
    {
        return new File(sharedHomePath);
    }

    @Nonnull
    @Override
    public File getLocalHome()
    {
        return new File(localHomePath);
    }

    public void setHomePath(final String homePath)
    {
        this.sharedHomePath = homePath;
        this.localHomePath = homePath;
    }
}
