package com.atlassian.jira.functest.framework.navigation;

import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermission;

import java.util.List;
import java.util.Set;

/**
 * Navigate filter functionality.
 *
 * @since v3.13
 */
public interface FilterNavigation
{
    /**
     * Add filter as favourite to the current logged in user.
     *
     * @param id The id of the filter
     */
    void addFavourite(int id);

    /**
     * Remove filter as favourite from the current logged in user.
     *
     * @param id The id of the filter
     */
    void removeFavourite(int id);

    /**
     * Navigate to the default filters page for this FilterNavigation.
     */
    void goToDefault();

    /**
     * Navigate to manage subscriptions for a given filter
     *
     * @param filterId id of the filter
     */
    void manageSubscriptions(int filterId);

    public void addSubscription(final int filterId);

    /**
     * Navigate to favourite filters.
     */
    void favouriteFilters();

    /**
     * Navigate to manage my filters
     */
    void myFilters();

    /**
     * Navigate to manage all filters
     */
    void allFilters();

    /**
     * Navigate to manage popular filters
     */
    void popularFilters();

    /**
     * Navigate to search filters
     */
    void searchFilters();

    /**
     * Creates a filter with the specified name and description
     *
     * @param filterName the name of the filter.
     * @param filterDesc the description of the filter.
     * @return the id of the newly created filter.
     */
    long createFilter(String filterName, String filterDesc);

    /**
     * Creates a filter with the specified name and description and set of sharing permissions.
     *
     * @param filterName the name of the filter.
     * @param filterDesc the description of the filter.
     * @param sharingPermissions the sharing permissions of the filter.
     *
     * @return the id of the newly created filter.
     */
    long createFilter(String filterName, String filterDesc, Set<TestSharingPermission> sharingPermissions);

    /**
     * Delete the specified filter.
     * @param id the id of the filter to delete.
     */
    void deleteFilter(int id);

    /**
     * Does a filter search from a filter search form page with the given search criteria.
     * Client is left on filter search results screen.
     *
     * @param filterName     the name of the filter, possibly "" but not null.
     *
     */
    void findFilters(String filterName);


    /**
     * Return a new list containing new expected items based on the given ones which are stripped
     * of any field expectations that are not designed to be met by this FilterNavigation
     * implementation's search results view (including popular filters).
     *
     * @param expectedItems the template items to expect.
     * @return equivalent expected items for this search results view.
     */
    List <FilterItem> sanitiseSearchFilterItems(final List <FilterItem> expectedItems);

    /**
     * Return a new list containing new expected items based on the given ones which are stripped
     * of any field expectations that are not designed to be met by this FilterNavigation
     * implementation's favourite view.
     *
     * @param expectedItems the template items to expect.
     * @return equivalent expected items for this favourite filters view.
     */
    List <FilterItem> sanitiseFavouriteFilterItems(final List <FilterItem> expectedItems);

    /**
     * Return the basic URL of the action.
     * @return this.
     */
    String getActionBaseUrl();

    /**
     * Goes to the project tab of the filterpicker popup (only visible with showProjects param).
     *
     * @return this.
     */
    FilterNavigation projects();
}
