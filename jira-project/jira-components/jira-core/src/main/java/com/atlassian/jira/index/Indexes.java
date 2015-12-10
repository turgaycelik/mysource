package com.atlassian.jira.index;

import javax.annotation.Nonnull;

import com.atlassian.jira.index.DefaultIndexEngine.FlushPolicy;
import com.atlassian.jira.index.Index.Manager;
import com.atlassian.jira.index.Index.Result;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

/**
 * Static factory class for creating {@link Index} and {@link Manager}
 * instances.
 *
 * @since v4.0
 */
public class Indexes
{
    /**
     * Creates an index where the index operations are placed on a queue and the
     * actual work is done on a background thread. Any {@link Result} may be
     * waited on to make sure that subsequent searchers will see the result of
     * that update, but you can timeout on that without losing the update.
     *
     * @param name used to name the background thread.
     * @param config that holds the {@link Directory} and {@link Analyzer} used
     *            for indexing and searching.
     * @param maxQueueSize
     * @return a {@link Manager} that has an index configured for queued
     *         operations.
     */
    @Nonnull
    public static Index.Manager createQueuedIndexManager(final @Nonnull String name, final @Nonnull Configuration config, final int maxQueueSize)
    {
        // writePolicy is that the IndexWriter is committed after every write
        final DefaultIndexEngine engine = new DefaultIndexEngine(config, FlushPolicy.FLUSH);
        return new DefaultManager(config, engine, new QueueingIndex(name, new DefaultIndex(engine), maxQueueSize));
    }

    /**
     * Creates an index where the index operation work is done in the calling
     * thread. Any {@link Result} may be waited on but it will always be a
     * non-blocking operation as it will be complete already. There is no way to
     * timeout these operations.
     * <p>
     * The Index write policy is that flushes will only occur if a Searcher is
     * requested, when the IndexWriter decides to according to its internal
     * buffering policy, or when the index is closed.
     *
     * @param config that holds the {@link Directory} and {@link Analyzer} used
     *            for indexing and searching.
     * @return a {@link Manager} that has an index configured for direct
     *         operations.
     */
    public static Index.Manager createSimpleIndexManager(final @Nonnull Configuration config)
    {
        final DefaultIndexEngine engine = new DefaultIndexEngine(config, FlushPolicy.NONE);
        return new DefaultManager(config, engine, new DefaultIndex(engine));
    }

    /** do not ctor */
    // /CLOVER:OFF
    private Indexes()
    {
        throw new AssertionError("cannot instantiate");
    }
    // /CLOVER:ON
}
