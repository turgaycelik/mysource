/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event;

import com.atlassian.jira.JiraException;

/**
 * The exception that should be thrown for all errors within Listeners.
 *
 * ///CLOVER:OFF
 */
public class ListenerException extends JiraException
{
    public ListenerException()
    {
    }

    public ListenerException(Exception e)
    {
        super(e);
    }

    public ListenerException(String msg)
    {
        super(msg);
    }

    public ListenerException(String msg, Exception e)
    {
        super(msg, e);
    }
}
