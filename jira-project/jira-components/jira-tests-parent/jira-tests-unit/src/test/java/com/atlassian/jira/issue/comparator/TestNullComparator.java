package com.atlassian.jira.issue.comparator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestNullComparator
{
    private NullComparator comparator = new NullComparator();

    @Test
    public void testNulls()
    {
        assertEquals(0, comparator.compare(null, null));

        assertTrue(comparator.compare("test", null) > 0);

        assertTrue(comparator.compare(null, "test") < 0);
    }

    @Test
    public void testNonNulls()
    {
        Integer i1 = new Integer(1);
        Integer i2 = new Integer(2);
        String str = "some string";
        Object obj = new Object();

        assertEquals(0, comparator.compare(i1, i1));
        assertTrue(comparator.compare(i1, i2) < 0);
        assertTrue(comparator.compare(i2, i1) > 0);
        assertEquals(0, comparator.compare(str, str));

        try
        {
            comparator.compare(i1, str);
            fail("Cannot compare comparables and not comparables");
        }
        catch (ClassCastException e)
        {
            // expected
        }
        try
        {
            comparator.compare(i2, str);
            fail("Cannot compare comparables and not comparables");
        }
        catch (ClassCastException e)
        {
            // expected
        }
        try
        {
            comparator.compare(str, i1);
            fail("Cannot compare comparables and not comparables");
        }
        catch (ClassCastException e)
        {
            // expected
        }

        // comparing non-comparable and something else (not necessarily comparable) returns 0
        assertEquals(0, comparator.compare(obj, i1));
        assertEquals(0, comparator.compare(obj, i2));
        assertEquals(0, comparator.compare(obj, str));

        // doing it the other way results in nasty ClassCastException
        try
        {
            comparator.compare(i1, obj);
            fail("Cannot compare comparables and not comparables");
        }
        catch (ClassCastException e)
        {
            // expected
        }
        try
        {
            comparator.compare(str, obj);
            fail("Cannot compare comparables and not comparables");
        }
        catch (ClassCastException e)
        {
            // expected
        }
    }

}
