package com.atlassian.jira.util.concurrent;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.util.RuntimeInterruptedException;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nonnull;

/**
 * BoundedExecutor is an ExecutorService wrapper that bounds the number of runnables
 * allowed in the execution queue. {@link #execute(Runnable)} blocks if the number
 * of runnables already in the queue equals the maximum number of permits available.
 */
// Note: The lock used here is a semaphore, which may be safely unlocked by a different
// thread than obtained the lock.  Tasks that are accepted will release the lock when the
// task completes (whether successfully or not); those that are rejected by the executor
// release the lock immediately before throwing the exception.  This is a safe pattern,
// but confuses code analysis tools.
@SuppressWarnings("LockAcquiredButNotSafelyReleased")
public class BoundedExecutor implements Executor
{
    private final ExecutorService executor;
    @ClusterSafe("Only used for setting a bound on an executor queue")
    private final Lock lock;

    /**
     * Constructor.
     *
     * @param executor the executor service whose queue is to be bounded (required)
     * @param permits the number of runnables allowed in the queue at once
     */
    public BoundedExecutor(final ExecutorService executor, final int permits)
    {
        this.executor = executor;
        lock = new SemaphoreLock(permits);
    }

    public void execute(@Nonnull final Runnable command)
    {
        lock.lock();
        try
        {
            executor.execute(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        command.run();
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            });
        }
        catch (final RejectedExecutionException rej)
        {
            lock.unlock();
            throw rej;
        }
    }

    public <T> Future<T> submit(@Nonnull final Callable<T> task)
    {
        lock.lock();
        try
        {
            return executor.submit(new Callable<T>()
            {
                public T call() throws Exception
                {
                    try
                    {
                        return task.call();
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            });
        }
        catch (final RejectedExecutionException rej)
        {
            lock.unlock();
            throw rej;
        }
    }

    /**
     * shutdown the ExecutorService and wait for it. This method is not interruptible.
     */
    public void shutdownAndWait()
    {
        executor.shutdown();

        boolean interrupted = shutdownAndWaitAndCheckInterrupt();
        if (interrupted)
        {
            Thread.currentThread().interrupt();
        }
    }

    private boolean shutdownAndWaitAndCheckInterrupt()
    {
        boolean interrupted = Thread.interrupted();
        while (true)
        {
            try
            {
                if (executor.awaitTermination(60L, TimeUnit.SECONDS))
                {
                    break;
                }
            }
            catch (final InterruptedException e)
            {
                interrupted = true;
            }
        }
        return interrupted;
    }

    public void shutdownAndIgnoreQueue()
    {
        executor.shutdownNow();
    }

    /**
     * A Lock that can be acquired a given number of times at once.
     */
    static final class SemaphoreLock implements Lock
    {
        private final Semaphore semaphore;

        /**
         * Constructor for a lock that can be acquired the given number of times at once.
         *
         * @param permits the number of times this lock can be held at once
         * @see java.util.concurrent.Semaphore#Semaphore(int)
         */
        public SemaphoreLock(final int permits)
        {
            semaphore = new Semaphore(permits);
        }

        /**
         * Waits if necessary until this lock is available, then acquires it.
         */
        public void lock()
        {
            try
            {
                semaphore.acquire();
            }
            catch (final InterruptedException e)
            {
                throw new RuntimeInterruptedException(e);
            }
        }

        /**
         * Releases this lock for another caller to acquire.
         */
        public void unlock()
        {
            semaphore.release();
        }

        public void lockInterruptibly() throws InterruptedException
        {
            throw new UnsupportedOperationException();
        }

        public Condition newCondition()
        {
            throw new UnsupportedOperationException();
        }

        public boolean tryLock()
        {
            throw new UnsupportedOperationException();
        }

        public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException
        {
            throw new UnsupportedOperationException();
        }
    }
}