package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.gadgets.system.util.ResourceDateValidator;
import com.atlassian.jira.issue.index.SearchUnavailableException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.TimeSeriesCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.core.util.DateUtils.DAY_MILLIS;
import static com.atlassian.jira.issue.index.DocumentConstants.ISSUE_RESOLUTION_DATE;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST endpoint to validate and retreive the Resolution Time chart.
 *
 * @since v4.0
 */
@Path ("resolutiontime")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class ResolutionTimeResource extends SearchQueryBackedResource
{
    static final String DAYS = "daysprevious";
    static final String PERIOD_NAME = "periodName";
    private static final String SEARCH_QUERY = "projectOrFilterId";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String INLINE = "inline";
    private static final String RETURN_DATA = "returnData";

    private final ChartFactory chartFactory;
    private static final String LABEL_SUFFIX_KEY = "datacollector.daystoresolve";
    private final ResourceDateValidator resourceDateValidator;
    private final TimeZoneManager timeZoneManager;

    public ResolutionTimeResource(final ChartFactory chartFactory, final ChartUtils chartUtils,
            final JiraAuthenticationContext authenticationContext, final SearchService searchService,
            final PermissionManager permissionManager, VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties, TimeZoneManager timeZoneManager)
    {
        this(chartUtils, authenticationContext, searchService, permissionManager, chartFactory, new ResourceDateValidator(applicationProperties), velocityRequestContextFactory, timeZoneManager);
    }

    ResolutionTimeResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext, final SearchService searchService, final PermissionManager permissionManager, final ChartFactory chartFactory, final ResourceDateValidator resourceDateValidator, VelocityRequestContextFactory velocityRequestContextFactory, TimeZoneManager timeZoneManager)
    {
        super(chartUtils, authenticationContext, searchService, permissionManager, velocityRequestContextFactory);
        this.chartFactory = chartFactory;
        this.resourceDateValidator = resourceDateValidator;
        this.timeZoneManager = timeZoneManager;
    }

    /**
     * Generate a bar chart of the time required to resolve issues.
     *
     * @param queryString a filter id (starts with "filter-") or project id (starts with "project-") or jql (starts with
     *                    "jql-")
     * @param days        The number of days previous to go back for.  Must be positive.
     * @param periodName  The name of the period.  See - {@link com.atlassian.jira.charts.ChartFactory.PeriodName}
     * @param returnData  if true, the detailed data will be returned.
     * @param width       the width of the chart in pixels (defaults to 400px)
     * @param height      the height of the chart in pixels (defaults to 300px)
     * @return a response that represents the view for the bar chart.
     */
    @GET
    @Path ("/generate")
    public Response getChart(@QueryParam (SEARCH_QUERY) String queryString,
            @QueryParam (DAYS) @DefaultValue ("30") final String days,
            @QueryParam (PERIOD_NAME) @DefaultValue ("daily") final String periodName,
            @QueryParam (RETURN_DATA) @DefaultValue ("false") final boolean returnData,
            @QueryParam (WIDTH) @DefaultValue ("450") final int width,
            @QueryParam (HEIGHT) @DefaultValue ("300") final int height,
            @QueryParam (INLINE) @DefaultValue ("false") final boolean inline)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        final User user = authenticationContext.getLoggedInUser();
        Map<String, Object> params = new HashMap<String, Object>();
        final SearchRequest searchRequest = getSearchRequestAndValidate(queryString, errors, params);
        final ChartFactory.PeriodName period = resourceDateValidator.validatePeriod(PERIOD_NAME, periodName, errors);
        final int daysInt = resourceDateValidator.validateDaysPrevious(DAYS, period, days, errors);

        if (!errors.isEmpty())
        {
            return createErrorResponse(errors);
        }

        try
        {
            final Chart chart = generateChart(width, height, user, searchRequest, period, daysInt, inline);

            TimeChart.TimeDataRow[] data = returnData ? getData(new TimeChart.Generator(), chart) : null;
            final TimeChart resolutionTimeChart = createChart(width, height, params, chart, data);

            return Response.ok(resolutionTimeChart).cacheControl(NO_CACHE).build();
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

    Chart generateChart(final int width, final int height, final User user, final SearchRequest searchRequest, final ChartFactory.PeriodName period, final int daysInt, final boolean inline)
    {
        final ChartFactory.ChartContext context = new ChartFactory.ChartContext(user, searchRequest, width, height, inline);
        return chartFactory.generateDateRangeTimeChart(context, daysInt, period, DAY_MILLIS, LABEL_SUFFIX_KEY, ISSUE_RESOLUTION_DATE);
    }

    TimeChart createChart(final int width, final int height, final Map<String, Object> params, final Chart chart, final TimeChart.TimeDataRow[] data)
    {
        final String location = chart.getLocation();
        final String title = getFilterTitle(params);
        final String filterUrl = getFilterUrl(params);
        final String imageMap = chart.getImageMap();
        final String imageMapName = chart.getImageMapName();

        return new TimeChart(location, title, filterUrl, imageMap, imageMapName, data, width, height, chart.getBase64Image());
    }

    TimeChart.TimeDataRow[] getData(TimeChart.Generator generator, final Chart chart)
    {
        Map<String, Object> params = chart.getParameters();
        final TimeSeriesCollection dataSet = (TimeSeriesCollection) params.get("completeDataset");
        final XYURLGenerator urlGenerator = (XYURLGenerator) params.get("completeDatasetUrlGenerator");
        return generator.generateDataSet(dataSet, urlGenerator, timeZoneManager);
    }

    /**
     * Ensures all parameters are valid for the resolution time graph.
     *
     * @param queryString a filter id (starts with "filter-") or project id (starts with "project-") or jql (starts with
     *                    "jql-")
     * @param days        the number of days over which the bar chart is to be drawn.
     * @param periodName  the name of a period See - {@link com.atlassian.jira.charts.ChartFactory.PeriodName}
     * @return a failure response if the parameters are not valid for producing a bar chart.
     */
    @GET
    @Path ("validate")
    public Response validate(@QueryParam (SEARCH_QUERY) String queryString,
            @QueryParam (DAYS) @DefaultValue ("30") final String days,
            @QueryParam (PERIOD_NAME) @DefaultValue ("daily") final String periodName)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        getSearchRequestAndValidate(queryString, errors, new HashMap<String, Object>());
        final ChartFactory.PeriodName period = resourceDateValidator.validatePeriod(PERIOD_NAME, periodName, errors);
        resourceDateValidator.validateDaysPrevious(DAYS, period, days, errors);
        return createValidationResponse(errors);
    }
}
