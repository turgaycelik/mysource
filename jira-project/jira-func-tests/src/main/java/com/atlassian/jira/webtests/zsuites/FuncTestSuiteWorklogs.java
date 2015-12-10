package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestCreateWorklog;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestDeleteWorklog;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestUpdateWorklog;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestWorkLogOperationVisibility;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestWorkLogTabPanel;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestWorkLogTabPanelVisibility;
import com.atlassian.jira.webtests.ztests.timetracking.modern.TestCreateWorklogOnCloseTransition;
import com.atlassian.jira.webtests.ztests.timetracking.modern.TestCreateWorklogOnCreateIssue;
import com.atlassian.jira.webtests.ztests.timetracking.modern.TestCreateWorklogOnEditIssue;
import com.atlassian.jira.webtests.ztests.timetracking.modern.TestCreateWorklogOnResolveTransition;
import com.atlassian.jira.webtests.ztests.screens.tabs.TestFieldScreenTabsOnCreateIssue;
import com.atlassian.jira.webtests.ztests.screens.tabs.TestFieldScreenTabsOnEditIssue;
import com.atlassian.jira.webtests.ztests.screens.tabs.TestFieldScreenTabsOnResolveIssue;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveTimeTracking;
import junit.framework.Test;

/**
 * A suite of tests around Worklogs
 *
 * @since v4.0
 */
public class FuncTestSuiteWorklogs extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteWorklogs();

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

    public FuncTestSuiteWorklogs()
    {
        addTest(TestWorkLogOperationVisibility.class);
        addTest(TestWorkLogTabPanel.class);
        addTest(TestWorkLogTabPanelVisibility.class);
        addTest(TestCreateWorklog.class);
        addTest(TestUpdateWorklog.class);
        addTest(TestDeleteWorklog.class);
        addTest(TestCreateWorklogOnCloseTransition.class);
        addTest(TestCreateWorklogOnResolveTransition.class);
        addTest(TestCreateWorklogOnCreateIssue.class);
        addTest(TestCreateWorklogOnEditIssue.class);
        addTest(TestFieldScreenTabsOnCreateIssue.class);
        addTest(TestFieldScreenTabsOnEditIssue.class);
        addTest(TestFieldScreenTabsOnResolveIssue.class);
        addTest(TestBulkMoveTimeTracking.class);
    }
}