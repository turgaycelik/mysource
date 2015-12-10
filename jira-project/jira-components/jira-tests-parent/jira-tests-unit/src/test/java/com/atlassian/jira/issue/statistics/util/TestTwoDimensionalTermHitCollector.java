package com.atlassian.jira.issue.statistics.util;

import java.io.IOException;
import java.util.Collection;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.DefaultReaderCache;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.LongFieldStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticGatherer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TwoDimensionalStatsMap;
import com.atlassian.jira.web.FieldVisibilityManager;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestTwoDimensionalTermHitCollector extends AbstractHitCollectorTestCase
{
    private final StatisticsMapper reporter = new MockStatsMapper("reporter");
    private final StatisticsMapper status = new MockStatsMapper("status");
    private final StatisticsMapper votes = new LongFieldStatisticsMapper("votes");

    private final Document doc = new Document();
    private final Document doc2 = new Document();
    private final Document doc3 = new Document();
    private final String reporterField = reporter.getDocumentConstant();
    private final String statusField = status.getDocumentConstant();
    private final String votesField = votes.getDocumentConstant();
    TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(reporter, status);
    TwoDimensionalStatsMap meanStatsMap = new TwoDimensionalStatsMap(reporter, status, new StatisticGatherer.Mean());

    public TestTwoDimensionalTermHitCollector(Boolean useSegmentedReader)
    {
        super(useSegmentedReader);
    }

    @Test
    public void testVotes() throws IOException
    {
        index(doc, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc, reporterField, "Scott Farquhar");
        index(doc, statusField, "Open");
        index(doc, votesField, "3");

        index(doc2, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc2, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc2, reporterField, "Mike Cannon-Brookes");
        index(doc2, statusField, "Resolved");
        index(doc2, votesField, "9");

        index(doc3, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc3, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc3, reporterField, "Scott Farquhar");
        index(doc3, statusField, "Resolved");
        index(doc3, votesField, "7");

        //Test normally
        collectStats(EasyList.build(doc, doc2, doc3), statsMap, null);

        assertEquals(3, statsMap.getUniqueTotal());
        assertEquals(2, statsMap.getXAxis().size());

        //Test votes
        TwoDimensionalStatsMap statsMap = new TwoDimensionalStatsMap(reporter, status);
        collectStats(EasyList.build(doc, doc2, doc3), statsMap, votes);

        assertEquals(19, statsMap.getUniqueTotal());
        assertEquals(10, statsMap.getXAxisUniqueTotal("Scott Farquhar"));
        assertEquals(9, statsMap.getCoordinate("Mike Cannon-Brookes", "Resolved"));
        assertEquals(2, statsMap.getXAxis().size());
    }

    @Test
    public void testVotesWithNulls() throws IOException
    {
        index(doc, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc, reporterField, "Scott Farquhar");
        index(doc, statusField, "Open");
        index(doc, votesField, "3");

        index(doc2, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc2, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc2, reporterField, "Mike Cannon-Brookes");
        index(doc2, statusField, "Resolved");

        index(doc3, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc3, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc3, reporterField, "Scott Farquhar");
        index(doc3, statusField, "Resolved");
        index(doc3, votesField, "4");

        //Test votes
        collectStats(EasyList.build(doc, doc2, doc3), statsMap, votes);

        assertEquals(7, statsMap.getUniqueTotal());
        assertEquals(7, statsMap.getXAxisUniqueTotal("Scott Farquhar"));
        assertEquals(0, statsMap.getCoordinate("Mike Cannon-Brookes", "Resolved"));
        assertEquals(2, statsMap.getXAxis().size());
    }

    @Test
    public void testVotesWithAverages() throws IOException
    {
        index(doc, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc, reporterField, "Scott Farquhar");
        index(doc, statusField, "Open");
        index(doc, votesField, "10");

        index(doc2, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc2, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc2, reporterField, "Mike Cannon-Brookes");
        index(doc2, statusField, "Resolved");

        index(doc3, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc3, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc3, reporterField, "Scott Farquhar");
        index(doc3, statusField, "Resolved");
        index(doc3, votesField, "40");

        //Test votes
        collectStats(EasyList.build(doc, doc2, doc3), meanStatsMap, votes);

        assertEquals(25, meanStatsMap.getXAxisUniqueTotal("Scott Farquhar"));
        assertEquals(0, meanStatsMap.getXAxisUniqueTotal("Mike Cannon-Brookes"));

        assertEquals(20, meanStatsMap.getYAxisUniqueTotal("Resolved"));
        assertEquals(0, meanStatsMap.getCoordinate("Mike Cannon-Brookes", "Resolved"));
        assertEquals(2, meanStatsMap.getXAxis().size());
        assertEquals(2, meanStatsMap.getYAxis().size());
        assertEquals(16, meanStatsMap.getUniqueTotal());
    }

    @Test
    public void testValuesWithIrrelevantData() throws IOException
    {
        // This one has no status field so is invalid
        index(doc, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc, reporterField, "Dude");
        index(doc, votesField, "3");

        index(doc2, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc2, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc2, reporterField, "Mike Cannon-Brookes");
        index(doc2, statusField, "Resolved");
        index(doc2, votesField, "9");

        index(doc3, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc3, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc3, reporterField, "Scott Farquhar");
        index(doc3, statusField, "Resolved");
        index(doc3, votesField, "7");

        //Test normally
        collectStatsIrrelevant(EasyList.build(doc, doc2, doc3), statsMap, null);

        assertEquals(3, statsMap.getUniqueTotal());
        assertEquals(3, statsMap.getXAxis().size());
        assertEquals(1, statsMap.getYAxisUniqueTotal(FilterStatisticsValuesGenerator.IRRELEVANT));
        assertTrue(statsMap.hasIrrelevantYData());
        assertFalse(statsMap.hasIrrelevantXData());
    }

    private void collectStats(Collection docs, TwoDimensionalStatsMap statsMap, LuceneFieldSorter aggregateField) throws IOException
    {
        IndexReader reader = addToIndex(docs);

        final FieldManager fieldManager = EasyMock.createMock(FieldManager.class);
        EasyMock.expect(fieldManager.isCustomField("status")).andReturn(false).anyTimes();
        final FieldVisibilityManager fieldVisibilityManager = EasyMock.createMock(FieldVisibilityManager.class);
        EasyMock.replay(fieldVisibilityManager, fieldManager);
        TwoDimensionalTermHitCollector hitCollector = new TwoDimensionalTermHitCollector(statsMap, reader, fieldVisibilityManager, new DefaultReaderCache(), aggregateField, fieldManager);

        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new MatchAllDocsQuery();
        searcher.search(query, hitCollector);
    }

    private void collectStatsIrrelevant(Collection docs, TwoDimensionalStatsMap statsMap, LuceneFieldSorter aggregateField) throws IOException
    {
        IndexReader reader = addToIndex(docs);

        final FieldManager fieldManager = EasyMock.createMock(FieldManager.class);
        EasyMock.expect(fieldManager.isCustomField("status")).andReturn(false).anyTimes();
        final FieldVisibilityManager fieldVisibilityManager = EasyMock.createMock(FieldVisibilityManager.class);
        EasyMock.expect(fieldVisibilityManager.isFieldHidden(10000L, "status", "1")).andReturn(true).anyTimes();
        EasyMock.replay(fieldVisibilityManager, fieldManager);
        TwoDimensionalTermHitCollector hitCollector = new TwoDimensionalTermHitCollector(statsMap, reader, fieldVisibilityManager, new DefaultReaderCache(), aggregateField, fieldManager);

        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new MatchAllDocsQuery();
        searcher.search(query, hitCollector);
    }

    private static class MockStatsMapper extends NullStatsMapper
    {
        private final String documentConstant;

        MockStatsMapper(String documentConstant)
        {
            this.documentConstant = documentConstant;
        }

        public String getDocumentConstant()
        {
            return documentConstant;
        }

    }
}
