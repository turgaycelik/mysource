package com.atlassian.jira;

import org.junit.Test;
import org.picocontainer.ComponentAdapter;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class TestKeyedDelegateComponentAdapter
{
    interface One
    {
        int one();
    }

    interface Two
    {
        int two();
    }

    public static class OneTwoImpl implements One, Two
    {
        int one, two;

        public int one()
        {
            return ++one;
        }

        public int two()
        {
            return ++two;
        }
    }

    @Test
    public void testAdapterWithNullImplementer() throws Exception
    {
        try
        {
            final ComponentAdapter delegate  = mock(ComponentAdapter.class);

            new KeyedDelegateComponentAdapter<OneTwoImpl>(null, delegate);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testAdapterWithNullDelegate() throws Exception
    {
        try
        {
            new KeyedDelegateComponentAdapter<OneTwoImpl>(One.class, null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testAdapterDelegatesVerify() throws Exception
    {
        final ComponentAdapter delegate  = mock(ComponentAdapter.class);
        final ComponentAdapter adapter = new KeyedDelegateComponentAdapter<OneTwoImpl>(One.class, delegate);
        verify(delegate, never()).verify(null);
        adapter.verify(null);
        verify(delegate).verify(null);
    }

    @Test
    public void testAdapterDelegatesGetComponentInstance() throws Exception
    {
        final ComponentAdapter delegate  = mock(ComponentAdapter.class);
        final ComponentAdapter adapter = new KeyedDelegateComponentAdapter<OneTwoImpl>(One.class, delegate);
        assertNull(adapter.getComponentInstance(null));
        verify(delegate).getComponentInstance(null, ComponentAdapter.NOTHING.class);
    }

}
