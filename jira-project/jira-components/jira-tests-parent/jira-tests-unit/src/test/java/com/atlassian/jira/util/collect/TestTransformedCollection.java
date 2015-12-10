package com.atlassian.jira.util.collect;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.jira.util.Function;

import org.junit.Test;

import static com.atlassian.jira.util.collect.Assert.assertUnsupportedOperation;
import static com.atlassian.jira.util.collect.Transformed.collection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestTransformedCollection
{
    @Test
    public void testIterator() throws Exception
    {
        final Collection<Long> original = CollectionBuilder.<Long> newBuilder().add(1L).add(2L).add(3L).asList();
        final AtomicInteger count = new AtomicInteger();
        final Collection<String> transformed = collection(original, new Function<Long, String>()
        {
            public String get(final Long input)
            {
                count.getAndIncrement();
                return input.toString();
            }
        });
        assertEquals(0, count.get());
        final Iterator<String> it = transformed.iterator();
        assertEquals("1", it.next());
        assertEquals(1, count.get());
        assertEquals("2", it.next());
        assertEquals(2, count.get());
        assertEquals("3", it.next());
        assertEquals(3, count.get());
        assertFalse(it.hasNext());
    }

    @Test
    public void testNotEquals() throws Exception
    {
        final Collection<Long> original = CollectionBuilder.<Long> newBuilder().add(1L).add(2L).add(3L).asList();
        assertFalse("Collections are only equal if reference is same", collection(original, new Function<Long, String>()
        {
            public String get(final Long input)
            {
                return input.toString();
            }
        }).equals(original));

        assertFalse("Collections are only equal if reference is same", collection(original, new Function<Long, Long>()
        {
            public Long get(final Long input)
            {
                return new Long(input.longValue());
            }
        }).equals(original));
    }

    @Test
    public void testAddNotSupported() throws Exception
    {
        Assert.assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                collection(CollectionBuilder.<Long> newBuilder().add(1L).asArrayList(), new ReferenceFunction<Long>()).add(2L);
            }
        });
    }

    @Test
    public void testAddAllNotSupported() throws Exception
    {
        assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                final List<Long> original = CollectionBuilder.<Long> newBuilder().add(1L).asArrayList();
                collection(original, new ReferenceFunction<Long>()).addAll(original);
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
                collection(CollectionBuilder.<Long> newBuilder().add(1L).asArrayList(), new ReferenceFunction<Long>()).remove(1L);
            }
        });
    }

    @Test
    public void testRemoveAllNotSupported() throws Exception
    {
        assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                final List<Long> original = CollectionBuilder.<Long> newBuilder().add(1L).asArrayList();
                collection(original, new ReferenceFunction<Long>()).removeAll(original);
            }
        });
    }

    @Test
    public void testRetainAllNotSupported() throws Exception
    {
        assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                final List<Long> original = CollectionBuilder.<Long> newBuilder().add(1L).asArrayList();
                collection(original, new ReferenceFunction<Long>()).removeAll(original);
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
                collection(CollectionBuilder.<Long> newBuilder().add(1L).asArrayList(), new ReferenceFunction<Long>()).clear();
            }
        });
    }

    @Test
    public void testIteratorRemovalNotSupported() throws Exception
    {
        assertUnsupportedOperation(new Runnable()
        {
            public void run()
            {
                final Collection<Long> collection = collection(CollectionBuilder.<Long> newBuilder().add(1L).asArrayList(),
                    new ReferenceFunction<Long>());
                final Iterator<Long> iterator = collection.iterator();
                assertEquals(new Long(1L), iterator.next());
                iterator.remove();
            }
        });
    }
}
