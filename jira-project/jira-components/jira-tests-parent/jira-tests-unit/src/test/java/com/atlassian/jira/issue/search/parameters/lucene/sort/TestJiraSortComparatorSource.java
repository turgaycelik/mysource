package com.atlassian.jira.issue.search.parameters.lucene.sort;

import java.io.IOException;
import java.util.Comparator;

import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.search.LuceneFieldSorter;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the badly named JiraLuceneFieldCache for changes to its behavior and its parent
 * class MappedSortComparator.  It should no longer throw run time exceptions if it cant
 * find a field with a name alphabetically greater than the largest known fieldname.
 */
public class TestJiraSortComparatorSource
{
    RAMDirectory directory = null;
    IndexWriter indexWriter = null;
    IndexSearcher indexSearcher = null;
    int MAX_DOCS = 10;

    @Before
    public void setUp() throws Exception
    {
        directory = new RAMDirectory();
        indexWriter = new IndexWriter(directory, new IndexWriterConfig(LuceneVersion.get(), new WhitespaceAnalyzer(LuceneVersion.get())));
        setupData(indexWriter);
        indexWriter.close();

        indexSearcher = new IndexSearcher(directory);
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
        doc.add(new Field("issuetype", "bug", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("key", issueKey, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("duedate", dueDateValue, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("summary", summaryValue, Field.Store.YES, Field.Index.NOT_ANALYZED));
        indexWriter.addDocument(doc);

    }

    @Test
    public void testValueRetrieval() throws IOException
    {
        TopFieldDocs hits;
        Sort sort;
        MappedSortComparator mappedSortComparator;
        String sortField;
        
        final String queryField = "issuetype";
        final Query query = new TermQuery(new Term(queryField, "bug"));


        sortField = "duedate";
        mappedSortComparator = newMappedSortComparator(sortField, MAX_DOCS, false);
        sort = new Sort(new SortField(sortField, mappedSortComparator));
        hits = indexSearcher.search(query, Integer.MAX_VALUE, sort);
        assertEquals(MAX_DOCS, hits.totalHits);

        sortField = "field_that_does_not_exist_but_is_alphabetically_before_summary";
        mappedSortComparator = newMappedSortComparator(sortField, MAX_DOCS, true);
        sort = new Sort(new SortField(sortField, mappedSortComparator));
        hits = indexSearcher.search(query, Integer.MAX_VALUE, sort);
        assertEquals(MAX_DOCS, hits.totalHits);



        sortField = "summary";
        mappedSortComparator = newMappedSortComparator(sortField, MAX_DOCS, false);
        sort = new Sort(new SortField(sortField, mappedSortComparator));
        hits = indexSearcher.search(query, Integer.MAX_VALUE, sort);
        assertEquals(MAX_DOCS, hits.totalHits);

        sortField = "workratio";
        mappedSortComparator = newMappedSortComparator(sortField, MAX_DOCS, true);
        sort = new Sort(new SortField(sortField, mappedSortComparator));
        hits = indexSearcher.search(query, Integer.MAX_VALUE, sort);
        assertEquals(MAX_DOCS, hits.totalHits);

        sortField = "zztop";
        mappedSortComparator = newMappedSortComparator(sortField, MAX_DOCS, true);
        sort = new Sort(new SortField(sortField, mappedSortComparator));
        hits = indexSearcher.search(query, Integer.MAX_VALUE, sort);
        assertEquals(MAX_DOCS, hits.totalHits);

    }

    /*
     * Calls into the MappedSortComparator
     */
    private MappedSortComparator newMappedSortComparator(final String sortField, final int expectedLength, final boolean mustAllBeNull)
    {
        return new MappedSortComparator(newLuceneFieldSorter(sortField))
        {

            Object[] getLuceneValues(final String field, final IndexReader reader) throws IOException
            {
                Object[] values = super.getLuceneValues(field, reader);
                // put in some asserts for testing that the array is non null and equal to the number of expected
                // documents
                assertNotNull(values);
                assertEquals(expectedLength, values.length);
                //
                // check values
                for (int i = 0; i < values.length; i++)
                {
                    if (mustAllBeNull)
                    {
                        assertNull(values[i]);
                    } else {
                        assertNotNull(values[i]);
                    }
                }
                return values;
            }
        };
    }

    private LuceneFieldSorter newLuceneFieldSorter(final String field)
    {
        return new LuceneFieldSorter()
        {
            public String getDocumentConstant()
            {
                return field;
            }

            public Object getValueFromLuceneField(String documentValue)
            {
                return documentValue;
            }

            public Comparator getComparator()
            {
                return new Comparator()
                {
                    public int compare(Object o1, Object o2)
                    {
                        return String.valueOf(o1).compareTo(String.valueOf(o2));
                    }
                };
            }
        };
    }
}
