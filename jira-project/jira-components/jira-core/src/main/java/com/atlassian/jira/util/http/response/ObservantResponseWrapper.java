package com.atlassian.jira.util.http.response;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This HttpServletResponseWrapper can capture the HTTP response code and also to know the length of the data output.
 * <p/>
 * THREAD SAFETY - One of these must be constructed on each HTTP request/response and hence
 * only one thread may write to it at the one time.  This suits the whole servlet engine idea,
 *
 * @since v3.13.2
 */
public class ObservantResponseWrapper extends HttpServletResponseWrapper
{
    private long contentLen = 0;
    private int status = HttpServletResponse.SC_OK;
    private SizeObservingPrintWriter sizeCapturingPrintWriter;
    private SizeObservingServletOutputStream sizeCapturingServletOutputStream;

    public ObservantResponseWrapper(final HttpServletResponse httpServletResponse)
    {
        super(httpServletResponse);
    }

    /**
     * @return the HTTP status code set on this HttpServletResponseWrapper
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * Returns the length of the content that has been sent back via this HttpServletResponseWrapper.
     *
     * This is a count in character OR bytes, depending on form of output was used.  Let em explain!
     * If the web response uses the {@link #getOutputStream()} method, then the content length will be a count of
     * bytes.  If they use the {@link #getWriter()} method, then it will be a count of characters.
     *
     * We dont want to get into the game of working out how a set of Unicode characters translate into a series of bytes.  Its expensive
     * and the point of this code is to be as cheap as possible.
     *
     *
     * @return the length of the content that has been sent back via this HttpServletResponseWrapper in bytes or characters
     * depending on whcih output mechanism is used.
     */
    public long getContentLen()
    {
        return contentLen;
    }

    /**
     * Overridden to capture HTTP status codes
     */
    public void sendError(final int sc, final String s) throws IOException
    {
        status = sc;
        super.sendError(sc, s);
    }

    /**
     * Overridden to capture HTTP status codes
     */
    public void sendError(final int sc) throws IOException
    {
        status = sc;
        super.sendError(sc);
    }

    /**
     * Overridden to capture HTTP status codes
     */
    public void setStatus(final int sc)
    {
        status = sc;
        super.setStatus(sc);
    }

    /**
     * Overridden to capture HTTP status codes
     */
    public void setStatus(final int sc, final String s)
    {
        status = sc;
        super.setStatus(sc, s);
    }

    /**
     * Overridden to capture output content length
     */
    public PrintWriter getWriter() throws IOException
    {
        if (sizeCapturingPrintWriter == null)
        {
            sizeCapturingPrintWriter = new SizeObservingPrintWriter(super.getWriter());
        }
        return sizeCapturingPrintWriter;
    }

    /**
     * Overridden to capture output content length
     */
    public ServletOutputStream getOutputStream() throws IOException
    {
        if (sizeCapturingServletOutputStream == null)
        {
            sizeCapturingServletOutputStream = new SizeObservingServletOutputStream(super.getOutputStream());
        }
        return sizeCapturingServletOutputStream;
    }

    /**
     * This will must be used in JIRA since most of our view layer is in JSP and its uses
     * the JSPWriter thingy. But we have both types just in case
     */
    private class SizeObservingPrintWriter extends PrintWriter
    {
        private SizeObservingPrintWriter(final PrintWriter delegate)
        {
            super(delegate);
        }

        /**
         * See the comment above about our strategy in determining byteCount
         */
        private int determineByteCount(final char buf[], final int off, final int len) {
            return len;
        }

        public void write(final char buf[], final int off, final int len)
        {
            super.write(buf, off, len);
            contentLen += determineByteCount(buf,off,len);
        }

        public void write(final String s, final int off, final int len)
        {
            super.write(s, off, len);
            contentLen += determineByteCount(s.toCharArray(),off,len);
        }

        public void println()
        {
            super.println();
            // see above about why this counts in 1's
            contentLen += 1;
        }
    }

    /**
     * This is less likely to be used but is still needed!
     */
    private class SizeObservingServletOutputStream extends ServletOutputStream
    {
        private final ServletOutputStream delegateStream;

        private SizeObservingServletOutputStream(ServletOutputStream delegateStream)
        {
            this.delegateStream = delegateStream;
        }

        public void write(final int b) throws IOException
        {
            delegateStream.write(b);
            contentLen++;
        }

        public void write(final byte[] b) throws IOException {
            delegateStream.write(b);
            contentLen += b.length;
        }

        public void write(final byte[] b, final int off, final int len) throws IOException
        {
            delegateStream.write(b, off, len);
            contentLen += len;
        }

    }
}
