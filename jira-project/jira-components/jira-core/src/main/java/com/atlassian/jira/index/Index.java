package com.atlassian.jira.index;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.util.IndexWriterConfiguration;
import com.atlassian.jira.config.util.IndexWriterConfiguration.WriterSettings;
import com.atlassian.jira.util.Closeable;
import com.atlassian.jira.util.RuntimeInterruptedException;

import org.apache.lucene.search.IndexSearcher;

/**
 * An {@link Index} is where data is stored for fast retrieval. The
 * {@link Index} itself has {@link Operation operations} performed on it to
 * update it. The index is held by a {@link Manager} where you can access a
 * {@link IndexSearcher searcher} that reflects the latest update that has
 * completed.
 * <p>
 * Note: in order to guarantee that an {@link IndexSearcher} returned from
 * {@link Index.Manager#openSearcher()} contains a particular {@link Operation}
 * that is {@link #perform(Operation) performed}, the {@link Result} must be
 * {@link Index.Result#await() waited on}.
 *
 * @since v4.0
 */
public interface Index
{
    /**
     * Perform an {@link Operation} on the index.
     *
     * @param operation the work to do.
     * @return a Result object
     */
    @Nonnull
    Result perform(@Nonnull Operation operation);

    /**
     * The payload is unimportant. Call {@link #await()} simply to block on the
     * result being computed.
     */
    interface Result
    {
        /**
         * Await the result of the operation.
         *
         * @throws RuntimeInterruptedException if interrupted
         * @throws RuntimeException if the underlying operation caught an
         *             exception
         * @throws Error if the underlying operation caught an error
         */
        void await();

        /**
         * Await the result of the operation for the specified time, throwing a
         * {@link TimeoutException} if the timeout is reached.
         *
         * @param timeout the amount to wait
         * @param unit the unit to count the timeout in
         * @throws RuntimeInterruptedException if interrupted
         * @throws RuntimeException if the underlying operation caught an
         *             exception
         * @throws Error if the underlying operation caught an error
         * @return false if the timeout is exceeded before the underlying
         *         operation has completed, true if it has completed in time.
         */
        boolean await(final long timeout, final TimeUnit unit);

        /**
         * Has the operation completed yet. If true then {@link #await()} and
         * {@link #await(long, TimeUnit)} will not block.
         *
         * @return whether the operation is complete or not.
         */
        boolean isDone();
    }

    /**
     * Management of an {@link Index}
     */
    interface Manager extends Closeable
    {
        /**
         * Get the current IndexConnection this manager holds. May throw
         * exceptions if the index has not been created.
         *
         * @return the Index this manager refers to
         * @throws IllegalStateException if the index directory is not created
         *             etc.
         */
        @Nonnull
        Index getIndex();

        /**
         * Get the current {@link IndexSearcher} from the {@link Index}.
         * <p>
         * You must call the {@link IndexSearcher#close() close} method in a
         * finally block once the searcher is no longer needed.
         *
         * @return the current {@link IndexSearcher}
         */
        @Nonnull
        IndexSearcher openSearcher();

        /**
         * Returns true if the index has been created. This means that the index
         * directory itself exists AND has been initialised with the default
         * required index files.
         *
         * @return true if the index exists, false otherwise.
         */
        boolean isIndexCreated();

        /**
         * Clean out the underlying directory the index is contained in.
         * <p>
         * Blow away any indexes that currently live there.
         */
        void deleteIndexDirectory();
    }

    public enum UpdateMode
    {
        INTERACTIVE
        {
            @Override
            WriterSettings getWriterSettings(final IndexWriterConfiguration configuration)
            {
                return configuration.getInteractiveSettings();
            }
        },
        BATCH
        {
            @Override
            WriterSettings getWriterSettings(final IndexWriterConfiguration configuration)
            {
                return configuration.getBatchSettings();
            }
        };

        abstract IndexWriterConfiguration.WriterSettings getWriterSettings(IndexWriterConfiguration writerConfiguration);
    }

    /**
     * An operation that is performed on an Index. See {@link Operations} for
     * factory methods.
     * <p>
     * Note: this is not an interface to prevent clients implementing it. All
     * clients need to now is that they have an {@link Operation} that will do a
     * create/delete/update/whatever, not how it is implemented.
     */
    public abstract class Operation
    {
        Operation()
        {}

        abstract void perform(@Nonnull Writer writer) throws IOException;

        abstract UpdateMode mode();
    }
}
