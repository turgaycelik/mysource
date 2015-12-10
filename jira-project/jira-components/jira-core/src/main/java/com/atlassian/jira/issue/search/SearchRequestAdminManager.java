package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.collect.EnclosedIterable;

/**
 * SearchRequestAdminManager is for the admin section only.
 */
public interface SearchRequestAdminManager
{
    /**
     * Return the search request as stored in the database.
     * NOTE: This method does not perform permission checks to see if the current user has permission to
     * see the requested search request. This method should not be used unless you know you have to.
     *
     * @param id The id of the filter
     * @return The SearchRequest, or null if the request id does not exist
     */
    SearchRequest getSearchRequestById(Long id);

    /**
     * Get all SearchRequests associate with a given project.
     * <p>
     * Note: Permissions are NOT set for these objects.
     *
     * @param project Project that is associate with the filters
     * @return Collection of {@link SearchRequest} that have their project set to the given project
     */
    EnclosedIterable<SearchRequest> getSearchRequests(final Project project);

    /**
     * Gets all {@link SearchRequest}s that are explicitly shared with a group.
     * <p>
     * Note: Permissions are NOT set for these objects.
     *
     * @param group The group associated with the SearchRequests
     * @return The {@link SearchRequest} objects that are shared with the given group
     */
    EnclosedIterable<SearchRequest> getSearchRequests(final Group group);

    /**
     * Update a SearchRequest. Useful for upgrading.
     */
    SearchRequest update(final SearchRequest request);

    /**
     * Delete a SearchRequest.
     */
    void delete(final Long searchRequestId);
}
