package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.TestPermissionSchemes;
import com.atlassian.jira.webtests.ztests.admin.TestSystemAdminAndAdminPermissions;
import com.atlassian.jira.webtests.ztests.customfield.TestCustomFieldsNoSearcherPermissions;
import com.atlassian.jira.webtests.ztests.dashboard.TestManageDashboardPagePermissions;
import com.atlassian.jira.webtests.ztests.dashboard.reports.TestDeveloperWorkloadReportPermissions;
import com.atlassian.jira.webtests.ztests.issue.TestIssueOperationsWithLimitedPermissions;
import com.atlassian.jira.webtests.ztests.issue.comments.TestCommentPermissions;
import com.atlassian.jira.webtests.ztests.misc.TestAddPermission;
import com.atlassian.jira.webtests.ztests.subtask.TestCreateSubTasksContextPermission;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestTimeTrackingReportPermissions;
import com.atlassian.jira.webtests.ztests.user.TestDeleteUserAndPermissions;
import com.atlassian.jira.webtests.ztests.user.TestGroupSelectorPermissions;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowBasedPermissions;
import junit.framework.Test;

/**
 * A suite of tests related to Permissions
 *
 * @since v4.0
 */
public class FuncTestSuitePermissions extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuitePermissions();

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

    public FuncTestSuitePermissions()
    {
        addTest(TestDeveloperWorkloadReportPermissions.class);
        addTest(TestTimeTrackingReportPermissions.class);
        addTest(TestIssueOperationsWithLimitedPermissions.class);
        addTest(TestPermissionSchemes.class);
        addTest(TestCreateSubTasksContextPermission.class);
        addTest(TestManageDashboardPagePermissions.class);
        addTest(TestDeleteUserAndPermissions.class);
        addTest(TestWorkflowBasedPermissions.class);
        addTest(TestGroupSelectorPermissions.class);
        addTest(TestCustomFieldsNoSearcherPermissions.class);
        addTest(TestSystemAdminAndAdminPermissions.class);
        addTest(TestAddPermission.class);
        addTest(TestCommentPermissions.class);
    }
}