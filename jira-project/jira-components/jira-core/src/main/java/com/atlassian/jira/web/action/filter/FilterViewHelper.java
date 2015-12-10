package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchViewHelper;

/**
 * A class to help with implementing searching of Filters. 
 *
 * @since v3.13
 */
public class FilterViewHelper extends SharedEntitySearchViewHelper<SearchRequest>
{
    private final SearchRequestService searchRequestService;

    public FilterViewHelper(final ShareTypeFactory shareTypeFactory, final JiraAuthenticationContext authCtx,
            final String applicationContext, final String actionUrlPrefix, final SearchRequestService searchRequestService)
    {
        super(shareTypeFactory, authCtx, applicationContext, actionUrlPrefix, "filterView", "search", SearchRequest.ENTITY_TYPE);

        Assertions.notNull("searchRequestService", searchRequestService);
        this.searchRequestService = searchRequestService;
    }

    @Override
    public SharedEntitySearchContext getEntitySearchContext()
    {
        return SharedEntitySearchContext.USE;
    }

    protected SharedEntitySearchResult<SearchRequest> doExecuteSearch(final JiraServiceContext ctx, final SharedEntitySearchParameters searchParameters, final int pageOffset, final int pageWidth)
    {
        return searchRequestService.search(ctx, searchParameters, pageOffset, pageWidth);
    }

    protected boolean validateSearchParameters(final JiraServiceContext ctx, final SharedEntitySearchParameters searchParameters, final int pageOffset, final int pageWidth)
    {
        searchRequestService.validateForSearch(ctx, searchParameters);
        return !ctx.getErrorCollection().hasAnyErrors();
    }
}
