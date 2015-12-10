package com.atlassian.jira.task;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** @since v3.13 */

public class TestForkedThreadExecutor
{
    private static final String DEFAULT_RETURN = "AReturnValue";
    private static final int MAX_THREADS = 3;

    @Test
    public void testConstructor()
    {
        final ThreadFactory factory = new ForkedThreadExecutor.DefaultThreadFactory();
        ForkedThreadExecutor executor = new ForkedThreadExecutor(MAX_THREADS, factory);
        assertEquals(MAX_THREADS, executor.getMaxThreads());
        assertSame(factory, executor.getThreadFactory());

        executor = new ForkedThreadExecutor(MAX_THREADS);
        assertEquals(MAX_THREADS, executor.getMaxThreads());

        try
        {
            new ForkedThreadExecutor(0, new ForkedThreadExecutor.DefaultThreadFactory());
            fail("Accepted number of threads <=0.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new ForkedThreadExecutor(MAX_THREADS, null);
            fail("Null thread factory accepted.");
        }
        catch (final RuntimeException e)
        {
            //expected.
        }
    }

    @Test
    public void testExecute() throws Exception
    {
        final Set<Thread> threadSet = Collections.synchronizedSet(new HashSet<Thread>());

        final LatchedTask tasks[] = new LatchedTask[6];

        final TestThreadFactory threadFactory = new TestThreadFactory();
        final ForkedThreadExecutor executor = new ForkedThreadExecutor(MAX_THREADS, threadFactory);
        final FinishedThread finishedThread = new FinishedThread(executor);
        finishedThread.start();

        for (int i = 0; i < tasks.length; i++)
        {
            final LatchedTask latchedTask = tasks[i] = new LatchedTask(threadSet);
            executor.submit(latchedTask);
        }

        for (int i = 0; i < tasks.length; i++)
        {
            final LatchedTask latchedTask = tasks[i];
            if (i < MAX_THREADS)
            {
                latchedTask.waitForStart();
                assertTrue(latchedTask.isStarted());
            }
            else
            {
                assertFalse(latchedTask.isStarted());
            }
        }

        //let one of the tasks finish, so a queued one should start executing.
        tasks[0].unblock();

        //make sure the queued runnable is working correctly.
        tasks[MAX_THREADS].waitForStart();
        assertTrue(tasks[MAX_THREADS].isStarted());

        //finish the other tasks and await for new threads to be queued.
        for (int i = 0; i < tasks.length; i++)
        {
            final LatchedTask latchedTask = tasks[i];
            if (i > MAX_THREADS)
            {
                latchedTask.waitForStart();
                assertTrue(latchedTask.isStarted());
            }
            latchedTask.unblock();
        }

        //lets see if we can execute a new task.
        final LatchedTask singleTask = new LatchedTask(threadSet);
        executor.submit(singleTask);

        singleTask.waitForStart();
        assertTrue(singleTask.isStarted());
        singleTask.unblock();

        //make sure that we created the correct number of threads.
        assertEquals(tasks.length + 1, threadFactory.getCreationCount());
        assertEquals(tasks.length + 1, threadSet.size());
        threadSet.clear();

        assertFalse(executor.isShutdown());
        assertFalse(executor.isTerminated());
        assertFalse(finishedThread.isExecutorFinished());

        executor.shutdown();
        assertTrue(executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS));
        finishedThread.join();
        assertTrue(finishedThread.isExecutorFinished());
    }

    @Test
    public void testShutdown() throws Exception
    {
        final Set<Thread> threadSet = Collections.synchronizedSet(new HashSet<Thread>());

        final LatchedTask finishedTask = new LatchedTask(threadSet);
        final LatchedTask blockedTask = new LatchedTask(threadSet);
        final LatchedTask waitingTask = new LatchedTask(threadSet);

        //lets do a sanity test on an empty executor.
        ForkedThreadExecutor executor = new ForkedThreadExecutor(1);
        FinishedThread finishedThread = new FinishedThread(executor);
        finishedThread.start();

        assertFalse(executor.isShutdown());
        assertFalse(executor.isTerminated());

        long start = System.currentTimeMillis();
        assertFalse(executor.awaitTermination(200, TimeUnit.MILLISECONDS));
        assertTrue(System.currentTimeMillis() - start >= 200);


        //make sure that we didn't return from awaitTermination method unless shutdown is called.
        assertFalse(finishedThread.isExecutorFinished());

        executor.shutdown();

        assertTrue(executor.isShutdown());
        assertTrue(executor.isTerminated());
        finishedThread.join();
        assertTrue(finishedThread.isExecutorFinished());

        final List<Runnable> waitingRunnables = executor.shutdownNow();

        assertTrue(waitingRunnables.isEmpty());

        executor = new ForkedThreadExecutor(1);
        finishedThread = new FinishedThread(executor);
        finishedThread.start();

        final Future<String> finishedFuture = executor.submit(finishedTask);
        final Future<String> blockedFuture = executor.submit(blockedTask);
        executor.submit(waitingTask);

        //we have three tasks still in the executor so we should not be shutdown but not terminated.
        executor.shutdown();
        assertTrue(executor.isShutdown());
        assertFalse(executor.isTerminated());
        assertFalse(executor.awaitTermination(-1, TimeUnit.SECONDS));
        assertFalse(finishedThread.isExecutorFinished());

        try
        {
            executor.submit(new Runnable()
            {
                public void run()
                {
                //just a dummy method.
                }
            });

            fail("The executor should not accept new requests after shutdown.");
        }
        catch (final RejectedExecutionException e)
        {
            //this is expected.
        }

        //let the first task complete.
        finishedTask.waitForStart();
        assertTrue(finishedTask.isStarted());
        assertFalse(blockedTask.isStarted());
        assertFalse(waitingTask.isStarted());

        finishedTask.unblock();
        assertEquals(DEFAULT_RETURN, finishedFuture.get());

        //lets make sure the second task starts but block it.
        blockedTask.waitForStart();
        assertTrue(blockedTask.isStarted());
        assertFalse(waitingTask.isStarted());

        assertTrue(executor.isShutdown());
        assertFalse(executor.isTerminated());
        assertFalse(executor.awaitTermination(0, TimeUnit.SECONDS));
        assertFalse(finishedThread.isExecutorFinished());

        //shutdown the executor.
        final List<Runnable> waitingList = executor.shutdownNow();

        try
        {
            //shutdownNow should interrupt all executing threads.
            blockedFuture.get();
            fail("The blocked future should have been interrupted.");
        }
        catch (final ExecutionException e)
        {
            //expected.
        }

        //make sure we only created two threads.
        assertEquals(2, threadSet.size());
        threadSet.clear();

        //shutdown now should only return the waiting thread.
        assertEquals(1, waitingList.size());
        final Runnable waitingRunnable = waitingList.remove(0);
        assertFalse(waitingTask.isStarted());
        waitingTask.unblock();

        //make sure that running the returned task actually runs our callable.
        waitingRunnable.run();
        assertTrue(waitingTask.isStarted());

        //the pool should not be terminated.
        assertTrue(executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS));
        assertTrue(executor.awaitTermination(0, TimeUnit.MILLISECONDS));
        assertTrue(executor.awaitTermination(-1, TimeUnit.MILLISECONDS));
        assertTrue(executor.isTerminated());

        //make sure that the shutdown thread was actually awoken when all tasks completed execution.
        finishedThread.join();
        assertTrue(finishedThread.isExecutorFinished());
    }

    private static class TestThreadFactory implements ThreadFactory
    {
        private final AtomicInteger creationCount = new AtomicInteger(0);

        public Thread newThread(final Runnable runnable)
        {
            creationCount.incrementAndGet();
            return new Thread(runnable);
        }

        public int getCreationCount()
        {
            return creationCount.intValue();
        }
    }

    private static class LatchedTask implements Callable<String>
    {
        private final CountDownLatch startedLatched = new CountDownLatch(1);
        private final CountDownLatch blockingLatch = new CountDownLatch(1);
        private final AtomicBoolean started = new AtomicBoolean(false);
        private final String returnValue;
        private final Set<Thread> threadSet;

        public LatchedTask(final Set<Thread> threadSet)
        {
            this(threadSet, DEFAULT_RETURN);
        }

        public LatchedTask(final Set<Thread> threadSet, final String returnValue)
        {
            this.returnValue = returnValue;
            this.threadSet = threadSet;
        }

        public String call() throws InterruptedException
        {
            threadSet.add(Thread.currentThread());
            started.set(true);
            startedLatched.countDown();
            blockingLatch.await();

            return returnValue;
        }

        public boolean isStarted()
        {
            return started.get();
        }

        public void waitForStart() throws InterruptedException
        {
            startedLatched.await();
        }

        public void unblock()
        {
            blockingLatch.countDown();
        }
    }

    private static class FinishedThread extends Thread
    {
        private final AtomicBoolean executorFinished = new AtomicBoolean(false);
        private final ExecutorService service;

        public FinishedThread(final ExecutorService service)
        {
            super("FinishedThreadTester");
            this.service = service;
        }

        @Override
        public void run()
        {
            try
            {
                executorFinished.set(service.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS));
            }
            catch (final InterruptedException e)
            {
                //do nothing.
            }
        }

        public boolean isExecutorFinished()
        {
            return executorFinished.get();
        }
    }
}
