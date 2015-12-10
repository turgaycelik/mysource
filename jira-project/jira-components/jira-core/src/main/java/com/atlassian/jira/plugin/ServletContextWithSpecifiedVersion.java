package com.atlassian.jira.plugin;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

/**
 * This class is used to fool the OSGi ExportsBuilder into thinking that we are providing a different version of the
 * Servlet API than we actually are. Unfortunately this is necessary due to overly-strict OSGi version constraints in
 * Spring MVC.
 * <p/>
 * Do not use to report a version that is <em>not</em> binary compatible with the version that is actually being
 * provided.
 *
 * @since v5.2
 */
class ServletContextWithSpecifiedVersion implements ServletContext
{
    private final ServletContext delegate;
    private final int majorVersion;
    private final int minorVersion;

    public ServletContextWithSpecifiedVersion(ServletContext delegate, int majorVersion, int minorVersion)
    {
        this.delegate = delegate;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    @Override
    public int getMajorVersion()
    {
        return majorVersion;
    }

    @Override
    public int getMinorVersion()
    {
        return minorVersion;
    }

    //
    // Below here just delegates.
    //

    @Override
    public ServletContext getContext(String uripath) {return delegate.getContext(uripath);}

    @Override
    public String getMimeType(String file) {return delegate.getMimeType(file);}

    @Override
    public Set getResourcePaths(String path) {return delegate.getResourcePaths(path);}

    @Override
    public URL getResource(String path) throws MalformedURLException {return delegate.getResource(path);}

    @Override
    public InputStream getResourceAsStream(String path) {return delegate.getResourceAsStream(path);}

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {return delegate.getRequestDispatcher(path);}

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {return delegate.getNamedDispatcher(name);}

    @Override
    public Servlet getServlet(String name) throws ServletException {return delegate.getServlet(name);}

    @Override
    public Enumeration getServlets() {return delegate.getServlets();}

    @Override
    public Enumeration getServletNames() {return delegate.getServletNames();}

    @Override
    public void log(String msg) {delegate.log(msg);}

    @Override
    public void log(Exception exception, String msg) {delegate.log(exception, msg);}

    @Override
    public void log(String message, Throwable throwable) {delegate.log(message, throwable);}

    @Override
    public String getRealPath(String path) {return delegate.getRealPath(path);}

    @Override
    public String getServerInfo() {return delegate.getServerInfo();}

    @Override
    public String getInitParameter(String name) {return delegate.getInitParameter(name);}

    @Override
    public Enumeration getInitParameterNames() {return delegate.getInitParameterNames();}

    @Override
    public Object getAttribute(String name) {return delegate.getAttribute(name);}

    @Override
    public Enumeration getAttributeNames() {return delegate.getAttributeNames();}

    @Override
    public void setAttribute(String name, Object object) {delegate.setAttribute(name, object);}

    @Override
    public void removeAttribute(String name) {delegate.removeAttribute(name);}

    @Override
    public String getServletContextName() {return delegate.getServletContextName();}
}
