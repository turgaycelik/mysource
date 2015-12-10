package com.atlassian.jira.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.joda.time.Duration;
import org.junit.Test;

import junit.framework.TestCase;

import static org.junit.Assert.assertTrue;

/**
 *
 * @since v6.3
 */
public class TestBoundedExecutorServiceWrapper
{
    @Test
    public void boundedExecutorServiceWrapperRunsThingsCorrectly() throws Exception
    {
        BoundedExecutorServiceWrapper s = new BoundedExecutorServiceWrapper.Builder().withConcurrency(1).withThreadPoolName("tester").build();
        TestCase.assertEquals(Integer.valueOf(1), s.submit(new Callable<Integer>()
        {
            @Override
            public Integer call() throws Exception
            {
                if (!"tester".equals(Thread.currentThread().getThreadGroup().getName()))
                {
                    throw new IllegalStateException("Incorrect thread group name:" + Thread.currentThread().getThreadGroup().getName() + " expected 'tester'");
                }
                return 1;
            }
        }).claim());
    }

    @Test
    public void boundedExecutorServiceWrapperShutsDown() throws Exception
    {
        BoundedExecutorServiceWrapper s = new BoundedExecutorServiceWrapper.Builder().withExecutorService(
                new Supplier<ListeningExecutorService>()
                {
                    @Override
                    public ListeningExecutorService get()
                    {
                        return MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
                    }
                }).withConcurrency(1).withShutdownTimeout(Duration.millis(1000)).build();
        TestCase.assertEquals(Integer.valueOf(1), s.submit(new Callable<Integer>()
        {
            @Override
            public Integer call() throws Exception
            {
                return 1;
            }
        }).claim());
        assertTrue(s.awaitTermination());
    }
}
