package com.atlassian.jira.issue.statistics.util;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.DefaultReaderCache;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.web.FieldVisibilityManager;

import com.google.common.collect.ImmutableList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestOneDimensionalTermHitCollector extends AbstractHitCollectorTestCase
{
    private static final String FIELD = "field";
    private OneDimensionalTermHitCollector hitCollector;

    private final Document doc = new Document();
    private final Document doc2 = new Document();
    private final Document doc3 = new Document();
    private final Document doc4 = new Document();

    public TestOneDimensionalTermHitCollector(Boolean useSegmentedReader)
    {
        super(useSegmentedReader);
    }

    /**
     * Test that the OneDimensionalTermHitCollector tracks the number of hits
     * @throws java.io.IOException if unable to index to RAM
     */
    @Test
    public void testOneDimensionalTermHitCollectorHitCount() throws IOException
    {
        index(doc, FIELD, "1");
        index(doc, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc2, FIELD, "22");
        index(doc2, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc2, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc3, FIELD, "333");
        index(doc3, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc3, SystemSearchConstants.forIssueType().getIndexField(), "1");

        List<Document> docsList = ImmutableList.of();
        collectStats(docsList);
        assertEquals(0, hitCollector.getHitCount());
        assertEquals(0, hitCollector.getIrrelevantCount());

        docsList = ImmutableList.of(doc);
        collectStats(docsList);
        assertEquals(1, hitCollector.getHitCount());
        assertEquals(0, hitCollector.getIrrelevantCount());

        docsList = ImmutableList.of(doc, doc2);
        collectStats(docsList);
        assertEquals(2, hitCollector.getHitCount());
        assertEquals(0, hitCollector.getIrrelevantCount());

        docsList = ImmutableList.of(doc, doc2, doc3);
        collectStats(docsList);
        assertEquals(3, hitCollector.getHitCount());
        assertEquals(0, hitCollector.getIrrelevantCount());

        docsList = ImmutableList.of(doc, doc, doc2, doc3, doc, doc3);
        collectStats(docsList);
        assertEquals(6, hitCollector.getHitCount());
        assertEquals(0, hitCollector.getIrrelevantCount());
    }

    @Test
    public void testOneDimensionalTermHitCollectorIrrelevantHitCount() throws IOException
    {
        index(doc, FIELD, "1");
        index(doc, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc2, "OTHER_FIELD", "22");
        index(doc2, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc2, SystemSearchConstants.forIssueType().getIndexField(), "2");
        index(doc3, FIELD, "333");
        index(doc3, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc3, SystemSearchConstants.forIssueType().getIndexField(), "1");

        List<Document> docsList = ImmutableList.of(doc, doc2, doc3);
        collectStatsWithIrrelevant(docsList);
        assertEquals(3, hitCollector.getHitCount());
        assertEquals(1, hitCollector.getIrrelevantCount());
    }

    @Test
    public void testOneDimensionalTermHitCollectorIrrelevantHitCountWithRelevantNullValues() throws IOException
    {
        index(doc, FIELD, "1");
        index(doc, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc2, "OTHER_FIELD", "22");
        index(doc2, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc2, SystemSearchConstants.forIssueType().getIndexField(), "2");
        index(doc3, FIELD, "333");
        index(doc3, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc3, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc4, "OTHER_FIELD", "22");
        index(doc4, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc4, SystemSearchConstants.forIssueType().getIndexField(), "4");

        List<Document> docsList = ImmutableList.of(doc, doc2, doc3, doc4);
        collectStatsWithIrrelevantAndRealNullValue(docsList);
        assertEquals(4, hitCollector.getHitCount());
        assertEquals(1, hitCollector.getIrrelevantCount());
    }

    private void collectStats(Collection<Document> docs) throws IOException
    {
        final IndexReader reader = addToIndex(docs);
        collectStats(reader);
    }

    private void collectStats(IndexReader reader) throws IOException
    {
        final FieldVisibilityManager fieldVisibilityManager = EasyMock.createMock(FieldVisibilityManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final FieldManager fieldManager = EasyMock.createMock(FieldManager.class);

        EasyMock.replay(fieldVisibilityManager);

        hitCollector = new OneDimensionalTermHitCollector(FIELD, fieldVisibilityManager, new DefaultReaderCache(),
                                                            fieldManager, projectManager);

        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new MatchAllDocsQuery();
        searcher.search(query, hitCollector);
    }

    private void collectStatsWithIrrelevant(Collection<Document> docs) throws IOException
    {
        final IndexReader reader = addToIndex(docs);
        final FieldVisibilityManager fieldVisibilityManager = EasyMock.createStrictMock(FieldVisibilityManager.class);
        final ProjectManager projectManager = EasyMock.createStrictMock(ProjectManager.class);
        final FieldManager fieldManager = EasyMock.createStrictMock(FieldManager.class);

        EasyMock.expect(fieldVisibilityManager.isFieldVisible(10000L, FIELD, "2")).andReturn(false);
        hitCollector = new OneDimensionalTermHitCollector(FIELD, fieldVisibilityManager, new DefaultReaderCache(),
                fieldManager, projectManager);
        EasyMock.replay(fieldVisibilityManager);

        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new MatchAllDocsQuery();
        searcher.search(query, hitCollector);
    }

    private void collectStatsWithIrrelevantAndRealNullValue(Collection<Document> docs) throws IOException
    {
        final IndexReader reader = addToIndex(docs);
        final FieldVisibilityManager fieldVisibilityManager = EasyMock.createStrictMock(FieldVisibilityManager.class);
        final ProjectManager projectManager = EasyMock.createStrictMock(ProjectManager.class);
        final FieldManager fieldManager = EasyMock.createStrictMock(FieldManager.class);

        EasyMock.expect(fieldVisibilityManager.isFieldVisible(10000L, FIELD, "2")).andReturn(false);
        EasyMock.expect(fieldVisibilityManager.isFieldVisible(10000L, FIELD, "4")).andReturn(true);
        hitCollector = new OneDimensionalTermHitCollector(FIELD, fieldVisibilityManager, new DefaultReaderCache(),
                                                            fieldManager, projectManager);
        EasyMock.replay(fieldVisibilityManager);

        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new MatchAllDocsQuery();
        searcher.search(query, hitCollector);
    }

}
