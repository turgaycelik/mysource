package com.atlassian.jira.index;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.instrumentation.Counter;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.index.DelayCloseable.AlreadyClosedException;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.util.Closeable;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.searchers.MockSearcherFactory;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDelayCloseSearcher
{
    @Mock
    private InstrumentRegistry instrumentRegistry;
    @Mock
    private Counter counter;

    @Before
    public void setUp() throws Exception
    {
        when(instrumentRegistry.pullCounter(any(String.class))).thenReturn(counter);
        final MockComponentWorker worker = new MockComponentWorker();
        worker.addMock(InstrumentRegistry.class, instrumentRegistry);
        ComponentAccessor.initialiseWorker(worker);
    }

    @Test
    public void testNullSearcher() throws Exception
    {
        try
        {
            new DelayCloseSearcher(null, new Closeable()
            {
                public void close()
                {}
            });
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNullCloseable() throws Exception
    {
        try
        {
            new DelayCloseSearcher(new IndexSearcher(MockSearcherFactory.getCleanRAMDirectory()), null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testCloseWhenDone() throws Exception
    {
        final AtomicBoolean searcherClosed = new AtomicBoolean();
        final AtomicBoolean closeCalled = new AtomicBoolean();
        Directory dir = MockSearcherFactory.getCleanRAMDirectory();
        IndexReader rdr = IndexReader.open(dir, true);
        final DelayCloseSearcher searcher = new DelayCloseSearcher(new IndexSearcher(rdr)
        {
            @Override
            public void close() throws IOException
            {
                searcherClosed.set(true);
                super.close();
            }
        }, new Closeable()
        {
            public void close()
            {
                closeCalled.set(true);
            }
        });
        searcher.open();
        searcher.open();
        searcher.closeWhenDone();
        assertFalse(searcherClosed.get());
        assertFalse(closeCalled.get());
        assertFalse(searcher.isClosed());
        searcher.close();
        assertFalse(searcherClosed.get());
        assertFalse(closeCalled.get());
        assertFalse(searcher.isClosed());
        searcher.close();
        assertTrue(searcherClosed.get());
        assertTrue(closeCalled.get());
        assertTrue(searcher.isClosed());
    }

    @Test
    public void testCloseThrowsIOException() throws Exception
    {
        final IOException blah = new IOException("blah!");
        final DelayCloseSearcher searcher = new DelayCloseSearcher(new IndexSearcher(MockSearcherFactory.getCleanRAMDirectory())
        {
            @Override
            public void close() throws IOException
            {
                throw blah;
            }
        }, new Closeable()
        {
            public void close()
            {}
        });
        try
        {
            searcher.closeWhenDone();
            fail("RuntimeIOException expected");
        }
        catch (final RuntimeIOException expected)
        {
            assertSame(blah, expected.getCause());
        }
    }

    @Test
    public void testOpenThrowsAlreadyClosedException() throws Exception
    {
        Directory dir = MockSearcherFactory.getCleanRAMDirectory();
        IndexReader rdr = IndexReader.open(dir, true);
        final DelayCloseSearcher searcher = new DelayCloseSearcher(new IndexSearcher(rdr)
        {}, new Closeable()
        {
            public void close()
            {}
        });
        searcher.closeWhenDone();
        try
        {
            searcher.open();
            fail("AlreadyClosedException expected");
        }
        catch (final AlreadyClosedException expected)
        {}
    }

    @Test
    public void testCloseWhenDoneClosesImmediatelyIfNotOpen() throws Exception
    {
        final AtomicBoolean closed = new AtomicBoolean();
        Directory dir = MockSearcherFactory.getCleanRAMDirectory();
        IndexReader rdr = IndexReader.open(dir, true);
        final DelayCloseSearcher searcher = new DelayCloseSearcher(new IndexSearcher(rdr), new Closeable()
        {
            public void close()
            {
                closed.set(true);
            }
        });
        searcher.closeWhenDone();
        assertTrue(closed.get());
    }

    @Test
    public void testCloseWhenDoneDoesNotCloseImmediatelyIfOpen() throws Exception
    {
        final AtomicBoolean closed = new AtomicBoolean();
        Directory dir = MockSearcherFactory.getCleanRAMDirectory();
        IndexReader rdr = IndexReader.open(dir, true);
        final DelayCloseSearcher searcher = new DelayCloseSearcher(new IndexSearcher(rdr), new Closeable()
        {
            public void close()
            {
                closed.set(true);
            }
        });
        searcher.open();
        searcher.closeWhenDone();
        assertFalse(closed.get());
        searcher.close();
        assertTrue(closed.get());
    }

    @After
    public void teardown() throws Exception
    {
        ComponentAccessor.initialiseWorker(null);
    }
}
