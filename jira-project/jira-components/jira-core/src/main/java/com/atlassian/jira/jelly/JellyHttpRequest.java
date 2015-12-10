/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.apache.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JellyHttpRequest implements HttpServletRequest
{
    private HttpSession httpSession;
    private static final Logger log = Logger.getLogger(JellyHttpRequest.class);
    private HashMap parameters = new HashMap();
    private HashMap attributes = new HashMap();
    private String user;
    private Principal userPrinciple;

    public JellyHttpRequest(HashMap session)
    {
        httpSession = new JellyHttpSession(session);
    }

    public HttpSession getSession()
    {
        log.debug("JellyHttpRequest.getSession");
        return httpSession;
    }

    public Locale getLocale()
    {
        log.debug("JellyHttpRequest.getLocale");
        return Locale.getDefault();
    }

    public String getParameter(String key)
    {
        log.debug("JellyHttpRequest.getParameter");
        return (String) parameters.get(key);
    }

    public Map getParameterMap()
    {
        log.debug("JellyHttpRequest.getParameterMap");
        return parameters;
    }

    ////////////////////// Null Implementations //////////////////////
    public String getAuthType()
    {
        return null;
    }

    public Cookie[] getCookies()
    {
        return new Cookie[0];
    }

    public long getDateHeader(String s)
    {
        return 0;
    }

    public String getHeader(String s)
    {
        return null;
    }

    public Enumeration getHeaders(String s)
    {
        return null;
    }

    public Enumeration getHeaderNames()
    {
        return null;
    }

    public int getIntHeader(String s)
    {
        return 0;
    }

    public String getMethod()
    {
        return null;
    }

    public String getPathInfo()
    {
        return null;
    }

    public String getPathTranslated()
    {
        return null;
    }

    public String getContextPath()
    {
        return null;
    }

    public String getQueryString()
    {
        return null;
    }

    public String getRemoteUser()
    {
        return user;
    }

    public void setRemoteUser(String user)
    {
        this.user = user;
    }

    public boolean isUserInRole(String s)
    {
        return false;
    }

    public Principal getUserPrincipal()
    {
        return userPrinciple;
    }

    public void setUserPrincipal(Principal user)
    {
        this.userPrinciple = user;
    }

    public String getRequestedSessionId()
    {
        return null;
    }

    public String getRequestURI()
    {
        return null;
    }

    public StringBuffer getRequestURL()
    {
        return null;
    }

    public String getServletPath()
    {
        return null;
    }

    public HttpSession getSession(boolean b)
    {
        return getSession();
    }

    public boolean isRequestedSessionIdValid()
    {
        return false;
    }

    public boolean isRequestedSessionIdFromCookie()
    {
        return false;
    }

    public boolean isRequestedSessionIdFromURL()
    {
        return false;
    }

    public boolean isRequestedSessionIdFromUrl()
    {
        return false;
    }

    public Enumeration getLocales()
    {
        return null;
    }

    public boolean isSecure()
    {
        return false;
    }

    public RequestDispatcher getRequestDispatcher(String s)
    {
        return null;
    }

    public String getRealPath(String s)
    {
        return null;
    }

    public int getRemotePort()
    {
        return 0;
    }

    public String getLocalName()
    {
        return null;
    }

    public String getLocalAddr()
    {
        return null;
    }

    public int getLocalPort()
    {
        return 0;
    }

    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    public Enumeration getAttributeNames()
    {
        return Collections.enumeration(attributes.keySet());
    }

    public String getCharacterEncoding()
    {
        return null;
    }

    public void setCharacterEncoding(String s) throws UnsupportedEncodingException
    {
    }

    public int getContentLength()
    {
        return 0;
    }

    public String getContentType()
    {
        return null;
    }

    public ServletInputStream getInputStream() throws IOException
    {
        return null;
    }

    public Enumeration getParameterNames()
    {
        return Collections.enumeration(parameters.keySet());
    }

    public String[] getParameterValues(String name)
    {
        Object value = parameters.get(name);
        if (value instanceof String[])
        {
            return (String[]) value;
        }
        else if (value instanceof String)
        {
            return new String[] { (String) value };
        }
        else
        {
            return null;
        }
    }

    public String getProtocol()
    {
        return null;
    }

    public String getScheme()
    {
        return null;
    }

    public String getServerName()
    {
        return null;
    }

    public int getServerPort()
    {
        return 0;
    }

    public BufferedReader getReader() throws IOException
    {
        return null;
    }

    public String getRemoteAddr()
    {
        return null;
    }

    public String getRemoteHost()
    {
        return null;
    }

    public void setAttribute(String key, Object value)
    {
        attributes.put(key, value);
    }

    public void removeAttribute(String key)
    {
        attributes.remove(key);
    }
}
