package com.atlassian.jira.util;

import com.atlassian.core.util.Clock;

/**
 * Utilities for clocks.
 *
 * @since v4.3
 */
public final class Clocks
{
    private Clocks()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static Clock getClock(Object instance)
    {
        if (instance instanceof ClockAware)
        {
            return ((ClockAware)instance).clock();
        }
        else
        {
            return RealClock.getInstance();
        }
    }
}
