package com.atlassian.jira.index;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.index.Index.Operation;
import com.atlassian.jira.index.Index.UpdateMode;

import com.google.common.collect.Lists;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TestOperations
{
    @Test
    public void testDelete() throws Exception
    {
        final Term term = new Term("one", "delete");
        final Index.Operation delete = Operations.newDelete(term, UpdateMode.BATCH);
        delete.perform(new MockWriter()
        {
            @Override
            public void deleteDocuments(final Term t) throws IOException
            {
                assertSame(term, t);
            }
        });
    }

    @Test
    public void testCreate() throws Exception
    {
        final Document document = new Document();
        final Index.Operation create = Operations.newCreate(document, UpdateMode.BATCH);
        create.perform(new MockWriter()
        {
            @Override
            public void addDocuments(final Collection<Document> d) throws IOException
            {
                for (final Document doc : d)
                {
                    assertSame(document, doc);
                }
            }
        });
    }

    @Test
    public void testCreateMany() throws Exception
    {
        final Collection<Document> documents = Lists.newArrayList(new Document(), new Document());
        final Index.Operation create = Operations.newCreate(documents, UpdateMode.BATCH);
        create.perform(new MockWriter()
        {
            @Override
            public void addDocuments(final Collection<Document> d) throws IOException
            {
                assertEquals(documents, d);
            }
        });
    }

    @Test
    public void testUpdate() throws Exception
    {
        final Term term = new Term("one", "delete");
        final Document document = new Document();
        final Index.Operation create = Operations.newUpdate(term, document, UpdateMode.BATCH);
        create.perform(new MockWriter()
        {
            @Override
            public void updateDocuments(final Term t, final Collection<Document> d) throws IOException
            {
                assertSame(term, t);
                assertEquals(Lists.newArrayList(document), d);
            }
        });
    }

    @Test
    public void testUpdateMany() throws Exception
    {
        final Term term = new Term("one", "delete");
        final Collection<Document> documents = Lists.newArrayList(new Document(), new Document());
        final Index.Operation create = Operations.newUpdate(term, documents, UpdateMode.BATCH);
        create.perform(new MockWriter()
        {
            @Override
            public void updateDocuments(final Term t, final Collection<Document> d) throws IOException
            {
                assertSame(term, t);
                assertEquals(documents, d);
            }
        });
    }

    @Test
    public void testCompletionDelegate() throws Exception
    {
        final MockOperation delegate = new MockOperation();
        final AtomicBoolean ran = new AtomicBoolean();
        final Operation operation = Operations.newCompletionDelegate(delegate, new Runnable()
        {
            public void run()
            {
                ran.set(true);
            }
        });
        operation.perform(new MockWriter());

        assertTrue(delegate.isPerformed());
        assertTrue(ran.get());
        assertSame(delegate.mode(), operation.mode());
    }

    @Test
    public void testOptimize() throws Exception
    {
        final AtomicBoolean ran = new AtomicBoolean();
        final Index.Operation optimize = Operations.newOptimize();
        optimize.perform(new MockWriter()
        {
            @Override
            public void optimize() throws IOException
            {
                ran.set(true);
            }
        });
        assertTrue(ran.get());
        assertSame(UpdateMode.BATCH, optimize.mode());
    }
}
