package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;

import java.util.Collection;

/**
 * Action class for managing filters.  Also used by delete filter.
 */
public class DeleteFilter extends AbstractFilterAction
{
    private final SearchRequestService searchRequestService;
    private final FilterSubscriptionService subscriptionService;
    private final FavouritesService favouriteService;

    private Long otherFavouriteCount;
    private Collection subscriptions;

    public DeleteFilter(final IssueSearcherManager issueSearcherManager,
                        final SearchRequestService searchRequestService, final FavouritesService favouriteService,
                        final SearchService searchService, final SearchSortUtil searchSortUtil, FilterSubscriptionService subscriptionService)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        this.searchRequestService = searchRequestService;
        this.favouriteService = favouriteService;
        this.subscriptionService = subscriptionService;
    }

    public int getOtherFavouriteCount()
    {
        if (otherFavouriteCount == null)
        {
            final SearchRequest request = getFilter();

            // We want to know how many times it has been favourited by OTHER people
            final boolean isFavourite = favouriteService.isFavourite(getLoggedInApplicationUser(), request);
            final int count = isFavourite ? request.getFavouriteCount().intValue() - 1 : request.getFavouriteCount().intValue();
            otherFavouriteCount = (long) count;
        }
        return otherFavouriteCount.intValue();
    }

    public boolean canDelete()
    {
        return !hasAnyErrors();
    }

    public int getSubscriptionCount()
    {
        return getSubscriptions().size();
    }

    public Collection getSubscriptions()
    {
        if (subscriptions == null)
        {
            subscriptions = subscriptionService.getVisibleSubscriptions(getLoggedInApplicationUser(), getFilter());
        }
        return subscriptions;
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

    @Override
    public String doDefault() throws Exception
    {
        final JiraServiceContext ctx = getJiraServiceContext();

        if (getFilterId() != null)
        {
            searchRequestService.validateForDelete(ctx, getFilterId());

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

        return INPUT;
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
            searchRequestService.deleteFilter(ctx, getFilterId());
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
        return returnComplete(getReturnUrl());
    }
}
