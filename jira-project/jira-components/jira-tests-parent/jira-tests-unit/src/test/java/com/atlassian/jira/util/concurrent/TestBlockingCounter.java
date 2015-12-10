package com.atlassian.jira.util.concurrent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.util.concurrent.BlockingCounter}.
 *
 * @since v4.1
 */
public class TestBlockingCounter
{
    /*
     * Test to make sure that thread can go straight through.
     */

    @Test
    public void testThreadThrough() throws Exception
    {
        BlockingCounter counter = new BlockingCounter();
        assertEquals(0, counter.getCount());
        counter.await();
        assertTrue(counter.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testThreadBlocking() throws Exception
    {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(ThreadFactories.namedThreadFactory("testThreadBlocking"));
        try
        {
            final BlockingCounter counter = new BlockingCounter();
            counter.up();

            assertEquals(1, counter.getCount());

            assertFalse(counter.await(100, TimeUnit.MILLISECONDS));
            service.schedule(new Runnable()
            {
                public void run()
                {
                    counter.down();
                }
            }, 200, TimeUnit.MILLISECONDS);

            counter.await();
            assertEquals(0, counter.getCount());
            assertTrue(counter.await(0, TimeUnit.MILLISECONDS));

            counter.up();
            assertEquals(1, counter.getCount());
            counter.up();
            assertEquals(2, counter.getCount());
            assertFalse(counter.await(100, TimeUnit.MILLISECONDS));
            counter.down();
            assertEquals(1, counter.getCount());

            assertFalse(counter.await(100, TimeUnit.MILLISECONDS));

            service.schedule(new Runnable()
            {
                public void run()
                {
                    counter.down();
                }
            }, 200, TimeUnit.MILLISECONDS);

            counter.await();
            assertEquals(0, counter.getCount());
            assertTrue(counter.await(0, TimeUnit.MILLISECONDS));

            service.shutdown();
            assertTrue(service.awaitTermination(10, TimeUnit.SECONDS));
        }
        finally
        {
            service.shutdownNow();
        }
    }

    @Test
    public void testThreadBlockingMultipleReaders() throws Exception
    {
        final BlockingCounter counter = new BlockingCounter(1);
        final CyclicBarrier barrier = new CyclicBarrier(3);

        class Reader implements Runnable
        {
            private final Latch endLatch = new Latch(1);

            public void run()
            {
                try
                {
                    barrier.await();
                    assertTrue(0 != counter.getCount());
                    counter.await();
                    endLatch.countDown();
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                catch (BrokenBarrierException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        ExecutorService service = Executors.newFixedThreadPool(2, ThreadFactories.namedThreadFactory("testThreadBlockingMultipleReaders"));
        try
        {
            Reader reader1 = new Reader();
            Reader reader2 = new Reader();
            service.execute(reader1);
            service.execute(reader2);

            assertFalse(counter.await(100, TimeUnit.MILLISECONDS));

            //Wait for the threads to start.
            barrier.await(60, TimeUnit.SECONDS);

            assertFalse(counter.await(200, TimeUnit.MILLISECONDS));

            //Latched should be locked.
            assertTrue(reader1.endLatch.getCount() == 1);
            assertTrue(reader2.endLatch.getCount() == 1);
            assertEquals(1, counter.getCount());

            counter.down();

            assertEquals(0, counter.getCount());
            //Latches should now be unlocked now we have downed the counter.
            assertTrue(reader1.endLatch.await(60, TimeUnit.SECONDS));
            assertTrue(reader1.endLatch.await(60, TimeUnit.SECONDS));

            service.shutdown();
            assertTrue(service.awaitTermination(10, TimeUnit.SECONDS));
        }
        finally
        {
            service.shutdownNow();
        }
    }

    @Test
    public void testDownTooManyTimes() throws Exception
    {
        final BlockingCounter counter = new BlockingCounter(1);
        assertEquals(1, counter.getCount());

        ExecutorService service = Executors.newFixedThreadPool(2, ThreadFactories.namedThreadFactory("testDownTooManyTimes"));
        try
        {
            Future<?> future = service.submit(new Runnable()
            {
                public void run()
                {
                    //noinspection InfiniteLoopStatement
                    while (true)
                    {
                        counter.down();
                    }
                }
            });

            try
            {
                future.get();
                fail("Expected an exception to occur.");
            }
            catch (ExecutionException e)
            {
                assertTrue(e.getCause() instanceof IllegalStateException);
            }

            assertEquals(0, counter.getCount());

            service.shutdown();
            assertTrue(service.awaitTermination(10, TimeUnit.SECONDS));
        }
        finally
        {
            service.shutdownNow();
        }
    }

    @Test
    public void testBadConstructor() throws Exception
    {
        try
        {
            new BlockingCounter(-1);
            fail("Should not have allowed negative count.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }
}
