package com.atlassian.jira.charts.reports;

import java.util.Map;
import java.util.TimeZone;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.report.AbstractChartReport;
import com.atlassian.jira.charts.report.AverageAgeReport;
import com.atlassian.jira.charts.report.RecentlyCreatedReport;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestRecentlyCreatedReport extends AbstractChartReportTestCase
{
    @Mock
    private TimeZoneManager timeZoneManager;

    public AbstractChartReport getChartReport()
    {
        return new AverageAgeReport(null, applicationProperties, projectManager, searchRequestService,
                null, null, timeZoneManager);
    }

    @Test
    public void testDaysPreviousValidation()
    {
        _testDaysPreviousValidation();
    }

    @Test
    public void testProjectOrFilterIdValidation()
    {
        _testProjectOrFilterIdValidation();
    }

    @Test
    public void testFilterIdValidation()
    {
        _testFilterIdValidation();
    }

    @Test
    public void testProjectIdValidation()
    {
        _testProjectIdValidation();
    }


    @Test
    public void testGenerateReportHtml() throws Exception
    {
        final ApplicationProperties mockApplicationProperties = mock(ApplicationProperties.class);

        final JiraAuthenticationContext jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(null);

        SearchRequest mockSearchRequest = mock(SearchRequest.class);

        ChartUtils mockChartUtils = mock(ChartUtils.class);
        when(mockChartUtils.retrieveOrMakeSearchRequest(eq("filter-123"), any(Map.class))).thenReturn(mockSearchRequest);

        final Object chartObject = new Object();

        final ChartFactory mockChartFactory = mock(ChartFactory.class);
        ChartFactory.ChartContext context = new ChartFactory.ChartContext(null, mockSearchRequest,  800, 500, true);
        when(mockChartFactory.generateRecentlyCreated(context, 30, ChartFactory.PeriodName.weekly))
                .thenReturn(new Chart(null, null, null, EasyMap.build("chart", chartObject)));

        RecentlyCreatedReport recentlyCreatedReport =
                new RecentlyCreatedReport(jiraAuthenticationContext, mockApplicationProperties, null, null,
                        mockChartUtils, mockChartFactory);

        ReportModuleDescriptor mockReportModuleDescriptor = mock(ReportModuleDescriptor.class);

        when(mockReportModuleDescriptor.getHtml("view", EasyMap.build("user", null,
                "action", null, "report", recentlyCreatedReport,
                "chart", chartObject, "projectOrFilterId", "filter-123")))
            .thenReturn("<html>chart</html>");

        recentlyCreatedReport.init(mockReportModuleDescriptor);

        Map<String, Object> params = MapBuilder.<String, Object>build("projectOrFilterId", "filter-123", "periodName", "weekly");

        final String html = recentlyCreatedReport.generateReportHtml(null, params);
        Assert.assertEquals("<html>chart</html>", html);
    }

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
    }
}