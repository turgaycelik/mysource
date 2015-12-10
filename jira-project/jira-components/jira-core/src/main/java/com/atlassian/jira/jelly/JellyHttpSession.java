/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class JellyHttpSession implements HttpSession
{
    private static final AtomicLong SESSION_ID_COUNTER = new AtomicLong();
    private static final transient Logger log = Logger.getLogger(JellyHttpSession.class);

    private HashMap sessionMap;
    private final long sessionId;

    public JellyHttpSession(HashMap storage)
    {
        this.sessionMap = storage;
        this.sessionId = SESSION_ID_COUNTER.incrementAndGet();
    }

    public long getCreationTime()
    {
        return 0;
    }

    public String getId()
    {
        return "<jelly-http-session-" + sessionId + ">";
    }

    public long getLastAccessedTime()
    {
        return 0;
    }

    public ServletContext getServletContext()
    {
        return null;
    }

    public void setMaxInactiveInterval(int i)
    {
    }

    public int getMaxInactiveInterval()
    {
        return 0;
    }

    public HttpSessionContext getSessionContext()
    {
        return null;
    }

    public Object getAttribute(String key)
    {
        return sessionMap.get(key);
    }

    public Object getValue(String s)
    {
        return null;
    }

    public Enumeration getAttributeNames()
    {
        return null;
    }

    public String[] getValueNames()
    {
        return new String[0];
    }

    public void setAttribute(String key, Object value)
    {
        sessionMap.put(key, value);
    }

    public void putValue(String s, Object o)
    {
    }

    public void removeAttribute(String s)
    {
        sessionMap.remove(s);
    }

    public void removeValue(String s)
    {
    }

    public void invalidate()
    {
    }

    public boolean isNew()
    {
        return false;
    }
}
