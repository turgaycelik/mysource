package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.index.TestReindexMessages;
import junit.framework.Test;

/**
 * Some tests for custom fields.
 *
 * @since v4.0
 */
public class FuncTestSuiteCustomFields extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteCustomFields();

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

    public FuncTestSuiteCustomFields()
    {
        addTestsInPackage("com.atlassian.jira.webtests.ztests.customfield", true);
        addTest(TestReindexMessages.class);
    }
}