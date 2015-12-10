package com.atlassian.jira.mock.servlet;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Mock implementation of HttpServletResponse
 *
 */
public class MockHttpServletResponse implements HttpServletResponse
{
    private ServletOutputStream servletOutputStream;
    private PrintWriter writer;

    private int status;
    private String contentType;
    private String redirect;

    private final List<Cookie> cookies = Lists.newArrayList();

    private ListMultimap<String, String> headers = ArrayListMultimap.create();

    public MockHttpServletResponse()
    {
    }

    public MockHttpServletResponse(final ServletOutputStream servletOutputStream)
    {
        this.servletOutputStream = servletOutputStream;
    }

    public MockHttpServletResponse(final PrintWriter writer)
    {
        this.writer = writer;
    }

    public void addCookie(final Cookie cookie)
    {
        this.cookies.add(cookie);
    }

    public Iterable<Cookie> getCookies()
    {
        return ImmutableList.copyOf(cookies);
    }

    public boolean containsHeader(final String s)
    {
        return false;
    }

    public String encodeURL(final String s)
    {
        return null;
    }

    public String encodeRedirectURL(final String s)
    {
        return null;
    }

    public String encodeUrl(final String s)
    {
        return null;
    }

    public String encodeRedirectUrl(final String s)
    {
        return null;
    }

    public void sendError(final int i, final String s) throws IOException
    {
        status = i;
    }

    public void sendError(final int i) throws IOException
    {
        status = i;
    }

    public void sendRedirect(final String s) throws IOException
    {
        status = 302;
        redirect = s;
    }

    public String getRedirect()
    {
        return redirect;
    }

    public void setRedirect(final String redirect)
    {
        this.redirect = redirect;
    }

    public void setDateHeader(final String s, final long l)
    {
    }

    public void addDateHeader(final String s, final long l)
    {
    }

    @Override
    public void setHeader(String name, String value)
    {
        headers.put(name.toLowerCase(Locale.US), value);
    }

    public List<String> getHeader(String name)
    {
        return headers.get(name.toLowerCase(Locale.US));
    }

    @Override
    public void addHeader(final String name, final String value)
    {
        // we don't care here about difference between setHeader and addHeader
        // if you ever care please update those methods accordingly
        setHeader(name, value);
    }

    public void setIntHeader(final String s, final int i)
    {
    }

    public void addIntHeader(final String s, final int i)
    {
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(final int i)
    {
        status = i;
    }

    public void setStatus(final int i, final String s)
    {
        status = i;
    }

    public String getCharacterEncoding()
    {
        return null;
    }

    public String getContentType()
    {
        return contentType;
    }

    public ServletOutputStream getOutputStream() throws IOException
    {
        return servletOutputStream;
    }

    public PrintWriter getWriter() throws IOException
    {
        return writer;
    }

    public void setCharacterEncoding(final String s)
    {
    }

    public void setContentLength(final int i)
    {
    }

    public void setContentType(final String ct)
    {
        this.contentType = ct;
    }

    public void setBufferSize(final int i)
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

    public void setLocale(final Locale locale)
    {
    }

    public Locale getLocale()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE, false, MockHttpServletResponse.class);
    }
}
