package com.atlassian.jira.task.context;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple counter class that can return percentage complete.
 *
 * @since v3.13
 */
@Internal
class PercentageProgressCounter
{
    private final Calculator calculator;
    private final AtomicInteger done = new AtomicInteger();

    /**
     * @param total no of tasks, should be positive non-zero.
     */
    PercentageProgressCounter(final int total)
    {
        calculator = new Calculator(total);
    }

    /**
     * Increment the done count.
     *
     * @return true if the percentage int changed as a result
     */
    boolean increment()
    {
        return calculator.calculate(done.incrementAndGet());
    }

    int getCountComplete()
    {
        return done.get();
    }

    int getPercentComplete()
    {
        return calculator.get();
    }

    /**
     * Holds the current percentage and works out our new percentage. The calculation
     * also tells us if the current percentage has changed when we try and update it.
     */
    private static class Calculator
    {
        private static final int HUNDRED = 100;

        private final int total;
        private final AtomicInteger current = new AtomicInteger();

        public Calculator(final int total)
        {
            Assertions.not("total <= 0", total <= 0);
            this.total = total;
        }

        boolean calculate(final int count)
        {
            final int percentage = (count * HUNDRED) / total;
            return (percentage != current.getAndSet(percentage > HUNDRED ? HUNDRED : percentage));
        }

        int get()
        {
            return current.get();
        }
    }
}
