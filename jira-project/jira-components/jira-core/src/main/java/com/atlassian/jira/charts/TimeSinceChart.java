package com.atlassian.jira.charts;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.charts.jfreechart.HistogramChartGenerator;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.charts.util.DataUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.QueryOptimizer;
import com.atlassian.jira.issue.search.util.RedundantClausesQueryOptimizer;
import com.atlassian.jira.issue.statistics.util.FieldableDocumentHitCollector;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.urls.TimeSeriesURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Produces a chart showing the number of issues based on a particular datefield.
 *
 * @since v4.0
 */
class TimeSinceChart
{
    private final FieldManager fieldManager;
    private final SearchProvider searchProvider;
    private final IssueIndexManager issueIndexManager;
    private final SearchService searchService;
    private final ApplicationProperties applicationProperties;
    private final TimeZone chartTimeZone;

    public TimeSinceChart(FieldManager fieldManager, SearchProvider searchProvider, IssueIndexManager issueIndexManager,
            SearchService searchService, ApplicationProperties applicationProperties, TimeZone chartTimeZone)
    {
        this.fieldManager = fieldManager;
        this.searchProvider = searchProvider;
        this.issueIndexManager = issueIndexManager;
        this.searchService = searchService;
        this.applicationProperties = applicationProperties;
        this.chartTimeZone = chartTimeZone;
    }

    public Chart generateInlineChart(final User remoteUser, final SearchRequest searchRequest, int days,
            final ChartFactory.PeriodName periodName, int width, int height, boolean cumulative, final String dateFieldId)
    {
        return generateChartInternal(remoteUser, searchRequest, days, periodName, width, height, cumulative, dateFieldId, true);
    }

    public Chart generateChart(final User remoteUser, final SearchRequest searchRequest, int days,
            final ChartFactory.PeriodName periodName, int width, int height, boolean cumulative, final String dateFieldId)
    {
        return generateChartInternal(remoteUser, searchRequest, days, periodName, width, height, cumulative, dateFieldId, false);
    }

    public Chart generateChartInternal(final User remoteUser, final SearchRequest searchRequest, int days,
            final ChartFactory.PeriodName periodName, int width, int height, boolean cumulative, final String dateFieldId, boolean inline)
    {
        Assertions.notNull("searchRequest", searchRequest);
        Assertions.notNull("dateFieldId", dateFieldId);

        try
        {
            final NavigableField dateField = fieldManager.getNavigableField(dateFieldId);

            days = DataUtils.normalizeDaysValue(days, periodName);

            final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());
            final JqlClauseBuilder whereClauseBuilder = queryBuilder.where().defaultAnd();
            whereClauseBuilder.field(getJqlFieldIdFor(dateField)).gtEq().string("-" + days + "d");

            final Map<RegularTimePeriod, Number> matchingIssues = new TreeMap<RegularTimePeriod, Number>();
            final Class timePeriodClass = ChartUtil.getTimePeriodClass(periodName);
            final Collector hitCollector = new GenericDateFieldIssuesHitCollector(matchingIssues, issueIndexManager.getIssueSearcher(),
                    dateField.getSorter(), timePeriodClass, dateFieldId, chartTimeZone);
            final Query query = whereClauseBuilder.buildQuery();
            searchProvider.search(query, remoteUser, hitCollector);
            DataUtils.normaliseDateRangeCount(matchingIssues, days - 1, timePeriodClass, chartTimeZone);

            final I18nBean i18nBean = new I18nBean(remoteUser);
            final TimeSeriesCollection originalDataset = DataUtils.getTimeSeriesCollection(Lists.newArrayList(matchingIssues),
                    new String[] { dateField.getName() }, timePeriodClass);

            //need to calculate the number of total issues before potentially making the values cumulative, which
            //would yield the wrong total.
            int numIssues = DataUtils.getTotalNumber(matchingIssues);
            if (cumulative)
            {
                DataUtils.makeCumulative(matchingIssues);
            }


            final TimeSeriesCollection dataset = DataUtils.getTimeSeriesCollection(Lists.newArrayList(matchingIssues),
                    new String[] { i18nBean.getText("datacollector.createdresolved") }, timePeriodClass);
            ChartHelper helper = new HistogramChartGenerator(dataset, i18nBean.getText("common.concepts.issues"), i18nBean).generateChart();
            JFreeChart chart = helper.getChart();
            XYPlot plot = (XYPlot) chart.getPlot();
            XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
            renderer.setToolTipGenerator(new XYToolTipGenerator()
            {
                public String generateToolTip(XYDataset xyDataset, int row, int col)
                {
                    final TimeSeriesCollection timeSeriesCollection = (TimeSeriesCollection) xyDataset;
                    final TimeSeries timeSeries = timeSeriesCollection.getSeries(row);
                    final TimeSeriesDataItem item = timeSeries.getDataItem(col);
                    final RegularTimePeriod period = item.getPeriod();

                    int total = matchingIssues.get(period).intValue();

                    return period.toString() + ": " + total + " " + StringEscapeUtils.escapeHtml(dateField.getName()) + " " + i18nBean.getText("datacollector.tooltip.issues") + ".";
                }
            });
            final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
            XYURLGenerator urlGenerator = new TimeSeriesURLGenerator()
            {
                public String generateURL(XYDataset xyDataset, int row, int col)
                {
                    final TimeSeriesCollection timeSeriesCollection = (TimeSeriesCollection) xyDataset;
                    final TimeSeries timeSeries = timeSeriesCollection.getSeries(row);
                    final TimeSeriesDataItem item = timeSeries.getDataItem(col);
                    final RegularTimePeriod period = item.getPeriod();
                    final JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder(searchRequest.getQuery());

                    final Date startDate = period.getStart();
                    final Date endDate = new Date(period.getEnd().getTime());

                    jqlQueryBuilder.where().defaultAnd().addDateRangeCondition(getJqlFieldIdFor(dateField), startDate, endDate);
                    jqlQueryBuilder.orderBy().addSortForFieldName(dateFieldId, SortOrder.DESC, true);

                    final QueryOptimizer optimizer = new RedundantClausesQueryOptimizer();
                    final Query query = optimizer.optimizeQuery(jqlQueryBuilder.buildQuery());
                    return velocityRequestContext.getCanonicalBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(remoteUser, query);
                }
            };
            renderer.setURLGenerator(urlGenerator);
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
            params.put("completeDataset", originalDataset);
            params.put("completeDatasetUrlGenerator", urlGenerator);
            params.put("numIssues", numIssues);
            params.put("period", periodName.toString());
            params.put("imagemap", helper.getImageMap());
            params.put("imagemapName", helper.getImageMapName());
            params.put("field", dateField);
            params.put("daysPrevious", days);
            params.put("cumulative", cumulative);
            params.put("dateField", dateFieldId);
            params.put("chartFilterUrl", "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(remoteUser, query));
            if (inline)
            {
                String base64Image = ComponentAccessor.getComponent(ChartUtils.class).renderBase64Chart(helper.getImage(), "Time Since Chart");
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

    private String getJqlFieldIdFor(final Field field)
    {
        if (field instanceof CustomField)
        {
            final CustomField asCustomField = (CustomField) field;
            return JqlCustomFieldId.toString(asCustomField.getIdAsLong());
        }
        else
        {
            return field.getId();
        }
    }

    /**
     * Creates a mapping of RegularTimePeriod -> Issuecount.
     */
    static class GenericDateFieldIssuesHitCollector extends FieldableDocumentHitCollector
    {
        private final Map<RegularTimePeriod, Number> resolvedMap;
        private final LuceneFieldSorter fieldSorter;
        private final Class timePeriodClass;
        private final String dateFieldId;
        private final TimeZone timeZone;
        private final FieldSelector fieldSelector;

        public GenericDateFieldIssuesHitCollector(Map<RegularTimePeriod, Number> resolvedMap, IndexSearcher searcher,
                LuceneFieldSorter fieldSorter,
                Class timePeriodClass, final String dateFieldId, TimeZone timeZone)
        {
            super(searcher);
            this.resolvedMap = resolvedMap;
            this.fieldSorter = fieldSorter;
            this.timePeriodClass = timePeriodClass;
            this.dateFieldId = dateFieldId;
            this.timeZone = timeZone;
            fieldSelector = new MapFieldSelector(dateFieldId);
        }

        @Override
        protected FieldSelector getFieldSelector()
        {
            return fieldSelector;
        }

        public void collect(Document d)
        {
            final Object dateObject = fieldSorter.getValueFromLuceneField(d.get(dateFieldId));
            final Date date;
            if (dateObject instanceof LocalDate)
            {
                final LocalDate localDate = (LocalDate) dateObject;
                // special handling for Due Date which is not a real date
                date = new DateTime(localDate.getYear(), localDate.getMonth(), localDate.getDay(), 0, 0).toDate();
            }
            else
            {
                // dateObject is Date always, but if it ever changes we can try to handle it (or throw an exception if we fail)
                date = new DateTime(dateObject).toDate();
            }

            final RegularTimePeriod period = RegularTimePeriod.createInstance(timePeriodClass, date, timeZone);

            incrementMap(resolvedMap, period);
        }

        private void incrementMap(Map<RegularTimePeriod, Number> map, RegularTimePeriod period)
        {
            Number count = map.get(period);
            if (count == null)
            {
                count = 0;
            }

            map.put(period, count.intValue() + 1);
        }
    }
}
