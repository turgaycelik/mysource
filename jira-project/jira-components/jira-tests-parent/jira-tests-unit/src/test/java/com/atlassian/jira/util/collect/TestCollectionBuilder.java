package com.atlassian.jira.util.collect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A simple test for {@link com.atlassian.jira.util.collect.CollectionBuilder}.
 *
 * @since v4.0
 */
public class TestCollectionBuilder
{
    @Test
    public void testNewBuilder()
    {
        final CollectionBuilder<Object> emptyBuilder = CollectionBuilder.newBuilder();
        assertNotNull(emptyBuilder);
        assertTrue(emptyBuilder.asCollection().isEmpty());

        final CollectionBuilder<Integer> initialisedBuilder = CollectionBuilder.newBuilder(1, 2, 3, 4);
        assertNotNull(initialisedBuilder);
        assertCollectionsEqual(asList(1, 2, 3, 4), initialisedBuilder.asCollection());
    }

    @Test
    public void testAdd()
    {
        final CollectionBuilder<Float> builder = CollectionBuilder.newBuilder();
        builder.add(5.6f);
        assertCollectionsEqual(asList(5.6f), builder.asCollection());
        builder.add(6.7f);
        assertCollectionsEqual(asList(5.6f, 6.7f), builder.asCollection());
    }

    @Test
    public void testAddTakesNull()
    {
        final CollectionBuilder<Float> builder = CollectionBuilder.newBuilder();
        builder.add(5.6f);
        assertCollectionsEqual(asList(5.6f), builder.asCollection());
        builder.add(null);
        assertCollectionsEqual(asList(5.6f, null), builder.asCollection());
        assertCollectionsEqual(asList(5.6f, null), builder.asList());
        assertCollectionsEqual(asList(5.6f, null), builder.asSet());
    }

    @Test
    public void testAddAllVarArgs()
    {
        final CollectionBuilder<Number> builder = CollectionBuilder.newBuilder();
        builder.addAll(4.5, 6.7);
        assertCollectionsEqual(TestCollectionBuilder.<Number> asList(4.5, 6.7), builder.asCollection());
        builder.addAll(Math.PI);
        assertCollectionsEqual(TestCollectionBuilder.<Number> asList(4.5, 6.7, Math.PI), builder.asCollection());
        builder.addAll();
        assertCollectionsEqual(TestCollectionBuilder.<Number> asList(4.5, 6.7, Math.PI), builder.asCollection());
    }

    @Test
    public void testAddAllCollection()
    {
        final CollectionBuilder<Number> builder = CollectionBuilder.newBuilder();
        builder.addAll(asList(4.5, 6.7));
        assertCollectionsEqual(TestCollectionBuilder.<Number> asList(4.5, 6.7), builder.asCollection());
        builder.addAll(asList(Math.E));
        assertCollectionsEqual(TestCollectionBuilder.<Number> asList(4.5, 6.7, Math.E), builder.asCollection());
        builder.addAll(Collections.<Number> emptyList());
        assertCollectionsEqual(TestCollectionBuilder.<Number> asList(4.5, 6.7, Math.E), builder.asCollection());
    }

    @Test
    public void testAsCollection()
    {
        final CollectionBuilder<Number> builder = CollectionBuilder.<Number> newBuilder(5.6, 4.5, 6.7f);
        final Collection<Number> actualCollection = builder.asCollection();
        assertCollectionsEqual(TestCollectionBuilder.<Number> asList(5.6, 4.5, 6.7f), actualCollection);
        assertImmutable(actualCollection);
    }

    @Test
    public void testAsMutableCollection()
    {
        final CollectionBuilder<Number> builder = CollectionBuilder.<Number> newBuilder(5.6, 4.5, 6.7f);
        final Collection<Number> actualCollection = builder.asMutableCollection();
        assertCollectionsEqual(TestCollectionBuilder.<Number> asList(5.6, 4.5, 6.7f), actualCollection);
        assertMutable(actualCollection);
    }

    @Test
    public void testAsArrayList() throws Exception
    {
        final CollectionBuilder<Number> builder = CollectionBuilder.<Number> newBuilder(2, 5l);
        final List<Number> numberList = builder.asArrayList();
        assertTrue(numberList instanceof ArrayList);
        assertEquals(TestCollectionBuilder.<Number> asList(2, 5L), numberList);
    }

    @Test
    public void testAsLinkedList() throws Exception
    {
        final CollectionBuilder<Number> builder = CollectionBuilder.<Number> newBuilder(2, 5l);
        final List<Number> numberList = builder.asLinkedList();
        assertTrue(numberList instanceof LinkedList);
        assertEquals(TestCollectionBuilder.<Number> asList(2, 5L), numberList);
    }

    @Test
    public void testAsList() throws Exception
    {
        final CollectionBuilder<Number> builder = CollectionBuilder.<Number> newBuilder(2, 5l);
        final List<Number> numberList = builder.asList();
        assertEquals(TestCollectionBuilder.<Number> asList(2, 5L), numberList);
        assertImmutable(numberList);
    }

    @Test
    public void testAsMutableList() throws Exception
    {
        final CollectionBuilder<Number> builder = CollectionBuilder.<Number> newBuilder(2, 5l);
        final List<Number> numberSet = builder.asMutableList();
        assertEquals(TestCollectionBuilder.<Number> asList(2, 5L), numberSet);
        assertMutable(numberSet);
    }

    @Test
    public void testAsHashSet() throws Exception
    {
        final CollectionBuilder<Number> builder = CollectionBuilder.<Number> newBuilder(5l, 6.5f, 3.4d);
        final Set<Number> numberSet = builder.asHashSet();
        assertEquals(TestCollectionBuilder.<Number> asSet(5L, 6.5f, 3.4d), numberSet);
        assertTrue(numberSet instanceof HashSet);
    }

    @Test
    public void testAsTreeSet() throws Exception
    {
        final CollectionBuilder<Integer> builder = CollectionBuilder.newBuilder(5, 6, 3);
        final Set<Integer> numberSet = builder.asTreeSet();
        assertEquals(TestCollectionBuilder.asSet(5, 6, 3), numberSet);
        assertTrue(numberSet instanceof TreeSet);
    }

    @Test
    public void testAsSet() throws Exception
    {
        final CollectionBuilder<Number> builder = CollectionBuilder.<Number> newBuilder(5l, 6.5f, 3.4d);
        final Set<Number> numberSet = builder.asSet();
        assertEquals(TestCollectionBuilder.<Number> asSet(5L, 6.5f, 3.4d), numberSet);
        assertImmutable(numberSet);
    }

    @Test
    public void testAsMutableSet() throws Exception
    {
        final CollectionBuilder<Number> builder = CollectionBuilder.<Number> newBuilder(5l, 6.5f, 3.4d);
        final Set<Number> numberSet = builder.asMutableSet();
        assertEquals(TestCollectionBuilder.<Number> asSet(5L, 6.5f, 3.4d), numberSet);
        assertMutable(numberSet);
    }

    @Test
    public void testAsSortedSet() throws Exception
    {
        final CollectionBuilder<Integer> builder = CollectionBuilder.newBuilder(5, 6, 3);
        final Set<Integer> numberSet = builder.asSortedSet();
        assertEquals(TestCollectionBuilder.asSet(5, 6, 3), numberSet);
        assertImmutable(numberSet);
    }

    @Test
    public void testAsMutableSortedSet() throws Exception
    {
        final CollectionBuilder<Integer> builder = CollectionBuilder.newBuilder(5, 6, 3);
        final Set<Integer> numberSet = builder.asMutableSortedSet();
        assertEquals(TestCollectionBuilder.asSet(5, 6, 3), numberSet);
        assertMutable(numberSet);
    }

    @Test
    public void testAsListOrderedSet() throws Exception
    {
        final CollectionBuilder<Integer> builder = CollectionBuilder.newBuilder(5, 6, 3, Integer.MAX_VALUE, Integer.MIN_VALUE);
        final Set<Integer> numberSet = builder.asListOrderedSet();
        assertOrder(asList(5, 6, 3, Integer.MAX_VALUE, Integer.MIN_VALUE), numberSet);
        assertMutable(numberSet);
    }

    @Test
    public void testAsImmuatbleListOrderedSet() throws Exception
    {
        final CollectionBuilder<Integer> builder = CollectionBuilder.newBuilder(5, 6, 3, Integer.MAX_VALUE, Integer.MIN_VALUE);
        final Set<Integer> numberSet = builder.asImmutableListOrderedSet();
        assertOrder(asList(5, 6, 3, Integer.MAX_VALUE, Integer.MIN_VALUE), numberSet);
        assertImmutable(numberSet);
    }

    @Test
    public void testAsEnclosedIterable() throws Exception
    {
        final CollectionBuilder<Double> builder = CollectionBuilder.newBuilder(Math.PI);
        assertNotNull(builder.asEnclosedIterable());
    }

    private void assertOrder(final List<?> expectedOrder, final Set<?> actualOrder)
    {
        final String message = String.format("%s is not in the same order as %s.", expectedOrder, actualOrder);
        assertEquals(message, expectedOrder.size(), actualOrder.size());

        final Iterator<?> actualIterator = actualOrder.iterator();
        for (final Object expected : expectedOrder)
        {
            assertEquals(message, expected, actualIterator.next());
        }
    }

    private static <T> void assertCollectionsEqual(final Collection<T> expectedElements, final Collection<T> actualEments)
    {
        final String message = String.format("%s != %s", expectedElements, actualEments);
        assertTrue(message, actualEments.containsAll(expectedElements));
        assertEquals(message, expectedElements.size(), actualEments.size());
    }

    private static void assertImmutable(final Collection<?> collection)
    {
        final Iterator<?> iterator = collection.iterator();
        iterator.next();
        try
        {
            iterator.remove();
            fail(String.format("Collection of type %s is mutable.", collection.getClass()));
        }
        catch (final UnsupportedOperationException expected)
        {
            //expected.
        }
    }

    private static void assertMutable(final Collection<?> collection)
    {
        final Iterator<?> iterator = collection.iterator();
        iterator.next();
        try
        {
            iterator.remove();
        }
        catch (final UnsupportedOperationException e)
        {
            fail(String.format("Collection of type %s is not mutable.", collection.getClass()));
        }
    }

    public static <T> List<T> asList(final T... elements)
    {
        final List<T> arrayList = new ArrayList<T>();
        arrayList.addAll(Arrays.asList(elements));
        return arrayList;
    }

    public static <T> Set<T> asSet(final T... elements)
    {
        final Set<T> set = new HashSet<T>();
        set.addAll(Arrays.asList(elements));
        return set;
    }
}
