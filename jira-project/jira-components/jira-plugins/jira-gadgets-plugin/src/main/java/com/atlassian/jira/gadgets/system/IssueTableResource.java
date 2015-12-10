package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutException;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.index.SearchUnavailableException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.util.log.OneShotLogger;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;
import com.atlassian.jira.web.component.TableLayoutUtils;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.atlassian.jira.issue.search.util.SearchSortUtil.SORTER_FIELD;
import static com.atlassian.jira.issue.search.util.SearchSortUtil.SORTER_ORDER;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static java.util.Arrays.asList;

/**
 * REST resource to retreive a pre-rendered issue table.
 *
 * @since v4.0
 */
@Path ("/issueTable")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueTableResource extends AbstractResource
{
    private static final Logger log = Logger.getLogger(IssueTableResource.class);
    private static OneShotLogger maxRowsLogger = new OneShotLogger(log);

    private static final String DEFAULT_NUM_TO_SHOW = "5";
    private static final int DEFAULT_MAX_ROWS = 50;

    private static final String REST_CONTEXT_PATH = "/rest/gadget/1.0";
    private static final String FILTER_ID = "filterId";
    private static final String JQL = "jql";
    protected static final String PROJECT_ID = "projectId";
    private static final String FILTER_PREFIX = "filter-";
    private static final String PROJECT_PREFIX = "project-";
    private static final String JQL_PREFIX = "jql-";
    static final String NUM_FIELD = "num";
    private static final String TABLE_CONTEXT = "tableContext";
    static final String COLUMN_NAMES = "columnNames";
    private static final String SORT_BY = "sortBy";
    private static final String PAGING = "paging";
    private static final String START_INDEX = "startIndex";
    private static final String ENABLE_SORTING = "enableSorting";
    private static final String DISPLAY_HEADER = "displayHeader";
    private static final String SHOW_ACTIONS = "showActions";
    private static final String COMPLETED = "completed";
    private static final String TITLE = "title";
    private static final String ADD_DEFAULTS = "addDefault";
    private static final String USE_CONFIGURED_COLS = "useConfiguredCols";

    private final JiraAuthenticationContext authenticationContext;
    private final SearchService searchService;
    private final SearchProvider searchProvider;
    private final TableLayoutUtils tableLayoutUtils;
    private final SearchRequestService searchRequestService;
    private final FieldManager fieldManager;
    private final SearchSortUtil searchSortUtil;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final VersionManager versionManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ApplicationProperties applicationProperties;
    private final ColumnLayoutManager columnLayoutManager;
    private final JqlStringSupport jqlStringSupport;

    public IssueTableResource(JiraAuthenticationContext authenticationContext, SearchService searchService, SearchProvider searchProvider,
            TableLayoutUtils tableLayoutUtils, SearchRequestService searchRequestService, FieldManager fieldManager,
            SearchSortUtil searchSortUtil, ProjectManager projectManager, PermissionManager permissionManager,
            VersionManager versionManager, VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, ColumnLayoutManager columnLayoutManager, JqlStringSupport jqlStringSupport)
    {
        this.authenticationContext = authenticationContext;
        this.searchService = searchService;
        this.searchProvider = searchProvider;
        this.tableLayoutUtils = tableLayoutUtils;
        this.searchRequestService = searchRequestService;
        this.fieldManager = fieldManager;
        this.searchSortUtil = searchSortUtil;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.versionManager = versionManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.applicationProperties = applicationProperties;
        this.columnLayoutManager = columnLayoutManager;
        this.jqlStringSupport = jqlStringSupport;
    }

    /**
     * Validates the input for an issue table based off a filter
     *
     * @param filterId     the filter id of the filter to create the table for
     * @param columnNames  the column names to display (this can be left blank to get the default columns)
     * @param numberToShow the number of issues to display per page
     * @return A response containing and empty 200 response or a 400 containg a {@link
     *         com.atlassian.jira.rest.v1.model.errors.ErrorCollection} containg all validation errors
     */
    @GET
    @Path ("filter/validate")
    public Response validateFilterTable(@QueryParam (FILTER_ID) String filterId,
            @QueryParam (COLUMN_NAMES) List<String> columnNames,
            @QueryParam (NUM_FIELD) String numberToShow)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        getSearchRequestAndValidate(filterId, errors);

        validateColumnNames(columnNames, errors);

        validateNumberToShow(NUM_FIELD, numberToShow, errors);

        return createValidationResponse(errors);
    }

    /**
     * Validates and then creates an issue table based off a filter
     *
     * @param filterId      the filter id of the filter to create the table for
     * @param columnNames   the column names to display (this can be left blank to get the default columns)
     * @param numberToShow  the number of issues to display per page
     * @param request       the current request.
     * @param context       the context which the table is being rendered.  Used for getting default columns, and
     *                      redirects
     * @param sortBy        The extra column to sort by with optional :ASC or :DESC suffix
     * @param isPaging      Is paging enabled
     * @param start         The issue to start from
     * @param enableSorting enable sorting
     * @param displayHeader display the header?
     * @param showActions   show the actions column
     * @param addDefaults   add the default columns to the table
     * @return A response 200 response containing a {@link IssueTable} containg the rendered html and other nice to
     *         haves (counts, urls, titles) or a 400 containg a {@link ErrorCollection} containg all validation errors
     */
    @GET
    @Path ("filter")
    public Response getFilterTable(@Context HttpServletRequest request,
            @QueryParam (TABLE_CONTEXT) String context,
            @QueryParam (FILTER_ID) String filterId,
            @QueryParam (COLUMN_NAMES) List<String> columnNames,
            @QueryParam (SORT_BY) String sortBy,
            @QueryParam (PAGING) @DefaultValue ("false") boolean isPaging,
            @QueryParam (START_INDEX) @DefaultValue ("0") int start,
            @QueryParam (NUM_FIELD) @DefaultValue (DEFAULT_NUM_TO_SHOW) String numberToShow,
            @QueryParam (ENABLE_SORTING) @DefaultValue ("false") boolean enableSorting,
            @QueryParam (DISPLAY_HEADER) @DefaultValue ("true") boolean displayHeader,
            @QueryParam (SHOW_ACTIONS) @DefaultValue ("false") boolean showActions,
            @QueryParam (ADD_DEFAULTS) @DefaultValue ("false") boolean addDefaults)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        final SearchRequest searchRequest = getSearchRequestAndValidate(filterId, errors);

        if (StringUtils.isBlank(numberToShow))
        {
            numberToShow = DEFAULT_NUM_TO_SHOW;
        }

        final int validatedNumberToShow = validateNumberToShow(NUM_FIELD, numberToShow, errors);

        if (!errors.isEmpty())
        {
            return createErrorResponse(errors);
        }
        addOrderByToSearchRequest(searchRequest, sortBy);
        LinkedLabelledQuery linkedLabelledQuery = new LinkedLabelledQuery(searchRequest);
        List<ColumnLayoutItem> columns = getColumns(context, columnNames, addDefaults, false);
        return createResponse(request, linkedLabelledQuery, columns, isPaging, start, validatedNumberToShow, enableSorting, displayHeader, showActions, null);
    }

    @GET
    @Path ("jql/validate")
    public Response validateJql(@QueryParam (NUM_FIELD) String numberToShow,
            @QueryParam (COLUMN_NAMES) List<String> columnNames)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        validateColumnNames(columnNames, errors);

        validateNumberToShow(NUM_FIELD, numberToShow, errors);

        return createValidationResponse(errors);
    }

    @GET
    @Path ("jql")
    public Response getJqlTable(@Context HttpServletRequest request,
            @QueryParam (TABLE_CONTEXT) String context,
            @QueryParam (JQL) String jql,
            @QueryParam (TITLE) String title,
            @QueryParam (COLUMN_NAMES) List<String> columnNames,
            @QueryParam (SORT_BY) String sortBy,
            @QueryParam (PAGING) @DefaultValue ("false") boolean isPaging,
            @QueryParam (START_INDEX) @DefaultValue ("0") int start,
            @QueryParam (NUM_FIELD) @DefaultValue (DEFAULT_NUM_TO_SHOW) String numberToShow,
            @QueryParam (ENABLE_SORTING) @DefaultValue ("false") boolean enableSorting,
            @QueryParam (DISPLAY_HEADER) @DefaultValue ("true") boolean displayHeader,
            @QueryParam (SHOW_ACTIONS) @DefaultValue ("false") boolean showActions,
            @QueryParam (ADD_DEFAULTS) @DefaultValue ("false") boolean addDefaults,
            @QueryParam (USE_CONFIGURED_COLS) @DefaultValue("false") boolean useConfiguredCols)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        final User user = authenticationContext.getLoggedInUser();

        if (StringUtils.isBlank(numberToShow))
        {
            numberToShow = DEFAULT_NUM_TO_SHOW;
        }

        SearchService.ParseResult parseResult = searchService.parseQuery(user, jql);
        if (!parseResult.isValid())
        {
            Set<String> messageErrors = parseResult.getErrors().getErrorMessages();
            for (String messageError : messageErrors)
            {
                errors.add(new ValidationError(JQL, messageError));
            }
            if (errors.isEmpty())
            {
                //shouldn't happen
                errors.add(new ValidationError(JQL, authenticationContext.getI18nHelper().getText("jql.parse.unknown.no.pos")));
            }
            return Response.status(400).entity(ErrorCollection.Builder.newBuilder(errors).build()).cacheControl(NO_CACHE).build();
        }
        SearchRequest sr = new SearchRequest(parseResult.getQuery());

        addOrderByToSearchRequest(sr, sortBy);
        LinkedLabelledQuery query = new LinkedLabelledQuery(sr.getQuery(), title, null, jql);
        query.query = new QueryImpl(query.query.getWhereClause(), query.query.getOrderByClause(), null);

        final int validatedNumberToShow = validateNumberToShow(NUM_FIELD, numberToShow, errors);

        if (!errors.isEmpty())
        {
            return createErrorResponse(errors);
        }

        List<ColumnLayoutItem> columns = getColumns(context, columnNames, addDefaults, useConfiguredCols);

        Map<String, String> columnSortJql = generateColumnSortJql(sr, query, columns);

        return createResponse(request, query, columns, isPaging, start, validatedNumberToShow, enableSorting, displayHeader, showActions, columnSortJql);
    }

    public Map<String, String> generateColumnSortJql(SearchRequest sr, LinkedLabelledQuery query, List<ColumnLayoutItem> columns)
    {
        Map<String, String> columnSortJql = new HashMap<String, String>();

        for (ColumnLayoutItem column : columns)
        {
            final String id = column.getId();
            OrderBy ob = buildOrderBy(sr.getQuery().getOrderByClause(), id);
            Query queryWithOrder = new QueryImpl(query.query.getWhereClause(), ob, null);
            columnSortJql.put(id, jqlStringSupport.generateJqlString(queryWithOrder));
        }
        return columnSortJql;
    }

    private OrderBy buildOrderBy(OrderBy ob, String columnName) {
        List<SearchSort> newSearchSortList = new ArrayList<SearchSort>();
        SortOrder columnDirection = SortOrder.ASC;
        for (SearchSort searchSort : ob.getSearchSorts())
        {
            SearchSort newSearchSort = searchSort;
            if (searchSort.getField().equalsIgnoreCase(columnName)) {
                if (searchSort.getSortOrder() == SortOrder.ASC) {
                    columnDirection = SortOrder.DESC;
                }
            } else {
                newSearchSortList.add(newSearchSort);
            }
        }
        newSearchSortList.add(0, new SearchSort(columnName, columnDirection));
        return new OrderByImpl(newSearchSortList);
    }



    /**
     * Validates the input for an issue table based the next unreleased version for a project
     *
     * @param projectId    the project to get the next unreleased version for and base the table off
     * @param columnNames  the column names to display (this can be left blank to get the default columns)
     * @param numberToShow the number of issues to display per page
     * @return A response containing and empty 200 response or a 400 containg a {@link
     *         com.atlassian.jira.rest.v1.model.errors.ErrorCollection} containg all validation errors
     */
    @GET
    @Path ("iteration/validate")
    public Response validateIterationTable(@QueryParam (PROJECT_ID) String projectId,
            @QueryParam (COLUMN_NAMES) List<String> columnNames,
            @QueryParam (NUM_FIELD) String numberToShow)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        getSearchRequestAndValidateForIteration(projectId, errors, true);
        validateColumnNames(columnNames, errors);
        validateNumberToShow(NUM_FIELD, numberToShow, errors);
        return createValidationResponse(errors);
    }

    /**
     * Validates and then creates an issue table based the next unreleased version for a project
     *
     * @param projectId     the project to get the next unreleased version for and base the table off
     * @param completed     Whether to show completed or uncompleted issues
     * @param columnNames   the column names to display (this can be left blank to get the default columns)
     * @param numberToShow  the number of issues to display per page
     * @param request       the current request.
     * @param context       the context which the table is being rendered.  Used for getting default columns, and
     *                      redirects
     * @param sortBy        The extra column to sort by with optional :ASC or :DESC suffix
     * @param isPaging      Is paging enabled
     * @param start         The issue to start from
     * @param enableSorting enable sorting
     * @param displayHeader display the header?
     * @param showActions   show the actions column
     * @return A response 200 response containing a {@link IssueTable} containg the rendered html and other nice to
     *         haves (counts, urls, titles) or a 400 containg a {@link ErrorCollection} containg all validation errors
     */
    @GET
    @Path ("iteration")
    public Response getIterationTable(@Context HttpServletRequest request,
            @QueryParam (TABLE_CONTEXT) String context,
            @QueryParam (PROJECT_ID) String projectId,
            @QueryParam (COMPLETED) @DefaultValue ("true") boolean completed,
            @QueryParam (COLUMN_NAMES) List<String> columnNames,
            @QueryParam (SORT_BY) String sortBy,
            @QueryParam (PAGING) @DefaultValue ("false") boolean isPaging,
            @QueryParam (START_INDEX) @DefaultValue ("0") int start,
            @QueryParam (NUM_FIELD) @DefaultValue (DEFAULT_NUM_TO_SHOW) String numberToShow,
            @QueryParam (ENABLE_SORTING) @DefaultValue ("false") boolean enableSorting,
            @QueryParam (DISPLAY_HEADER) @DefaultValue ("true") boolean displayHeader,
            @QueryParam (SHOW_ACTIONS) @DefaultValue ("false") boolean showActions)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        final SearchRequest searchRequest = getSearchRequestAndValidateForIteration(projectId, errors, completed);

        final int validatedNumberToShow = validateNumberToShow(NUM_FIELD, numberToShow, errors);

        if (!errors.isEmpty())
        {
            return createErrorResponse(errors);
        }
        addOrderByToSearchRequest(searchRequest, sortBy);
        LinkedLabelledQuery linkedLabelledQuery = new LinkedLabelledQuery(searchRequest);

        List<ColumnLayoutItem> columns = getColumns(context, columnNames, false, false);
        return createResponse(request, linkedLabelledQuery, columns, isPaging, start, validatedNumberToShow, enableSorting, displayHeader, showActions, null);
    }

    void addOrderByToSearchRequest(SearchRequest searchRequest, String sortBy)
    {
        if (StringUtils.isNotBlank(sortBy))
        {
            String sortDirection = null;
            if (sortBy.endsWith(":DESC") || sortBy.endsWith(":ASC"))
            {
                sortDirection = sortBy.substring(sortBy.lastIndexOf(':') + 1);
                sortBy = sortBy.substring(0, sortBy.lastIndexOf(':'));
            }

            final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());

            final String[] sortArray = { sortDirection };
            final String[] fieldArray = { sortBy };

            final MapBuilder<String, String[]> builder = MapBuilder.newBuilder();
            final Map<String, String[]> params = builder.add(SORTER_ORDER, sortArray).add(SORTER_FIELD, fieldArray).toMap();

            final OrderBy newOrder = searchSortUtil.getOrderByClause(params);
            final OrderBy oldOrder = queryBuilder.orderBy().buildOrderBy();

            final User user = authenticationContext.getLoggedInUser();
            final List<SearchSort> newSearchSorts = newOrder.getSearchSorts();
            final List<SearchSort> oldSearchSorts = oldOrder.getSearchSorts();
            final List<SearchSort> sorts = searchSortUtil.mergeSearchSorts(user, newSearchSorts, oldSearchSorts, 3);

            queryBuilder.orderBy().setSorts(sorts);
            searchRequest.setQuery(queryBuilder.buildQuery());
        }
    }

    private String getURLFromSearchRequest(final SearchRequest searchRequest)
    {
        if (searchRequest.isLoaded())
        {
            return "/secure/IssueNavigator.jspa?requestId=" + searchRequest.getId() + "&mode=hide";
        }
        else
        {
            final User user = authenticationContext.getLoggedInUser();
            final Query query = searchRequest.getQuery();
            return "/secure/IssueNavigator.jspa?" + searchService.getQueryString(user, query) + "&mode=hide";
        }
    }

    private IssueTableLayoutBean createLayout(final LinkedLabelledQuery linkedLabelledQuery, final List<ColumnLayoutItem> columns, final boolean enableSorting, final boolean displayHeader, final boolean showActions)
    {
        final OrderBy orderBy = linkedLabelledQuery.query.getOrderByClause();
        final IssueTableLayoutBean layout = new IssueTableLayoutBean(columns, orderBy.getSearchSorts());
        layout.setSortingEnabled(enableSorting);
        layout.setDisplayHeader(displayHeader);
        layout.setShowExteriorTable(false);
        layout.setShowActionColumn(showActions);
        layout.setTableCssClass("grid issuetable-db maxWidth");
        layout.setDisplayHeaderPager(false);
        return layout;
    }

    private IssueTable createIssueTable(final SearchResults results, final LinkedLabelledQuery linkedLabelledQuery, final IssueTableLayoutBean layout, final boolean isPaging, final int numberToShow, final Map<String, String> columnSortJql)
    {// only get the table html if there are search results.
        final String html = results.getIssues().isEmpty() ? null : new IssueTableWebComponent().getHtml(layout, results.getIssues(), isPaging ? results : null);
        final String url = linkedLabelledQuery.link;
        final String name = linkedLabelledQuery.title;
        final String description = linkedLabelledQuery.description;

        return new IssueTable(html, results.getIssues().size(), results.getTotal(), results.getNiceStart(), results.getEnd(), 0, numberToShow, url, name, description, columnSortJql);
    }

    Response createResponse(HttpServletRequest request, LinkedLabelledQuery linkedLabelledQuery, List<ColumnLayoutItem> columns, boolean isPaging, int start, int numberToShow, boolean enableSorting, boolean displayHeader, boolean showActions, Map<String, String> columnSortJql)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        User user = authenticationContext.getLoggedInUser();
        if (columns == null || columns.isEmpty())
        {
            errors.add(new ValidationError(COLUMN_NAMES, "gadget.issuetable.common.no.columns"));
            return createErrorResponse(errors);
        }

        final IssueTableLayoutBean layout = createLayout(linkedLabelledQuery, columns, enableSorting, displayHeader, showActions);

        final PagerFilter pagerFilter = new PagerFilter(numberToShow);
        pagerFilter.setStart(start);
        try
        {

            final SearchResults results = searchProvider.search(linkedLabelledQuery.query, user, pagerFilter);
            String baseUrl = getBaseUrl(request);

            velocityRequestContextFactory.setVelocityRequestContext(baseUrl, request);
            final IssueTable table = createIssueTable(results, linkedLabelledQuery, layout, isPaging, numberToShow, columnSortJql);

            return Response.ok(table).cacheControl(NO_CACHE).build();
        }
        catch (SearchUnavailableException e)
        {
            if (!e.isIndexingEnabled())
            {
                final String message = createIndexingUnavailableMessage();
                return createIndexingUnavailableResponse(message);
            }
            else
            {
                throw e;
            }
        }
        catch (SearchException e)
        {
            log.error("Exception thrown while running search for issue table gadget", e);
            throw new RuntimeException(e);
        }
    }

    private String createIndexingUnavailableMessage()
    {
        final String msg1 = authenticationContext.getI18nHelper().getText("gadget.common.indexing");
        String msg2;
        if (permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser()))
        {
            String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
            msg2 = authenticationContext.getI18nHelper().getText("gadget.common.indexing.configure",
                    "<a href=\"" + baseUrl + "/secure/admin/jira/IndexAdmin.jspa\">", "</a>");
        }
        else
        {
            msg2 = authenticationContext.getI18nHelper().getText("gadget.common.indexing.admin");
        }
        return msg1 + " " + msg2;
    }

    private List<ColumnLayoutItem> getColumns(String context, List<String> columnNames, boolean addDefaults, boolean useConfiguredCols)
    {
        User user = authenticationContext.getLoggedInUser();
        try
        {

            if (useConfiguredCols)
            {
                final ColumnLayout columnLayout = columnLayoutManager.getColumnLayout(user);
                return columnLayout.getAllVisibleColumnLayoutItems(user);
            }

            List<ColumnLayoutItem> list = tableLayoutUtils.getColumns(user, context, columnNames, addDefaults);
            if (columnNames != null && columnNames.size() == 1 && list.size() != 1) {
                // HACKETY HACK!! Workaround for a igoogle's container. It sends get requests with column names
                // specified as a comma-separated list instead of multiple params. This results in a single element
                // in columnNames. So if we are here we probably fell back to default cols, try again splitting on comma
                // but only if this would yield multiple column names
                columnNames = asList(columnNames.get(0).split(","));
                if (columnNames.size() > 1)
                {
                    // looks like we've got a google workaround opportunity
                    list = tableLayoutUtils.getColumns(user, context, columnNames, addDefaults);
                }
            }
            return list;
        }
        catch (FieldException e)
        {
            log.error("Exception thrown while retreiving fields for issue table gadget", e);
            throw new RuntimeException(e);
        }
        catch (ColumnLayoutException e)
        {
            log.error("Exception thrown while retreiving fields for issue table gadget", e);
            throw new RuntimeException(e);
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error("Exception thrown while retreiving fields for issue table gadget", e);
            throw new RuntimeException(e);
        }
    }

    private String getBaseUrl(final HttpServletRequest request)
    {// we do not want the context path being added to the image map
        String baseUrl = JiraUrl.constructBaseUrl(request);

        // this has the rest url at the end of it.  Easy to remove
        if (baseUrl.endsWith(REST_CONTEXT_PATH))
        {
            baseUrl = baseUrl.substring(0, baseUrl.length() - REST_CONTEXT_PATH.length());
        }
        return baseUrl;
    }

    private SearchRequest getSearchRequestAndValidate(String filterParam, Collection<ValidationError> errors)
    {
        final User user = authenticationContext.getLoggedInUser();
        SearchRequest searchRequest = null;
        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(user);
        if (StringUtils.isBlank(filterParam))
        {
            errors.add(new ValidationError(FILTER_ID, "gadget.common.no.filter.id"));
        }
        else
        {
            Long filterId;
            // if the filter id starts with "jql-" then we are using this gadget in "Preview Mode" for the Charts Popup
            // in IssueNav; chop off the prefix and validate the JQL query
            if (filterParam.startsWith(JQL_PREFIX))
            {
                final SearchService.ParseResult parseResult = searchService.parseQuery(user, filterParam.substring(JQL_PREFIX.length()));
                if (parseResult.isValid())
                {
                    searchRequest = new SearchRequest(parseResult.getQuery());
                }
                else
                {
                    for (String errorMessage : parseResult.getErrors().getErrorMessages())
                    {
                        errors.add(new ValidationError(FILTER_ID, "gadget.common.invalid.filter.validationfailed", CollectionBuilder.newBuilder(filterParam, errorMessage).asList()));
                    }
                }
                filterId = null;
            }
            else
            {
                filterId = stripFilterPrefix(filterParam, FILTER_PREFIX);
                searchRequest = searchRequestService.getFilter(serviceContext, filterId);
            }

            if (searchRequest == null)
            {
                errors.add(new ValidationError(FILTER_ID, "gadget.common.invalid.filter.id", filterParam));
            }
            else
            {
                final MessageSet messageSet = searchService.validateQuery(user, searchRequest.getQuery(), filterId);
                if (messageSet.hasAnyErrors())
                {
                    for (String errorMessage : messageSet.getErrorMessages())
                    {
                        errors.add(new ValidationError(FILTER_ID, "gadget.common.invalid.filter.validationfailed", CollectionBuilder.newBuilder(filterParam, errorMessage).asList()));
                    }
                }
            }
        }
        return searchRequest;
    }

    Long stripFilterPrefix(String filterId, String prefix)
    {
        if (filterId.startsWith(prefix))
        {
            final String numPart = filterId.substring(prefix.length());
            return Long.valueOf(numPart);
        }
        else
        {
            return Long.valueOf(filterId);
        }
    }

    private SearchRequest getSearchRequestAndValidateForIteration(String projectId, Collection<ValidationError> errors, final boolean completed)
    {
        final ApplicationUser user = authenticationContext.getUser();
        if (StringUtils.isBlank(projectId))
        {
            errors.add(new ValidationError(PROJECT_ID, "gadget.common.project.none.selected"));
            return null;
        }

        final Project project = projectManager.getProjectObj(stripFilterPrefix(projectId, PROJECT_PREFIX));
        if (project == null || !permissionManager.hasPermission(Permissions.BROWSE, project, user))
        {
            errors.add(new ValidationError(PROJECT_ID, "gadget.common.invalid.project"));
            return null;
        }

        final Collection<Version> versions = versionManager.getVersionsUnreleased(project.getId(), false);
        if (versions.isEmpty())
        {
            errors.add(new ValidationError(PROJECT_ID, "gadget.issuetable.common.no.unreleased.versions", project.getName()));
            return null;
        }
        else
        {
            final Version version = versions.iterator().next();

            final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().project(project.getId()).and().fixVersion(version.getId());
            if (completed)
            {
                builder.and().resolution().isNotEmpty();
            }
            else
            {
                builder.and().resolution().isEmpty();
            }
            final SearchRequest searchRequest = new SearchRequest(builder.buildQuery());
            searchRequest.setName(project.getName() + " " + version.getName());
            return searchRequest;
        }
    }

    /**
     * Validates a field that contains the number of results or items to show.  Ensures the value is a number and
     * between 0 and max rows
     *
     * @param fieldName    The field to log errors against
     * @param numberToShow The number to show value
     * @param errors       The errors collection to add to
     * @return The number to show or -1 if not a number
     */
    private int validateNumberToShow(String fieldName, String numberToShow, Collection<ValidationError> errors)
    {
        try
        {
            final int maxRows = getMaxRows();

            final int validatedNum = Integer.valueOf(numberToShow);
            if (validatedNum <= 0)
            {
                errors.add(new ValidationError(fieldName, "gadget.common.num.negative"));
            }
            else if (validatedNum > maxRows)
            {
                errors.add(new ValidationError(fieldName, "gadget.common.num.overlimit", "" + maxRows));
            }
            return validatedNum;
        }
        catch (NumberFormatException e)
        {
            errors.add(new ValidationError(fieldName, "gadget.common.num.nan"));
        }

        return -1;
    }

    private int getMaxRows()
    {
        try
        {
            if (applicationProperties != null)
            {
                final String maxRowsStr = applicationProperties.getDefaultBackedString("jira.table.gadget.max.rows");
                if (StringUtils.isNotBlank(maxRowsStr))
                {
                    return Integer.valueOf(maxRowsStr);
                }
                else
                {
                    final String logMessage = "'jira.table.gadget.max.rows' doesn't exist in jira-application.properties";
                    maxRowsLogger.warn(logMessage);
                    log.info(logMessage);
                }
            }
        }
        catch (NumberFormatException e)
        {
            log.warn("'jira.table.gadget.max.rows' contains something thats not a number!", e);
        }

        return DEFAULT_MAX_ROWS;
    }

    void validateColumnNames(List<String> columnNames, Collection<ValidationError> errors)
    {
        if (columnNames != null && !columnNames.isEmpty())
        {
            try
            {
                List<String> fieldsNotFound = new ArrayList<String>();
                final User user = authenticationContext.getLoggedInUser();
                final Set<NavigableField> availableFields = fieldManager.getAvailableNavigableFields(user);
                for (String columnName : columnNames)
                {
                    // skip the --Default-- column if present
                    if (IssueTableLayoutBean.DEFAULT_COLUMNS.equalsIgnoreCase(columnName))
                    {
                        continue;
                    }

                    boolean found = false;
                    for (NavigableField availableField : availableFields)
                    {
                        if (columnName.equals(availableField.getId()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        fieldsNotFound.add(columnName);
                    }
                }
                if (!fieldsNotFound.isEmpty())
                {
                    String fieldsNotFoundString = StringUtils.join(fieldsNotFound, ", ");
                    errors.add(new ValidationError(COLUMN_NAMES, "gadget.issuetable.common.cols.not.found", fieldsNotFoundString));
                }
            }
            catch (FieldException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    class LinkedLabelledQuery
    {
        private Query query;
        private String title;
        private String link;
        private String description;

        private LinkedLabelledQuery(final Query query, final String title, final String link, String description)
        {
            this.query = query;
            this.title = title;
            this.link = link;
            this.description = description;
        }

        private LinkedLabelledQuery(SearchRequest searchRequest)
        {
            this(searchRequest.getQuery(), searchRequest.getName(), getURLFromSearchRequest(searchRequest), searchRequest.getDescription());
        }
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class IssueTable
    {
        @XmlElement
        private String table;
        @XmlElement
        private int displayed;
        @XmlElement
        private int startIndex;
        @XmlElement
        private int total;
        @XmlElement
        private int end;
        @XmlElement
        private int page;
        @XmlElement
        private int pageSize;
        @XmlElement
        private String url;
        @XmlElement
        private String title;
        @XmlElement
        private String description;
        @XmlElement
        private Map<String, String> columnSortJql;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private IssueTable()
        {
        }

        IssueTable(String table, int displayed, int total, int startIndex, int end, int page, int pageSize, String url, String title, String description, Map<String, String> columnSortJql)
        {
            this.table = table;
            this.displayed = displayed;
            this.total = total;
            this.startIndex = startIndex;
            this.end = end;
            this.page = page;
            this.pageSize = pageSize;
            this.url = url;
            this.title = title;
            this.description = description;
            this.columnSortJql = columnSortJql;
        }
    }
    ///CLOVER:ON
}
