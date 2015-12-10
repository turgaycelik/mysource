package com.atlassian.jira.util;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

/**
 * @since v6.2.3
 */
public class MockBuildUtilsInfo implements BuildUtilsInfo
{
    private String docVersion;

    @Override
    public String getVersion()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getDocVersion()
    {
        return docVersion;
    }

    public MockBuildUtilsInfo setDocVersion(final String docVersion)
    {
        this.docVersion = docVersion;
        return this;
    }

    @Override
    public int[] getVersionNumbers()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getCurrentBuildNumber()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getApplicationBuildNumber()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getDatabaseBuildNumber()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getMinimumUpgradableBuildNumber()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Date getCurrentBuildDate()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getBuildPartnerName()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getBuildInformation()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getSvnRevision()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getCommitId()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getMinimumUpgradableVersion()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Locale> getUnavailableLocales()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getSalVersion()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getApplinksVersion()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getLuceneVersion()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getGuavaOsgiVersion()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getBuildProperty(final String key)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isBeta()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isRc()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isSnapshot()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isMilestone()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
