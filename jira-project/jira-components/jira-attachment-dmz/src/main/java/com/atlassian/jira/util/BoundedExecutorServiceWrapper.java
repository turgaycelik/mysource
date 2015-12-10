package com.atlassian.jira.util;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.fugue.Option;
import com.atlassian.util.concurrent.ExecutorSubmitter;
import com.atlassian.util.concurrent.Executors;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.ThreadFactories;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps an ExecutorService to ensure the number of queued tasks is bounded to the specified concurrency. Submission of
 * tasks will block until a thread is available.
 *
 * Construct using a BoundedExecutorServiceWrapper.Builder.
 *
 * @since v6.3
 */
public final class BoundedExecutorServiceWrapper
{
    private static final Logger log = LoggerFactory.getLogger(BoundedExecutorServiceWrapper.class);

    private final ListeningExecutorService executor;
    private final Duration shutdownTimeout;
    private final ExecutorSubmitter executorSubmitter;

    private BoundedExecutorServiceWrapper(final int concurrency, @Nonnull final Duration shutdownTimeout,
            @Nonnull final String threadPoolName)
    {
        this(defaultExecutor(threadPoolName), concurrency, shutdownTimeout);
    }

    private BoundedExecutorServiceWrapper(@Nonnull final ListeningExecutorService executor, final int queueLength, @Nonnull final Duration shutdownTimeout)
    {
        Preconditions.checkArgument(queueLength > 0);
        this.executor = Preconditions.checkNotNull(executor);
        this.shutdownTimeout = Preconditions.checkNotNull(shutdownTimeout);
        this.executorSubmitter = Executors.submitter(Executors.limited(executor, queueLength));
    }

    public <O> Promise<O> submit(final Callable<O> job)
    {
        return executorSubmitter.submit(job);
    }

    /**
     * Shutdown thread pool and block until it is drained using the configured timeout.
     *
     * @return true if the executor service has been shutdown.
     */
    public boolean awaitTermination()
    {
        return awaitTermination(shutdownTimeout.getMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Shutdown thread pool and block until it is drained.
     * @param timeout the shutdown timeout
     * @param unit the shutdown timeout unit
     * @return true if the executor service has been shutdown.
     */
    public boolean awaitTermination(long timeout, TimeUnit unit)
    {
        try
        {
            executor.shutdown();
            if (!executor.awaitTermination(timeout, unit))
            {
                log.info("Concurrent processor executor service did not shutdown in {} {}. Killing.", timeout, unit);
                executor.shutdownNow();
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        return executor.isTerminated();
    }

    private static ListeningExecutorService defaultExecutor(String threadPoolName)
    {
        return MoreExecutors.listeningDecorator(
                java.util.concurrent.Executors.newCachedThreadPool(
                    ThreadFactories.namedThreadFactory(threadPoolName)
                ));
    }

    public static class Builder
    {
        private int concurrency = 5;
        private Duration shutdownTimeout = Duration.millis(60000);
        private String threadPoolName = "concurrent-processor-pool";
        private Option<Supplier<ListeningExecutorService>> executorServiceSupplier;

        /**
         * Set the max number of concurrent operations supported by the ExecutorService
         * @param concurrency The max number of concurrent operations.
         * @return a configured builder.
         */
        public Builder withConcurrency(final int concurrency)
        {
            Preconditions.checkArgument(concurrency > 0, "Concurrency must be greater than 0");
            this.concurrency = concurrency;
            this.executorServiceSupplier = Option.none();
            return this;
        }

        /**
         * Set the timeout duration for shutting down the executor service.
         * @param shutdownTimeout The timeout duration for shutting down the executor service.
         * @return a configured builder
         */
        public Builder withShutdownTimeout(final @Nonnull Duration shutdownTimeout)
        {
            this.shutdownTimeout = Preconditions.checkNotNull(shutdownTimeout);
            return this;
        }

        /**
         * Set the thread pool name used by the executor service that is managed by the wrapper. This setting cannot be
         * used with {@link #withExecutorService(Supplier)}.
         * @param threadPoolName The thread pool name
         * @return a configured builder
         */
        public Builder withThreadPoolName(final @Nonnull String threadPoolName)
        {
            Preconditions.checkArgument(threadPoolName != null && threadPoolName.length() > 0, "Thread pool name must not be empty");
            this.threadPoolName = threadPoolName;
            this.executorServiceSupplier = Option.none();
            return this;
        }

        /**
         * Provide a custom executor service supplier. This setting cannot be used with the thread pool name setting.
         * NB A cached thread pool is recommended as the executor service.
         * @param executorServiceSupplier Supplier of a suitable executor service to use
         * @return a builder configured with the supplier.
         */
        public Builder withExecutorService(final @Nonnull Supplier<ListeningExecutorService> executorServiceSupplier)
        {
            Preconditions.checkNotNull(executorServiceSupplier);
            this.executorServiceSupplier = Option.some(executorServiceSupplier);
            return this;
        }

        public BoundedExecutorServiceWrapper build()
        {
            if (executorServiceSupplier.isDefined())
            {
                return new BoundedExecutorServiceWrapper(executorServiceSupplier.get().get(), concurrency, shutdownTimeout);
            }
            else
            {
                return new BoundedExecutorServiceWrapper(concurrency, shutdownTimeout, threadPoolName);
            }
        }
    }
}
