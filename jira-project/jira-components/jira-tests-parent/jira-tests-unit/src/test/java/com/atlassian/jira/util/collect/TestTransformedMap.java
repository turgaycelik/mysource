package com.atlassian.jira.util.collect;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.jira.util.Function;

import org.junit.Test;

import static com.atlassian.jira.util.collect.Assert.assertUnsupportedOperation;
import static com.atlassian.jira.util.collect.Transformed.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestTransformedMap
{
    @Test
    public void testIterator() throws Exception
    {
        final Map<Long, Long> original = new CollectionMap<Long>(CollectionBuilder.<Long> newBuilder().add(1L).add(2L).add(3L).asList());
        final AtomicInteger count = new AtomicInteger();
        final Map<Long, String> transformed = map(original, new Function<Long, String>()
        {
            public String get(final Long input)
            {
                count.getAndIncrement();
                return input.toString();
            }
        });
        assertEquals(0, count.get());
        final Iterator<String> it = transformed.values().iterator();
        assertEquals("1", it.next());
        assertEquals(1, count.get());
        assertEquals("2", it.next());
        assertEquals(2, count.get());
        assertEquals("3", it.next());
        assertEquals(3, count.get());
        assertFalse(it.hasNext());
    }

    @Test
    public void testContainsValue() throws Exception
    {
        final Map<Long, Long> original = new CollectionMap<Long>(CollectionBuilder.<Long> newBuilder().add(1L).add(2L).add(3L).asList());
        final AtomicInteger count = new AtomicInteger();
        final Map<Long, String> transformed = map(original, new Function<Long, String>()
        {
            public String get(final Long input)
            {
                count.getAndIncrement();
                return input.toString();
            }
        });
        assertTrue(transformed.containsValue("1"));
        assertTrue(transformed.containsValue("2"));
        assertTrue(transformed.containsValue("3"));
        assertFalse(transformed.containsValue("4"));
    }

    @Test
    public void testNotEquals() throws Exception
    {
        final Map<Long, Long> original = new CollectionMap<Long>(CollectionBuilder.<Long> newBuilder().add(1L).add(2L).add(3L).asList());
        assertFalse("Maps are not equal if values are different", map(original, new Function<Long, String>()
        {
            public String get(final Long input)
            {
                return input.toString();
            }
        }).equals(original));
    }

    @Test
    public void testEquals() throws Exception
    {
        final Map<Long, Long> original = new CollectionMap<Long>(CollectionBuilder.<Long> newBuilder().add(1L).add(2L).add(3L).asList());
        assertTrue("Maps are equal if reference is same", map(original, new Function<Long, Long>()
        {
            public Long get(final Long input)
            {
                return new Long(input.longValue());
            }
        }).equals(original));
    }

    @Test
    public void testPutNotSupported() throws Exception
    {
        Assert.assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                map(new CollectionMap<Long>(CollectionBuilder.<Long> newBuilder().add(1L).asArrayList()), new ReferenceFunction<Long>()).put(2L, 2L);
            }
        });
    }

    @Test
    public void testPutAllNotSupported() throws Exception
    {
        assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                final CollectionMap<Long> original = new CollectionMap<Long>(CollectionBuilder.<Long> newBuilder().add(1L).asArrayList());
                map(original, new ReferenceFunction<Long>()).putAll(original);
            }
        });
    }

    @Test
    public void testRemoveNotSupported() throws Exception
    {
        assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                map(new CollectionMap<Long>(CollectionBuilder.<Long> newBuilder().add(1L).asArrayList()), new ReferenceFunction<Long>()).remove(1L);
            }
        });
    }

    @Test
    public void testClearNotSupported() throws Exception
    {
        assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                map(new CollectionMap<Long>(CollectionBuilder.<Long> newBuilder().add(1L).asArrayList()), new ReferenceFunction<Long>()).clear();
            }
        });
    }

    @Test
    public void testEntrySetIteratorRemovalNotSupported() throws Exception
    {
        assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                final Map<Long, Long> map = map(new CollectionMap<Long>(CollectionBuilder.<Long> newBuilder().add(1L).asArrayList()),
                    new ReferenceFunction<Long>());
                final Iterator<Entry<Long, Long>> iterator = map.entrySet().iterator();
                assertEquals(new CollectionMap.SimpleEntry<Long>(1L), iterator.next());
                iterator.remove();
            }
        });
    }

    @Test
    public void testKeySetIteratorRemovalNotSupported() throws Exception
    {
        assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                final Map<Long, Long> map = map(new CollectionMap<Long>(CollectionBuilder.<Long> newBuilder().add(1L).asArrayList()),
                    new ReferenceFunction<Long>());
                final Iterator<Long> iterator = map.keySet().iterator();
                assertEquals(new Long(1L), iterator.next());
                iterator.remove();
            }
        });
    }

    @Test
    public void testValuesIteratorRemovalNotSupported() throws Exception
    {
        assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                final Map<Long, Long> map = map(new CollectionMap<Long>(CollectionBuilder.<Long> newBuilder().add(1L).asArrayList()),
                    new ReferenceFunction<Long>());
                final Iterator<Long> iterator = map.values().iterator();
                assertEquals(new Long(1L), iterator.next());
                iterator.remove();
            }
        });
    }
}
