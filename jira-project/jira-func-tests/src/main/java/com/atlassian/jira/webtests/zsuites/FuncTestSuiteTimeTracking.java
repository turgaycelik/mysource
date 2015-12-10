package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.TestTimeTrackingAdmin;
import com.atlassian.jira.webtests.ztests.issue.move.TestDeleteHiddenFieldOnMove;
import com.atlassian.jira.webtests.ztests.misc.TestReplacedLocalVelocityMacros;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveTimeTracking;
import junit.framework.Test;

/**
 * Responsible for holding the time tracking module's functional tests. 
 * @since v4.0
 */
public class FuncTestSuiteTimeTracking extends FuncTestSuite
{
    private final static String TIME_TRACKING_TESTS_PACKAGE = "com.atlassian.jira.webtests.ztests.timetracking";

    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteTimeTracking();

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

    public FuncTestSuiteTimeTracking()
    {
        addTestsInPackage(TIME_TRACKING_TESTS_PACKAGE, true);
        addTest(TestTimeTrackingAdmin.class);
        addTest(TestDeleteHiddenFieldOnMove.class);
        addTest(TestBulkMoveTimeTracking.class);

        //Holds tests for worklog.
        addTest(TestReplacedLocalVelocityMacros.class);
    }
}