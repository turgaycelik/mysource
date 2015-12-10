package com.atlassian.jira.issue.comparator;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractComparatorTestCase
{
    /**
     * Executes compare method on given objects a and b using given comparator
     * and asserts that the result of comparison is negative.
     *
     * @param comparator comparator to use
     * @param a          object to compare
     * @param b          object to compare
     */
    public <T> void assertLessThan(Comparator<T> comparator, T a, T b)
    {
        assertTrue(comparator.compare(a, b) < 0);
    }

    /**
     * Executes compare method on given objects a and b using given comparator
     * and asserts that the result of comparison is positive..
     *
     * @param comparator comparator to use
     * @param a          object to compare
     * @param b          object to compare
     */
    public <T> void assertGreaterThan(Comparator<T> comparator, T a, T b)
    {
        assertTrue(comparator.compare(a, b) > 0);
    }

    /**
     * Executes compare method on given objects a and b using given comparator
     * and asserts that the result of comparison is zero.
     *
     * @param comparator comparator to use
     * @param a          object to compare
     * @param b          object to compare
     */
    public <T> void assertEqualTo(Comparator<T> comparator, T a, T b)
    {
        assertEquals(0, comparator.compare(a, b));
    }
}
