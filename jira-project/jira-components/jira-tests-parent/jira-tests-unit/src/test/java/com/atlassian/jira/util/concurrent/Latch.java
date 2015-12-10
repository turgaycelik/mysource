package com.atlassian.jira.util.concurrent;

import java.util.concurrent.CountDownLatch;

import com.atlassian.jira.util.RuntimeInterruptedException;

public class Latch extends CountDownLatch
{
    public Latch(final int permits)
    {
        super(permits);
    }

    @Override
    public void await()
    {
        try
        {
            super.await();
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeInterruptedException(e);
        }
    }
}
