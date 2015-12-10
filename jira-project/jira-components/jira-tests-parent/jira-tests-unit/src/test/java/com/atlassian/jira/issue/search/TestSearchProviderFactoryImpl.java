/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.search;

import java.io.IOException;

import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.index.IssueIndexManager;

import com.mockobjects.dynamic.Mock;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class TestSearchProviderFactoryImpl
{
    private SearchProviderFactoryImpl searchProviderFactory;
    private Mock mockIndexManager;

    @Before
    public void setUp() throws Exception
    {
        mockIndexManager = new Mock(IssueIndexManager.class);
        mockIndexManager.setStrict(true);

        searchProviderFactory = new SearchProviderFactoryImpl((IssueIndexManager) mockIndexManager.proxy());
    }

    @Test
    public void testGetNullSearcher()
    {
        try
        {
            searchProviderFactory.getSearcher(null);
            fail("UnsupportedOperationException");
        }
        catch (final UnsupportedOperationException yay)
        {}
    }

    @Test
    public void testGetEmpySearcher()
    {
        try
        {
            searchProviderFactory.getSearcher("");
            fail("UnsupportedOperationException");
        }
        catch (final UnsupportedOperationException yay)
        {}
    }

    @Test
    public void testGetUnknownSearcher()
    {
        try
        {
            searchProviderFactory.getSearcher("hi there");
            fail("UnsupportedOperationException");
        }
        catch (final UnsupportedOperationException yay)
        {}
    }

    @Test
    public void testGetBlahSearcher()
    {
        try
        {
            searchProviderFactory.getSearcher("blah");
            fail("UnsupportedOperationException");
        }
        catch (final UnsupportedOperationException yay)
        {}
    }

    @Test
    public void testGetIssueSearcher() throws Exception
    {
        final IndexSearcher expectedSearcher = createIndexSearcher();
        try
        {
            mockIndexManager.expectAndReturn("getIssueSearcher", expectedSearcher);
            final IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
            verifyMocks();
            assertNotNull(searcher);
            assertSame(expectedSearcher, searcher);
        }
        finally
        {
            expectedSearcher.close();
        }
    }

    @Test
    public void testGetSearcherThrowsSameException() throws Exception
    {
        final IllegalMonitorStateException expectedException = new IllegalMonitorStateException("something went wrong");
        try
        {
            mockIndexManager.expectAndThrow("getIssueSearcher", expectedException);
            searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
            fail("LuceneException expected");
        }
        catch (final IllegalMonitorStateException e)
        {
            assertSame(expectedException, e);
        }
        verifyMocks();
    }

    @Test
    public void testGetSearcherThrowsSomeRuntimeException() throws Exception
    {
        final NullPointerException expectedException = new NullPointerException("something went wrong");
        try
        {
            mockIndexManager.expectAndThrow("getIssueSearcher", expectedException);
            searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
            fail("NullPointerException expected");
        }
        catch (final NullPointerException e)
        {
            assertSame(expectedException, e);
        }

        verifyMocks();
    }

    @Test
    public void testGetCommentSearcher() throws Exception
    {
        final IndexSearcher expectedSearcher = createIndexSearcher();
        try
        {
            mockIndexManager.expectAndReturn("getCommentSearcher", expectedSearcher);
            final IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.COMMENT_INDEX);
            assertNotNull(searcher);
            assertSame(expectedSearcher, searcher);

            verifyMocks();
        }
        finally
        {
            expectedSearcher.close();
        }
    }

    @Test
    public void testGetCommentSearcherThrowsSameException() throws Exception
    {
        final IllegalMonitorStateException expectedException = new IllegalMonitorStateException("something went wrong");
        try
        {
            mockIndexManager.expectAndThrow("getCommentSearcher", expectedException);
            searchProviderFactory.getSearcher(SearchProviderFactory.COMMENT_INDEX);
            fail("IllegalMonitorStateException expected");
        }
        catch (final IllegalMonitorStateException e)
        {
            assertSame(expectedException, e);
        }

        verifyMocks();
    }

    @Test
    public void testGetCommentSearcherThrowsSomeRuntimeException() throws Exception
    {
        final NullPointerException expectedException = new NullPointerException("something went wrong");
        try
        {
            mockIndexManager.expectAndThrow("getCommentSearcher", expectedException);
            searchProviderFactory.getSearcher(SearchProviderFactory.COMMENT_INDEX);
            fail("NullPointerException expected");
        }
        catch (final NullPointerException e)
        {
            assertSame(expectedException, e);
        }

        verifyMocks();
    }

    private void verifyMocks()
    {
        mockIndexManager.verify();
    }

    private IndexSearcher createIndexSearcher() throws CorruptIndexException, IOException, LockObtainFailedException
    {
        final RAMDirectory directory = new RAMDirectory();
        IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), null);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        new IndexWriter(directory, conf).close();
        return new IndexSearcher(directory);
    }
}
