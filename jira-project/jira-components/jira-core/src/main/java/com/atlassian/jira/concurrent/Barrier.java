package com.atlassian.jira.concurrent;

import com.atlassian.annotations.Internal;

/**
 * The barrier allows us to hold up an operation for a short while. This class is <b>not API</b> and should only be used
 * for testing.
 * <p/>
 * Barriers <b>must</b> be lowered in a {@code finally} block so as to avoid blocking operations forever.
 *
 * @since v5.2
 */
@Internal
public interface Barrier
{
    /**
     * Returns this barrier's name.
     *
     * @return a String containing the barrier's name
     */
    String name();

    /**
     * Blocks the calling thread until the barrier is lowered or the calling thread is interrupted. Callers may use
     * {@link Thread#isInterrupted()} to check if the thread has been interrupted.
     */
    void await();

    /**
     * Raises the barrier. Threads that call {@link #await()} when the barrier is raised will block indefinitely until
     * the barrier is lowered again.
     */
    void raise();

    /**
     * Lowers the barrier. Any threads that are blocked on {@link #await()} will no longer be blocked.
     */
    void lower();
}
