package com.atlassian.jira.charts;

import com.atlassian.core.util.DateUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.charts.jfreechart.HistogramChartGenerator;
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
import com.atlassian.query.operator.Operator;
import com.google.common.collect.Lists;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.search.IndexSearcher;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.urls.TimeSeriesURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Produces a chart showing the average time for issues in the time period specified.  This may be used for example for
 * the resolution time chart or the time to first response chart.
 *
 * @since v4.0
 */
class DateRangeTimeChart
{
    private final SearchProvider searchProvider;
    private final IssueIndexManager issueIndexManager;
    private final SearchService searchService;
    private final ApplicationProperties applicationProperties;
    private final TimeZoneManager timeZoneManager;

    public DateRangeTimeChart(SearchProvider searchProvider, IssueIndexManager issueIndexManager,
            SearchService searchService, ApplicationProperties applicationProperties, TimeZoneManager timeZoneManager)
    {
        this.searchProvider = searchProvider;
        this.issueIndexManager = issueIndexManager;
        this.searchService = searchService;
        this.applicationProperties = applicationProperties;
        this.timeZoneManager = timeZoneManager;
    }

    public Chart generateChart(final User remoteUser, final String dateFieldId, final SearchRequest searchRequest, int days, final ChartFactory.PeriodName periodName, int width, int height, long yAxisTimePeriod, String labelSuffixKey)
    {
        return generateChartInternal(remoteUser, dateFieldId, searchRequest, days, periodName, width, height, yAxisTimePeriod, labelSuffixKey, false);
    }

    public Chart generateInlineChart(final User remoteUser, final String dateFieldId, final SearchRequest searchRequest, int days, final ChartFactory.PeriodName periodName, int width, int height, long yAxisTimePeriod, String labelSuffixKey)
    {
        return generateChartInternal(remoteUser, dateFieldId, searchRequest, days, periodName, width, height, yAxisTimePeriod, labelSuffixKey, true);
    }

    private Chart generateChartInternal(final User remoteUser, final String dateFieldId, final SearchRequest searchRequest, int days, final ChartFactory.PeriodName periodName, int width, int height, long yAxisTimePeriod, String labelSuffixKey, boolean inline)
    {
        Assertions.notNull("searchRequest", searchRequest);
        Assertions.notNull("dateFieldId", dateFieldId);

        days = DataUtils.normalizeDaysValue(days, periodName);

        try
        {
            final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
            final JqlClauseBuilder whereClauseBuilder = queryBuilder.where().defaultAnd();
            whereClauseBuilder.addStringCondition(dateFieldId, Operator.GREATER_THAN_EQUALS, "-" + days + "d");
            final TimeSeriesCollection dataset = getAverageOpenTimes(whereClauseBuilder.buildQuery(), remoteUser, periodName, days, dateFieldId, yAxisTimePeriod);
            final I18nBean i18nBean = getI18nBean(remoteUser);
            final TimeSeriesCollection chartDataset = DataUtils.reduceDataset(dataset, Lists.newArrayList(i18nBean.getText("datacollector.averageresolution")));
            final ChartHelper helper = new HistogramChartGenerator(chartDataset, i18nBean.getText(getTimePeriodi18nName(yAxisTimePeriod)), i18nBean).generateChart();

            JFreeChart chart = helper.getChart();
            XYPlot plot = (XYPlot) chart.getPlot();
            XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
            renderer.setToolTipGenerator(new StandardXYToolTipGenerator("{1}: {2} " + i18nBean.getText(labelSuffixKey),
                    new SimpleDateFormat("dd-MMMMM-yyyy", i18nBean.getLocale()), NumberFormat.getInstance()));
            final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
            XYURLGenerator urlGenerator = new TimeSeriesURLGenerator()
            {
                public String generateURL(final XYDataset xyDataset, final int row, final int col)
                {
                    TimeSeriesCollection timeSeriesCollection = (TimeSeriesCollection) xyDataset;
                    TimeSeries timeSeries = timeSeriesCollection.getSeries(row);
                    if (row == 0) // only generate urls for the first row in the dataset
                    {
                        final TimeSeriesDataItem item = timeSeries.getDataItem(col);
                        final RegularTimePeriod period = item.getPeriod();
                        final StatisticsMapper createdMapper = new DatePeriodStatisticsMapper(ChartUtil.getTimePeriodClass(periodName), dateFieldId, timeZoneManager.getLoggedInUserTimeZone());
                        final SearchRequest searchUrlSuffix = createdMapper.getSearchUrlSuffix(period, searchRequest);
                        Query query;
                        if (searchUrlSuffix == null)
                        {
                            query = new QueryImpl();
                        }
                        else
                        {
                            QueryOptimizer optimizer = new RedundantClausesQueryOptimizer();
                            query = optimizer.optimizeQuery(searchUrlSuffix.getQuery());
                        }
                        return velocityRequestContext.getCanonicalBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(remoteUser, query);
                    }
                    else
                    {
                        return null;
                    }
                }
            };
            renderer.setURLGenerator(urlGenerator);
            plot.setRenderer(renderer);

            //calculate the total number of issues being displayed
            final TimeSeries series = dataset.getSeries(0);
            @SuppressWarnings ("unchecked")
            final List<TimeSeriesDataItem> resolvedIssuesCount = series.getItems();
            Integer totalIssuesCount = 0;
            if (resolvedIssuesCount != null)
            {
                for (TimeSeriesDataItem timeSeriesDataItem : resolvedIssuesCount)
                {
                    final Number value = timeSeriesDataItem.getValue();
                    totalIssuesCount += value == null ? 0 : value.intValue();
                }
            }

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
            params.put("chartDataset", chartDataset);
            params.put("numIssues", totalIssuesCount);
            params.put("completeDataset", dataset);
            params.put("completeDatasetUrlGenerator", urlGenerator);
            params.put("daysPrevious", days);
            params.put("period", periodName.toString());
            params.put("imagemap", helper.getImageMap());
            params.put("imagemapName", helper.getImageMapName());

            if (inline)
            {
                String base64Image = ComponentAccessor.getComponent(ChartUtils.class).renderBase64Chart(helper.getImage(), "Date Range Time Chart");
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

    private TimeSeriesCollection getAverageOpenTimes(Query query, User remoteUser, final ChartFactory.PeriodName periodName, int days, String dateFieldId, long yAxisTimePeriodSizeInMillis)
            throws IOException, SearchException
    {
        final Class timePeriodClass = ChartUtil.getTimePeriodClass(periodName);
        final TimeZone timePeriodZone = timeZoneManager.getLoggedInUserTimeZone();
        StatisticsMapper createdMapper = new DatePeriodStatisticsMapper(timePeriodClass, DocumentConstants.ISSUE_CREATED, timePeriodZone);
        StatisticsMapper dateMapper = new DatePeriodStatisticsMapper(timePeriodClass, dateFieldId, timePeriodZone);

        final Map<RegularTimePeriod, List<Long>> allOpenTimes = new TreeMap<RegularTimePeriod, List<Long>>();
        searchProvider.search(query, remoteUser,
                new DateRangeObjectHitCollector(createdMapper.getDocumentConstant(), dateMapper.getDocumentConstant(),
                        allOpenTimes, issueIndexManager.getIssueSearcher(), timePeriodClass, timePeriodZone));

        DataUtils.normaliseDateRange(allOpenTimes, days - 1, timePeriodClass, timePeriodZone);

        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        final I18nBean i18nBean = getI18nBean(remoteUser);
        final TimeSeries countSeries = new TimeSeries(i18nBean.getText("datacollector.issuesresolvedcapital"), timePeriodClass);
        final TimeSeries totalSeries = new TimeSeries(i18nBean.getText("datacollector.totalresolvetime"), timePeriodClass);
        final TimeSeries averageSeries = new TimeSeries(i18nBean.getText("datacollector.averageresolvetime"), timePeriodClass);

        for (final Map.Entry<RegularTimePeriod, List<Long>> entry : allOpenTimes.entrySet())
        {
            final RegularTimePeriod period = entry.getKey();
            final List<Long> times = entry.getValue();
            long total = 0;
            long average = 0;

            if (times != null)
            {
                for (final Long time : times)
                {
                    total += time;
                }
                if (times.size() > 0)
                {
                    average = total / times.size();
                }

                //special case for if there's only one entry with 0 duration.  This was put in by the normalise
                //and means there is no resolved issues.  Issue being resolved within 0 seconds of it being created
                //isn't possible anyways.
                if (total == 0 && times.size() == 1)
                {
                    countSeries.add(period, 0);
                }
                else
                {
                    countSeries.add(period, times.size());
                }
            }

            totalSeries.add(period, total / yAxisTimePeriodSizeInMillis);
            averageSeries.add(period, average / yAxisTimePeriodSizeInMillis);
        }

        dataset.addSeries(countSeries);
        dataset.addSeries(totalSeries);
        dataset.addSeries(averageSeries);

        return dataset;
    }

    private I18nBean getI18nBean(final User remoteUser)
    {
        return new I18nBean(remoteUser);
    }

    //From a particular time period length, return an i18n string to determine the name of that time period.
    private String getTimePeriodi18nName(long timePeriodLength)
    {
        // yes - this method is dirty and shouldn't exist
        if (DateUtils.DAY_MILLIS == timePeriodLength)
        {
            return "datacollector.days";
        }
        if (DateUtils.HOUR_MILLIS == timePeriodLength)
        {
            return "datacollector.hours";
        }
        return "";
    }

    static class DateRangeObjectHitCollector extends FieldableDocumentHitCollector
    {
        private String dateDocumentConstant1;
        private String dateDocumentConstant2;
        private final Map<RegularTimePeriod, List<Long>> result;
        private final Class timePeriodClass;
        private final TimeZone periodTimeZone;
        private final FieldSelector fieldSelector;

        public DateRangeObjectHitCollector(final String dateDocumentConstant1, final String dateDocumentConstant2,
                Map<RegularTimePeriod, List<Long>> result, IndexSearcher searcher, Class timePeriodClass,
                TimeZone periodTimeZone)
        {
            super(searcher);
            this.dateDocumentConstant1 = dateDocumentConstant1;
            this.dateDocumentConstant2 = dateDocumentConstant2;
            this.result = result;
            this.timePeriodClass = timePeriodClass;
            this.periodTimeZone = periodTimeZone;
            fieldSelector = new MapFieldSelector(dateDocumentConstant1, dateDocumentConstant2);
        }

        @Override
        protected FieldSelector getFieldSelector()
        {
            return fieldSelector;
        }

        public void collect(Document d)
        {
            final Date creationDate = LuceneUtils.stringToDate(d.get(dateDocumentConstant1));
            final Date otherDate = LuceneUtils.stringToDate(d.get(dateDocumentConstant2));

            final RegularTimePeriod period = RegularTimePeriod.createInstance(timePeriodClass, otherDate, periodTimeZone);

            List<Long> values = result.get(period);
            if (values == null)
            {
                values = new ArrayList<Long>();
            }
            values.add(otherDate.getTime() - creationDate.getTime());
            result.put(period, values);
        }
    }
}
