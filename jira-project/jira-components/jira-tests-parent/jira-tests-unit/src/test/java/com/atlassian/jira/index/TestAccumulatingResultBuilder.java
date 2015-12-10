package com.atlassian.jira.index;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.util.RuntimeInterruptedException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import junit.framework.AssertionFailedError;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith (JUnit4.class)
public class TestAccumulatingResultBuilder
{
    @Test
    public void testNonNullResult() throws Exception
    {
        assertNotNull(new AccumulatingResultBuilder().add(new MockResult()).toResult());
    }

    @Test
    public void testNullResultNotAllowed() throws Exception
    {
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        try
        {
            builder.add(null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNullCompletionTaskNotAllowed() throws Exception
    {
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        try
        {
            builder.addCompletionTask(null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testResultThatCallsInnerResultAwait() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public void await()
            {
                count.getAndIncrement();
            }
        });
        builder.toResult().await();
        assertEquals(1, count.get());
    }

    @Test
    public void testResultThatCallsInnerResultAwaitWithTimeout() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public boolean await(final long timeout, final TimeUnit unit)
            {
                count.getAndIncrement();
                return true;
            }
        });
        assertTrue(builder.toResult().await(100, TimeUnit.SECONDS));
        assertEquals(1, count.get());
    }

    @Test
    public void testResultThatCallsInnerResultIsDone() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public boolean isDone()
            {
                count.getAndIncrement();
                return true;
            }
        });
        assertTrue(builder.toResult().isDone());
        assertEquals(1, count.get());
    }

    @Test
    public void testResultThatReturnNotDoneIfInnerNotDone() throws Exception
    {
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public boolean isDone()
            {
                return false;
            }
        });
        assertFalse(builder.toResult().isDone());
    }

    @Test
    public void testResultThatCallsCompletionTask() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        final Runnable completionTask = new Runnable()
        {
            public void run()
            {
                count.getAndIncrement();
            }
        };
        builder.addCompletionTask(completionTask);
        builder.toResult().await();
        assertEquals(1, count.get());
    }

    @Test
    public void testResultCallsCompletionTasksOnlyOnce() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        final Runnable completionTask = new Runnable()
        {
            public void run()
            {
                count.getAndIncrement();
            }
        };
        builder.addCompletionTask(completionTask);
        final Result result = builder.toResult();
        result.await();
        assertEquals(1, count.get());
        result.await();
        assertEquals(1, count.get());
    }

    @Test
    public void testResultThatDoesNotCallCompletionTaskIfInterrupted() throws Exception
    {
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public void await()
            {
                throw new RuntimeInterruptedException(new InterruptedException());
            }
        });
        final Runnable completionTask = new Runnable()
        {
            ///CLOVER:OFF
            public void run()
            {
                throw new AssertionFailedError("should not be called");
            }
            ///CLOVER:ON

        };
        builder.addCompletionTask(completionTask);
        try
        {
            builder.toResult().await();
            fail("RuntimeInterruptedException expected");
        }
        catch (final IndexingFailureException expected)
        {}
    }

    @Test
    public void testResultThatDoesNotCallCompletionTaskIfAwaitTimeoutFails() throws Exception
    {
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public boolean await(final long timeout, final TimeUnit unit)
            {
                return false;
            }
        });
        final Runnable completionTask = new Runnable()
        {
            public void run()
            {
                throw new AssertionFailedError("should not be called");
            }
        };
        builder.addCompletionTask(completionTask);
        builder.toResult().await(100, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testFailuresReported() throws Exception
    {
        final AccumulatingResultBuilder builder = new AccumulatingResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public boolean await(final long timeout, final TimeUnit unit)
            {
                return true;
            }
        });
        builder.add(new MockResult()
        {
            @Override
            public boolean await(final long timeout, final TimeUnit unit)
            {
                throw new IllegalStateException("Test fail 1");
            }
        });
        builder.add(new MockResult()
        {
            @Override
            public boolean await(final long timeout, final TimeUnit unit)
            {
                throw new IllegalStateException("Test fail 2");
            }
        });
        final Runnable completionTask = new Runnable()
        {
            public void run()
            {
                throw new AssertionFailedError("should not be called");
            }
        };
        builder.addCompletionTask(completionTask);
        try
        {
            builder.toResult().await(100, TimeUnit.MILLISECONDS);
            fail("An Indexing failed error should have been thrown");
        }
        catch (IndexingFailureException e)
        {
            assertEquals("Indexing completed with 2 errors", e.getMessage());
        }


    }
}
