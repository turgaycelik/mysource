package com.atlassian.jira.charts.reports;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.report.AbstractChartReport;
import com.atlassian.jira.charts.report.PieReport;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestPieReport extends AbstractChartReportTestCase
{
    public AbstractChartReport getChartReport()
    {
        return new PieReport(null, null, projectManager, searchRequestService, null, null);
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
        when(mockChartFactory.generatePieChart(context, "type"))
            .thenReturn(new Chart(null, null, null, EasyMap.build("chart", chartObject)));

        PieReport pieReport =
                new PieReport(jiraAuthenticationContext, mockApplicationProperties, null, null,
                        mockChartUtils, mockChartFactory);

        ReportModuleDescriptor mockReportModuleDescriptor = mock(ReportModuleDescriptor.class);

        when(mockReportModuleDescriptor.getHtml("view", EasyMap.build("user", null,
                "action", null, "report", pieReport,
                "chart", chartObject, "projectOrFilterId", "filter-123")))
            .thenReturn("<html>chart</html>");

        pieReport.init(mockReportModuleDescriptor);

        Map<String, Object> params = MapBuilder.<String, Object>build("projectOrFilterId", "filter-123", "statistictype", "type");

        final String html = pieReport.generateReportHtml(null, params);
        Assert.assertEquals("<html>chart</html>", html);
    }
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }
}