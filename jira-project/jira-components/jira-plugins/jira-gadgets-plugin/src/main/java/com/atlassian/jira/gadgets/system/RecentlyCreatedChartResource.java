
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
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST endpoint to validate and retreive a Recent Created chart.
 *
 * @since v4.0
 */
@Path ("/recentlyCreated")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class RecentlyCreatedChartResource extends SearchQueryBackedResource
{
    private static final String PERIOD_NAME = "periodName";
    private static final String DAYS = "daysprevious";
    private static final String NUM_ISSUES = "numIssues";

    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String INLINE = "inline";

    private final ChartFactory chartFactory;
    private static final String RETURN_DATA = "returnData";
    private ResourceDateValidator resourceDateValidator;

    public RecentlyCreatedChartResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext,
            final SearchService searchService, final PermissionManager permissionManager,
            final ChartFactory chartFactory, final VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties)
    {
        super(chartUtils, authenticationContext, searchService, permissionManager, velocityRequestContextFactory);
        this.chartFactory = chartFactory;
        resourceDateValidator = new ResourceDateValidator(applicationProperties);
    }

    /**
     * Generate a Recently Created Chart and returns a simple bean containing all relevent information
     *
     * @param request     The current HTTPRequest. Needed for url generation
     * @param queryString a filter id (starts with "filter-") or project id (starts with "project-")or jql (starts with
     *                    "jql-")
     * @param days        The number of days previous to go back for.  Must be positive.
     * @param periodName  The name of the period.  See - {@link com.atlassian.jira.charts.ChartFactory.PeriodName}
     * @param width       the width of the chart in pixels (defaults to 400px)
     * @param height      the height of the chart in pixels (defaults to 250px)
     * @return a {@link com.atlassian.jira.gadgets.system.RecentlyCreatedChartResource.RecentlyCreatedChart} if all
     *         params validated else a Collection of {@link com.atlassian.jira.rest.v1.model.errors.ValidationError}
     */
    @GET
    @Path ("/generate")
    public Response generateChart(@Context HttpServletRequest request,
            @QueryParam (QUERY_STRING) String queryString,
            @QueryParam (DAYS) @DefaultValue ("30") final String days,
            @QueryParam (PERIOD_NAME) @DefaultValue ("daily") final String periodName,
            @QueryParam (RETURN_DATA) @DefaultValue ("false") final boolean returnData,
            @QueryParam (WIDTH) @DefaultValue ("400") final int width,
            @QueryParam (HEIGHT) @DefaultValue ("250") final int height,
            @QueryParam (INLINE) @DefaultValue ("false") final boolean inline)
    {

        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        final User user = authenticationContext.getLoggedInUser();
        final SearchRequest searchRequest;

        Map<String, Object> params = new HashMap<String, Object>();

        // validate input
        searchRequest = getSearchRequestAndValidate(queryString, errors, params);
        final ChartFactory.PeriodName period = resourceDateValidator.validatePeriod(PERIOD_NAME, periodName, errors);
        final int validatedDays = resourceDateValidator.validateDaysPrevious(DAYS, period, days, errors);

        if (!errors.isEmpty())
        {
            return createErrorResponse(errors);
        }

        final ChartFactory.ChartContext context = new ChartFactory.ChartContext(user, searchRequest, width, height, inline);
        try
        {
            final Chart chart = chartFactory.generateRecentlyCreated(context, validatedDays, period);

            final String location = chart.getLocation();
            final String title = getFilterTitle(params);
            final String filterUrl = getFilterUrl(params);
            final Integer issueCount = (Integer) chart.getParameters().get(NUM_ISSUES);
            final String imageMap = chart.getImageMap();
            final String imageMapName = chart.getImageMapName();
            final Integer imageHeight = (Integer) chart.getParameters().get(HEIGHT);
            final Integer imageWidth = (Integer) chart.getParameters().get(WIDTH);

            DataRow[] data = null;
            if (returnData)
            {
                final CategoryDataset completeDataset = (CategoryDataset) chart.getParameters().get("completeDataset");
                final CategoryURLGenerator completeUrlGenerator = (CategoryURLGenerator) chart.getParameters().get("completeDatasetUrlGenerator");

                data = generateDataset(completeDataset, completeUrlGenerator);
            }

            final RecentlyCreatedChart recentlyCreatedChart = new RecentlyCreatedChart(location, title, filterUrl, imageMap, imageMapName, issueCount, imageWidth, imageHeight, data, chart.getBase64Image());

            return Response.ok(recentlyCreatedChart).cacheControl(NO_CACHE).build();
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

    private DataRow[] generateDataset(CategoryDataset dataset, CategoryURLGenerator urlGenerator)
    {
        final DataRow[] data = new DataRow[dataset.getColumnCount()];
        // header
        for (int col = 0; col < dataset.getColumnCount(); col++)
        {
            Object key = dataset.getColumnKey(col);
            int unresolvedVal = dataset.getValue(0, col).intValue();
            String unresolvedUrl = urlGenerator.generateURL(dataset, 0, col);
            int resolvedVal = dataset.getValue(1, col).intValue();
            String resolvedUrl = urlGenerator.generateURL(dataset, 1, col);
            int totalCreatedVal = unresolvedVal + resolvedVal;

            data[col] = new DataRow(key, totalCreatedVal, resolvedVal, resolvedUrl, unresolvedVal, unresolvedUrl);
        }

        return data;
    }

    /**
     * Ensures all parameters are valid for the Recently Created Chart
     *
     * @param queryString a filter id (starts with "filter-") or project id (starts with "project-").
     * @param days        The number of days previous to go back for.  Must be positive.
     * @param periodName  The name of the period.  See - {@link com.atlassian.jira.charts.ChartFactory.PeriodName}
     * @return a Collection of {@link com.atlassian.jira.rest.v1.model.errors.ValidationError}.  Or empty list if no
     *         errors.
     */
    @GET
    @Path ("/validate")
    public Response validateChart(@QueryParam (QUERY_STRING) String queryString,
            @QueryParam (DAYS) @DefaultValue ("30") final String days,
            @QueryParam (PERIOD_NAME) @DefaultValue ("daily") final String periodName)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        getSearchRequestAndValidate(queryString, errors, new HashMap<String, Object>());
        final ChartFactory.PeriodName period = resourceDateValidator.validatePeriod(PERIOD_NAME, periodName, errors);
        resourceDateValidator.validateDaysPrevious(DAYS, period, days, errors);

        return createValidationResponse(errors);
    }

    ///CLOVER:OFF
    /**
     * A simple bean contain all information required to render the Recently Created Chart
     */
    @XmlRootElement
    public static class RecentlyCreatedChart
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
        private String imageMap;
        @XmlElement
        private String imageMapName;
        @XmlElement
        private Integer issueCount;
        @XmlElement
        private DataRow[] data;
        @XmlElement
        private Integer height;
        @XmlElement
        private Integer width;
        @XmlElement
        private String base64Image;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        RecentlyCreatedChart()
        {}

        RecentlyCreatedChart(String location, String filterTitle, String filterUrl, String imageMap, String imageMapName, Integer issueCount,Integer width, Integer height, DataRow[] data, String base64Image)
        {
            this.location = location;
            this.filterTitle = filterTitle;
            this.filterUrl = filterUrl;
            this.imageMap = imageMap;
            this.imageMapName = imageMapName;
            this.issueCount = issueCount;
            this.height = height;
            this.width = width;
            this.data = data;
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

        public String getImageMap()
        {
            return imageMap;
        }

        public String getImageMapName()
        {
            return imageMapName;
        }

        public Integer getIssueCount()
        {
            return issueCount;
        }

        public Integer getHeight()
        {
            return height;
        }

        public Integer getWidth()
        {
            return width;
        }

        public DataRow[] getData()
        {
            return data;
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
    @XmlType (namespace = "com.atlassian.jira.gadgets.system.RecentlyCreatedChartResource")
    public static class DataRow
    {
        private Object key;

        @XmlElement
        private int createdValue;

        @XmlElement
        private int resolvedValue;

        @XmlElement
        private String resolvedUrl;

        @XmlElement
        private int unresolvedValue;

        @XmlElement
        private String unresolvedUrl;

        @XmlElement(name="key")
        private String keyString;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        public DataRow()
        {}

        public DataRow(final Object key, final int createdValue, final int resolvedValue, final String resolvedUrl, final int unresolvedValue, final String unresolvedUrl)
        {
            this.key = key;
            this.createdValue = createdValue;
            this.resolvedValue = resolvedValue;
            this.resolvedUrl = resolvedUrl;
            this.unresolvedValue = unresolvedValue;
            this.unresolvedUrl = unresolvedUrl;
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

        public int getCreatedValue()
        {
            return createdValue;
        }

        public int getResolvedValue()
        {
            return resolvedValue;
        }

        public String getResolvedUrl()
        {
            return resolvedUrl;
        }

        public int getUnresolvedValue()
        {
            return unresolvedValue;
        }

        public String getUnresolvedUrl()
        {
            return unresolvedUrl;
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

