package com.atlassian.jira.web.action.util;

import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.issue.search.ClauseTooComplexSearchException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.FullNameUserFormat;
import com.atlassian.jira.plugin.userformat.UserNameUserFormat;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.sharing.type.ShareTypePermissionChecker;
import com.atlassian.jira.sharing.type.ShareTypeRenderer;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.util.sharing.SharesListHelper;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ofbiz.core.entity.GenericValue;

/**
 * Utility class for displaying a search request. Does a lot of the calculations that need to be done. E.g. is it a
 * favourite, how many issues it contains, how many subscriptions.
 *
 * @since v3.13
 */
public class SearchRequestDisplayBean implements SharesListHelper
{
    private final JiraAuthenticationContext authCtx;
    private final SearchRequest request;
    private final SearchProvider searchProvider;
    private final FavouritesService favouriteService;
    private final PermissionManager permissionManager;
    private final FilterSubscriptionService subscriptionService;
    private final ShareTypeFactory shareTypeFactory;
    private final UserFormatManager userFormatManager;

    private Collection<GenericValue> subscriptions;
    private long issueCount = -1;
    private Boolean isFavourite = null;
    private Collection<SharePermission> sortedPermissions;
    private Collection<SharePermission> allSharePermissions;

    public SearchRequestDisplayBean(final JiraAuthenticationContext authCtx, final SearchRequest request, final SearchProvider searchProvider, final FavouritesService favouriteService, final PermissionManager permissionManager, final FilterSubscriptionService subscriptionService, final ShareTypeFactory shareTypeFactory, final UserFormatManager userFormatManager)
    {
        this.authCtx = authCtx;
        this.request = request;
        this.searchProvider = searchProvider;
        this.favouriteService = favouriteService;
        this.permissionManager = permissionManager;
        this.subscriptionService = subscriptionService;
        this.shareTypeFactory = shareTypeFactory;
        this.userFormatManager = userFormatManager;
    }

    public Long getId()
    {
        return request.getId();
    }

    public String getOwnerUserName()
    {
        final ApplicationUser owner = request.getOwner();
        if (owner != null)
        {
            return userFormatManager.formatUserkey(owner.getKey(), UserNameUserFormat.TYPE, "search_request");
        }
        return null;
    }

    public String getOwnerFullName()
    {
        final ApplicationUser owner = request.getOwner();
        if (owner != null)
        {
            return userFormatManager.formatUserkey(owner.getKey(), FullNameUserFormat.TYPE, "search_request");
        }
        return null;
    }

    public String getName()
    {
        return request.getName();
    }

    public boolean isCurrentOwner()
    {
        final ApplicationUser owner = request.getOwner();
        return owner != null && owner.equals(authCtx.getUser());
    }

    public String getDescription()
    {
        return request.getDescription();
    }

    /**
     * How many user visible issues does this filter match?
     *
     * @return the number of user visible issues that this filter matches
     */
    public long getIssueCount()
    {
        if (issueCount == -1)
        {
            try
            {
                issueCount = searchProvider.searchCount((request != null) ? request.getQuery() : null, authCtx.getLoggedInUser());
            }
            catch (final ClauseTooComplexSearchException e)
            {
                return -1;
            }
            catch (final SearchException e)
            {
                throw new RuntimeException(e);
            }
        }
        return issueCount;

    }

    /**
     * How many subscriptions does this filter have? Only counts those subscriptions a user can see.
     *
     * @return The number of subscriptions for this filter
     */
    public long getSubscriptionCount()
    {
        return getSubscriptions().size();
    }

    /**
     * Retrieves the subscriptions for this filter that the current user can see
     *
     * @return a Collection of GenericValue objects representing the subscriptions for this filter
     */
    private Collection<GenericValue> getSubscriptions()
    {
        if (subscriptions == null)
        {
            subscriptions = subscriptionService.getVisibleSubscriptions(authCtx.getUser(), request);
        }
        return subscriptions;
    }

    /**
     * Is this filter privately shared?
     *
     * @return true if the filter has privacy set to Private, else false
     */
    public boolean isPrivate()
    {
        return request.getPermissions().isPrivate();
    }

    /**
     * Is this filter a favourite of the current user
     *
     * @return true if the user has favourited this filter, else false
     */
    public boolean isFavourite()
    {
        final ApplicationUser user = authCtx.getUser();
        if (isFavourite == null)
        {
            if (user == null)
            {
                isFavourite = Boolean.FALSE;
            }
            else
            {
                isFavourite = favouriteService.isFavourite(user, request);
            }
        }
        return isFavourite;
    }

    /**
     * How many times has this filter been favourited
     *
     * @return the count of favourites of this issue
     */
    public Long getFavouriteCount()
    {
        return request.getFavouriteCount();
    }

    /**
     * How many times has this filter been favourited altered by by wether it will disabled or enabled
     *
     * @return the count of favourites of this issue ltered by by wether it will disabled or enabled
     */
    public long getAlternateFavouriteCount()
    {
        return (isFavourite()) ? (request.getFavouriteCount().intValue() - 1) : (request.getFavouriteCount().intValue() + 1);
    }

    /**
     * Is it possible to share filters
     *
     * @return true if it is Enterprise or Professional and the current user has the Share permission
     */
    public boolean canShare()
    {
        return permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, authCtx.getLoggedInUser());
    }

    /**
     * Is it possible to edit columns in this version of JIRA
     *
     * @return true if it is possible (it is an Enterprise or Professional instance), else false
     */
    public boolean canEditColumns()
    {
        return true;
    }

    public Collection<SharePermission> getSharePermissions()
    {
        if (sortedPermissions == null)
        {
            final List<SharePermission> permissions;
            if (isCurrentOwner())
            {
                permissions = new ArrayList<SharePermission>(request.getPermissions().getPermissionSet());
            }
            else
            {
                permissions = new ArrayList<SharePermission>();
                for (final SharePermission sharePermission : request.getPermissions())
                {
                    final ShareType type = shareTypeFactory.getShareType(sharePermission.getType());
                    if (type != null)
                    {
                        final ShareTypePermissionChecker permissionChecker = type.getPermissionsChecker();
                        if (permissionChecker.hasPermission(authCtx.getLoggedInUser(), sharePermission))
                        {
                            permissions.add(sharePermission);
                        }
                    }
                }
            }

            Collections.sort(permissions, shareTypeFactory.getPermissionComparator());
            sortedPermissions = permissions;
        }
        return sortedPermissions;
    }


    //  if the current user has admin rights, then they can see all shares , if not an admin user then you will only
    //  see the shares available to you
    public Collection<SharePermission> getAllSharePermissions()
    {
        if (permissionManager.hasPermission(Permissions.ADMINISTER, authCtx.getLoggedInUser()))
        {
            if (allSharePermissions == null)
            {
                final List<SharePermission> permissions = Lists.newArrayList(request.getPermissions());
                Collections.sort(permissions, shareTypeFactory.getPermissionComparator());
                allSharePermissions = permissions;
            }
            return allSharePermissions;
        }
        else
        {
            return getSharePermissions();
        }
    }


    /**
     * Return HTML view of the current sharing status of the filter.
     *
     * @param sharePermission the permision to generate HTML for.
     * @return html view of the current sharing state.
     */

    public String getShareView(final SharePermission sharePermission)
    {
        final ShareType type = shareTypeFactory.getShareType(sharePermission.getType());
        if (type != null)
        {
            final ShareTypeRenderer shareTypeRenderer = type.getRenderer();
            return shareTypeRenderer.renderPermission(sharePermission, authCtx);
        }
        return null;
    }

    /**
     * Return a simple description of the passed in permission.
     *
     * @param sharePermission The permission to describe.
     * @return a simple description of the passed in permission.
     */
    public String getSimpleDescription(final SharePermission sharePermission)
    {
        final ShareType type = shareTypeFactory.getShareType(sharePermission.getType());
        if (type != null)
        {
            final ShareTypeRenderer shareTypeRenderer = type.getRenderer();
            return shareTypeRenderer.getSimpleDescription(sharePermission, authCtx);
        }
        return null;
    }

    public static class Factory
    {
        private final JiraAuthenticationContext authCtx;
        private final SearchProvider searchProvider;
        private final FavouritesService favouriteService;
        private final PermissionManager permissionManager;
        private final FilterSubscriptionService subscriptionService;
        private final ShareTypeFactory shareTypeFactory;
        private final UserFormatManager userFormatManager;

        public Factory(final JiraAuthenticationContext authCtx, final SearchProvider searchProvider, final FavouritesService favouriteService, final PermissionManager permissionManager, final FilterSubscriptionService subscriptionService, final ShareTypeFactory shareTypeFactory, final UserFormatManager userFormatManager)
        {
            this.authCtx = authCtx;
            this.searchProvider = searchProvider;
            this.favouriteService = favouriteService;
            this.permissionManager = permissionManager;
            this.subscriptionService = subscriptionService;
            this.shareTypeFactory = shareTypeFactory;
            this.userFormatManager = userFormatManager;
        }

        public SearchRequestDisplayBean createDisplayBean(final SearchRequest request)
        {
            return new SearchRequestDisplayBean(authCtx, request, searchProvider, favouriteService, permissionManager, subscriptionService,
                    shareTypeFactory, userFormatManager);
        }

        public List<SearchRequestDisplayBean> createDisplayBeans(final Collection<SearchRequest> requests)
        {
            if ((requests == null) || requests.isEmpty())
            {
                return Collections.emptyList();
            }
            final List<SearchRequestDisplayBean> displayList = new ArrayList<SearchRequestDisplayBean>(requests.size());
            for (final SearchRequest searchRequest : requests)
            {
                displayList.add(createDisplayBean(searchRequest));
            }
            return displayList;
        }
    }
}
