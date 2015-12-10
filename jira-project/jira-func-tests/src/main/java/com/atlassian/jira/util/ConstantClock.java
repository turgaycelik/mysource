package com.atlassian.jira.util;

import com.atlassian.core.util.Clock;

import java.util.Date;

/**
 * Simple clock that returns a constant value given during construction. Useful for testing date related functions.
 *
 * @since v4.0
 */
public final class ConstantClock implements Clock
{
    private final Date currentDate;

    public ConstantClock(final Date currentDate)
    {
        this.currentDate = currentDate;
    }

    public Date getCurrentDate()
    {
        return currentDate;
    }

    @Override
    public String toString()
    {
        return "Constant Time Clock [" + currentDate + "]";
    }
}
