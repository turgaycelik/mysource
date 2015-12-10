package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.dashboard.DashboardPagePortletInfo;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;

import java.util.List;

/**
 * This makes assertions about Dashboard pages and their content
 *
 * @since v3.13
 */
public interface DashboardAssertions
{
    /**
     * Make sure that the passed dashboard pages are listed in the passed table.
     *
     * @param pages             the pages to test.
     * @param table the table that contains the portal pages.
     */
    void assertDashboardPages(List<? extends SharedEntityInfo> pages, Dashboard.Table table);

    /**
     * Ensure that the given portlets exist on the passed dashboard page.
     *
     * @param id                   the id of the dashboard to check.
     * @param dashboardPortletInfo the portlets that should exist on the dashboard.
     */
    void assertDashboardPortlets(Long id, DashboardPagePortletInfo dashboardPortletInfo);

    /**
     * Ensure that the given portlets exist on the system default dashboard page.
     *
     * @param dashboardPortletInfo the portlets that should exist on the dashboard.
     */
    void assertDefaultDashboardPortlets(DashboardPagePortletInfo dashboardPortletInfo);

    /**
     * Assert that the columns exist
     *
     * @param colHeaders        The text of the Column Header
     * @param dashboardsLocator Locator of the dashboard table
     */
    void assertColumns(List<String> colHeaders, Locator dashboardsLocator);
}
