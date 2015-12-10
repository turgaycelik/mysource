package com.atlassian.jira.index;

import java.io.IOException;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.util.searchers.MockSearcherFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.Weight;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.DocIdBitSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TestDelegateSearcher
{
    @Test
    public void testClose() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public void close() throws IOException
            {
                called.set(true);
                super.close();
            }
        };
        new DelegateSearcher(searcher).close();
        assertTrue(called.get());
    }

    @Test
    public void testMaxDoc() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public int maxDoc()
            {
                called.set(true);
                return 27;
            }
        };
        assertEquals(27, new DelegateSearcher(searcher).maxDoc());
        assertTrue(called.get());
    }

    @Test
    public void testGet() throws IOException
    {
        final IndexSearcher searcher = new IndexSearcher(getDirectory());
        assertSame(searcher, new DelegateSearcher(searcher).get());
    }

    @Test
    public void testGetIndexReader() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public IndexReader getIndexReader()
            {
                called.set(true);
                return super.getIndexReader();
            }
        };
        new DelegateSearcher(searcher).getIndexReader();
        assertTrue(called.get());
    }

    @Test
    public void testCreateWeightQuery() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public Query rewrite(final Query original) throws IOException
            {
                called.set(true);
                return super.rewrite(original);
            }
        };
        new DelegateSearcher(searcher).createNormalizedWeight(new BooleanQuery());
        assertTrue(called.get());
    }

    @Test
    public void testDocIntFieldSelector() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public Document doc(final int i, final FieldSelector fieldSelector) throws CorruptIndexException, IOException
            {
                called.set(true);
                return new Document();
            }
        };
        new DelegateSearcher(searcher).doc(3, new FieldSelector()
        {
            public FieldSelectorResult accept(final String fieldName)
            {
                return null;
            }
        });
        assertTrue(called.get());
    }

    @Test
    public void testDocInt() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public Document doc(final int i) throws CorruptIndexException, IOException
            {
                called.set(true);
                return new Document();
            }
        };
        new DelegateSearcher(searcher).doc(3);
        assertTrue(called.get());
    }

    @Test
    public void testDocFreqTerm() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public int docFreq(final Term term) throws IOException
            {
                called.set(true);
                return super.docFreq(term);
            }
        };
        new DelegateSearcher(searcher).docFreq(new Term("one", "two"));
        assertTrue(called.get());
    }

    @Test
    public void testDocFreqsTermArray() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public int[] docFreqs(final Term[] terms) throws IOException
            {
                called.set(true);
                return super.docFreqs(terms);
            }
        };
        new DelegateSearcher(searcher).docFreqs(new Term[] { new Term("one", "two") });
        assertTrue(called.get());
    }

    @Test
    public void testEqualsObject() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public boolean equals(final Object obj)
            {
                called.set(true);
                return super.equals(obj);
            }
        };
        new DelegateSearcher(searcher).equals(null);
        assertTrue(called.get());
    }

    @Test
    public void testExplainQueryInt() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public Explanation explain(final Query query, final int doc) throws IOException
            {
                called.set(true);
                return null;
            }
        };
        new DelegateSearcher(searcher).explain(new BooleanQuery(), 1);
        assertTrue(called.get());
    }

    @Test
    public void testExplainWeightInt() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public Explanation explain(final Weight weight, final int doc) throws IOException
            {
                called.set(true);
                return null;
            }
        };
        new DelegateSearcher(searcher).explain(new BooleanQuery().weight(searcher), 1);
        assertTrue(called.get());
    }

    @Test
    public void testGetSimilarity() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public Similarity getSimilarity()
            {
                called.set(true);
                return super.getSimilarity();
            }
        };
        new DelegateSearcher(searcher).getSimilarity();
        assertTrue(called.get());
    }

    @Test
    public void testRewriteQuery() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public Query rewrite(final Query original) throws IOException
            {
                called.set(true);
                return super.rewrite(original);
            }
        };
        new DelegateSearcher(searcher).rewrite(new BooleanQuery());
        assertTrue(called.get());
    }

    @Test
    public void testSearchQueryFilterCollector() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public void search(final Query query, final Filter filter, final Collector results) throws IOException
            {
                called.set(true);
                super.search(query, filter, results);
            }
        };
        new DelegateSearcher(searcher).search(new BooleanQuery(), new Filter()
        {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException
            {
                return new DocIdBitSet(new BitSet());
            }
        }, new Collector()
        {
            @Override
            public void setScorer(Scorer scorer) throws IOException
            {}

            @Override
            public void collect(int doc) throws IOException
            {}

            @Override
            public void setNextReader(IndexReader reader, int docBase) throws IOException
            {}

            @Override
            public boolean acceptsDocsOutOfOrder()
            {
                return true;
            }
        });
        assertTrue(called.get());
    }

    @Test
    public void testSearchQueryFilterIntSort() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public TopFieldDocs search(final Query query, final Filter filter, final int n, final Sort sort) throws IOException
            {
                called.set(true);
                return super.search(query, filter, n, sort);
            }
        };
        new DelegateSearcher(searcher).search(new BooleanQuery(), new Filter()
        {
                    @Override
                    public DocIdSet getDocIdSet(IndexReader reader) throws IOException
                    {
                        return new DocIdBitSet(new BitSet());
                    }
        }, Integer.MAX_VALUE, new Sort());
        assertTrue(called.get());
    }

    @Test
    public void testSearchQueryFilterInt() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public TopDocs search(final Query query, final Filter filter, final int n) throws IOException
            {
                called.set(true);
                return super.search(query, filter, n);
            }
        };
        new DelegateSearcher(searcher).search(new BooleanQuery(), new Filter()
        {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException
            {
                return new DocIdBitSet(new BitSet());
            }
        }, 1);
        assertTrue(called.get());
    }

    @Test
    public void testSearchQueryFilterSort() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public TopFieldDocs search(final Query query, final Filter filter, final int n, final Sort sort) throws IOException
            {
                called.set(true);
                return super.search(query, filter, n, sort);
            }
        };
        new DelegateSearcher(searcher).search(new BooleanQuery(), new Filter()
        {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException
            {
                return new DocIdBitSet(new BitSet());
            }
        }, 1, new Sort());
        assertTrue(called.get());
    }

    @Test
    public void testSearchQueryFilter() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public TopDocs search(final Query query, final Filter filter, final int n) throws IOException
            {
                called.set(true);
                return super.search(query, filter, n);
            }
        };
        new DelegateSearcher(searcher).search(new BooleanQuery(), new Filter()
        {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException
            {
                return new DocIdBitSet(new BitSet());
            }
        }, 1);
        assertTrue(called.get());
    }

    @Test
    public void testSearchQueryCollector() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public void search(final Query query, final Collector results) throws IOException
            {
                called.set(true);
                super.search(query, results);
            }
        };
        new DelegateSearcher(searcher).search(new BooleanQuery(), new Collector()
        {
            @Override
            public void setScorer(Scorer scorer) throws IOException
            {}

            @Override
            public void collect(int doc) throws IOException
            {}

            @Override
            public void setNextReader(IndexReader reader, int docBase) throws IOException
            {}

            @Override
            public boolean acceptsDocsOutOfOrder()
            {
                return true;
            }
        });
        assertTrue(called.get());
    }

    @Test
    public void testSearchQuerySort() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public TopFieldDocs search(final Query query, final int n, final Sort sort) throws IOException
            {
                called.set(true);
                return super.search(query, n, sort);
            }
        };
        new DelegateSearcher(searcher).search(new BooleanQuery(), Integer.MAX_VALUE, new Sort());
        assertTrue(called.get());
    }

    @Test
    public void testSearchWeightFilterCollector() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public void search(final Weight weight, final Filter filter, final Collector results) throws IOException
            {
                called.set(true);
                super.search(weight, filter, results);
            }
        };
        new DelegateSearcher(searcher).search(new BooleanQuery().weight(searcher), new Filter()
        {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException
            {
                return new DocIdBitSet(new BitSet());
            }
        }, new Collector()
        {
            @Override
            public void setScorer(Scorer scorer) throws IOException
            {}

            @Override
            public void collect(int doc) throws IOException
            {}

            @Override
            public void setNextReader(IndexReader reader, int docBase) throws IOException
            {}

            @Override
            public boolean acceptsDocsOutOfOrder()
            {
                return true;
            }
        });
        assertTrue(called.get());
    }

    @Test
    public void testSearchWeightFilterIntSort() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public TopFieldDocs search(final Weight weight, final Filter filter, final int docs, final Sort sort) throws IOException
            {
                called.set(true);
                return super.search(weight, filter, docs, sort);
            }
        };
        new DelegateSearcher(searcher).search(new BooleanQuery().weight(searcher), new Filter()
        {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException
            {
                return new DocIdBitSet(new BitSet());
            }
        }, Integer.MAX_VALUE, new Sort());
        assertTrue(called.get());
    }

    @Test
    public void testSearchWeightFilterInt() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public TopDocs search(final Weight weight, final Filter filter, final int docs) throws IOException
            {
                called.set(true);
                return super.search(weight, filter, docs);
            }
        };
        new DelegateSearcher(searcher).search(new BooleanQuery().weight(searcher), new Filter()
        {
            @Override
            public DocIdSet getDocIdSet(IndexReader reader) throws IOException
            {
                return new DocIdBitSet(new BitSet());
            }
        }, 1);
        assertTrue(called.get());
    }

    @Test
    public void testSetSimilaritySimilarity() throws IOException
    {
        final AtomicBoolean called = new AtomicBoolean();
        final IndexSearcher searcher = new IndexSearcher(getDirectory())
        {
            @Override
            public void setSimilarity(final Similarity similarity)
            {
                called.set(true);
                super.setSimilarity(similarity);
            }
        };
        new DelegateSearcher(searcher).setSimilarity(null);
        assertTrue(called.get());
    }

    @Test
    public void testHashCode() throws IOException
    {
        final IndexSearcher searcher = new IndexSearcher(getDirectory());
        assertEquals(searcher.hashCode(), new DelegateSearcher(searcher).hashCode());
    }

    @Test
    public void testToString() throws IOException
    {
        final IndexSearcher searcher = new IndexSearcher(getDirectory());
        assertEquals(searcher.toString(), new DelegateSearcher(searcher).toString());
    }

    private Directory getDirectory()
    {
        return MockSearcherFactory.getCleanRAMDirectory();
    }
}
