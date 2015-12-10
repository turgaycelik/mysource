package com.atlassian.jira.index;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import com.atlassian.jira.index.Index.Operation;
import com.atlassian.jira.index.Index.Result;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultIndex
{
    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void testPerformThrowsForNullOp() throws Exception
    {
        final DefaultIndex index = new DefaultIndex(new MockIndexEngine());
        index.perform(null);
    }

    @Test
    public void testPerformReturnsFailureIfExceptionWriting() throws Exception
    {
        final IOException blah = new IOException("blah");
        final DefaultIndex index = new DefaultIndex(new MockIndexEngine()
        {
            @Override
            public void write(@Nonnull final Operation operation) throws IOException
            {
                throw blah;
            }
        });
        final Result result = index.perform(new MockOperation());
        assertTrue(result.isDone());
        try
        {
            result.await();
        }
        catch (final RuntimeException expected)
        {
            assertSame(blah, expected.getCause());
        }
        try
        {
            result.await(100, TimeUnit.MILLISECONDS);
        }
        catch (final RuntimeException expected)
        {
            assertSame(blah, expected.getCause());
        }
    }

    @Test
    public void testPerformReturnsFailureIfRuntimeExceptionWriting() throws Exception
    {
         final RuntimeException blah = new UnsupportedOperationException("blah");
         final DefaultIndex index = new DefaultIndex(new MockIndexEngine()
         {
             @Override
             public void write(@Nonnull final Operation operation) throws IOException
             {
                 throw blah;
             }
         });
         final Result result = index.perform(new MockOperation());
         assertTrue(result.isDone());
         try
         {
             result.await();
         }
         catch (final RuntimeException expected)
         {
             assertSame(blah, expected);
         }
         try
         {
             result.await(100, TimeUnit.MILLISECONDS);
         }
         catch (final RuntimeException expected)
         {
             assertSame(blah, expected);
         }
     }

    @Test
    public void testPerformReturnsFailureIfErrorWriting() throws Exception
    {
         final Error blah = new AssertionError("blah");
         final DefaultIndex index = new DefaultIndex(new MockIndexEngine()
         {
             @Override
             public void write(@Nonnull final Operation operation) throws IOException
             {
                 throw blah;
             }
         });
         final Result result = index.perform(new MockOperation());
         assertTrue(result.isDone());
         try
         {
             result.await();
         }
         catch (final AssertionError expected)
         {
             assertSame(blah, expected);
         }
         try
         {
             result.await(100, TimeUnit.MILLISECONDS);
         }
         catch (final AssertionError expected)
         {
             assertSame(blah, expected);
         }
     }

    @Test
    public void testPerformPassesSameOperation() throws Exception
    {
        final MockOperation mockOperation = new MockOperation();
        final DefaultIndex index = new DefaultIndex(new MockIndexEngine()
        {
            @Override
            public void write(@Nonnull final Operation operation) throws IOException
            {
                assertSame(mockOperation, operation);
            }
        });
        final Result result = index.perform(mockOperation);
        assertTrue(result.isDone());
        result.await();
        assertTrue(result.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testClose() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final DefaultIndex index = new DefaultIndex(new MockIndexEngine()
        {
            @Override
            public void close()
            {
                count.incrementAndGet();
            }
        });
        assertEquals(0, count.get());
        index.close();
        assertEquals(1, count.get());
        index.close();
        assertEquals(2, count.get());
    }

    @Test
    public void testCloseThrows() throws Exception
    {
        final UnsupportedOperationException blah = new UnsupportedOperationException();
        final DefaultIndex index = new DefaultIndex(new MockIndexEngine()
        {
            @Override
            public void close()
            {
                throw blah;
            }
        });
        try
        {
            index.close();
            fail("UnsupportedOperationException expected");
        }
        catch (final UnsupportedOperationException expected)
        {
            assertSame(blah, expected);
        }
    }
}
