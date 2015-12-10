package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import junit.framework.Test;

/**
 * A func test suite for shared entities such as Dashboards and Filters.  Not how this uses composition to create a test
 * suite
 *
 * @since v4.0
 */
public class FuncTestSuiteSharedEntities extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteSharedEntities();

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

    public FuncTestSuiteSharedEntities()
    {
        addTestSuite(FuncTestSuiteDashboards.SUITE);
        addTestSuite(FuncTestSuiteFilters.SUITE);
    }
}
