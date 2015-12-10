package com.atlassian.jira.index;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import com.atlassian.instrumentation.Counter;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.index.DefaultIndexEngine.FlushPolicy;
import com.atlassian.jira.index.Index.UpdateMode;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import junit.framework.AssertionFailedError;

import static com.atlassian.jira.util.searchers.MockSearcherFactory.getCleanSearcher;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultIndexEngine
{
    @Mock
    private InstrumentRegistry instrumentRegistry;
    @Mock
    private Counter counter;
    @Mock
    private Supplier<IndexSearcher> indexSearcherSupplier;

    @Before
    public void setUp() throws Exception
    {
        when(instrumentRegistry.pullCounter(any(String.class))).thenReturn(counter);
        final MockComponentWorker worker = new MockComponentWorker();
        worker.addMock(InstrumentRegistry.class, instrumentRegistry);
        ComponentAccessor.initialiseWorker(worker);
    }

    @Test
    public void testSearcherClosed() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final DefaultIndexEngine engine = new DefaultIndexEngine(toFactory(new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                return getCleanSearcher();
            }
        }), new Function<Index.UpdateMode, Writer>()
        {
            public Writer get(final Index.UpdateMode mode)
            {
                return new WriterWrapper(configuration, mode, indexSearcherSupplier);
            }
        }, configuration, FlushPolicy.NONE);

        final IndexSearcher searcher = engine.getSearcher();
        assertSame("should be same until something is written", searcher, engine.getSearcher());
        touch(engine);

        final IndexSearcher newSearcher = engine.getSearcher();
        assertNotSame(searcher, newSearcher);
    }

    @Test
    public void testWriterNotFlushedForWritePolicyNone() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final DummyWriterWrapper writerWrapper = new DummyWriterWrapper(configuration, indexSearcherSupplier);
        final DefaultIndexEngine engine = new DefaultIndexEngine(new DummySearcherFactory(),
                new WriterWrapperFunction(writerWrapper), configuration, FlushPolicy.NONE);
        touch(engine);
    }

    @Test
    public void testWriterClosedForWritePolicyClose() throws Exception
    {
        final DefaultConfiguration configuration = new DefaultConfiguration(new RAMDirectory(), new StandardAnalyzer(LuceneVersion.get()));
        final CloseCountingWriterWrapper writerWrapper = new CloseCountingWriterWrapper(configuration, indexSearcherSupplier);
        final DefaultIndexEngine engine = new DefaultIndexEngine(new DummySearcherFactory(),
                new WriterWrapperFunction(writerWrapper), configuration, FlushPolicy.CLOSE);
        touch(engine);
        assertEquals(1, writerWrapper.getCloseCount());
    }

    @Test // JRA-29587
    public void testOldReaderClosedWhenSearcherClosedBeforeEngine() throws Exception
    {
        DefaultIndexEngine engine = getRamDirectory();
        IndexSearcher searcher = engine.getSearcher();
        IndexReader reader = searcher.getIndexReader();
        assertReaderOpen(reader);
        engine.close();
        assertReaderOpen(reader);

        searcher.close();
        assertReaderClosed(reader);
    }

    @Test // JRA-29587
    public void testOldReaderClosedWhenSearcherClosedAfterEngine() throws Exception
    {
        DefaultIndexEngine engine = getRamDirectory();
        IndexSearcher searcher = engine.getSearcher();
        IndexReader reader = searcher.getIndexReader();
        assertReaderOpen(reader);
        searcher.close();
        assertReaderOpen(reader);

        engine.close();
        assertReaderClosed(reader);
    }

    @Test
    public void testWriterAndSearcherClosedWhenClosed() throws Exception
    {
        final AtomicInteger searcherCloseCount = new AtomicInteger();
        final RAMDirectory directory = new RAMDirectory();
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer(LuceneVersion.get()));
        final CloseCountingWriterWrapper writerWrapper = new CloseCountingWriterWrapper(configuration, indexSearcherSupplier);
        final DefaultIndexEngine engine = new DefaultIndexEngine(toFactory(new Supplier<IndexSearcher>()
        {
            public IndexSearcher get()
            {
                try
                {
                    return new IndexSearcher(directory)
                    {
                        @Override
                        public void close() throws IOException
                        {
                            searcherCloseCount.incrementAndGet();
                            super.close();
                        }
                    };
                }
                catch (final IOException e)
                {
                    throw new RuntimeIOException(e);
                }
            }
        }), new WriterWrapperFunction(writerWrapper), configuration, FlushPolicy.CLOSE);

        touch(engine);
        assertEquals(1, writerWrapper.getCloseCount());
        assertEquals(0, searcherCloseCount.get());

        engine.getSearcher().close();
        engine.getSearcher().close();
        assertEquals(2, writerWrapper.getCloseCount());
        assertEquals(0, searcherCloseCount.get());

        engine.close();
        assertEquals(2, writerWrapper.getCloseCount());
        assertEquals(1, searcherCloseCount.get());
    }

    @Test
    public void testDirectoryCleaned() throws Exception
    {
        final RAMDirectory directory = new RAMDirectory();
        final StandardAnalyzer analyzer = new StandardAnalyzer(LuceneVersion.get());
        {
            IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), analyzer);
            final IndexWriter writer = new IndexWriter(directory, conf);
            writer.addDocument(new Document());
            writer.close();
        }
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, analyzer);
        final DefaultIndexEngine engine = new DefaultIndexEngine(new DummySearcherFactory(),
                new DummyWriterFunction(), configuration, FlushPolicy.NONE);

        assertEquals(1, new IndexSearcher(directory).getIndexReader().numDocs());
        engine.clean();
        assertEquals(0, new IndexSearcher(directory).getIndexReader().numDocs());
    }

    @Test
    public void testCanRecoverFromOutOfMemoryErrorInSearcher() throws Exception
    {
        final IndexSearcher indexSearcherBroken = mock(IndexSearcher.class, new Answer()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                throw new IllegalStateException("Oopsie!", new OutOfMemoryError("Ouch"));
            }
        });
        final IndexSearcher indexSearcherOkay = getCleanSearcher();

        // Return a broken one on the first call, then a good one, then null if we ever get called again (we shouldn't)
        when(indexSearcherSupplier.get()).thenReturn(indexSearcherBroken, indexSearcherOkay, null);

        final RAMDirectory directory = new RAMDirectory();
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer(LuceneVersion.get()));
        final WriterWrapper writerWrapper = new CloseCountingWriterWrapper(configuration, indexSearcherSupplier);
        final DefaultIndexEngine engine = new DefaultIndexEngine(toFactory(indexSearcherSupplier),
                new WriterWrapperFunction(writerWrapper), configuration, FlushPolicy.NONE);
        try
        {
            engine.getSearcher();
            fail("Expected an IllegalStateException");
        }
        catch (IllegalStateException ex)
        {
            // JDEV-28513: The failed initialization from the previous getSearcher() shouldn't be a permanent problem
            engine.getSearcher().close();
        }
    }

    @Test
    public void testCanRecoverFromIllegalStateExceptionInWriter() throws Exception
    {
        final IndexSearcher indexSearcherOkay = getCleanSearcher();

        when(indexSearcherSupplier.get()).thenReturn(indexSearcherOkay);

        final RAMDirectory directory = new RAMDirectory();
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer(LuceneVersion.get()));

        final WriterWrapper writerWrapper1 = mock(WriterWrapper.class);
        final WriterWrapper writerWrapper2 = mock(WriterWrapper.class);
        final WriterWrapperFunction writerWrapperFunction = mock(WriterWrapperFunction.class);

        when(writerWrapperFunction.get(any(UpdateMode.class)))
                .thenReturn(writerWrapper1)
                .thenReturn(writerWrapper2)
                .thenReturn(null);

        doThrow(new IllegalStateException("m00")).doNothing().when(writerWrapper1).commit();

        final DefaultIndexEngine engine = new DefaultIndexEngine(toFactory(indexSearcherSupplier),
                writerWrapperFunction, configuration, FlushPolicy.FLUSH);
        try
        {
            engine.getSearcher();
            fail("Expected an IllegalStateException");
        }
        catch (IllegalStateException ex)
        {
            // JDEV-29937: The failed initialization from the previous getSearcher() shouldn't be a permanent problem,
            // and it should have replaced the writer with the clean one
            engine.getSearcher().close();
        }

        final InOrder inOrder = inOrder(writerWrapper1, writerWrapper2);
        inOrder.verify(writerWrapper1).commit();
        inOrder.verify(writerWrapper1).close();
        inOrder.verify(writerWrapper2).commit();
        verifyNoMoreInteractions(writerWrapper1, writerWrapper2);
    }


    @Test
    public void testDirectoryCleanThrowsRuntimeIO() throws Exception
    {
        final RAMDirectory directory = new RAMDirectory()
        {
            @Override
            public IndexOutput createOutput(final String name) throws IOException
            {
                throw new IOException("haha");
            }
        };
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer(LuceneVersion.get()));
        final DefaultIndexEngine engine = new DefaultIndexEngine(new DummySearcherFactory(),
                new DummyWriterFunction(), configuration, FlushPolicy.NONE);

        try
        {
            engine.clean();
            fail("RuntimeIOException expected");
        }
        catch (final RuntimeIOException expected)
        {
            assertTrue(expected.getMessage().contains("haha"));
        }
    }

    /**
     * Test the simple flow of get searcher / write / close searcher / openSearcher
     * correctly gets a new reader and closes the old reader.
     * @throws Exception
     */
    @Test
    public void testSimpleFlowReaderIsClosed() throws Exception
    {
        final DefaultIndexEngine engine = getRamDirectory();

        final IndexSearcher searcher = engine.getSearcher();
        final IndexReader reader = searcher.getIndexReader();
        writeTestDocument(engine);
        searcher.close();

        final IndexSearcher newSearcher = engine.getSearcher();
        assertNotSame(searcher, newSearcher);
        final IndexReader newReader = newSearcher.getIndexReader();
        assertNotSame(reader, newReader);
        assertReaderClosed(reader);
        assertReaderOpen(newReader);
    }

    /**
     * Test se just get the same searcher and reader when there are no writes.
     * @throws Exception
     */
    @Test
    public void testMultipleSearchersWithoutWrites() throws Exception
    {
        final DefaultIndexEngine engine = getRamDirectory();

        final IndexSearcher searcher = engine.getSearcher();
        final IndexReader reader = searcher.getIndexReader();
        searcher.close();

        final IndexSearcher newSearcher = engine.getSearcher();
        assertSame(searcher, newSearcher);
        final IndexReader newReader = newSearcher.getIndexReader();
        assertSame(reader, newReader);
        assertReaderOpen(reader);
    }

    /**
     * Test the old reader is still open until all searchers using it are closed
     * @throws Exception
     */
    @Test
    public void testOldReaderStillOpenTillAllSearchersClosed() throws Exception
    {
        final DefaultIndexEngine engine = getRamDirectory();

        final IndexSearcher oldSearcher1 = engine.getSearcher();
        final IndexSearcher oldSearcher2 = engine.getSearcher();
        final IndexSearcher oldSearcher3 = engine.getSearcher();
        final IndexReader reader = oldSearcher1.getIndexReader();
        assertSame("should be same until something is written", oldSearcher1, oldSearcher2);
        assertSame("should be same until something is written", oldSearcher1, oldSearcher3);

        writeTestDocument(engine);

        oldSearcher1.close();

        final IndexSearcher newSearcher = engine.getSearcher();
        assertNotSame(oldSearcher1, newSearcher);
        final IndexReader newReader = newSearcher.getIndexReader();
        assertNotSame(reader, newReader);
        assertReaderOpen(reader);
        assertReaderOpen(newReader);
        oldSearcher2.close();
        assertReaderOpen(reader);
        oldSearcher3.close();
        assertReaderClosed(reader);
        assertReaderOpen(newReader);
    }

    @Test
    public void testToleratesAlreadyClosedExceptionOnReopen() throws Exception
    {
        final DefaultIndexEngine engine = getRamDirectory();
        final IndexSearcher oldSearcher = engine.getSearcher();

        final IndexReader oldReader = oldSearcher.getIndexReader();
        oldReader.close();
        oldSearcher.close();

        writeTestDocument(engine);
        assertReaderClosed(oldReader);

        engine.getSearcher();
    }
    /**
     * Test the old reader is still open until all searchers using it are closed
     * @throws Exception
     */
    @Test
    public void testMultiWritesBetweenSearcherCloses() throws Exception
    {
        final DefaultIndexEngine engine = getRamDirectory();

        final IndexSearcher oldSearcher1 = engine.getSearcher();
        final IndexSearcher oldSearcher2 = engine.getSearcher();
        assertSame("should be same until something is written", oldSearcher1, oldSearcher2);

        writeTestDocument(engine);
        writeTestDocument(engine);
        writeTestDocument(engine);
        writeTestDocument(engine);
        final IndexSearcher oldSearcher3 = engine.getSearcher();
        assertNotSame(oldSearcher1, oldSearcher3);
        writeTestDocument(engine);
        writeTestDocument(engine);
        final IndexSearcher oldSearcher4 = engine.getSearcher();
        final IndexSearcher oldSearcher5 = engine.getSearcher();
        assertNotSame(oldSearcher1, oldSearcher4);
        assertNotSame(oldSearcher3, oldSearcher4);
        assertSame(oldSearcher4, oldSearcher5);

        assertReaderOpen(oldSearcher1.getIndexReader());
        assertReaderOpen(oldSearcher2.getIndexReader());
        assertReaderOpen(oldSearcher3.getIndexReader());
        assertReaderOpen(oldSearcher4.getIndexReader());
        assertReaderOpen(oldSearcher5.getIndexReader());

        oldSearcher1.close();
        oldSearcher2.close();
        oldSearcher3.close();
        oldSearcher4.close();
        oldSearcher5.close();

        assertReaderClosed(oldSearcher1.getIndexReader());
        assertReaderClosed(oldSearcher2.getIndexReader());
        assertReaderClosed(oldSearcher3.getIndexReader());
        assertReaderOpen(oldSearcher4.getIndexReader());
        assertReaderOpen(oldSearcher5.getIndexReader());

        writeTestDocument(engine);
        assertReaderOpen(oldSearcher4.getIndexReader());
        // Getting a new searcher -> gets a new reader -> closes the old reader
        final IndexSearcher oldSearcher6 = engine.getSearcher();
        assertReaderClosed(oldSearcher4.getIndexReader());
        assertReaderClosed(oldSearcher5.getIndexReader());

    }

    private void assertReaderClosed(IndexReader reader) throws IOException
    {
        // If the reader is closed. flush will throw an AlreadyClosedException
        try
        {
            reader.flush();
            fail("The reader should have been closed after a write when we get a new searcher");
        }
        catch (AlreadyClosedException e)
        {
            assertTrue(true);
        }
    }

    private void assertReaderOpen(IndexReader reader) throws IOException
    {
        // If the reader is closed. flush will throw an AlreadyClosedException
        try
        {
            reader.flush();
        }
        catch (AlreadyClosedException e)
        {
            fail("The reader should not have been closed.");
        }
    }

    private void writeTestDocument(DefaultIndexEngine engine) throws IOException
    {
        final Document d = new Document();
        d.add(new Field("test", "bytes".getBytes()));
        engine.write(Operations.newCreate(d, UpdateMode.INTERACTIVE));
    }

    private DefaultIndexEngine getRamDirectory() throws IOException
    {
        final RAMDirectory directory = new RAMDirectory();
        IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), new StandardAnalyzer(LuceneVersion.get()));
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        new IndexWriter(directory, conf).close();
        final DefaultConfiguration configuration = new DefaultConfiguration(directory, new StandardAnalyzer(LuceneVersion.get()));
        return new DefaultIndexEngine(configuration, FlushPolicy.FLUSH);
    }

    @After
    public void teardown() throws Exception
    {
        ComponentAccessor.initialiseWorker(null);
    }

    private void touch(DefaultIndexEngine engine) throws IOException
    {
        engine.write(new Index.Operation()
        {
            @Override
            void perform(@Nonnull final Writer writer)
            {}

            @Override
            UpdateMode mode()
            {
                return UpdateMode.INTERACTIVE;
            }
        });
    }

    private static class DummySearcherFactory implements DefaultIndexEngine.SearcherFactory
    {
        public void release() {}
        public IndexSearcher get()
        {
            throw new AssertionFailedError("no searcher required");
        }
    }

    private static class DummyWriterFunction implements Function<Index.UpdateMode, Writer>
    {
        public Writer get(final Index.UpdateMode mode)
        {
            throw new AssertionFailedError("no writer required");
        }
    }

    private static class WriterWrapperFunction implements Function<UpdateMode, Writer>
    {
        private final WriterWrapper writerWrapper;

        WriterWrapperFunction(WriterWrapper writerWrapper)
        {
            this.writerWrapper = writerWrapper;
        }

        public Writer get(final Index.UpdateMode mode)
        {
            return writerWrapper;
        }
    }

    private static class DummyWriterWrapper extends WriterWrapper
    {
        DummyWriterWrapper(final Configuration configuration, Supplier<IndexSearcher> indexSearcherSupplier)
        {
            super(configuration, UpdateMode.INTERACTIVE, indexSearcherSupplier);
        }

        @Override
        public void close()
        {
            throw new AssertionFailedError("should not close!");
        }

        @Override
        public void commit()
        {
            throw new AssertionFailedError("should not commit!");
        }
    }

    private static class CloseCountingWriterWrapper extends WriterWrapper
    {
        private final AtomicInteger count = new AtomicInteger();

        CloseCountingWriterWrapper(final Configuration configuration, Supplier<IndexSearcher> indexSearcherSupplier)
        {
            super(configuration, UpdateMode.INTERACTIVE, indexSearcherSupplier);
        }

        @Override
        public void close()
        {
            count.incrementAndGet();
            super.close();
        }

        public int getCloseCount()
        {
            return count.get();
        }

        @Override
        public void commit()
        {
            throw new AssertionFailedError("should not commit!");
        }
    }

    private static DefaultIndexEngine.SearcherFactory toFactory(final Supplier<IndexSearcher> indexSearcherSupplier)
    {
        if (indexSearcherSupplier instanceof DefaultIndexEngine.SearcherFactory)
        {
            return (DefaultIndexEngine.SearcherFactory)indexSearcherSupplier;
        }
        return new DefaultIndexEngine.SearcherFactory()
        {
            public void release() {}
            public IndexSearcher get()
            {
                return indexSearcherSupplier.get();
            }
        };
    }
}
