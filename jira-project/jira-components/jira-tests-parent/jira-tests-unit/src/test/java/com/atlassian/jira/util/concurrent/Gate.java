package com.atlassian.jira.util.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Simple starting gate for co-ordinating a bunch of threads.
 */
public class Gate
{
    final CountDownLatch ready;
    final CountDownLatch go = new CountDownLatch(1);

    public Gate(final int threads)
    {
        ready = new CountDownLatch(threads);
    }

    /**
     * Called from the racing threads when ready. They will then block until all threads are at this point;
     */
    public void ready()
    {
        ready.countDown();
        await(go);
    }

    /**
     * Called from the starter thread. Blocks until everybody is ready, and then signals go.
     */
    public void go()
    {
        await(ready);
        go.countDown();
    }

    static void await(final CountDownLatch latch)
    {
        try
        {
            if (!latch.await(5, TimeUnit.SECONDS))
            {
                throw new TimedOutException();
            }
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    static final class TimedOutException extends IllegalStateException
    {}
}
