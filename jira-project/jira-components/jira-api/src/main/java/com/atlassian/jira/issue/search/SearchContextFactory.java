package com.atlassian.jira.issue.search;

import java.util.List;

/**
 *  Use this factory to construct a {@link SearchContext}.
 *
 * @since v5.0
 */
public interface SearchContextFactory
{
    /**
     * Creates a {@link com.atlassian.jira.issue.search.SearchContext} with the given project categories, projects and issue types.
     *
     * @param projectCategoryIds the ids of the project categories.
     * @param projectIds the project ids.
     * @param issueTypeIds the issue type ids.
     *
     * @return the {@link com.atlassian.jira.issue.search.SearchContext}
     */
    SearchContext create(List projectCategoryIds, List projectIds, List issueTypeIds);

    /**
     * Creates an empty {@link com.atlassian.jira.issue.search.SearchContext}
     *
     * @return the {@link com.atlassian.jira.issue.search.SearchContext}
     */
    SearchContext create();

    /**
     * Creates a search context based on the given {@link com.atlassian.jira.issue.search.SearchContext}.
     *
     * @param searchContext an existing {@link com.atlassian.jira.issue.search.SearchContext}.
     *
     * @return a new {@link com.atlassian.jira.issue.search.SearchContext} based on the given search context.
     */
    SearchContext create(SearchContext searchContext);
}
