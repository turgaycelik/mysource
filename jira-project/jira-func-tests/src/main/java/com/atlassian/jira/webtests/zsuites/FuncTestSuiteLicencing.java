package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.license.TestLicenseFooters;
import com.atlassian.jira.webtests.ztests.license.TestLicenseMessages;
import com.atlassian.jira.webtests.ztests.license.TestPersonalLicense;
import com.atlassian.jira.webtests.ztests.license.TestUserLimitedLicense;
import com.atlassian.jira.webtests.ztests.license.TestViewLicense;
import com.atlassian.jira.webtests.ztests.misc.TestSetup;
import junit.framework.Test;

/**
 * A suite of tests around JIRA licence handling
 *
 * @since v4.0
 */
public class FuncTestSuiteLicencing extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteLicencing();

    /**
     * The pattern in JUnit/IDEA JUnit runner is that if a class has a static suite() method that returns a Test, then
     * this is the entry point for running your tests.  So make sure you declare one of these in the FuncTestSuite
     * implementation.
     *
     * @return a Test that can be run by as JUnit TestRunner
     */
    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteLicencing()
    {
        addTest(TestViewLicense.class);
        addTest(TestPersonalLicense.class);
        addTest(TestUserLimitedLicense.class);
        addTest(TestLicenseMessages.class);
        addTest(TestSetup.class);
        addTest(TestLicenseFooters.class);
    }
}