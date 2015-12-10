/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.apache.log4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class JellyHttpResponse implements HttpServletResponse
{
    private static final Logger log = Logger.getLogger(JellyHttpResponse.class);
    private String redirectUrl;

    public JellyHttpResponse()
    {
    }

    public String getRedirectUrl()
    {
        return redirectUrl;
    }

    public void sendRedirect(String location) throws IOException
    {
        log.debug("JellyHttpResponse.sendRedirect");
        this.redirectUrl = location;
    }

    public String encodeURL(String url)
    {
        log.debug("JellyHttpResponse.encodeURL");
        return url;
    }

    ////////////////////// Null Implementations //////////////////////
    public void addCookie(Cookie cookie)
    {
    }

    public boolean containsHeader(String s)
    {
        return false;
    }

    public String encodeRedirectURL(String s)
    {
        return null;
    }

    public String encodeUrl(String s)
    {
        return null;
    }

    public String encodeRedirectUrl(String s)
    {
        return null;
    }

    public void sendError(int i, String s) throws IOException
    {
    }

    public void sendError(int i) throws IOException
    {
    }

    public void setDateHeader(String s, long l)
    {
    }

    public void addDateHeader(String s, long l)
    {
    }

    public void setHeader(String s, String s1)
    {
    }

    public void addHeader(String s, String s1)
    {
    }

    public void setIntHeader(String s, int i)
    {
    }

    public void addIntHeader(String s, int i)
    {
    }

    public void setStatus(int i)
    {
    }

    public void setStatus(int i, String s)
    {
    }

    public String getCharacterEncoding()
    {
        return null;
    }

    public String getContentType()
    {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException
    {
        return null;
    }

    public PrintWriter getWriter() throws IOException
    {
        return null;
    }

    public void setCharacterEncoding(String string)
    {
    }

    public void setContentLength(int i)
    {
    }

    public void setContentType(String s)
    {
    }

    public void setBufferSize(int i)
    {
    }

    public int getBufferSize()
    {
        return 0;
    }

    public void flushBuffer() throws IOException
    {
    }

    public void resetBuffer()
    {
    }

    public boolean isCommitted()
    {
        return false;
    }

    public void reset()
    {
    }

    public void setLocale(Locale locale)
    {
    }

    public Locale getLocale()
    {
        return null;
    }
}
