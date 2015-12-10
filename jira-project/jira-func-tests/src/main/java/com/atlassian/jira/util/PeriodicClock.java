package com.atlassian.jira.util;

import com.atlassian.core.util.Clock;

import java.util.Date;

import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;

/**
 * A {@link com.atlassian.core.util.Clock} implementation that periodically increments the returned value.
 *
 * @since v4.3
 */
public class PeriodicClock implements Clock
{
    private final long increment;
    private final int periodLength;

    private long current;
    private int currentInPeriod = 0;

    public PeriodicClock(long intitialValue, long increment, int periodLength)
    {
        this.current = intitialValue;
        this.increment = greaterThan("increment", increment, 0);
        this.periodLength = greaterThan("periodLength", periodLength, 0);
    }

    public PeriodicClock(long increment, int periodLength)
    {
        this(0, increment, periodLength);
    }

    public PeriodicClock(long increment)
    {
        this(0, increment, 1);
    }


    @Override
    public Date getCurrentDate()
    {
        long answer = current;
        if (periodFinished())
        {
            current += increment;
        }
        return new Date(answer);
    }

    private boolean periodFinished()
    {
        currentInPeriod++;
        if (currentInPeriod == periodLength)
        {
            currentInPeriod = 0;
            return true;
        }
        return false;
    }
}
