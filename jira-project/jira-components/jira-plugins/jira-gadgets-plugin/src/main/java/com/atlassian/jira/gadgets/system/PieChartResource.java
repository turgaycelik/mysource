package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.PieSegmentWrapper;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.issue.index.SearchUnavailableException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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
import javax.xml.bind.annotation.XmlType;

/**
 * REST endpoint to validate and retreive Pie chart.
 *
 * @since v4.0
 */
@Path ("piechart")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class PieChartResource extends SearchQueryBackedResource
{
    private static final String SEARCH_QUERY = "projectOrFilterId";
    static final String STAT_TYPE = "statType";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String NUM_ISSUES = "numIssues";
    private static final String RETURN_DATA = "returnData";
    private static final String INLINE = "inline";

    private final ChartFactory chartFactory;
    private final JiraAuthenticationContext authenticationContext;
    private StatisticTypesResource statisticTypesResource;
    static final String KEY_URL_GENERATOR = "completeDatasetUrlGenerator";
    static final String KEY_DATASET = "completeDataset";

    public PieChartResource(final ChartFactory chartFactory, final ChartUtils chartUtils,
            final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager,
            final StatisticTypesResource statisticTypesResource, final SearchService searchService, VelocityRequestContextFactory velocityRequestContextFactory)
    {
        super(chartUtils, authenticationContext, searchService, permissionManager, velocityRequestContextFactory);
        this.chartFactory = chartFactory;
        this.authenticationContext = authenticationContext;
        this.statisticTypesResource = statisticTypesResource;
    }

    /**
     * Generate a pie chart and returns a simple bean containing all relievent information
     *
     * @param queryString a filter id (starts with "filter-") or project id (starts with "project-") or jql (starts with
     *                    "jql-")
     * @param statType    a valid statistic type.  See {@link StatisticTypesResource}
     * @param returnData  whther or not to return the data
     * @param width       the width of the chart in pixels (defaults to 400px)
     * @param height      the height of the chart in pixels (defaults to 250px)
     * @return a {@link PieChart} if all params validated else a Collection of {@link com.atlassian.jira.rest.v1.model.errors.ValidationError}
     */
    @GET
    @Path ("/generate")
    public Response getPieChart(@QueryParam (SEARCH_QUERY) String queryString,
            @QueryParam (STAT_TYPE) @DefaultValue ("assignees") final String statType,
            @QueryParam (RETURN_DATA) @DefaultValue ("false") final boolean returnData,
            @QueryParam (WIDTH) @DefaultValue ("400") final int width,
            @QueryParam (HEIGHT) @DefaultValue ("250") final int height,
            @QueryParam (INLINE) @DefaultValue ("false") final boolean inline)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        final User user = authenticationContext.getLoggedInUser();
        final SearchRequest searchRequest;

        Map<String, Object> params = new HashMap<String, Object>();

        searchRequest = getSearchRequestAndValidate(queryString, errors, params);
        final String displayName = validateStatType(errors, statType);

        if (!errors.isEmpty())
        {
            return createErrorResponse(errors);
        }

        try
        {
            final Chart chart = createChart(statType, width, height, user, searchRequest, inline);

            final Map<String, Object> chartParams = chart.getParameters();
            final String location = chart.getLocation();
            final String title = getFilterTitle(params);
            final String filterUrl = getFilterUrl(params);
            final Long issueCount = (Long) chartParams.get(NUM_ISSUES);
            final int chartWidth =  (Integer) chartParams.get(WIDTH);
            final int chartHeight = (Integer) chartParams.get(HEIGHT);
            final String imageMap = chart.getImageMap();
            final String imageMapName = chart.getImageMapName();

            DataRow[] data = null;
            if (returnData)
            {
                data = getData(statType, chartParams);
            }

            final PieChart pieChart = new PieChart(location, title, filterUrl, issueCount, displayName, imageMap, imageMapName, data, chartWidth, chartHeight, chart.getBase64Image());

            return Response.ok(pieChart).cacheControl(NO_CACHE).build();
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

    Chart createChart(final String statType, final int width, final int height, final User user, final SearchRequest searchRequest, final boolean inline)
    {
        final ChartFactory.ChartContext context = new ChartFactory.ChartContext(user, searchRequest, width, height, inline);
        return chartFactory.generatePieChart(context, statType);
    }

    DataRow[] getData(final String statType, Map<String, Object> chartParams)
    {
        final CategoryURLGenerator completeUrlGenerator = (CategoryURLGenerator) chartParams.get(KEY_URL_GENERATOR);
        final CategoryDataset completeDataset = (CategoryDataset) chartParams.get(KEY_DATASET);
        final DataRow[] data = generateDataSet(completeDataset, completeUrlGenerator);
        sort(statType, data);
        return data;
    }

    void sort(final String statType, final DataRow[] data)
    {
        Arrays.sort(data, new DataSorter(statisticTypesResource.getStatsMapper(statType).getComparator()));
    }

    DataRow[] generateDataSet(CategoryDataset dataset, CategoryURLGenerator urlGenerator)
    {
        final DataRow[] data = new DataRow[dataset.getColumnCount()];
        // header
        for (int col = 0; col < dataset.getColumnCount(); col++)
        {
            Comparable key = dataset.getColumnKey(col);
            int val = dataset.getValue(0, col).intValue();
            String url = urlGenerator.generateURL(dataset, 0, col);
            int percentage = dataset.getValue(1, col).intValue();
            data[col] = new DataRow(key, url, val, percentage);
        }

        return data;
    }

    String validateStatType(final Collection<ValidationError> errors, final String statType)
    {
        final String displayName = statisticTypesResource.getDisplayName(statType);

        if (StringUtils.isBlank(displayName))
        {
            errors.add(new ValidationError(STAT_TYPE, "gadget.common.invalid.stat.type", statType));
        }

        return displayName;
    }

    /**
     * Ensures all parameters are valid for the Pie Chart
     *
     * @param queryString a filter id (starts with "filter-") or project id (starts with "project-") or jql (starts with
     *                    "jql-")
     * @param statType    a valid statistic type.  See {@link StatisticTypesResource}
     * @return a Collection of {@link com.atlassian.jira.rest.v1.model.errors.ValidationError}.  Or empty list if no
     *         errors.
     */
    @GET
    @Path ("validate")
    public Response validatePieChart(@QueryParam (SEARCH_QUERY) String queryString, @QueryParam (STAT_TYPE) final String statType)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        getSearchRequestAndValidate(queryString, errors, new HashMap<String, Object>());
        validateStatType(errors, statType);
        return createValidationResponse(errors);
    }

    class DataSorter implements Comparator<DataRow>
    {
        final Comparator comparator;

        public DataSorter(final Comparator comparator)
        {
            this.comparator = comparator;
        }

        public int compare(final DataRow dataRow, final DataRow dataRow1)
        {
            if (dataRow == null)
            {
                if (dataRow1 == null)
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
            else if (dataRow1 == null)
            {
                return -1;
            }
            else
            {
                return comparator.compare(((PieSegmentWrapper) dataRow.getRawKey()).getKey(), ((PieSegmentWrapper) dataRow1.getRawKey()).getKey());
            }
        }
    }

    ///CLOVER:OFF
    /**
     * A simple bean contain all information required to render the Pie Chart
     */
    @XmlRootElement
    public static class PieChart
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
        // The number issues included
        @XmlElement
        private long issueCount;
        // The Statistic Type to categorize by
        @XmlElement
        private String statType;
        // The image map for the chart
        @XmlElement
        private String imageMap;
        // The name ofr the image map
        @XmlElement
        private String imageMapName;
        @XmlElement
        private DataRow[] data;
        // The name ofr the image map
        @XmlElement
        private int height;
        @XmlElement
        private int width;
        @XmlElement
        private String base64Image;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private PieChart()
        {}

        PieChart(String location, String filterTitle, String filterUrl, long issueCount, String statType, String imageMap, String imageMapName, DataRow[] data, int width, int height, String base64Image)
        {
            this.location = location;
            this.filterTitle = filterTitle;
            this.filterUrl = filterUrl;
            this.issueCount = issueCount;
            this.statType = statType;
            this.imageMap = imageMap;
            this.imageMapName = imageMapName;
            this.data = data;
            this.width = width;
            this.height = height;
            this.base64Image = base64Image;
        }
    }

    @XmlRootElement
    //have to define a namespace here, since there's other 'DataRow' JAXB beans
    @XmlType (namespace = "com.atlassian.jira.gadgets.system.PieChartResource")
    public static class DataRow
    {
        private Comparable key;
        @XmlElement
        private String url;
        @XmlElement
        private int value;
        @XmlElement
        private int pecentage;
        @XmlElement(name="key")
        private String keyString;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        public DataRow()
        {}

        DataRow(final Comparable key, final String url, final int value, final int pecentage)
        {
            this.key = key;
            this.url = url;
            this.value = value;
            this.pecentage = pecentage;
            this.keyString = key.toString();
        }

        public Comparable getRawKey()
        {
            return key;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final DataRow dataRow = (DataRow) o;

            if (pecentage != dataRow.pecentage)
            {
                return false;
            }
            if (value != dataRow.value)
            {
                return false;
            }
            if (key != null ? !key.equals(dataRow.key) : dataRow.key != null)
            {
                return false;
            }
            if (url != null ? !url.equals(dataRow.url) : dataRow.url != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (url != null ? url.hashCode() : 0);
            result = 31 * result + value;
            result = 31 * result + pecentage;
            return result;
        }
    }
    ///CLOVER:ON
}
