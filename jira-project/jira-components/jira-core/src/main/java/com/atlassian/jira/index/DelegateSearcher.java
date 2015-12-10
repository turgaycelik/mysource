package com.atlassian.jira.index;

import com.atlassian.jira.util.Supplier;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.Weight;

import java.io.IOException;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Override all searcher methods and delegate to another {@link IndexSearcher}.
 * <p>
 * Note, this is fragile extension. We need to check each time we update Lucene that new superclass
 * methods have been added and override them too!
 */
class DelegateSearcher extends IndexSearcher implements Supplier<IndexSearcher>
{
    private final IndexSearcher searcher;

    DelegateSearcher(final IndexSearcher searcher)
    {
        super(searcher.getIndexReader());
        this.searcher = notNull("searcher", searcher);
    }

    public IndexSearcher get()
    {
        return searcher;
    }

    @Override
    public IndexReader getIndexReader()
    {
        return searcher.getIndexReader();
    }

    @Override
    protected void gatherSubReaders(List<IndexReader> allSubReaders, IndexReader r)
    {
        // Note: cannot call protected method. Check super to make sure it calls back using public methods only.
        super.gatherSubReaders(allSubReaders, r);
    }

    @Override
    public IndexReader[] getSubReaders()
    {
        return searcher.getSubReaders();
    }

    @Override
    protected TopFieldDocs search(Weight weight, Filter filter, int nDocs, Sort sort, boolean fillFields)
            throws IOException
    {
        throw new UnsupportedOperationException("We Cannot delegate this protected method.");
    }

    @Override
    public void setDefaultFieldSortScoring(boolean doTrackScores, boolean doMaxScore)
    {
        searcher.setDefaultFieldSortScoring(doTrackScores, doMaxScore);
    }

    @Override
    public void close() throws IOException
    {
        searcher.close();
    }

    @Override
    public Document doc(final int n, final FieldSelector fieldSelector) throws CorruptIndexException, IOException
    {
        return searcher.doc(n, fieldSelector);
    }

    @Override
    public Document doc(final int i) throws CorruptIndexException, IOException
    {
        return searcher.doc(i);
    }

    @Override
    public int docFreq(final Term term) throws IOException
    {
        return searcher.docFreq(term);
    }

    @Override
    public int[] docFreqs(final Term[] terms) throws IOException
    {
        return searcher.docFreqs(terms);
    }

    @Override
    public boolean equals(final Object obj)
    {
        return searcher.equals(obj);
    }

    @Override
    public Explanation explain(final Query query, final int doc) throws IOException
    {
        return searcher.explain(query, doc);
    }

    @Override
    public Explanation explain(final Weight weight, final int doc) throws IOException
    {
        return searcher.explain(weight, doc);
    }

    @Override
    public Similarity getSimilarity()
    {
        return searcher.getSimilarity();
    }

    @Override
    public int hashCode()
    {
        return searcher.hashCode();
    }

    @Override
    public int maxDoc()
    {
        return searcher.maxDoc();
    }

    @Override
    public Query rewrite(final Query query) throws IOException
    {
        return searcher.rewrite(query);
    }

    @Override
    public void search(final Query query, final Filter filter, final Collector results) throws IOException
    {
        searcher.search(query, filter, results);
    }

    @Override
    public TopFieldDocs search(final Query query, final int n, final Sort sort) throws IOException
    {
        return searcher.search(query, n, sort);
    }

    @Override
    public TopFieldDocs search(final Query query, final Filter filter, final int n, final Sort sort) throws IOException
    {
        return searcher.search(query, filter, n, sort);
    }

    @Override
    public TopDocs search(Query query, int n) throws IOException
    {
        return searcher.search(query, n);
    }

    @Override
    public TopDocs search(final Query query, final Filter filter, final int n) throws IOException
    {
        return searcher.search(query, filter, n);
    }

    @Override
    public void search(final Query query, final Collector results) throws IOException
    {
        searcher.search(query, results);
    }

    @Override
    public void search(final Weight weight, final Filter filter, final Collector results) throws IOException
    {
        searcher.search(weight, filter, results);
    }

    @Override
    public TopFieldDocs search(final Weight weight, final Filter filter, final int n, final Sort sort) throws IOException
    {
        return searcher.search(weight, filter, n, sort);
    }

    @Override
    public TopDocs search(final Weight weight, final Filter filter, final int n) throws IOException
    {
        return searcher.search(weight, filter, n);
    }

    @Override
    public void setSimilarity(final Similarity similarity)
    {
        searcher.setSimilarity(similarity);
    }

    @Override
    public String toString()
    {
        return searcher.toString();
    }
}
