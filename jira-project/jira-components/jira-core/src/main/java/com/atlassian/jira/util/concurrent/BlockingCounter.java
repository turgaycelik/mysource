package com.atlassian.jira.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * A simple counter that can will cause threads calling the {@link #await()} method to block while its value
 * is &gt; 0. The counter may only be incremented and decremented and must have a value &gt;= 0.
 *
 * @since v4.1
 */
public final class BlockingCounter
{
    private final Sync sync;

    public BlockingCounter()
    {
        sync = new Sync(0);
    }

    public BlockingCounter(int count)
    {
        if (count < 0)
        {
            throw new IllegalArgumentException("count must be >= 0");
        }
        sync = new Sync(count);
    }

    public void up()
    {
        sync.acquireShared(Sync.COUNTER);
    }

    public void down()
    {
        sync.releaseShared(Sync.COUNTER);
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException
    {
        return sync.tryAcquireSharedNanos(Sync.WAITER, unit.toNanos(timeout));
    }

    /**
     * Block while counter is &gt; 0, throwing InterruptedException if interrupted.<p>
     * Note the behaviour has changed since v6.1 - before that InterruptedException was never thrown due to a bug.
     *
     * @see AbstractQueuedSynchronizer#acquireSharedInterruptibly(int)
     * @throws InterruptedException
     */
    public void await() throws InterruptedException
    {
        sync.acquireSharedInterruptibly(Sync.WAITER);
    }

    /**
     * Block while counter is &gt; 0, ignoring interrupts.
     * @see AbstractQueuedSynchronizer#doAcquireShared(int)
     * @since v6.1
     */
    public void awaitUninterruptibly()
    {
        sync.acquireShared(Sync.WAITER);
    }

    /**
     * Returns true if calling {@link #await()} at the same very moment would block.
     *
     * @return true when count is &gt; 0 so {@link #await()} would block
     * @since v6.1
     */
    public boolean wouldBlock()
    {
        return getCount() > 0;
    }

    public int getCount()
    {
        return sync.getCount();
    }

    @Override
    public String toString()
    {
        return String.format("Blocking Counter [%d]", getCount());
    }

    private final static class Sync extends AbstractQueuedSynchronizer
    {
        /**
         * A waiter thread is one that is blocked on wait.
         */
        private static final int WAITER = 0;

        /**
         * A counter thread is one that is either incrementing and decrementing the counter.
         */
        private static final int COUNTER = 1;

        private Sync(int count)
        {
            setState(count);
        }

        @Override
        protected int tryAcquireShared(final int arg)
        {
            if (arg == WAITER)
            {
                //The waiters can only return when the count is zero.
                return (getState() == 0) ? 1 : -1;
            }
            else if (arg == COUNTER)
            {
                //The counter thread can just increment the counter.
                while (true)
                {
                    int count = getState();
                    if (compareAndSetState(count, count + 1))
                    {
                        return 1;
                    }
                }
            }
            else
            {
                throw new AssertionError("This code should never be reached.");
            }
        }

        @Override
        protected boolean tryReleaseShared(final int arg)
        {
            if (arg == COUNTER)
            {
                while (true)
                {
                    final int count = getState();
                    if (count == 0)
                    {
                        throw new IllegalStateException("Trying to set counter below zero.");
                    }
                    final int nextCount = count - 1;
                    if (compareAndSetState(count, nextCount))
                    {
                        //if the count is zero, then lets wake up some any threads that are sleeping.
                        return nextCount == 0;
                    }
                }
            }
            else
            {
                throw new AssertionError("This code should never be called.");
            }
        }

        public int getCount()
        {
            return getState();
        }
    }
}
