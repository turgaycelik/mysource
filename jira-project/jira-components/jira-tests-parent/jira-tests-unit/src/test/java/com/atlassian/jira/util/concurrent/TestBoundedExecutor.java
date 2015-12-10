package com.atlassian.jira.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class TestBoundedExecutor
{
    private ExecutorService executorService;

    @Before
    public void setUp()
    {
        executorService = Executors.newFixedThreadPool(1);
    }

    @After
    public void tearDown()
    {
        if (executorService != null)
        {
            executorService.shutdownNow();
        }
    }

    @Test
    public void testShutdownAndWaitDoesNotInterruptRunnable()
    {
        final Boolean[] done = { false };

        BoundedExecutor exe = new BoundedExecutor(executorService, 1);
        exe.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //If shutdownAndWait() interrupts then this will throw InterruptedException and done will never be set
                    Thread.sleep(500L);
                    done[0] = true;
                }
                catch (InterruptedException e)
                {
                    //Should not happen, bounded executor should not be interruptible
                }
            }
        });

        assertArrayEquals(new Boolean[] { false }, done);
        exe.shutdownAndWait();
        assertArrayEquals(new Boolean[] { true }, done);
    }

    @Test
    public void testInterruptedThreadDoesNotInterruptShutdownAndWait()
    {
        final Boolean[] done = { false };

        BoundedExecutor exe = new BoundedExecutor(executorService, 1);
        exe.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //If shutdownAndWait() interrupts then this will throw InterruptedException and done will never be set
                    Thread.sleep(500L);
                    done[0] = true;
                }
                catch (InterruptedException e)
                {
                    //Should not happen, bounded executor should not be interruptible
                }
            }
        });

        assertArrayEquals(new Boolean[] { false }, done);

        try
        {
            Thread.currentThread().interrupt();
            exe.shutdownAndWait();

            assertArrayEquals(new Boolean[] { true }, done);
        }
        finally
        {
            Thread.interrupted();
        }
    }
}
