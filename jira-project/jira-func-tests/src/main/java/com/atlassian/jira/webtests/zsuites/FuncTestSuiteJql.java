package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.issue.TestLabels;
import junit.framework.Test;

/**
 * A test suite for all JQL related func tests.
 *
 * @since v4.0
 */
public class FuncTestSuiteJql extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteJql();

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

    public FuncTestSuiteJql()
    {
        addTestsInPackage("com.atlassian.jira.webtests.ztests.navigator.jql", true);
        addTest(TestLabels.class);
    }
}
