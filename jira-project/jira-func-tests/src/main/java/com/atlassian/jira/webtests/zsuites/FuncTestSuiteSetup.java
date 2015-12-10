package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.misc.TestDatabaseSetup;
import com.atlassian.jira.webtests.ztests.misc.TestDefaultJiraDataFromInstall;
import com.atlassian.jira.webtests.ztests.misc.TestJohnsonFiltersWhileNotSetup;
import junit.framework.Test;

/**
 * A set of tests that must be run before any JIRA data is imported into it. They will fail if run out of order.
 *
 * @since v4.0
 */
public class FuncTestSuiteSetup extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteSetup();

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

    public FuncTestSuiteSetup()
    {
        // NOTE: the tests that import data to run should go at the end of the tests, it
        // seems that some of the tests don't like the state of JIRA after one or many
        // of the imports. This needs to be looked at and fixed, but for now do not add
        // any of these tests anywhere other than the end of the list.

        // this test must run before JIRA is setup because it tests the
        // johnson event filter for this situation
        addTest(TestJohnsonFiltersWhileNotSetup.class);

        // this test must run before the rest as it sets up the db connection
        addTest(TestDatabaseSetup.class);

        /*
          THESE TESTS MUST RUN FIRST
          FOR TESTING JIRA BEING SETUP FROM SCRATCH
          (UPGRADE TASKS etc.)
        */
        addTest(TestDefaultJiraDataFromInstall.class);
    }
}