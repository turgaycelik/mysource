package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.api.messages.TextMessage;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MapBuilder;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.isA;

/**
 * Tests REST endpoint of recently created chart resource
 *
 * @since v4.0
 */
public class TestRecentlyCreatedChartResource extends ResourceTest
{
    private ChartUtils mockChartUtils;
    private JiraAuthenticationContext mockAuthCtx;
    private SearchService mockSearchService;
    private PermissionManager mockPermissionManager;
    private ChartFactory mockChartFactory;
    private User mockUser = new MockUser("fred");
    private ApplicationProperties mockApplicationProperties;

    public void setUp() throws Exception
    {
        super.setUp();
        mockChartUtils = mockControl.createMock(ChartUtils.class);
        mockAuthCtx = mockControl.createMock(JiraAuthenticationContext.class);
        mockSearchService = mockControl.createMock(SearchService.class);
        mockPermissionManager = mockControl.createMock(PermissionManager.class);
        mockChartFactory = mockControl.createMock(ChartFactory.class);
        mockApplicationProperties = mockControl.createMock(ApplicationProperties.class);
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
        String expectedQuery = "blah-100";
        _testValidateChart_ok(query, days, periodName, expectedQuery);
    }

    public final void testValidateChart_defaultToFilter()
    {
        String query = "filter-100";
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

    public final void testGenerateChart_rosyScenario()
    {
        String query = "blah-100";
        int days = 30;
        String periodName = "daily";
        String expectedQuery = "blah-100";
        _testGenerateChart_ok(query, days, periodName, expectedQuery, null);
    }

    public final void testGenerateChart_defaultToFilter()
    {
        String query = "filter-100";
        int days = 30;
        String periodName = "daily";
        String expectedQuery = "filter-100";
        _testGenerateChart_ok(query, days, periodName, expectedQuery, null);
    }

    public void testGenerateChart_returnDataTrue()
    {
        String query = "blah-100";
        int days = 30;
        String periodName = "daily";
        String expectedQuery = "blah-100";
        RecentlyCreatedChartResource.DataRow[] data = new RecentlyCreatedChartResource.DataRow[2];
        data[0] = new RecentlyCreatedChartResource.DataRow("key1", 3, 1, "resolvedUrl1", 2, "unresolvedUrl1");
        data[1] = new RecentlyCreatedChartResource.DataRow("key2", 30, 10, "resolvedUrl1", 20, "unresolvedUrl1");
        _testGenerateChart_ok(query, days, periodName, expectedQuery, data);
    }

    private void _testGenerateChart_ok(final String query, final int days, final String periodName,
            final String expectedQuery, final RecentlyCreatedChartResource.DataRow[] expectedReturnData)
    {
        expect(mockAuthCtx.getLoggedInUser()).andReturn(mockUser);

        CategoryDataset dataSet = mock(CategoryDataset.class);
        CategoryURLGenerator mockUrlGenerator = mock(CategoryURLGenerator.class);

        if (expectedReturnData != null)
        {
            expect(dataSet.getColumnCount()).andReturn(expectedReturnData.length).atLeastOnce();

            for (int i = 0; i < expectedReturnData.length; i++)
            {
                expect(dataSet.getColumnKey(i)).andReturn(expectedReturnData[i].getKey());
                expect(dataSet.getValue(0, i)).andReturn(expectedReturnData[i].getUnresolvedValue());
                expect(mockUrlGenerator.generateURL(dataSet, 0, i)).andReturn(expectedReturnData[i].getUnresolvedUrl());
                expect(dataSet.getValue(1, i)).andReturn(expectedReturnData[i].getResolvedValue());
                expect(mockUrlGenerator.generateURL(dataSet, 1, i)).andReturn(expectedReturnData[i].getResolvedUrl());
            }
        }

        MockSearchQueryBackedResource instance = createInstance();
        instance.setExpectedQueryString(expectedQuery);

        HttpServletRequest req = stubOutHttpServletRequest();

        expect(mockApplicationProperties.getDefaultBackedString("jira.chart.days.previous.limit." + periodName))
                .andReturn(String.valueOf(days));

        expect(mockChartFactory.generateRecentlyCreated(
                isA(ChartFactory.ChartContext.class), eq(days), eq(ChartFactory.PeriodName.daily))).
                andReturn(new Chart("location", "imageMap", "imageMapName",
                        MapBuilder.<String, Object>newBuilder().
                                add("numIssues", 3).
                                add("completeDataset", dataSet).
                                add("completeDatasetUrlGenerator", mockUrlGenerator).
                                add("width",400).
                                add("base64Image", "base64Image==").
                                add("height",250).toHashMap()));

        replayAll();

        Response actual = instance.generateChart(req, query, String.valueOf(days), periodName, expectedReturnData != null, 400, 250, true);

        assertEquals(Response.ok(
                new RecentlyCreatedChartResource.RecentlyCreatedChart(
                        "location", "filterTitle", "filterUrl", "imageMap", "imageMapName", 3, 400, 250, expectedReturnData, "base64Image==")).build(), actual);

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

    static class MockSearchQueryBackedResource extends RecentlyCreatedChartResource
    {
        private String expectedQueryString;
        private Collection<ValidationError> errorsToReturn = new ArrayList<ValidationError>();

        MockSearchQueryBackedResource(final ChartUtils chartUtils, final JiraAuthenticationContext authenticationContext, final SearchService searchService, final PermissionManager permissionManager, ChartFactory chartFactory, final ApplicationProperties applicationProperties)
        {
            super(chartUtils, authenticationContext, searchService, permissionManager, chartFactory, null, applicationProperties);
        }

        public void setExpectedQueryString(String anExpectedQueryString)
        {
            this.expectedQueryString = anExpectedQueryString;
        }

        public void addErrorsToReturn(ValidationError... errorsToAdd)
        {
            if (errorsToAdd != null)
            {
                errorsToReturn.addAll(Arrays.asList(errorsToAdd));
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
