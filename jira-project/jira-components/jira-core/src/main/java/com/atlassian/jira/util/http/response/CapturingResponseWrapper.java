package com.atlassian.jira.util.http.response;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This HttpServletResponseWrapper can capture an initial amount of response data and the headers/cookies that may be
 * set on the response.
 * <p/>
 * THREAD SAFETY - One of these must be constructed on each HTTP request/response and hence only one thread may write to
 * it at the one time.  This suits the whole servlet engine idea,
 *
 * @since v3.13.2
 */
public class CapturingResponseWrapper extends HttpServletResponseWrapper
{
    private final long maxCaptureLen;
    private final List/*<Cookie>*/ cookieList;
    private final List/*<HttpHeader>*/ headerList;

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final Writer byteArrayWriter;

    private CapturingPrintWriter writer;
    private CapturingServletOutputStream outputStream;

    /**
     * This will wrap the HttpServletResponse and capture up to maxCaptureLen of the response output.
     *
     * @param httpServletResponse the servlet response
     * @param maxCaptureLen the maximum number of bytes to capture
     */
    public CapturingResponseWrapper(final HttpServletResponse httpServletResponse, final int maxCaptureLen)
    {
        super(httpServletResponse);
        this.maxCaptureLen = maxCaptureLen;
        this.headerList = new ArrayList();
        this.cookieList = new ArrayList();

        this.byteArrayOutputStream = new ByteArrayOutputStream(maxCaptureLen);
        this.byteArrayWriter = new OutputStreamWriter(byteArrayOutputStream);

    }

    /**
     * This returns the bytes that have been captured on the ResponseWrapper.
     *
     * @return the bytes that have been captured on the ResponseWrapper
     */
    public byte[] getBytes()
    {
        try
        {
            byteArrayWriter.flush();
        }
        catch (IOException ignore)
        {
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * This returns the number of bytes that have been captured so far.
     *
     * @return the the number of bytes that have been captured so far.
     */
    public int size()
    {
        try
        {
            byteArrayWriter.flush();
        }
        catch (IOException ignore)
        {
        }
        return byteArrayOutputStream.size();
    }

    /**
     * This returns a list of all the {@link Cookie}'s that have been set on this response
     *
     * @return a list of all the {@link Cookie}'s that have been set on this response
     */
    public List/*<Cookie>*/ getCookieList()
    {
        return Collections.unmodifiableList(cookieList);
    }

    /**
     * This returns a list of all the {@link CapturingResponseWrapper.HttpHeader}'s that have been set on this response
     *
     * @return a list of all the {@link CapturingResponseWrapper.HttpHeader}'s that have been set on this response
     */
    public List /*<HttpHeader>*/ getHeaderList()
    {
        return Collections.unmodifiableList(headerList);
    }

    /**
     * Overridden to capture cookie information.
     */
    public void addCookie(Cookie cookie)
    {
        super.addCookie(cookie);
        cookieList.add(cookie);
    }

    private void setHeader(HttpHeader header)
    {
        int index = findNamedHeader(header.getName());
        if (index == -1)
        {
            headerList.add(header);
        }
        else
        {
            headerList.set(index, header);
        }
    }

    private int findNamedHeader(String name)
    {
        for (int i = 0; i < headerList.size(); i++)
        {
            HttpHeader httpHeader = (HttpHeader) headerList.get(i);
            if (httpHeader.getName().equals(name))
            {
                return i;
            }
        }
        return -1;
    }

    private void addHeader(HttpHeader header)
    {
        headerList.add(header);
    }

    /**
     * Overridden to capture HTTP header information
     */
    public void setDateHeader(String s, long l)
    {
        super.setDateHeader(s, l);
        setHeader(new HttpHeader(s, new Date(l)));
    }

    /**
     * Overridden to capture HTTP header information
     */
    public void addDateHeader(String s, long l)
    {
        super.addDateHeader(s, l);
        addHeader(new HttpHeader(s, new Date(l)));
    }

    /**
     * Overridden to capture HTTP header information
     */
    public void setHeader(String s, String s1)
    {
        super.setHeader(s, s1);
        setHeader(new HttpHeader(s, s1));
    }

    /**
     * Overridden to capture HTTP header information
     */
    public void addHeader(String s, String s1)
    {
        super.addHeader(s, s1);
        addHeader(new HttpHeader(s, s1));
    }

    /**
     * Overridden to capture HTTP header information
     */
    public void setIntHeader(String s, int i)
    {
        super.setIntHeader(s, i);
        setHeader(new HttpHeader(s, new Integer(i)));
    }

    public void addIntHeader(String s, int i)
    {
        super.addIntHeader(s, i);
        addHeader(new HttpHeader(s, new Integer(i)));
    }

    /**
     * This class represent the HTTP headers that have been output to this ResponseWrapper
     */
    public static class HttpHeader
    {
        private String headerName;
        private Object headerValue;

        private HttpHeader(String headerName, Object headerValue)
        {
            this.headerName = headerName;
            this.headerValue = headerValue;
        }

        public String getName()
        {
            return headerName;
        }

        public String getValue()
        {
            if (headerValue instanceof Date)
            {
                return formatDate((Date) headerValue);
            }
            else
            {
                return String.valueOf(headerValue);
            }
        }

        private static final String HTTP_HEADER_DF = "dd/MMM/yyyy:HH:mm:ss Z";

        private String formatDate(Date date)
        {
            return new SimpleDateFormat(HTTP_HEADER_DF).format(date);
        }

        ///CLOVER:OFF
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            HttpHeader header = (HttpHeader) o;
            if (headerName != null ? !headerName.equals(header.headerName) : header.headerName != null)
            {
                return false;
            }
            return !(headerValue != null ? !headerValue.equals(header.headerValue) : header.headerValue != null);

        }

        public int hashCode()
        {
            int result;
            result = (headerName != null ? headerName.hashCode() : 0);
            result = 31 * result + (headerValue != null ? headerValue.hashCode() : 0);
            return result;
        }

        public String toString()
        {
            return headerName + '=' + headerValue;
        }
        ///CLOVER:ON
    }

    public PrintWriter getWriter() throws IOException
    {
        if (writer == null)
        {
            writer = new CapturingPrintWriter(super.getWriter());
        }
        return writer;
    }

    public ServletOutputStream getOutputStream() throws IOException
    {
        if (outputStream == null)
        {
            outputStream = new CapturingServletOutputStream(super.getOutputStream());
        }
        return outputStream;
    }

    /**
     * This will most likely be used in JIRA since most of our view layer is in JSP and its uses the JSPWriter thingy.
     * But we have both types just in case
     */
    private class CapturingPrintWriter extends PrintWriter
    {
        private CapturingPrintWriter(final PrintWriter delegate)
        {
            super(delegate);
        }

        private void capture(char[] buf, int off, int len)
        {
            // how much left do we have in the tank
            long roomLeft = maxCaptureLen - size();
            if (roomLeft > 0)
            {
                len = (int) Math.min(roomLeft, len);
                try
                {
                    byteArrayWriter.write(buf, off, len);
                }
                catch (IOException ignore)
                {
                }
            }
        }

        public void write(final char buf[], final int off, final int len)
        {
            super.write(buf, off, len);
            if (size() < maxCaptureLen)
            {
                capture(buf, off, len);
            }
        }

        public void write(final String s, final int off, final int len)
        {
            super.write(s, off, len);
            if (size() < maxCaptureLen)
            {
                capture(s.toCharArray(), off, len);
            }
        }

        public void println()
        {
            super.println();
            if (size() < maxCaptureLen)
            {
                capture(new char[] { '\n' }, 0, 1);
            }
        }
    }

    /**
     * This is less likely to be used but is still needed!
     */
    private class CapturingServletOutputStream extends ServletOutputStream
    {
        private final ServletOutputStream delegateStream;

        private CapturingServletOutputStream(ServletOutputStream delegateStream)
        {
            this.delegateStream = delegateStream;
        }

        private void capture(byte bytes[], int offset, int len)
        {
            long roomLeft = maxCaptureLen - size();
            if (roomLeft > 0)
            {
                byteArrayOutputStream.write(bytes, offset, len);
            }
        }

        private void capture(byte b)
        {
            long roomLeft = maxCaptureLen - size();
            if (roomLeft > 0)
            {
                byteArrayOutputStream.write(b);
            }
        }

        public void write(final int b) throws IOException
        {
            delegateStream.write(b);
            capture((byte) b);
        }

        public void write(final byte b[]) throws IOException
        {
            delegateStream.write(b);
            capture(b, 0, b.length);
        }

        public void write(final byte b[], final int off, final int len) throws IOException
        {
            delegateStream.write(b, off, len);
            capture(b, off, len);
        }

        public void close() throws IOException {
            delegateStream.close();
        }

        public void flush() throws IOException {
            delegateStream.flush();
        }
    }

}