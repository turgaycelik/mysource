package com.atlassian.jira.index;

import com.atlassian.jira.config.util.IndexWriterConfiguration;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;
import com.google.common.annotations.VisibleForTesting;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

/**
 * {@link Writer} implementation that actually writes to an {@link IndexWriter}.
 */
class WriterWrapper implements Writer
{
    private static final Logger log = LoggerFactory.getLogger(WriterWrapper.class);

    private final IndexWriter writer;
    private final Supplier<IndexSearcher> indexSearcherSupplier;

    // for testing, can't make this accept an IndexWriter without making main constructor throw IOException
    @VisibleForTesting
    WriterWrapper(final Supplier<IndexWriter> writerFactory, Supplier<IndexSearcher> indexSearcherSupplier)
    {
        writer = writerFactory.get();
        this.indexSearcherSupplier = indexSearcherSupplier;
    }

    WriterWrapper(final @Nonnull Configuration configuration, final Index.UpdateMode mode, Supplier<IndexSearcher> indexSearcherSupplier)
    {
        this(new Supplier<IndexWriter>()
                {
                    public IndexWriter get()
                    {
                        try
                        {
                            IndexWriterConfiguration.WriterSettings writerSettings = configuration.getWriterSettings(mode);
                            IndexWriterConfig luceneConfig = writerSettings.getWriterConfiguration(configuration.getAnalyzer());
                            return new IndexWriter(configuration.getDirectory(), luceneConfig);
                        }
                        ///CLOVER:OFF
                        catch (final IOException e)
                        {
                            throw new RuntimeIOException(e);
                        }
                        ///CLOVER:ON
                    }
                }, indexSearcherSupplier);
    }



    public void addDocuments(@Nonnull final Collection<Document> documents) throws IOException
    {
        for (final Document document : documents)
        {
            writer.addDocument(notNull("document", document));
        }
    }

    public void deleteDocuments(final @Nonnull Term identifyingTerm) throws IOException
    {
        writer.deleteDocuments(notNull("identifyingTerm", identifyingTerm));
    }

    public void updateDocuments(final @Nonnull Term identifyingTerm, final @Nonnull Collection<Document> documents) throws IOException
    {
        if (documents.size() == 1)
        {
            writer.updateDocument(identifyingTerm, documents.iterator().next());
        }
        else
        {
            writer.deleteDocuments(identifyingTerm);
            for (final Document document : documents)
            {
                writer.addDocument(document);
            }
        }
    }

    public void updateDocumentConditionally(@Nonnull Term identifyingTerm, @Nonnull Document document, @Nonnull String optimisticLockField) throws IOException
    {
        // use the specified field as an optimistic locking check
        BooleanQuery updateCondition = new BooleanQuery(true);
        updateCondition.add(new BooleanClause(new TermQuery(identifyingTerm), MUST));
        updateCondition.add(new BooleanClause(new TermRangeQuery(optimisticLockField, null, document.get(optimisticLockField), true, true), MUST));

        // try to reuse searchers
        IndexSearcher searcher = indexSearcherSupplier.get();
        try
        {
            // if we have a matching document, that means that the document we are about to write is at least as
            // up-to-date as what is already in there (this check only works because there is a single thread updating the
            // index). so if we have a hit then go ahead and update, otherwise this update is a NOP
            TopDocs topDocs = searcher.search(updateCondition, 1);
            if (topDocs.totalHits > 0)
            {
                writer.updateDocument(identifyingTerm, document);
            }
        }
        finally
        {
            closeQuietly(searcher);
        }
    }

    public void optimize() throws IOException
    {
        writer.optimize();
    }

    public void commit()
    {
        try
        {
            writer.commit();
        }
        catch (final IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }

    public void close()
    {
        try
        {
            writer.close();
        }
        catch (final IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }

    private static void closeQuietly(IndexSearcher searcher)
    {
        try
        {
            if (searcher != null)
            {
                searcher.close();
            }
        }
        catch (IOException e)
        {
            log.error("Error closing: " + searcher, e);
        }
    }
}