package com.atlassian.jira.web.debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * Trying to find out who is setting the content type?  Debugging who is adding that extra header?
 * <p/>
 * This set of classes is just for you.  It only gets setup in dev.mode and it allows you to set a breakpoint into the
 * method of your choice.
 *
 * @since v6.0
 */
@SuppressWarnings ("UnnecessaryLocalVariable")
public class BreakpointReadyHttpServletRequest extends HttpServletRequestWrapper
{
    public BreakpointReadyHttpServletRequest(final HttpServletRequest delegate)
    {
        super(delegate);
    }

    @Override
    public void setAttribute(final String name, final Object value)
    {
        super.setAttribute(name, value);
    }

    @Override
    public void setCharacterEncoding(final String encoding) throws UnsupportedEncodingException
    {
        super.setCharacterEncoding(encoding);
    }

    @Override
    public void removeAttribute(final String name)
    {
        super.removeAttribute(name);
    }

    //
    // GETTERS
    //

    @Override
    public String getAuthType()
    {
        String authType = super.getAuthType();
        return authType;
    }

    @Override
    public Cookie[] getCookies()
    {
        Cookie[] cookies = super.getCookies();
        return cookies;
    }

    @Override
    public long getDateHeader(final String name)
    {
        long dateHeader = super.getDateHeader(name);
        return dateHeader;
    }

    @Override
    public String getHeader(final String name)
    {
        String header = super.getHeader(name);
        return header;
    }

    @Override
    public Enumeration getHeaders(final String name)
    {
        Enumeration headers = super.getHeaders(name);
        return headers;
    }

    @Override
    public Enumeration getHeaderNames()
    {
        Enumeration headerNames = super.getHeaderNames();
        return headerNames;
    }

    @Override
    public int getIntHeader(final String name)
    {
        int intHeader = super.getIntHeader(name);
        return intHeader;
    }

    @Override
    public String getMethod()
    {
        String method = super.getMethod();
        return method;
    }

    @Override
    public String getPathInfo()
    {
        String pathInfo = super.getPathInfo();
        return pathInfo;
    }

    @Override
    public String getPathTranslated()
    {
        String pathTranslated = super.getPathTranslated();
        return pathTranslated;
    }

    @Override
    public String getContextPath()
    {
        String contextPath = super.getContextPath();
        return contextPath;
    }

    @Override
    public String getQueryString()
    {
        String queryString = super.getQueryString();
        return queryString;
    }

    @Override
    public String getRemoteUser()
    {
        String remoteUser = super.getRemoteUser();
        return remoteUser;
    }

    @Override
    public boolean isUserInRole(final String role)
    {
        boolean userInRole = super.isUserInRole(role);
        return userInRole;
    }

    @Override
    public Principal getUserPrincipal()
    {
        Principal userPrincipal = super.getUserPrincipal();
        return userPrincipal;
    }

    @Override
    public String getRequestedSessionId()
    {
        String requestedSessionId = super.getRequestedSessionId();
        return requestedSessionId;
    }

    @Override
    public String getRequestURI()
    {
        String requestURI = super.getRequestURI();
        return requestURI;
    }

    @Override
    public StringBuffer getRequestURL()
    {
        StringBuffer requestURL = super.getRequestURL();
        return requestURL;
    }

    @Override
    public String getServletPath()
    {
        String servletPath = super.getServletPath();
        return servletPath;
    }

    @Override
    public HttpSession getSession(final boolean create)
    {
        HttpSession session = super.getSession(create);
        return session;
    }

    @Override
    public HttpSession getSession()
    {
        HttpSession session = super.getSession();
        return session;
    }

    @Override
    public boolean isRequestedSessionIdValid()
    {
        boolean requestedSessionIdValid = super.isRequestedSessionIdValid();
        return requestedSessionIdValid;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie()
    {
        boolean requestedSessionIdFromCookie = super.isRequestedSessionIdFromCookie();
        return requestedSessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromURL()
    {
        boolean requestedSessionIdFromURL = super.isRequestedSessionIdFromURL();
        return requestedSessionIdFromURL;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl()
    {
        boolean requestedSessionIdFromUrl = super.isRequestedSessionIdFromUrl();
        return requestedSessionIdFromUrl;
    }

    @Override
    public Object getAttribute(final String name)
    {
        Object attribute = super.getAttribute(name);
        return attribute;
    }

    @Override
    public Enumeration getAttributeNames()
    {
        Enumeration attributeNames = super.getAttributeNames();
        return attributeNames;
    }

    @Override
    public String getCharacterEncoding()
    {
        String characterEncoding = super.getCharacterEncoding();
        return characterEncoding;
    }

    @Override
    public int getContentLength()
    {
        int contentLength = super.getContentLength();
        return contentLength;
    }

    @Override
    public String getContentType()
    {
        String contentType = super.getContentType();
        return contentType;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        ServletInputStream inputStream = super.getInputStream();
        return inputStream;
    }

    @Override
    public String getParameter(final String name)
    {
        String parameter = super.getParameter(name);
        return parameter;
    }

    @Override
    public Enumeration getParameterNames()
    {
        Enumeration parameterNames = super.getParameterNames();
        return parameterNames;
    }

    @Override
    public String[] getParameterValues(final String name)
    {
        String[] parameterValues = super.getParameterValues(name);
        return parameterValues;
    }

    @Override
    public Map getParameterMap()
    {
        Map parameterMap = super.getParameterMap();
        return parameterMap;
    }

    @Override
    public String getProtocol()
    {
        String protocol = super.getProtocol();
        return protocol;
    }

    @Override
    public String getScheme()
    {
        String scheme = super.getScheme();
        return scheme;
    }

    @Override
    public String getServerName()
    {
        String serverName = super.getServerName();
        return serverName;
    }

    @Override
    public int getServerPort()
    {
        int serverPort = super.getServerPort();
        return serverPort;
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        BufferedReader reader = super.getReader();
        return reader;
    }

    @Override
    public String getRemoteAddr()
    {
        String remoteAddr = super.getRemoteAddr();
        return remoteAddr;
    }

    @Override
    public String getRemoteHost()
    {
        String remoteHost = super.getRemoteHost();
        return remoteHost;
    }


    @Override
    public Locale getLocale()
    {
        Locale locale = super.getLocale();
        return locale;
    }

    @Override
    public Enumeration getLocales()
    {
        Enumeration locales = super.getLocales();
        return locales;
    }

    @Override
    public boolean isSecure()
    {
        boolean secure = super.isSecure();
        return secure;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(final String path)
    {
        RequestDispatcher requestDispatcher = super.getRequestDispatcher(path);
        return requestDispatcher;
    }

    @Override
    public String getRealPath(final String path)
    {
        String realPath = super.getRealPath(path);
        return realPath;
    }

    @Override
    public int getRemotePort()
    {
        int remotePort = super.getRemotePort();
        return remotePort;
    }

    @Override
    public String getLocalName()
    {
        String localName = super.getLocalName();
        return localName;
    }

    @Override
    public String getLocalAddr()
    {
        String localAddr = super.getLocalAddr();
        return localAddr;
    }

    @Override
    public int getLocalPort()
    {
        int localPort = super.getLocalPort();
        return localPort;
    }

    // Servlet 3

    public boolean isAsyncSupported()
    {
        return false;
    }
}
