package com.atlassian.jira.bc.filter;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.issue.comparator.FilterNameComparator;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.ShareTypeValidatorUtils;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.action.util.sharing.SharedEntitySearchAction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of SearchRequestService
 *
 * @since v3.13
 */
public class DefaultSearchRequestService implements SearchRequestService
{
    private final SearchRequestManager searchRequestManager;
    private final FavouritesManager<SearchRequest> favouritesManager;
    private final ShareTypeValidatorUtils shareTypeValidatorUtils;
    private final UserUtil userUtil;
    private final PermissionManager permissionManager;

    public DefaultSearchRequestService(final SearchRequestManager searchRequestManager,
            final FavouritesManager<SearchRequest> favouritesManager, final ShareTypeValidatorUtils shareTypeValidatorUtils,
            final UserUtil userUtil, final PermissionManager permissionManager)
    {
        this.searchRequestManager = Assertions.notNull("searchRequestManager", searchRequestManager);
        this.favouritesManager = Assertions.notNull("favouritesManager", favouritesManager);
        this.shareTypeValidatorUtils = Assertions.notNull("shareTypeValidatorUtils", shareTypeValidatorUtils);
        this.userUtil = Assertions.notNull("userUtil", userUtil);
        this.permissionManager = permissionManager;
    }

    @Override
    public Collection<SearchRequest> getFavouriteFilters(final ApplicationUser user)
    {
        final Collection<Long> ids = getFavouriteIds(user);
        final List<SearchRequest> results = new ArrayList<SearchRequest>(ids.size());
        for (final Long id : ids)
        {
            final SearchRequest searchRequest = searchRequestManager.getSearchRequestById(user, id);
            if (searchRequest != null)
            {
                results.add(searchRequest);
            }
        }

        Collections.sort(results, FilterNameComparator.COMPARATOR);

        return results;
    }

    @Override
    public Collection<SearchRequest> getFavouriteFilters(final User user)
    {
        return getFavouriteFilters(ApplicationUsers.from(user));
    }

    @Override
    public Collection<SearchRequest> getOwnedFilters(final ApplicationUser user)
    {
        return DefaultSearchRequestService.sortByName(searchRequestManager.getAllOwnedSearchRequests(user));
    }

    @Override
    public Collection<SearchRequest> getOwnedFilters(final User user)
    {
        return getOwnedFilters(ApplicationUsers.from(user));
    }

    @Override
    public Collection<SearchRequest> getNonPrivateFilters(final ApplicationUser user)
    {
        final Collection<SearchRequest> filters = getOwnedFilters(user);
        CollectionUtils.filter(filters, new Predicate()
        {
            public boolean evaluate(final Object o)
            {
                return !((SearchRequest) o).getPermissions().isPrivate();
            }
        });
        return filters;
    }

    @Override
    public Collection<SearchRequest> getNonPrivateFilters(final User user)
    {
        return getNonPrivateFilters(ApplicationUsers.from(user));
    }

    @Override
    public Collection<SearchRequest> getFiltersFavouritedByOthers(final ApplicationUser user)
    {
        final Collection<SearchRequest> nonPrivateFilters = getNonPrivateFilters(user);
        if (!nonPrivateFilters.isEmpty())
        {
            final Collection<Long> favouriteIds = favouritesManager.getFavouriteIds(user, SearchRequest.ENTITY_TYPE);
            CollectionUtils.filter(nonPrivateFilters, new Predicate()
            {
                @Override
                public boolean evaluate(final Object o)
                {
                    final SearchRequest request = (SearchRequest) o;

                    // Has someone apart from the owner favourited this filter?
                    return (favouriteIds.contains(request.getId())) ? request.getFavouriteCount() > 1 : request.getFavouriteCount() > 0;
                }
            });
        }

        return nonPrivateFilters;
    }

    @Override
    public Collection<SearchRequest> getFiltersFavouritedByOthers(final User user)
    {
        return getFiltersFavouritedByOthers(ApplicationUsers.from(user));
    }

    @Override
    public void deleteFilter(final JiraServiceContext serviceCtx, final Long filterId)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("filterId", filterId);

        final ApplicationUser user = serviceCtx.getLoggedInApplicationUser();

        validateForDelete(serviceCtx, filterId);

        if (!serviceCtx.getErrorCollection().hasAnyErrors())
        {
            // Get the filter, and check that the remote user is the owner of this filter
            final SearchRequest filter = searchRequestManager.getSearchRequestById(user, filterId);
            if (filter != null)
            {
                deleteFilter(filter);
            }
            else
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.cannot.delete.filter"));
            }
        }
    }

    @Override
    public void deleteAllFiltersForUser(final JiraServiceContext serviceCtx, final ApplicationUser user)
    {
        Assertions.notNull("user", user);

        final Collection<SearchRequest> ownedRequests = searchRequestManager.getAllOwnedSearchRequests(user);
        for (final SearchRequest searchRequest : ownedRequests)
        {
            deleteFilter(searchRequest);
        }

        favouritesManager.removeFavouritesForUser(user, SearchRequest.ENTITY_TYPE);
    }

    @Override
    public void deleteAllFiltersForUser(final JiraServiceContext serviceCtx, final User user)
    {
        deleteAllFiltersForUser(serviceCtx, ApplicationUsers.from(user));
    }

    @Override
    public SearchRequest getFilter(final JiraServiceContext serviceCtx, final Long filterId)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("filterId", filterId);

        final SearchRequest filter = searchRequestManager.getSearchRequestById(serviceCtx.getLoggedInApplicationUser(), filterId);
        if (filter == null)
        {
            serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.nonexistent"));
        }

        return filter;
    }

    @Override
    public void validateFilterForUpdate(final JiraServiceContext serviceCtx, final SearchRequest request)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("request", request);

        validateUpdateSearchParameters(serviceCtx, request);

        if (this.validateFilterName(serviceCtx, request))
        {
            if (serviceCtx.getLoggedInUser() != null)
            {
                final SearchRequest filterByName = searchRequestManager.getOwnedSearchRequestByName(serviceCtx.getLoggedInApplicationUser(), request.getName());
                if ((filterByName != null) && ((request.getId() == null) || !request.getId().equals(filterByName.getId())))
                {
                    serviceCtx.getErrorCollection().addError("filterName", serviceCtx.getI18nBean().getText("admin.errors.filters.same.name"));
                }
            }
        }
        shareTypeValidatorUtils.isValidSharePermission(serviceCtx, request);
    }

    @Override
    public boolean validateUpdateSearchParameters(final JiraServiceContext serviceCtx, final SearchRequest request)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("request", request);

        // make sure the request is actually saved.
        if (request.getId() == null)
        {
            serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.not.saved"));
        }

        // check the null user.
        if (serviceCtx.getLoggedInUser() == null)
        {
            serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.owned.anonymous.user"));
        }
        else
        {
            final ApplicationUser owner = searchRequestManager.getSearchRequestOwner(request.getId());
            if (owner == null)
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.not.saved"));
            }
            else if (!owner.equals(serviceCtx.getLoggedInApplicationUser()) || request.getOwner() == null ||
                     !request.getOwner().equals(serviceCtx.getLoggedInApplicationUser()))
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.must.be.owner"));
            }
        }
        return !serviceCtx.getErrorCollection().hasAnyErrors();
    }

    @Override
    public void validateFilterForCreate(final JiraServiceContext serviceCtx, final SearchRequest request)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("request", request);

        // check the null user.
        if (serviceCtx.getLoggedInUser() == null)
        {
            serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.owned.anonymous.user"));
        }
        else
        {
            if ((request.getOwner() == null) || !request.getOwner().equals(serviceCtx.getLoggedInApplicationUser()))
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.must.be.owner"));
            }
        }

        if (this.validateFilterName(serviceCtx, request))
        {
            if (serviceCtx.getLoggedInUser() != null)
            {
                final SearchRequest filterByName = searchRequestManager.getOwnedSearchRequestByName(serviceCtx.getLoggedInApplicationUser(), request.getName());
                if (filterByName != null)
                {
                    serviceCtx.getErrorCollection().addError("filterName", serviceCtx.getI18nBean().getText("admin.errors.filters.same.name"));
                }
            }
        }

        shareTypeValidatorUtils.isValidSharePermission(serviceCtx, request);
    }

    /**
     * Validates the filter name is valid testing for the two following cases, that the name is not missing and its not too long.
     * @param serviceCtx contains the {@link ErrorCollection} to be used to report errors.
     * @param request the request whose filter name should be validated.
     * @return Returns true if the name is valid otherwise returns false if the name is missing or too long etc.
     */
    private boolean validateFilterName(final JiraServiceContext serviceCtx, final SearchRequest request)
    {
        boolean valid = true;

        final String filterName = request.getName();
        if (StringUtils.isBlank(filterName))
        {
            serviceCtx.getErrorCollection().addError("filterName", serviceCtx.getI18nBean().getText("admin.errors.filters.must.specify.name"));
            valid = false;
        }
        else if (filterName.length() > 255)
        {
            serviceCtx.getErrorCollection().addError("filterName", serviceCtx.getI18nBean().getText("admin.errors.filters.name.toolong"));
            valid = false;
        }

        return valid;
    }

    @Override
    public void validateForDelete(final JiraServiceContext serviceCtx, final Long filterId)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("filterId", filterId);

        if (serviceCtx.getLoggedInUser() == null)
        {
            serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.owned.anonymous.user"));
        }
        else
        {
            final ApplicationUser owner = searchRequestManager.getSearchRequestOwner(filterId);
            if (owner == null)
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.not.saved"));
            }
            else if (!owner.equals(serviceCtx.getLoggedInApplicationUser()) &&
                    !permissionManager.hasPermission(Permissions.ADMINISTER, serviceCtx.getLoggedInApplicationUser()))
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.must.be.owner"));
            }
        }
    }

    @Override
    public SearchRequest createFilter(final JiraServiceContext serviceCtx, final SearchRequest request)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("request", request);

        if (checkPermissionsForCreate(serviceCtx, request))
        {
            return searchRequestManager.create(request);
        }
        else
        {
            return null;
        }
    }

    @Override
    public SearchRequest createFilter(final JiraServiceContext serviceCtx, final SearchRequest request, final boolean isFavourite)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("request", request);

        final SearchRequest filter = createFilter(serviceCtx, request);

        if (!serviceCtx.getErrorCollection().hasAnyErrors())
        {
            if (isFavourite)
            {
                try
                {
                    favouritesManager.addFavourite(serviceCtx.getLoggedInApplicationUser(), filter);
                }
                catch (final PermissionException e)
                {
                    serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("common.favourites.not.added"));
                }
            }
            else
            {
                favouritesManager.removeFavourite(serviceCtx.getLoggedInApplicationUser(), filter);
            }
        }
        return filter;
    }

    @Override
    public SearchRequest updateFilter(final JiraServiceContext serviceCtx, final SearchRequest request)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("request", request);

        if (checkPermissionsForUpdate(serviceCtx, request))
        {
            return searchRequestManager.update(request);
        }
        else
        {
            return null;
        }
    }

    @Override
    public SearchRequest updateFilter(final JiraServiceContext serviceCtx, final SearchRequest request, final boolean isFavourite)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("request", request);

        final SearchRequest filter = updateFilter(serviceCtx, request);

        if (!serviceCtx.getErrorCollection().hasAnyErrors())
        {
            if (isFavourite)
            {
                try
                {
                    favouritesManager.addFavourite(serviceCtx.getLoggedInApplicationUser(), filter);
                }
                catch (final PermissionException e)
                {
                    serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("common.favourites.not.added"));
                }
            }
            else
            {
                favouritesManager.removeFavourite(serviceCtx.getLoggedInApplicationUser(), filter);
            }
        }
        return filter;
    }

    public void validateFilterForChangeOwner(final JiraServiceContext serviceCtx, final SearchRequest request)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("request", request);

        if (this.validateFilterName(serviceCtx, request))
        {
            if (serviceCtx.getLoggedInApplicationUser() != null)
            {
                final SearchRequest filterByName = searchRequestManager.getOwnedSearchRequestByName(serviceCtx.getLoggedInApplicationUser(), request.getName());
                if (filterByName != null)
                {
                    serviceCtx.getErrorCollection().addError("filterName", serviceCtx.getI18nBean().getText("admin.errors.filters.already.owns.same.name", serviceCtx.getLoggedInApplicationUser().getDisplayName()));
                }
            }
            if (request.getPermissions().isPrivate())
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.private"));
            }
        }
    }

    @Override
    public SearchRequest updateFilterOwner(JiraServiceContext serviceCtx, ApplicationUser user, SearchRequest request)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("request", request);
        if (checkPermissionsForOwnerUpdate(serviceCtx, user,  request))
        {
            return searchRequestManager.update(request);
        }
        else
        {
            return null;
        }
    }

    @Override
    public SearchRequest updateFilterOwner(JiraServiceContext serviceCtx, User user, SearchRequest request)
    {
        return updateFilterOwner(serviceCtx, ApplicationUsers.from(user), request);
    }

    @Override
    public SearchRequest updateSearchParameters(final JiraServiceContext serviceCtx, final SearchRequest request)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("request", request);

        if (checkPermissionsForUpdateSearchParameters(serviceCtx, request))
        {
            final SearchRequest databaseRequest = searchRequestManager.getSearchRequestById(serviceCtx.getLoggedInApplicationUser(), request.getId());
            if (databaseRequest != null)
            {
                final SearchRequest newRequest = new SearchRequest(request.getQuery(), databaseRequest.getOwner(),
                         databaseRequest.getName(), databaseRequest.getDescription(), databaseRequest.getId(),
                        databaseRequest.getFavouriteCount());
                newRequest.setPermissions(request.getPermissions());

                return searchRequestManager.update(newRequest);
            }
            else
            {
                serviceCtx.getErrorCollection().addErrorMessage(serviceCtx.getI18nBean().getText("admin.errors.filters.not.saved"));
            }
        }
        return null;
    }

    @Override
    public void validateForSearch(final JiraServiceContext serviceCtx, final SharedEntitySearchParameters searchParameters)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("searchParameters", searchParameters);

        final ErrorCollection errorCollection = serviceCtx.getErrorCollection();
        final I18nHelper nBean = serviceCtx.getI18nBean();

        final String searchOwnerUserName = searchParameters.getUserName();
        if (!StringUtils.isBlank(searchOwnerUserName) && permissionManager.hasPermission(Permissions.USER_PICKER, serviceCtx.getLoggedInUser()))
        {
            if (!userUtil.userExists(searchOwnerUserName))
            {
                errorCollection.addError("searchOwnerUserName", nBean.getText("admin.errors.filters.userdoesnotexist", searchOwnerUserName));
            }
        }
        final ShareTypeSearchParameter shareTypeSearchParameter = searchParameters.getShareTypeParameter();
        if (shareTypeSearchParameter != null)
        {
            shareTypeValidatorUtils.isValidSearchParameter(serviceCtx, shareTypeSearchParameter);
        }
        SharedEntitySearchAction.QueryValidator.validate(searchParameters, errorCollection, serviceCtx.getI18nBean());
    }

    @Override
    public SharedEntitySearchResult<SearchRequest> search(final JiraServiceContext serviceCtx, final SharedEntitySearchParameters searchParameters, final int pagePosition, final int pageWidth)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("searchParameters", searchParameters);

        if (pagePosition < 0)
        {
            throw new IllegalArgumentException("pagePosition < 0");
        }
        if (pageWidth <= 0)
        {
            throw new IllegalArgumentException("pageWidth <= 0");
        }

        return searchRequestManager.search(searchParameters, serviceCtx.getLoggedInApplicationUser(), pagePosition, pageWidth);
    }

    private static Collection<SearchRequest> sortByName(final Collection<SearchRequest> filters)
    {
        if (filters.isEmpty())
        {
            return filters;
        }
        else
        {
            final List<SearchRequest> filtersList = new ArrayList<SearchRequest>(filters);
            Collections.sort(filtersList, FilterNameComparator.COMPARATOR);

            return filtersList;
        }
    }

    private Collection<Long> getFavouriteIds(final ApplicationUser user)
    {
        return user == null ? Collections.<Long>emptyList() : favouritesManager.getFavouriteIds(user, SearchRequest.ENTITY_TYPE);
    }

    private void deleteFilter(final SearchRequest filter)
    {
        Assertions.notNull("filter", filter);

        favouritesManager.removeFavouritesForEntityDelete(filter);
        searchRequestManager.delete(filter.getId());
    }

    private boolean checkPermissionsForUpdate(final JiraServiceContext ctx, final SearchRequest request)
    {
        checkPermissionsForUpdateSearchParameters(ctx, request);
        shareTypeValidatorUtils.isValidSharePermission(ctx, request);

        return !ctx.getErrorCollection().hasAnyErrors();
    }

    private boolean checkPermissionsForOwnerUpdate(JiraServiceContext ctx, ApplicationUser user, SearchRequest request)
    {
        checkPermissionsForOwnerUpdateSearchParameters(ctx, user, request);
        shareTypeValidatorUtils.isValidSharePermission(ctx, request);

        return !ctx.getErrorCollection().hasAnyErrors();
    }

    private boolean checkPermissionsForUpdateSearchParameters(final JiraServiceContext ctx, final SearchRequest request)
    {
        if (request.getId() == null)
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("admin.errors.filters.not.saved"));
        }
        else if (ctx.getLoggedInUser() == null)
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("admin.errors.filters.owned.anonymous.user"));
        }
        else if ((request.getOwner() == null) || !request.getOwner().equals(ctx.getLoggedInApplicationUser()))
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("admin.errors.filters.must.be.owner"));
        }
        else
        {
            final ApplicationUser currentOwner = searchRequestManager.getSearchRequestOwner(request.getId());
            if (currentOwner == null)
            {
                ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("admin.errors.filters.not.saved"));
            }
            else if (!currentOwner.equals(ctx.getLoggedInApplicationUser()))
            {
                ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("admin.errors.filters.must.be.owner"));
            }
        }

        return !ctx.getErrorCollection().hasAnyErrors();
    }

    private boolean checkPermissionsForOwnerUpdateSearchParameters(final JiraServiceContext ctx, ApplicationUser user, final SearchRequest request)
    {
        if (request.getId() == null)
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("admin.errors.filters.not.saved"));
        }
        else if (ctx.getLoggedInUser() == null)
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("admin.errors.filters.owned.anonymous.user"));
        }
        else if ((request.getOwner() == null) || !permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("admin.errors.filters.must.be.admin"));
        }
        return !ctx.getErrorCollection().hasAnyErrors();
    }

    private boolean checkPermissionsForCreate(final JiraServiceContext ctx, final SearchRequest request)
    {
        if (ctx.getLoggedInUser() == null)
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("admin.errors.filters.owned.anonymous.user"));
        }
        else
        if ((request.getOwner() == null) || !request.getOwner().equals(ctx.getLoggedInApplicationUser()))
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("admin.errors.filters.must.be.owner"));
        }

        shareTypeValidatorUtils.isValidSharePermission(ctx, request);

        return !ctx.getErrorCollection().hasAnyErrors();
    }
}
