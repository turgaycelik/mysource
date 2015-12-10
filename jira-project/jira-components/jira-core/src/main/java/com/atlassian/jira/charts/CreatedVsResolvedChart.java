package com.atlassian.jira.charts;

import com.atlassian.core.util.DateUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.charts.jfreechart.CreatedVsResolvedChartGenerator;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.charts.util.DataUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.transformer.ProjectSearchInputTransformer;
import com.atlassian.jira.issue.statistics.DatePeriodStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.util.OneDimensionalObjectHitCollector;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.Collector;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import static com.atlassian.jira.charts.ChartFactory.PeriodName;
import static com.atlassian.jira.charts.ChartFactory.VersionLabel;

/**
 * Produces a created vs resolved chart given a searchrequest.
 *
 * @since v4.0
 */
class CreatedVsResolvedChart
{

    private final SearchProvider searchProvider;
    private final VersionManager versionManager;
    private final IssueIndexManager issueIndexManager;
    private final IssueSearcherManager issueSearcherManager;
    private final ProjectManager projectManager;
    private final ApplicationProperties applicationProperties;
    private final SearchService searchService;
    private final TimeZoneManager timeZoneManager;

    public CreatedVsResolvedChart(SearchProvider searchProvider, VersionManager versionManager,
            IssueIndexManager issueIndexManager, IssueSearcherManager issueSearcherManager,
            ProjectManager projectManager, ApplicationProperties applicationProperties, SearchService searchService,
            TimeZoneManager timeZoneManager)
    {
        this.searchProvider = searchProvider;
        this.versionManager = versionManager;
        this.issueIndexManager = issueIndexManager;
        this.issueSearcherManager = issueSearcherManager;
        this.projectManager = projectManager;
        this.applicationProperties = applicationProperties;
        this.searchService = searchService;
        this.timeZoneManager = timeZoneManager;
    }

    public Chart generateChart(final User remoteUser, final SearchRequest searchRequest, int days, final PeriodName periodName, VersionLabel versionLabel, final boolean cumulative,
            boolean showUnresolvedTrend, int width, int height)
    {
        return generateChartInternal(remoteUser, searchRequest, days, periodName, versionLabel, cumulative, showUnresolvedTrend, width, height, false);
    }

    public Chart generateInlineChart(final User remoteUser, final SearchRequest searchRequest, int days, final PeriodName periodName, VersionLabel versionLabel, final boolean cumulative,
            boolean showUnresolvedTrend, int width, int height)
    {
        return generateChartInternal(remoteUser, searchRequest, days, periodName, versionLabel, cumulative, showUnresolvedTrend, width, height, true);
    }

    private Chart generateChartInternal(final User remoteUser, final SearchRequest searchRequest, int days, final PeriodName periodName, VersionLabel versionLabel, final boolean cumulative,
            boolean showUnresolvedTrend, int width, int height, boolean inline)
    {
        Assertions.notNull("searchrequest", searchRequest);

        days = DataUtils.normalizeDaysValue(days, periodName);

        try
        {
            final Map<String, Object> params = new HashMap<String, Object>();

            final Query query = searchRequest.getQuery();
            List domainMarkers = null;
            final IssueSearcher projectSearcher = issueSearcherManager.getSearcher(IssueFieldConstants.PROJECT);
            final ProjectSearchInputTransformer searchInputTransformer = (ProjectSearchInputTransformer) projectSearcher.getSearchInputTransformer();
            final Set<String> projectIds = searchInputTransformer.getIdValuesAsStrings(remoteUser, query);
            if (projectIds != null && !VersionLabel.none.equals(versionLabel))
            {
                domainMarkers = getDomainMarkers(projectIds, days, periodName, versionLabel);
            }

            JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder(query);
            JqlClauseBuilder whereClauseBuilder = jqlQueryBuilder.where().defaultAnd();
            whereClauseBuilder.createdAfter("-" + days + "d");
            final Map<RegularTimePeriod, Number> createdDataMap = getCreatedIssues(whereClauseBuilder.buildQuery(), remoteUser, periodName);

            jqlQueryBuilder = JqlQueryBuilder.newBuilder(query);
            whereClauseBuilder = jqlQueryBuilder.where().defaultAnd();
            whereClauseBuilder.resolutionDateAfter("-" + days + "d");
            final Map<RegularTimePeriod, Number> resolvedDataMap = getResolvedIssues(whereClauseBuilder.buildQuery(), remoteUser, periodName);

            params.put("numCreatedIssues", DataUtils.getTotalNumber(createdDataMap));
            params.put("numResolvedIssues", DataUtils.getTotalNumber(resolvedDataMap));

            final Class timePeriodClass = ChartUtil.getTimePeriodClass(periodName);
            DataUtils.normaliseDateRangeCount(createdDataMap, days - 1, timePeriodClass, timeZoneManager.getLoggedInUserTimeZone()); // only need to do one map as normalising both map keys will fix second
            DataUtils.normaliseMapKeys(createdDataMap, resolvedDataMap);

            // calculate trend of unresolved as a difference of the existing maps
            final Map<RegularTimePeriod, Number> unresolvedTrendDataMap = new TreeMap<RegularTimePeriod, Number>();
            if (showUnresolvedTrend)
            {
                int unresolvedTrend = 0;
                for (RegularTimePeriod key : createdDataMap.keySet())
                {
                    Integer created = (Integer) createdDataMap.get(key);
                    Integer resolved = (Integer) resolvedDataMap.get(key);

                    unresolvedTrend = unresolvedTrend + created - resolved;
                    unresolvedTrendDataMap.put(key, unresolvedTrend);
                }
            }

            final I18nBean i18nBean = getI18nBean(remoteUser);
            String created = i18nBean.getText("issue.field.created");
            String resolved = i18nBean.getText("portlet.createdvsresolved.resolved");
            String unresolvedTrend = i18nBean.getText("portlet.createdvsresolved.trendOfUnresolved");
            Map[] dataMaps = showUnresolvedTrend ? new Map[] { createdDataMap, resolvedDataMap, unresolvedTrendDataMap } : new Map[] { createdDataMap, resolvedDataMap };
            String[] seriesNames = showUnresolvedTrend ? new String[] { created, resolved, unresolvedTrend } : new String[] { created, resolved };
            CategoryDataset dataset = getCategoryDataset(dataMaps, seriesNames);

            if (cumulative)
            {
                DataUtils.makeCumulative(createdDataMap);
                DataUtils.makeCumulative(resolvedDataMap);
            }

            XYDataset createdVsResolved = generateTimeSeriesXYDataset(created, createdDataMap, resolved, resolvedDataMap);
            TimeSeries trendSeries = null;
            if (showUnresolvedTrend)
            {
                trendSeries = createTimeSeries(unresolvedTrend, unresolvedTrendDataMap);
            }
            ChartHelper helper = new CreatedVsResolvedChartGenerator(createdVsResolved, trendSeries, domainMarkers, i18nBean).generateChart();

            XYPlot plot = (XYPlot) helper.getChart().getPlot();
            XYItemRenderer renderer = plot.getRenderer();
            renderer.setToolTipGenerator(new StandardXYToolTipGenerator("{0} {2} " + i18nBean.getText("portlet.createdvsresolved.tooltip.issues"), NumberFormat.getNumberInstance(), NumberFormat.getNumberInstance()));
            final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
            XYURLGenerator xyurlGenerator = new XYURLGenerator()
            {
                public String generateURL(XYDataset xyDataset, int series, int item)
                {
                    final TimeSeriesCollection timeSeriesCollection = (TimeSeriesCollection) xyDataset;
                    //only display links if the chart is not cumulative.  If it's cumulative, links don't make sense!
                    if (!cumulative && series < timeSeriesCollection.getSeriesCount())
                    {
                        final TimeSeries timeSeries = timeSeriesCollection.getSeries(series);
                        final RegularTimePeriod timePeriod = timeSeries.getTimePeriod(item);
                        StatisticsMapper mapper = null;
                        if (series == 0)
                        {
                            mapper = new DatePeriodStatisticsMapper(ChartUtil.getTimePeriodClass(periodName), DocumentConstants.ISSUE_CREATED, getTimeZone());
                        }
                        else if (series == 1)
                        {
                            mapper = new DatePeriodStatisticsMapper(ChartUtil.getTimePeriodClass(periodName), DocumentConstants.ISSUE_RESOLUTION_DATE, getTimeZone());
                        }
                        if (mapper != null)
                        {
                            final SearchRequest searchUrlSuffix = mapper.getSearchUrlSuffix(timePeriod, searchRequest);
                            return velocityRequestContext.getCanonicalBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(remoteUser, (searchUrlSuffix == null) ? new QueryImpl() : searchUrlSuffix.getQuery());
                        }
                    }
                    return null;
                }
            };
            renderer.setURLGenerator(xyurlGenerator);

            if (inline)
            {
                helper.generateInline(width, height);
            }
            else
            {
                helper.generate(width, height);
            }

            params.put("chart", helper.getLocation());
            params.put("daysPrevious", days);
            params.put("chartDataset", createdVsResolved);
            params.put("trendSeries", trendSeries);
            params.put("completeDataset", dataset);
            params.put("completeDatasetUrlGenerator", xyurlGenerator);
            params.put("period", periodName.toString());
            params.put("cumulative", Boolean.toString(cumulative));
            params.put("showUnresolvedTrend", Boolean.toString(showUnresolvedTrend));
            params.put("versionLabels", versionLabel.toString());
            params.put("imagemap", helper.getImageMap());
            params.put("imagemapName", helper.getImageMapName());
            params.put("imageWidth", width);
            params.put("imageHeight", height);

            if (inline)
            {
                String base64Image = ComponentAccessor.getComponent(ChartUtils.class).renderBase64Chart(helper.getImage(), "Created vs Resolved Chart");
                params.put("base64Image", base64Image);
            }
            return new Chart(helper.getLocation(), helper.getImageMap(), helper.getImageMapName(), params);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error generating chart", e);
        }
        catch (SearchException e)
        {
            throw new RuntimeException("Error generating chart", e);
        }
    }

    private TimeZone getTimeZone()
    {
        return timeZoneManager.getLoggedInUserTimeZone();
    }

    private List getDomainMarkers(Set<String> projectIds, int days, ChartFactory.PeriodName periodName, VersionLabel versionLabel)
    {
        //if we don't want versions labels, or if we don't have any project ids don't show labels.
        if (VersionLabel.none.equals(versionLabel) || projectIds.isEmpty())
        {
            return Collections.EMPTY_LIST;
        }

        final List<Long> searchedProjectIds = transformToLongs(projectIds);
        final Set<Version> versions = new HashSet<Version>();
        final Map<Long, String> projectIdToNameMapping = new HashMap<Long, String>();
        for (Long searchedProjectId : searchedProjectIds)
        {
            // JRA-18210 - we should only included unarchived versions
            versions.addAll(versionManager.getVersionsUnarchived(searchedProjectId));
            //if there's more than one project we'll want to display the project key next to the version
            //so the user knows what project a particular version corresponds to.
            if (searchedProjectIds.size() > 1)
            {
                final Project projectObj = projectManager.getProjectObj(searchedProjectId);
                if (projectObj != null)
                {
                    projectIdToNameMapping.put(searchedProjectId, projectObj.getKey());
                }
            }
        }

        final Date releasedAfter = new Date(System.currentTimeMillis() - days * DateUtils.DAY_MILLIS);
        final List<ValueMarker> markers = new ArrayList<ValueMarker>();
        final Class periodClass = ChartUtil.getTimePeriodClass(periodName);

        for (final Version version : versions)
        {
            if (version.getReleaseDate() != null && releasedAfter.before(version.getReleaseDate()))
            {
                RegularTimePeriod timePeriod = RegularTimePeriod.createInstance(periodClass, version.getReleaseDate(), RegularTimePeriod.DEFAULT_TIME_ZONE);
                ValueMarker valueMarker = new ValueMarker(timePeriod.getFirstMillisecond());
                boolean isMinorVersion = isMinorVersion(version);

                // skip minor versions
                if (VersionLabel.major.equals(versionLabel) && isMinorVersion)
                {
                    continue;
                }

                if (isMinorVersion)
                {
                    valueMarker.setPaint(Color.LIGHT_GRAY); // minor version
                    valueMarker.setStroke(new BasicStroke(1.0f));
                }
                else
                {
                    valueMarker.setPaint(Color.GRAY); // major version
                    valueMarker.setStroke(new BasicStroke(1.2f));
                    valueMarker.setLabelPaint(Color.GRAY);
                    String valueMarkerLabel = version.getName();
                    final Long projectId = version.getProjectObject().getId();
                    //if there's a mapping, use it.
                    if (projectIdToNameMapping.containsKey(projectId))
                    {
                        valueMarkerLabel = valueMarkerLabel + "[" + projectIdToNameMapping.get(projectId) + "]";
                    }
                    valueMarker.setLabel(valueMarkerLabel);
                }
                markers.add(valueMarker);
            }
        }

        return markers;
    }

    private List<Long> transformToLongs(final Set<String> projects)
    {
        final List<Long> ids = new ArrayList<Long>(projects.size());

        for (String idStr : projects)
        {
            final Long id = getValueAsLong(idStr);
            if (id != null)
            {
                ids.add(id);
            }
        }
        return ids;
    }

    private Long getValueAsLong(final String value)
    {
        try
        {
            return new Long(value);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private CategoryDataset getCategoryDataset(Map[] dataMaps, String[] seriesNames)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (dataMaps.length != seriesNames.length)
        {
            throw new IllegalArgumentException("Number of datamaps and series names must be the equal.");
        }

        for (int i = 0; i < seriesNames.length; i++)
        {
            String seriesName = seriesNames[i];
            Map data = dataMaps[i];
            for (final Object o : data.keySet())
            {
                RegularTimePeriod period = (RegularTimePeriod) o;
                dataset.addValue((Number) data.get(period), seriesName, period);
            }
        }

        return dataset;
    }

    private XYDataset generateTimeSeriesXYDataset(String series1Name, Map series1Map, String series2Name, Map series2Map)
    {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        if (series1Name != null && series1Map != null)
        {
            dataset.addSeries(createTimeSeries(series1Name, series1Map));
        }
        if (series2Name != null && series2Map != null)
        {
            dataset.addSeries(createTimeSeries(series2Name, series2Map));
        }

        return dataset;
    }

    private TimeSeries createTimeSeries(final String seriesName, final Map seriesMap)
    {
        TimeSeries series = null;

        for (final Object o : seriesMap.keySet())
        {
            RegularTimePeriod period = (RegularTimePeriod) o;

            if (series == null)
            {
                series = new TimeSeries(seriesName, period.getClass());
            }

            series.add(period, (Number) seriesMap.get(period));
        }
        return series;
    }

    private Map<RegularTimePeriod, Number> getCreatedIssues(Query query, User remoteUser, final ChartFactory.PeriodName periodName)
            throws IOException, SearchException
    {

        final StatisticsMapper createdMapper = new DatePeriodStatisticsMapper(ChartUtil.getTimePeriodClass(periodName), DocumentConstants.ISSUE_CREATED, getTimeZone());
        final Map<RegularTimePeriod, Number> result = new TreeMap<RegularTimePeriod, Number>();
        Collector hitCollector = new OneDimensionalObjectHitCollector(createdMapper, result, true);
        searchProvider.search(query, remoteUser, hitCollector);
        return result;
    }

    private Map<RegularTimePeriod, Number> getResolvedIssues(Query query, User remoteUser, final ChartFactory.PeriodName periodName)
            throws IOException, SearchException
    {
        final Map<RegularTimePeriod, Number> data = new TreeMap<RegularTimePeriod, Number>();
        StatisticsMapper resolvedMapper = new DatePeriodStatisticsMapper(ChartUtil.getTimePeriodClass(periodName), DocumentConstants.ISSUE_RESOLUTION_DATE, getTimeZone());
        Collector hitCollector = new OneDimensionalObjectHitCollector(resolvedMapper, data, true);
        searchProvider.search(query, remoteUser, hitCollector);
        return data;
    }

    private I18nBean getI18nBean(User user)
    {
        return new I18nBean(user);
    }

    //Returns whether the version is major, based on some version naming conventions. (This is an egregious major/minor
    private boolean isMinorVersion(Version version)
    {
        return StringUtils.countMatches(version.getName(), ".") > 1 ||
                StringUtils.contains(version.getName().toLowerCase(), "alpha") ||
                StringUtils.contains(version.getName().toLowerCase(), "beta");
    }
}
