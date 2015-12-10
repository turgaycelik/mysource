package com.atlassian.jira.charts;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.util.profiling.UtilTimerStack;

/**
 * @since v4.0
 */
public class DefaultChartFactory implements ChartFactory
{
    private final SearchProvider searchProvider;
    private final VersionManager versionManager;
    private final IssueIndexManager issueIndexManager;
    private final IssueSearcherManager issueSearcherManager;
    private final ConstantsManager constantsManager;
    private final CustomFieldManager customFieldManager;
    private final FieldManager fieldManager;
    private final SearchService searchService;
    private final ApplicationProperties applicationProperties;
    private final ProjectManager projectManager;
    private final TimeZoneManager timeZoneManager;

    public DefaultChartFactory(final SearchProvider searchProvider, final VersionManager versionManager,
            final IssueIndexManager issueIndexManager, final IssueSearcherManager issueSearcherManager,
            final ConstantsManager constantsManager, final CustomFieldManager customFieldManager,
            final FieldManager fieldManager, SearchService searchService, ApplicationProperties applicationProperties,
            final ProjectManager projectManager, TimeZoneManager timeZoneManager)
    {
        this.searchProvider = searchProvider;
        this.versionManager = versionManager;
        this.issueIndexManager = issueIndexManager;
        this.issueSearcherManager = issueSearcherManager;
        this.constantsManager = constantsManager;
        this.customFieldManager = customFieldManager;
        this.fieldManager = fieldManager;
        this.searchService = searchService;
        this.applicationProperties = applicationProperties;
        this.projectManager = projectManager;
        this.timeZoneManager = timeZoneManager;
    }

    public Chart generateCreatedVsResolvedChart(final ChartContext context, final int days, final PeriodName periodName,
            final VersionLabel versionLabel, final boolean cumulative, final boolean showUnresolvedTrend)
    {
        UtilTimerStack.push("Generating Created vs Resolved chart");
        try
        {
            final CreatedVsResolvedChart createdVsResolvedChart =
                    new CreatedVsResolvedChart(searchProvider, versionManager, issueIndexManager, issueSearcherManager,
                            projectManager, applicationProperties, searchService, timeZoneManager);
            if (context.isInline())
            {
                return createdVsResolvedChart.generateInlineChart(context.getRemoteUser(), context.getSearchRequest(), days,
                        periodName, versionLabel, cumulative, showUnresolvedTrend, context.getWidth(), context.getHeight());
            }
            return createdVsResolvedChart.generateChart(context.getRemoteUser(), context.getSearchRequest(), days,
                    periodName, versionLabel, cumulative, showUnresolvedTrend, context.getWidth(), context.getHeight());
        }
        finally
        {
            UtilTimerStack.pop("Generating Created vs Resolved chart");
        }
    }

    public Chart generateDateRangeTimeChart(final ChartContext context, final int days, final PeriodName periodName, final long yAxisTimePeriod, final String labelSuffixKey, final String dateFieldId)
    {
        UtilTimerStack.push("Generating Date Range Time chart");
        try
        {
            final DateRangeTimeChart dateRangeChart = new DateRangeTimeChart(searchProvider, issueIndexManager, searchService, applicationProperties, timeZoneManager);
            if (context.isInline())
            {
                return dateRangeChart.generateInlineChart(context.getRemoteUser(), dateFieldId, context.getSearchRequest(), days,
                        periodName, context.getWidth(), context.getHeight(), yAxisTimePeriod, labelSuffixKey);
            }
            return dateRangeChart.generateChart(context.getRemoteUser(), dateFieldId, context.getSearchRequest(), days,
                    periodName, context.getWidth(), context.getHeight(), yAxisTimePeriod, labelSuffixKey);
        }
        finally
        {
            UtilTimerStack.pop("Generating Date Range Time chart");
        }
    }

    public Chart generatePieChart(final ChartContext context, final String statisticType)
    {
        UtilTimerStack.push("Generating Pie chart");
        try
        {
            final PieChart pieChart = new PieChart(constantsManager, customFieldManager, searchService, applicationProperties);
            if (context.isInline())
            {
                return pieChart.generateInlineChart(context.getRemoteUser(), context.getSearchRequest(), statisticType, context.getWidth(), context.getHeight());
            }
            return pieChart.generateChart(context.getRemoteUser(), context.getSearchRequest(), statisticType, context.getWidth(), context.getHeight());
        }
        finally
        {
            UtilTimerStack.pop("Generating Pie chart");
        }
    }

    public Chart generateAverageAgeChart(final ChartContext context, final int days, final PeriodName periodName)
    {
        UtilTimerStack.push("Generating Average Age chart");
        try
        {
            final AverageAgeChart averageAgeChart = new AverageAgeChart(searchProvider, issueIndexManager, timeZoneManager.getLoggedInUserTimeZone());
            if (context.isInline())
            {
                return averageAgeChart.generateInlineChart(context.getRemoteUser(), context.getSearchRequest(), days, periodName, context.getWidth(), context.getHeight());
            }
            return averageAgeChart.generateChart(context.getRemoteUser(), context.getSearchRequest(), days, periodName, context.getWidth(), context.getHeight());
        }
        finally
        {
            UtilTimerStack.pop("Generating Average Age chart");
        }
    }

    public Chart generateRecentlyCreated(final ChartContext context, final int days, final PeriodName periodName)
    {
        UtilTimerStack.push("Generating Recently Created chart");
        try
        {
            final RecentlyCreatedChart recentlyCreatedChart = new RecentlyCreatedChart(searchProvider, issueIndexManager, searchService, applicationProperties, timeZoneManager);
            if (context.isInline())
            {
                return recentlyCreatedChart.generateInlineChart(context.getRemoteUser(), context.getSearchRequest(), days, periodName, context.getWidth(), context.getHeight());
            }
            return recentlyCreatedChart.generateChart(context.getRemoteUser(), context.getSearchRequest(), days, periodName, context.getWidth(), context.getHeight());
        }
        finally
        {
            UtilTimerStack.pop("Generating Recently Created chart");
        }
    }

    public Chart generateTimeSinceChart(final ChartContext context, final int days, final PeriodName periodName, final boolean cumulative, final String dateFieldId)
    {
        UtilTimerStack.push("Generating Time Since chart");
        try
        {
            final TimeSinceChart timeSinceChart = new TimeSinceChart(fieldManager, searchProvider, issueIndexManager, searchService, applicationProperties, timeZoneManager.getLoggedInUserTimeZone());
            if (context.isInline())
            {
                return timeSinceChart.generateInlineChart(context.getRemoteUser(), context.getSearchRequest(), days, periodName, context.getWidth(), context.getHeight(), cumulative, dateFieldId);
            }
            return timeSinceChart.generateChart(context.getRemoteUser(), context.getSearchRequest(), days, periodName, context.getWidth(), context.getHeight(), cumulative, dateFieldId);
        }
        finally
        {
            UtilTimerStack.pop("Generating Time Since chart");
        }
    }
}
