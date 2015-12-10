package com.atlassian.jira.index;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.concurrent.ResettableLazyReference;
import com.atlassian.jira.index.DelayCloseable.AlreadyClosedException;
import com.atlassian.jira.index.Index.Operation;
import com.atlassian.jira.util.Closeable;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.jira.util.Supplier;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import net.jcip.annotations.ThreadSafe;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Thread-safe container that manages our current {@link IndexSearcher} and {@link Writer}.
 * <p>
 * Gets passed searcher and writer factories that create new instances of these when required.
 */
@ThreadSafe
class DefaultIndexEngine implements DefaultIndex.Engine
{
    private static final Logger log = Logger.getLogger(DefaultIndexEngine.class);

    /**
     * How to perform an actual write to the writer.
     */
    static enum FlushPolicy
    {
        /**
         * Do not flush or close.
         */
        NONE()
        {
            @Override
            void commit(final WriterReference writer)
            {}
        },
        /**
         * Commit the writer's pending updates, do not close.
         */
        FLUSH()
        {
            @Override
            void commit(final WriterReference writer)
            {
                writer.commit();
            }
        },

        /**
         * Close the writer after performing the write.
         */
        CLOSE()
        {
            @Override
            @ClusterSafe ("Indexing")
            synchronized void commit(final WriterReference writer)
            {
                writer.close();
            }
        };

        void perform(final Operation operation, final WriterReference writer) throws IOException
        {
            try
            {
                operation.perform(writer.get(operation.mode()));
            }
            finally
            {
                commit(writer);
            }
        }

        abstract void commit(final WriterReference writer);
    }

    private final WriterReference writerReference;
    private final SearcherFactory searcherFactory;
    private final SearcherReference searcherReference;
    private final FlushPolicy writePolicy;
    private final Configuration configuration;

    /**
     * Production ctor.
     *
     * @param configuration the {@link Directory} and {@link Analyzer}
     * @param writePolicy when to flush writes
     */
    DefaultIndexEngine(final @Nonnull Configuration configuration, final @Nonnull FlushPolicy writePolicy)
    {
        this(new SearcherFactoryImpl(configuration), null, configuration, writePolicy);
    }

    /**
     * Main ctor.
     *
     * @param searcherFactory for creating {@link IndexSearcher searchers}
     * @param writerFactory for creating Writer instances of the correct mode
     * @param configuration the {@link Directory} and {@link Analyzer}
     * @param writePolicy when to flush writes
     */
    DefaultIndexEngine(final @Nonnull SearcherFactory searcherFactory, @Nullable final Function<Index.UpdateMode, Writer> writerFactory, final @Nonnull Configuration configuration, final @Nonnull FlushPolicy writePolicy)
    {
        this.writePolicy = notNull("writePolicy", writePolicy);
        this.configuration = notNull("configuration", configuration);
        this.searcherFactory = notNull("searcherFactory", searcherFactory);
        this.searcherReference = new SearcherReference(searcherFactory);
        this.writerReference = new WriterReference(writerFactory == null ? new DefaultWriterFactory() : writerFactory);
    }

    /**
     * leak a {@link IndexSearcher}. Must get closed after usage.
     */
    @Nonnull
    public IndexSearcher getSearcher()
    {
        // mode is irrelevant to a Searcher
        return searcherReference.get(Index.UpdateMode.INTERACTIVE);
    }

    public void clean()
    {
        close();
        try
        {
            IndexWriterConfig luceneConfig = new IndexWriterConfig(LuceneVersion.get(), configuration.getAnalyzer());
            luceneConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            new IndexWriter(configuration.getDirectory(), luceneConfig).close();
        }
        catch (final IOException e)
        {
            throw new RuntimeIOException(e);
        }
    }

    public void write(@Nonnull final Operation operation) throws IOException
    {
        try
        {
            writePolicy.perform(operation, writerReference);
        }
        finally
        {
            searcherReference.close();
        }
    }

    public void close()
    {
        writerReference.close();
        searcherReference.close();
        searcherFactory.release();  // JRA-29587
    }

    /**
     * Thread-safe holder of the current Searcher
     */
    @ThreadSafe
    private class SearcherReference extends ReferenceHolder<DelayCloseSearcher>
    {
        private final SearcherFactory searcherSupplier;

        SearcherReference(@Nonnull final SearcherFactory searcherSupplier)
        {
            this.searcherSupplier = notNull("searcherSupplier", searcherSupplier);
        }

        @Override
        DelayCloseSearcher doCreate(final Index.UpdateMode mode)
        {
            // To create a valid searcher, we need a valid writer.
            // Getting the writer reference, here ensures that.
            writerReference.get(mode);
            writePolicy.commit(writerReference);
            return new DelayCloseSearcher(searcherSupplier.get());
        }

        @Override
        DelayCloseSearcher open(final DelayCloseSearcher searcher)
        {
            searcher.open();
            return searcher;
        }

        @Override
        void doClose(final DelayCloseSearcher searcher)
        {
            searcher.closeWhenDone();
        }
    }

    /**
     * Thread-safe holder of the current Writer
     */
    @ThreadSafe
    private static class WriterReference extends ReferenceHolder<Writer>
    {
        private final Function<Index.UpdateMode, Writer> writerFactory;

        WriterReference(@Nonnull final Function<Index.UpdateMode, Writer> writerFactory)
        {
            this.writerFactory = notNull("writerFactory", writerFactory);
        }

        public void commit()
        {
            final Option<Writer> writerOption = get();
            if (writerOption.isDefined())
            {
                try
                {
                    writerOption.get().commit();
                }
                catch (IllegalStateException ise)
                {
                    log.error("Hit an exception committing writes to the index; discarding the current writer!", ise);
                    safeClose(writerOption.get());
                    throw ise;
                }
            }
        }

        @Override
        Writer doCreate(final Index.UpdateMode mode)
        {
            return writerFactory.get(mode);
        }

        @Override
        void doClose(final Writer writer)
        {
            writer.close();
        }

        @Override
        Writer open(final Writer writer)
        {
            return writer;
        }

    }

    private class DefaultWriterFactory implements Function<Index.UpdateMode, Writer>
    {
        @Override
        public Writer get(Index.UpdateMode mode)
        {
            // be default, create a writer wrapper that has access to this engine's searcher
            return new WriterWrapper(configuration, mode, new Supplier<IndexSearcher>() {
                @Override
                public IndexSearcher get()
                {
                    return getSearcher();
                }
            });
        }
    }

    static abstract class ReferenceHolder<T> implements Function<Index.UpdateMode, T>, Closeable
    {
        private final ResettableLazyReference<T> reference = new ResettableLazyReference<T>();
        private final Effect<T> resetEffect = new Effect<T>()
        {
            @Override
            public void apply(final T localReference)
            {
                try
                {
                    doClose(localReference);
                }
                catch (final RuntimeException re)
                {
                    log.debug("Error closing reference", re);
                }
            }
        };

        /**
         * Close if and only if the specified expected value is the currently held reference.
         */
        final void safeClose(final T expected)
        {
            reference.safeReset(expected).foreach(resetEffect);
        }

        public final void close()
        {
            reference.reset().foreach(resetEffect);
        }

        abstract void doClose(T element);

        @Override
        public final T get(final Index.UpdateMode mode)
        {
            while (true)
            {
                try
                {
                    return open(reference.getOrCreate(new Supplier<T>()
                    {
                        @Override
                        public T get()
                        {
                            return doCreate(mode);
                        }
                    }));
                }
                catch (final AlreadyClosedException ace)
                {
                    log.debug("Already closed", ace);
                }
                // in the rare case of a race condition, try again
            }
        }

        abstract T doCreate(Index.UpdateMode mode);

        abstract T open(T element);

        final Option<T> get()
        {
            return reference.get();
        }
    }

    static interface SearcherFactory extends Supplier<IndexSearcher>
    {
        void release();
    }

    static class SearcherFactoryImpl implements SearcherFactory
    {
        private final Configuration configuration;
        /* This is already held in the thread safe SearcherReference. */
        private volatile IndexReader oldReader = null;

        SearcherFactoryImpl(final Configuration configuration)
        {
            this.configuration = notNull("configuration", configuration);
        }

        public IndexSearcher get()
        {
            try
            {
                IndexReader reader;
                if (oldReader != null)
                {
                    try
                    {
                        reader = oldReader.reopen(true);
                        // If we actually get a new reader, we must close the old one
                        //noinspection ObjectEquality
                        if (reader != oldReader)
                        {
                            // This will really close only when the ref count goes to zero.
                            try
                            {
                                oldReader.close();
                            }
                            catch (org.apache.lucene.store.AlreadyClosedException ace)
                            {
                                log.debug("Tried to close an already closed reader.", ace);
                            }
                        }
                    }
                    catch (org.apache.lucene.store.AlreadyClosedException ignore)
                    {
                        // JRADEV-7825: Really this shouldn't happen unless someone closes the reader from outside all
                        // the inscrutable code in this class (and its friends) but
                        // don't worry, we will just open a new one in that case.
                        log.warn("Tried to reopen the IndexReader, but it threw AlreadyClosedException. Opening a fresh IndexReader.");
                        reader = IndexReader.open(configuration.getDirectory(), true);
                    }
                }
                else
                {
                    reader = IndexReader.open(configuration.getDirectory(), true);
                }
                oldReader = reader;
                return new IndexSearcher(reader);
            }
            catch (final IOException e)
            {
                ///CLOVER:OFF
                throw new RuntimeIOException(e);
                ///CLOVER:ON
            }
        }

        public void release()
        {
            final IndexReader reader = oldReader;
            if (reader != null)
            {
                try
                {
                    reader.close();
                    oldReader = null;
                }
                catch (org.apache.lucene.store.AlreadyClosedException ignore)
                {
                    // Ignore
                }
                catch (IOException e)
                {
                    ///CLOVER:OFF
                    throw new RuntimeException(e);
                    ///CLOVER:ON
                }
            }
        }
    }
}
