package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.i18n.TestI18n500Page;
import com.atlassian.jira.webtests.ztests.i18n.TestTranslateSubTasks;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestTimeTrackingLocalization;
import junit.framework.Test;

/**
 * I18n tests
 *
 * @since v4.0
 */
public class FuncTestSuiteI18n extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteI18n();

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

    public FuncTestSuiteI18n()
    {
        addTest(TestI18n500Page.class);
        addTest(TestTranslateSubTasks.class);
        addTest(TestTimeTrackingLocalization.class);
    }
}