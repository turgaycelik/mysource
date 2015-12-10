package com.atlassian.jira.mail;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: MIKE
 * Date: Jul 7, 2005
 * Time: 10:06:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubscriptionMailQueueMockRequest implements HttpServletRequest
{
    private String ctx;

    public SubscriptionMailQueueMockRequest(String contextPath)
    {
        this.ctx = contextPath;
    }

    // these two methods are used...
    public String getContextPath()
    {
        return this.ctx;
    }

    public String getRequestURI()
    {
        return ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL);
    }

    // the rest aren't! :)
    public String getAuthType()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Cookie[] getCookies()
    {
        return new Cookie[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getDateHeader(String string)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getHeader(String string)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getHeaders(String string)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getHeaderNames()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getIntHeader(String string)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getMethod()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPathInfo()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPathTranslated()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getQueryString()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRemoteUser()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isUserInRole(String string)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Principal getUserPrincipal()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRequestedSessionId()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public StringBuffer getRequestURL()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getServletPath()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public HttpSession getSession(boolean b)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public HttpSession getSession()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRequestedSessionIdValid()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRequestedSessionIdFromCookie()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRequestedSessionIdFromURL()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRequestedSessionIdFromUrl()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getAttribute(String string)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getAttributeNames()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getCharacterEncoding()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setCharacterEncoding(String string) throws UnsupportedEncodingException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getContentLength()
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getContentType()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServletInputStream getInputStream() throws IOException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getParameter(String string)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getParameterNames()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getParameterValues(String string)
    {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getParameterMap()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getProtocol()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getScheme()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getServerName()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getServerPort()
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BufferedReader getReader() throws IOException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRemoteAddr()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRemoteHost()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAttribute(String string, Object object)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAttribute(String string)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Locale getLocale()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Enumeration getLocales()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSecure()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RequestDispatcher getRequestDispatcher(String string)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRealPath(String string)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
}
