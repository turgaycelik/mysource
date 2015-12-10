package com.atlassian.jira.charts.report;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.ProjectActionSupport;

import static com.atlassian.jira.charts.ChartFactory.ChartContext;
import static com.atlassian.jira.charts.ChartFactory.PeriodName;
import static com.atlassian.jira.charts.ChartFactory.REPORT_IMAGE_HEIGHT;
import static com.atlassian.jira.charts.ChartFactory.REPORT_IMAGE_WIDTH;

/**
 * A report showing a bar chart with average open times for issues.
 *
 * @since v4.0
 */
public class RecentlyCreatedReport extends AbstractChartReport
{
    private final ChartFactory chartFactory;

    public RecentlyCreatedReport(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties,
            ProjectManager projectManager, SearchRequestService searchRequestService, ChartUtils chartUtils,
            ChartFactory chartFactory)
    {
        super(authenticationContext, applicationProperties, projectManager, searchRequestService, chartUtils);
        this.chartFactory = chartFactory;
    }

    public String generateReportHtml(ProjectActionSupport action, Map reqParams) throws Exception
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("report", this);
        params.put("action", action);
        params.put("user", authenticationContext.getLoggedInUser());

        // Retrieve the portlet parameters
        final String projectOrFilterId = (String) reqParams.get("projectOrFilterId");
        final PeriodName periodName = PeriodName.valueOf((String) reqParams.get("periodName"));

        int days = 30;

        if (reqParams.containsKey("daysprevious"))
        {
            days = Integer.parseInt((String) reqParams.get("daysprevious"));
        }

        final SearchRequest request = chartUtils.retrieveOrMakeSearchRequest(projectOrFilterId, params);
        params.put("projectOrFilterId", projectOrFilterId);

        final ChartContext context = new ChartContext(authenticationContext.getLoggedInUser(), request, REPORT_IMAGE_WIDTH,
                REPORT_IMAGE_HEIGHT, true);
        final Chart chart = chartFactory.generateRecentlyCreated(context, days, periodName);
        params.putAll(chart.getParameters());

        return descriptor.getHtml("view", params);
    }
}