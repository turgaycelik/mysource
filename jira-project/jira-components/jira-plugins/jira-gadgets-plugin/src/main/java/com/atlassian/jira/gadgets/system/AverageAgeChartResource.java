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
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.TimeSeriesCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST endpoint to validate and retreive a Average Age chart.
 *
 * @since v4.0
 */
@Path ("/averageage")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class AverageAgeChartResource extends SearchQueryBackedResource
{
    private static final String PERIOD_NAME = "periodName";
    private static final String DAYS = "daysprevious";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String RETURN_DATA = "returnData";
    private static final String INLINE = "inline";

    private final ChartFactory chartFactory;
    private final TimeZoneManager timeZoneManager;
    private final ResourceDateValidator resourceDateValidator;

    public AverageAgeChartResource(final ChartFactory chartFactory, final ChartUtils chartUtils,
            final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager,
            final SearchService searchService, VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties, TimeZoneManager timeZoneManager)
    {
        super(chartUtils, authenticationContext, searchService, permissionManager, velocityRequestContextFactory);
        this.chartFactory = chartFactory;
        this.timeZoneManager = timeZoneManager;
        this.resourceDateValidator = new ResourceDateValidator(applicationProperties);
    }

    /**
     * Generate an Average Age Chart and returns a simple bean containing all relievent information
     *
     * @param request     The current HTTPRequest. Needed for url generation
     * @param queryString a filter id (starts with "filter-") or project id (starts with "project-")or jql (starts with
     *                    "jql-")
     * @param days        The number of days previous to go back for.  Must be positive.
     * @param periodName  The name of the period.  See - {@link com.atlassian.jira.charts.ChartFactory.PeriodName}
     * @param returnData  whether to return the data or not
     * @param width       the width of the chart in pixels (defaults to 400px)
     * @param height      the height of the chart in pixels (defaults to 250px)
     * @return a {@link DateRangeChart} if all params validated else a Collection of {@link
     *         com.atlassian.jira.rest.v1.model.errors.ValidationError}
     */
    @GET
    @Path ("/generate")
    public Response generateChart(@Context HttpServletRequest request,
            @QueryParam (QUERY_STRING) String queryString,
            @QueryParam (DAYS) @DefaultValue ("30") final String days,
            @QueryParam (PERIOD_NAME) @DefaultValue ("daily") final String periodName,
            @QueryParam (RETURN_DATA) @DefaultValue ("false") final boolean returnData,
            @QueryParam (WIDTH) @DefaultValue ("450") final int width,
            @QueryParam (HEIGHT) @DefaultValue ("300") final int height,
            @QueryParam (INLINE) @DefaultValue ("false") final boolean inline)
    {
        if (StringUtils.isNotBlank(queryString) && !queryString.contains("-"))
        {
            queryString = "filter-" + queryString;
        }

        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        final User user = authenticationContext.getLoggedInUser();
        final SearchRequest searchRequest;

        Map<String, Object> params = new HashMap<String, Object>();

        // validate input
        searchRequest = getSearchRequestAndValidate(queryString, errors, params);
        final ChartFactory.PeriodName period = resourceDateValidator.validatePeriod(PERIOD_NAME, periodName, errors);
        final int numberOfDays = resourceDateValidator.validateDaysPrevious(DAYS, period, days, errors);

        if (!errors.isEmpty())
        {
            return Response.status(400).entity(ErrorCollection.Builder.newBuilder(errors).build()).cacheControl(NO_CACHE).build();
        }

        final ChartFactory.ChartContext context = new ChartFactory.ChartContext(user, searchRequest, width, height, inline);
        try
        {
            final Chart chart = chartFactory.generateAverageAgeChart(context, numberOfDays, period);

            final String location = chart.getLocation();
            final String title = getFilterTitle(params);
            final String filterUrl = getFilterUrl(params);
            final String imageMap = chart.getImageMap();
            final String imageMapName = chart.getImageMapName();

            TimeChart.TimeDataRow[] data = null;
            if (returnData)
            {
                final TimeSeriesCollection dataSet = (TimeSeriesCollection) chart.getParameters().get("completeDataset");
                final XYURLGenerator urlGenerator = (XYURLGenerator) chart.getParameters().get("completeDatasetUrlGenerator");

                data = generateTimeChartDataSet(dataSet, urlGenerator);
            }

            final TimeChart averageAgeChart = new TimeChart(location, title, filterUrl, imageMap, imageMapName, data, width, height, chart.getBase64Image());

            return Response.ok(averageAgeChart).cacheControl(NO_CACHE).build();
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

    TimeChart.TimeDataRow[] generateTimeChartDataSet(final TimeSeriesCollection dataSet, final XYURLGenerator urlGenerator)
    {
        return new TimeChart.Generator().generateDataSet(dataSet, urlGenerator, timeZoneManager);
    }

    /**
     * Ensures all parameters are valid for the Average Age Chart
     *
     * @param queryString a filter id (starts with "filter-") or project id (starts with "project-").
     * @param days        The number of days previous to go back for.  Must be positive.
     * @param periodName  The name of the period.  See - {@link com.atlassian.jira.charts.ChartFactory.PeriodName}
     * @return a Collection of {@link ValidationError}.  Or empty list if no errors.
     */
    @GET
    @Path ("/validate")
    public Response validateChart(@QueryParam (QUERY_STRING) String queryString,
            @QueryParam (DAYS) @DefaultValue ("30") final String days,
            @QueryParam (PERIOD_NAME) @DefaultValue ("daily") final String periodName)
    {
        if (StringUtils.isNotBlank(queryString) && !queryString.contains("-"))
        {
            queryString = "filter-" + queryString;
        }

        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        Map<String, Object> params = new HashMap<String, Object>();
        getSearchRequestAndValidate(queryString, errors, params);
        final ChartFactory.PeriodName period = resourceDateValidator.validatePeriod(PERIOD_NAME, periodName, errors);
        resourceDateValidator.validateDaysPrevious(DAYS, period, days, errors);

        return createValidationResponse(errors);
    }
}
