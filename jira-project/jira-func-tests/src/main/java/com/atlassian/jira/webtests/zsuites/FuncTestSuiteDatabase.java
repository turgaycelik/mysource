package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.database.TestDatabaseSystemTimeReader;

import junit.framework.Test;

/**
 * Raw-database releated tests.
 *
 * @since 6.3
 */
public class FuncTestSuiteDatabase extends FuncTestSuite
{
    /**
     * The suite instance.
     */
    public static final FuncTestSuiteDatabase SUITE = new FuncTestSuiteDatabase();

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

    public FuncTestSuiteDatabase()
    {
        addTestsInPackage(TestDatabaseSystemTimeReader.class.getPackage().getName(), true);
    }
}
