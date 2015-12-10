package com.atlassian.jira.issue.statistics.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;

import java.io.IOException;

/**
 * A simple Lucene {@link Collector} that visits the entire Document for each result in the search.
 * <p>
 * JIRA Issue Documents are quite large, so this is probably rather inefficient.
 * Consider using {@link FieldableDocumentHitCollector} instead if you only want a subset of each Document.
 *
 * @deprecated {@since v6.1} Only return the fields you seek. See {@link FieldableDocumentHitCollector}
 */
public abstract class DocumentHitCollector extends Collector
{
    protected final IndexSearcher searcher;
    private int docBase;

    protected DocumentHitCollector(IndexSearcher searcher)
    {
        this.searcher = searcher;
    }

    public void collect(int i)
    {
        try
        {
            Document d = searcher.doc(docBase + i);
            collect(d);
        }
        catch (IOException e)
        {
            //do nothing
        }
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException
    {
        // Do nothing
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException
    {
        this.docBase = docBase;
        
    }

    @Override
    public boolean acceptsDocsOutOfOrder()
    {
        return true;
    }

    public abstract void collect(Document d);
}
