/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.ComponentClassManager;

import java.util.Map;

/**
 * The base factory used to create Listeners.
 */
public class ListenerFactory
{
    /**
     * Create an implementation of a Listener.
     *
     * @param className The fully qualified class name of this Listener
     * @param params Init parameters for the listener
     * @return An implementation of the Listener
     * @throws ListenerException Thrown if any problems occur instantiating the Listener
     */
    public static JiraListener getListener(final String className, final Map<String, String> params)
            throws ListenerException
    {
        try
        {
            final ComponentClassManager componentClassManager = ComponentAccessor.getComponentClassManager();
            JiraListener listener = componentClassManager.newInstance(className);

            listener.init(params);
            return listener;
        }
        catch (ClassNotFoundException e)
        {
            throw new ListenerException("Could not find class: " + className, e);
        }
        catch (Exception e)
        {
            throw new ListenerException("Could not create listener for class: " + className, e);
        }
    }
}
