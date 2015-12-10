package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.gadgets.system.util.ResourceDateValidator;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.SearchUnavailableException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

/**
 * REST endpoint to validate and retreive a Recent Created chart.
 *
 * @since v4.0
 */
@Path ("/timeSince")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class TimeSinceChartResource extends SearchQueryBackedResource
{
    static final String PERIOD_NAME = "periodName";
    static final String DAYS = "daysprevious";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String NUM_ISSUES = "numIssues";
    private static final String DATE_FIELD = "dateField";
    private static final String IS_CUMULATIVE = "isCumulative";
    private static final String INLINE = "inline";

    private final ChartFactory chartFactory;
    private final FieldManager fieldManager;
    private ResourceDateValidator resourceDateValidator;

    public TimeSinceChartResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext,
            final SearchService searchService, final PermissionManager permissionManager, ChartFactory chartFactory,
            FieldManager fieldManager, VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties)
    {
        this(chartUtils, authenticationContext, searchService, permissionManager, chartFactory, fieldManager, new ResourceDateValidator(applicationProperties), velocityRequestContextFactory);
    }

    public TimeSinceChartResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext, final SearchService searchService, final PermissionManager permissionManager, final ChartFactory chartFactory, final FieldManager fieldManager, final ResourceDateValidator resourceDateValidator, VelocityRequestContextFactory velocityRequestContextFactory)
    {
        super(chartUtils, authenticationContext, searchService, permissionManager, velocityRequestContextFactory);
        this.chartFactory = chartFactory;
        this.fieldManager = fieldManager;
        this.resourceDateValidator = resourceDateValidator;
    }

    /**
     * Generate a Time Since Chart Chart and returns a simple bean containing all relevent information
     *
     * @param request      The current HTTPRequest. Needed for url generation
     * @param queryString  a filter id (starts with "filter-") or project id (starts with "project-")or jql (starts with
     *                     "jql-")
     * @param dateField    The date field to calculate chart against
     * @param days         The number of days previous to go back for.  Must be positive.
     * @param periodName   The name of the period.  See - {@link com.atlassian.jira.charts.ChartFactory.PeriodName}
     * @param isCumulative Whether or not previous column is added the current column to give a total for the period.
     * @param width        the width of the chart in pixels (defaults to 400px)
     * @param height       the height of the chart in pixels (defaults to 250px)
     * @return a {@link com.atlassian.jira.gadgets.system.TimeSinceChartResource.TimeSinceChart} if all params validated
     *         else a Collection of {@link com.atlassian.jira.rest.v1.model.errors.ValidationError}
     */
    @GET
    @Path ("/generate")
    public Response generateChart(@Context HttpServletRequest request,
            @QueryParam (QUERY_STRING) String queryString,
            @QueryParam (DATE_FIELD) @DefaultValue ("created") String dateField,
            @QueryParam (DAYS) @DefaultValue ("30") final String days,
            @QueryParam (PERIOD_NAME) @DefaultValue ("daily") final String periodName,
            @QueryParam (IS_CUMULATIVE) @DefaultValue ("true") final boolean isCumulative,
            @QueryParam (WIDTH) @DefaultValue ("450") final int width,
            @QueryParam (HEIGHT) @DefaultValue ("300") final int height,
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
        final Field field = validateDateField(dateField, errors);

        if (!errors.isEmpty())
        {
            return createErrorResponse(errors);
        }

        final ChartFactory.ChartContext context = new ChartFactory.ChartContext(user, searchRequest, width, height, inline);
        try
        {
            final Chart chart = chartFactory.generateTimeSinceChart(context, validatedDays, period, isCumulative, field.getId());

            final String location = chart.getLocation();
            final String title = getFilterTitle(params);
            final String filterUrl = getFilterUrl(params);
            final Integer issueCount = (Integer) chart.getParameters().get(NUM_ISSUES);
            final String imageMap = chart.getImageMap();
            final String imageMapName = chart.getImageMapName();
            final String chartFilterUrl = (String) chart.getParameters().get("chartFilterUrl");
            final boolean isProject = params.containsKey("project");
            final TimeSinceChart timeSinceChart = new TimeSinceChart(location, title, filterUrl, imageMap, imageMapName, issueCount, field.getName(), width, height, chartFilterUrl, isProject, chart.getBase64Image());

            return Response.ok(timeSinceChart).cacheControl(NO_CACHE).build();
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

    Field validateDateField(String fieldId, Collection<ValidationError> errors)
    {
        final Field field = fieldManager.getField(fieldId);
        if (field == null)
        {
            final ValidationError error = new ValidationError(DAYS, "gadget.time.since.invalid.date.field", fieldId);
            errors.add(error);
        }
        else
        {
            if (!isDateTypeField(field))
            {
                final List<String> params = Arrays.asList(fieldId, field.getName());
                final ValidationError error = new ValidationError(DAYS, "gadget.time.since.not.date.field", params);
                errors.add(error);
            }
        }
        return field;
    }

    boolean isDateTypeField(Field field)
    {
        if (fieldManager.isCustomField(field))
        {
            final CustomFieldType customFieldType = ((CustomField) field).getCustomFieldType();
            return customFieldType instanceof DateField;
        }
        else
        {
            return field instanceof DateField;
        }
    }

    /**
     * Ensures all parameters are valid for the Recently Created Chart
     *
     * @param queryString a filter id (starts with "filter-") or project id (starts with "project-").
     * @param dateField   The date field to calculate chart against
     * @param days        The number of days previous to go back for.  Must be positive.
     * @param periodName  The name of the period.  See - {@link com.atlassian.jira.charts.ChartFactory.PeriodName}
     * @return a Collection of {@link com.atlassian.jira.rest.v1.model.errors.ValidationError}.  Or empty list if no
     *         errors.
     */
    @GET
    @Path ("/validate")
    public Response validateChart(@QueryParam (QUERY_STRING) String queryString,
            @QueryParam (DATE_FIELD) @DefaultValue ("created") String dateField,
            @QueryParam (DAYS) @DefaultValue ("30") final String days,
            @QueryParam (PERIOD_NAME) @DefaultValue ("daily") final String periodName)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();

        getSearchRequestAndValidate(queryString, errors, new HashMap<String, Object>());
        final ChartFactory.PeriodName period = resourceDateValidator.validatePeriod(PERIOD_NAME, periodName, errors);
        resourceDateValidator.validateDaysPrevious(DAYS, period, days, errors);
        validateDateField(dateField, errors);

        return createValidationResponse(errors);
    }

    ///CLOVER:OFF
    /**
     * A simple bean contain all information required to render the Recently Created Chart
     */
    @XmlRootElement
    public static class TimeSinceChart
    {
        //The URL for the filter used in the chart (usually filter + datefield >= -days_previous d)
        @XmlElement
        private String chartFilterUrl;
        //If the chart is displaying a project or filter
        @XmlElement
        private boolean isProject;
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
        private String fieldName;
        @XmlElement
        private int width;
        @XmlElement
        private int height;
        @XmlElement
        private String base64Image;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private TimeSinceChart()
        {}

        TimeSinceChart(String location, String filterTitle, String filterUrl, String imageMap, String imageMapName,
                Integer issueCount, String fieldName, int width, int height, String chartFilterUrl, boolean isProject,
                String base64Image)
        {
            this.location = location;
            this.filterTitle = filterTitle;
            this.filterUrl = filterUrl;
            this.imageMap = imageMap;
            this.imageMapName = imageMapName;
            this.issueCount = issueCount;
            this.fieldName = fieldName;
            this.width = width;
            this.height = height;
            this.chartFilterUrl = chartFilterUrl;
            this.isProject = isProject;
            this.base64Image = base64Image;
        }
    }
    ///CLOVER:ON
}
