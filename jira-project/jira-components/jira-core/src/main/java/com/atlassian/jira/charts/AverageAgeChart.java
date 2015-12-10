package com.atlassian.jira.charts;

import com.atlassian.core.util.DateUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.charts.jfreechart.HistogramChartGenerator;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.charts.util.DataUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.util.FieldableDocumentHitCollector;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.I18nBean;
import com.google.common.collect.Lists;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.search.IndexSearcher;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
class AverageAgeChart
{
    private final SearchProvider searchProvider;
    private final IssueIndexManager issueIndexManager;
    private final TimeZone chartTimeZone;

    public AverageAgeChart(SearchProvider searchProvider, IssueIndexManager issueIndexManager, TimeZone chartTimeZone)
    {
        this.searchProvider = searchProvider;
        this.issueIndexManager = issueIndexManager;
        this.chartTimeZone = chartTimeZone;
    }

    public Chart generateChart(User remoteUser, SearchRequest searchRequest, int days, final ChartFactory.PeriodName periodName, int width, int height)
    {
        return generateChartInternal(remoteUser, searchRequest, days, periodName, width, height, false);
    }

    public Chart generateInlineChart(User remoteUser, SearchRequest searchRequest, int days, final ChartFactory.PeriodName periodName, int width, int height)
    {
        return generateChartInternal(remoteUser, searchRequest, days, periodName, width, height, true);
    }

    private Chart generateChartInternal(User remoteUser, SearchRequest searchRequest, int days, final ChartFactory.PeriodName periodName, int width, int height, boolean inline)
    {
        Assertions.notNull("searchRequest", searchRequest);

        days = DataUtils.normalizeDaysValue(days, periodName);

        try
        {
            //Note, we only care about the issues that are either still open, or whose resolution date is in the
            // date range displayed by the chart.  Any issues that were resolved prior to the date the chart goes
            // back to are closed and wont affect the chart in any way.
            final SearchRequest clonedSr = getModifiedSearchRequest(searchRequest, days);

            final TimeSeriesCollection dataset = getAverageAge(clonedSr, remoteUser, periodName, days);
            final I18nBean i18nBean = getI18n(remoteUser);
            final TimeSeriesCollection chartDataset = DataUtils.reduceDataset(dataset, Lists.newArrayList(i18nBean.getText("datacollector.averageage")));
            final ChartHelper helper = new HistogramChartGenerator(chartDataset, i18nBean.getText("datacollector.days"), i18nBean).generateChart();

            JFreeChart chart = helper.getChart();
            XYPlot plot = (XYPlot) chart.getPlot();
            XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
            renderer.setToolTipGenerator(new StandardXYToolTipGenerator("{1}: {2} " + i18nBean.getText("datacollector.daysunresolved"),
                    new SimpleDateFormat("dd-MMMMM-yyyy", i18nBean.getLocale()), NumberFormat.getInstance()));
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
            params.put("chartDataset", chartDataset);
            params.put("completeDataset", dataset);
            params.put("daysPrevious", days);
            params.put("period", periodName.toString());
            params.put("imagemap", helper.getImageMap());
            params.put("imagemapName", helper.getImageMapName());

            if (inline)
            {
                String base64Image = ComponentAccessor.getComponent(ChartUtils.class).renderBase64Chart(helper.getImage(), "Average Age Chart");
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

    public TimeSeriesCollection getAverageAge(SearchRequest searchRequest, User remoteUser, final ChartFactory.PeriodName periodName, int days)
            throws IOException, SearchException
    {
        final Class timePeriodClass = ChartUtil.getTimePeriodClass(periodName);
        final Map<RegularTimePeriod, Long> ageTotals = new TreeMap<RegularTimePeriod, Long>();
        final Map<RegularTimePeriod, Long> ageCounts = new TreeMap<RegularTimePeriod, Long>();
        searchProvider.search((searchRequest != null) ? searchRequest.getQuery() : null, remoteUser,
                new AverageAgeHitCollector(DocumentConstants.ISSUE_CREATED, DocumentConstants.ISSUE_RESOLUTION_DATE, ageTotals, ageCounts, issueIndexManager.getIssueSearcher(), timePeriodClass, days, getCurrentTime(), chartTimeZone));

        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        final I18nBean i18nBean = getI18n(remoteUser);
        final TimeSeries countSeries = new TimeSeries(i18nBean.getText("datacollector.issuesunresolvedcapital"), timePeriodClass);
        final TimeSeries totalSeries = new TimeSeries(i18nBean.getText("datacollector.totalage"), timePeriodClass);
        final TimeSeries averageSeries = new TimeSeries(i18nBean.getText("datacollector.averageage"), timePeriodClass);
        for (final RegularTimePeriod period : ageTotals.keySet())
        {
            final Long ageTotalLong = ageTotals.get(period);
            long total = ageTotals.get(period) == null ? 0L : ageTotalLong;
            final Long ageCountLong = ageCounts.get(period);
            long count = ageCountLong == null ? 0L : ageCountLong;
            long average = 0L;

            if (count > 0L)
            {
                average = total / count;
            }

            countSeries.add(period, count);
            totalSeries.add(period, total / DateUtils.DAY_MILLIS);
            averageSeries.add(period, average / DateUtils.DAY_MILLIS);
        }

        dataset.addSeries(countSeries);
        dataset.addSeries(totalSeries);
        dataset.addSeries(averageSeries);

        return dataset;
    }

    protected long getCurrentTime()
    {
        return System.currentTimeMillis();
    }

    private SearchRequest getModifiedSearchRequest(final SearchRequest searchRequest, final int days)
    {
        final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
        queryBuilder.where().defaultAnd().sub().resolutionDate().gtEq().string("-" + days + "d").or().unresolved().endsub();
        return new SearchRequest(queryBuilder.buildQuery());
    }

    /**
     * Produces a timeperiod -> totalTime and timeperiod -> totalcount maps for issues that were open in a given time
     * period.
     */
    static class AverageAgeHitCollector extends FieldableDocumentHitCollector
    {
        private final String createdDateConstant;
        private final String resolvedDateConstant;
        private final Map<RegularTimePeriod, Long> totalTimes;
        private final Map<RegularTimePeriod, Long> totalCounts;
        private final Class timePeriodClass;
        private final int days;
        private final Long currentTime;
        private final TimeZone timeZone;
        private final FieldSelector fieldSelector;

        public AverageAgeHitCollector(final String createdDateConstant, final String resolvedDateConstant,
                Map<RegularTimePeriod, Long> totalTime, Map<RegularTimePeriod, Long> totalCount, IndexSearcher searcher,
                Class timePeriodClass, int days, final Long currentTime, TimeZone timeZone)
        {
            super(searcher);
            this.createdDateConstant = createdDateConstant;
            this.resolvedDateConstant = resolvedDateConstant;
            this.totalTimes = totalTime;
            this.totalCounts = totalCount;
            this.timePeriodClass = timePeriodClass;
            this.days = days;
            this.currentTime = currentTime;
            this.timeZone = timeZone;
            fieldSelector = new MapFieldSelector(createdDateConstant, resolvedDateConstant);
        }

        @Override
        protected FieldSelector getFieldSelector()
        {
            return fieldSelector;

        }

        public void collect(Document document)
        {
            final Date creationDate = LuceneUtils.stringToDate(document.get(createdDateConstant));
            Date resolutionDate = null;
            final String resolutionDateStr = document.get(resolvedDateConstant);
            if (resolutionDateStr != null)
            {
                resolutionDate = LuceneUtils.stringToDate(resolutionDateStr);
            }

            // find earliest date, then move it forwards until we hit now
            Calendar cal = Calendar.getInstance(timeZone);
            cal.setTimeInMillis(currentTime);
            cal.add(Calendar.DAY_OF_MONTH, -(days - 1));
            Date earliest = cal.getTime();
            RegularTimePeriod cursor = RegularTimePeriod.createInstance(timePeriodClass, earliest, timeZone);
            final RegularTimePeriod end = RegularTimePeriod.createInstance(timePeriodClass, new Date(currentTime), timeZone);

            while (cursor != null && cursor.compareTo(end) <= 0)
            {
                final Long totalTimeLong = totalTimes.get(cursor);
                long totalTime = totalTimeLong == null ? 0L : totalTimeLong;
                final Long totalCountLong = totalCounts.get(cursor);
                long totalCount = totalCountLong == null ? 0L : totalCountLong;

                long cursorStart = cursor.getFirstMillisecond();
                if (creationDate != null && creationDate.getTime() <= cursorStart && (resolutionDate == null || resolutionDate.getTime() > cursorStart))
                {
                    //if we have a resolution date we work out which is earlier, the end of the period or the resolution date.
                    //then add the time from the creation date to the end to the total for this period.
                    long endDate = cursor.getLastMillisecond();
                    if (currentTime < endDate)
                    {
                        endDate = currentTime;
                    }

                    if (resolutionDate != null)
                    {
                        final long resolutionDateInMillis = resolutionDate.getTime();
                        if (resolutionDateInMillis < endDate)
                        {
                            endDate = resolutionDateInMillis;
                        }
                    }
                    totalTime += (endDate - creationDate.getTime());
                    totalCount++;
                }

                totalTimes.put(cursor, totalTime);
                totalCounts.put(cursor, totalCount);

                cursor = cursor.next();
                cursor.peg(cal);
            }
        }
    }

    ///CLOVER:OFF
    I18nBean getI18n(User user)
    {
        return new I18nBean(user);
    }
    ///CLOVER:ON

}
