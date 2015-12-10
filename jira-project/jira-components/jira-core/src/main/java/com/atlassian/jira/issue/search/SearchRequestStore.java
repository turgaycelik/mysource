package com.atlassian.jira.issue.search;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor.RetrievalDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Visitor;
import com.atlassian.jira.util.collect.EnclosedIterable;

/**
 * Store used for CRUD of SearchRequests
 *
 * @since v3.13
 */
public interface SearchRequestStore
{

    /**
     * Get a {@link EnclosedIterable} of SearchRequests for the specified List of ids.
     *
     * @param descriptor retrieval descriptor
     * @return CloseableIterable that contains reference to SearchRequests with the specified ids.
     */
    EnclosedIterable<SearchRequest> get(RetrievalDescriptor descriptor);

    /**
     * Get a {@link EnclosedIterable} of all SearchRequests in the database.
     *
     * @return CloseableIterable that contains reference to all SearchRequests.
     */
    EnclosedIterable<SearchRequest> getAll();

    /**
     * Get a {@link EnclosedIterable} of all IndexableSharedEntities representing SearchRequests in the database.
     *
     * Note: this is used so that we can retrieve all the meta data about a SearchRequest without having to deal with
     * the {@link com.atlassian.query.Query}.
     *
     * @return CloseableIterable that contains reference to all IndexableSharedEntities representing SearchRequests.
     */
    EnclosedIterable<IndexableSharedEntity<SearchRequest>> getAllIndexableSharedEntities();

    /**
     * Retrieves a collection of SearchRequest objects that a user created.
     *
     * @param user The user who created the SearchRequests
     * @return Collection of all {@link SearchRequest} that user created.
     */
    Collection<SearchRequest> getAllOwnedSearchRequests(ApplicationUser user);

    /**
     * Retrieves a collection of SearchRequest objects that a user created.
     *
     * @param userKey The key of the user who created the SearchRequests
     * @return Collection of all {@link SearchRequest} that user created.
     */
    Collection<SearchRequest> getAllOwnedSearchRequests(String userKey);

    /**
     * Find a search request given the author and the request name.
     *
     * @param author Author of the SearchRequest
     * @param name   Name of the SearchRequest
     * @return The SearchRequest, or null if there is no matching request
     */
    SearchRequest getRequestByAuthorAndName(ApplicationUser author, String name);

    /**
     * Return the search request as stored in the database
     *
     * @param id Id of the SearchRequest
     * @return The SearchRequest, or null if the request id does not exist
     */
    SearchRequest getSearchRequest(@Nonnull Long id);

    /**
     * Takes a {@link SearchRequest}, user, name of search request and description and persists the XML representation
     * of the SearchRequest object to the database along with the rest of the details
     *
     * @param request SearchResult that should be persisted
     * @return SearchRequest object that was persisted to the database
     */
    SearchRequest create(@Nonnull SearchRequest request);

    /**
     * Updates an existing search request in the database.
     *
     * @param request the request to persist.
     * @return A {@link SearchRequest} that was persisted to the database
     */
    SearchRequest update(@Nonnull SearchRequest request);

    /**
     * Updates the favourite count of the SearchRequest in the database.
     *
     * @param searchRequestId the identifier of the search request to decrease.
     * @param incrementValue  the value to increase the favourite count by. Can be a number < 0 to decrease the favourite count.
     * @return the updated {@link SearchRequest}.
     */
    SearchRequest adjustFavouriteCount(@Nonnull Long searchRequestId, int incrementValue);

    /**
     * Removes the SearchRequest GenericValue from the database based on its id
     *
     * @param id of the search request to be removed from storage
     */
    void delete(@Nonnull Long id);

    /**
     * Get all {@link SearchRequest search requests} associate with a given {@link Project}.
     *
     * @param project Project that is associated with the filters
     * @return Collection of {@link SearchRequest} that have their project set to the given project
     */
    EnclosedIterable<SearchRequest> getSearchRequests(final Project project);

    /**
     * Find search requests matching the given name.
     *
     * @param name   Name of the SearchRequest
     * @return List of all search requests matching the given name. Never null.
     */
    @Nonnull
    List<SearchRequest> findByNameIgnoreCase(String name);

    /**
     * Get all {@link SearchRequest search requests} associated with a given {@link Group}.
     *
     * @param group the group that is associated with the filters
     * @return Collection of {@link SearchRequest} that have their project set to the given project
     */
    EnclosedIterable<SearchRequest> getSearchRequests(final Group group);

    void visitAll(Visitor<SearchRequestEntity> visitor);
}
