package com.atlassian.jira.mock.matcher;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A test for {@link com.atlassian.jira.mock.matcher.ComparatorAdaptorMatcher}.
 *
 * @since v4.0
 */
public class TestComparatorAdaptorMatcher
{
    @Test
    public void testMatch() throws Exception
    {
        ComparatorAdaptorMatcher<SubBadInteger> matcher = new ComparatorAdaptorMatcher<SubBadInteger>(new BadIntegerComparator());
        assertTrue(matcher.match(null, null));
        assertTrue(matcher.match(new SubBadInteger(1), new SubBadInteger(1)));
        assertTrue(matcher.match(new SubBadInteger(50), new SubBadInteger(50)));
        assertTrue(matcher.match(new SubBadInteger(23398), new SubBadInteger(23398)));

        assertFalse(matcher.match(null, new SubBadInteger(1)));
        assertFalse(matcher.match(new SubBadInteger(1), null));
        assertFalse(matcher.match(new SubBadInteger(1), new SubBadInteger(2)));
    }

    @Test
    public void testToString() throws Exception
    {
        ComparatorAdaptorMatcher<SubBadInteger> matcher = new ComparatorAdaptorMatcher<SubBadInteger>(new BadIntegerComparator());
        assertTrue(StringUtils.isNotBlank(matcher.toString(null)));
        assertEquals("BadInt: 5780", matcher.toString(new SubBadInteger(5780)));
    }

    private static class BadIntegerComparator implements Comparator<BadInteger>
    {
        public int compare(final BadInteger o1, final BadInteger o2)
        {
            return o1.value - o2.value;
        }
    }

    private static class BadInteger
    {
        private final int value;

        private BadInteger(final int value)
        {
            this.value = value;
        }

        @SuppressWarnings ({ "EqualsWhichDoesntCheckParameterClass" })
        @Override
        public boolean equals(final Object obj)
        {
            return false;
        }

        @Override
        public String toString()
        {
            return "BadInt: " + String.valueOf(value);
        }
    }

    public static class SubBadInteger extends BadInteger
    {
        private SubBadInteger(final int value)
        {
            super(value);
        }
    }
}
