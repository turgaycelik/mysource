package com.atlassian.jira.util;

import com.atlassian.core.test.util.DuckTypeProxy;

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestDuckTypeProxy
{
    public interface MyInterface
    {
        String getString();
        Long getLong();
        Integer getInteger();
        String get(String string);
        String get(Integer integer);
    }

    @Test
    public void testProxyReturns() throws Exception
    {
        Object obj = new Object()
        {
            public String getString()
            {
                return "who's you're daddy?";
            }
        };
        MyInterface impl = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, Lists.newArrayList(obj), DuckTypeProxy.THROW);
        assertEquals("who's you're daddy?", impl.getString());
    }

    @Test
    public void testProxyThrows() throws Exception
    {
        MyInterface impl = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, Lists.newArrayList(new Object()), DuckTypeProxy.THROW);
        try
        {
            impl.getString();
            fail("should have thrown USOE");
        }
        catch (UnsupportedOperationException yay)
        {
        }
    }

    @Test
    public void testProxyDelegatesToSecond() throws Exception
    {
        Object obj = new Object()
        {
            public String getString()
            {
                return "who's you're daddy?";
            }
        };
        MyInterface impl = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, Lists.newArrayList(new Object(), obj), DuckTypeProxy.THROW);
        assertEquals("who's you're daddy?", impl.getString());
    }

    @Test
    public void testNotNullParameter() throws Exception
    {
        Object obj = new Object()
        {
            public String get(String string)
            {
                return "how about: " + string;
            }
        };
        MyInterface impl = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, Lists.newArrayList(obj), DuckTypeProxy.THROW);
        assertEquals("how about: me", impl.get("me"));
    }

    @Test
    public void testNullParameter() throws Exception
    {
        Object obj = new Object()
        {
            public String get(String string)
            {
                return "how about: " + string;
            }
        };
        MyInterface impl = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, Lists.newArrayList(obj), DuckTypeProxy.THROW);
        assertEquals("how about: null", impl.get((String) null));
    }
}
