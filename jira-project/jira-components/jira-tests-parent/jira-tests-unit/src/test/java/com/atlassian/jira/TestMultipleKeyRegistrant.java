package com.atlassian.jira;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 *
 * @since v6.2
 */
public class TestMultipleKeyRegistrant
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
    public void testBuilderRegistersOne() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();

        final MultipleKeyRegistrant<OneTwoImpl> builder = MultipleKeyRegistrant.registrantFor(OneTwoImpl.class);
        builder.implementing(One.class);
        builder.registerWith(ComponentContainer.Scope.INTERNAL, container);
        assertNotNull(container.getComponentInstance(One.class));
    }

    @Test
    public void testBuilderRegistersOneAndTwo() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();

        MultipleKeyRegistrant.registrantFor(OneTwoImpl.class).implementing(One.class).implementing(Two.class).registerWith(ComponentContainer.Scope.INTERNAL,
                container);
        final One one = container.getComponentInstance(One.class);
        assertNotNull(one);
        final Two two = container.getComponentInstance(Two.class);
        assertNotNull(two);
        assertSame(one, two);
    }

    @Test
    public void testBuilderWithNullClass() throws Exception
    {
        try
        {
            MultipleKeyRegistrant.registrantFor(null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testBuilderWithNoInterfaces() throws Exception
    {
        try
        {
            MultipleKeyRegistrant.registrantFor(OneTwoImpl.class).registerWith(ComponentContainer.Scope.INTERNAL, new ComponentContainer());
            fail("IllegalStateException expected");
        }
        catch (final IllegalStateException expected)
        {}
    }

}
