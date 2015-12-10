package com.atlassian.jira.issue.index;

import java.io.IOException;

import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.searchers.MockSearcherFactory;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class TestSearcherCache
{
    /**
     * Check that the ThreadLocalSearcherCache is only cached once per request-cache
     */
    @Test
    public void testGetSearcher() throws IOException
    {
        final Supplier<IndexSearcher> supplier = new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                return MockSearcherFactory.getCleanSearcher();
            }
        };
        final SearcherCache cache = SearcherCache.getThreadLocalCache();
        final IndexSearcher searcher = cache.retrieveIssueSearcher(supplier);
        final IndexSearcher anotherSearcher = cache.retrieveIssueSearcher(supplier);

        assertSame(searcher, anotherSearcher);

        cache.closeSearchers();
        final IndexSearcher newSearcher = cache.retrieveIssueSearcher(supplier);

        assertNotSame(searcher, newSearcher);
    }

    /**
     * Check that the ThreadLocalSearcherCache is only cached once per request-cache
     */
    @Test
    public void testGetCommentSearcher() throws IOException
    {
        final Supplier<IndexSearcher> supplier = new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                return MockSearcherFactory.getCleanSearcher();
            }
        };
        final SearcherCache cache = SearcherCache.getThreadLocalCache();
        final IndexSearcher searcher = cache.retrieveCommentSearcher(supplier);
        final IndexSearcher anotherSearcher = cache.retrieveCommentSearcher(supplier);

        assertSame(searcher, anotherSearcher);

        cache.closeSearchers();
        final IndexSearcher newSearcher = cache.retrieveCommentSearcher(supplier);

        assertNotSame(searcher, newSearcher);
    }

    /**
     * Check that the ThreadLocalSearcherCache is only cached once per requestcache
     */
    @Test
    public void testGetReader() throws IOException
    {
        final Supplier<IndexSearcher> supplier = new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                return MockSearcherFactory.getCleanSearcher();
            }
        };
        final SearcherCache cache = SearcherCache.getThreadLocalCache();
        final IndexReader reader = cache.retrieveIssueReader(supplier);
        final IndexReader anotherReader = cache.retrieveIssueReader(supplier);

        assertSame(reader, anotherReader);

        cache.closeSearchers();
        final IndexReader newReader = cache.retrieveIssueReader(supplier);

        assertNotSame(reader, newReader);
    }

    @Test
    public void testResetSearchers() throws IOException
    {
        final SearcherCache cache = SearcherCache.getThreadLocalCache();
        cache.closeSearchers();

        final MockControl mockIssueSearcherControl = MockClassControl.createControl(IndexSearcher.class);
        final IndexSearcher issueSearcher = (IndexSearcher) mockIssueSearcherControl.getMock();
        issueSearcher.close();
        mockIssueSearcherControl.replay();

        final MockControl mockCommentSearcherControl = MockClassControl.createControl(IndexSearcher.class);
        final IndexSearcher commentSearcher = (IndexSearcher) mockCommentSearcherControl.getMock();
        commentSearcher.close();
        mockCommentSearcherControl.replay();

        cache.retrieveCommentSearcher(new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                return commentSearcher;
            }
        });
        cache.retrieveIssueSearcher(new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                return issueSearcher;
            }
        });
        cache.closeSearchers();

        mockIssueSearcherControl.verify();
        mockCommentSearcherControl.verify();
    }

    @Test
    public void testResetNullSearchers() throws IOException
    {
        //nothing to assert really.  Just need to make sure this test passes without any exceptions.
        SearcherCache.getThreadLocalCache().closeSearchers();
    }
}
