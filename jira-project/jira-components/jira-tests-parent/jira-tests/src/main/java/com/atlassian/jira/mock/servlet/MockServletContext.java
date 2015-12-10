package com.atlassian.jira.mock.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * A mock implementation of  {@link javax.servlet.ServletContext}
 *
 * @since v4.0
 */
public class MockServletContext implements ServletContext
{
    private final Map<String,Object> attributes = new HashMap<String,Object>();
    private final String realPath;

    public MockServletContext()
    {
        this(null);
    }

    public MockServletContext(String realPath)
    {
        this.realPath = realPath;
    }

    public ServletContext getContext(final String s)
    {
        return this;
    }

    public int getMajorVersion()
    {
        return 0;
    }

    public int getMinorVersion()
    {
        return 0;
    }

    public String getMimeType(final String s)
    {
        return null;
    }

    public Set getResourcePaths(final String s)
    {
        return null;
    }

    public URL getResource(final String s) throws MalformedURLException
    {
        return null;
    }

    public InputStream getResourceAsStream(final String s)
    {
        return null;
    }

    public RequestDispatcher getRequestDispatcher(final String s)
    {
        return null;
    }

    public RequestDispatcher getNamedDispatcher(final String s)
    {
        return null;
    }

    public Servlet getServlet(final String s) throws ServletException
    {
        return null;
    }

    public Enumeration getServlets()
    {
        return null;
    }

    public Enumeration getServletNames()
    {
        return null;
    }

    public void log(final String s)
    {
    }

    public void log(final Exception e, final String s)
    {
    }

    public void log(final String s, final Throwable throwable)
    {
    }

    public String getRealPath(final String s)
    {
        return realPath;
    }

    public String getServerInfo()
    {
        return "JIRA Mock Application Server";
    }

    public String getInitParameter(final String s)
    {
        return null;
    }

    public Enumeration getInitParameterNames()
    {
        return null;
    }

    public Object getAttribute(final String s)
    {
        return attributes.get(s);
    }

    public Enumeration getAttributeNames()
    {
        final Set<String> set = attributes.keySet();
        return new Vector<String>(set).elements();
    }

    public void setAttribute(final String s, final Object o)
    {
        attributes.put(s,o);
    }

    public void removeAttribute(final String s)
    {
        attributes.remove(s);
    }


    public String getServletContextName()
    {
        return "jira";
    }
}
