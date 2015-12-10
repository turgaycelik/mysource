package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.gadgets.system.util.ResourceDateValidator;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.api.messages.TextMessage;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockUser;
import org.easymock.classextension.EasyMock;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.TimeSeriesCollection;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.atlassian.jira.charts.ChartFactory.PeriodName.quarterly;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static org.easymock.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

/**
 * Unit Test for {@link com.atlassian.jira.gadgets.system.ResolutionTimeResource}
 *
 * @since v4.0
 */
public class TestResolutionTimeResource extends ResourceTest
{
    @Mock private ApplicationProperties applicationProperties;

    @Mock private TimeZoneManager timeZoneManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        replay(applicationProperties, timeZoneManager);
    }

    @Override
    protected void tearDown() throws Exception
    {
        verify(applicationProperties);
        super.tearDown();
    }

    public void testValidateFail()
    {
        final String query = "filter-foobar";
        final String testNumDays = "somedays";
        final String testPeriodName = "periodname";
        final AtomicBoolean validatedDays = new AtomicBoolean(false);
        final AtomicBoolean validatedPeriod = new AtomicBoolean(false);
        ResourceDateValidator failer = new ResourceDateValidator(applicationProperties)
        {
            @Override
            public int validateDaysPrevious(final String fieldName, final ChartFactory.PeriodName period, final String days, final Collection<ValidationError> errors)
            {
                validatedDays.set(true);
                assertEquals(ResolutionTimeResource.DAYS, fieldName);
                assertEquals(testNumDays, days);
                errors.add(new ValidationError("days field", "mock days error"));
                return -1;
            }

            @Override
            public ChartFactory.PeriodName validatePeriod(final String fieldName, final String periodName, final Collection<ValidationError> errors)
            {
                validatedPeriod.set(true);
                assertEquals(ResolutionTimeResource.PERIOD_NAME, fieldName);
                assertEquals(testPeriodName, periodName);
                errors.add(new ValidationError("period field", "mock  period error"));
                return null;
            }
        };
        ResolutionTimeResource rtr = new ResolutionTimeResource(null, null, null, null, null, failer, null, timeZoneManager)
        {
            @Override
            protected SearchRequest getSearchRequestAndValidate(final String queryString, final Collection<ValidationError> errors, final Map<String, Object> params)
            {
                assertEquals(query, queryString);
                errors.add(new ValidationError("filter field", "mock filter error"));
                return null;
            }
        };
        Response response = rtr.validate(query, testNumDays, testPeriodName);
        assertEquals(400, response.getStatus());
        List<ValidationError> expectedErrors = Arrays.asList(
                new ValidationError("filter field", "mock filter error"),
                new ValidationError("period field", "mock  period error"),
                new ValidationError("days field", "mock days error")
        );
        assertEquals(ErrorCollection.Builder.newBuilder(expectedErrors).build(), response.getEntity());
        assertTrue(validatedDays.get());
        assertTrue(validatedPeriod.get());
    }

    public void testValidateSuccess()
    {
        final String query = "filter-foobar";
        final String testNumDays = "somedays";
        final String testPeriodName = "periodname";
        final ArrayList<ValidationError> noErrors = new ArrayList<ValidationError>();

        ResourceDateValidator mock = EasyMock.createNiceMock(ResourceDateValidator.class);
        EasyMock.expect(mock.validatePeriod(ResolutionTimeResource.PERIOD_NAME, testPeriodName, noErrors)).andReturn(quarterly);
        EasyMock.expect(mock.validateDaysPrevious(ResolutionTimeResource.DAYS, quarterly, testNumDays, noErrors)).andReturn(-123);
        EasyMock.replay(mock);
        ResolutionTimeResource rtr = new ResolutionTimeResource(null, null, null, null, null, mock, null, timeZoneManager)
        {
            @Override
            protected SearchRequest getSearchRequestAndValidate(final String queryString, final Collection<ValidationError> errors, final Map<String, Object> params)
            {
                assertEquals(query, queryString);
                return null;
            }
        };
        Response response = rtr.validate(query, testNumDays, testPeriodName);
        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), response);
        EasyMock.verify(mock);
    }

    public void testGetData()
    {
        ResolutionTimeResource rtr = new ResolutionTimeResource(null, null, null, null, null, null, applicationProperties, timeZoneManager);
        HashMap<String, Object> params = new HashMap<String, Object>();
        TimeSeriesCollection dataSet = EasyMock.createMock(TimeSeriesCollection.class);
        XYURLGenerator urlGenerator = EasyMock.createMock(XYURLGenerator.class);
        params.put("completeDataset", dataSet);
        params.put("completeDatasetUrlGenerator", urlGenerator);
        Chart chart = EasyMock.createNiceMock(Chart.class);
        EasyMock.expect(chart.getParameters()).andReturn(params);
        TimeChart.Generator mockGenerator = EasyMock.createNiceMock(TimeChart.Generator.class);
        mockGenerator.generateDataSet(dataSet, urlGenerator, timeZoneManager);
        TimeChart.TimeDataRow[] dataRows = new TimeChart.TimeDataRow[0];
        EasyMock.expectLastCall().andReturn(dataRows);
        EasyMock.replay(dataSet, urlGenerator, chart, mockGenerator);

        assertTrue(dataRows == rtr.getData(mockGenerator, chart)); // deliberate reference equality assertion

        EasyMock.verify(dataSet, urlGenerator, chart, mockGenerator);
    }

    public void testCreateChart()
    {
        final String filterTitle = "theFilterTitle";
        final String filterUrl = "theFilterURL";

        ResolutionTimeResource rtr = new ResolutionTimeResource(null, null, null, null, null, null, applicationProperties, timeZoneManager)
        {
            @Override
            protected String getFilterTitle(final Map<String, Object> params)
            {
                return filterTitle;
            }

            @Override
            protected String getFilterUrl(final Map<String, Object> params)
            {
                return filterUrl;
            }
        };

        final Map<String, Object> params = new HashMap<String, Object>();
        String chartLocation = "http://one.bogus/location.png";
        final String imageMap = "<map>yadda yadda image map</map>";

        Chart chart = new Chart(chartLocation, imageMap, null, new HashMap<String, Object>());
        TimeChart timeChart = rtr.createChart(1001, 999, params, chart, null);
        assertEquals(chartLocation, timeChart.location);
        assertEquals(1001, timeChart.width);
        assertEquals(999, timeChart.height);
        assertEquals(filterTitle, timeChart.filterTitle);
        assertEquals(filterUrl, timeChart.filterUrl);
        assertNull(timeChart.getData());
    }

    public void testGetChart()
    {
        final String query = "filter-foobar";
        final String testNumDays = "somedays";
        final String testPeriodName = "periodname";
        final Collection<ValidationError> noErrors = new ArrayList<ValidationError>();
        ResourceDateValidator mockValidator = EasyMock.createMock(ResourceDateValidator.class);
        EasyMock.expect(mockValidator.validatePeriod(ResolutionTimeResource.PERIOD_NAME, testPeriodName, noErrors)).andReturn(quarterly);
        final int mockDays = -123;
        EasyMock.expect(mockValidator.validateDaysPrevious(ResolutionTimeResource.DAYS, quarterly, testNumDays, noErrors)).andReturn(mockDays);

        final Chart mockChart = EasyMock.createMock(Chart.class);
        String chartLocation = "http://bogus/chart-location.png";
        JiraAuthenticationContext mockJiraAuthenticationContext = EasyMock.createMock(JiraAuthenticationContext.class);
        final User barney = new MockUser("barney");
        EasyMock.expect(mockJiraAuthenticationContext.getLoggedInUser()).andReturn(barney);

        EasyMock.replay(mockValidator, mockChart, mockJiraAuthenticationContext);
        final TimeChart expectedChart = new TimeChart(chartLocation, null, null, null, null, null, 700, 9000, "base64Image");

        final AtomicBoolean getSearchRequestAndValidateWasCalled = new AtomicBoolean(false);
        final AtomicBoolean createChartWasCalled = new AtomicBoolean(false);
        final AtomicBoolean generateChartWasCalled = new AtomicBoolean(false);
        ResolutionTimeResource rtr = new ResolutionTimeResource(null, mockJiraAuthenticationContext, null, null, null, mockValidator, null, timeZoneManager)
        {
            @Override
            protected SearchRequest getSearchRequestAndValidate(final String queryString, final Collection<ValidationError> errors, final Map<String, Object> params)
            {
                getSearchRequestAndValidateWasCalled.set(true);
                assertEquals(query, queryString);
                return null;
            }

            @Override
            TimeChart createChart(final int width, final int height, final Map<String, Object> params, final Chart chart, final TimeChart.TimeDataRow[] data)
            {
                createChartWasCalled.set(true);
                assertEquals(chart, mockChart);
                assertNull(data);
                assertEquals(700, width);
                assertEquals(9000, height);
                return expectedChart;
            }

            @Override
            Chart generateChart(final int width, final int height, final User user, final SearchRequest searchRequest, final ChartFactory.PeriodName period, final int daysInt, boolean inline)
            {
                generateChartWasCalled.set(true);
                assertEquals(700, width);
                assertEquals(9000, height);
                assertEquals(barney, user);
                assertEquals(mockDays, daysInt);
                assertEquals(quarterly, period);
                return mockChart;
            }
        };
        Response chart = rtr.getChart(query, testNumDays, testPeriodName, false, 700, 9000, true);
        assertEquals(200, chart.getStatus());

        assertTrue(expectedChart == chart.getEntity());
        EasyMock.verify(mockValidator, mockChart, mockJiraAuthenticationContext);
        assertTrue(getSearchRequestAndValidateWasCalled.get());
        assertTrue(createChartWasCalled.get());
        assertTrue(generateChartWasCalled.get());
    }
}
