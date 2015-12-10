package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.dashboard.reports.TestConfigureReport;
import com.atlassian.jira.webtests.ztests.dashboard.reports.TestDeveloperWorkloadReport;
import com.atlassian.jira.webtests.ztests.dashboard.reports.TestDeveloperWorkloadReportPermissions;
import com.atlassian.jira.webtests.ztests.dashboard.reports.TestPieChartReport;
import com.atlassian.jira.webtests.ztests.dashboard.reports.TestSingleLevelGroupByReport;
import com.atlassian.jira.webtests.ztests.dashboard.reports.TestSingleLevelGroupByReportByLabels;
import com.atlassian.jira.webtests.ztests.dashboard.reports.TestSingleLevelGroupByReportIrrelevantIssues;
import com.atlassian.jira.webtests.ztests.dashboard.reports.security.xss.TestXssInConfigureReport;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestTimeTrackingExcelReport;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestTimeTrackingReport;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestTimeTrackingReportPermissions;
import junit.framework.Test;

/**
 * a suite of tests related to Reports
 *
 * @since v4.0
 */
public class FuncTestSuiteReports extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteReports();

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

    public FuncTestSuiteReports()
    {
        addTest(TestDeveloperWorkloadReport.class);
        addTest(TestDeveloperWorkloadReportPermissions.class);
        addTest(TestTimeTrackingReport.class);
        addTest(TestTimeTrackingReportPermissions.class);
        addTest(TestTimeTrackingExcelReport.class);
        addTest(TestSingleLevelGroupByReport.class);
        addTest(TestSingleLevelGroupByReportIrrelevantIssues.class);
        addTest(TestConfigureReport.class);
        addTest(TestPieChartReport.class);
        addTest(TestSingleLevelGroupByReportByLabels.class);
        addTest(TestXssInConfigureReport.class);
    }
}