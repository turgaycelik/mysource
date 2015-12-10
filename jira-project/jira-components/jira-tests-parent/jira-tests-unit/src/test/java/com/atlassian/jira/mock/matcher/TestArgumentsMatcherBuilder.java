package com.atlassian.jira.mock.matcher;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

import org.easymock.ArgumentsMatcher;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.mock.matcher.TestArgumentsMatcherBuilder}.
 *
 * @since v4.0
 */
@SuppressWarnings ({ "RedundantTypeArguments" })
public class TestArgumentsMatcherBuilder
{
    @Test
    public void testNewBuilderAlwaysBuilder() throws Exception
    {
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newAlwaysBuilder();
        assertTrue(builder.getDefaultMatcher() instanceof AlwaysMatcher);
    }

    @Test
    public void testNewBuilder() throws Exception
    {
        final ArgumentMatcher<String> defaultMatcher = new ComparatorAdaptorMatcher<String>(Collator.getInstance(Locale.US));
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newBuilder(String.class, defaultMatcher);

        assertSame(builder.getDefaultMatcher(), defaultMatcher);
    }

    @Test
    public void testSetDefaultMatcher() throws Exception
    {
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newAlwaysBuilder();
        assertTrue(builder.getDefaultMatcher() instanceof AlwaysMatcher);

        final ArgumentMatcher<String> defaultMatcher = new ComparatorAdaptorMatcher<String>(Collator.getInstance(Locale.US));
        builder.setDefaultMatcher(String.class, defaultMatcher);
        assertSame(builder.getDefaultMatcher(), defaultMatcher);
    }

    @Test
    public void testAddDefaultMatcher() throws Exception
    {
        final ArgumentMatcher<String> defaultMatcher = new ComparatorAdaptorMatcher<String>(Collator.getInstance(Locale.US));
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newBuilder(String.class, defaultMatcher);

        builder.addDefaultMatcher();
        assertSame(defaultMatcher, builder.getMatcherForArgument(0));
        builder.addDefaultMatcher();
        assertSame(defaultMatcher, builder.getMatcherForArgument(0));
        assertSame(defaultMatcher, builder.getMatcherForArgument(1));
    }

    @Test
    public void testAddAlwaysDefaultMatcher() throws Exception
    {
        final ArgumentMatcher<String> defaultMatcher = new ComparatorAdaptorMatcher<String>(Collator.getInstance(Locale.US));
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newBuilder(String.class, defaultMatcher);

        builder.addAlwaysMatcher();
        assertNotSame(defaultMatcher, builder.getMatcherForArgument(0));
        assertTrue(builder.getMatcherForArgument(0) instanceof AlwaysMatcher);
        builder.addAlwaysMatcher(Integer.class);
        assertNotSame(defaultMatcher, builder.getMatcherForArgument(0));
        assertTrue(builder.getMatcherForArgument(0) instanceof AlwaysMatcher);
        assertNotSame(defaultMatcher, builder.getMatcherForArgument(1));
        assertTrue(builder.getMatcherForArgument(1) instanceof AlwaysMatcher);
    }

    @Test
    public void testAddNatrualMatcher() throws Exception
    {
        final ArgumentMatcher<String> defaultMatcher = new ComparatorAdaptorMatcher<String>(Collator.getInstance(Locale.US));
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newBuilder(String.class, defaultMatcher);

        builder.addNaturalMatcher(Double.class);
        assertNotSame(defaultMatcher, builder.getMatcherForArgument(0));
        assertTrue(builder.getMatcherForArgument(0) instanceof NaturalMatcher);
        builder.addNaturalMatcher();
        assertNotSame(defaultMatcher, builder.getMatcherForArgument(0));
        assertTrue(builder.getMatcherForArgument(0) instanceof NaturalMatcher);
        assertNotSame(defaultMatcher, builder.getMatcherForArgument(1));
        assertTrue(builder.getMatcherForArgument(1) instanceof NaturalMatcher);
    }

    @Test
    public void testAddComparableMatcher() throws Exception
    {
        final ArgumentMatcher<String> defaultMatcher = new ComparatorAdaptorMatcher<String>(Collator.getInstance(Locale.US));
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newBuilder(String.class, defaultMatcher);

        builder.addComparableMatcher(Integer.class);
        assertNotSame(defaultMatcher, builder.getMatcherForArgument(0));
        assertTrue(builder.getMatcherForArgument(0) instanceof ComparableMatcher);
        builder.addComparableMatcher(Double.class);
        assertNotSame(defaultMatcher, builder.getMatcherForArgument(0));
        assertTrue(builder.getMatcherForArgument(0) instanceof ComparableMatcher);
        assertNotSame(defaultMatcher, builder.getMatcherForArgument(1));
        assertTrue(builder.getMatcherForArgument(1) instanceof ComparableMatcher);
    }

    @Test
    public void testAddComparatorMatcher() throws Exception
    {
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newNaturalBuilder();

        builder.addComparatorMatcher(String.class, Collator.getInstance(Locale.US));
        assertNotSame(builder.getDefaultMatcher(), builder.getMatcherForArgument(0));
        assertTrue(builder.getMatcherForArgument(0) instanceof ComparatorAdaptorMatcher);
        builder.addComparatorMatcher(Double.class, new DoubleComparator());
        assertNotSame(builder.getDefaultMatcher(), builder.getMatcherForArgument(0));
        assertTrue(builder.getMatcherForArgument(0) instanceof ComparatorAdaptorMatcher);
        assertNotSame(builder.getDefaultMatcher(), builder.getMatcherForArgument(1));
        assertTrue(builder.getMatcherForArgument(1) instanceof ComparatorAdaptorMatcher);
    }

    @Test
    public void testAddNotNullMatcher() throws Exception
    {
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newNaturalBuilder();

        builder.addNotNullMatcher(String.class);
        assertNotSame(builder.getDefaultMatcher(), builder.getMatcherForArgument(0));
        assertTrue(builder.getMatcherForArgument(0) instanceof NotNullMatcher);
        builder.addNotNullMatcher(Double.class);
        assertNotSame(builder.getDefaultMatcher(), builder.getMatcherForArgument(0));
        assertTrue(builder.getMatcherForArgument(0) instanceof NotNullMatcher);
        assertNotSame(builder.getDefaultMatcher(), builder.getMatcherForArgument(1));
        assertTrue(builder.getMatcherForArgument(1) instanceof NotNullMatcher);
    }

    @Test
    public void testAddArgumentMatcher() throws Exception
    {
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newNaturalBuilder();
        final ArgumentMatcher<Integer> matcherOne = ArgumentMatchers.<Integer>alwaysMatcher();
        final ArgumentMatcher<String> matcherTwo = ArgumentMatchers.comparatorMatcher(Collator.getInstance(Locale.US));
        builder.addArgumentMatcher(Integer.class, matcherOne);
        assertSame(builder.getMatcherForArgument(0), matcherOne);
        builder.addArgumentMatcher(String.class, matcherTwo);
        assertSame(builder.getMatcherForArgument(0), matcherOne);
        assertSame(builder.getMatcherForArgument(1), matcherTwo);
    }

    @Test
    public void testComplexMatcher() throws Exception
    {
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newNaturalBuilder();
        builder.addNotNullMatcher().addNaturalMatcher().addDefaultMatcher().addComparableMatcher(String.class);
        builder.addComparatorMatcher(Double.class, new DoubleComparator());

        final ArgumentMatcher<Integer> matcherOne = ArgumentMatchers.<Integer>alwaysMatcher();
        builder.addArgumentMatcher(Integer.class, matcherOne);

        assertTrue(builder.getMatcherForArgument(0) instanceof NotNullMatcher);
        assertTrue(builder.getMatcherForArgument(1) instanceof NaturalMatcher);
        assertSame(builder.getDefaultMatcher(), builder.getMatcherForArgument(2));
        assertTrue(builder.getMatcherForArgument(3) instanceof ComparableMatcher);
        assertTrue(builder.getMatcherForArgument(4) instanceof ComparatorAdaptorMatcher);
        assertSame(matcherOne, builder.getMatcherForArgument(5));
        assertSame(builder.getDefaultMatcher(), builder.getMatcherForArgument(6));
        assertSame(builder.getDefaultMatcher(), builder.getMatcherForArgument(7));
    }

    @Test
    public void testActualArgumentsMatcherWithSingleArgument() throws Exception
    {
        final ArgumentsMatcherBuilder builder = ArgumentsMatcherBuilder.newNaturalBuilder();

        assertTrue(builder.getDefaultMatcher() instanceof NaturalMatcher);

        final ArgumentsMatcher matcher = builder.asArgumentsMatcher();
        assertTrue(callMatch(matcher, new NatrualEqualsObject(1), new NatrualEqualsObject(1)));
        assertFalse(callMatch(matcher, new NatrualEqualsObject(2), new NatrualEqualsObject(5)));
        assertFalse(callMatch(matcher, new NatrualEqualsObject(2), null));
        assertFalse(callMatch(matcher, null, new NatrualEqualsObject(5)));
        assertTrue(callMatch(matcher, null, null));

        assertEquals("(N(6))", callToString(matcher, new NatrualEqualsObject(6)));
    }

    @Test
    public void testActualArgumentsMatcherWithMultipleArguments() throws Exception
    {
        ArgumentsMatcher matcher = ArgumentsMatcherBuilder.newNaturalBuilder().asArgumentsMatcher();

        assertTrue(matcher.matches(null, null));
        assertTrue(matcher.matches(new Object[] { new NatrualEqualsObject(1), new NatrualEqualsObject(2) }, new Object[] { new NatrualEqualsObject(1), new NatrualEqualsObject(2) }));

        assertFalse(matcher.matches(new Object[] { new NatrualEqualsObject(1), new NatrualEqualsObject(2) }, new Object[] { new NatrualEqualsObject(1), new NatrualEqualsObject(-1) }));
        assertFalse(matcher.matches(new Object[] { new NatrualEqualsObject(1) }, new Object[] { new NatrualEqualsObject(1), new NatrualEqualsObject(2) }));
        assertFalse(matcher.matches(new Object[] { new NatrualEqualsObject(1) }, new Object[] { }));
        assertFalse(matcher.matches(null, new Object[] { }));
        assertFalse(matcher.matches(new Object[] { }, null));

        assertEquals("(N(1), N(2))", matcher.toString(new Object[] { new NatrualEqualsObject(1), new NatrualEqualsObject(2) }));
    }

    @Test
    public void testActualArgumentsMatcherWithMultipleComplexArguments() throws Exception
    {
        final ArgumentsMatcherBuilder matcherBuilder = ArgumentsMatcherBuilder.newNaturalBuilder();
        matcherBuilder.addAlwaysMatcher(Integer.class);
        matcherBuilder.addDefaultMatcher();
        matcherBuilder.addComparableMatcher(ComparableObject.class);
        matcherBuilder.addComparatorMatcher(ComparatorObject.class, new ComparatorObjectComparator());
        matcherBuilder.addNotNullMatcher(Number.class);
        final ArgumentsMatcher macther = matcherBuilder.asArgumentsMatcher();

        assertTrue(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f }));

        //Lets test the first argument (Always Matcher).
        assertTrue(macther.matches(
                new Object[] { null, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6 },
                new Object[] { null, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6 }));
        assertTrue(macther.matches(
                new Object[] { 12, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6 },
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6 }));
        assertTrue(macther.matches(
                new Object[] { null, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6 },
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6 }));
        assertTrue(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6 },
                new Object[] { null, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6 }));
        //Non-Integer object should not match.
        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6 },
                new Object[] { 1f, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6 }));

        //Lets test the second argument (Natural Matcher).
        assertTrue(macther.matches(
                new Object[] { 1, null, new ComparableObject(5), new ComparatorObject(6), 5.6 },
                new Object[] { 1, null, new ComparableObject(5), new ComparatorObject(6), 5.6 }));
        assertFalse(macther.matches(
                new Object[] { 1, "qq", new ComparableObject(5), new ComparatorObject(6), 5.6 },
                new Object[] { 1, null, new ComparableObject(5), new ComparatorObject(6), 5.6 }));
        assertFalse(macther.matches(
                new Object[] { 1, null, new ComparableObject(5), new ComparatorObject(6), 5.6 },
                new Object[] { 1, "rrr", new ComparableObject(5), new ComparatorObject(6), 5.6 }));
        assertFalse(macther.matches(
                new Object[] { 1, "qq", new ComparableObject(5), new ComparatorObject(6), 5.6 },
                new Object[] { 1, "rrr", new ComparableObject(5), new ComparatorObject(6), 5.6 }));
        assertFalse(macther.matches(
                new Object[] { 1, 5.6, new ComparableObject(5), new ComparatorObject(6), 5.6 },
                new Object[] { 1, "rrr", new ComparableObject(5), new ComparatorObject(6), 5.6 }));

        //Lets test the third argument (Comparable Matcher)
        assertTrue(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(67856), new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", new SubComparableObject(67856), new ComparatorObject(6), 5.6f }));
        assertTrue(macther.matches(
                new Object[] { 1, "natural", null, new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", null, new ComparatorObject(6), 5.6f }));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(2), new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", null, new ComparatorObject(6), 5.6f }));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", null, new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", new ComparableObject(2), new ComparatorObject(6), 5.6f }));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(2), new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", null, new ComparatorObject(6), 5.6f }));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(2), new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", "me", new ComparatorObject(6), 5.6f }));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(2), new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", "me", new ComparatorObject(6), 5.6f }));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", "me", new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", "me", new ComparatorObject(6), 5.6f }));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", "me", new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", null, new ComparatorObject(6), 5.6f }));


        //Lets test the forth argument (Comparator Matcher)
        assertTrue(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", new ComparableObject(5), new SubComparatorObject(6), 5.6f }));

        assertTrue(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), null, 5.6f },
                new Object[] { 1, "natural", new ComparableObject(5), null, 5.6f }));

        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", new ComparableObject(5), null, 5.6f }));

        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), null, 5.6f },
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f }));

        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", new ComparableObject(5), 6, 5.6f }));

        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), 107, 5.6f },
                new Object[] { 1, "natural", new ComparableObject(5), 107, 5.6f }));

        //Lets test the forth argument (NotNull Matcher)
        assertTrue(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), null },
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6d }));

        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), new Object() },
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 7l }));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), null }));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), null },
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), null }));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f },
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), new ArrayList<Integer>() }));

        //Lets these the default matcher (Natural Matcher)
        assertTrue(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, new NatrualEqualsObject(5)},
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, new NatrualEqualsObject(5)}));
        assertTrue(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, 5L},
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, 5L}));
        assertTrue(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, null},
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, null}));

        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, 5L},
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, 5}));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, null},
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, 5}));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, 5},
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, null}));
        assertFalse(macther.matches(
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, 5},
                new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f, new ArrayList<Integer>()}));
    }

    @Test
    public void testActualToStringComplex() throws Exception
    {
        final ArgumentsMatcherBuilder matcherBuilder = ArgumentsMatcherBuilder.newNaturalBuilder();
        matcherBuilder.addAlwaysMatcher(Integer.class);
        matcherBuilder.addDefaultMatcher();
        matcherBuilder.addComparableMatcher(ComparableObject.class);
        matcherBuilder.addComparatorMatcher(ComparatorObject.class, new ComparatorObjectComparator());
        matcherBuilder.addNotNullMatcher(Number.class);
        final ArgumentsMatcher matcher = matcherBuilder.asArgumentsMatcher();

        assertEquals("(1, natural, Comparable(5), Comparator(6), 5.6)", 
                matcher.toString(new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), 5.6f }));

        assertEquals("(1, natural, Comparable(5), Comparator(6), null)", 
                matcher.toString(new Object[] { 1, "natural", new ComparableObject(5), new ComparatorObject(6), null}));

        assertEquals("()", matcher.toString(new Object[]{}));
        assertEquals("()", matcher.toString(null));
    }

    private static boolean callMatch(ArgumentsMatcher matcher, Object expected, Object actual)
    {
        return matcher.matches(new Object[] { expected }, new Object[] { actual });
    }

    private static String callToString(ArgumentsMatcher matcher, Object obj)
    {
        return matcher.toString(new Object[] { obj });
    }

    private static class NatrualEqualsObject
    {
        private final int value;

        public NatrualEqualsObject(final int value)
        {
            this.value = value;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof NatrualEqualsObject))
            {
                return false;
            }

            final NatrualEqualsObject that = (NatrualEqualsObject) o;

            if (value != that.value)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return String.format("N(%d)", value);
        }
    }

    private static class ComparatorObjectComparator implements Comparator<ComparatorObject>
    {
        public int compare(final ComparatorObject o1, final ComparatorObject o2)
        {
            return o1.getValue() - o2.getValue();
        }
    }

    private static class DoubleComparator implements Comparator<Double>
    {
        public int compare(final Double o1, final Double o2)
        {
            return o1.compareTo(o2);
        }
    }

    private static class ComparatorObject
    {
        private final int value;

        public ComparatorObject(final int value)
        {
            this.value = value;
        }

        @Override
        public int hashCode()
        {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings ({ "EqualsWhichDoesntCheckParameterClass" })
        @Override
        public boolean equals(final Object obj)
        {
            throw new UnsupportedOperationException();
        }

        public int getValue()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return String.format("Comparator(%d)", value);
        }
    }

    private static class SubComparatorObject extends ComparatorObject
    {
        public SubComparatorObject(final int value)
        {
            super(value);
        }
    }

    private static class ComparableObject implements Comparable<ComparableObject>
    {
        private final int value;

        public ComparableObject(final int value)
        {
            this.value = value;
        }

        public int compareTo(final ComparableObject o)
        {
            return this.value - o.value;
        }

        public int getValue()
        {
            return value;
        }

        @Override
        public int hashCode()
        {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings ({ "EqualsWhichDoesntCheckParameterClass" })
        @Override
        public boolean equals(final Object obj)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString()
        {
            return String.format("Comparable(%d)", value);
        }
    }

    private static class SubComparableObject extends ComparableObject
    {
        public SubComparableObject(final int value)
        {
            super(value);
        }
    }
}
