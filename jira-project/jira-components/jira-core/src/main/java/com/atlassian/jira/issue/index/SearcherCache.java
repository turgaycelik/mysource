package com.atlassian.jira.issue.index;

import com.atlassian.jira.util.Supplier;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

/**
 * This class manages the searcher thread local cache.  The actual searchers themselves are
 * stored in this object, which is stored in a {@link ThreadLocal}.
 */
class SearcherCache
{
    private static final ThreadLocal<SearcherCache> THREAD_LOCAL = new ThreadLocal<SearcherCache>();

    static SearcherCache getThreadLocalCache()
    {
        SearcherCache threadLocalSearcherCache = THREAD_LOCAL.get();
        if (threadLocalSearcherCache == null)
        {
            threadLocalSearcherCache = new SearcherCache();
            THREAD_LOCAL.set(threadLocalSearcherCache);
        }
        return threadLocalSearcherCache;
    }

    private IndexSearcher issueSearcher;
    private IndexSearcher commentSearcher;
    private IndexSearcher changeSearcher;

    IndexSearcher retrieveIssueSearcher(final Supplier<IndexSearcher> searcherSupplier)
    {
        if (issueSearcher == null)
        {
            issueSearcher = searcherSupplier.get();
        }

        return issueSearcher;
    }

    IndexReader retrieveIssueReader(final Supplier<IndexSearcher> searcherSupplier)
    {
        return retrieveIssueSearcher(searcherSupplier).getIndexReader();
    }

    IndexSearcher retrieveCommentSearcher(final Supplier<IndexSearcher> searcherSupplier)
    {
        if (commentSearcher == null)
        {
            commentSearcher = searcherSupplier.get();
        }

        return commentSearcher;
    }

    IndexSearcher retrieveChangeHistorySearcher(final Supplier<IndexSearcher> searcherSupplier)
    {
        if (changeSearcher == null)
        {
            changeSearcher = searcherSupplier.get();
        }

        return changeSearcher;
    }

    /**
     * Close the issues and comments searchers.
     *
     * @throws java.io.IOException if there's a lucene exception accessing the disk
     */
    void closeSearchers() throws IOException
    {
        try
        {
            closeSearcher(issueSearcher);
        }
        finally
        {
            // if close throws an IOException, we still need to null the searcher (JRA-10423)
            issueSearcher = null;

            try
            {
                closeSearcher(commentSearcher);
            }
            finally
            {
                // if close throws an IOException, we still need to null the searcher (JRA-10423)
                commentSearcher = null;
                 try
                {
                    closeSearcher(changeSearcher);
                }
                finally
                {
                    // if close throws an IOException, we still need to null the searcher (JRA-10423)
                    changeSearcher = null;
                }
            }

        }
    }

    private void closeSearcher(final IndexSearcher searcher) throws IOException
    {
        if (searcher != null)
        {
            searcher.close();
        }
    }

    private SearcherCache()
    {}
}
