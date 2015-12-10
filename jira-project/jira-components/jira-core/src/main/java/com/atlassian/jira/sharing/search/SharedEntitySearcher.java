package com.atlassian.jira.sharing.search;

import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;

/**
 * Perform searches for {@link SharedEntity} instances.
 *
 * @since v3.13
 */
public interface SharedEntitySearcher<S extends SharedEntity>
{
    /**
     * Search for {@link SharedEntity} instances that match the passed in searchParameters.
     *
     * @param searchParameters the search searchParameters for the search.
     * @param user the user to perform the search as.
     * @param pageOffset the page that should be returned by the search. Must be >=0.
     * @param pageWidth the width of the page that should be returned by the search. Can be set to Integer.MAX_VALUE if you want to return all results
     *        at one. In this case it is compulsory to set pageOffest to 0.
     * @return the search results.
     */
    SharedEntitySearchResult<S> search(SharedEntitySearchParameters searchParameters, ApplicationUser user, int pageOffset, int pageWidth);

    /**
     * Search for {@link SharedEntity} instances that match the passed in searchParameters. Used for system searches. Unpaged and no permissions query
     * performed.
     *
     * @param searchParameters the search searchParameters for the search.
     * @return the search results.
     */
    SharedEntitySearchResult<S> search(SharedEntitySearchParameters searchParameters);
}
