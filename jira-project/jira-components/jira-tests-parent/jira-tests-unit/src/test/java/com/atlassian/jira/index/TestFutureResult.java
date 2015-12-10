package com.atlassian.jira.index;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.util.RuntimeInterruptedException;
import com.atlassian.util.concurrent.TimedOutException;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestFutureResult
{
    @Test
    public void testNullFuture() throws Exception
    {
        try
        {
            new FutureResult(null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testFutureNotDone() throws Exception
    {
        assertFalse(new FutureResult(new FutureTask<Result>(new Callable<Result>()
        {
            public Result call() throws Exception
            {
                return null;
            }
        })).isDone());
    }

    @Test
    public void testFutureDone() throws Exception
    {
        final FutureTask<Result> future = new FutureTask<Result>(new Callable<Result>()
        {
            public Result call() throws Exception
            {
                return null;
            }
        });
        final FutureResult result = new FutureResult(future);
        future.run();
        assertTrue(result.isDone());
    }

    @Test
    public void testFutureAwait() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final FutureResult result = new FutureResult(new MockFuture<Result>()
        {
            @Override
            public Result get() throws InterruptedException, ExecutionException
            {
                return new MockResult()
                {
                    @Override
                    public void await()
                    {
                        called.set(true);
                    }
                };
            }
        });
        result.await();
        assertTrue(called.get());
    }

    @Test
    public void testFutureAwaitTimeout() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final FutureResult result = new FutureResult(new MockFuture<Result>()
        {
            @Override
            public Result get(final long arg0, final TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException
            {
                return new MockResult()
                {
                    @Override
                    public boolean await(final long timeout, final TimeUnit unit)
                    {
                        assertSame(unit, arg1);
                        assertTrue(timeout <= arg0);
                        called.set(true);
                        return true;
                    }
                };
            }
        });
        result.await(1, TimeUnit.SECONDS);
        assertTrue(called.get());
    }

    @Test
    public void testFutureTimesOut() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final FutureResult result = new FutureResult(new MockFuture<Result>()
        {
            @Override
            public Result get(final long time, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
            {
                throw new TimedOutException(time, unit);
            }
        });
        result.await(1, TimeUnit.SECONDS);
        assertFalse(called.get());
    }

    @Test
    public void testFutureGetInterrupted() throws Exception
    {
        final FutureResult result = new FutureResult(new MockFuture<Result>()
        {
            @Override
            public Result get() throws InterruptedException, ExecutionException
            {
                throw new InterruptedException();
            }
        });
        try
        {
            result.await();
            fail("RuntimeInterruptedException expected");
        }
        catch (final RuntimeInterruptedException expected)
        {}
    }

    @Test
    public void testFutureGetTimeoutInterrupted() throws Exception
    {
        final FutureResult result = new FutureResult(new MockFuture<Result>()
        {
            @Override
            public Result get(final long time, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
            {
                throw new InterruptedException();
            }
        });
        try
        {
            result.await(1, TimeUnit.SECONDS);
            fail("RuntimeInterruptedException expected");
        }
        catch (final RuntimeInterruptedException expected)
        {}
    }
}
