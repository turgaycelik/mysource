package com.atlassian.jira.charts;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.charts.jfreechart.StackedBarChartGenerator;
import com.atlassian.jira.charts.jfreechart.TimePeriodUtils;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.charts.util.DataUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.QueryOptimizer;
import com.atlassian.jira.issue.search.util.RedundantClausesQueryOptimizer;
import com.atlassian.jira.issue.statistics.DatePeriodStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.util.FieldableDocumentHitCollector;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.time.RegularTimePeriod;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Produces a chart that displays the average age issues have been open for for a certain period.
 *
 * @since v4.0
 */
class RecentlyCreatedChart
{
    private final SearchProvider searchProvider;
    private final IssueIndexManager issueIndexManager;
    private final SearchService searchService;
    private final ApplicationProperties applicationProperties;
    private final TimeZoneManager timeZoneManager;

    public RecentlyCreatedChart(SearchProvider searchProvider, IssueIndexManager issueIndexManager,
            SearchService searchService, ApplicationProperties applicationProperties, TimeZoneManager timeZoneManager)
    {
        this.searchProvider = searchProvider;
        this.issueIndexManager = issueIndexManager;
        this.searchService = searchService;
        this.applicationProperties = applicationProperties;
        this.timeZoneManager = timeZoneManager;
    }

    public Chart generateChart(final User remoteUser, final SearchRequest searchRequest, int days,
            final ChartFactory.PeriodName periodName, int width, int height)
    {
        return generateChartInternal(remoteUser, searchRequest, days, periodName, width, height, false);
    }

    public Chart generateInlineChart(final User remoteUser, final SearchRequest searchRequest, int days,
            final ChartFactory.PeriodName periodName, int width, int height)
    {
        return generateChartInternal(remoteUser, searchRequest, days, periodName, width, height, true);
    }

    private Chart generateChartInternal(final User remoteUser, final SearchRequest searchRequest, int days,
            final ChartFactory.PeriodName periodName, int width, int height, boolean inline)
    {
        Assertions.notNull("searchRequest", searchRequest);

        days = DataUtils.normalizeDaysValue(days, periodName);

        try
        {
            final TimeZone userTimeZone = timeZoneManager.getLoggedInUserTimeZone();
            final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
            final JqlClauseBuilder whereClauseBuilder = queryBuilder.where().defaultAnd();
            whereClauseBuilder.createdAfter("-" + days + "d");
            final Map<RegularTimePeriod, Number> createdResolved = new TreeMap<RegularTimePeriod, Number>();
            final Map<RegularTimePeriod, Number> createdUnresolved = new TreeMap<RegularTimePeriod, Number>();
            Class timePeriodClass = ChartUtil.getTimePeriodClass(periodName);
            Collector hitCollector = new ResolutionSplittingCreatedIssuesHitCollector(createdResolved, createdUnresolved, issueIndexManager.getIssueSearcher(), timePeriodClass, userTimeZone);
            searchProvider.search(whereClauseBuilder.buildQuery(), remoteUser, hitCollector);

            DataUtils.normaliseDateRangeCount(createdResolved, days - 1, timePeriodClass, userTimeZone); // only need to do one map as normalising keys will fix second
            DataUtils.normaliseMapKeys(createdResolved, createdUnresolved);

            final I18nBean i18nBean = new I18nBean(remoteUser);
            final Series createdUnresolvedSeries = new Series(i18nBean.getText("datacollector.createdunresolved"), createdUnresolved);
            final Series createdResolvedSeries = new Series(i18nBean.getText("datacollector.createdresolved"), createdResolved);

            CategoryDataset dataset = DataUtils.getCategoryDataset(
                    Lists.newArrayList(createdUnresolvedSeries.data, createdResolvedSeries.data),
                    new String[] { createdUnresolvedSeries.name, createdResolvedSeries.name }
            );

            final ChartHelper helper = new StackedBarChartGenerator(dataset, i18nBean.getText("common.concepts.issues"), i18nBean).generateChart();
            final JFreeChart chart = helper.getChart();
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
            renderer.setToolTipGenerator(new CategoryToolTipGenerator()
            {
                public String generateToolTip(CategoryDataset categoryDataset, int row, int col)
                {
                    String periodAsString = (String) categoryDataset.getColumnKey(col);
                    int resolved = (Integer) createdResolvedSeries.getValue(periodAsString);
                    int unresolved = (Integer) createdUnresolvedSeries.getValue(periodAsString);
                    int total = resolved + unresolved;
                    if (row == 0)
                    {
                        return periodAsString + ": " + unresolved + " / " + total + " " + i18nBean.getText("datacollector.issuesunresolved") + ".";
                    }
                    else if (row == 1)
                    {
                        return periodAsString + ": " + resolved + " / " + total + " " + i18nBean.getText("datacollector.issuesresolved") + ".";
                    }
                    return "";
                }
            });
            final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
            CategoryURLGenerator urlGenerator = new CategoryURLGenerator()
            {
                public String generateURL(CategoryDataset categoryDataset, int row, int col)
                {
                    String periodAsString = (String) categoryDataset.getColumnKey(col);
                    StatisticsMapper createdMapper = new DatePeriodStatisticsMapper(ChartUtil.getTimePeriodClass(periodName), DocumentConstants.ISSUE_CREATED, userTimeZone);
                    SearchRequest searchUrlSuffix = createdMapper.getSearchUrlSuffix(createdResolvedSeries.getTimePeriod(periodAsString), searchRequest);
                    Query query;
                    if (row == 0)
                    {
                        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(searchUrlSuffix.getQuery());
                        queryBuilder.where().and().unresolved();
                        query = queryBuilder.buildQuery();
                    }
                    else if (row == 1)
                    {
                        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(searchUrlSuffix.getQuery());
                        queryBuilder.where().and().not().unresolved();
                        query = queryBuilder.buildQuery();
                    }
                    else
                    {
                        query = searchUrlSuffix == null ? new QueryImpl() : searchUrlSuffix.getQuery();
                    }

                    QueryOptimizer optimizer = new RedundantClausesQueryOptimizer();
                    query = optimizer.optimizeQuery(query);

                    return velocityRequestContext.getCanonicalBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(remoteUser, query);

                }
            };
            renderer.setItemURLGenerator(urlGenerator);
            plot.setRenderer(renderer);
            if (inline)
            {
                helper.generateInline(width, height);
            }
            else
            {
                helper.generate(width, height);
            }
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("chart", helper.getLocation());
            params.put("chartDataset", dataset);
            params.put("completeDataset", dataset);
            params.put("completeDatasetUrlGenerator", urlGenerator);
            params.put("numIssues", DataUtils.getTotalNumber(createdResolved) + DataUtils.getTotalNumber(createdUnresolved));
            params.put("period", periodName.toString());
            params.put("imagemap", helper.getImageMap());
            params.put("imagemapName", helper.getImageMapName());
            params.put("daysPrevious", days);
            params.put("width", width);
            params.put("height", height);
            if (inline)
            {
                String base64Image = ComponentAccessor.getComponent(ChartUtils.class).renderBase64Chart(helper.getImage(), "Recently Created Chart");
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

    static class ResolutionSplittingCreatedIssuesHitCollector extends FieldableDocumentHitCollector
    {
        private final Map<RegularTimePeriod, Number> resolvedMap;
        private final Map<RegularTimePeriod, Number> unresolvedMap;
        private final Class timePeriodClass;
        private final TimeZone timeZone;
        private final FieldSelector fieldSelector = new MapFieldSelector(DocumentConstants.ISSUE_CREATED, DocumentConstants.ISSUE_RESOLUTION);

        public ResolutionSplittingCreatedIssuesHitCollector(Map<RegularTimePeriod, Number> resolvedMap, Map<RegularTimePeriod, Number> unresolvedMap, IndexSearcher searcher, Class timePeriodClass, TimeZone timeZone)
        {
            super(searcher);
            this.resolvedMap = resolvedMap;
            this.unresolvedMap = unresolvedMap;
            this.timePeriodClass = timePeriodClass;
            this.timeZone = timeZone;
        }

        @Override
        protected FieldSelector getFieldSelector()
        {
            return fieldSelector;
        }

        public void collect(Document d)
        {
            Date creationDate = LuceneUtils.stringToDate(d.get(DocumentConstants.ISSUE_CREATED));
            RegularTimePeriod period = RegularTimePeriod.createInstance(timePeriodClass, creationDate, timeZone);

            String resolution = d.get(DocumentConstants.ISSUE_RESOLUTION);
            boolean unresolved = (resolution == null || "-1".equals(resolution));
            if (unresolved)
            {
                incrementMap(unresolvedMap, period);
            }
            else
            {
                incrementMap(resolvedMap, period);
            }
        }

        private void incrementMap(Map<RegularTimePeriod, Number> map, RegularTimePeriod key)
        {
            Number count = map.get(key);
            if (count == null)
            {
                count = 0;
            }

            map.put(key, count.intValue() + 1);
        }
    }

    class Series
    {
        final Map<String, Number> data;
        final Map<String, RegularTimePeriod> columnKeyToTimePeriod;
        final String name;

        Series(String name, Map<RegularTimePeriod, Number> data)
        {
            this.name = name;
            this.data = convertDomainAxisValues(data);
            this.columnKeyToTimePeriod = mapByAxisValue(data);
        }

        ImmutableMap<String, Number> convertDomainAxisValues(Map<RegularTimePeriod, Number> data)
        {
            ImmutableMap.Builder<String, Number> result = ImmutableMap.builder();
            for (Map.Entry<RegularTimePeriod, Number> entry : data.entrySet())
            {
                result.put(new TimePeriodUtils(timeZoneManager).prettyPrint(entry.getKey()), entry.getValue());
            }

            return result.build();
        }

        ImmutableMap<String, RegularTimePeriod> mapByAxisValue(Map<RegularTimePeriod, Number> data)
        {
            ImmutableMap.Builder<String, RegularTimePeriod> result = ImmutableMap.builder();
            for (Map.Entry<RegularTimePeriod, Number> entry : data.entrySet())
            {
                result.put(new TimePeriodUtils(timeZoneManager).prettyPrint(entry.getKey()), entry.getKey());
            }

            return result.build();
        }

        public RegularTimePeriod getTimePeriod(String columnKey)
        {
            return columnKeyToTimePeriod.get(columnKey);
        }

        public Number getValue(String columnKey)
        {
            return data.get(columnKey);
        }
    }
}
