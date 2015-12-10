package com.atlassian.jira.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import com.atlassian.jira.cluster.ClusterSafe;

import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A simple executor that executes each task on a new thread. It will only allow a specified number
 * of threaded tasks to be executed at one time.
 *
 * @since v3.13
 */
@ClusterSafe ("Local object")
class ForkedThreadExecutor extends AbstractExecutorService
{
    private static final Logger log = Logger.getLogger(ForkedThreadExecutor.class);

    private final int maxThreads;
    private final Queue<Runnable> waitingTasks = new LinkedList<Runnable>();
    private final ThreadFactory threadFactory;
    private final Set<ForkedRunnableDecorator> executingTasks = new HashSet<ForkedRunnableDecorator>();
    private boolean shutdown;

    ForkedThreadExecutor(final int maxThreads, @Nonnull final ThreadFactory threadFactory)
    {
        notNull("threadFactory", threadFactory);

        if (maxThreads <= 0)
        {
            throw new IllegalArgumentException("maxThreads must be > 0");
        }

        this.threadFactory = threadFactory;
        this.maxThreads = maxThreads;
        setShutdown(false);
    }

    ForkedThreadExecutor(final int maxThreads)
    {
        this(maxThreads, new DefaultThreadFactory());
    }

    public ThreadFactory getThreadFactory()
    {
        return threadFactory;
    }

    public int getMaxThreads()
    {
        return maxThreads;
    }

    public void shutdown()
    {
        setShutdown(true);
    }

    public synchronized boolean isShutdown()
    {
        return shutdown;
    }

    public synchronized List<Runnable> shutdownNow()
    {
        shutdown();

        if (log.isDebugEnabled())
        {
            log.debug("Called shutdownNow. Interrupting " + executingTasks.size() + " threads and returning " + waitingTasks.size() + " queued tasks.");
        }

        for (final Object element : executingTasks)
        {
            final ForkedRunnableDecorator o = (ForkedRunnableDecorator) element;
            final Thread th = o.getThread();
            th.interrupt();
        }

        final List<Runnable> returningTasks = new ArrayList<Runnable>(waitingTasks);
        waitingTasks.clear();
        notifyAll();

        return returningTasks;

    }

    public synchronized boolean isTerminated()
    {
        return isShutdown() && executingTasks.isEmpty() && waitingTasks.isEmpty();
    }

    public synchronized boolean awaitTermination(long length, @Nonnull final TimeUnit timeUnit) throws InterruptedException
    {
        notNull("timeUnit", timeUnit);

        if (log.isDebugEnabled())
        {
            log.debug("Called awaitTermination. Awaiting " + length + " " + timeUnit + ".");
        }

        if (length > 0)
        {
            length = timeUnit.toMillis(length);
            final long startTime = System.currentTimeMillis();

            while (!isTerminated())
            {
                final long diff = System.currentTimeMillis() - startTime;
                if (diff < length)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Waiting " + (length - diff) + " ms for executor to terminate.");
                    }

                    wait(length - diff);
                }
                else
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Timed out for executor to terminate.");
                    }

                    break;
                }
            }
        }

        return isTerminated();
    }

    public synchronized void execute(@Nonnull Runnable runnable)
    {
        notNull("runnable", runnable);

        if (isShutdown())
        {
            throw new RejectedExecutionException("Executor has been shutdown.");
        }

        if (executingTasks.size() >= maxThreads)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Adding new task to waiting queue. ExecutingTasks=" + executingTasks.size() + ", WaitingTasks=" + waitingTasks.size());
            }

            //if there are currently maxThreads running, then enqueue the task.
            waitingTasks.offer(runnable);
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Starting new task. ExecutingTasks=" + executingTasks.size() + ", WaitingTasks=" + waitingTasks.size());
            }

            //we can start a thread. Starte a waiting task over one that was
            //just submitted.
            if (!waitingTasks.isEmpty())
            {
                final Runnable nextRunnable = waitingTasks.remove();
                waitingTasks.offer(runnable);
                runnable = nextRunnable;
            }
            startThread(runnable);
        }
    }

    /**
     * Called by the executing task threads when they are finished. This will start another
     * thread if necessary.
     *
     * @param runnableDecorator the callable that completed.
     */

    private synchronized void finishedTask(final ForkedRunnableDecorator runnableDecorator)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Finished executing task. ExecutingTasks=" + executingTasks.size() + ", WaitingTasks=" + waitingTasks.size());
        }

        executingTasks.remove(runnableDecorator);
        if (!waitingTasks.isEmpty())
        {
            log.debug("Starting next task in queue.");

            final Runnable nextRunnable = waitingTasks.remove();
            startThread(nextRunnable);
        }

        //we may no longer have anything to do. Notify those that may be waiting
        //for the executor to complete.

        notifyAll();
    }

    private synchronized void startThread(final Runnable runnable)
    {
        final ForkedRunnableDecorator runnableDecorator = new ForkedRunnableDecorator(runnable);
        final Thread thread = threadFactory.newThread(runnableDecorator);
        runnableDecorator.setThread(thread);
        executingTasks.add(runnableDecorator);
        thread.start();
    }

    private synchronized void setShutdown(final boolean shutdown)
    {
        this.shutdown = shutdown;
        notifyAll();
    }

    /**
     * Simple decorator for runnables executing in this executor. It just informs the executor when
     * a thread has finished executing so that it may start any pending tasks.
     */
    private class ForkedRunnableDecorator implements Runnable
    {
        private final Runnable runnable;

        /** This variable should only be accessed when the monitor of the ForkedThreadExecutor is held. */

        private Thread thread;

        public ForkedRunnableDecorator(final Runnable runnable)
        {
            this.runnable = runnable;
            thread = null;
        }

        public void run()
        {
            try
            {
                runnable.run();
            }
            finally
            {
                finishedTask(this);
            }
        }

        public Thread getThread()
        {
            return thread;
        }

        public void setThread(final Thread thread)
        {
            this.thread = thread;
        }
    }

    /**
     * Simple thread thread factory used by the executor when none is supplied.
     */
    static class DefaultThreadFactory implements ThreadFactory
    {
        private final AtomicInteger idCounter = new AtomicInteger(0);

        public Thread newThread(final Runnable runnable)
        {
            final String title = "ForkedThreadExecutor-runner-" + idCounter.getAndIncrement();
            return new Thread(runnable, title);
        }
    }
}
