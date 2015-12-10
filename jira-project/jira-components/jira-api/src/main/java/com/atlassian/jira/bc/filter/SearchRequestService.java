package com.atlassian.jira.bc.filter;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Collection;

/**
 * Service exposing Search Request Management and retrieval.
 *
 * @since v3.13
 */
@PublicApi
public interface SearchRequestService
{
    /**
     * Retrieve all filters a user has favourited.  Permission checks are done to ensure the user can see the filter, as
     * visibility may have been removed from underneath them.
     *
     * @param user The user who has favourite filters. Also to test visibility and with
     * @return a Collection of {@link SearchRequest} objects that represent filters the user has favourited.
     */
    Collection<SearchRequest> getFavouriteFilters(ApplicationUser user);

    /**
     * @deprecated Use {@link #getFavouriteFilters(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     * Retrieve all filters a user has favourited.  Permission checks are done to ensure the user can see the filter, as
     * visibility may have been removed from underneath them.
     *
     * @param user The user who has favourite filters. Also to test visibility and with
     * @return a Collection of {@link SearchRequest} objects that represent filters the user has favourited.
     */
    Collection<SearchRequest> getFavouriteFilters(User user);

    /**
     * Retrieve all filters a user owns/has created.
     *
     * @param user The user who created the filters.
     * @return a Collection of {@link SearchRequest} objects that represent filters the user has created.
     */
    Collection<SearchRequest> getOwnedFilters(ApplicationUser user);

    /**
     * @deprecated Use {@link #getOwnedFilters(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     *
     * Retrieve all filters a user owns/has created.
     *
     * @param user The user who created the filters.
     * @return a Collection of {@link SearchRequest} objects that represent filters the user has created.
     */
    Collection<SearchRequest> getOwnedFilters(User user);

    /**
     * Get a user's non private filters.  I.e. filters that other users can possibly see.
     *
     * @param user The author of the filters
     * @return Collection of SearchRequest objects that do not have private scope.
     */
    Collection<SearchRequest> getNonPrivateFilters(ApplicationUser user);

    /**
     * @deprecated Use {@link #getNonPrivateFilters(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     *
     * Get a user's non private filters.  I.e. filters that other users can possibly see.
     *
     * @param user The author of the filters
     * @return Collection of SearchRequest objects that do not have private scope.
     */
    Collection<SearchRequest> getNonPrivateFilters(User user);

    /**
     * Get filters owned by a given user that have been favourited by at least one other user
     *
     * @param user The author of the filters
     * @return Collection of SearchRequest objects owned by the given user and favourited by at least one other user
     */
    Collection<SearchRequest> getFiltersFavouritedByOthers(ApplicationUser user);

    /**
     * @deprecated Use {@link #getFiltersFavouritedByOthers(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     *
     * Get filters owned by a given user that have been favourited by at least one other user
     *
     * @param user The author of the filters
     * @return Collection of SearchRequest objects owned by the given user and favourited by at least one other user
     */
    Collection<SearchRequest> getFiltersFavouritedByOthers(User user);

    /**
     * Delete a given filter.
     *
     * @param serviceCtx JIRA Service context containing an error collection and user performing action.  User must be
     *                  owner of filter, else error is passed back through the error collection.
     * @param filterId  The id of the filter to delete.  Id must not be null, else error is passed back through the error
     *                  collection.
     */
    void deleteFilter(JiraServiceContext serviceCtx, Long filterId);

    /**
     * Delete all filters for a given user
     *
     * @param serviceCtx JIRA Service context containing an error collection and user performing action
     * @param user       The user to remove all filters for
     */
    void deleteAllFiltersForUser(JiraServiceContext serviceCtx, ApplicationUser user);

    /**
     * @deprecated Use {@link #deleteAllFiltersForUser(com.atlassian.jira.bc.JiraServiceContext, com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     * Delete all filters for a given user
     *
     * @param serviceCtx JIRA Service context containing an error collection and user performing action
     * @param user       The user to remove all filters for
     */
    void deleteAllFiltersForUser(JiraServiceContext serviceCtx, User user);

    /**
     * Retrieve a given filter by id.
     *
     * @param serviceCtx JIRA Service context containing an error collection and user requesting (to run) the filter.
     *                  The filter must exist and the user must be able to see filter else an error will result.
     * @param filterId  The id of the filter to retrieve.  Id must not be null.
     * @return The Filter as specified by the id, or null if none exists for the user.
     */
    SearchRequest getFilter(JiraServiceContext serviceCtx, Long filterId);

    /**
     * Validates that a filter is in a correct state to be updated.
     *
     * @param serviceCtx Context containing user, error collection and i18n bean
     * @param request   the SearchRequest to validate
     */
    void validateFilterForUpdate(JiraServiceContext serviceCtx, SearchRequest request);

    /**
      * Validate that the passed {@link SearchRequest}'s search parameters and search sorts can be persisted. This method
      * does *not* check the validity of the name, description, share permissions or any other fields in the
      * SearchRequest. Any errors will be reported in the passed context.
      *
      * @param serviceContext Context containing user, error collection and i18n bean
      * @param request the request to validate.
      * @return <code>true</code> iff the passed request's parameters can be saved.
      */
    boolean validateUpdateSearchParameters(JiraServiceContext serviceContext, SearchRequest request);

    /**
     * Validates that a filter is in a correct state to be created.
     *
     * @param serviceCtx Context containing user, error collection and i18n bean
     * @param request   the SearchRequest to validate
     */
    void validateFilterForCreate(JiraServiceContext serviceCtx, SearchRequest request);

    /**
     * Validates that the filter can be deleted successfully.
     *
     * @param serviceCtx      context of the calling user.
     * @param filterId the filter to delete.
     */
    void validateForDelete(JiraServiceContext serviceCtx, Long filterId);

     /**
     * Validates that the proposed owner can take over the ownership of the filter
      * @param serviceCtx  containing proposed owner
     *  @param request   the SearchRequest that you want to change ownership of
     */
    void validateFilterForChangeOwner(final JiraServiceContext serviceCtx, final SearchRequest request);

    /**
     * Persists a {@link SearchRequest} to the database.
     *
     * @param serviceCtx Context containing user, error collection and i18n bean
     * @param request   the request to save
     * @return returns the SearchRequest that was persisted to the database.
     */
    SearchRequest createFilter(JiraServiceContext serviceCtx, SearchRequest request);

    /**
     * Persists a {@link SearchRequest} to the database.
     *
     * @param serviceCtx   Context containing user, error collection and i18n bean
     * @param request     the request to save
     * @param isFavourite saves the SearchRequest as a favourite (or not)
     * @return returns the SearchRequest that was persisted to the database.
     */
    SearchRequest createFilter(JiraServiceContext serviceCtx, SearchRequest request, boolean isFavourite);

    /**
     * Persists a {@link SearchRequest} to the database.
     *
     * @param serviceCtx Context containing user, error collection and i18n bean
     * @param request   the request to update
     * @return returns the SearchRequest that was persisted to the database.
     */
    SearchRequest updateFilter(JiraServiceContext serviceCtx, SearchRequest request);

    /**
     * Persists a {@link SearchRequest} to the database.
     *
     * @param serviceCtx   Context containing user, error collection and i18n bean
     * @param request     the request to update
     * @param isFavourite saves the SearchRequest as a favourite (or not)
     * @return returns the SearchRequest that was persisted to the database.
     */
    SearchRequest updateFilter(JiraServiceContext serviceCtx, SearchRequest request, boolean isFavourite);

     /**
     * Persists a {@link SearchRequest} to the database - only available to administrators.
     *
     * @param serviceCtx Context containing the new owner, error collection and i18n bean
     * @param user the user requesting the update, must have Permissions.ADMINISTER permissions
     * @param request   the request to update
     * @return returns the SearchRequest that was persisted to the database .
     */
    SearchRequest updateFilterOwner(JiraServiceContext serviceCtx, ApplicationUser user, SearchRequest request);

     /**
     * @deprecated Use {@link #updateFilterOwner(com.atlassian.jira.bc.JiraServiceContext, com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.search.SearchRequest)} instead. Since v6.0.
     *
     * Persists a {@link SearchRequest} to the database - only available to administrators.
     *
     * @param serviceCtx Context containing the new owner, error collection and i18n bean
     * @param user the user requesting the update, must have Permissions.ADMINISTER permissions
     * @param request   the request to update
     * @return returns the SearchRequest that was persisted to the database .
     */
    SearchRequest updateFilterOwner(JiraServiceContext serviceCtx, User user, SearchRequest request);

    /**
     * Persists changes to passed {@link SearchRequest}'s search parameters and search sorts. Changes to the
     * SearchRequest's other fields (name, description, share permissions, ...) are not saved. Any errors will be
     * reported in the passed context.
     *
     * This method does not check the validity of the SearchRequest's share permissions when saving. This is to
     * allow JIRA to save search parameter changes even when the SearchRequest's permissions are invalid.
     *
     * @param serviceCtx Context containing user, error collection and i18n bean
     * @param request the request to update.
     * @return returns the SearchRequest that was persisted to the database. May return <code>null</code> if an error
     * occurs. The passed service context will have details of any errors.
     */
    SearchRequest updateSearchParameters(JiraServiceContext serviceCtx, SearchRequest request);

    /**
     * This will validate that the input parameters are valid for a search that encompasses ANY share entity type.
     *
     * @param serviceCtx   Context containing user, error collection and i18n bean
     * @param searchParameters the SharedEntitySearchParameters to validate
     *
     */
    void validateForSearch(JiraServiceContext serviceCtx, SharedEntitySearchParameters searchParameters);

    /**
     * Search for the SearchRequests that match the passed searchParameters. The result can be paged so that a subset of the
     * results can be returned.
     *
     * @param serviceCtx   Context containing user, error collection and i18n bean
     * @param searchParameters the searchParameters to query.
     * @param pagePosition     the page to return.
     * @param pageWidth        the number of results per page.
     * @return the result of the search.
     */
    SharedEntitySearchResult<SearchRequest> search(JiraServiceContext serviceCtx, SharedEntitySearchParameters searchParameters, int pagePosition, int pageWidth);
}
