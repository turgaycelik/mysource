package com.atlassian.jira.issue.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Visitor;
import com.atlassian.jira.util.collect.EnclosedIterable;

import java.util.Collection;
import java.util.List;

/**
 * This manager is responsible for {@link SearchRequest}s. SearchRequests encapsulate all information used for searches in the issue navigator.
 */
@PublicApi
public interface SearchRequestManager extends SharedEntityAccessor<SearchRequest>
{
    /**
     * Retrieves a collection of SearchRequest objects that a user created.
     *
     * @param user The user who created the SearchRequests
     * @return Collection of all {@link SearchRequest} that user created.
     */
    Collection<SearchRequest> getAllOwnedSearchRequests(ApplicationUser user);

    /**
     * @deprecated Use {@link #getAllOwnedSearchRequests(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     * Retrieves a collection of SearchRequest objects that a user created.
     *
     * @param user The user who created the SearchRequests
     * @return Collection of all {@link SearchRequest} that user created.
     */
    Collection<SearchRequest> getAllOwnedSearchRequests(User user);

    /**
     * Find a search request given the author and the request name.
     *
     * @param author The author of the SearchRequest
     * @param name   The name of the SearchRequest
     * @return The SearchRequest, or null if there is no matching request
     */
    SearchRequest getOwnedSearchRequestByName(ApplicationUser author, String name);

    /**
     * @deprecated Use {@link #getOwnedSearchRequestByName(com.atlassian.jira.user.ApplicationUser, String)} instead. Since v6.0.
     * Find a search request given the author and the request name.
     *
     * @param author The author of the SearchRequest
     * @param name   The name of the SearchRequest
     * @return The SearchRequest, or null if there is no matching request
     */
    SearchRequest getOwnedSearchRequestByName(User author, String name);

    /**
     * Return the search request as stored in the database if the user has permission to see it.
     *
     * @param user The user to check shares with
     * @param id   The id of the filter
     * @return The SearchRequest, or null if the request id does not exist
     *
     * @see #getSearchRequestById(Long)
     */
    SearchRequest getSearchRequestById(ApplicationUser user, Long id);

    /**
     * @deprecated Use {@link #getSearchRequestById(com.atlassian.jira.user.ApplicationUser, Long)} instead. Since v6.0.
     * Return the search request as stored in the database if the user has permission to see it.
     *
     * @param user The user to check shares with
     * @param id   The id of the filter
     * @return The SearchRequest, or null if the request id does not exist
     *
     * @see #getSearchRequestById(Long)
     */
    SearchRequest getSearchRequestById(User user, Long id);

    /**
     * Return the search request as stored in the database without any permission checks.
     *
     * @param id   The id of the filter
     * @return The SearchRequest, or null if the request id does not exist
     */
    SearchRequest getSearchRequestById(Long id);

    /**
     * Get all SearchRequests.
     *
     * WARNING: This method will run horribly slow on systems with a lot of saved filters.
     *
     * @return an {@link com.atlassian.jira.util.collect.EnclosedIterable} of SearchRequests
     *
     * @deprecated Use {@link #visitAll(Visitor)} instead. Since v5.2.
     */
    EnclosedIterable<SearchRequest> getAll();

    /**
     * Iterates over all SearchRequests using a Visitor pattern callback.
     */
    void visitAll(Visitor<SearchRequestEntity> visitor);

    List<SearchRequest> findByNameIgnoreCase(String name);

    /**
     * @deprecated Use {@link #getSearchRequestOwner(Long id)} instead. Since v6.0.
     *
     * Return the owner of the passed SearchRequest. This is mainly used for permission checks.
     *
     * @param id the identifier of the SearchRequest.
     * @return the username of the owner of the SearchRequest. Null is returned if the SearchRequest does not exist.
     */
    String getSearchRequestOwnerUserName(Long id);

    ApplicationUser getSearchRequestOwner(Long id);

    /**
     * Takes a SearchRequest, user, name of search request and description and persists the XML representation
     * of the SearchRequest object to the database along with the rest of the details
     *
     * @param request SearchResult that should be persisted
     * @return SearchRequest object that was persisted to the database
     */
    SearchRequest create(SearchRequest request);

    /**
     * Updates an existing search request in the database.
     *
     * @param request the request to persist
     * @return A {@link SearchRequest} that was persisted to the database
     */
    SearchRequest update(SearchRequest request);

    /**
     * Removes the SearchRequest GenericValue from the database based on its id
     *
     * @param id of the search request to be removed from storage
     */
    void delete(Long id);

    /**
     * Search for the SearchRequests that match the passed searchParameters. The result can be paged so that a subset
     * of the results can be returned.
     *
     * @param searchParameters the searchParameters to query.
     * @param user             the user performing the search.
     * @param pagePosition     the page to return.
     * @param pageWidth        the number of results per page.
     * @return the result of the search.
     */
    SharedEntitySearchResult<SearchRequest> search(SharedEntitySearchParameters searchParameters, ApplicationUser user, int pagePosition, int pageWidth);

    /**
     * @deprecated Use {@link #search(com.atlassian.jira.sharing.search.SharedEntitySearchParameters, com.atlassian.jira.user.ApplicationUser, int, int)} instead. Since v6.0.
     * Search for the SearchRequests that match the passed searchParameters. The result can be paged so that a subset
     * of the results can be returned.
     *
     * @param searchParameters the searchParameters to query.
     * @param user             the user performing the search.
     * @param pagePosition     the page to return.
     * @param pageWidth        the number of results per page.
     * @return the result of the search.
     */
    SharedEntitySearchResult<SearchRequest> search(SharedEntitySearchParameters searchParameters, User user, int pagePosition, int pageWidth);
}
