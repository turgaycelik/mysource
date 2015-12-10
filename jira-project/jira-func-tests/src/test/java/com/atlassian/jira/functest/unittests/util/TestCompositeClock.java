package com.atlassian.jira.functest.unittests.util;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.util.CompositeClock;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link com.atlassian.jira.util.CompositeClock}.
 *
 * @since v4.3
 */
public class TestCompositeClock
{

    private final List<String> calls = Lists.newArrayList();

    private class MockClock implements Clock
    {
        private final String name;

        public MockClock(String name)
        {
            this.name = name;
        }

        @Override
        public Date getCurrentDate()
        {
            calls.add(name);
            return new Date();
        }
    }

    @Test
    public void shouldCallThroughToCorrectClock()
    {
        CompositeClock tested = new CompositeClock(new MockClock("initial")).addClock(3, new MockClock("second"));
        for (int i=0; i<8; i++)
        {
            tested.getCurrentDate();
        }
        assertEquals(Arrays.asList(
                "initial", "initial",
                "second", "second", "second", "second", "second", "second"),
                calls);
    }
}
