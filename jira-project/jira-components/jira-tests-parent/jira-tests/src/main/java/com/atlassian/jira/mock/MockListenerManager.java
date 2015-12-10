/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock;

import com.atlassian.jira.event.JiraListener;
import com.atlassian.jira.event.ListenerManager;

import java.util.HashMap;
import java.util.Map;

public class MockListenerManager implements ListenerManager
{
    private final Map<String, JiraListener> listeners;

    public MockListenerManager()
    {
        this.listeners = new HashMap<String, JiraListener>();
    }

    public Map<String, JiraListener> getListeners()
    {
        return listeners;
    }

    @Override
    public JiraListener createListener(String name, Class<? extends JiraListener> clazz)
    {
        try
        {
            final JiraListener listener = clazz.newInstance();
            addListener(name, listener);
            return listener;
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteListener(Class<? extends JiraListener> clazz)
    {
        for (Map.Entry<String, JiraListener> entry : listeners.entrySet())
        {
            if (entry.getValue().getClass().equals(clazz))
                listeners.remove(entry.getKey());
        }
    }

    public void addListener(String name, JiraListener listener)
    {
        listeners.put(name, listener);
    }

    public void refresh()
    {
        listeners.clear();
    }

    @Override
    public void onRefreshListeners()
    {
        listeners.clear();
    }
}
