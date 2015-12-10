package com.atlassian.jira.web.action.admin.filters;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.action.issue.SearchDescriptionEnabledAction;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;

/**
 * This is the base class for DeleteSharedFilter and ChangeSharedFilterOwner
 *
 * @since v4.4
 */
public abstract class AbstractAdministerFilter extends SearchDescriptionEnabledAction
{
    private SearchRequest filter;
    private Long filterId;
    private String searchName;
    private String searchOwnerUserName;
    private String sortColumn;
    private String sortAscending;
    private String pagingOffset;
    private String totalResultCount;
    private final SearchRequestService searchRequestService;
    private final PermissionManager permissionManager;
    private final SearchRequestManager searchRequestManager;

    public AbstractAdministerFilter(IssueSearcherManager issueSearcherManager, SearchRequestService searchRequestService,
            FavouritesService favouriteService, SearchService searchService, SearchSortUtil searchSortUtil,
            FilterSubscriptionService subscriptionService, PermissionManager permissionManager,
            SearchRequestManager searchRequestManager)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        this.searchRequestService = searchRequestService;
        this.permissionManager = permissionManager;
        this.searchRequestManager = searchRequestManager;
    }

    /**
     * This will always return the Filter using the filter context - this means you will (should?) never get Permission
     * exceptions
     * @return  the {@link SearchRequest} that represents the current filterId
     */

    protected SearchRequest getFilter()
    {
        if ((filter == null) && (getFilterId() != null))
        {
            final JiraServiceContext ctx = getJiraServiceContext(getFilterId());
            filter = searchRequestService.getFilter(ctx, getFilterId());
        }
        return filter;
    }

    public Long getFilterId()
    {
        return filterId;
    }

    public void setFilterId(final Long filterId)
    {
        this.filterId = filterId;
    }

    public String getFilterName() throws GenericEntityException
    {
        final SearchRequest filter = getFilter();
        return (filter == null) ? null : filter.getName();
    }

    public String getSearchName()
    {
        return searchName;
    }

    public void setSearchName(String searchName)
    {
        this.searchName = searchName;
    }

    public String getSearchOwnerUserName()
    {
        return searchOwnerUserName;
    }

    public void setSearchOwnerUserName(String searchOwnerUserName)
    {
        this.searchOwnerUserName = searchOwnerUserName;
    }

    public String getSortColumn()
    {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn)
    {
        this.sortColumn = sortColumn;
    }

    public String getSortAscending()
    {
        return sortAscending;
    }

    public void setSortAscending(String sortAscending)
    {
        this.sortAscending = sortAscending;
    }

    public String getPagingOffset()
    {
        return pagingOffset;
    }

    public void setPagingOffset(String pagingOffset)
    {
        this.pagingOffset = pagingOffset;
    }

    public String getTotalResultCount()
    {
        return totalResultCount;
    }

    public void setTotalResultCount(String totalResultCount)
    {
        this.totalResultCount = totalResultCount;
    }

    /**
     *
     * @param filterId    The id of the filter
     * @return        A Jira Service context that represents the owner of the filter if you are the administrator
     */
    protected JiraServiceContext getJiraServiceContext(Long filterId)
    {
        JiraServiceContext ctx;
        if (permissionManager.hasPermission(Permissions.ADMINISTER, getLoggedInUser()))
        {
            ctx = new JiraServiceContextImpl(searchRequestManager.getSearchRequestOwner(filterId));
        }
        else
        {
            ctx =  getJiraServiceContext();
        }
        return ctx;
    }

    protected String buildReturnUri()
    {
        StringBuilder url = new StringBuilder(getReturnUrl());
        url.append("?atl_token=").append(JiraUrlCodec.encode(getXsrfToken(), "UTF-8"));
        if (StringUtils.isNotBlank(getSearchName()))
        {
            url.append("&searchName=").append(JiraUrlCodec.encode(getSearchName(), "UTF-8"));
        }
        if (StringUtils.isNotBlank(getSearchOwnerUserName()))
        {
            url.append("&searchOwnerUserName=").append(JiraUrlCodec.encode(getSearchOwnerUserName(), "UTF-8"));
        }
        if (StringUtils.isNotBlank(getSortColumn()))
        {
            url.append("&sortColumn=").append(JiraUrlCodec.encode(getSortColumn(), "UTF-8"));
            url.append("&sortAscending=").append(JiraUrlCodec.encode(getSortAscending(), "UTF-8"));
            url.append("&pagingOffset=").append(JiraUrlCodec.encode(getPagingOffset(), "UTF-8"));
            url.append("&totalResultCount=").append(JiraUrlCodec.encode(getTotalResultCount(), "UTF-8"));
        }
        return url.toString();
    }
}
