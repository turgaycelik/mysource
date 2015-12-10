package com.atlassian.jira.index;

import javax.annotation.Nonnull;
import com.atlassian.jira.util.RuntimeInterruptedException;
import com.atlassian.jira.util.concurrent.ThreadFactories;
import com.atlassian.util.concurrent.SettableFuture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import com.google.common.annotations.VisibleForTesting;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Queueing {@link Index} implementation that takes all operations on the queue
 * and batches them to the underlying {@link Index delegate} on the task thread.
 * <p>
 * The created thread is interruptible and dies when interrupted, but will be
 * recreated if any new index jobs arrive. The initial task thread is not created
 * until the first indexing job arrives.
 */
class QueueingIndex implements CloseableIndex
{
    /** Size hint for array list used to drain the queue. */
    private static final int DEFAULT_QUEUE_BUFFER = 128;

    private final Task task = new Task();
    private final AtomicSupplier<Thread> indexerThread = new AtomicSupplier<Thread>()
    {
        @Override
        protected Thread create()
        {
            final Thread thread = threadFactory.newThread(task);
            thread.start();
            return thread;
        }
    };

    private final ThreadFactory threadFactory;
    private final CloseableIndex delegate;

    @VisibleForTesting
    final BlockingQueue<FutureOperation> queue;

    QueueingIndex(@Nonnull final String name, @Nonnull final CloseableIndex delegate, final int maxQueueSize)
    {
        this(ThreadFactories.namedThreadFactory(notNull("name", name) + "-indexQueue"), delegate, maxQueueSize);
    }

    @VisibleForTesting
    QueueingIndex(@Nonnull final ThreadFactory threadFactory, @Nonnull final CloseableIndex delegate,
            final int maxQueueSize)
    {
        this.threadFactory = notNull("threadFactory", threadFactory);
        this.delegate = notNull("delegate", delegate);
        this.queue = new LinkedBlockingQueue<FutureOperation>(maxQueueSize);
    }

    public Result perform(@Nonnull final Operation operation)
    {
        final FutureOperation future = new FutureOperation(operation);
        try
        {
            queue.put(future);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeInterruptedException(e);
        }
        ensureRunning();
        return new FutureResult(future);
    }

    public void close()
    {
        final Thread thread = indexerThread.get();
        try
        {
            while (thread.isAlive())
            {
                task.interrupt(thread);
                thread.join(100L);
            }
        }
        catch (final InterruptedException e)
        {
            ///CLOVER:OFF
            throw new RuntimeInterruptedException(e);
            ///CLOVER:ON
        }
        finally
        {
            indexerThread.compareAndSetNull(thread);
            delegate.close();
        }
    }

    /**
     * Check that there is an indexer thread and it is running.
     * If there is still work in the queue, then we need a thread to process it.
     */
    private void ensureRunning()
    {
        while (true)
        {
            final Thread thread = indexerThread.get();
            if (thread.isAlive())
            {
                return;
            }
            indexerThread.compareAndSetNull(thread);
        }
    }

    class Task implements Runnable
    {
        public void run()
        {
            while (!Thread.interrupted())
            {
                try
                {
                    index();
                }
                catch (final InterruptedException e)
                {
                    return;
                }
            }
        }

        // JRA-41409: If we interrupt the thread while it is inside Lucene's code, then we could
        // interrupt an NIO operation on a file channel used by NIOFSDirectory or MMapDirectory.
        // This would abort the NIO operation in an indeterminate state and slam the file channel
        // shut, which pretty much guarantees a corrupted index.  This intrinsic lock should not
        // normally be contested, but it ensures that the interruption will be seen by Task.run()
        // or the queue.take() call in Task.index(), never inside delegate.perform(Operation).
        // Synchronize interrupt() and perform() to prevent operations being interrupted midway.
        synchronized void interrupt(Thread thread)
        {
            thread.interrupt();
        }

        synchronized void perform(CompositeOperation operation)
        {
            final boolean interrupted = Thread.interrupted();
            try
            {
                operation.set(delegate.perform(operation));
            }
            finally
            {
                if (interrupted)
                {
                    selfInterrupt();
                }
            }
        }

        void index() throws InterruptedException
        {
            final List<FutureOperation> list = new ArrayList<FutureOperation>(DEFAULT_QUEUE_BUFFER);
            list.add(queue.take());  // interruptible
            queue.drainTo(list);
            perform(new CompositeOperation(list));
        }
    }

    private static void selfInterrupt()
    {
        Thread.currentThread().interrupt();
    }

    /**
     * Class that is responsible for returning Result to the calling thread.
     * <p>
     * Calls to {@link FutureOperation#get()} will block until the reference is set
     */
    static class FutureOperation extends SettableFuture<Index.Result>
    {
        private final Operation operation;

        FutureOperation(final Operation operation)
        {
            this.operation = notNull("operation", operation);
        }

        UpdateMode mode()
        {
            return operation.mode();
        }
    }

    static class CompositeOperation extends Index.Operation
    {
        private final List<FutureOperation> operations;

        CompositeOperation(final List<FutureOperation> operations)
        {
            this.operations = Collections.unmodifiableList(operations);
        }

        public void set(final Result result)
        {
            for (final FutureOperation future : operations)
            {
                future.set(result);
            }
        }

        @Override
        void perform(@Nonnull final Writer writer) throws IOException
        {
            final Iterator<FutureOperation> iter = operations.iterator();
            try
            {
                while (iter.hasNext())
                {
                    iter.next().operation.perform(writer);
                }
            }
            catch (RuntimeException re)
            {
                cancelTheRest(iter, re);
                throw re;
            }
            catch (IOException ioe)
            {
                cancelTheRest(iter, ioe);
                throw ioe;
            }
        }

        private static void cancelTheRest(final Iterator<FutureOperation> iter, final Throwable cause)
        {
            final CancellationException ce = new CancellationException(
                    "Cancelled composite indexing operation due to unhandled exception " + cause);
            ce.initCause(cause);
            while (iter.hasNext())
            {
                iter.next().setException(ce);
            }
        }

        @Override
        UpdateMode mode()
        {
            //@TODO check size to simply return BATCH
            for (final FutureOperation future : operations)
            {
                if (future.mode() == UpdateMode.BATCH)
                {
                    return UpdateMode.BATCH;
                }
            }
            return UpdateMode.INTERACTIVE;
        }
    }
}
