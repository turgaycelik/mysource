package com.atlassian.jira.functest.framework.admin.user.shared;

import com.atlassian.jira.functest.framework.parser.dashboard.DashboardItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;

import java.util.List;

/**
 * Represents to the Shared Dashboards Administration Page
 *
 * @since v4.4
 */
public interface SharedDashboardsAdministration
{
    /**
     * Navigates to the Shared Dashboards Administration Page.
     * @return This instance of SharedDashboardsAdministration.
     */
    SharedDashboardsAdministration goTo();

    /**
     * Searches for shared dashboards from the Shared Dashboards Administration Page according to the specified criteria.
     *
     * @param searchText The text to search for in the name/description of the dashboard.
     * @param owner The name of the user that owns the dashboards to search for.
     * @return This instance of SharedDashboardsAdministration.
     */
    SharedDashboardsAdministration search(String searchText, String owner);

    /**
     * Searches for all the shared dashboards in this instance of JIRA from the Shared dashboards Administration Page.
     * @return This instance of ShareddashboardsAdministration.
     */
    SharedDashboardsAdministration searchAll();

    /**
     * Retrieves an instance of {@link Dashboards} that represents the list of dashboards currently displayed on the Shared
     * Dashboards Administration Page.
     * @return an instance of {@link Dashboards}.
     */
    Dashboards dashboards();

    /**
     *  Deletes the dashboard represented by the dashboard id
     * @param dashboardId   Id of the dashboard to be deleted
     */
    SharedDashboardsAdministration deleteDashboard(long dashboardId);

    /**
     * Change the owner of the dashboard
     * @param dashboardId  id of the dashboard
     * @param newOwner  the new owner username
     * @return
     */
    SharedDashboardsAdministration changeDashboardOwner(long dashboardId, String newOwner);

    /**
     * Represents the list of dashboards available in the page
     */
    interface Dashboards
    {
        /**
         * Gets the list of dashboards shown in the {@link com.atlassian.jira.functest.framework.admin.user.shared.SharedDashboardsAdministration} page.
         * @return A list of {@link com.atlassian.jira.functest.framework.parser.filter.FilterItem}.
         */
        List<DashboardItem> list();
    }
}
