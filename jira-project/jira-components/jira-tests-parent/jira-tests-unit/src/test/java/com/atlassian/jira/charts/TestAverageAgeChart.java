package com.atlassian.jira.charts;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.MockSearchProvider;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.Query;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.issue.index.DocumentConstants.ISSUE_CREATED;
import static com.atlassian.jira.issue.index.DocumentConstants.ISSUE_RESOLUTION_DATE;
import static java.util.TimeZone.getTimeZone;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.jfree.data.time.RegularTimePeriod.DEFAULT_TIME_ZONE;
import static org.jfree.data.time.RegularTimePeriod.createInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@SuppressWarnings ( { "unchecked" })
@RunWith(MockitoJUnitRunner.class)
public class TestAverageAgeChart
{
    private static final TimeZone SYDNEY_TZ = getTimeZone("Australia/Sydney");
    private static final TimeZone ANCHORAGE_TZ = getTimeZone("America/Anchorage");

    private final DateFormat dateFormat = dateFormat();

    @Mock IndexSearcher mockSearcher;

    Document issue2;
    Date issue2CreatedDate;
    Date issue2ResolvedDate;

    @After
    public void tearDown()
    {
        mockSearcher = null;
        issue2 = null;
        issue2CreatedDate = null;
        issue2ResolvedDate = null;
    }

    @Test
    public void testCollect()
    {
        IndexSearcher mockSearcher = mock(IndexSearcher.class);

        final String createdDateConstant = "createdDate";
        final String resolvedDateConstant = "resolvedDate";
        final Map<RegularTimePeriod, Long> totalTimes = new LinkedHashMap<RegularTimePeriod, Long>();
        final Map<RegularTimePeriod, Long> totalCount = new LinkedHashMap<RegularTimePeriod, Long>();
        final int daysBefore = 3;

        final AverageAgeChart.AverageAgeHitCollector averageHitCollector = new AverageAgeChart.AverageAgeHitCollector(
                createdDateConstant, resolvedDateConstant, totalTimes, totalCount,
                mockSearcher, Day.class, daysBefore, System.currentTimeMillis(), SYDNEY_TZ);

        final List<Document> documents = new ArrayList<Document>();
        final Calendar createdDate = Calendar.getInstance();
        final Calendar resolvedDate = Calendar.getInstance();

        createdDate.add(Calendar.DAY_OF_YEAR, -(daysBefore + 1));
        resolvedDate.add(Calendar.DAY_OF_YEAR, 1);

        /* Generate test data */
        for (int i = 0; i < daysBefore; ++i)
        {
            final Document doc = new Document();
            doc.add(new Field(createdDateConstant, LuceneUtils.dateToString(createdDate.getTime()), Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field(resolvedDateConstant,  LuceneUtils.dateToString(resolvedDate.getTime()), Field.Store.YES, Field.Index.NOT_ANALYZED));
            documents.add(doc);
        }

        for (Document doc : documents)
        {
            averageHitCollector.collect(doc);
        }

        assertEquals(3, totalCount.size());
        assertEquals(Lists.newArrayList(3L, 3L, 3L), new ArrayList(totalCount.values()));
    }

    @Test
    public void testGetAverageAgeNineOClock() throws Exception
    {
        final List<Document> documents = createTestData();

        final SearchProvider searchProvider = new MockSearchProvider()
        {
            @Override
            public void search(Query query, User user, Collector hitCollector) throws SearchException
            {
                AverageAgeChart.AverageAgeHitCollector averageHitCollector = (AverageAgeChart.AverageAgeHitCollector) hitCollector;
                for (final Iterator i = documents.iterator(); i.hasNext(); )
                {
                    averageHitCollector.collect((Document) i.next());
                }
            }
        };

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        final Date now = simpleDateFormat.parse("18.09.2009 9:00");

        final IssueIndexManager issueIndexManager = mock(IssueIndexManager.class);
        AverageAgeChart ageChart = new AverageAgeChart(searchProvider, issueIndexManager, DEFAULT_TIME_ZONE)
        {
            @Override
            I18nBean getI18n(User user)
            {
                return new MockI18nBean();
            }

            protected long getCurrentTime()
            {
                return now.getTime();
            }
        };

        final SearchRequest searchRequest = mock(SearchRequest.class);
        when(searchRequest.getQuery()).thenReturn(null);
        when(issueIndexManager.getIssueSearcher()).thenReturn(null);

        final int daysBefore = 5;
        final TimeSeriesCollection averageAge = ageChart.getAverageAge(searchRequest, null, ChartFactory.PeriodName.daily, daysBefore);
        assertEquals(3, averageAge.getSeries().size());

        final TimeSeries countSeries = averageAge.getSeries(0);
        final TimeSeries totalSeries = averageAge.getSeries(1);
        final TimeSeries averageSeries = averageAge.getSeries(2);

        final List<TimeSeriesDataItem> items = countSeries.getItems();
        final List<TimeSeriesDataItem> totalItems = totalSeries.getItems();
        final List<TimeSeriesDataItem> averageItems = averageSeries.getItems();

        for (int i = 0; i < items.size(); i++)
        {
            final TimeSeriesDataItem dataItem = items.get(i);
            final TimeSeriesDataItem totalItem = totalItems.get(i);
            final TimeSeriesDataItem averageItem = averageItems.get(i);

            final double[] issuesUnresolved = { 1, 1, 3, 2, 2 };
            final double[] totalAge = { 2, 3, 6, 4, 3 };
            final double[] avgAge = { 2, 3, 2, 2, 1 };
            assertEquals(issuesUnresolved[i], dataItem.getValue());
            assertEquals(avgAge[i], averageItem.getValue());
            assertEquals(totalAge[i], totalItem.getValue());
        }
    }

    @Test
    public void testGetAverageAgeSevenOClock() throws Exception
    {
        final List<Document> documents = createTestData();


        final SearchProvider searchProvider = new MockSearchProvider()
        {
            @Override
            public void search(Query query, User user, Collector hitCollector) throws SearchException
            {
                AverageAgeChart.AverageAgeHitCollector averageHitCollector = (AverageAgeChart.AverageAgeHitCollector) hitCollector;
                for (Document doc : documents)
                {
                    averageHitCollector.collect(doc);
                }
            }
        };

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        final Date now = simpleDateFormat.parse("18.09.2009 7:00");

        final IssueIndexManager issueIndexManager = mock(IssueIndexManager.class);
        AverageAgeChart ageChart = new AverageAgeChart(searchProvider, issueIndexManager, DEFAULT_TIME_ZONE)
        {
            @Override
            I18nBean getI18n(User user)
            {
                return new MockI18nBean();
            }

            protected long getCurrentTime()
            {


                return now.getTime();
            }
        };

        final SearchRequest searchRequest = mock(SearchRequest.class);
        when(searchRequest.getQuery()).thenReturn(null);
        when(issueIndexManager.getIssueSearcher()).thenReturn(null);

        final int daysBefore = 5;

        final TimeSeriesCollection averageAge = ageChart.getAverageAge(searchRequest, null, ChartFactory.PeriodName.daily, daysBefore);
        assertEquals(3, averageAge.getSeries().size());

        final TimeSeries countSeries = averageAge.getSeries(0);
        final TimeSeries totalSeries = averageAge.getSeries(1);
        final TimeSeries averageSeries = averageAge.getSeries(2);

        final List<TimeSeriesDataItem> items = countSeries.getItems();
        final List<TimeSeriesDataItem> totalItems = totalSeries.getItems();
        final List<TimeSeriesDataItem> averageItems = averageSeries.getItems();

        for (int i = 0; i < items.size(); i++)
        {
            final TimeSeriesDataItem dataItem = items.get(i);
            final TimeSeriesDataItem totalItem = totalItems.get(i);
            final TimeSeriesDataItem averageItem = averageItems.get(i);

            final double[] issuesUnresolved = { 1, 1, 3, 2, 2 };
            final double[] totalAge = { 2, 3, 6, 4, 2 };
            final double[] avgAge = { 2, 3, 2, 2, 1 };
            assertEquals(issuesUnresolved[i], dataItem.getValue());
            assertEquals(avgAge[i], averageItem.getValue());
            assertEquals(totalAge[i], totalItem.getValue());
        }
    }

    @Test
    public void testGetAverageAge22OClock() throws Exception
    {
        final List documents = createTestData();

        final SearchProvider searchProvider = new MockSearchProvider()
        {
            @Override
            public void search(Query query, User user, Collector hitCollector) throws SearchException
            {
                AverageAgeChart.AverageAgeHitCollector averageHitCollector = (AverageAgeChart.AverageAgeHitCollector) hitCollector;
                for (final Iterator i = documents.iterator(); i.hasNext(); )
                {
                    averageHitCollector.collect((Document) i.next());
                }
            }
        };

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        final Date now = simpleDateFormat.parse("18.09.2009 21:00");

        final IssueIndexManager issueIndexManager = mock(IssueIndexManager.class);
        AverageAgeChart ageChart = new AverageAgeChart(searchProvider, issueIndexManager, DEFAULT_TIME_ZONE)
        {
            @Override
            I18nBean getI18n(User user)
            {
                return new MockI18nBean();
            }

            protected long getCurrentTime()
            {


                return now.getTime();
            }
        };

        final SearchRequest searchRequest = mock(SearchRequest.class);
        when(searchRequest.getQuery()).thenReturn(null);
        when(issueIndexManager.getIssueSearcher()).thenReturn(null);

        final int daysBefore = 5;
        final TimeSeriesCollection averageAge = ageChart.getAverageAge(searchRequest, null, ChartFactory.PeriodName.daily, daysBefore);
        assertEquals(3, averageAge.getSeries().size());

        final TimeSeries countSeries = averageAge.getSeries(0);
        final TimeSeries totalSeries = averageAge.getSeries(1);
        final TimeSeries averageSeries = averageAge.getSeries(2);

        final List<TimeSeriesDataItem> items = countSeries.getItems();
        final List<TimeSeriesDataItem> totalItems = totalSeries.getItems();
        final List<TimeSeriesDataItem> averageItems = averageSeries.getItems();

        for (int i = 0; i < items.size(); i++)
        {
            final TimeSeriesDataItem dataItem = items.get(i);
            final TimeSeriesDataItem totalItem = totalItems.get(i);
            final TimeSeriesDataItem averageItem = averageItems.get(i);

            final double[] issuesUnresolved = { 1, 1, 3, 2, 2 };
            final double[] totalAge = { 2, 3, 6, 4, 4 };
            final double[] avgAge = { 2, 3, 2, 2, 2 };
            assertEquals(issuesUnresolved[i], dataItem.getValue());
            assertEquals(avgAge[i], averageItem.getValue());
            assertEquals(totalAge[i], totalItem.getValue());
        }
    }

    private List<Document> createTestData() throws ParseException
    {
        List<Document> documents = new ArrayList<Document>();
        final String createdDateConstant = ISSUE_CREATED;
        final String resolvedDateConstant = ISSUE_RESOLUTION_DATE;

        Date createdDate;
        Date resolvedDate;

        dateFormat.parse("17.09.2009 21:00");

        createdDate = dateFormat.parse("17.09.2009 21:00");
        resolvedDate = dateFormat.parse("17.09.2009 21:00");
        final Document issue1 = new Document();
        issue1.add(new Field(createdDateConstant, LuceneUtils.dateToString(createdDate), Field.Store.YES, Field.Index.NOT_ANALYZED));
        issue1.add(new Field(resolvedDateConstant, LuceneUtils.dateToString(resolvedDate), Field.Store.YES, Field.Index.NOT_ANALYZED));
        documents.add(issue1);

        createdDate = dateFormat.parse("17.09.2009 21:00");
        final Document issue2 = new Document();
        issue2.add(new Field(createdDateConstant, LuceneUtils.dateToString(createdDate), Field.Store.YES, Field.Index.NOT_ANALYZED));
        documents.add(issue2);

        createdDate = dateFormat.parse("15.09.2009 21:00");
        resolvedDate = dateFormat.parse("17.09.2009 21:00");
        final Document issue3 = new Document();
        issue3.add(new Field(createdDateConstant, LuceneUtils.dateToString(createdDate), Field.Store.YES, Field.Index.NOT_ANALYZED));
        issue3.add(new Field(resolvedDateConstant, LuceneUtils.dateToString(resolvedDate), Field.Store.YES, Field.Index.NOT_ANALYZED));
        documents.add(issue3);

        createdDate = dateFormat.parse("15.09.2009 21:00");
        final Document issue4 = new Document();
        issue4.add(new Field(createdDateConstant, LuceneUtils.dateToString(createdDate), Field.Store.YES, Field.Index.NOT_ANALYZED));
        documents.add(issue4);

        createdDate = dateFormat.parse("12.09.2009 21:00");
        resolvedDate = dateFormat.parse("16.09.2009 21:00");
        final Document issue5 = new Document();
        issue5.add(new Field(createdDateConstant, LuceneUtils.dateToString(createdDate), Field.Store.YES, Field.Index.NOT_ANALYZED));
        issue5.add(new Field(resolvedDateConstant, LuceneUtils.dateToString(resolvedDate), Field.Store.YES, Field.Index.NOT_ANALYZED));
        documents.add(issue5);

        return documents;
    }

    @Test
    public void averageAgeHitCollectorShouldCollectInUserTimeZone() throws Exception
    {
        final int daysBefore = 3;
        Long nowTime = dateFormat(SYDNEY_TZ).parse("14.04.2011 09:35").getTime();

        {
            final Map<RegularTimePeriod, Long> sydTotalTimes = Maps.newHashMap();
            final Map<RegularTimePeriod, Long> sydUnresolvedCount = Maps.newHashMap();

            new AverageAgeChart.AverageAgeHitCollector(ISSUE_CREATED, ISSUE_RESOLUTION_DATE, sydTotalTimes, sydUnresolvedCount, mockSearcher, Day.class, daysBefore, nowTime, SYDNEY_TZ).collect(issue2);

            // assert total count is ok for Sydney
            RegularTimePeriod apr12Sydney = dateInSydney("12.04.2011");
            RegularTimePeriod apr13Sydney = dateInSydney("13.04.2011");
            RegularTimePeriod apr14Sydney = dateInSydney("14.04.2011");
            assertThat(sydUnresolvedCount.size(), equalTo(daysBefore));
            assertThat(sydUnresolvedCount, allOf(
                    hasEntry(apr12Sydney, 1L), hasEntry(apr13Sydney, 1L), hasEntry(apr14Sydney, 0L)
            ));
            assertThat(sydTotalTimes, allOf(
                    hasEntry(apr12Sydney, 31501499999L), hasEntry(apr13Sydney, 31536000000L), hasEntry(apr14Sydney, 0L)
            ));
        }

        {
            final Map<RegularTimePeriod, Long> ancTotalTimes = Maps.newHashMap();
            final Map<RegularTimePeriod, Long> ancUnresolvedCount = Maps.newHashMap();

            // in Anchorage we are a day behind, so we expect values for Apr 12 and 13
            new AverageAgeChart.AverageAgeHitCollector(ISSUE_CREATED, ISSUE_RESOLUTION_DATE, ancTotalTimes, ancUnresolvedCount, mockSearcher, Day.class, daysBefore, nowTime, ANCHORAGE_TZ).collect(issue2);
            RegularTimePeriod apr11Anchorage = dateInAnchorage("11.04.2011");
            RegularTimePeriod apr12Anchorage = dateInAnchorage("12.04.2011");
            RegularTimePeriod apr13Anchorage = dateInAnchorage("13.04.2011");
            assertThat(ancUnresolvedCount.size(), equalTo(daysBefore));
            assertThat(ancUnresolvedCount, allOf(
                    hasEntry(apr11Anchorage, 1L), hasEntry(apr12Anchorage, 1L), hasEntry(apr13Anchorage, 0L)
            ));
            assertThat(ancTotalTimes, allOf(
                    hasEntry(apr11Anchorage, 31479899999L), hasEntry(apr12Anchorage, 31536000000L), hasEntry(apr13Anchorage, 0L)
            ));
        }
    }

    @Before
    public void setUpIssue2() throws Exception
    {
        issue2CreatedDate = dateFormat(SYDNEY_TZ).parse("13.04.2010 09:35");
        issue2ResolvedDate = dateFormat(SYDNEY_TZ).parse("13.04.2011 09:35");

        issue2 = new Document();
        issue2.add(new Field(ISSUE_CREATED, LuceneUtils.dateToString(issue2CreatedDate), Field.Store.YES, Field.Index.NOT_ANALYZED));
        issue2.add(new Field(ISSUE_RESOLUTION_DATE, LuceneUtils.dateToString(issue2ResolvedDate), Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    protected RegularTimePeriod dateInSydney(String date) throws ParseException
    {
        return createInstance(Day.class, dateFormat(SYDNEY_TZ).parse(date + " 00:00"), SYDNEY_TZ);
    }

    protected RegularTimePeriod dateInAnchorage(String date) throws ParseException
    {
        return createInstance(Day.class, dateFormat(ANCHORAGE_TZ).parse(date + " 00:00"), ANCHORAGE_TZ);
    }

    private SimpleDateFormat dateFormat()
    {
        return dateFormat(null);
    }

    private SimpleDateFormat dateFormat(TimeZone timeZone)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        if (timeZone != null)
        {
            dateFormat.setTimeZone(timeZone);
        }

        return dateFormat;
    }
}
