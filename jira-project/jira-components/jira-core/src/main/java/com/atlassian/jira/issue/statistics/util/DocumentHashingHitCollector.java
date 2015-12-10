package com.atlassian.jira.issue.statistics.util;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import java.io.IOException;

/**
 * A Collector that iterates through the documents, and calculates a hash code.
 * <p>
 * This is useful if you wish to detemine if the results of a search have changed, but you don't
 * want to calculate the results each time.
 * 
 * @see com.atlassian.jira.issue.pager.PagerManager
 */
public class DocumentHashingHitCollector extends Collector
{
    private int hashCode = 1;
    private int docBase = 0;

    public void collect(int doc)
    {
        hashCode = hashCode * 31 + docBase + doc;
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

    public int getHashCode()
    {
        return hashCode;
    }
}
