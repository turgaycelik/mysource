package com.atlassian.jira.index;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.jira.config.util.IndexWriterConfiguration;
import com.atlassian.jira.config.util.IndexWriterConfiguration.WriterSettings;
import com.atlassian.jira.index.Index.UpdateMode;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.searchers.MockSearcherFactory;

import com.google.common.collect.Lists;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TestWriterWrapper
{
    static Configuration configuration()
    {
        return new Configuration()
        {
            final Directory directory = MockSearcherFactory.getCleanRAMDirectory();

            public Directory getDirectory()
            {
                return directory;
            }

            public Analyzer getAnalyzer()
            {
                return new StandardAnalyzer(LuceneVersion.get());
            }

            public WriterSettings getWriterSettings(final UpdateMode mode)
            {
                return IndexWriterConfiguration.Default.INTERACTIVE;
            }
        };
    }

    @Test
    public void testOptimize() throws Exception
    {
        final IOException called = new IOException();
        final Configuration configuration = configuration();
        IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), configuration.getAnalyzer());
        conf.setMergePolicy(new LogDocMergePolicy());
        final IndexWriter writer = new IndexWriter(configuration.getDirectory(), conf)
        {
            @Override
            public void optimize() throws CorruptIndexException, IOException
            {
                throw called;
            }
        };
        try
        {
            final WriterWrapper wrapper = createWriterWrapper(writer);

            try
            {
                wrapper.optimize();
            }
            catch (final IOException expected)
            {
                assertSame(called, expected);
            }
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCloseWrapsIOException() throws Exception
    {
        final IOException called = new IOException();
        final Configuration configuration = configuration();
        IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), configuration.getAnalyzer());
        conf.setMergePolicy(new LogDocMergePolicy());
        final IndexWriter writer = new IndexWriter(configuration.getDirectory(), conf)
        {
            @Override
            public void close() throws CorruptIndexException, IOException
            {
                super.close();
                throw called;
            }
        };
        final WriterWrapper wrapper = createWriterWrapper(writer);

        try
        {
            wrapper.close();
        }
        catch (final RuntimeIOException expected)
        {
            assertSame(called, expected.getCause());
        }
    }

    @Test
    public void testUpdate() throws Exception
    {
        final AtomicInteger add = new AtomicInteger();
        final AtomicInteger delete = new AtomicInteger();
        final AtomicInteger update = new AtomicInteger();
        final Configuration configuration = configuration();
        IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), configuration.getAnalyzer());
        conf.setMergePolicy(new LogDocMergePolicy());
        final IndexWriter writer = new IndexWriter(configuration.getDirectory(), conf)
        {
            @Override
            public void deleteDocuments(final Term term) throws CorruptIndexException, IOException
            {
                delete.incrementAndGet();
                super.deleteDocuments(term);
            }

            @Override
            public void addDocument(final Document doc) throws CorruptIndexException, IOException
            {
                add.incrementAndGet();
                super.addDocument(doc);
            }

            @Override
            public void updateDocument(final Term term, final Document doc) throws CorruptIndexException, IOException
            {
                update.incrementAndGet();
                super.updateDocument(term, doc);
            }
        };
        final WriterWrapper wrapper = createWriterWrapper(writer);

        wrapper.updateDocuments(new Term("blah", "blah"), Lists.newArrayList(new Document(), new Document(), new Document()));
        assertEquals(1, delete.get());
        assertEquals(3, add.get());
        assertEquals(0, update.get());
        wrapper.updateDocuments(new Term("blah", "blah"), Lists.newArrayList(new Document(), new Document()));
        assertEquals(2, delete.get());
        assertEquals(5, add.get());
        assertEquals(0, update.get());
        wrapper.updateDocuments(new Term("blah", "blah"), Lists.newArrayList(new Document()));
        assertEquals(2, delete.get());
        assertEquals(5, add.get());
        assertEquals(1, update.get());
    }

    private WriterWrapper createWriterWrapper(final IndexWriter writer)
    {
        return new WriterWrapper(new Supplier<IndexWriter>()
        {
            public IndexWriter get()
            {
                return writer;
            }
        }, null);
    }
}
