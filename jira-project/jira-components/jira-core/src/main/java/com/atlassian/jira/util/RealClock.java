package com.atlassian.jira.util;

import com.atlassian.core.util.Clock;

import java.util.Date;

/**
 * A clock implementation the returns the current time.
 *
 * @since v4.0
 */
public final class RealClock implements Clock
{
    private static final RealClock INSTANCE = new RealClock();

    private RealClock()
    {
    }
    
    public Date getCurrentDate()
    {
        return new Date();
    }

    public static RealClock getInstance()
    {
        return INSTANCE;
    }
}
