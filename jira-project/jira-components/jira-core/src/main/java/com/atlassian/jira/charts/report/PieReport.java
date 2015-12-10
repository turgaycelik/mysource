package com.atlassian.jira.charts.report;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.ChartFactory.ChartContext;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.ProjectActionSupport;

import static com.atlassian.jira.charts.ChartFactory.ChartContext;
import static com.atlassian.jira.charts.ChartFactory.REPORT_IMAGE_HEIGHT;
import static com.atlassian.jira.charts.ChartFactory.REPORT_IMAGE_WIDTH;

/**
 * A report showing a pie chart by any single field of the issues for a given project or search request.
 *
 * @since v4.0
 */
public class PieReport extends AbstractChartReport
{
    private final ChartFactory chartFactory;

    public PieReport(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties,
            ProjectManager projectManager, SearchRequestService searchRequestService, ChartUtils chartUtils,
            ChartFactory chartFactory)
    {
        super(authenticationContext, applicationProperties, projectManager, searchRequestService, chartUtils);
        this.chartFactory = chartFactory;
    }

    // override the validate() method as we have no daysPrevious
    @Override
    public void validate(ProjectActionSupport action, Map params)
    {
        validateProjectOrFilterId(action, params);
    }

    public String generateReportHtml(ProjectActionSupport action, Map reqParams) throws Exception
    {
        // Generate profiling information.
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("report", this);
        params.put("action", action);
        params.put("user", authenticationContext.getLoggedInUser());

        // Retrieve the portlet parameters
        final String projectOrFilterId = (String) reqParams.get("projectOrFilterId");
        final String statisticType = (String) reqParams.get("statistictype");

        final SearchRequest request = chartUtils.retrieveOrMakeSearchRequest(projectOrFilterId, params);
        params.put("projectOrFilterId", projectOrFilterId);

        final ChartContext context =
                new ChartContext(authenticationContext.getLoggedInUser(), request, REPORT_IMAGE_WIDTH, REPORT_IMAGE_HEIGHT, true);
        final Chart chart = chartFactory.generatePieChart(context, statisticType);
        params.putAll(chart.getParameters());

        return descriptor.getHtml("view", params);
    }
}