package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.index.SearchUnavailableException;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TwoDimensionalStatsMap;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.issue.statistics.util.TwoDimensionalTermHitCollector;
import com.atlassian.jira.issue.statistics.util.ObjectToFieldValueMapper;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST endpoint to validate and retreive a two dimensional stats resource.
 *
 * @since v4.0
 */
@Path ("/twodimensionalfilterstats")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class TwoDimensionalStatsResource extends SearchQueryBackedResource
{
    private static final Logger log = Logger.getLogger(TwoDimensionalStatsResource.class);
    

    private static final int DEFAULT_MAX_ROWS = 50;
    private static final String FILTER_ID = "filterId";
    private static final String X_STAT_TYPE = "xstattype";
    private static final String Y_STAT_TYPE = "ystattype";
    private static final String SORT_BY = "sortBy";
    private static final String SORT_DIRECTION = "sortDirection";
    private static final String SHOW_TOTALS = "showTotals";
    private static final String NUMBER_TO_SHOW = "numberToShow";
    private static final String REQUEST_ID = "requestId";

    private StatisticTypesResource statisticTypesResource;
    private SearchProvider searchProvider;
    private FilterStatisticsValuesGenerator generator;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private IssueIndexManager issueIndexManager;
    private final FieldValueToDisplayTransformer<StatsMarkup> fieldValueToDisplayTransformer;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final FieldManager fieldManager;
    private final ApplicationProperties applicationProperties;
    private final ReaderCache readerCache;

    public TwoDimensionalStatsResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext,
            final SearchService searchService, final PermissionManager permissionManager,
            final StatisticTypesResource statisticTypesResource, final SearchProvider searchProvider,
            final VelocityRequestContextFactory velocityRequestContextFactory, final IssueIndexManager issueIndexManager,
            final FieldValueToDisplayTransformer<StatsMarkup> fieldValueToDisplayTransformer,
            final FieldManager fieldManager, final FieldVisibilityManager fieldVisibilityManager, final ApplicationProperties applicationProperties,
            final ReaderCache readerCache)
    {
        super(chartUtils, authenticationContext, searchService, permissionManager, velocityRequestContextFactory);
        this.statisticTypesResource = statisticTypesResource;
        this.searchProvider = searchProvider;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.issueIndexManager = issueIndexManager;
        this.fieldValueToDisplayTransformer = fieldValueToDisplayTransformer;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.fieldManager = fieldManager;
        this.applicationProperties = applicationProperties;
        this.readerCache = readerCache;
    }

    @GET
    @Path ("validate")
    /**
     * Validation for the parameters to this resource
     *
     * @param queryString a filter id (starts with "filter-", or just the number)
     * @param xStatType         The stat to group by on the x axis
     * @param yStatType         The stat to group by on the y axis
     */
    public Response validate(@QueryParam (FILTER_ID) String queryString,
            @QueryParam (X_STAT_TYPE) final String xStatType,
            @QueryParam (Y_STAT_TYPE) final String yStatType,
            @QueryParam (NUMBER_TO_SHOW) final String numberToShow)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(queryString) && !queryString.contains("-"))
        {
            try
            {
                Long.parseLong(queryString);
                queryString = "filter-" + queryString;
            }
            catch (NumberFormatException ex)
            {
                // treat as an empty query string
            }
        }

        final Collection<ValidationError> unfilteredErrors = new ArrayList<ValidationError>();
        getSearchRequestAndValidate(queryString, unfilteredErrors, params);

        // We have to rewrite errors for the 'filterOrProjectId' field to our 'filterId'
        for (ValidationError err : unfilteredErrors)
        {
            errors.add(new ValidationError(QUERY_STRING.equals(err.getField()) ? "filterId" : err.getField(), err.getError(), err.getParams()));
        }

        validateStatType(errors, xStatType, X_STAT_TYPE);
        validateStatType(errors, yStatType, Y_STAT_TYPE);
        validateNumberToShow("numberToShow", numberToShow, errors);
        return createValidationResponse(errors);
    }

    private FilterStatisticsValuesGenerator getGenerator()
    {
        if (generator == null)
        {
            generator = new FilterStatisticsValuesGenerator();
        }
        return generator;
    }

    void setGenerator(FilterStatisticsValuesGenerator generator)
    {
        this.generator = generator;
    }

    /**
     * Generate a two dimensional statistics view of a filter
     *
     * @param queryString     a filter id (starts with "filter-", or just the number)
     * @param xStatType       The stat to group by on the x axis
     * @param yStatType       The stat to group by on the y axis
     * @param sortBy          The field with which to order the stats
     * @param showTotals      Include additional rows / columns that include the total number of issue for x / y axis
     *                        and totals
     * @param sortDirection   Ascending or descending sort
     * @param numberToShowStr Maximum number of results to display on the y-axis. 0 represents unlimited.
     * @return a {@link com.atlassian.jira.gadgets.system.TwoDimensionalStatsResource.TwoDimensionalProperties} if all
     *         params validate else a Collection of {@link com.atlassian.jira.rest.v1.model.errors.ValidationError}
     */
    @GET
    @Path ("/generate")
    public Response getStats(@QueryParam (FILTER_ID) String queryString,
            @QueryParam (X_STAT_TYPE) @DefaultValue ("assignees") final String xStatType,
            @QueryParam (Y_STAT_TYPE) @DefaultValue ("assignees") final String yStatType,
            @QueryParam (SORT_DIRECTION) @DefaultValue ("asc") final String sortDirection,
            @QueryParam (SORT_BY) @DefaultValue ("natural") final String sortBy,
            @QueryParam (SHOW_TOTALS) @DefaultValue ("false") final boolean showTotals,
            @QueryParam (NUMBER_TO_SHOW) @DefaultValue ("5") String numberToShowStr
    )
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        final SearchRequest searchRequest = getSearchRequestAndValidate(queryString, errors, params);

        if (StringUtils.isBlank(numberToShowStr))
        {
            numberToShowStr = "5";
        }

        final int numberToShow = validateNumberToShow("numberToShow", numberToShowStr, errors);

        if (!errors.isEmpty())
        {
            return createErrorResponse(errors);
        }

        try
        {
            final TwoDimensionalProperties properties = buildProperties(searchRequest, sortDirection, sortBy, numberToShow, xStatType, showTotals, yStatType);
            return Response.ok(properties).cacheControl(NO_CACHE).build();
        }
        catch (SearchUnavailableException e)
        {
            if (!e.isIndexingEnabled())
            {
                return createIndexingUnavailableResponse(createIndexingUnavailableMessage());
            }
            else
            {
                throw e;
            }
        }
    }

    TwoDimensionalProperties buildProperties(SearchRequest searchRequest, String sortDirection, String sortBy, int numberToShow, String xStatType, boolean showTotals, String yStatType)
    {
        final StatisticsMapper xAxisMapper = getGenerator().getStatsMapper(xStatType);
        final StatisticsMapper yAxisMapper = getGenerator().getStatsMapper(yStatType);

        final String xDisplayName = validateStatType(new ArrayList<ValidationError>(), xStatType, X_STAT_TYPE);
        final String yDisplayName = validateStatType(new ArrayList<ValidationError>(), yStatType, Y_STAT_TYPE);

        TwoDimensionalStatsMap statsMap = getAndPopulateTwoDimensionalStatsMap(xAxisMapper, yAxisMapper, searchRequest);
        Collection<?> allYAxisObjects = statsMap.getYAxis(sortBy, sortDirection);

        Collection<?> xAxisObjects = statsMap.getXAxis();
        Collection<?> yAxisObjects = buildYObjects(numberToShow, allYAxisObjects);

        String filterUrl = urlPrefix() + "&" + REQUEST_ID + "=" + searchRequest.getId();
        FilterProperties fp = new FilterProperties(searchRequest.getName(), searchRequest.getDescription(), filterUrl, xAxisObjects.isEmpty() || yAxisObjects.isEmpty());

        Row firstRow = makeFirstRow(xStatType, searchRequest, xAxisMapper, xAxisObjects, showTotals, statsMap.hasIrrelevantXData());
        final int rowCount = statsMap.hasIrrelevantYData() ? allYAxisObjects.size() + 1 : allYAxisObjects.size();
        List<Row> rows = makeRows(searchRequest, xAxisMapper, yAxisMapper, statsMap, xAxisObjects, yAxisObjects, showTotals, yStatType);
        return new TwoDimensionalProperties(fp, firstRow, rows, showTotals, xDisplayName, yDisplayName, rowCount);
    }

    protected TwoDimensionalStatsMap getAndPopulateTwoDimensionalStatsMap(StatisticsMapper xAxisMapper, StatisticsMapper yAxisMapper, SearchRequest searchRequest)
    {
        TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(xAxisMapper, yAxisMapper);
        populateStatsMap(searchRequest, statsMap);
        return statsMap;
    }

    private Collection<?> buildYObjects(int numberToShow, Collection<?> yAxisObjects)
    {
        if (numberToShow != 0 && yAxisObjects.size() > numberToShow)
        {
            return new ArrayList<Object>(yAxisObjects).subList(0, numberToShow);
        }
        return yAxisObjects;
    }

    private void populateStatsMap(SearchRequest searchRequest, TwoDimensionalStatsMap statsMap)
    {
        Collector aHitCollector = new TwoDimensionalTermHitCollector(statsMap, fieldVisibilityManager, readerCache, null, fieldManager);
        try
        {
            searchProvider.search(searchRequest.getQuery(), authenticationContext.getLoggedInUser(), aHitCollector);
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }
    }

    private List<Row> makeRows(SearchRequest searchRequest, StatisticsMapper xAxisMapper,
            StatisticsMapper yAxisMapper, TwoDimensionalStatsMap statsMap,
            Collection<?> xAxisObjects, Collection<?> yAxisObjects, boolean showTotals, String yStatType)
    {
        List<Row> rows = new ArrayList<Row>();
        I18nHelper i18nHelper = authenticationContext.getI18nHelper();
        for (Object y : yAxisObjects)
        {
            List<Cell> cells = new ArrayList<Cell>();
            for (Object x : xAxisObjects)
            {
                String searchUrl = getStatsSearchUrlBuilder().getSearchUrlForCell(x, y, statsMap, searchRequest);
                Cell cell = new Cell(makeMarkupForCell(makeUrlForQuery(searchUrl), Integer.toString(statsMap.getCoordinate(x, y))));
                cells.add(cell);
            }

            String searchUrlForHeader = getStatsSearchUrlBuilder().getSearchUrlForHeaderCell(y, yAxisMapper, searchRequest);
            String searchUrl = makeUrlForQuery(searchUrlForHeader);
            Cell first = new Cell(makeHeadingCell(yStatType, y, searchUrl));

            // Lets add the Irrelevant column cells if we need to
            if (statsMap.hasIrrelevantXData())
            {
                final Number yIrrelevantCount = statsMap.getXAxisIrrelevantTotal(y);
                Cell irrelevantCount = new Cell(yIrrelevantCount.toString());
                cells.add(irrelevantCount);
            }

            if (showTotals)
            {
                Cell last;
                Number totalForColumn = statsMap.getYAxisUniqueTotal(y);
                // If we are including irrelevant numbers in the total then we can not generate a JQL URL for the cell
                if (statsMap.hasIrrelevantXData() && statsMap.getXAxisIrrelevantTotal(y) > 0)
                {
                    totalForColumn = totalForColumn.longValue() + statsMap.getXAxisIrrelevantTotal(y);
                    last = new Cell(totalForColumn.toString(), CollectionBuilder.newBuilder("totals").asList());
                }
                else
                {
                    last = new Cell(makeMarkupForCell(searchUrl, totalForColumn.toString()), CollectionBuilder.newBuilder("totals").asList());
                }
                cells.add(last);
            }

            Row row = new Row(first, cells);
            rows.add(row);
        }

        // We may need an additional row for irrelevant Y stats
        if (statsMap.hasIrrelevantYData())
        {
            List<Cell> cells = new ArrayList<Cell>();
            for (Object o : xAxisObjects)
            {
                Number totalForColumn = statsMap.getYAxisIrrelevantTotal(o);
                Cell cell = new Cell(totalForColumn.toString());
                cells.add(cell);
            }

            // We only need the Irrelevant/Irrelevant column if both X and Y have irrelevant data
            if (statsMap.hasIrrelevantXData() && statsMap.hasIrrelevantYData())
            {
                // Need a cell that is the total of irrelevant/irrelevant
                cells.add(new Cell(Long.toString(statsMap.getBothIrrelevant())));
            }

            // Need to calculate the totals for the Irrelevant Column (the whole Y axis)
            if (showTotals)
            {
                Cell last = new Cell(Long.toString(statsMap.getYAxisUniqueTotal(FilterStatisticsValuesGenerator.IRRELEVANT) + statsMap.getBothIrrelevant()),
                        CollectionBuilder.newBuilder("totals").asList());
                cells.add(last);
            }

            Cell first = new Cell("<span title=\"" + i18nHelper.getText("common.concepts.irrelevant.desc") + "\">" + i18nHelper.getText("common.concepts.irrelevant") + "</span>");
            Row row = new Row(first, cells);
            rows.add(row);
        }


        //Needs an additional row for show totals
        if (showTotals)
        {
            List<Cell> cells = new ArrayList<Cell>();
            for (Object o : xAxisObjects)
            {
                Number totalForColumn = statsMap.getXAxisUniqueTotal(o);
                Cell cell;

                // If we are including irrelevant numbers in the total then we can not generate a JQL URL for the cell
                if (statsMap.hasIrrelevantYData() && statsMap.getYAxisIrrelevantTotal(o) > 0)
                {
                    totalForColumn = totalForColumn.longValue() + statsMap.getYAxisIrrelevantTotal(o);
                    cell = new Cell(totalForColumn.toString(), CollectionBuilder.newBuilder("totals").asList());
                }
                else
                {
                    String searchUrl = makeUrlForQuery(getStatsSearchUrlBuilder().getSearchUrlForHeaderCell(o, xAxisMapper, searchRequest));
                    cell = new Cell(makeMarkupForCell(searchUrl, totalForColumn.toString()), CollectionBuilder.newBuilder("totals").asList());
                }

                cells.add(cell);
            }

            // Need to calculate the totals for the Irrelevant Column (the whole X axis)
            if (statsMap.hasIrrelevantXData() || statsMap.getBothIrrelevant() > 0)
            {
                Cell last = new Cell(Long.toString(statsMap.getXAxisUniqueTotal(FilterStatisticsValuesGenerator.IRRELEVANT) + statsMap.getBothIrrelevant()),
                        CollectionBuilder.newBuilder("totals").asList());
                cells.add(last);
            }

            Cell last = new Cell(Long.toString(statsMap.getUniqueTotal()), CollectionBuilder.newBuilder("totals").asList());
            cells.add(last);

            Cell first = new Cell(i18nHelper.getText("gadget.twodimensionalfilterstats.total.yaxis") + ":", CollectionBuilder.newBuilder("totals").asList());
            Row row = new Row(first, cells);
            rows.add(row);
        }

        return rows;
    }

    private String makeMarkupForCell(String searchUrl, String value)
    {
        return "<a href='" + searchUrl + "'>" + value + "</a>";
    }

    private Row makeFirstRow(String xStatType, SearchRequest searchRequest, StatisticsMapper xAxisMapper, Collection<?> xAxisObjects, boolean showTotals, final boolean hasIrrelevantDataForXAxis)
    {
        List<Cell> headingCells = new ArrayList<Cell>();
        for (Object o : xAxisObjects)
        {
            String urlForQuery = makeUrlForQuery(getStatsSearchUrlBuilder().getSearchUrlForHeaderCell(o, xAxisMapper, searchRequest));
            Cell cell = new Cell(makeHeadingCell(xStatType, o, urlForQuery));
            headingCells.add(cell);
        }
        I18nHelper i18nHelper = authenticationContext.getI18nHelper();

        // Include the Irrelevant header if we are showing irrelevant data for the X axis
        if (hasIrrelevantDataForXAxis)
        {
            Cell cell = new Cell("<span title=\"" + i18nHelper.getText("common.concepts.irrelevant.desc") + "\">" + i18nHelper.getText("common.concepts.irrelevant") + "</span>");
            headingCells.add(cell);
        }
        
        if (showTotals)
        {
            Cell cell = new Cell(i18nHelper.getText("gadget.twodimensionalfilterstats.total.xaxis"));
            headingCells.add(cell);
        }

        return new Row(headingCells);
    }

    private StatsMarkup makeHeadingCell(String statType, Object value, String url)
    {
        return ObjectToFieldValueMapper.transform(statType, value, url, fieldValueToDisplayTransformer);
    }

    private String makeUrlForQuery(String s)
    {
        return urlPrefix() + "&reset=true" + s;
    }

    private String urlPrefix()
    {
        return getBaseUrl() + "/secure/IssueNavigator.jspa?mode=hide";
    }

    private String getBaseUrl()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
    }

    private String validateStatType(final Collection<ValidationError> errors, final String statType, String statTypeName)
    {
        final String displayName = statisticTypesResource.getDisplayName(statType);

        if (StringUtils.isBlank(displayName))
        {
            errors.add(new ValidationError(statTypeName, "gadget.common.invalid.stat.type", statType));
        }
        return displayName;
    }

   /**
     * Validates a field that contains the number of results or items to show.  Ensures the value is greater than 0
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
            final int validatedNum = Integer.valueOf(numberToShow);
            if (validatedNum <= 0)
            {
                errors.add(new ValidationError(fieldName, "gadget.common.num.negative"));
            }
            return validatedNum;
        }
        catch (NumberFormatException e)
        {
            errors.add(new ValidationError(fieldName, "gadget.common.num.nan"));
        }

        return -1;
    }

    protected StatsSearchUrlBuilder getStatsSearchUrlBuilder()
    {
        return new DefaultStatsSearchUrlBuilder(searchService, authenticationContext);
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class TwoDimensionalProperties
    {
        @XmlElement
        private FilterProperties filter;
        @XmlElement
        private List<Row> rows;
        @XmlElement
        private Row firstRow;

        @XmlElement
        private String xHeading;
        @XmlElement
        private String yHeading;

        @XmlElement
        private int totalRows;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private TwoDimensionalProperties()
        {}

        @XmlElement
        private boolean showTotals;

        public TwoDimensionalProperties(FilterProperties filter, Row firstRow, List<Row> rows, boolean showTotals, String xHeading, String yHeading, int totalRows)
        {
            this.filter = filter;
            this.firstRow = firstRow;
            this.rows = rows;

            this.showTotals = showTotals;
            this.xHeading = xHeading;
            this.yHeading = yHeading;
            this.totalRows = totalRows;
        }

        public TwoDimensionalProperties(FilterProperties filter)
        {
            this.filter = filter;
        }

        public FilterProperties getFilter()
        {
            return filter;
        }

        public List<Row> getRows()
        {
            return rows;
        }

        public Row getFirstRow()
        {
            return firstRow;
        }

        public String getXHeading()
        {
            return xHeading;
        }

        public String getYHeading()
        {
            return yHeading;
        }

        public boolean isShowTotals()
        {
            return showTotals;
        }

        public int getTotalRows()
        {
            return totalRows;
        }
    }

    @XmlRootElement
    public static class Row
    {
        @XmlElement
        private List<Cell> cells;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private Row()
        {}

        public Row(Cell first, List<Cell> otherCells)
        {

            this.cells = new ArrayList<Cell>();
            cells.add(first);
            cells.addAll(otherCells);
        }

        public Row(List<Cell> otherCells)
        {
            this.cells = new ArrayList<Cell>();
            cells.addAll(otherCells);
        }

        public List<Cell> getCells()
        {
            return cells;
        }
    }

    @XmlRootElement
    public static class Cell
    {
        @XmlElement
        private String markup;
        @XmlElement
        private List<String> classes;

        public Cell(String markup)
        {
            this.markup = markup;
        }

        public Cell(String markup, List<String> classes)
        {
            this.markup = markup;
            this.classes = classes;
        }

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private Cell()
        {}

        public Cell(StatsMarkup markup)
        {
            this.markup = markup.getHtml();
            this.classes = markup.getClasses();
        }

        public String getMarkup()
        {
            return markup;
        }

        public List<String> getClasses()
        {
            return classes;
        }
    }

    @XmlRootElement
    public static class FilterProperties
    {
        @XmlElement
        private String filterTitle;
        @XmlElement
        private String filterDescription;
        @XmlElement
        private String filterUrl;
        @XmlElement
        private boolean empty;

        public boolean isEmpty()
        {
            return empty;
        }

        public String getFilterTitle()
        {
            return filterTitle;
        }

        public String getFilterUrl()
        {
            return filterUrl;
        }

        public String getFilterDescription()
        {
            return filterDescription;
        }

        public FilterProperties(String filterTitle, String filterDescription, String filterUrl, boolean empty)
        {
            this.filterTitle = filterTitle;
            this.filterDescription = filterDescription;
            this.filterUrl = filterUrl;
            this.empty = empty;
        }

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        public FilterProperties()
        {}
    }
    ///CLOVER:ON
}


