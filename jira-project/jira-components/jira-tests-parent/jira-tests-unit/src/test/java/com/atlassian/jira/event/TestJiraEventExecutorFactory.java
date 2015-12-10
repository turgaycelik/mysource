package com.atlassian.jira.event;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TestJiraEventExecutorFactory
{
    @Test
    public void testAsync() throws InterruptedException
    {
        final JiraEventExecutorFactory jiraEventExecutorFactory = new JiraEventExecutorFactory();
        final Executor executor = jiraEventExecutorFactory.getExecutor();

        CountDownLatch startLatch = new CountDownLatch(2);
        TestRunnable runnable1 = new TestRunnable(startLatch);
        TestRunnable runnable2 = new TestRunnable(startLatch);

        executor.execute(runnable1);
        executor.execute(runnable2);

        //If this times out then both events have not started concurrently.
        startLatch.await(5, TimeUnit.SECONDS);

        assertFalse(runnable1.isFinished());
        assertFalse(runnable2.isFinished());

        runnable1.proceed();

        assertTrue(runnable1.isFinished());
        assertFalse(runnable2.isFinished());

        runnable2.proceed();
        assertTrue(runnable1.isFinished());
        assertTrue(runnable2.isFinished());

        assertTrue(jiraEventExecutorFactory.shutdownAndWait(5));
    }

    @Test
    public void testDirectRunOnShutdownAsync() throws InterruptedException
    {
        final JiraEventExecutorFactory jiraEventExecutorFactory = new JiraEventExecutorFactory();
        assertTrue(jiraEventExecutorFactory.shutdownAndWait(5));

        final DirectRunnable command = new DirectRunnable();
        jiraEventExecutorFactory.getExecutor().execute(command);
        assertTrue(command.isCalled());
    }

    static class DirectRunnable implements Runnable
    {
        private boolean called = false;
        @Override
        public void run()
        {
            called = true;
        }

        public boolean isCalled()
        {
            return called;
        }
    }

    private static class TestRunnable implements Runnable
    {
        private final CountDownLatch holdLatch = new CountDownLatch(1);
        private final CountDownLatch endLatch = new CountDownLatch(1);
        private final CountDownLatch startLatch;

        private TestRunnable(final CountDownLatch startLatch) {this.startLatch = startLatch;}

        @Override
        public void run()
        {
            startLatch.countDown();
            try
            {
                holdLatch.await();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            endLatch.countDown();
        }

        public boolean isFinished()
        {
            return endLatch.getCount() == 0;
        }

        public void proceed()
        {
            holdLatch.countDown();
            try
            {
                endLatch.await(5, TimeUnit.SECONDS);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException("Runnable did not complete in time.", e);
            }
        }
    }
}
