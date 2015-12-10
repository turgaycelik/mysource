package com.atlassian.jira.util.collect;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.junit.Test;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestCopyOnWriteSortedCache
{

    @Test
    public void testNullCtor()
    {
        try
        {
            new CopyOnWriteSortedCache<String>(null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testEmptyCtorList()
    {
        assertEquals(0, new CopyOnWriteSortedCache<String>(Collections.<String> emptyList()).asList().size());
    }

    @Test
    public void testEmptyCtorSet()
    {
        assertEquals(0, new CopyOnWriteSortedCache<String>(Collections.<String> emptyList()).asSortedSet().size());
    }

    @Test
    public void testEmptyCtorIterable()
    {
        assertFalse(new CopyOnWriteSortedCache<String>(Collections.<String> emptyList()).iterator().hasNext());
    }

    @Test
    public void testSingleElementCtorList()
    {
        assertEquals(1, new CopyOnWriteSortedCache<String>(singleton("test")).asList().size());
    }

    @Test
    public void testSingleElementCtorSet()
    {
        assertEquals(1, new CopyOnWriteSortedCache<String>(singleton("test")).asList().size());
    }

    @Test
    public void testSingleElementCtorIterable()
    {
        assertTrue(new CopyOnWriteSortedCache<String>(singleton("test")).iterator().hasNext());
    }

    @Test
    public void testAdd()
    {
        final CopyOnWriteSortedCache<String> cache = new CopyOnWriteSortedCache<String>(singleton("test"));
        cache.add("two");
        final SortedSet<String> set = cache.asSortedSet();
        assertEquals(2, set.size());
        assertTrue(set.contains("two"));
        assertTrue(set.contains("test"));
    }

    @Test
    public void testAddAsListOrdered()
    {
        final CopyOnWriteSortedCache<String> cache = new CopyOnWriteSortedCache<String>(singleton("test"));
        cache.add("two");
        cache.add("one");
        final List<String> list = cache.asList();
        assertEquals(3, list.size());

        assertEquals("one", list.get(0));
        assertEquals("test", list.get(1));
        assertEquals("two", list.get(2));
    }

    @Test
    public void testAddIterableOrdered()
    {
        final CopyOnWriteSortedCache<String> cache = new CopyOnWriteSortedCache<String>(singleton("test"));
        cache.add("two");
        cache.add("one");
        final Iterator<String> it = cache.iterator();

        assertEquals("one", it.next());
        assertEquals("test", it.next());
        assertEquals("two", it.next());
        assertFalse(it.hasNext());
    }


    @Test
    public void testAddStableIterable()
    {
        final CopyOnWriteSortedCache<String> cache = new CopyOnWriteSortedCache<String>(singleton("test"));
        cache.add("two");
        cache.add("one");
        final Iterator<String> it = cache.iterator();

        cache.add("seven");
        assertEquals("one", it.next());
        assertEquals("test", it.next());
        assertEquals("two", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testAddNull()
    {
        try
        {
            final CopyOnWriteSortedCache<String> cache = new CopyOnWriteSortedCache<String>(singleton("test"));
            cache.add(null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testRemove()
    {
        final CopyOnWriteSortedCache<String> cache = new CopyOnWriteSortedCache<String>(Arrays.asList("one", "two", "three"));
        cache.remove("two");
        final SortedSet<String> set = cache.asSortedSet();
        assertEquals(2, set.size());
        assertTrue(set.contains("one"));
        assertTrue(set.contains("three"));
    }

    @Test
    public void testRemoveNull()
    {
        try
        {
            final CopyOnWriteSortedCache<String> cache = new CopyOnWriteSortedCache<String>(Arrays.asList("one", "two", "three"));
            cache.remove(null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }
}
