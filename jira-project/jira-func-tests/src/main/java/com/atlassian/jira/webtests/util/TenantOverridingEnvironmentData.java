package com.atlassian.jira.webtests.util;

import junit.framework.TestCase;

import java.io.File;
import java.net.URL;

/**
 * @since v4.3
 */
public class TenantOverridingEnvironmentData implements JIRAEnvironmentData
{
    private final String tenant;
    private final JIRAEnvironmentData environmentData;

    public TenantOverridingEnvironmentData(String tenant, JIRAEnvironmentData environmentData)
    {
        this.tenant = tenant;
        this.environmentData = environmentData;
    }

    @Override
    public String getTenant()
    {
        return tenant;
    }

    @Override
    public boolean shouldCreateDummyTenant()
    {
        return environmentData.shouldCreateDummyTenant();
    }

    @Override
    public URL getBaseUrl()
    {
        return environmentData.getBaseUrl();
    }

    @Override
    public File getXMLDataLocation()
    {
        return environmentData.getXMLDataLocation();
    }

    @Override
    public File getWorkingDirectory()
    {
        return environmentData.getWorkingDirectory();
    }

    @Override
    @Deprecated
    public File getJIRAHomeLocation()
    {
        return environmentData.getJIRAHomeLocation();
    }

    @Override
    public String getReleaseInfo()
    {
        return environmentData.getReleaseInfo();
    }

    @Override
    public boolean isBundledPluginsOnly()
    {
        return environmentData.isBundledPluginsOnly();
    }

    @Override
    public boolean isAllTests()
    {
        return environmentData.isAllTests();
    }

    @Override
    public boolean isSingleNamedTest()
    {
        return environmentData.isSingleNamedTest();
    }

    @Override
    public Class<? extends TestCase> getSingleTestClass()
    {
        return environmentData.getSingleTestClass();
    }

    @Override
    public boolean isTpmLdapTests()
    {
        return environmentData.isTpmLdapTests();
    }

    @Override
    public boolean isBlame()
    {
        return environmentData.isBlame();
    }

    @Override
    public String getProperty(String key)
    {
        return environmentData.getProperty(key);
    }


    @Override
    public String getContext()
    {
        return environmentData.getContext();
    }
}
