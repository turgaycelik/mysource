package com.atlassian.jira.issue.search.managers;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroup;

import java.util.Collection;

/**
 * Manager to obtain a list of {@link IssueSearcher} objects as well as {@link SearcherGroup} collections
 */
@PublicApi
public interface IssueSearcherManager
{
    /**
     * Get searchers that are applicable for a given context. This is found through the
     * {@link com.atlassian.jira.issue.search.searchers.IssueSearcher#getSearchRenderer()#isShown(SearchContext)} method.
     * @param searcher that is performing this action.
     * @param context for the list of searchers. Must not be null
     * @return Collection of {@link IssueSearcher}
     */
    public Collection<IssueSearcher<?>> getSearchers(User searcher, SearchContext context);

    /**
     * Return all the active searchers in JIRA. It will not return the searchers unless they are associated with
     * a field.
     *
     * @return all the searchers in JIRA.
     */
    public Collection<IssueSearcher<?>> getAllSearchers();

    /**
     * Get all searcher groups with the {@link IssueSearcher} that are applicable for the context.
     * {@link com.atlassian.jira.issue.search.searchers.IssueSearcher#getSearchRenderer()#isShown(SearchContext)} method.
     * Note that the {@link SearcherGroup} will still appear even if no {@link IssueSearcher} are shown for the group.
     * @param searchContext for the searcher groups.
     * @return Collection of {@link SearcherGroup}
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
     * Refreshes the {@link IssueSearcher} cache
     */
    void refresh();

}
