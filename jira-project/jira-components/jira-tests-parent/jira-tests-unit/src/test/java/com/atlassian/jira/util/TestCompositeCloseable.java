package com.atlassian.jira.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestCompositeCloseable
{
    @Test
    public void testNullFirst() throws Exception
    {
        try
        {
            new CompositeCloseable(null, new Closeable()
            {
                public void close()
                {}
            });
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNullSecond() throws Exception
    {
        try
        {
            new CompositeCloseable(new Closeable()
            {
                public void close()
                {}
            }, null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testCloseCalledForBoth() throws Exception
    {
        final AtomicInteger oneCount = new AtomicInteger();
        final AtomicInteger twoCount = new AtomicInteger();

        // close order is important
        final Closeable one = new Closeable()
        {
            public void close()
            {
                assertEquals(oneCount.incrementAndGet(), twoCount.get() + 1);
            }
        };
        final Closeable two = new Closeable()
        {
            public void close()
            {
                assertEquals(twoCount.incrementAndGet(), oneCount.get());
            }
        };
        final CompositeCloseable closeable = new CompositeCloseable(one, two);

        // should work
        one.close();
        two.close();

        // should fail
        try
        {
            two.close();
            fail("AssertionFailedError expected");
        }
        catch (final AssertionError expected)
        {}
        try
        {
            one.close();
            fail("AssertionFailedError expected");
        }
        catch (final AssertionError expected)
        {}

        assertEquals(2, oneCount.get());
        assertEquals(2, twoCount.get());
        closeable.close();
        closeable.close();
        assertEquals(4, oneCount.get());
        assertEquals(4, twoCount.get());
        assertEquals(4, oneCount.get());
        assertEquals(4, twoCount.get());
        closeable.close();
        assertEquals(5, oneCount.get());
        assertEquals(5, twoCount.get());
    }
}
