package com.atlassian.jira.util;

import com.atlassian.core.util.Clock;
import com.google.common.collect.ImmutableList;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A mock {@link com.atlassian.core.util.Clock} that will return predefined dates and will throws exception, if
 * called more times than the number of predefined values.
 *
 * @since v4.3
 */
public class StrictMockClock implements Clock
{
    private static final Logger log = Logger.getLogger(StrictMockClock.class);

    private final List<Long> times;
    private int current = 0;

    public StrictMockClock(List<Long> times)
    {
        this.times = ImmutableList.copyOf(times);
    }

    public StrictMockClock(Long... times)
    {
        this.times = Arrays.asList(times);
    }

    public List<Long> times()
    {
        return ImmutableList.copyOf(times);
    }

    public long first()
    {
        if (times.isEmpty())
        {
            return -1L;
        }
        return times.get(0);
    }

    public long last()
    {
        if (times.isEmpty())
        {
            return -1L;
        }
        return times.get(times.size()-1);
    }

    @Override
    public Date getCurrentDate()
    {
        log.debug("#getCurrentDate: times=" + times + ",current=" + current);        
        if (current >= times.size())
        {
            throw new IllegalStateException("Called too many times, only supports " + times.size() + " invocations");
        }
        return new Date(times.get(current++));
    }
}
