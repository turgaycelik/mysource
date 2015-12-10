package com.atlassian.jira.issue.comparator.util;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDelegatingComparator
{
    private final Comparator<?> cZero = new Comparator<Object>()
    {
        public int compare(final Object o1, final Object o2)
        {
            return 0;
        }
    };

    private final Comparator<?> cPositive = new Comparator<Object>()
    {
        public int compare(final Object o1, final Object o2)
        {
            return 1;
        }
    };

    private final Comparator<?> cNegative = new Comparator<Object>()
    {
        public int compare(final Object o1, final Object o2)
        {
            return -1;
        }
    };

    @Test
    public void testAllCombinations()
    {
        assertEquals(0, new DelegatingComparator(cZero, cZero).compare("1", "2"));
        assertTrue(new DelegatingComparator(cZero, cPositive).compare("1", "2") > 0);
        assertTrue(new DelegatingComparator(cZero, cNegative).compare("1", "2") < 0);

        assertTrue(new DelegatingComparator(cPositive, cZero).compare("1", "2") > 0);
        assertTrue(new DelegatingComparator(cPositive, cPositive).compare("1", "2") > 0);
        assertTrue(new DelegatingComparator(cPositive, cNegative).compare("1", "2") > 0);

        assertTrue(new DelegatingComparator(cNegative, cZero).compare("1", "2") < 0);
        assertTrue(new DelegatingComparator(cNegative, cPositive).compare("1", "2") < 0);
        assertTrue(new DelegatingComparator(cNegative, cNegative).compare("1", "2") < 0);
    }

    @Test
    public void testFirstZeroResultsInCallToSecond()
    {
        final AtomicBoolean wasCalled = new AtomicBoolean(false);

        final Comparator<?> lc = new Comparator<Object>()
        {
            public int compare(final Object o1, final Object o2)
            {
                wasCalled.set(true);
                return 123;
            }
        };

        assertEquals(123, new DelegatingComparator(cZero, lc).compare("1", "2"));
        assertTrue(wasCalled.get());
    }

    @Test
    public void testFirstNonZeroResultsInNoCallToSecond()
    {
        final AtomicBoolean wasCalled = new AtomicBoolean(false);

        final Comparator<?> lc = new Comparator<Object>()
        {
            public int compare(final Object o1, final Object o2)
            {
                wasCalled.set(true);
                return 123;
            }
        };

        assertTrue(new DelegatingComparator(cPositive, lc).compare("1", "2") > 0);
        assertFalse(wasCalled.get());

        assertTrue(new DelegatingComparator(cNegative, lc).compare("1", "2") < 0);
        assertFalse(wasCalled.get());
    }

}
