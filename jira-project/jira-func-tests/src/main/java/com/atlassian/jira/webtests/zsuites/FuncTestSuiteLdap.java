package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.tpm.ldap.TestBrowseUserDirectories;
import com.atlassian.jira.webtests.ztests.tpm.ldap.TestCrowdDirectoryMaintenance;
import com.atlassian.jira.webtests.ztests.tpm.ldap.TestDelegatingLdapDirectoryMaintenance;
import com.atlassian.jira.webtests.ztests.tpm.ldap.TestLdapDirectoryMaintenance;
import com.atlassian.jira.webtests.ztests.tpm.ldap.TestTpmDelegatingLdap;
import com.atlassian.jira.webtests.ztests.tpm.ldap.TestTpmLdap;
import com.atlassian.jira.webtests.ztests.tpm.ldap.TestTpmLdapAdvanced;
import com.atlassian.jira.webtests.ztests.tpm.ldap.TestTpmLdapRename;
import com.atlassian.jira.webtests.ztests.tpm.ldap.TestTpmLdapSetup;
import junit.framework.Test;

/**
 * @since v4.3
 */
public class FuncTestSuiteLdap extends FuncTestSuite
{
    public static final FuncTestSuite SUITE = new FuncTestSuiteLdap();

    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteLdap()
    {
        // TestTpmLdapSetup must be run FIRST
        addTpmLdapOnly(TestTpmLdapSetup.class);
        addTpmLdapOnly(TestTpmLdap.class);
        addTpmLdapOnly(TestTpmDelegatingLdap.class);
        addTpmLdapOnly(TestTpmLdapAdvanced.class);
        addTpmLdapOnly(TestTpmLdapRename.class);

        addTpmLdapOnly(TestBrowseUserDirectories.class);
        addTpmLdapOnly(TestCrowdDirectoryMaintenance.class);
        addTpmLdapOnly(TestDelegatingLdapDirectoryMaintenance.class);
        addTpmLdapOnly(TestLdapDirectoryMaintenance.class);
    }
}