package com.atlassian.jira.webtests.util;

import junit.framework.TestCase;

import java.io.File;
import java.net.URL;

public interface JIRAEnvironmentData extends com.atlassian.jira.testkit.client.JIRAEnvironmentData
{
    String getContext();

    /**
     * Returns the tenant to run requests on.  This triggers X-Atlassian-Tenant to be set for each request to this
     * value.  If null, no header is set.
     *
     * @return The tenant
     */
    String getTenant();

    /**
     * Whether a dummy tenant should be created after the tenant is created.  This is useful for picking up issues where
     * tenant specific state is statically referenced, so the most recently created tenant is fine, but older tenants
     * fail.  The dummy tenant will be fine, but the tests will be run against the older tenant.
     * 
     * @return true if a dummy tenant should be created after the tenant is created.
     */
    boolean shouldCreateDummyTenant();

    URL getBaseUrl();

    File getXMLDataLocation();

    /**
     * Returns a directory that can be used by the func tests whenever file operations need to be done.
     *
     * <p> This directory is guaranteed to not be used by other func test instances simultaneously running on the same machine in Bamboo.
     *
     * <p> This should return a canonical file name (ie an absolute file name, without any '..' parts in it).
     *
     * @return a directory that can be used by the func tests whenever file operations need to be done.
     * @see java.io.File#getCanonicalFile()
     */
    File getWorkingDirectory();

    /**
     * @return a directory that can be used by the func tests whenever file operations need to be done.
     * @deprecated Please use {@link #getWorkingDirectory()}. This was renamed to avoid confusion with the "jira-home" functionality. Deprecated since v4.0
     */
    @Deprecated
    File getJIRAHomeLocation();

    /**
     * This is the expected release information shown under "Installation Type" on the sysinfo page.
     * @return the expected release information shown under "Installation Type" on the sysinfo page.
     */
    String getReleaseInfo();

    boolean isBundledPluginsOnly();

    boolean isAllTests();

    boolean isSingleNamedTest();

    /** Run a specifically named test instead of the normal suite */
    Class<? extends TestCase> getSingleTestClass();

    /**
     * A special suite of tests that connect to an LDAP server on LabManager (TPM).
     *
     * @return true if we are running the TPM LDAP tests only.
     */
    public boolean isTpmLdapTests();

    /**
     * Experimental Judge Judy build.
     *
     * @return true if we are running the Judge Judy tests.
     */
    public boolean isBlame();

    /**
     * Returns a property from the test environment.
     * If the property is not available it returns null.
     * 
     * @param key the property key
     * @return a property from the test environment, or null if not available.
     */
    public String getProperty(String key);
}
