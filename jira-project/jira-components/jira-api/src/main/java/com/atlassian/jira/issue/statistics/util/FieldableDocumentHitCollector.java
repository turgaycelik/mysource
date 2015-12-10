package com.atlassian.jira.issue.statistics.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;

import java.io.IOException;

/**
 * A Lucene search {@link Collector} that retrieves only a subset of fields within the Document for each result.
 *
 * @see DocumentHitCollector
 */
public abstract class FieldableDocumentHitCollector extends Collector
{
    private IndexReader reader;

    /**
     * Deprecated constructor. The searcher is not used by this class anymore.
     * @param searcher
     * @deprecated {@since v6.1}
     */
    @Deprecated
    protected FieldableDocumentHitCollector(IndexSearcher searcher)
    {
    }

    protected FieldableDocumentHitCollector()
    {
    }

    public void collect(int i)
    {
        try
        {
            final Document d = reader.document(i, getFieldSelector());
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
        this.reader = reader;
    }

    @Override
    public boolean acceptsDocsOutOfOrder()
    {
        return true;
    }

    /**
     * This method should cache up the FieldSelector, such that overhead will be as small as possible when
     * this method is called from the {@link #collect(int)} method.
     *
     * @return a {@link org.apache.lucene.document.FieldSelector}
     */
    protected abstract FieldSelector getFieldSelector();

    public abstract void collect(Document d);
}
