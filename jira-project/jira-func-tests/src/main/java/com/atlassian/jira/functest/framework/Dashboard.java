package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;

/**
 * An abstraction for logically grouping JIRA dashboard functionality for functional tests. When used as a return
 * value it also implies that the current location is the dashboard.
 *
 * @since v3.13
 */
public interface Dashboard
{
    /**
     * Turns on configuration mode for the dashboard. If it is already turned on, has no effect. If the current
     * user has the default dashboard, subsequently they will have a custom dashboard, even though it will be
     * configured the same as the default dashboard. Needs to be on the dashboard to work.
     *
     * @return this in a FluentInterface style.
     */
    public Dashboard enableConfigureMode();

    /**
     * Turns off configuration mode for the dashboard. If it is already off, it has no effect. Expects to be on
     * the Dashboard to work.
     *
     * @return itself.
     */
    public Dashboard disableConfigureMode();

    /**
     * Navigates to the dashboard. Request to return to the location represented by this functional area.
     *
     * @return itself.
     */
    public Dashboard navigateTo();

    /**
     * Navigates to the a specific dashboard page. Request to return to the location represented by this functional area.
     *
     * @param pageId specific page
     * @return itself.
     */
    public Dashboard navigateTo(long pageId);

    /**
     * Add the passed dashboard page to JIRA.
     *
     * @param info the basic information for the dashboard.
     * @param cloneId the id of the dashboard to clone. Can be null to clone the blank dashboard.
     * @return the navigator object.
     */
    Dashboard addPage(SharedEntityInfo info, Long cloneId);

    /**
     * Change the passed dashboard page in JIRA.
     *
     * @param info the basic information for the dashboard.
     * @return the navigator object.
     */
    Dashboard editPage(SharedEntityInfo info);

    /**
     * Navigate to the screen that shows favourite dashboards.
     *
     * @return the navigator object.
     */
    Dashboard navigateToFavourites();

    /**
     * Navigate to the screen that shows my dashboards.
     *
     * @return the navigator object.
     */
    Dashboard navigateToMy();

    /**
     * Navigate to the screen that shows popular dashboards.
     *
     * @return the navigator object.
     */
    Dashboard navigateToPopular();

    /**
     * Navigate to the screen that allows dashboard searching.
     *
     * @return the navigator object.
     */

    Dashboard navigateToSearch();

    /**
     * Find the id of the dashboard page with the given name from a table to dashboard pages.
     *
     * @param dashboardPageName the name of the dashboard to find.
     * @param pagesLocator      the location of the table listing the dashboard.
     * @return the identifier if the dashboard page or null if no such page exists.
     */
    Long getDashboardPageId(String dashboardPageName, Locator pagesLocator);

    /**
     * Open the full configure screen for the passed dashboard.
     *
     * @param dashboardPageId the id of the dashboard to navigate to.
     * @return the navigator object.
     */
    Dashboard navigateToFullConfigure(Long dashboardPageId);

    /**
     * Open the default dashboard configuration.
     *
     * @return the navigation object.
     */
    Dashboard navigateToDefaultFullConfigure();

    /**
     * Make the passed dashboard one of the logged in user's favourites.
     *
     * @param id the id of the dashboard to favourite.
     * @return this object so that the requests can be chained.
     */
    Dashboard favouriteDashboard(long id);

    /**
     * Remove the passed dashboard from the user's favourites.
     *
     * @param id the id of the dashboard to unfavourite.
     * @return this object so that the requests can be chained.
     */
    Dashboard unFavouriteDashboard(long id);

    /**
     * Reset user session state.
     *
     * @return this object so that the requests can be chained.
     */
    Dashboard resetUserSessionState();

    /**
     * Reset the dashboard configuration to its initial state.
     *
     * @return this object so that the requests can be chained.
     */
    Dashboard resetToDefault();

    /**
     * Holds the identifiers of the tables that display Portal Pages.
     */
    class Table
    {
        public static final Table FAVOURITE = new Table("pp_favourite");
        public static final Table OWNED = new Table("pp_owned");
        public static final Table POPULAR = new Table("pp_popular");
        public static final Table SEARCH = new Table("pp_browse");

        private final String tableId;

        public Table(final String tableId)
        {
            this.tableId = tableId;
        }

        public String toXPath()
        {
            return "//table[@id='" + tableId + "']";
        }

        public String getTableId()
        {
            return tableId;
        }
    }
}
