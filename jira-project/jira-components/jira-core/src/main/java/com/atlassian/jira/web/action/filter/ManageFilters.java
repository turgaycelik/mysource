package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.issue.SearchDescriptionEnabledAction;
import com.atlassian.jira.web.action.util.SearchRequestDisplayBean;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchAction;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchViewHelper;
import com.atlassian.jira.web.ui.model.DropDownModel;
import com.atlassian.jira.web.ui.model.DropDownModelBuilder;
import com.atlassian.jira.web.ui.model.DropDownModelProvider;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.collect.ImmutableList;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Action class for managing filters.  Also used by delete filter.
 */
public class ManageFilters extends SearchDescriptionEnabledAction
        implements SharedEntitySearchAction, FilterOperationsAction, DropDownModelProvider<SearchRequestDisplayBean>
{
    private static final FilterLinkRenderer ISSUE_NAVIGATOR_LINK_RENDERER = new FilterLinkRenderer()
    {
        public String render(final Long id, final String name)
        {
            return "<a id=\"filterlink_" + id + "\" href=\"IssueNavigator.jspa?mode=hide&requestId=" + id + "\">" + TextUtils.htmlEncode(name) + "</a>";
        }
    };

    protected static final String CONTENTONLY = "contentonly";

    private static final String KEY_NO_SEARCH = "common.sharing.searching.no.search.performed";
    private static final String KEY_NO_RESULTS = "filters.no.search.results";

    private final SearchRequestService searchRequestService;
    protected final SearchRequestDisplayBean.Factory beanFactory;
    private final FilterViewHelper filterHelper;

    private String filterView;
    private String searchEmptyMessageKey = KEY_NO_RESULTS;

    private List<SearchRequestDisplayBean> filters;
    private FilterViewHelper.SearchResult searchResults;

    private boolean contentOnly = false;
    private boolean searchContentOnly = false;

    private FilterViewTabs validTabs;
    private List<SearchRequestDisplayBean> subscribedFilters;
    private WebResourceManager webResourceManager;

    public ManageFilters(final JiraAuthenticationContext authCtx, final IssueSearcherManager issueSearcherManager,
            final SearchRequestService searchRequestService, final ShareTypeFactory shareTypeFactory,
            final SearchRequestDisplayBean.Factory beanFactory, final SearchService searchService,
            final SearchSortUtil searchSortUtil, final WebResourceManager webResourceManager)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        this.searchRequestService = searchRequestService;
        this.beanFactory = beanFactory;
        this.webResourceManager = webResourceManager;
        filterHelper = new FilterViewHelper(shareTypeFactory, authCtx, ActionContext.getRequest().getContextPath(),
                "ManageFilters.jspa?filterView=search", searchRequestService);

        setReturnUrl("ManageFilters.jspa");
    }

    //
    // Implementation of the SharedEntitySearchAction interface.
    //
    public String getSearchName()
    {
        return getFilterHelper().getSearchName();
    }

    public void setSearchName(final String searchName)
    {
        getFilterHelper().setSearchName(searchName);
    }

    public String getSearchOwnerUserName()
    {
        return getFilterHelper().getSearchOwnerUserName();
    }

    public void setSearchOwnerUserName(final String searchOwnerUserName)
    {
        getFilterHelper().setSearchOwnerUserName(searchOwnerUserName);
    }

    public String getSearchShareType()
    {
        return getFilterHelper().getSearchShareType();
    }

    public void setSearchShareType(final String searchShareType)
    {
        getFilterHelper().setSearchShareType(searchShareType);
    }

    public void setGroupShare(final String groupShare)
    {
        getFilterHelper().setGroupShare(groupShare);
    }

    public String getGroupShare()
    {
        return getFilterHelper().getGroupShare();
    }

    public Long getPagingOffset()
    {
        return getFilterHelper().getPagingOffset();
    }

    public void setProjectShare(final String projectShare)
    {
        getFilterHelper().setProjectShare(projectShare);
    }

    public String getProjectShare()
    {
        return getFilterHelper().getProjectShare();
    }

    public void setRoleShare(final String roleShare)
    {
        getFilterHelper().setRoleShare(roleShare);
    }

    public String getRoleShare()
    {
        return getFilterHelper().getRoleShare();
    }

    public void setPagingOffset(final Long pagingOffset)
    {
        getFilterHelper().setPagingOffset(pagingOffset);
    }

    public String getSortColumn()
    {
        return getFilterHelper().getSortColumn();
    }

    public void setSortColumn(final String sortColumn)
    {
        getFilterHelper().setSortColumn(sortColumn);
    }

    public boolean isSortAscending()
    {
        return getFilterHelper().isSortAscending();
    }

    public void setSortAscending(final boolean sortAscending)
    {
        getFilterHelper().setSortAscending(sortAscending);
    }

    //
    // Parameter methods.
    //
    public String getFilterView()
    {
        return filterView;
    }

    public void setFilterView(final String filterView)
    {
        this.filterView = filterView;
    }

    //
    //Methods used by the view.
    //
    public List getFilters()
    {
        return filters;
    }

    protected void setFilters(List<SearchRequestDisplayBean> filters)
    {
        this.filters=filters;
    }

    public String getNextUrl()
    {
        return searchResults != null ? searchResults.getNextUrl() : null;
    }

    public String getPreviousUrl()
    {
        return searchResults != null ? searchResults.getPreviousUrl() : null;
    }

    public int getStartPosition()
    {
        return searchResults != null ? searchResults.getStartResultPosition() : -1;
    }

    public int getEndPosition()
    {
        return searchResults != null ? searchResults.getEndResultPosition() : -1;
    }

    public int getTotalResultCount()
    {
        return searchResults != null ? searchResults.getTotalResultCount() : -1;
    }

    public FilterViewHelper getFiltersViewHelper()
    {
        return getFilterHelper();
    }

    protected void setSearchResults(SharedEntitySearchViewHelper.SearchResult<SearchRequest> searchResults)
    {
        this.searchResults = searchResults;
    }
    @Override
    public DropDownModel getDropDownModel(final SearchRequestDisplayBean displayBean, final int listIndex)
    {
        DropDownModelBuilder builder = DropDownModelBuilder.builder();

        if (displayBean.isCurrentOwner())
        {
            builder.setTopText(getText("common.words.operations"));
            builder.startSection()
                    .addItem(
                            builder.item()
                                    .setText(getText("managefilters.edit.filter"))
                                    .setAttr("id", "edit_filter_" + displayBean.getId())
                                    .setAttr("class", "edit-filter")
                                    .setAttr("href",toUrl(displayBean, "EditFilter!default.jspa", true))
                    )
                    .addItem(
                            builder.item()
                                    .setText(getText("managefilters.delete"))
                                    .setAttr("id", "delete_" + displayBean.getId())
                                    .setAttr("class", "delete-filter")
                                    .setAttr("href", toUrl(displayBean, "DeleteFilter!default.jspa", true))
                                    .setAttr("rel", ""+ displayBean.getId())
                    );

            builder.endSection();
        }
        return builder.build();
    }

    protected String toUrl(SearchRequestDisplayBean filter, final String page, final boolean includeReturnUrl)
     {
         StringBuilder url = new StringBuilder(page);
         url.append("?filterId=").append(filter.getId());
         if (includeReturnUrl && StringUtils.isNotBlank(getReturnUrl()))
         {
             url.append("&returnUrl=").append(getReturnUrl());
         }
         return getUriValidator().getSafeUri("", url.toString());
     }



    /**
     * Can we display the favourite column?  Determined by if there is a user in the current session.
     *
     * @return true if there is a logged in user, else false
     */
    public boolean canShowFavourite()
    {
        return isUserLoggedIn();
    }

    /**
     * Is there a user associated with the session.
     *
     * @return true if a user is associated with the action or false otherwise.
     */
    public boolean isUserLoggedIn()
    {
        return getLoggedInUser() != null;
    }

    public boolean isSearchRequested()
    {
        return ActionContext.getParameters().get("Search") != null;
    }

    public String getSearchEmptyMessageKey()
    {
        return searchEmptyMessageKey;
    }

    public boolean isTabShowing(final String tab)
    {
        return getTabs().isValid(tab);
    }

    public boolean isFirstTab(final String tab)
    {
        return getTabs().isFirst(tab);
    }

    private FilterViewTabs getTabs()
    {
        if (validTabs == null)
        {
            if (!isUserLoggedIn())
            {
                validTabs = new FilterViewTabs(ImmutableList.of(FilterViewTabs.POPULAR, FilterViewTabs.SEARCH), FilterViewTabs.POPULAR);
            }
            else
            {
                validTabs = new FilterViewTabs(ImmutableList.of(FilterViewTabs.FAVOURITES, FilterViewTabs.MY, FilterViewTabs.POPULAR,
                        FilterViewTabs.SEARCH), FilterViewTabs.FAVOURITES);
            }
        }

        return validTabs;
    }

    protected String doExecute()
    {

        if (filterView != null)
        {
            ActionContext.getSession().put(SessionKeys.MANAGE_FILTERS_TAB, filterView);
        }
        else
        {
            filterView = (String) ActionContext.getSession().get(SessionKeys.MANAGE_FILTERS_TAB);
        }


        if (contentOnly && !getTabs().isValid(filterView))
        {
            ServletActionContext.getResponse().setStatus(401);
            return NONE;
        }

        // Detect what the filter is and for the current state of the user and data input in case of URL hack
        final FilterViewTabs.Tab filterViewTab = getTabs().getTabSafely(filterView);
        if (!contentOnly)
        {
            webResourceManager.requireResource("jira.webresources:managefilters");
        }

        if (filterViewTab != null)
        {
            filterView = filterViewTab.getName();

            // now go to different methods to perform the actual action code
            if (FilterViewTabs.SEARCH == filterViewTab)
            {
                return executeSearchView();
            }
            else if (FilterViewTabs.POPULAR == filterViewTab)
            {
                return executePopularView();
            }
            else if (FilterViewTabs.MY == filterViewTab)
            {
                return executeMyView();
            }
            else if (FilterViewTabs.FAVOURITES == filterViewTab)
            {
                return executeFavouriteView();
            }
            else
            {
                filterView = null;
            }

        }
        else
        {
            filterView = null;
        }

        return INPUT;
    }

    public String doView()
    {
        return SUCCESS;
    }

    public void setContentOnly(boolean contentOnly)
    {
        this.contentOnly = contentOnly;
    }
    public boolean isContentOnly()
    {
        return contentOnly;
    }

    public void setSearchContentOnly(boolean searchContentOnly)
    {
        this.searchContentOnly = searchContentOnly;
    }

    public boolean isSearchContentOnly()
    {
        return searchContentOnly;
    }

    public List<SearchRequestDisplayBean> getFiltersWithSubscriptions()
    {
        if (subscribedFilters == null)
        {
            if (filters == null)
            {
                subscribedFilters = emptyList();
            }
            else
            {
                subscribedFilters = new ArrayList<SearchRequestDisplayBean>();
                for (SearchRequestDisplayBean search : filters)
                {
                    if (search.getSubscriptionCount() > 0)
                    {
                        subscribedFilters.add(search);
                    }
                }
            }

        }
        return subscribedFilters;
    }

    public int getFiltersWithSubscriptionsCount()
    {
        return getFiltersWithSubscriptions().size();
    }

    //
    //Search methods.
    //

    private String executeFavouriteView()
    {
        final Collection<SearchRequest> favouriteFilters = searchRequestService.getFavouriteFilters(getLoggedInApplicationUser());
        filters = beanFactory.createDisplayBeans(favouriteFilters);
        return contentOnly ? CONTENTONLY : SUCCESS;
    }

    private String executeMyView()
    {
        final Collection<SearchRequest> myFilters = searchRequestService.getOwnedFilters(getLoggedInApplicationUser());
        filters = beanFactory.createDisplayBeans(myFilters);
        return contentOnly ? CONTENTONLY : SUCCESS;
    }

    /**
     * This action method handles the actual Searching for SharedEntitys in this case Search Requests.
     *
     * @return the JSP view
     */
    protected String executeSearchView()
    {
        filterView = FilterViewTabs.SEARCH.getName();
        ActionContext.getSession().put(SessionKeys.MANAGE_FILTERS_TAB, filterView);

        // if they have pressed the 'Search' button then
        boolean searchPerformed = isSearchRequested();
        if (searchPerformed)
        {
            final JiraServiceContext ctx = getJiraServiceContext();
            searchResults = getFilterHelper().search(ctx);
            if ((searchResults == null) || ctx.getErrorCollection().hasAnyErrors())
            {
                searchPerformed = false;
            }
            else
            {
                filters = beanFactory.createDisplayBeans(searchResults.getResults());
            }
        }
        if (!searchPerformed)
        {
            searchEmptyMessageKey = KEY_NO_SEARCH;
        }
        return contentOnly ? CONTENTONLY : SUCCESS;
    }

    /**
     * This action method handles the actual Searching for Popular Shared Filters
     *
     * @return the JSP view
     */
    private String executePopularView()
    {
        filterView = FilterViewTabs.POPULAR.getName();
        ActionContext.getSession().put(SessionKeys.MANAGE_FILTERS_TAB, filterView);

        final JiraServiceContext serviceContext = getJiraServiceContext();
        final List<SearchRequest> results = getFilterHelper().getPopularFilters(serviceContext);
        if ((results != null) && !serviceContext.getErrorCollection().hasAnyErrors())
        {
            filters = beanFactory.createDisplayBeans(results);
        }
        return contentOnly ? CONTENTONLY : SUCCESS;
    }

    public FilterLinkRenderer getFilterLinkRenderer()
    {
        return ISSUE_NAVIGATOR_LINK_RENDERER;
    }

    protected FilterViewHelper getFilterHelper()
    {
        return filterHelper;
    }
}
