package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.dashboard.TestAddPortalPageSharing;
import com.atlassian.jira.webtests.ztests.dashboard.TestDashboardDeleteConfirm;
import com.atlassian.jira.webtests.ztests.dashboard.TestDashboardRelatedEntitiesDelete;
import com.atlassian.jira.webtests.ztests.dashboard.TestEditPortalPage;
import com.atlassian.jira.webtests.ztests.dashboard.TestEditPortalPageSharing;
import com.atlassian.jira.webtests.ztests.dashboard.TestManageDashboardChooseTab;
import com.atlassian.jira.webtests.ztests.dashboard.TestManageDashboardPagePermissions;
import com.atlassian.jira.webtests.ztests.dashboard.TestManageDashboardPages;
import com.atlassian.jira.webtests.ztests.dashboard.TestReorderDashboardPages;
import com.atlassian.jira.webtests.ztests.dashboard.TestSearchDashboardPages;
import com.atlassian.jira.webtests.ztests.dashboard.management.TestDeleteSharedDashboardsByAdmins;
import com.atlassian.jira.webtests.ztests.dashboard.management.TestSharedDashboardSearchingByAdmins;
import junit.framework.Test;

/**
 * A func test suite for Dashboards
 *
 * @since v4.0
 */
public class FuncTestSuiteDashboards extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteDashboards();

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

    public FuncTestSuiteDashboards()
    {
        addTest(TestManageDashboardPages.class);
        addTest(TestReorderDashboardPages.class);
        addTest(TestDashboardRelatedEntitiesDelete.class);
        addTest(TestManageDashboardPagePermissions.class);

        addTest(TestAddPortalPageSharing.class);
        addTest(TestEditPortalPage.class);
        addTest(TestEditPortalPageSharing.class);
        addTest(TestManageDashboardChooseTab.class);
        addTest(TestSearchDashboardPages.class);
        addTest(TestSharedDashboardSearchingByAdmins.class);

        addTest(TestDashboardDeleteConfirm.class);
        addTest(TestDeleteSharedDashboardsByAdmins.class);
    }
}
