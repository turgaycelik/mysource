package com.atlassian.jira.functest.unittests.util;

import com.atlassian.jira.util.PeriodicClock;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Test case for {@link com.atlassian.jira.util.PeriodicClock}.
 *
 * @since v4.3
 */
public class TestPeriodicClock
{

    @Test
    public void shouldReturnCorrectDatesForSimplePeriod()
    {
        PeriodicClock tested = new PeriodicClock(50);
        List<Long> results = Lists.newArrayList();
        for (int i=0; i<8; i++)
        {
            results.add(tested.getCurrentDate().getTime());
        }
        assertEquals(Arrays.asList(0L,50L,100L,150L,200L,250L,300L,350L), results);
    }

    @Test
    public void shouldReturnCorrectDatesForLongPeriod()
    {
        PeriodicClock tested = new PeriodicClock(50, 3);
        List<Long> results = Lists.newArrayList();
        for (int i=0; i<12; i++)
        {
            results.add(tested.getCurrentDate().getTime());
        }
        assertEquals(Arrays.asList(0L,0L,0L,50L,50L,50L,100L,100L,100L,150L,150L,150L), results);
    }

    @Test
    public void shouldReturnCorrectDatesForSimplePeriodAndCustomInitialValue()
    {
        PeriodicClock tested = new PeriodicClock(40, 50, 1);
        List<Long> results = Lists.newArrayList();
        for (int i=0; i<8; i++)
        {
            results.add(tested.getCurrentDate().getTime());
        }
        assertEquals(Arrays.asList(40L,90L,140L,190L,240L,290L,340L,390L), results);
    }

    @Test
    public void shouldReturnCorrectDatesForLongPeriodAndCustomInitialValue()
    {
        PeriodicClock tested = new PeriodicClock(30, 50, 3);
        List<Long> results = Lists.newArrayList();
        for (int i=0; i<12; i++)
        {
            results.add(tested.getCurrentDate().getTime());
        }
        assertEquals(Arrays.asList(30L,30L,30L,80L,80L,80L,130L,130L,130L,180L,180L,180L), results);
    }
}
