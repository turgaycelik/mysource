package com.atlassian.jira.util.collect;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Predicate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.util.collect.CollectionUtil}.
 *
 * @since v4.0
 */
@SuppressWarnings( { "AssertEqualsBetweenInconvertibleTypes" })
public class TestCollectionUtil
{
    @Test
    public void testToSet() throws Exception
    {
        final List<String> list = CollectionBuilder.list("A", "B", "A");
        final Set<String> expectedSet = new HashSet<String>();
        expectedSet.add("A");
        expectedSet.add("B");

        assertEquals(expectedSet, CollectionUtil.toSet(list));
    }

    @Test
    public void testTransformSet() throws Exception
    {
        final List<String> list = CollectionBuilder.list("Ant", "Batch", "Catch", "Dog");
        final Set<Integer> expectedSet = new HashSet<Integer>();
        expectedSet.add(3);
        expectedSet.add(5);

        assertEquals(expectedSet, CollectionUtil.transformSet(list, new Function<String, Integer>()
        {
            public Integer get(final String input)
            {
                return input.length();
            }
        }));
    }

    @Test
    public void testCopyAsImmutableList() throws Exception
    {
        try
        {
            CollectionUtil.copyAsImmutableList(null);
            fail("Should not work on null list.");
        }
        catch (final IllegalArgumentException e)
        {}

        final CollectionBuilder<String> builder = CollectionBuilder.newBuilder("test", "me");
        final List<String> input = builder.asList();
        final List<String> copyMe = builder.asMutableList();
        final List<Object> output = CollectionUtil.<Object> copyAsImmutableList(copyMe);
        assertEquals(input, output);

        try
        {
            output.add(new Object());
            fail("The returned collection appears to be mutable.");
        }
        catch (final UnsupportedOperationException expected)
        {

        }
        copyMe.add("other");
        assertEquals(input, output);

        assertEquals(Collections.emptyList(), CollectionUtil.copyAsImmutableList(Collections.emptyList()));
    }

    @Test
    public void testCopyAsImmutableSet()
    {
        try
        {
            CollectionUtil.copyAsImmutableSet(null);
            fail("Should not work on null list.");
        }
        catch (final IllegalArgumentException e)
        {}

        final CollectionBuilder<String> builder = CollectionBuilder.newBuilder("test", "me");
        final Set<String> input = builder.asSet();
        final Set<String> copyMe = builder.asMutableSet();
        final Set<Object> output = CollectionUtil.<Object> copyAsImmutableSet(copyMe);
        assertEquals(input, output);

        try
        {
            output.add(new Object());
            fail("The returned collection appears to be mutable.");
        }
        catch (final UnsupportedOperationException expected)
        {

        }
        copyMe.add("other");
        assertEquals(input, output);

        assertEquals(Collections.emptySet(), CollectionUtil.copyAsImmutableSet(Collections.emptySet()));
    }
    
    @Test
    public void testCopyAsImmutableMap()
    {
        try
        {
            CollectionUtil.copyAsImmutableMap(null);
            fail("Should not work on null list.");
        }
        catch (final IllegalArgumentException e)
        {}

        final MapBuilder<String, Integer> builder = MapBuilder.newBuilder("a", 1);
        final Map<String, Integer> input = builder.toMap();
        final Map<String, Integer> copyMe = builder.toHashMap();
        final Map<String, Integer> output = CollectionUtil.copyAsImmutableMap(copyMe);
        assertEquals(input, output);

        try
        {
            output.put("cv", 78);
            fail("The returned collection appears to be mutable.");
        }
        catch (final UnsupportedOperationException expected)
        {

        }
        copyMe.put("other", 47844);
        assertEquals(input, output);
        assertEquals(Collections.emptyMap(), CollectionUtil.copyAsImmutableMap(Collections.emptyMap()));
    }

    @Test
    public void testFirstReturnsFirstElement()
    {
        assertEquals(Integer.valueOf(1), CollectionUtil.first(Arrays.asList(1, 2, 3)));
        assertEquals(Integer.valueOf(2), CollectionUtil.first(Arrays.asList(2, 3, 1)));
        assertEquals(Integer.valueOf(3), CollectionUtil.first(Arrays.asList(3, 2, 1)));
    }

    @Test
    public void testFirstReturnsNullIfEmpty()
    {
        assertEquals(null, CollectionUtil.first(Arrays.asList()));
    }

    @Test
    public void testFilterByType()
    {
        final List<Number> source = CollectionBuilder.<Number> list(1, 1L, 2, 2L, 3, 3L);
        final Collection<Integer> result = CollectionUtil.filterByType(source, Integer.class);
        assertEquals(3, result.size());
        final Iterator<Integer> it = result.iterator();
        assertEquals(new Integer(1), it.next());
        assertEquals(new Integer(2), it.next());
        assertEquals(new Integer(3), it.next());
    }

    @Test
    public void testIndexOf()
    {
        final Predicate<Integer> is42 = new Predicate<Integer>()
        {
            public boolean evaluate(final Integer input)
            {
                return input == 42;
            }
        };

        assertEquals(-1, CollectionUtil.indexOf(Collections.<Integer>emptyList(), is42));
        assertEquals(-1, CollectionUtil.indexOf(Arrays.asList(1, 2, 3), is42));
        assertEquals(0, CollectionUtil.indexOf(Arrays.asList(42, 2, 3), is42));
        assertEquals(2, CollectionUtil.indexOf(Arrays.asList(1, 2, 42), is42));
        assertEquals(1, CollectionUtil.indexOf(Arrays.asList(1, 42, 42), is42));

        try
        {
            CollectionUtil.indexOf(null, is42);
            fail();
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            CollectionUtil.indexOf(Arrays.asList(1, 2, 3), null);
            fail();
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            CollectionUtil.indexOf(null, null);
            fail();
        }
        catch (IllegalArgumentException expected) {}
    }

}
