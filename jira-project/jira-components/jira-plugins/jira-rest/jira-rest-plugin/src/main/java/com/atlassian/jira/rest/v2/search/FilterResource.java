package com.atlassian.jira.rest.v2.search;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.fields.ColumnService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.column.EditableSearchRequestColumnLayoutImpl;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserSharingPreferencesUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.expand.EntityCrawler;
import com.atlassian.plugins.rest.common.expand.parameter.DefaultExpandParameter;
import com.atlassian.plugins.rest.common.expand.parameter.ExpandParameter;
import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;
import com.atlassian.plugins.rest.common.expand.resolver.ListWrapperEntityExpanderResolver;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.atlassian.jira.rest.v2.search.ColumnOptions.toColumnOptions;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.Response.noContent;
import static javax.ws.rs.core.Response.ok;

/**
 * Resource for searches.
 *
 * @since v5.0
 */
@Path ("filter")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class FilterResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final SearchRequestService searchRequestService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final FavouritesService favouritesService;
    private final SearchService searchService;
    private final BeanBuilderFactory beanBuilderFactory;
    private final UserSharingPreferencesUtil userSharingPreferencesUtil;
    private final PermissionManager permissionsManager;
    private final UserPreferencesManager userPreferencesManager;
    private final ColumnLayoutManager columnLayoutManager;
    private final ColumnService columnService;

    private final SharePermissions sharePermissions = SharePermissions.PRIVATE;

    public FilterResource(final JiraAuthenticationContext authenticationContext,
            final SearchRequestService searchRequestService,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final FavouritesService favouritesService,
            final SearchService searchService, BeanBuilderFactory beanBuilderFactory, UserSharingPreferencesUtil userSharingPreferencesUtil, PermissionManager permissionsManager,
            final UserPreferencesManager userPreferencesManager,
            final ColumnLayoutManager columnLayoutManager,
            final ColumnService columnService)
    {
        this.authenticationContext = authenticationContext;
        this.searchRequestService = searchRequestService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.favouritesService = favouritesService;
        this.searchService = searchService;
        this.beanBuilderFactory = beanBuilderFactory;
        this.userSharingPreferencesUtil = userSharingPreferencesUtil;
        this.permissionsManager = permissionsManager;
        this.userPreferencesManager = userPreferencesManager;
        this.columnLayoutManager = columnLayoutManager;
        this.columnService = columnService;
    }

    /**
     * Returns a filter given an id
     *
     * @param id the id of the filter being looked up
     * @param uriInfo info needed to construct URLs.
     * @param expand the parameters to expand
     * @return a {@link FilterBean}
     *
     * @response.representation.200.qname
     *      filter
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of a filter
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.search.FilterBean#DOC_EXAMPLE_1}
     *
     * @response.representation.400.doc
     *     Returned if there is a problem looking up the filter given the id
     */
    @Path ("{id}")
    @GET
    public FilterBean getFilter(@PathParam ("id") Long id, @Context UriInfo uriInfo, @QueryParam("expand") StringList expand)
    {
        final ApplicationUser user = authenticationContext.getUser();
        final JiraServiceContextImpl context = new JiraServiceContextImpl(user);
        final SearchRequest filter = searchRequestService.getFilter(context, id);
        if (filter == null)
        {
            throw new RESTException(BAD_REQUEST, ErrorCollection.of(context.getErrorCollection()));
        }
        return new SearchRequestToFilterBean(uriInfo, expand).apply(filter);
    }

    /**
     * Creates a new filter, and returns newly created filter.
     * Currently sets permissions just using the users default sharing permissions
     *
     * @param bean  the filter being created
     * @param uriInfo info needed to construct URLs.
     * @param expand the parameters to expand
     * @return a {@link FilterBean}
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.search.FilterBean#DOC_EXAMPLE_REQUEST}
     *
     * @response.representation.200.qname
     *      filter
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of a filter
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.search.FilterBean#DOC_EXAMPLE_1}
     *
     * @response.representation.400.doc
     *     Returned if the input is invalid (e.g. filter name was not provided).
     */
    @POST
    public FilterBean createFilter(final FilterBean bean, @Context UriInfo uriInfo, @QueryParam("expand") StringList expand)
    {
        final User user = authenticationContext.getLoggedInUser();
        final I18nHelper i18nHelper = authenticationContext.getI18nHelper();
        final JiraServiceContextImpl context = new JiraServiceContextImpl(user);
        if (user == null)
        {
            context.getErrorCollection().addErrorMessage(i18nHelper.getText("admin.errors.filters.no.user"));
            throw new RESTException(UNAUTHORIZED, ErrorCollection.of(context.getErrorCollection()));
        }

        SearchRequest copiedFromFilter = null;
        Query queryToCopy = null;
        if (bean.getId() != null)
        {
            copiedFromFilter = searchRequestService.getFilter(context, Long.parseLong(bean.getId()));
            if (copiedFromFilter == null)
            {
                throw new RESTException(BAD_REQUEST, ErrorCollection.of(context.getErrorCollection()));
            }
            if (bean.getJql() == null)
            {
                queryToCopy = copiedFromFilter.getQuery();
            }
        }
        if (queryToCopy == null)
        {
            SearchService.ParseResult parseResult = parseAndValidateJql(bean.getJql(), null, user, context);
            queryToCopy = parseResult.getQuery();
        }
        SearchRequest newFilter = processFilterBeanForCreate(bean, queryToCopy, context);
        if (newFilter == null)
        {
            throw new RESTException(BAD_REQUEST, ErrorCollection.of(context.getErrorCollection()));
        }
        newFilter = searchRequestService.createFilter(new JiraServiceContextImpl(user), newFilter, bean.isFavourite());
        if (copiedFromFilter != null)
        {
            applyFilterColumnsToFilter(copiedFromFilter, newFilter, context);
        }
        //get the copiedFromFilter object
        return getFilter(newFilter.getId(), uriInfo, expand);
    }

    /**
     * Updates an existing filter, and returns its new value.
     *
     * @param filterId  the id of the filter to update
     * @param bean  the filter being created
     * @param uriInfo info needed to construct URLs.
     * @param expand the parameters to expand
     * @return a {@link FilterBean}
     *
     * @response.representation.200.qname
     *      filter
     *
     * @response.representation.200.mediaType application/json
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.search.FilterBean#DOC_EXAMPLE_REQUEST}
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of a filter
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.search.FilterBean#DOC_EXAMPLE_1}
     *
     * @response.representation.400.doc
     *     Returned if there is a problem updating up the filter of the given id
     */
    @PUT
    @Path ("{id}")
    public FilterBean editFilter(@PathParam ("id") final Long filterId, final FilterBean bean, @Context UriInfo uriInfo, @QueryParam("expand") StringList expand)
    {
        final User user = authenticationContext.getLoggedInUser();
        final I18nHelper i18nHelper = authenticationContext.getI18nHelper();

        if (user == null)
        {
            throw new RESTException(UNAUTHORIZED, ErrorCollection.of(i18nHelper.getText("admin.errors.filters.no.user")));
        }

        final JiraServiceContextImpl context = new JiraServiceContextImpl(user);
        final SearchRequest searchRequest = searchRequestService.getFilter(context, filterId);

        if (bean.getJql() != null) {
            final SearchService.ParseResult parseResult = parseAndValidateJql(bean.getJql(), filterId, user, context);

            if (parseResult.isValid())
            {
                searchRequest.setQuery(parseResult.getQuery());
            }
        }

        if (bean.getName() != null) {
            searchRequest.setName(bean.getName());
        }

        if (bean.getDescription() != null) {
            searchRequest.setDescription(bean.getDescription());
        }

        searchRequestService.validateFilterForUpdate(context, searchRequest);

        if (context.getErrorCollection().hasAnyErrors())
        {
            throw new RESTException(ErrorCollection.of(context.getErrorCollection()));
        }

        searchRequestService.updateFilter(context, searchRequest);

        //return the persisted filter object
        return getFilter(searchRequest.getId(), uriInfo, expand);
    }

    /**
     * Delete a filter.
     *
     * @param id The ID of the filter to delete.
     *
     * @return a 204 HTTP status if everything goes well
     *
     * @response.representation.204.doc
     *      Returned if the filter was removed successfully.
     *
     * @response.representation.400.doc
     *      Returned if an error occurs.
     *
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     */
    @DELETE
    @Path ("{id}")
    public Response deleteFilter(@PathParam("id") Long id)
    {
        final User user = authenticationContext.getLoggedInUser();
        final JiraServiceContextImpl context = new JiraServiceContextImpl(user);
        searchRequestService.deleteFilter(context, id);

        if (context.getErrorCollection().hasAnyErrors()) {
            throw new RESTException(ErrorCollection.of(context.getErrorCollection()));
        } else {
            return noContent().cacheControl(never()).build();
        }
    }

    private void applyFilterColumnsToFilter(SearchRequest filter, SearchRequest newFilter, JiraServiceContext context)
    {
        final User user = authenticationContext.getLoggedInUser();
        if (filter != null)
        {
            try
            {
                ColumnLayout filterColumnLayout = columnLayoutManager.getColumnLayout(user, filter);
                if (filterColumnLayout != null)
                {
                    columnLayoutManager.storeEditableSearchRequestColumnLayout(new EditableSearchRequestColumnLayoutImpl(filterColumnLayout.getColumnLayoutItems(), user, newFilter));
                }
            }
            catch (ColumnLayoutStorageException e)
            {
                throw new RuntimeException("Failed to store column layout for filter [" + newFilter.getId() + "]", e);
            }
        }
    }

    private SearchRequest processFilterBeanForCreate(FilterBean bean, Query query, JiraServiceContext context) {
        //create search request
        final User user = authenticationContext.getLoggedInUser();
        final I18nHelper i18nHelper = authenticationContext.getI18nHelper();
        SearchRequest request =new SearchRequest(query);

        if (StringUtils.isBlank(bean.getName())) {
            context.getErrorCollection().addError("filterName", i18nHelper.getText("saveasfilter.specify.name"));
        }
        //we are all good, full steam ahead
        request.setName(bean.getName());
        //set owner
        request.setOwnerUserName(user.getName());
        request.setDescription(bean.getDescription());
        //permissions
        request.setPermissions(SharePermissions.PRIVATE);
        if (isEditEnabled())
        {
            request.setPermissions(userSharingPreferencesUtil.getDefaultSharePermissions(user));
        }

        searchRequestService.validateFilterForCreate(context, request);

        // Throw any errors
        if (context.getErrorCollection().hasAnyErrors()) {
            throw new RESTException(BAD_REQUEST, ErrorCollection.of(context.getErrorCollection()));
        }
        return request;
    }

    private SearchService.ParseResult parseAndValidateJql(String jql, Long filterId, User user, JiraServiceContext context) {
        //parse query
        SearchService.ParseResult parseResult = searchService.parseQuery(user, jql);
        if (!parseResult.isValid())
        {
            context.getErrorCollection().addErrorMessages(parseResult.getErrors().getErrorMessages());
        }
        //validate Query
        else {
            MessageSet validationResults = searchService.validateQuery(user, parseResult.getQuery(), filterId);
            if (validationResults.hasAnyErrors())
            {
                context.getErrorCollection().addErrorMessages(validationResults.getErrorMessages());
            }
        }
        return parseResult;
    }

    /**
     * Returns the favourite filters of the logged-in user.
     *
     * @param uriInfo info needed to construct URLs.
     * @param expand the parameters to expand
     * @return a List of {@link FilterBean}
     *
     * @response.representation.200.qname
     *      filter
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of a list of filters
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.search.FilterBean#DOC_FILTER_LIST_EXAMPLE}
     */
    @Path("favourite")
    @GET
    public List<FilterBean> getFavouriteFilters(final @Context UriInfo uriInfo, final @QueryParam("expand") StringList expand)
    {
        final ApplicationUser user = authenticationContext.getUser();
        final Collection<SearchRequest> favouriteFilters = searchRequestService.getFavouriteFilters(user);
        final Iterable<FilterBean> favouriteFilterBeans = Iterables.transform(favouriteFilters, new SearchRequestToFilterBean(uriInfo, expand, true));
        return Lists.newArrayList(favouriteFilterBeans);
    }

    /**
     * Returns the default share scope of the logged-in user.
     *
     * @return a {@link DefaultShareScopeBean}
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns the default share scope of the logged-in user.
     *
     * @response.representation.200.example
     *      {@link DefaultShareScopeBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *     Returned if there is a problem looking up preferences for the logged-in user
     */
    @Path("defaultShareScope")
    @GET
    public DefaultShareScopeBean getDefaultShareScope()
    {
        final User user = authenticationContext.getLoggedInUser();
        final I18nHelper i18nHelper = authenticationContext.getI18nHelper();
        if (user == null)
        {
            throw new RESTException(UNAUTHORIZED, ErrorCollection.of(i18nHelper.getText("admin.errors.filters.no.user")));
        }

        final DefaultShareScopeBean shareScope;
        if (isEditEnabled() && userSharingPreferencesUtil.getDefaultSharePermissions(user).isGlobal())
        {
            shareScope = new DefaultShareScopeBean(DefaultShareScopeBean.Scope.GLOBAL);
        }
        else
        {
            shareScope = new DefaultShareScopeBean(DefaultShareScopeBean.Scope.PRIVATE);
        }
        return shareScope;
    }

    /**
     * Sets the default share scope of the logged-in user. Available values are GLOBAL and PRIVATE.
     *
     * @return a {@link DefaultShareScopeBean}
     *
     * @request.representation.example
     *      {@link DefaultShareScopeBean#DOC_EXAMPLE}
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.example
     *      {@link DefaultShareScopeBean#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returns the new default share scope of the logged-in user.
     *
     * @response.representation.400.doc
     *     Returned if there is a problem setting the preferences for the logged-in user
     */
    @Path("defaultShareScope")
    @PUT
    public DefaultShareScopeBean setDefaultShareScope(final DefaultShareScopeBean shareScope)
    {
        final User user = authenticationContext.getLoggedInUser();
        final I18nHelper i18nHelper = authenticationContext.getI18nHelper();
        if (user == null)
        {
            throw new RESTException(UNAUTHORIZED, ErrorCollection.of(i18nHelper.getText("admin.errors.filters.no.user")));
        }
        final Preferences userPreferences = userPreferencesManager.getPreferences(user);
        try
        {
            boolean defaultToPrivate = DefaultShareScopeBean.Scope.PRIVATE.equals(shareScope.getScope());
            userPreferences.setBoolean(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE, defaultToPrivate);
        }
        catch (AtlassianCoreException e)
        {
            // Shouldn't happen since we checked user already
            throw new RESTException(UNAUTHORIZED, ErrorCollection.of(i18nHelper.getText("admin.errors.filters.no.user")));
        }
        return getDefaultShareScope();
    }

    /**
     * Returns the default columns for the given filter. Currently logged in user will be used as
     * the user making such request.
     *
     * @since v6.1
     *
     * @param filterId id of the filter
     * @return column configuration
     *
     * @response.representation.200.qname
     *      columns
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a list of columns for configured for the given user
     *
     * @response.representation.404.doc
     *      Returned if the filter does not have any columns.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the column configuration.
     */
    @GET
    @Path ("{id}/columns")
    public Response defaultColumns(@PathParam ("id") final Long filterId)
    {
        final ApplicationUser currentUser = authenticationContext.getUser();

        final ServiceOutcome<ColumnLayout> outcome = columnService.getColumnLayout(currentUser, filterId);
        if (outcome.isValid())
        {
            ColumnLayout columnLayout = outcome.getReturnedValue();
            if (columnLayout == null)
            {
                return Response.status(Response.Status.NOT_FOUND).cacheControl(never()).build();
            }

            final List<ColumnLayoutItem> columnLayoutItems = columnLayout.getColumnLayoutItems();
            return ok(toColumnOptions(columnLayoutItems)).cacheControl(never()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    /**
     * Sets the default columns for the given filter.
     *
     * @since v6.1
     *
     * @param filterId id of the filter
     * @param fields list of column ids
     * @return javax.ws.rs.core.Response containing basic message and http return code
     *
     * @response.representation.200.doc
     *      Returned when the columns are saved successfully
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the column configuration.
     */
    @PUT
    @Path ("{id}/columns")
    @Consumes (MediaType.WILDCARD)
    public Response setColumns(@PathParam ("id") final Long filterId,
                               @FormParam ("columns") final List<String> fields)
    {
        final ApplicationUser currentUser = authenticationContext.getUser();

        final ServiceResult outcome = columnService.setColumns(currentUser, filterId, fields);
        if (outcome.isValid())
        {
            return ok().cacheControl(never()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    /**
     * Resets the columns for the given filter such that the filter no longer has its own column config.
     *
     * @since v6.1
     *
     * @param filterId id of the filter
     * @return javax.ws.rs.core.Response containing basic message and http return code
     *
     * @response.representation.204.doc
     *      Returned when the columns are reset/removed successfully
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the column configuration.
     */
    @DELETE
    @Path ("{id}/columns")
    @Consumes (MediaType.WILDCARD)
    public Response resetColumns(@PathParam ("id") final Long filterId)
    {
        final ApplicationUser currentUser = authenticationContext.getUser();

        final ServiceResult outcome = columnService.resetColumns(currentUser, filterId);
        if (outcome.isValid())
        {
            return noContent().cacheControl(never()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    private class SearchRequestToFilterBean implements Function<SearchRequest, FilterBean>
    {
        private final UriInfo uriInfo;
        private final ExpandParameter expand;
        private final Boolean isFavourite;
        private final EntityExpanderResolver expandResolver = new ListWrapperEntityExpanderResolver();
        private final EntityCrawler entityCrawler = new EntityCrawler();

        public SearchRequestToFilterBean(UriInfo uriInfo, StringList expand)
        {
            this(uriInfo, expand, null);
        }

        public SearchRequestToFilterBean(UriInfo uriInfo, StringList expand, Boolean isFavourite)
        {
            this.uriInfo = uriInfo;
            this.expand = new DefaultExpandParameter(expand != null ? expand.asList() : Collections.<String>emptyList());
            this.isFavourite = isFavourite;
        }

        @Override
        public FilterBean apply(SearchRequest filter)
        {
            final ApplicationUser user = authenticationContext.getUser();
            final String canonicalBaseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
            boolean isFavourite = this.isFavourite == null ?
                    user != null && favouritesService.isFavourite(user, filter) :
                    this.isFavourite;
            final FilterBean bean = beanBuilderFactory.newFilterBeanBuilder()
                    .filter(filter)
                    .context(uriInfo, canonicalBaseUrl)
                    .owner(ApplicationUsers.toDirectoryUser(filter.getOwner()))
                    .favourite(isFavourite).build();
            entityCrawler.crawl(bean, expand, expandResolver);
            return bean;
        }
    }

    private SharePermissions getPermissions()
    {
        return sharePermissions;
    }

    private boolean isEditEnabled()
    {
        return permissionsManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, getLoggedInUser());
    }

    private User getLoggedInUser()
    {
        return authenticationContext.getLoggedInUser();
    }

}
