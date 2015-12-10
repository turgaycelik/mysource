package com.atlassian.jira.web.debug;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Trying to find out who is setting the content type?  Debugging who is adding that extra header?
 * <p/>
 * This set of classes is just for you.  It only gets setup in dev.mode and it allows you to set a breakpoint into the
 * method of your choice.
 *
 * @since v6.0
 */
@SuppressWarnings ("UnnecessaryLocalVariable")
public class BreakpointReadyHttpServletResponse extends HttpServletResponseWrapper
{

    public BreakpointReadyHttpServletResponse(final HttpServletResponse delegate)
    {
       super(delegate);
    }

    @Override
    public void addCookie(final Cookie cookie)
    {
        super.addCookie(cookie);
    }

    @Override
    public void sendError(final int sc, final String msg) throws IOException
    {
        super.sendError(sc, msg);
    }

    @Override
    public void sendError(final int sc) throws IOException
    {
        super.sendError(sc);
    }

    @Override
    public void sendRedirect(final String location) throws IOException
    {
        super.sendRedirect(location);
    }

    @Override
    public void setDateHeader(final String name, final long date)
    {
        super.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(final String name, final long date)
    {
        super.addDateHeader(name, date);
    }

    @Override
    public void setHeader(final String name, final String value)
    {
        super.setHeader(name, value);
    }

    @Override
    public void addHeader(final String name, final String value)
    {
        super.addHeader(name, value);
    }

    @Override
    public void setIntHeader(final String name, final int value)
    {
        super.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(final String name, final int value)
    {
        super.addIntHeader(name, value);
    }

    @Override
    public void setStatus(final int sc)
    {
        super.setStatus(sc);
    }

    @Override
    public void setStatus(final int sc, final String sm)
    {
        super.setStatus(sc, sm);
    }

    @Override
    public void setCharacterEncoding(final String charset)
    {
        super.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(final int len)
    {
        super.setContentLength(len);
    }

    @Override
    public void setContentType(final String type)
    {
        super.setContentType(type);
    }

    @Override
    public void setBufferSize(final int size)
    {
        super.setBufferSize(size);
    }

    @Override
    public void flushBuffer() throws IOException
    {
        super.flushBuffer();
    }

    @Override
    public void resetBuffer()
    {
        super.resetBuffer();
    }

    @Override
    public void reset()
    {
        super.reset();
    }

    @Override
    public void setLocale(final Locale loc)
    {
        super.setLocale(loc);
    }

    //
    // GETTERS
    //


    @Override
    public boolean containsHeader(final String name)
    {
        boolean b = super.containsHeader(name);
        return b;
    }

    @Override
    public String encodeURL(final String url)
    {
        String s = super.encodeURL(url);
        return s;
    }

    @Override
    public String encodeRedirectURL(final String url)
    {
        String s = super.encodeRedirectURL(url);
        return s;
    }

    @Override
    public String encodeUrl(final String url)
    {
        String s = super.encodeUrl(url);
        return s;
    }

    @Override
    public String encodeRedirectUrl(final String url)
    {
        String s = super.encodeRedirectUrl(url);
        return s;
    }

    @Override
    public int getBufferSize()
    {
        int bufferSize = super.getBufferSize();
        return bufferSize;
    }

    @Override
    public boolean isCommitted()
    {
        boolean committed = super.isCommitted();
        return committed;
    }

    @Override
    public String getCharacterEncoding()
    {
        String characterEncoding = super.getCharacterEncoding();
        return characterEncoding;
    }

    @Override
    public String getContentType()
    {
        String contentType = super.getContentType();
        return contentType;
    }

    @Override
    public Locale getLocale()
    {
        Locale locale = super.getLocale();
        return locale;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        ServletOutputStream outputStream = super.getOutputStream();
        return new BreakpointReadyServletOutputStream(outputStream);
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        PrintWriter writer = super.getWriter();
        return new BreakpointReadyPrintWriter(writer);
    }
}
