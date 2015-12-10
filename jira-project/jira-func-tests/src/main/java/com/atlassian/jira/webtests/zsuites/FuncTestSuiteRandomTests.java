package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.misc.TestJiraLockedError;
import com.atlassian.jira.webtests.ztests.misc.TestOpenSearchProvider;
import com.atlassian.jira.webtests.ztests.misc.TestReportProblem;
import com.atlassian.jira.webtests.ztests.misc.TestResourceHeaders;
import com.atlassian.jira.webtests.ztests.misc.TestSeraphAuthType;
import com.atlassian.jira.webtests.ztests.misc.TestUserAgent;
import com.atlassian.jira.webtests.ztests.misc.TestXSS;
import com.atlassian.jira.webtests.ztests.sendheadearly.TestSendHeadEarly;
import junit.framework.Test;

/**
 * This is a suite of radom test that have nothing no real place currently. Please don't add anything here is you can
 * avoid it (pretty please?).
 *
 * @since v4.0
 */
public class FuncTestSuiteRandomTests extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteRandomTests();

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

    public FuncTestSuiteRandomTests()
    {
        addTest(TestResourceHeaders.class);
        addTest(TestXSS.class);
        addTest(TestJiraLockedError.class);
        addTest(TestUserAgent.class);
        addTest(TestSeraphAuthType.class);
        addTest(TestOpenSearchProvider.class);
        addTest(TestReportProblem.class);
        addTest(TestSendHeadEarly.class);
    }
}
