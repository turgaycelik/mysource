package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroup;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.issue.transport.impl.IssueNavigatorActionParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.IssueActionSupport;
import com.atlassian.jira.web.action.filter.FilterOperationsBean;
import com.atlassian.query.Query;
import com.atlassian.query.order.SearchSort;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class SearchDescriptionEnabledAction extends IssueActionSupport
{
    protected final IssueSearcherManager issueSearcherManager;
    protected FieldValuesHolder fieldValuesHolder;
    protected PermissionManager permissionManager;
    private JiraAuthenticationContext authenticationContext;
    private SearchContext searchContext;
    private IssueNavigatorActionParams actionParams;
    private FavouritesService favouritesService;
    private FilterOperationsBean filterOperationsBean;
    private final SearchService searchService;
    private final SearchSortUtil searchSortUtil;

    public SearchDescriptionEnabledAction(IssueSearcherManager issueSearcherManager, SearchService searchService, final SearchSortUtil searchSortUtil)
    {
        this.issueSearcherManager = issueSearcherManager;
        this.searchService = searchService;
        this.searchSortUtil = searchSortUtil;
        this.permissionManager = ComponentAccessor.getPermissionManager();
        this.authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        this.favouritesService = ComponentAccessor.getComponentOfType(FavouritesService.class);
    }

    public boolean isFilterFavourite()
    {
        return favouritesService.isFavourite(getLoggedInApplicationUser(), getSearchRequest());
    }

    protected void populateFieldValuesHolderFromQuery(final Query query, final FieldValuesHolder fieldValuesHolder)
    {
        final SearchContext context = searchService.getSearchContext(getLoggedInUser(), query);
        final Collection<IssueSearcher<?>> searchers = getSearchers();
        for (IssueSearcher<?> searcher : searchers)
        {
            searcher.getSearchInputTransformer().populateFromQuery(getLoggedInUser(), fieldValuesHolder, query, context);
        }
    }

    public Collection getSearcherGroups()
    {
        return issueSearcherManager.getSearcherGroups();
    }

    public String getSearcherViewHtml(IssueSearcher searcher)
    {
        final SearchRequest searchRequest = getSearchRequest();
        final SearchRenderer searchRenderer = searcher.getSearchRenderer();
        if (searchRequest != null && searchRenderer.isRelevantForQuery(getLoggedInUser(), searchRequest.getQuery()))
        {
            final SearchContext searchContext = getSearchContext();
            return searchRenderer.getViewHtml(getLoggedInUser(), searchContext, getFieldValuesHolder(), new HashMap(), this);
        }
        else
        {
            return "";
        }
    }

    public boolean isSearchRequestFitsNavigator()
    {
        return searchService.doesQueryFitFilterForm(getLoggedInUser(), (getSearchRequest() != null) ? getSearchRequest().getQuery() : null);
    }

    public boolean isShown(SearcherGroup searcherGroup)
    {
        return searcherGroup.isShown(getLoggedInUser(), getSearchContext());
    }

    protected IssueNavigatorActionParams getActionParams()
    {
        if (actionParams == null)
        {
            actionParams = new IssueNavigatorActionParams(ActionContext.getParameters());
        }

        return actionParams;
    }

    protected Collection<IssueSearcher<?>> getSearchers()
    {
        return issueSearcherManager.getAllSearchers();
    }

    protected SearchContext getSearchContext()
    {
        if (getActionParams().isUpdateParamsRequired())
        {
            final String query = getActionParams().getFirstValueForKey("jqlQuery");
            if (query != null)
            {
                return getSeachContextFromQueryString(query);
            }
            else
            {
                return getActionParams().getSearchContext();
            }
        }
        else
        {
            final SearchRequest searchRequest = getSearchRequest();
            if (searchRequest != null)
            {
                return searchService.getSearchContext(authenticationContext.getLoggedInUser(), searchRequest.getQuery());
            }
            else
            {
                if (searchContext == null)
                {
                    searchContext = createSearchContext();
                }
                return searchContext;
            }
        }
    }

    private SearchContext getSeachContextFromQueryString(final String query)
    {
        if (StringUtils.isNotBlank(query))
        {
            final SearchService.ParseResult jqlQuery = searchService.parseQuery(getLoggedInUser(), query);
            if (jqlQuery.isValid())
            {
                return searchService.getSearchContext(getLoggedInUser(), jqlQuery.getQuery());
            }
        }
        return createSearchContext();
    }

    private SearchContext createSearchContext()
    {
        Collection visibleProjects = permissionManager.getProjects(Permissions.BROWSE, authenticationContext.getLoggedInUser());
        if (visibleProjects != null && visibleProjects.size() == 1)
        {
            return new SearchContextImpl(null, EasyList.build(((GenericValue) visibleProjects.iterator().next()).getLong("id")), null);
        }
        else
        {
            return new SearchContextImpl();
        }
    }

    protected FieldValuesHolder getFieldValuesHolder()
    {
        if (fieldValuesHolder == null)
        {
            fieldValuesHolder = new FieldValuesHolderImpl();
            if (getSearchRequest() != null)
            {
                populateFieldValuesHolderFromQuery(getSearchRequest().getQuery(), fieldValuesHolder);
            }
        }

        return fieldValuesHolder;
    }

    public boolean validateSearchFilterIsSavedFilter(SearchRequest searchRequest, String i18n)
    {
        if (searchRequest.getId() == null)
        {
            // The id and name should be null but if they are not I want to know about it.
            String name = searchRequest.getName();
            log.error("Tried to perform operation on unsaved filter with id:" + searchRequest.getId() + " and name: " + name);
            addErrorMessage(getText(i18n, name == null ? "" : name));
            return false;
        }
        return true;
    }

    public FilterOperationsBean getFilterOperationsBean()
    {
        if (filterOperationsBean == null)
        {
            filterOperationsBean = createFilterOperationsBean(getLoggedInApplicationUser());
        }
        return filterOperationsBean;

    }

    protected FilterOperationsBean createFilterOperationsBean(final ApplicationUser user)
    {
        return FilterOperationsBean.create(getSearchRequest(), isFilterValid(), user, false);
    }

    /**
     * Check if the filter in session is valid or not.
     *
     * @return true iff the current filter is valid or false otherwise.
     */
    public boolean isFilterValid()
    {
        final SearchRequest searchRequest = getSearchRequest();
        return !(searchRequest != null && searchService.validateQuery(getLoggedInUser(), searchRequest.getQuery(), searchRequest.getId()).hasAnyErrors());
    }

    public String getSearchRequestJqlString()
    {
        final SearchRequest searchRequest = getSearchRequest();
        if(searchRequest != null)
        {
            return searchService.getJqlString(searchRequest.getQuery());
        }
        else
        {
            return "";
        }
    }

    public List<SearchSort> getSearchSorts()
    {
        final SearchRequest searchRequest = getSearchRequest();
        if (searchRequest != null)
        {
            return searchSortUtil.getSearchSorts(searchRequest.getQuery());
        }
        return null;
    }
}
