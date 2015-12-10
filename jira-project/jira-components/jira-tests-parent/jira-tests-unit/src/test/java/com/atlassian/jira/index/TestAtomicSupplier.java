package com.atlassian.jira.index;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TestAtomicSupplier
{
    @Test
    public void testCreate() throws Exception
    {
        final String result = "blah";
        final AtomicSupplier<String> ref = new AtomicSupplier<String>()
        {
            @Override
            protected String create()
            {
                return result;
            }
        };
        assertSame(result, ref.get());
    }

    @Test
    public void testCompareAndSetNullIfSame() throws Exception
    {
        final String result = "blah";
        final AtomicInteger count = new AtomicInteger();
        final AtomicSupplier<String> ref = new AtomicSupplier<String>()
        {
            @Override
            protected String create()
            {
                count.getAndIncrement();
                return result;
            }
        };
        assertEquals(0, count.get());
        assertSame(result, ref.get());
        assertEquals(1, count.get());
        assertSame(result, ref.get());
        assertEquals(1, count.get());
        ref.compareAndSetNull(result);
        assertSame(result, ref.get());
        assertEquals(2, count.get());
    }

    @Test
    public void testCompareAndSetFailsIfNotSame() throws Exception
    {
        final String result = "blah";
        final AtomicInteger count = new AtomicInteger();
        final AtomicSupplier<String> ref = new AtomicSupplier<String>()
        {
            @Override
            protected String create()
            {
                count.getAndIncrement();
                return result;
            }
        };
        assertEquals(0, count.get());
        assertSame(result, ref.get());
        assertEquals(1, count.get());
        assertSame(result, ref.get());
        assertEquals(1, count.get());
        ref.compareAndSetNull(new String(result));
        assertSame(result, ref.get());
        assertEquals(1, count.get());
    }
}
