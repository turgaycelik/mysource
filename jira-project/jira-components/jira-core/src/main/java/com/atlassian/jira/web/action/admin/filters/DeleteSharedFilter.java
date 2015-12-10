package com.atlassian.jira.web.action.admin.filters;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * The Delete Shared Filters action
 *
 * @since v4.4
 */
public class DeleteSharedFilter extends AbstractAdministerFilter
{
    private static final int FILTERS_PER_PAGE = 20;

    private final SearchRequestService searchRequestService;
    private final FilterSubscriptionService subscriptionService;
    private final FavouritesService favouriteService;
    private final SearchRequestManager searchRequestManager;

    private Long otherFavouriteCount;
    private Collection<GenericValue> subscriptions;

    public DeleteSharedFilter(final IssueSearcherManager issueSearcherManager,
            final SearchRequestService searchRequestService, final FavouritesService favouriteService,
            final SearchService searchService, final SearchSortUtil searchSortUtil, FilterSubscriptionService subscriptionService, PermissionManager permissionManager, SearchRequestManager searchRequestManager)
    {
        super(issueSearcherManager, searchRequestService, favouriteService, searchService, searchSortUtil, subscriptionService, permissionManager, searchRequestManager);
        this.searchRequestService = searchRequestService;
        this.favouriteService = favouriteService;
        this.subscriptionService = subscriptionService;
        this.searchRequestManager = searchRequestManager;
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final JiraServiceContext ctx = getJiraServiceContext();
        if (getFilterId() != null)
        {
            searchRequestService.validateForDelete(ctx, getFilterId());

            if (hasAnyErrors())
            {
                return ERROR;
            }
            searchRequestService.deleteFilter(getJiraServiceContext(getFilterId()), getFilterId());
            if (hasAnyErrors())
            {
                return ERROR;
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.filters.cannot.delete.filter"));
            return ERROR;
        }

        setSearchRequest(null);
        repaginateIfNeeded();
        if (isInlineDialogMode())
        {
            return returnCompleteWithInlineRedirect(buildReturnUri());
        }
        else
        {
            String returnUrl =  buildReturnUri();
            setReturnUrl(null);
            return forceRedirect(returnUrl);
        }
    }

    private void repaginateIfNeeded()
    {
        // only need to repaginate if on last page
        final int pagingOffset = StringUtils.isNotBlank(getPagingOffset()) ? Integer.parseInt(getPagingOffset()) - 1 : -1;
        final int newResultCount = StringUtils.isNotBlank(getTotalResultCount()) ? Integer.parseInt(getTotalResultCount()) - 1 :  -1;
        if (pagingOffset >= 0)
        {
            setTotalResultCount(""+newResultCount);
            if (newResultCount % FILTERS_PER_PAGE == 0)
            {
                setPagingOffset(""+pagingOffset);
            }
        }
    }

    public int getOtherFavouriteCount()
    {
        if (otherFavouriteCount == null)
        {
            final SearchRequest request = getFilter();

            // We want to know how many times it has been favourited by OTHER people
            ApplicationUser filterOwner = searchRequestManager.getSearchRequestOwner(getFilterId());
            final boolean isFavourite = favouriteService.isFavourite(filterOwner, request);
            final int count = isFavourite ? request.getFavouriteCount().intValue() - 1 : request.getFavouriteCount().intValue();
            otherFavouriteCount = (long) count;
        }
        return otherFavouriteCount.intValue();
    }

    public int getSubscriptionCount()
    {
        return getSubscriptions().size();
    }

    public Collection getSubscriptions()
    {
        if (subscriptions == null)
        {
            ApplicationUser filterOwner = searchRequestManager.getSearchRequestOwner(getFilterId());
            subscriptions = subscriptionService.getVisibleSubscriptions(filterOwner, getFilter());
        }
        return subscriptions;
    }

    public boolean canDelete()
    {
        return !hasAnyErrors();
    }

}
