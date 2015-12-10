package com.atlassian.jira.concurrent;

import javax.annotation.Nonnull;

/**
 * Mock BarrierFactory implementation.
 */
public class MockBarrierFactory implements BarrierFactory
{
    @Nonnull
    @Override
    public Barrier getBarrier(final String barrierName)
    {
        return new Barrier()
        {
            @Override
            public String name()
            {
                return barrierName;
            }

            @Override
            public void await()
            {
            }

            @Override
            public void raise()
            {
            }

            @Override
            public void lower()
            {
            }
        };
    }
}
