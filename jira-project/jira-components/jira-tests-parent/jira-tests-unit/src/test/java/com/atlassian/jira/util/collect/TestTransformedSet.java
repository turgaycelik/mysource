package com.atlassian.jira.util.collect;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.jira.util.Function;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestTransformedSet
{
    @Test
    public void testIterator() throws Exception
    {
        final Set<Long> original = CollectionBuilder.<Long> newBuilder().add(1L).add(2L).add(3L).asSortedSet();
        final AtomicInteger count = new AtomicInteger();
        final Set<String> transformed = new TransformingSet<Long, String>(original, new Function<Long, String>()
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
}
