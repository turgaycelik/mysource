package com.atlassian.jira.index;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.util.RuntimeInterruptedException;
import com.atlassian.util.concurrent.Timeout;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Result implementation that wraps a {@link Future}.
 */
final class FutureResult implements Result
{
    private final Future<Result> future;

    public FutureResult(@Nonnull final Future<Result> future)
    {
        this.future = notNull("future", future);
    }

    public void await()
    {
        try
        {
            future.get().await();
        }
        catch (final ExecutionException e)
        {
            throw new RuntimeException(e);
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeInterruptedException(e);
        }
    }

    public boolean await(final long time, final TimeUnit unit)
    {
        final Timeout timeout = Timeout.getNanosTimeout(time, unit);
        try
        {
            return future.get(timeout.getTime(), timeout.getUnit()).await(timeout.getTime(), timeout.getUnit());
        }
        catch (final ExecutionException e)
        {
            throw new RuntimeException(e);
        }
        catch (final TimeoutException e)
        {
            return false;
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeInterruptedException(e);
        }
    }

    public boolean isDone()
    {
        return future.isDone();
    }
}