package com.atlassian.jira.mock.matcher;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.mock.matcher.ComparableMatcher}.
 *
 * @since v4.0
 */
@SuppressWarnings ({ "UnnecessaryLocalVariable" })
public class TestComparableMatcher
{
    @Test
    public void testMatcher() throws Exception
    {
        @SuppressWarnings ({ "RedundantTypeArguments" }) ArgumentMatcher<SubClass> sup = ComparableMatcher.<SubClass>comparableMatcher();
        assertTrue(sup.match(null, null));
        assertTrue(sup.match(new SubClass(1), new SubClass(1)));

        assertFalse(sup.match(null, new SubClass(1)));
        assertFalse(sup.match(new SubClass(1), null));
        assertFalse(sup.match(new SubClass(1), new SubClass(2)));
    }

    @Test
    public void testToString() throws Exception
    {
        @SuppressWarnings ({ "RedundantTypeArguments" }) ArgumentMatcher<SubClass> sup = ComparableMatcher.<SubClass>comparableMatcher();
        assertTrue(StringUtils.isNotBlank(sup.toString(null)));
        assertEquals("57583", sup.toString(new SubClass(57583)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStaticFactory() throws Exception
    {
        @SuppressWarnings ({ "RedundantTypeArguments" }) final ArgumentMatcher<Integer> integerArgumentMatcher = ComparableMatcher.<Integer>comparableMatcher();
        @SuppressWarnings ({ "RedundantTypeArguments" }) final ArgumentMatcher<Double> doubleArgumentMatcher = ComparableMatcher.<Double>comparableMatcher();

        assertSame(doubleArgumentMatcher, integerArgumentMatcher);

        //This tests the logic behind the cast to ArgumentMatcher<T> in the ComparableMatcher.comparableMatcher(). We need
        //to make sure that the singleton matcher will work for all Comparable arguments.
        final ArgumentMatcher rawMatcher = doubleArgumentMatcher;
        assertTrue(rawMatcher.match(null, null));
        assertTrue(rawMatcher.match(1948543, 1948543));
        assertTrue(rawMatcher.match(45.67, 45.67));
        assertFalse(rawMatcher.match(34.5, 122.202));
        assertFalse(rawMatcher.match(89739847, 387852984));
    }

    private static class Super implements Comparable<Super>
    {
        private final Integer id;

        public Super(final Integer id)
        {
            this.id = id;
        }

        public int compareTo(final Super o)
        {
            return id.compareTo(o.id);
        }

        @SuppressWarnings ({ "EqualsWhichDoesntCheckParameterClass" })
        @Override
        public boolean equals(final Object obj)
        {
            //This is a very bad class, but this is just for testing.
            return false;
        }

        @Override
        public String toString()
        {
            return id.toString();
        }
    }

    public static class SubClass extends Super
    {
        public SubClass(final Integer id)
        {
            super(id);
        }
    }
}
