package com.atlassian.jira.util.collect;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestListOrderComparator
{
    @Test
    public void testSimpleGet() throws Exception
    {
        final List<String> list = Lists.newArrayList("one", "four", "eight");
        final Comparator<String> comparator = new ListOrderComparator<String>(list);
        final List<String> result = Lists.newArrayList("eight", "one", "four");

        Collections.sort(result, comparator);
        assertEquals(3, result.size());
        assertEquals("one", result.get(0));
        assertEquals("four", result.get(1));
        assertEquals("eight", result.get(2));
    }

    @Test
    public void testGetThrowsIllegalArg() throws Exception
    {
        final List<String> list = Lists.newArrayList("one", "four", "eight");
        final Comparator<String> comparator = new ListOrderComparator<String>(list);
        final List<String> result = Lists.newArrayList("eight", "one", "six");

        try
        {
            Collections.sort(result, comparator);
            fail("Should have thrown IllegalArg");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }
}
