package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.jfreechart.TimePeriodUtils;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.gadgets.system.util.ResourceDateValidator;
import com.atlassian.jira.issue.index.SearchUnavailableException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.xy.XYDataset;

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
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST endpoint to validate and retreive a Created vs Resolved chart.
 *
 * @since v4.0
 */
@Path ("/createdVsResolved")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class CreatedVsResolvedResource extends SearchQueryBackedResource
{
    public static final String DAYS_NAME = "daysprevious";
    private static final String PERIOD_NAME = "periodName";
    public static final String VERSION_LABEL = "versionLabel";
    private static final String IS_CUMULATIVE = "isCumulative";
    private static final String SHOW_UNRESOLVED_TREND = "showUnresolvedTrend";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String NUM_CREATED_ISSUES = "numCreatedIssues";
    private static final String RETURN_DATA = "returnData";
    private static final String NUM_RESOLVED_ISSUES = "numResolvedIssues";
    private static final String INLINE = "inline";

    private final ChartFactory chartFactory;
    private final ResourceDateValidator resourceDateValidator;
    private final TimeZoneManager timeZoneManager;

    public CreatedVsResolvedResource(final ChartFactory chartFactory, final ChartUtils chartUtils,
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
     * Generate a create Versus Resolved Chart and returns a simple bean containing all relievent information
     *
     * @param request             The current HTTPRequest. Needed for url generation
     * @param queryString         a filter id (starts with "filter-") or project id (starts with "project-")or jql
     *                            (starts with "jql-")
     * @param days                The number of days previous to go back for.  Must be positive.
     * @param periodName          The name of the period.  See - {@link ChartFactory.PeriodName}
     * @param versionLabel        The name of teh versions to show.  See - {@link com.atlassian.jira.charts.ChartFactory.VersionLabel}
     * @param isCumulative        Whether or not the values are added to the previous value.
     * @param showUnresolvedTrend Show sub-chart underneath showing trend of unresolved issues
     * @param returnData          Whether or not to have data returned
     * @param width               the width of the chart in pixels (defaults to 400px)
     * @param height              the height of the chart in pixels (defaults to 250px)
     * @return a {@link com.atlassian.jira.gadgets.system.CreatedVsResolvedResource.CreatedVsResolvedChart} if all
     *         params validated else a Collection of {@link ValidationError}
     */
    @GET
    @Path ("/generate")
    public Response generateChart(@Context HttpServletRequest request,
            @QueryParam (QUERY_STRING) String queryString,
            @QueryParam (DAYS_NAME) @DefaultValue ("30") final String days,
            @QueryParam (PERIOD_NAME) @DefaultValue ("daily") final String periodName,
            @QueryParam (VERSION_LABEL) @DefaultValue ("major") final String versionLabel,
            @QueryParam (IS_CUMULATIVE) @DefaultValue ("true") final boolean isCumulative,
            @QueryParam (SHOW_UNRESOLVED_TREND) @DefaultValue ("false") final boolean showUnresolvedTrend,
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

        final Map<String, Object> params = new HashMap<String, Object>();

        // validate input
        searchRequest = getSearchRequestAndValidate(queryString, errors, params);
        final ChartFactory.PeriodName period = resourceDateValidator.validatePeriod(PERIOD_NAME, periodName, errors);
        final int numberOfDays = resourceDateValidator.validateDaysPrevious(DAYS_NAME, period, days, errors);
        final ChartFactory.VersionLabel label = validateVersionLabel(versionLabel, errors);

        if (!errors.isEmpty())
        {
            return Response.status(400).entity(ErrorCollection.Builder.newBuilder(errors).build()).cacheControl(NO_CACHE).build();
        }

        final ChartFactory.ChartContext context = new ChartFactory.ChartContext(user, searchRequest, width, height, inline);
        try
        {
            final Chart chart = chartFactory.generateCreatedVsResolvedChart(context, numberOfDays, period, label, isCumulative, showUnresolvedTrend);

            final String location = chart.getLocation();
            final String title = getFilterTitle(params);
            final String filterUrl = getFilterUrl(params);
            final Integer issuesCreated = (Integer) chart.getParameters().get(NUM_CREATED_ISSUES);
            final Integer issuesResolved = (Integer) chart.getParameters().get(NUM_RESOLVED_ISSUES);
            final String imageMap = chart.getImageMap();
            final String imageMapName = chart.getImageMapName();

            DataRow[] data = null;
            if (returnData)
            {
                final CategoryDataset completeDataset = (CategoryDataset) chart.getParameters().get("completeDataset");
                final XYDataset chartDataset = (XYDataset) chart.getParameters().get("chartDataset");
                final XYURLGenerator completeUrlGenerator = (XYURLGenerator) chart.getParameters().get("completeDatasetUrlGenerator");
                data = generateDataSet(completeDataset, completeUrlGenerator, chartDataset, showUnresolvedTrend);
            }

            final CreatedVsResolvedChart createdVsResolvedChart = new CreatedVsResolvedChart(location, title, filterUrl, issuesCreated, issuesResolved, imageMap, imageMapName, data, width, height, chart.getBase64Image());

            return Response.ok(createdVsResolvedChart).cacheControl(NO_CACHE).build();
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

    private DataRow[] generateDataSet(CategoryDataset dataset, XYURLGenerator urlGenerator, XYDataset chartdataset, boolean showTrend)
    {
        final TimePeriodUtils timePeriodUtils = new TimePeriodUtils(timeZoneManager);
        final DataRow[] data = new DataRow[dataset.getColumnCount()];

        // header
        for (int col = 0; col < dataset.getColumnCount(); col++)
        {
            Object key = dataset.getColumnKey(col);
            if (key instanceof RegularTimePeriod)
            {
                key = timePeriodUtils.prettyPrint((RegularTimePeriod) key);
            }

            int createdVal = dataset.getValue(0, col).intValue();
            String createdUrl = urlGenerator.generateURL(chartdataset, 0, col);
            int resolvedVal = dataset.getValue(1, col).intValue();
            String resolvedUrl = urlGenerator.generateURL(chartdataset, 1, col);
            Integer trendCount = null;
            if (showTrend)
            {
                trendCount = dataset.getValue(2, col).intValue();
            }
            data[col] = new DataRow(key, createdUrl, createdVal, resolvedUrl, resolvedVal, trendCount);
        }

        return data;
    }

    private ChartFactory.VersionLabel validateVersionLabel(String versionLabel, Collection<ValidationError> errors)
    {
        try
        {
            return ChartFactory.VersionLabel.valueOf(versionLabel);
        }
        catch (IllegalArgumentException e)
        {
            errors.add(new ValidationError(VERSION_LABEL, "gadget.created.vs.resolved.invalid.version.label"));
        }
        return null;
    }

    /**
     * Ensures all parameters are valid for the Created Versus Resolved Chart
     *
     * @param queryString  a filter id (starts with "filter-") or project id (starts with "project-").
     * @param days         The number of days previous to go back for.  Must be positive.
     * @param periodName   The name of the period.  See - {@link ChartFactory.PeriodName}
     * @param versionLabel The name of teh versions to show.  See - {@link com.atlassian.jira.charts.ChartFactory.VersionLabel}
     * @return a Collection of {@link ValidationError}.  Or empty list if no errors.
     */
    @GET
    @Path ("/validate")
    public Response validateChart(@QueryParam (QUERY_STRING) String queryString,
            @QueryParam (DAYS_NAME) @DefaultValue ("30") final String days,
            @QueryParam (PERIOD_NAME) @DefaultValue ("daily") final String periodName,
            @QueryParam (VERSION_LABEL) @DefaultValue ("major") final String versionLabel)
    {
        if (StringUtils.isNotBlank(queryString) && !queryString.contains("-"))
        {
            queryString = "filter-" + queryString;
        }

        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        final Map<String, Object> params = new HashMap<String, Object>();
        getSearchRequestAndValidate(queryString, errors, params);
        final ChartFactory.PeriodName period = resourceDateValidator.validatePeriod(PERIOD_NAME, periodName, errors);
        resourceDateValidator.validateDaysPrevious(DAYS_NAME, period, days, errors);
        validateVersionLabel(versionLabel, errors);

        return createValidationResponse(errors);
    }

    ///CLOVER:OFF
    /**
     * A simple bean contain all information required to render the Created Versus Chart
     */
    @XmlRootElement
    public static class CreatedVsResolvedChart
    {
        // The URL where the chart image is available from.  The image is once of image that can only be accessed once.
        @XmlElement
        private String location;
        // The title of the chart
        @XmlElement
        private String filterTitle;
        // The link of where to send the user to - For a project, send em to the browse project, for a filter, send em tothe Issue Nav
        @XmlElement
        private String filterUrl;
        @XmlElement
        private int issuesCreated;
        @XmlElement
        private int issuesResolved;
        @XmlElement
        private String imageMap;
        @XmlElement
        private String imageMapName;
        @XmlElement
        private DataRow[] data;
        @XmlElement
        private int width;
        @XmlElement
        private int height;
        @XmlElement
        protected String base64Image;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private CreatedVsResolvedChart()
        {}

        CreatedVsResolvedChart(String location, String filterTitle, String filterUrl, int issuesCreated, int issuesResolved, String imageMap, String imageMapName,
                DataRow[] data, int width, int height, String base64Image)
        {
            this.location = location;
            this.filterTitle = filterTitle;
            this.filterUrl = filterUrl;
            this.issuesCreated = issuesCreated;
            this.issuesResolved = issuesResolved;
            this.imageMap = imageMap;
            this.imageMapName = imageMapName;
            this.data = data;
            this.width = width;
            this.height = height;
            this.base64Image = base64Image;
        }

        public String getLocation()
        {
            return location;
        }

        public String getFilterTitle()
        {
            return filterTitle;
        }

        public String getFilterUrl()
        {
            return filterUrl;
        }

        public int getIssuesCreated()
        {
            return issuesCreated;
        }

        public int getIssuesResolved()
        {
            return issuesResolved;
        }

        public String getImageMap()
        {
            return imageMap;
        }

        public String getImageMapName()
        {
            return imageMapName;
        }

        public DataRow[] getData()
        {
            return data;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @XmlRootElement
    //have to define a namespace here, since there's other 'DataRow' JAXB beans
    @XmlType (namespace = "com.atlassian.jira.gadgets.system.CreatedVsResolvedResource")
    public static class DataRow
    {
        private Object key;
        @XmlElement
        private String createdUrl;
        @XmlElement
        private int createdValue;
        @XmlElement
        private String resolvedUrl;
        @XmlElement
        private int resolvedValue;
        @XmlElement
        private Integer trendCount;
        @XmlElement(name="key")
        private String keyString;

        public DataRow() {}

        DataRow(final Object key, final String createdUrl, final int createdValue, final String resolvedUrl, final int resolvedValue, Integer trendCount)
        {
            this.key = key;
            this.createdUrl = createdUrl;
            this.createdValue = createdValue;
            this.resolvedUrl = resolvedUrl;
            this.resolvedValue = resolvedValue;
            this.trendCount = trendCount;
            this.keyString = key.toString();
        }

        public String getKey()
        {
            return key.toString();
        }

        public Object getRawKey()
        {
            return key;
        }

        public String getCreatedUrl()
        {
            return createdUrl;
        }

        public int getCreatedValue()
        {
            return createdValue;
        }

        public String getResolvedUrl()
        {
            return resolvedUrl;
        }

        public int getResolvedValue()
        {
            return resolvedValue;
        }

        public Integer getTrendCount()
        {
            return trendCount;
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
    ///CLOVER:ON
}
