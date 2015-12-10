package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.api.messages.TextMessage;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MapBuilder;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.TimeSeriesCollection;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

/**
 * Unit Test for REST endpoint of average age chart resource, {@link com.atlassian.jira.gadgets.system.AverageAgeChartResource}
 *
 * @since v4.0
 */
public class TestAverageAgeChartResource extends ResourceTest
{
    @Mock private ChartUtils mockChartUtils;
    @Mock private JiraAuthenticationContext mockAuthCtx;
    @Mock private SearchService mockSearchService;
    @Mock private PermissionManager mockPermissionManager;
    @Mock private ChartFactory mockChartFactory;
    @Mock private ApplicationProperties mockApplicationProperties;
    @Mock private TimeZoneManager timeZoneManager;

    private User mockUser = new MockUser("fred");

    private MockSearchQueryBackedResource createInstance()
    {
        return new MockSearchQueryBackedResource(mockChartUtils, mockAuthCtx,
                mockSearchService, mockPermissionManager, mockChartFactory, mockApplicationProperties, timeZoneManager);
    }

    public final void testValidateChart_rosyScenario()
    {
        String query = "blah-100";
        String days = "30";
        String periodName = "daily";
        String expectedQuery = "blah-100";
        _testValidateChart_ok(query, days, periodName, expectedQuery);
    }

    public final void testValidateChart_defaultToFilter()
    {
        String query = "100";
        String days = "30";
        String periodName = "daily";
        String expectedQuery = "filter-100";
        _testValidateChart_ok(query, days, periodName, expectedQuery);
    }

    private void _testValidateChart_ok(final String query, final String days, final String periodName,
            final String expectedQuery)
    {
        MockSearchQueryBackedResource instance = createInstance();
        instance.setExpectedQueryString(expectedQuery);

        expect(mockApplicationProperties.getDefaultBackedString("jira.chart.days.previous.limit." + periodName))
                .andReturn(days);

        replayAll();

        Response actual = instance.validateChart(query, days, periodName);

        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), actual);

        verifyAll();
    }

    public final void testValidateChart_badQuery()
    {
        final String query = "bad-query";
        String days = "10";
        String periodName = "daily";
        ValidationError error = new ValidationError("foo", "bar");

        expect(mockApplicationProperties.getDefaultBackedString("jira.chart.days.previous.limit." + periodName))
                .andReturn(days);

        _testValidateChart_fail(query, days, periodName, error, true);
    }

    public final void testValidateChart_invalidPeriodName()
    {
        String query = "good-query";
        String days = "10";
        String periodName = "rarely";
        ValidationError error = new ValidationError("periodName", "gadget.common.invalid.period");
        _testValidateChart_fail(query, days, periodName, error, false);
    }

    public final void testValidateChart_negativeDays()
    {
        String query = "good-query";
        String days = "-1";
        String periodName = "daily";
        ValidationError error = new ValidationError("daysprevious", "gadget.common.negative.days");
        _testValidateChart_fail(query, days, periodName, error, false);
    }

    public final void testValidateChart_nonNumericDays()
    {
        String query = "good-query";
        String days = "nan";
        String periodName = "daily";
        ValidationError error = new ValidationError("daysprevious", "gadget.common.days.nan");
        _testValidateChart_fail(query, days, periodName, error, false);
    }

    private void _testValidateChart_fail(String expectedQuery, String days, String periodName, ValidationError expectedError, boolean isSearchError)
    {
        MockSearchQueryBackedResource instance = createInstance();
        instance.setExpectedQueryString(expectedQuery);
        if (isSearchError)
        {
            instance.addErrorsToReturn(expectedError);
        }

        replayAll();

        Response actual = instance.validateChart(expectedQuery, days, periodName);

        assertEquals(Response.status(400).entity(ErrorCollection.Builder.newBuilder(expectedError).build()).build(),
                actual);

        verifyAll();
    }

    public final void testGenerateChart_rosyScenarioWithNoReturnData()
    {
        String query = "blah-100";
        int days = 30;
        String periodName = "daily";
        String expectedQuery = "blah-100";
        _testGenerateChart_ok(query, days, periodName, expectedQuery, null);
    }

    public final void testGenerateChart_rosyScenarioWithReturnData()
    {
        String query = "blah-100";
        int days = 30;
        String periodName = "daily";
        String expectedQuery = "blah-100";
        TimeChart.TimeDataRow[] data = new TimeChart.TimeDataRow[2];
        data[0] = new TimeChart.TimeDataRow("daily", 3, "issueLink", 50, 3);
        data[1] = new TimeChart.TimeDataRow("monthly", 90, "issueLink", 100, 12);
        _testGenerateChart_ok(query, days, periodName, expectedQuery, data);
    }

    public final void testGenerateChart_defaultToFilter()
    {
        String query = "100";
        int days = 30;
        String periodName = "daily";
        String expectedQuery = "filter-100";
        _testGenerateChart_ok(query, days, periodName, expectedQuery, null);
    }

    private void _testGenerateChart_ok(final String query, final int days, final String periodName,
            final String expectedQuery, final TimeChart.TimeDataRow[] expectedReturnData)
    {
        expect(mockAuthCtx.getLoggedInUser()).andReturn(mockUser);

        TimeSeriesCollection timeSeries = new TimeSeriesCollection();
        XYURLGenerator mockUrlGenerator = mock(XYURLGenerator.class);
        MockSearchQueryBackedResource instance = createInstance();
        instance.setExpectedQueryString(expectedQuery);
        instance.setExpectedTimeSeriesCollection(timeSeries);
        instance.setExpectedXyurlGenerator(mockUrlGenerator);
        instance.setTimeChartDataSet(expectedReturnData);

        HttpServletRequest req = stubOutHttpServletRequest();

        expect(mockApplicationProperties.getDefaultBackedString("jira.chart.days.previous.limit." + periodName))
                .andReturn(String.valueOf(days));

        expect(mockChartFactory.generateAverageAgeChart(
                isA(ChartFactory.ChartContext.class), eq(days), eq(ChartFactory.PeriodName.daily))).
                andReturn(new Chart("location", "imageMap", "imageMapName",
                        MapBuilder.<String, Object>newBuilder().
                                add("completeDataset", timeSeries).
                                add("base64Image", "base64Image==").
                                add("completeDatasetUrlGenerator", mockUrlGenerator).toHashMap()));

        replayAll();

        Response actual = instance.generateChart(req, query, String.valueOf(days), periodName,
                expectedReturnData != null, 400, 250, true);

        assertEquals(Response.ok(
                new TimeChart(
                        "location", "filterTitle", "filterUrl", "imageMap", "imageMapName", expectedReturnData, 400, 250, "base64Image==")).build(),
                actual);

        verifyAll();
    }

    public final void testGenerateChart_badQuery()
    {
        final String query = "bad-query";
        String days = "10";
        String periodName = "daily";
        ValidationError error = new ValidationError("foo", "bar");

        expect(mockApplicationProperties.getDefaultBackedString("jira.chart.days.previous.limit." + periodName))
                .andReturn(days);

        _testGenerateChart_fail(query, days, periodName, error, true);
    }

    public final void testGenerateChart_invalidPeriodName()
    {
        String query = "good-query";
        String days = "10";
        String periodName = "rarely";
        ValidationError error = new ValidationError("periodName", "gadget.common.invalid.period");
        _testGenerateChart_fail(query, days, periodName, error, false);
    }

    public final void testGenerateChart_negativeDays()
    {
        String query = "good-query";
        String days = "-1";
        String periodName = "daily";
        ValidationError error = new ValidationError("daysprevious", "gadget.common.negative.days");
        _testGenerateChart_fail(query, days, periodName, error, false);
    }

    public final void testGenerateChart_nonNumericDays()
    {
        String query = "good-query";
        String days = "nan";
        String periodName = "daily";
        ValidationError error = new ValidationError("daysprevious", "gadget.common.days.nan");
        _testGenerateChart_fail(query, days, periodName, error, false);
    }

    private void _testGenerateChart_fail(String expectedQuery, String days, String periodName, ValidationError expectedError, boolean isSearchError)
    {
        MockSearchQueryBackedResource instance = createInstance();
        instance.setExpectedQueryString(expectedQuery);
        if (isSearchError)
        {
            instance.addErrorsToReturn(expectedError);
        }
        expect(mockAuthCtx.getLoggedInUser()).andReturn(mockUser);
        HttpServletRequest req = stubOutHttpServletRequest();

        replayAll();

        Response actual = instance.generateChart(req, expectedQuery, days, periodName, false, 400, 250, true);

        assertEquals(Response.status(400).entity(ErrorCollection.Builder.newBuilder(expectedError).build()).build(),
                actual);

        verifyAll();
    }

    private HttpServletRequest stubOutHttpServletRequest()
    {
        // cannot use jira's MockHttpServletRequest as it is not exported in the jar
        HttpServletRequest req = mock(HttpServletRequest.class);
        expect(req.getServletPath()).andReturn("servletPath").anyTimes();
        expect(req.getRequestURL()).andReturn(new StringBuffer("requestUrl")).anyTimes();
        expect(req.getQueryString()).andReturn("queryString").anyTimes();
        expect(req.getParameterMap()).andReturn(new HashMap()).anyTimes();
        expect(req.getScheme()).andReturn("http").anyTimes();
        expect(req.getServerName()).andReturn("localhost").anyTimes();
        expect(req.getServerPort()).andReturn(8090).anyTimes();
        expect(req.getContextPath()).andReturn("jira").anyTimes();
        return req;
    }

    static class MockSearchQueryBackedResource extends AverageAgeChartResource
    {
        private String expectedQueryString;
        private Collection<ValidationError> errorsToReturn = new ArrayList<ValidationError>();

        private TimeSeriesCollection expectedTimeSeriesCollection;
        private XYURLGenerator expectedXyurlGenerator;
        private TimeChart.TimeDataRow[] timeChartDataSet;

        MockSearchQueryBackedResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext, final SearchService searchService, final PermissionManager permissionManager, ChartFactory chartFactory, final ApplicationProperties applicationProperties, TimeZoneManager timeZoneManager)
        {
            super(chartFactory, chartUtils, authenticationContext, permissionManager, searchService, null, applicationProperties, timeZoneManager);
        }

        public void setExpectedQueryString(String anExpectedQueryString)
        {
            this.expectedQueryString = anExpectedQueryString;
        }

        public void addErrorsToReturn(ValidationError... errorsToAdd)
        {
            if (errorsToAdd != null)
            {
                errorsToReturn.addAll(asList(errorsToAdd));
            }
        }

        public void setExpectedTimeSeriesCollection(final TimeSeriesCollection expectedTimeSeriesCollection)
        {
            this.expectedTimeSeriesCollection = expectedTimeSeriesCollection;
        }

        public void setExpectedXyurlGenerator(final XYURLGenerator expectedXyurlGenerator)
        {
            this.expectedXyurlGenerator = expectedXyurlGenerator;
        }

        public void setTimeChartDataSet(TimeChart.TimeDataRow[] data)
        {
            this.timeChartDataSet = data;
        }

        @Override
        protected SearchRequest getSearchRequestAndValidate(final String queryString, final Collection<ValidationError> errors, final Map<String, Object> params)
        {
            assertEquals(expectedQueryString, queryString);
            assertNotNull(errors);
            assertNotNull(params);
            errors.addAll(errorsToReturn);
            return new SearchRequest();
        }

        @Override
        protected String getFilterTitle(final Map<String, Object> params)
        {
            return "filterTitle";
        }

        @Override
        protected String getFilterUrl(final Map<String, Object> params)
        {
            return "filterUrl";
        }

        @Override
        TimeChart.TimeDataRow[] generateTimeChartDataSet(final TimeSeriesCollection dataSet, final XYURLGenerator urlGenerator)
        {
            assertSame(expectedTimeSeriesCollection, dataSet);
            assertSame(expectedXyurlGenerator, urlGenerator);
            return timeChartDataSet;
        }
    }
}
