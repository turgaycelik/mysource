package com.atlassian.jira.index;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.atlassian.jira.mock.Strict;
import com.atlassian.jira.util.searchers.MockSearcherFactory;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TestDefaultManager
{
    @Test
    public void testDeleteCallsClean() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine()
        {
            @Override
            public void clean()
            {
                called.set(true);
            }
        }, new MockIndex());

        assertThat("Not called yet", called.get(), is(false));
        manager.deleteIndexDirectory();
        assertThat("Called now", called.get(), is(true));
    }

    @Test
    public void testGetSearcherCallsEngine() throws Exception
    {
        final IndexSearcher searcher = mock(IndexSearcher.class, new Strict());
        final AtomicBoolean called = new AtomicBoolean();
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine()
        {
            @Nonnull
            @Override
            public IndexSearcher getSearcher()
            {
                called.set(true);
                return searcher;
            }
        }, new MockIndex());

        assertThat("Not called yet", called.get(), is(false));
        assertThat("Where'd this come from?!", manager.openSearcher(), sameInstance(searcher));
        assertThat("Called now", called.get(), is(true));
    }

    @Test
    public void testGetNumDocsCallsEngineSearcher() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine()
        {
            @Nonnull
            @Override
            public IndexSearcher getSearcher()
            {
                called.set(true);
                try
                {
                    return new IndexSearcher(configuration.getDirectory());
                }
                catch (final IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }, new MockIndex());

        assertThat("Not called yet", called.get(), is(false));
        assertThat("num docs", manager.getNumDocs(), is(0));
        assertThat("Called now", called.get(), is(true));
    }

    @Test
    public void testCloseCallsEngine() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine(), new MockIndex()
        {
            @Override
            public void close()
            {
                called.set(true);
            }
        });
        assertFalse(called.get());
        manager.close();
        assertTrue(called.get());
    }

    @Test
    public void testGetIndexReturnsSame() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final MockIndex index = new MockIndex();
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine(), index);
        assertSame(index, manager.getIndex());
    }

    @Test
    public void testIndexCreatedTrue() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(MockSearcherFactory.getCleanRAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine(), new MockIndex());
        assertTrue(manager.isIndexCreated());
    }

    @Test
    public void testIndexCreatedFalse() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final DefaultManager manager = new DefaultManager(configuration, new MockIndexEngine(), new MockIndex());
        assertFalse(manager.isIndexCreated());
    }
}
