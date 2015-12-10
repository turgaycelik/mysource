package com.atlassian.jira.issue.index;

import com.atlassian.jira.util.Supplier;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

/**
 * Utility methods related to performing consistency checks on indexes.
 *
 * @since v5.2
 */
public class IndexConsistencyUtils
{
    private static final Logger log = Logger.getLogger(IndexConsistencyUtils.class);

    // static-only class
    private IndexConsistencyUtils() {}

    // Note: Implicitly closes the searcher
    private static boolean isIndexConsistent(final String name, final int expectedCount, final IndexSearcher searcher) throws IOException
    {
        try
        {
            final int actualCount = searcher.getIndexReader().numDocs();
            if (log.isDebugEnabled())
            {
                log.debug("isIndexConsistent: " + name + ": expectedCount=" + expectedCount + "; actualCount=" + actualCount);
            }
            if (expectedCount >= 0)
            {
                final int delta = Math.abs(expectedCount - actualCount);
                final int tolerance = Math.max(10, expectedCount / 10);
                if (delta > tolerance)
                {
                    log.warn("Index consistency check failed for index '" + name + "': expectedCount=" + expectedCount + "; actualCount=" + actualCount);
                    return false;
                }
            }
        }
        finally
        {
            searcher.close();
        }
        return true;
    }

    /**
     * Performs a simple consistency check on an index by opening it, comparing the document count to an
     * expected value supplied by the caller, and closing it.  If the expected document count can not be
     * determined reliably and efficiently, then {@code -1} may be specified to skip that part of the
     * check.  If an expected count is given, the actual count must be within {@code 10%} of the expected
     * value or {@code 10} documents, whichever value is larger.
     *
     * @param name a name to identify the index
     * @param expectedCount the expected count of documents in the index, or {@code -1} to skip this check
     * @param supplier provides a searcher for the index; should throw an exception if the index is unavailable
     * @return {@code true} if the index is present and contains reasonably close to the expected number of documents;
     *      {@code false} if any exception is thrown or if the index document count is excessively different from the
     *      expected count
     */
    public static boolean isIndexConsistent(final String name, final int expectedCount, final Supplier<IndexSearcher> supplier)
    {
        try
        {
            return isIndexConsistent(name, expectedCount, supplier.get());
        }
        catch (Exception ex)
        {
            log.warn("Index consistency check failed for index '" + name + "': " + ex);
            return false;
        }
    }
}

