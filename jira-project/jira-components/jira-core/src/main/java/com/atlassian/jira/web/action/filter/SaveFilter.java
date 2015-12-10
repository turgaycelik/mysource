package com.atlassian.jira.web.action.filter;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.diff.DiffViewBean;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.web.action.util.DiffViewRenderer;
import com.atlassian.query.order.SearchSort;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Action called to update a filter's search parameters and search sorts.
 */
public class SaveFilter extends AbstractFilterAction implements FilterOperationsAction
{
    private final SearchRequestService searchRequestService;
    private final SearchService searchService;
    private final DiffViewRenderer diffViewRenderer;
    private SearchRequest dbSearchRequest;
    private SearchContext dbSearchContext;
    private FieldValuesHolder dbFieldValuesHolder;
    private DiffViewBean wordLevelDiff;

    public SaveFilter(IssueSearcherManager issueSearcherManager, final SearchRequestService searchRequestService, final SearchService searchService, final DiffViewRenderer diffViewRenderer, final SearchSortUtil searchSortUtil)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        this.searchRequestService = searchRequestService;
        this.searchService = searchService;
        this.diffViewRenderer = diffViewRenderer;
    }

    public SaveFilter(final SearchRequestService searchRequestService, final SearchService searchService, final DiffViewRenderer diffViewRenderer, final SearchSortUtil searchSortUtil)
    {
        this(ComponentAccessor.getComponentOfType(IssueSearcherManager.class), searchRequestService, searchService, diffViewRenderer, searchSortUtil);
    }

    public String doDefault() throws Exception
    {
        if (!validateSearchRequest())
        {
            return ERROR;
        }
        else
        {
            updateDbRequestState();
            return INPUT;
        }
    }

    protected String doExecute() throws Exception
    {
        if (!validateSearchRequest())
        {
            return ERROR;
        }

        final SearchRequest newRequest = searchRequestService.updateSearchParameters(getJiraServiceContext(), getSearchRequest());
        if (newRequest == null || hasAnyErrors())
        {
            return ERROR;
        }
        else
        {
            setSearchRequest(newRequest);
            return getRedirect("IssueNavigator.jspa?mode=hide&requestId=" + getFilterId());
        }
    }

    public SearchRequest getDbSearchRequest()
    {
        return dbSearchRequest;
    }

    public List<SearchSort> getSearchSorts(SearchRequest searchRequest)
    {
        if (searchRequest != null && searchRequest.getQuery().getOrderByClause() != null)
        {
            return searchRequest.getQuery().getOrderByClause().getSearchSorts();
        }
        return Collections.emptyList();
    }

    public boolean isSearchSortsEqual()
    {
        return getSearchSorts(getSearchRequest()).equals(getSearchSorts(getDbSearchRequest()));
    }

    public boolean isAdvancedQuery()
    {
        return !searchService.doesQueryFitFilterForm(getLoggedInUser(), (dbSearchRequest != null) ? dbSearchRequest.getQuery() : null) ||
               !searchService.doesQueryFitFilterForm(getLoggedInUser(), (getSearchRequest() != null) ? getSearchRequest().getQuery() : null);
    }

    public String getDiffedDbSearchRequestJql()
    {
        return diffViewRenderer.getOriginalHtml(getWordLevelDiff());
    }

    public String getDiffedCurrentSearchRequestJql()
    {
        return diffViewRenderer.getRevisedHtml(getWordLevelDiff());
    }

    private DiffViewBean getWordLevelDiff()
    {
        if (wordLevelDiff == null)
        {
            wordLevelDiff = DiffViewBean.createWordLevelDiff(getDbSearchRequestJql(), getCurrentSearchRequestJql());
        }
        return wordLevelDiff;
    }

    private String getDbSearchRequestJql()
    {
        if (dbSearchRequest != null)
        {
            return searchService.getJqlString(dbSearchRequest.getQuery());
        }
        return "";
    }

    private String getCurrentSearchRequestJql()
    {
        if (getSearchRequest() != null)
        {
            return searchService.getJqlString(getSearchRequest().getQuery());
        }
        return "";
    }

    public String getOldSearcherViewHtml(IssueSearcher searcher)
    {
        final SearchRenderer searchRenderer = searcher.getSearchRenderer();
        if (searchRenderer.isRelevantForQuery(getLoggedInUser(), (getDbSearchRequest() != null) ? getDbSearchRequest().getQuery() : null))
        {
            return searchRenderer.getViewHtml(getLoggedInUser(), dbSearchContext, dbFieldValuesHolder, EasyMap.build("currentFieldValuesHolder", getFieldValuesHolder()), this);
        }
        else
        {
            return "";
        }
    }

    public String getNewSearcherViewHtml(IssueSearcher searcher)
    {
        final SearchContext searchContext = getSearchContext();
        final SearchRequest searchRequest = getSearchRequest();
        final SearchRenderer searchRenderer = searcher.getSearchRenderer();
        if (searchRequest != null && searchRenderer.isRelevantForQuery(getLoggedInUser(), searchRequest.getQuery()))
        {
            return searchRenderer.getViewHtml(getLoggedInUser(), searchContext, getFieldValuesHolder(), EasyMap.build("dbFieldValuesHolder", dbFieldValuesHolder), this);
        }
        else
        {
            return "";
        }
    }

    private void updateDbRequestState()
    {
        if (getSearchRequest() == null || getFilterId() == null)
        {
            dbSearchRequest = new SearchRequest();
            dbSearchRequest.setOwner(getLoggedInApplicationUser());
        }
        else
        {
            dbSearchRequest = getFilter();
        }

        dbFieldValuesHolder = new FieldValuesHolderImpl();
        dbSearchContext = searchService.getSearchContext(getLoggedInUser(), dbSearchRequest.getQuery());
        final Collection<IssueSearcher<?>> searchers = issueSearcherManager.getAllSearchers();
        for (IssueSearcher<?> searcher : searchers)
        {
            searcher.getSearchInputTransformer().populateFromQuery(getLoggedInUser(), dbFieldValuesHolder, dbSearchRequest.getQuery(), dbSearchContext);
        }
    }

    private boolean validateSearchRequest()
    {
        if (getSearchRequest() == null)
        {
            addErrorMessage(getText("admin.errors.filters.no.search.request"));
            return false;
        }
        else if (!searchRequestService.validateUpdateSearchParameters(getJiraServiceContext(), getSearchRequest()))
        {
            return false;
        }
        return true;
    }

    @Override
    public Long getFilterId()
    {
        final SearchRequest searchRequest = getSearchRequest();
        return searchRequest != null ? searchRequest.getId() : null;
    }
}
