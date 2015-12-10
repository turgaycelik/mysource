package com.atlassian.jira.index;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.util.Closeable;
import com.atlassian.jira.util.RuntimeIOException;

import net.jcip.annotations.ThreadSafe;

interface DelayCloseable extends Closeable
{

    /**
     * Call this before usage and then call {@link #close()} in the finally
     * block.
     *
     * @throws AlreadyClosedException if this has already been closed.
     */
    void open();

    /**
     * Signals that this instance may really close when all open() calls have
     * been balanced with a call to close().
     *
     * @throws RuntimeIOException if an I/O error occurs.
     */
    void closeWhenDone();

    /**
     * Returns whether the {@link Closeable} has really been closed. If it is
     * true, this instance can no longer be used.
     *
     * @return whether the underlying {@link Closeable} has really been closed.
     */
    boolean isClosed();

    /**
     * Used to indicate a race-condition where a {@link DelayCloseable} is
     * opened after {@link DelayCloseable#closeWhenDone()} has been called.
     */
    static class AlreadyClosedException extends RuntimeIOException
    {
        private static final long serialVersionUID = 6294831692421064645L;

        AlreadyClosedException()
        {
            super(new IOException());
        }
    }

    /**
     * Helper class to do the actual tracking.
     */
    @ThreadSafe
    static final class Helper implements DelayCloseable
    {
        /**
         * delegate
         */
        private final Closeable closable;

        private final UsageTracker usageTracker = new UsageTracker();

        Helper(final Closeable closable)
        {
            this.closable = closable;
        }

        /**
         * This should be called whenever this instances is passed as a new
         * IndexSearcher. Only when each call to open() is balanced with a call
         * to close(), and closeWhenDone has been called, will super.close() be
         * called.
         */
        public void open()
        {
            if (!usageTracker.incrementUsage())
            {
                throw new AlreadyClosedException();
            }
        }

        public void closeWhenDone()
        {
            if (!usageTracker.close())
            {
                throw new AlreadyClosedException();
            }
            checkClosed();
        }

        public void close()
        {
            usageTracker.decrement();
            checkClosed();
        }

        /**
         * Returns whether the underlying IndexSearcher has really been closed.
         * If it is true, this instance can no longer be used.
         *
         * @return whether the underlying IndexSearcher has really been closed.
         */
        public boolean isClosed()
        {
            return usageTracker.isClosed();
        }

        private void checkClosed()
        {
            if (isClosed())
            {
                closable.close();
            }
        }

        /**
         * Manage the state atomically. Cannot increment if closed.
         */
        private static class UsageTracker
        {
            @ClusterSafe("Guards a local transient counter")
            private final Lock lock = new ReentrantLock();
            /**
             * The number of open() calls minus the number of close() calls. If
             * this drops to zero and closeWhenDone() is true,
             * {@link Closeable#close()} is called.
             */
            private int count;

            /**
             * Indicates if closeWhenDone() was called. If true and usageCount
             * is zero, super.close() is called.
             */
            private boolean closed;

            boolean incrementUsage()
            {
                lock.lock();
                try
                {
                    if (closed)
                    {
                        return false;
                    }
                    count++;
                    return true;
                }
                finally
                {
                    lock.unlock();
                }
            }

            void decrement()
            {
                lock.lock();
                try
                {
                    // don't decrement past zero
                    count = (count == 0) ? 0 : count - 1;
                }
                finally
                {
                    lock.unlock();
                }
            }

            boolean close()
            {
                lock.lock();
                try
                {
                    // check not true and then assign true
                    return !closed && (closed = true);
                }
                finally
                {
                    lock.unlock();
                }
            }

            boolean isClosed()
            {
                lock.lock();
                try
                {
                    return closed && (count == 0);
                }
                finally
                {
                    lock.unlock();
                }
            }
        }
    }
}
