package com.atlassian.jira.issue.search.managers;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroup;
import com.atlassian.jira.jql.ClauseHandler;
import javax.annotation.Nonnull;

import java.util.Collection;

/**
 * Manager that holds all references to search related information in JIRA.
 *
 * @since v4.0
 */
@PublicApi
public interface SearchHandlerManager
{
    /**
     * Get searchers that are applicable for a given context. This is found through the {@link
     * com.atlassian.jira.issue.search.searchers.IssueSearcher#getSearchRenderer()#isShown(com.atlassian.jira.issue.search.SearchContext)}
     * method.
     *
     * @param searcher  performing this action.
     * @param context for the list of searchers. Must not be null
     * @return Collection of {@link com.atlassian.jira.issue.search.searchers.IssueSearcher}
     */
    Collection<IssueSearcher<?>> getSearchers(User searcher, SearchContext context);

    /**
     * Return all the active searchers in JIRA. It will not return the searchers unless they are associated with a
     * field.
     *
     * @return all the searchers in JIRA.
     */
    Collection<IssueSearcher<?>> getAllSearchers();

    /**
     * Get all searcher groups with the {@link IssueSearcher} that are applicable for the context. {@link
     * com.atlassian.jira.issue.search.searchers.IssueSearcher#getSearchRenderer()#isShown(SearchContext)} method. Note
     * that the {@link com.atlassian.jira.issue.search.searchers.SearcherGroup} will still appear even if no {@link
     * IssueSearcher} are shown for the group.
     *
     * @param searchContext for the searcher groups.
     * @return Collection of {@link com.atlassian.jira.issue.search.searchers.SearcherGroup}
     * @deprecated Since 6.3.4. The {@link com.atlassian.jira.issue.search.SearchContext} parameter is no longer needed. Use {@link #getSearcherGroups()}
     */
    @Deprecated
    Collection<SearcherGroup> getSearcherGroups(SearchContext searchContext);

    /**
     * Get all searcher groups. Note that the {@link com.atlassian.jira.issue.search.searchers.SearcherGroup} will
     * still appear even if no {@link IssueSearcher} are shown for the group.
     *
     * @return Collection of {@link com.atlassian.jira.issue.search.searchers.SearcherGroup}
     */
    Collection<SearcherGroup> getSearcherGroups();

    /**
     * Get a searcher by the searchers name.
     *
     * @param id the string identifier returned by {@link com.atlassian.jira.issue.search.searchers.IssueSearcher#getSearchInformation()#getId()}
     * @return the searcher matching the id, null if none is found.
     */
    IssueSearcher<?> getSearcher(String id);

    /**
     * Refreshes the {@link com.atlassian.jira.issue.search.managers.SearchHandlerManager}.
     */
    void refresh();

    /**
     * Return a collection of {@link com.atlassian.jira.jql.ClauseHandler}s registered against the passed JQL clause
     * name. This will only return the handlers that the user has permission to see as specified by the {@link
     * com.atlassian.jira.jql.permission.ClausePermissionHandler#hasPermissionToUseClause(User)}
     * method. The reason this is returning a collection is that custom fields can have the same JQL clause name and
     * therefore resolve to multiple clause handlers, this will never be the case for System fields, we don't allow it!
     *
     * @param user that will be used to perform a permission check.
     * @param jqlClauseName the clause name to search for.
     * @return A collection of ClauseHandler that are associated with the passed JQL clause name. An empty collection
     *         will be returned to indicate failure.
     */
    @Nonnull
    Collection<ClauseHandler> getClauseHandler(final User user, final String jqlClauseName);

    /**
     * Return a collection of {@link com.atlassian.jira.jql.ClauseHandler}s registered against the passed JQL clause
     * name. This will return all available handlers, regardless of permissions. The reason this is returning a collection
     * is that custom fields can have the same JQL clause name and therefore resolve to multiple clause handlers, this
     * will never be the case for System fields, we don't allow it!
     *
     * @param jqlClauseName the clause name to search for.
     * @return A collection of ClauseHandler that are associated with the passed JQL clause name. An empty collection
     *         will be returned to indicate failure.
     */
    @Nonnull
    Collection<ClauseHandler> getClauseHandler(final String jqlClauseName);

    /**
     * Get the {@link com.atlassian.jira.issue.search.ClauseNames} associated with the provided field name.
     *
     * A collection can be returned because it is possible for multiple clause handlers to register against the same
     * field.
     *
     * @param fieldId the {@link com.atlassian.jira.issue.fields.Field#getId()}.
     * @return the {@link com.atlassian.jira.issue.search.ClauseNames} associated with the provided field name. Empty collection
     * is returned when the field has no JQL names (i.e. no clause handlers) associated with it.
     */
    @Nonnull
    Collection<ClauseNames> getJqlClauseNames(String fieldId);

    /**
     * Gets the field ids that are associated with the provided jqlClauseName. The reason this returns a collection is
     * that custom fields can have the same JQL clause name and therefore resolve to multiple field ids. This will only
     * return the fields associated with clause handlers that the user has permission to see as specified by the {@link
     * com.atlassian.jira.jql.permission.ClausePermissionHandler#hasPermissionToUseClause(com.atlassian.crowd.embedded.api.User)}
     * method.
     *
     * @param searcher that will be used to perform a permission check.
     * @param jqlClauseName the clause name to find the field id for.
     *
     * @return the field ids that are associated with the provided jqlClauseName, empty collection if not found
     */
    @Nonnull
    Collection<String> getFieldIds(final User searcher, String jqlClauseName);

    /**
     * Gets the field ids that are associated with the provided jqlClauseName. The reason this returns a collection is
     * that custom fields can have the same JQL clause name and therefore resolve to multiple field ids.
     *
     * @param jqlClauseName the clause name to find the field id for.
     * @return the field ids that are associated with the provided jqlClauseName, empty collection if not found
     */
    @Nonnull
    Collection<String> getFieldIds(String jqlClauseName);

    /**
     * Get all the available clause names that the searcher can see.
     *
     * @param searcher that will be used to perform a permission check.
     * @return the {@link com.atlassian.jira.issue.search.ClauseNames} visible to the user. Empty collection
     * is returned when the can see no clauses.
     */
    @Nonnull
    Collection<ClauseNames> getVisibleJqlClauseNames(User searcher);

    /**
     * Get all the available clause handlers that the searcher can see.
     *
     * @param searcher that will be used to perform a permission check.
     * @return the {@link com.atlassian.jira.jql.ClauseHandler} visible to the user. Empty collection
     * is returned when the can see no clauses.
     */
    @Nonnull
    Collection<ClauseHandler> getVisibleClauseHandlers(User searcher);

    /**
     * Return a collection of {@link com.atlassian.jira.issue.search.searchers.IssueSearcher}s registered against the
     * passed JQL clause name. This will only return the IssueSearchers that the user has permission to see as specified
     * by the {@link com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer#isShown(User, com.atlassian.jira.issue.search.SearchContext)}
     * method.
     *
     * @param user that will be used to perform a permission check.
     * @param jqlClauseName the clause name to search for.
     * @param searchContext the search context under which the searchers must be shown
     * @return A collection of IssueSearchers that are associetd with the passed JQL clause name. An empty collection
     *         will be returned to indicate failure.
     * @deprecated Since 6.3.3. The {@link com.atlassian.jira.issue.search.SearchContext} parameter is no longer needed. Use {@link #getSearchersByClauseName(com.atlassian.crowd.embedded.api.User, String)}.
     */
    @Deprecated
    @Nonnull
    Collection<IssueSearcher<?>> getSearchersByClauseName(final User user, String jqlClauseName, SearchContext searchContext);

    /**
     * Return a collection of {@link com.atlassian.jira.issue.search.searchers.IssueSearcher}s registered against the
     * passed JQL clause name. This will only return the IssueSearchers that the user has permission to see as specified
     * by the {@link com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer#isShown(User, com.atlassian.jira.issue.search.SearchContext)}
     * method.
     *
     * @param user that will be used to perform a permission check.
     * @param jqlClauseName the clause name to search for.
     * @return A collection of IssueSearchers that are associetd with the passed JQL clause name. An empty collection
     *         will be returned to indicate failure.
     */
    @Nonnull
    Collection<IssueSearcher<?>> getSearchersByClauseName(final User user, String jqlClauseName);
}
