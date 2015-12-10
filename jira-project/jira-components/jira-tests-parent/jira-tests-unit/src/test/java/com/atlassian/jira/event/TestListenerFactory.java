package com.atlassian.jira.event;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.event.MockListener;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugin.MockComponentClassManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestListenerFactory
{
    @Before
    public void setUp()
    {
        new MockComponentWorker().init().addMock(ComponentClassManager.class, new MockComponentClassManager());
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testListenerFactory() throws ListenerException
    {
        MockListener listener = (MockListener) ListenerFactory.getListener("com.atlassian.jira.mock.event.MockListener", EasyMap.build("foo", "bar"));
        assertEquals("bar", listener.getParam("foo"));
    }

    @Test
    public void testInvalidListener()
    {
        boolean exceptionThrown = false;
        try
        {
            ListenerFactory.getListener("foobar", null);
        }
        catch (ListenerException e)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
}
