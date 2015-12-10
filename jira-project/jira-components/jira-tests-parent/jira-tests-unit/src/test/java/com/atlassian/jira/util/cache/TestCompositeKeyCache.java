package com.atlassian.jira.util.cache;

import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Supplier;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestCompositeKeyCache
{
    @Test
    public void testSimpleGet() throws Exception
    {
        final SupplierFactory factory = new SupplierFactory();
        final CompositeKeyCache<Integer, Integer, String> cache = CompositeKeyCache.createWeakFirstKeySoftValueCache(null);

        assertEquals("1:1:1", cache.get(1, 1, factory.get("1:1")));
        assertEquals("1:1:1", cache.get(1, 1, factory.get("1:1")));
        assertEquals("1:2:2", cache.get(1, 2, factory.get("1:2")));
        assertEquals("2:2:3", cache.get(2, 2, factory.get("2:2")));
        assertEquals("1:2:2", cache.get(1, 2, factory.get("1:2")));
        assertEquals("1:1:1", cache.get(1, 1, factory.get("1:1")));
        assertEquals("2:2:3", cache.get(2, 2, factory.get("2:2")));
    }

    private static class SupplierFactory implements Function<String, Supplier<String>>
    {
        final AtomicInteger count = new AtomicInteger();

        public Supplier<String> get(final String input)
        {
            return new Supplier<String>()
            {
                public String get()
                {
                    return input + ":" + count.incrementAndGet();
                };
            };
        }
    }
}
