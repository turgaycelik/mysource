package com.atlassian.jira.issue.statistics.util;

import java.util.Comparator;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @since v5.0
 */
public class TestLongComparator
{
    @Test
    public void testCompareFirstNull() throws Exception
    {
        Comparator<Long> comparator = LongComparator.COMPARATOR;
        int compare = comparator.compare(null, new Long(10));
        assertEquals(-1, compare);
    }

    @Test
    public void testCompareSecondNull() throws Exception
    {
        Comparator<Long> comparator = LongComparator.COMPARATOR;
        int compare = comparator.compare(new Long(10), null);
        assertEquals(1, compare);
    }

    @Test
    public void testCompareBothNull() throws Exception
    {
       Comparator<Long> comparator = LongComparator.COMPARATOR;
       int compare = comparator.compare(null, null);
       assertEquals(0, compare);
    }

    @Test
    public void testCompareFirstGreater() throws Exception
    {
        Comparator<Long> comparator = LongComparator.COMPARATOR;
        int compare = comparator.compare(new Long(10), new Long(8));
        assertEquals(1, compare);
    }

    @Test
    public void testCompareSecondGreater() throws Exception
    {
        Comparator<Long> comparator = LongComparator.COMPARATOR;
        int compare = comparator.compare(new Long(10), new Long(12));
        assertEquals(-1, compare);
    }

    @Test
    public void testCompareEquals() throws Exception
    {
        Comparator<Long> comparator = LongComparator.COMPARATOR;
        int compare = comparator.compare(new Long(10), new Long(10));
        assertEquals(0, compare);
    }
}
