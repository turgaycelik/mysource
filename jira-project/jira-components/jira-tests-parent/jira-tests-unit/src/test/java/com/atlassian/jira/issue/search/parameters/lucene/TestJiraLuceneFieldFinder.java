package com.atlassian.jira.issue.search.parameters.lucene;

import java.io.IOException;
import java.util.List;

import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.search.parameters.lucene.sort.JiraLuceneFieldFinder;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the JiraLuceneFieldFinder
 *
 * @since v5.2
 */
public class TestJiraLuceneFieldFinder
{
    RAMDirectory directory = null;
    IndexWriter indexWriter = null;
    int MAX_DOCS = 10;

    @Before
    public void setUp() throws Exception
    {
        directory = new RAMDirectory();
        indexWriter = new IndexWriter(directory, new IndexWriterConfig(LuceneVersion.get(), new WhitespaceAnalyzer(LuceneVersion.get())));
        setupData(indexWriter);
        indexWriter.close();
    }

    private void setupData(IndexWriter indexWriter) throws IOException
    {
        for (int i = 0; i < MAX_DOCS; i++)
        {
            String num = String.valueOf(i);
            setupDataItems(indexWriter, "TI-123" + num, "2007080" + num, "A test issue " + num);
        }
    }

    private void setupDataItems(IndexWriter indexWriter, String issueKey, String dueDateValue, String summaryValue) throws IOException
    {
        Document doc = new Document();
        doc.add(new Field("issuetype", "bug", Field.Store.NO, Field.Index.NOT_ANALYZED));
        doc.add(new Field("key", issueKey, Field.Store.NO, Field.Index.NOT_ANALYZED));
        doc.add(new Field("duedate", dueDateValue, Field.Store.NO, Field.Index.NOT_ANALYZED));
        doc.add(new Field("summary", summaryValue, Field.Store.NO, Field.Index.NOT_ANALYZED));
        indexWriter.addDocument(doc);
    }

    @Test
    public void testGetTermValuesForField() throws Exception
    {
        IndexReader reader = null;
        try 
        { 
            reader = IndexReader.open(directory, true);
            final JiraLuceneFieldFinder fieldFinder = JiraLuceneFieldFinder.getInstance();
            final List<String> termValues = fieldFinder.getTermValuesForField(reader,"issuetype");
            Assert.assertEquals("Should only be 1 item", 1, termValues.size());
            Assert.assertTrue("Should contain bug", termValues.contains("bug"));
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    @Test
    public void testGetTermValuesForFieldWithNoFields() throws Exception
    {
        IndexReader reader = null;
        try
        {
            reader = IndexReader.open(directory, true);
            final JiraLuceneFieldFinder fieldFinder = JiraLuceneFieldFinder.getInstance();
            final List<String> termValues = fieldFinder.getTermValuesForField(reader,"issuetype1");
            Assert.assertEquals("Should be no items", 0, termValues.size());
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    @Test
    public void testGetTermValuesForFieldWithMultipleTerms() throws Exception
    {
        IndexReader reader = null;
        try
        {
            reader = IndexReader.open(directory, true);
            final JiraLuceneFieldFinder fieldFinder = JiraLuceneFieldFinder.getInstance();
            final List<String> termValues = fieldFinder.getTermValuesForField(reader,"summary");
            Assert.assertEquals("Should be 10 items", 10, termValues.size());
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }
    
    @Test
    public void testDoesFieldContainTerm() throws Exception
    {
        IndexReader reader = null;
        try
        {
            reader = IndexReader.open(directory, true);
            final JiraLuceneFieldFinder fieldFinder = JiraLuceneFieldFinder.getInstance();
            Assert.assertTrue("Field issuetype should contain bug", fieldFinder.doesFieldContainTerm(reader, "issuetype", "bug"));
            Assert.assertTrue("Field summary should contain term", fieldFinder.doesFieldContainTerm(reader, "summary", "A test issue 0"));
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }

    }

    @Test
    public void testFieldDoesNotContainTerm() throws Exception
    {
        IndexReader reader = null;
        try
        {
            reader = IndexReader.open(directory, true);
            final JiraLuceneFieldFinder fieldFinder = JiraLuceneFieldFinder.getInstance();
            Assert.assertFalse("Field issuetype should contain bug", fieldFinder.doesFieldContainTerm(reader, "issuetype", "feature"));
            Assert.assertFalse("Null fields should always return false", fieldFinder.doesFieldContainTerm(reader, null, null));
            Assert.assertFalse("Unknown fields should return false", fieldFinder.doesFieldContainTerm(reader, "issuetypes", "bug"));
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }

    }
}