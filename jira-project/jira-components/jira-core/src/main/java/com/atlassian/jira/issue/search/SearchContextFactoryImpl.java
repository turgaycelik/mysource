package com.atlassian.jira.issue.search;

import java.util.List;

/**
 * Use this factory to construct a {@link SearchContext}.
 *
 * @since v5.0
 */
public class SearchContextFactoryImpl implements SearchContextFactory
{
    @Override
    public SearchContext create(List projectCategoryIds, List projectIds, List issueTypeIds)
    {
        return new SearchContextImpl(projectCategoryIds, projectIds, issueTypeIds);
    }

    @Override
    public SearchContext create()
    {
        return new SearchContextImpl();
    }

    @Override
    public SearchContext create(SearchContext searchContext)
    {
        return new SearchContextImpl(searchContext);
    }


}
