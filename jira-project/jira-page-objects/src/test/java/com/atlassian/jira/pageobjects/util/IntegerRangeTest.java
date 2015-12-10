package com.atlassian.jira.pageobjects.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class IntegerRangeTest
{
    @Test
    public void testSimpleRange() throws Exception
    {
        final List<Integer> expectedDigits = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(expectedDigits, ImmutableList.copyOf(IntegerRange.ofSize(10)));
    }

    @Test
    public void testRangeWithFrom() throws Exception
    {
        // is it politically correct to say "expected teens"?
        final List<Integer> expectedTeens = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        assertEquals(expectedTeens, ImmutableList.copyOf(IntegerRange.of(10, 20)));
    }

    @Test
    public void testRangeWithFromSize() throws Exception
    {
        final List<Integer> expectedTeens = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        assertEquals(expectedTeens, ImmutableList.copyOf(IntegerRange.ofSizeStartingAt(10, 10)));
    }

}
