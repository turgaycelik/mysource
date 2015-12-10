package com.atlassian.jira.functest.framework.admin.user.shared;

import com.atlassian.jira.functest.framework.parser.filter.FilterItem;

import java.util.List;

/**
 * Represents to the Shared Filters Administration Page
 *
 * @since v4.4
 */
public interface SharedFiltersAdministration
{
    /**
     * Navigates to the Shared Filters Administration Page.
     * @return This instance of SharedFiltersAdministration.
     */
    SharedFiltersAdministration goTo();

    /**
     * Searches for shared filters from the Shared Filters Administration Page according to the specified criteria.
     *
     * @param searchText The text to search for in the name/description of the filter.
     * @param owner The name of the user that owns the filters to search for.
     * @return This instance of SharedFiltersAdministration.
     */
    SharedFiltersAdministration search(String searchText, String owner);

    /**
     * Searches for all the shared filters in this instance of JIRA from the Shared Filters Administration Page.
     * @return This instance of SharedFiltersAdministration.
     */
    SharedFiltersAdministration searchAll();

    /**
     * Retrieves an instance of {@link Filters} that represents the list of filters currently displayed on the Shared
     * Filters Administration Page.
     * @return an instance of {@link Filters}.
     */
    Filters filters();

    /**
     *  Deletes the filter represented by the filter id
     * @param filterId   Id of the filter to be deleted
     */
    SharedFiltersAdministration deleteFilter(long filterId);

    /**
     * Change the owner of the filter
     * @param filterId  id of the Filter
     * @param newOwner  the new owner username
     * @return
     */
    SharedFiltersAdministration changeFilterOwner(long filterId, String newOwner);

    /**
     * Represents the list of filters available in the page
     */
    interface Filters
    {
        /**
         * Gets the list of filters shown in the {@link SharedFiltersAdministration} page.
         * @return A list of {@link FilterItem}.
         */
        List<FilterItem> list();
    }
}
