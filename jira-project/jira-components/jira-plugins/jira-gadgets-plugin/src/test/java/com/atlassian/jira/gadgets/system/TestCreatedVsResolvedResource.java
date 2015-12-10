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
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

/**
 * Tests the REST endpoint of {@link com.atlassian.jira.gadgets.system.TestCreatedVsResolvedResource}.
 *
 * @since v4.0
 */
public class TestCreatedVsResolvedResource extends ResourceTest
{
    private ChartUtils mockChartUtils;
    private JiraAuthenticationContext mockAuthCtx;
    private SearchService mockSearchService;
    private PermissionManager mockPermissionManager;
    private ChartFactory mockChartFactory;
    private User mockUser = new MockUser("fred");
    private ApplicationProperties mockApplicationProperties;

    @Mock
    TimeZoneManager timeZoneManager;

    public void setUp() throws Exception
    {
        super.setUp();
        mockChartUtils = mockControl.createMock(ChartUtils.class);
        mockAuthCtx = mockControl.createMock(JiraAuthenticationContext.class);
        mockSearchService = mockControl.createMock(SearchService.class);
        mockPermissionManager = mockControl.createMock(PermissionManager.class);
        mockChartFactory = mockControl.createMock(ChartFactory.class);
        mockApplicationProperties = mockControl.createMock(ApplicationProperties.class);

        expect(timeZoneManager.getLoggedInUserTimeZone()).andStubReturn(TimeZone.getDefault());
    }

    private MockSearchQueryBackedResource createInstance()
    {
        return new MockSearchQueryBackedResource(mockChartUtils, mockAuthCtx,
                mockSearchService, mockPermissionManager, mockChartFactory, mockApplicationProperties);
    }

    public final void testValidateChart_rosyScenario()
    {
        String query = "blah-100";
        String days = "30";
        String periodName = "daily";
        String versionLabel = "major";
        String expectedQuery = "blah-100";
        _testValidateChart_ok(query, days, periodName, versionLabel, expectedQuery);
    }

    public final void testValidateChart_defaultToFilter()
    {
        String query = "100";
        String days = "30";
        String periodName = "daily";
        String versionLabel = "major";
        String expectedQuery = "filter-100";
        _testValidateChart_ok(query, days, periodName, versionLabel, expectedQuery);
    }

    private void _testValidateChart_ok(final String query, final String days, final String periodName,
            final String versionLabel, final String expectedQuery)
    {
        MockSearchQueryBackedResource instance = createInstance();
        instance.setExpectedQueryString(expectedQuery);

        expect(mockApplicationProperties.getDefaultBackedString("jira.chart.days.previous.limit." + periodName))
                .andReturn(days);

        replayAll();

        Response actual = instance.validateChart(query, days, periodName, versionLabel);

        assertEquals(Response.ok(new TextMessage("No input validation errors found.")).cacheControl(NO_CACHE).build(), actual);

        verifyAll();
    }

    public final void testValidateChart_badQuery()
    {
        final String query = "bad-query";
        String days = "10";
        String periodName = "daily";
        String versionLabel = "major";
        ValidationError error = new ValidationError("foo", "bar");

        expect(mockApplicationProperties.getDefaultBackedString("jira.chart.days.previous.limit." + periodName))
                .andReturn(days);

        _testValidateChart_fail(query, days, periodName, versionLabel, error, true);
    }

    public final void testValidateChart_invalidPeriodName()
    {
        String query = "good-query";
        String days = "10";
        String periodName = "rarely";
        String versionLabel = "major";
        ValidationError error = new ValidationError("periodName", "gadget.common.invalid.period");
        _testValidateChart_fail(query, days, periodName, versionLabel, error, false);
    }

    public final void testValidateChart_negativeDays()
    {
        String query = "good-query";
        String days = "-1";
        String periodName = "daily";
        String versionLabel = "major";
        ValidationError error = new ValidationError("daysprevious", "gadget.common.negative.days");
        _testValidateChart_fail(query, days, periodName, versionLabel, error, false);
    }

    public final void testValidateChart_nonNumericDays()
    {
        String query = "good-query";
        String days = "nan";
        String periodName = "daily";
        String versionLabel = "major";
        ValidationError error = new ValidationError("daysprevious", "gadget.common.days.nan");
        _testValidateChart_fail(query, days, periodName, versionLabel, error, false);
    }

    public final void testValidateChart_invalidVersionLabel()
    {
        String query = "good-query";
        String days = "10";
        String periodName = "daily";
        String versionLabel = "snapshot";
        ValidationError error = new ValidationError("versionLabel", "gadget.created.vs.resolved.invalid.version.label");

        expect(mockApplicationProperties.getDefaultBackedString("jira.chart.days.previous.limit." + periodName))
                .andReturn(days);

        _testValidateChart_fail(query, days, periodName, versionLabel, error, false);
    }

    private void _testValidateChart_fail(final String expectedQuery, final String days, final String periodName,
            final String versionLabel, final ValidationError expectedError, final boolean isSearchError)
    {
        MockSearchQueryBackedResource instance = createInstance();
        instance.setExpectedQueryString(expectedQuery);
        if (isSearchError)
        {
            instance.addErrorsToReturn(expectedError);
        }

        replayAll();

        Response actual = instance.validateChart(expectedQuery, days, periodName, versionLabel);

        assertEquals(Response.status(400).entity(ErrorCollection.Builder.newBuilder(expectedError).build()).build(),
                actual);

        verifyAll();
    }

    public final void testGenerateChart_rosyScenarioWithNoReturnData()
    {
        String query = "blah-100";
        int days = 30;
        String periodName = "daily";
        String versionLabel = "major";
        String expectedQuery = "blah-100";
        _testGenerateChart_ok(query, days, periodName, versionLabel, false, false, expectedQuery, null);
    }

    public final void testGenerateChart_rosyScenarioWithReturnData()
    {
        String query = "blah-100";
        int days = 30;
        String periodName = "daily";
        String versionLabel = "major";
        String expectedQuery = "blah-100";
        CreatedVsResolvedResource.DataRow[] data = new CreatedVsResolvedResource.DataRow[2];
        data[0] = new CreatedVsResolvedResource.DataRow("key1", "createdUrl1", 1, "resolvedUrl1", 11, 21);
        data[1] = new CreatedVsResolvedResource.DataRow("key2", "createdUrl2", 2, "resolvedUrl1", 12, 22);
        _testGenerateChart_ok(query, days, periodName, versionLabel, false, true, expectedQuery, data);
    }

    public final void testGenerateChart_defaultToFilter()
    {
        String query = "100";
        int days = 30;
        String periodName = "daily";
        String versionLabel = "major";
        String expectedQuery = "filter-100";
        _testGenerateChart_ok(query, days, periodName, versionLabel, false, false, expectedQuery, null);
    }

    private void _testGenerateChart_ok(final String query, final int days, final String periodName,
            final String versionLabel, final boolean cumulative, final boolean showUnresolvedTrend,
            final String expectedQuery, final CreatedVsResolvedResource.DataRow[] expectedReturnData)
    {
        expect(mockAuthCtx.getLoggedInUser()).andReturn(mockUser);

        CategoryDataset dataSet = mock(CategoryDataset.class);
        XYURLGenerator mockUrlGenerator = mock(XYURLGenerator.class);
        XYDataset mockXyDataset = mock(XYDataset.class);

        if (expectedReturnData != null)
        {
            expect(dataSet.getColumnCount()).andReturn(expectedReturnData.length).atLeastOnce();

            for (int i = 0; i < expectedReturnData.length; i++)
            {
                expect(dataSet.getColumnKey(i)).andReturn(expectedReturnData[i].getKey());
                expect(dataSet.getValue(0, i)).andReturn(expectedReturnData[i].getCreatedValue());
                expect(mockUrlGenerator.generateURL(mockXyDataset, 0, i)).andReturn(expectedReturnData[i].getCreatedUrl());
                expect(dataSet.getValue(1, i)).andReturn(expectedReturnData[i].getResolvedValue());
                expect(mockUrlGenerator.generateURL(mockXyDataset, 1, i)).andReturn(expectedReturnData[i].getResolvedUrl());
                if (showUnresolvedTrend)
                {
                    expect(dataSet.getValue(2, i)).andReturn(expectedReturnData[i].getTrendCount());
                }
            }
        }

        MockSearchQueryBackedResource instance = createInstance();
        instance.setExpectedQueryString(expectedQuery);

        HttpServletRequest req = stubOutHttpServletRequest();

        expect(mockApplicationProperties.getDefaultBackedString("jira.chart.days.previous.limit." + periodName))
                .andReturn(String.valueOf(days));

        expect(mockChartFactory.generateCreatedVsResolvedChart(
                isA(ChartFactory.ChartContext.class), eq(days), eq(ChartFactory.PeriodName.daily),
                eq(ChartFactory.VersionLabel.valueOf(versionLabel)), eq(cumulative), eq(showUnresolvedTrend))).
                andReturn(new Chart("location", "imageMap", "imageMapName",
                        MapBuilder.<String, Object>newBuilder().
                                add("numCreatedIssues", 10).
                                add("numResolvedIssues", 20).
                                add("completeDataset", dataSet).
                                add("chartDataset", mockXyDataset).
                                add("base64Image", "base64Image==").
                                add("completeDatasetUrlGenerator", mockUrlGenerator).toHashMap()));

        replayAll();

        Response actual = instance.generateChart(req, query, String.valueOf(days), periodName, versionLabel, cumulative,
                showUnresolvedTrend, expectedReturnData != null, 400, 250, true);

        assertEquals(Response.ok(
                new CreatedVsResolvedResource.CreatedVsResolvedChart(
                        "location", "filterTitle", "filterUrl", 10, 20, "imageMap", "imageMapName", expectedReturnData, 400, 250, "base64Image==")).build(),
                actual);

        verifyAll();
    }

    public final void testGenerateChart_badQuery()
    {
        final String query = "bad-query";
        String days = "10";
        String periodName = "daily";
        String versionLabel = "major";
        ValidationError error = new ValidationError("foo", "bar");

        expect(mockApplicationProperties.getDefaultBackedString("jira.chart.days.previous.limit." + periodName))
                .andReturn(days);

        _testGenerateChart_fail(query, days, periodName, versionLabel, false, false, error, true);
    }

    public final void testGenerateChart_invalidPeriodName()
    {
        String query = "good-query";
        String days = "10";
        String periodName = "rarely";
        String versionLabel = "major";
        ValidationError error = new ValidationError("periodName", "gadget.common.invalid.period");
        _testGenerateChart_fail(query, days, periodName, versionLabel, false, false, error, false);
    }

    public final void testGenerateChart_negativeDays()
    {
        String query = "good-query";
        String days = "-1";
        String periodName = "daily";
        String versionLabel = "major";
        ValidationError error = new ValidationError("daysprevious", "gadget.common.negative.days");
        _testGenerateChart_fail(query, days, periodName, versionLabel, false, false, error, false);
    }

    public final void testGenerateChart_nonNumericDays()
    {
        String query = "good-query";
        String days = "nan";
        String periodName = "daily";
        String versionLabel = "major";
        ValidationError error = new ValidationError("daysprevious", "gadget.common.days.nan");
        _testGenerateChart_fail(query, days, periodName, versionLabel, false, false, error, false);
    }

    public final void testGenerateChart_invalidVersionLabel()
    {
        String query = "good-query";
        String days = "10";
        String periodName = "daily";
        String versionLabel = "snapshot";
        ValidationError error = new ValidationError("versionLabel", "gadget.created.vs.resolved.invalid.version.label");

        expect(mockApplicationProperties.getDefaultBackedString("jira.chart.days.previous.limit." + periodName))
                .andReturn(days);

        _testGenerateChart_fail(query, days, periodName, versionLabel, false, false, error, false);
    }

    private void _testGenerateChart_fail(final String expectedQuery, final String days, final String periodName,
            final String versionLabel, final boolean cumulative, final boolean showUnresolvedTrend,
            final ValidationError expectedError, final boolean isSearchError)
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

        Response actual = instance.generateChart(req, expectedQuery, days, periodName, versionLabel, cumulative,
                showUnresolvedTrend, false, 400, 250, true);

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

    class MockSearchQueryBackedResource extends CreatedVsResolvedResource
    {
        private String expectedQueryString;
        private Collection<ValidationError> errorsToReturn = new ArrayList<ValidationError>();

        MockSearchQueryBackedResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext, final SearchService searchService, final PermissionManager permissionManager, ChartFactory chartFactory, final ApplicationProperties applicationProperties)
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
    }
}
