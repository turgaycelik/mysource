package com.atlassian.jira.util.searchers;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 * This class manages the searcher thread local cache.  The actual searchers themselves are
 * stored in this object, which is stored in a {@link ThreadLocal}.
 * 
 * @deprecated The {@link IssueIndexManager} is now responsible for maintaining a thread-local life-cycle 
 * of the issue and comment Searchers
 */
@Deprecated
public class ThreadLocalSearcherCache
{
    public static IndexSearcher getSearcher()
    {
        return getIndexManager().getIssueSearcher();
    }

    public static IndexReader getReader()
    {
        return getIndexManager().getIssueSearcher().getIndexReader();
    }

    public static IndexSearcher getCommentSearcher()
    {
        return getIndexManager().getCommentSearcher();
    }

    public static void resetSearchers()
    {
        DefaultIndexManager.flushThreadLocalSearchers();
    }

    /**
     * Get the index manager directly from the component manager every time.  NOTE that you cannot cache this
     * locally here, since we could end up in an invalid state, if a
     * {@link com.atlassian.jira.ManagerFactory#globalRefresh()} is called  (which would result in a stale
     * IndexManager dynamicProxy being cached here).
     *
     * @return IssueIndexManager The indexManager to get searchers.
     */
    private static IssueIndexManager getIndexManager()
    {
        return ComponentManager.getInstance().getIndexManager();
    }
}
