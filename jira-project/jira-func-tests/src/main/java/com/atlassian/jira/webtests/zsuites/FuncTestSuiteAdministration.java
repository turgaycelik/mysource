package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.TestAdminSectionVisibility;
import junit.framework.Test;

/**
 * Test for the administration of the JIRA instance. This is a kind of catch all for those configuration tests
 * that don't need their own suite. Consider adding the tests to a more specific test suite if possible.
 *
 * @since v4.0
 */
public class FuncTestSuiteAdministration extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteAdministration();

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

    public FuncTestSuiteAdministration()
    {
        addTestsInPackage("com.atlassian.jira.webtests.ztests.admin", true);
    }
}