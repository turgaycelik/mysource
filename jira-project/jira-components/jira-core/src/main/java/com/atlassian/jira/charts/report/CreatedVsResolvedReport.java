package com.atlassian.jira.charts.report;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.ChartFactory.ChartContext;
import com.atlassian.jira.charts.ChartFactory.PeriodName;
import com.atlassian.jira.charts.ChartFactory.VersionLabel;
import com.atlassian.jira.charts.jfreechart.TimePeriodUtils;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.web.action.ProjectActionSupport;

import org.apache.log4j.Logger;

import static com.atlassian.jira.charts.ChartFactory.ChartContext;
import static com.atlassian.jira.charts.ChartFactory.PeriodName;
import static com.atlassian.jira.charts.ChartFactory.REPORT_IMAGE_HEIGHT;
import static com.atlassian.jira.charts.ChartFactory.REPORT_IMAGE_WIDTH;
import static com.atlassian.jira.charts.ChartFactory.VersionLabel;

/**
 * A report showing issues created vs resolved for a given project or search request.
 */
public class CreatedVsResolvedReport extends AbstractChartReport
{
    private static final Logger log = Logger.getLogger(CreatedVsResolvedReport.class);
    private final ChartFactory chartFactory;
    private final TimeZoneManager timeZoneManager;

    public CreatedVsResolvedReport(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties,
            ProjectManager projectManager, SearchRequestService searchRequestService, ChartUtils chartUtils,
            ChartFactory chartFactory, TimeZoneManager timeZoneManager)
    {
        super(authenticationContext, applicationProperties, projectManager, searchRequestService, chartUtils);
        this.chartFactory = chartFactory;
        this.timeZoneManager = timeZoneManager;
    }

    public String generateReportHtml(final ProjectActionSupport action, final Map reqParams) throws Exception
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("report", this);
        params.put("action", action);
        params.put("user", authenticationContext.getLoggedInUser());
        params.put("timePeriods", new TimePeriodUtils(timeZoneManager));

        // Retrieve the portlet parameters
        final String projectOrFilterId = (String) reqParams.get("projectOrFilterId");
        final PeriodName periodName = PeriodName.valueOf((String) reqParams.get("periodName"));

        int days = 30;

        if (reqParams.containsKey("daysprevious"))
        {
            days = Integer.parseInt((String) reqParams.get("daysprevious"));
        }

        VersionLabel versionLabel = VersionLabel.none;
        if (reqParams.containsKey("versionLabels"))
        {
            versionLabel = VersionLabel.valueOf((String) reqParams.get("versionLabels"));
        }

        boolean cumulative = false;
        if (reqParams.containsKey("cumulative"))
        {
            cumulative = ((String) reqParams.get("cumulative")).equalsIgnoreCase("true");
        }

        boolean showUnresolvedTrend = "true".equalsIgnoreCase((String) reqParams.get("showUnresolvedTrend"));

        try
        {
            final SearchRequest request = chartUtils.retrieveOrMakeSearchRequest(projectOrFilterId, params);
            params.put("projectOrFilterId", projectOrFilterId);

            final ChartContext context = new ChartContext(authenticationContext.getLoggedInUser(), request, REPORT_IMAGE_WIDTH, REPORT_IMAGE_HEIGHT, true);
            Chart chart = chartFactory.generateCreatedVsResolvedChart(context, days, periodName, versionLabel, cumulative, showUnresolvedTrend);
            params.putAll(chart.getParameters());
        }
        catch (Exception e)
        {
            log.error("Could not create velocity parameters " + e.getMessage(), e);
        }

        return descriptor.getHtml("view", params);
    }
}