package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.SearchUnavailableException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.util.ComparatorSelector;
import com.atlassian.jira.issue.statistics.util.DefaultFieldValueToDisplayTransformer;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.issue.statistics.util.ObjectToFieldValueMapper;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.QueryImpl;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
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

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST endpoint to validate and retreive a Heat Map. Similar to the pie chart gadget 
 *
 * @since v4.0
 */
@Path ("heatmap")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class HeatMapResource extends SearchQueryBackedResource
{
    private static final String SEARCH_QUERY = "projectOrFilterId";
    static final String STAT_TYPE = "statType";
    private static final String NUM_ISSUES = "numIssues";
    private static final String NUM_OCCURRENCES = "numOccurences";

    private final JiraAuthenticationContext authenticationContext;
    private final StatisticTypesResource statisticTypesResource;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final CustomFieldManager customFieldManager;
    
    static final String KEY_URL_GENERATOR = "completeDatasetUrlGenerator";
    static final String KEY_DATASET = "completeDataset";

    static final double MIN_FONT = 12.0;
    static final double MAX_FONT = 60.0;
    static final double FONT_INCREMENT = 5.0;
    static final double BIN_SIZE = 10.0;



    public HeatMapResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager,
            final StatisticTypesResource statisticTypesResource, final SearchService searchService, final VelocityRequestContextFactory velocityRequestContextFactory,
            final CustomFieldManager customFieldManager)
    {
        super(chartUtils, authenticationContext, searchService, permissionManager, velocityRequestContextFactory);
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.authenticationContext = authenticationContext;
        this.statisticTypesResource = statisticTypesResource;
        this.customFieldManager = customFieldManager;
    }

    /**
     * Generate a heat map and returns a simple bean containing all relievent information
     *
     * @param queryString a filter id (starts with "filter-") or project id (starts with "project-") or jql (starts with
     *                    "jql-")
     * @param statType    a valid statistic type.  See {@link StatisticTypesResource}
     * @return a {@link com.atlassian.jira.gadgets.system.HeatMapResource.HeatMap} if all params validated else a Collection of {@link com.atlassian.jira.rest.v1.model.errors.ValidationError}
     */
    @GET
    @Path ("/generate")
    public Response getHeatMap(@QueryParam (SEARCH_QUERY) final String queryString,
            @QueryParam (STAT_TYPE) @DefaultValue ("assignees") final String statType)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        final User user = authenticationContext.getLoggedInUser();
        final Map<String, Object> params = new HashMap<String, Object>();
        final SearchRequest searchRequest = getSearchRequestAndValidate(queryString, errors, params);

        final String displayName = validateStatType(errors, statType);

        if (!errors.isEmpty())
        {
            return createErrorResponse(errors);
        }

        try
        {
            final Map<String, Object> chartParams = createHeatMap(statType, user, searchRequest);
            final String title = getFilterTitle(params);
            final String filterUrl = getFilterUrl(params);
            final int issueCount = ((Long) chartParams.get(NUM_ISSUES)).intValue();
            final int occurrenceCount = (Integer) chartParams.get(NUM_OCCURRENCES);

            final DataRow[] data = (DataRow[]) chartParams.get(KEY_DATASET);

            final HeatMap heatMap = new HeatMap(title, filterUrl, occurrenceCount, issueCount, displayName, data);

            return Response.ok(heatMap).cacheControl(NO_CACHE).build();
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

    /***
     *
     * Generates the parameters required to render a heat map. Loosely based on
     * {@link com.atlassian.jira.charts.PieChart#PieChart(com.atlassian.jira.config.ConstantsManager, com.atlassian.jira.issue.CustomFieldManager, com.atlassian.jira.bc.issue.search.SearchService, com.atlassian.jira.config.properties.ApplicationProperties)}
     *
     * @param statType statistic type to render
     * @param user user requesting the map
     * @param searchRequest search request for the data to be used in the map
     * @return parameters of the heat map, in particular the data set and translated names among other things
     */
    Map<String, Object> createHeatMap(final String statType, final User user, final SearchRequest searchRequest)
    {

        final I18nHelper i18nBean = authenticationContext.getI18nHelper();

        try
        {
            final SearchRequest clonedSearchRequest = new SearchRequest(searchRequest.getQuery());
            final StatisticAccessorBean statBean = new StatisticAccessorBean(user, clonedSearchRequest);
            final StatisticMapWrapper<Object,Number> statWrapper = statBean.getAllFilterBy(statType);
            final StatisticsMapper statMapper = statBean.getMapper(statType);
            final FieldValueToDisplayTransformer<String> fieldValueToDisplayTransformer =
                    new DefaultFieldValueToDisplayTransformer(i18nBean, customFieldManager);

            // 1 more row for irrelevant issues, if any exist
            final int numRows = statWrapper.size() +
                    ((statWrapper.getIrrelevantCount() > 0) ? 1 : 0);
            final VelocityRequestContext velocityRequestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();

            // The rows should be sorted according to the ordering of the statType. We get the ordering
            // from the statMapper comparator
            final Comparator comparator = ComparatorSelector.getComparator(statMapper);
            final SortedMap<Object, DataRow> rowOrdering = new TreeMap<Object, DataRow>(comparator);

            //JRASEV-2933 HeatMap actually counts occurrences
            int numOccurences = 0;
            for (final Object o : statWrapper.entrySet())
            {
                final Map.Entry statistic = (Map.Entry) o;
                numOccurences += ((Number) statistic.getValue()).intValue();
            }
            numOccurences += statWrapper.getIrrelevantCount();
            
            for (final Object o : statWrapper.entrySet())
            {
                final Map.Entry statistic = (Map.Entry) o;
                final Object key = statistic.getKey();
                final int issues = ((Number) statistic.getValue()).intValue();
                final String url = createSearchUrl(user, clonedSearchRequest, statMapper, velocityRequestContext, key);
                final DataRow dataRow = createDataRow(key, fieldValueToDisplayTransformer, statType, numOccurences, url, numRows, issues);
                rowOrdering.put(key, dataRow);
            }

            // Unfortunately, irrelevant issues are indicated use an empty object for the key, and
            // the statMapper comparator will throw a fit as it will attempt to cast the object to
            // the appropriate stat type. So we do it after we have the array and put irrelevant issues
            // at the end.
            final DataRow[] dataRows = new DataRow[numRows];
            rowOrdering.values().toArray(dataRows);

            if (statWrapper.getIrrelevantCount() > 0)
            {
                final int issues = statWrapper.getIrrelevantCount();
                final DataRow dataRow = createDataRow(FilterStatisticsValuesGenerator.IRRELEVANT, fieldValueToDisplayTransformer, statType, numOccurences, null, numRows, issues);
                dataRows[rowOrdering.size()] = dataRow;
            }

            final Map<String, Object> params = new HashMap<String, Object>();
            params.put(HeatMapResource.KEY_DATASET, dataRows);
            params.put(HeatMapResource.NUM_ISSUES, statWrapper.getTotalCount());
            params.put(HeatMapResource.NUM_OCCURRENCES,numOccurences);
            params.put(HeatMapResource.STAT_TYPE, statType);
            params.put("statisticTypeI18nName", getStatisticsTypeI18nName(i18nBean, statType));

            return params;
        }
        catch (SearchException e)
        {
            throw new RuntimeException("Error generating heat map", e);
        }
    }

    String createSearchUrl(final User user, final SearchRequest clonedSearchRequest, final StatisticsMapper statMapper, final VelocityRequestContext velocityRequestContext, final Object key)
    {
        final SearchRequest searchUrlSuffix = statMapper.getSearchUrlSuffix(key, clonedSearchRequest);
        return velocityRequestContext.getCanonicalBaseUrl() +
                "/secure/IssueNavigator.jspa?reset=true" +
                searchService.getQueryString(user,
                        (searchUrlSuffix == null) ? new QueryImpl() : searchUrlSuffix.getQuery());
    }

    DataRow createDataRow(final Object key, final FieldValueToDisplayTransformer<String> fieldValueToDisplayTransformer, final String statType, final int totalIssues, final String url,
            final int numRows, final int issues)
    {
        final String name = ObjectToFieldValueMapper.transform(statType, key, url, fieldValueToDisplayTransformer);
        // We can't use the getPercentage method from the statsMapper as custom fields have a null key
        final int percentage = (100 * issues / totalIssues);
        final double fontSize = calculateFontSize(numRows, percentage);
        return new DataRow(name, url, issues, percentage, fontSize);

    }

    /**
     * Determines the size of each stat values' font .
     * Font is calculated by increasing the minimum font using a increment (fontIncrement) value,
     * the number of stat values (numberOfData) returned and the range of each bin (binSize).
     * How big each font increment is fontIncrement *  args.chart.numberOfData.
     * We round the percentage to the nearest bin and increment the font from the minimum fontsize
     * by the bin number * font increment calculated earlier.
     * The maximum font is capped by the minFont + maxFont.
     *
     * This algorithm aims to give more emphasis to values that are only slightly larger when lots of values are present.
     *
     * @param numberOfData number of stat values
     * @param percentage percentage of total this stat value constitutes
     * @return font size to be used in the generated heat map
     */
    double calculateFontSize(final int numberOfData, final double percentage) {
        return MIN_FONT +
                Math.min(MAX_FONT,(FONT_INCREMENT * numberOfData * Math.round(percentage/BIN_SIZE) * BIN_SIZE/100));

    }

    String getStatisticsTypeI18nName(final I18nHelper i18nBean, final String statisticType)
    {
        //check if it's a custom field and look up the custom field name if that's the case
        if (statisticType.startsWith(FieldManager.CUSTOM_FIELD_PREFIX))
        {
            final CustomField customField = customFieldManager.getCustomFieldObject(statisticType);
            if (customField == null)
            {
                throw new RuntimeException("No custom field with id '" + statisticType + "'");
            }
            if (customField.getCustomFieldSearcher() instanceof CustomFieldStattable)
            {
                return customField.getName();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return i18nBean.getText("gadget.filterstats.field.statistictype." + statisticType.toLowerCase());
        }
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
     * Ensures all parameters are valid for the heat map
     *
     * @param queryString a filter id (starts with "filter-") or project id (starts with "project-") or jql (starts with
     *                    "jql-")
     * @param statType    a valid statistic type.  See {@link StatisticTypesResource}
     * @return a Collection of {@link com.atlassian.jira.rest.v1.model.errors.ValidationError}.  Or empty list if no
     *         errors.
     */
    @GET
    @Path ("validate")
    public Response validateHeatMap(@QueryParam (SEARCH_QUERY) final String queryString, @QueryParam (STAT_TYPE) final String statType)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        getSearchRequestAndValidate(queryString, errors, new HashMap<String, Object>());
        validateStatType(errors, statType);
        return createValidationResponse(errors);
    }

    ///CLOVER:OFF
    /**
     * A simple bean contain all information required to render the Heat Map
     */
    @XmlRootElement
    public static class HeatMap
    {
        // The title of the chart
        @XmlElement                                                                        
        private String filterTitle;
        // The link of where to send the user to - For a project, send em to the browse project, for a filter, send em tothe Issue Nav
        @XmlElement
        private String filterUrl;
        // The number of issues in the query
        @XmlElement
        private int issueCount;
        // the occurences of the statistic in the query
        @XmlElement
        private int occurrenceCount;
        // The Statistic Type to categorize by
        @XmlElement
        private String statType;
        // DataRow for table creation
        @XmlElement
        private DataRow[] data;
        // Number of DataRow elements
        @XmlElement
        private int numberOfData;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private HeatMap()
        {}

        public HeatMap(final String filterTitle, final String filterUrl, final int occurrenceCount, final int issueCount, final String statType, final DataRow[] data)
        {
            this.filterTitle = filterTitle;
            this.filterUrl = filterUrl;
            this.issueCount = issueCount;
            this.occurrenceCount = occurrenceCount;
            this.statType = statType;
            this.data = data;
            this.numberOfData = (data == null) ? 0 : data.length - 1;
        }
    }

    @XmlRootElement
    //have to define a namespace here, since there's other 'DataRow' JAXB beans
    @XmlType (namespace = "com.atlassian.jira.gadgets.system.HeatMapResource")
    public static class DataRow
    {
        private Comparable key;
        @XmlElement
        private String url;
        @XmlElement
        private int value;
        @XmlElement
        private int percentage;
        @XmlElement
        private double fontSize;
        @XmlElement(name="key")
        private String keyString;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        public DataRow()
        {}

        DataRow(final Comparable key, final String url, final int value, final int percentage, final double fontSize)
        {
            this.key = key;
            this.url = url;
            this.value = value;
            this.percentage = percentage;
            this.fontSize = fontSize;
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

            if (percentage != dataRow.percentage)
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
            return (fontSize == dataRow.fontSize);

        }

        @Override
        public int hashCode() {
            int result;
            final long temp;
            result = key != null ? key.hashCode() : 0;
            result = 31 * result + (url != null ? url.hashCode() : 0);
            result = 31 * result + value;
            result = 31 * result + percentage;
            temp = fontSize != +0.0d ? Double.doubleToLongBits(fontSize) : 0L;
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }
    ///CLOVER:ON
}
