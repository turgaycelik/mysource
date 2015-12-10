package com.atlassian.jira.util;

import com.atlassian.core.util.Clock;
import com.google.common.collect.Maps;

import java.util.Date;
import java.util.NavigableMap;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;

/**
 * Composite clock that returns answer from one of the clocks it builds upon, depending on the number of times it
 * was called already.
 *
 * @since v4.3
 */
public class CompositeClock implements Clock
{
    private final NavigableMap<Integer, Clock> clocks = Maps.newTreeMap();
    private int currentCall = 1;


    public CompositeClock(Clock initialClock)
    {
        clocks.put(1, notNull("initialClock", initialClock));
    }

    public CompositeClock addClock(int startFromCall, Clock clock)
    {
        greaterThan("startFromCall", startFromCall, clocks.lastKey());
        clocks.put(startFromCall, clock);
        return this;
    }

    @Override
    public Date getCurrentDate()
    {
        Date answer = findClock().getCurrentDate();
        currentCall++;
        return answer;
    }

    private Clock findClock()
    {
        if (currentCall >= clocks.lastKey())
        {
            return clocks.lastEntry().getValue();
        }
        int clockKey = clocks.firstKey();
        for (int startFromCall : clocks.keySet())
        {
            if (currentCall < startFromCall)
            {
                break;
            }
            else
            {
                clockKey = startFromCall;
            }
        }
        return clocks.get(clockKey);
    }
}
