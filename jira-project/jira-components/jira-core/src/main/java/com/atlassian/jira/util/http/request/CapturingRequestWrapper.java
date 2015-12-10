package com.atlassian.jira.util.http.request;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * This HttpServletRequestWrapper can capture the data contained within a HttpServletRequest.
 * <p/>
 * As data is read from the HttpServletRequest, it is stored in memory up to a certain limit.
 * <p/>
 * THREAD SAFETY - One of these must be constructed on each HTTP request/response and hence only one thread may write to
 * it at the one time.  This suits the whole servlet engine idea,
 *
 * @since v3.13.2
 */
public class CapturingRequestWrapper extends HttpServletRequestWrapper
{
    private final int maxCaptureBytes;
    private final ByteArrayOutputStream byteArrayOutputStream;
    private final OutputStreamWriter byteArrayWriter;

    private CapturingInputStream capturingInputStream;
    private CapturingBufferedReader capturingBufferedReader;
    private long writtenSoFar;

    /**
     * This request wrapper will capture up to maxCaptureBytes of input data as it is read by the application server.
     *
     * @param httpServletRequest the underlying HttpServletRequest
     * @param maxCaptureBytes the maximum number of bytes to capture
     */
    public CapturingRequestWrapper(HttpServletRequest httpServletRequest, final int maxCaptureBytes)
    {
        super(httpServletRequest);
        this.maxCaptureBytes = maxCaptureBytes;
        byteArrayOutputStream = new ByteArrayOutputStream(maxCaptureBytes);
        byteArrayWriter = new OutputStreamWriter(byteArrayOutputStream);
        writtenSoFar = 0;
    }

    /**
     * This returns the bytes that have been captured via the RequestWrapper,  This will be no bigger than
     * maxCaptureBytes
     *
     * @return the bytes that have been captured via the RequestWrapper
     */
    public byte[] getBytes()
    {
        try
        {
            byteArrayWriter.flush();
        }
        catch (IOException ignored)
        {
        }
        return byteArrayOutputStream.toByteArray();
    }

    public ServletInputStream getInputStream() throws IOException
    {
        if (capturingInputStream == null)
        {
            capturingInputStream = new CapturingInputStream(super.getInputStream());
        }
        return capturingInputStream;
    }

    public BufferedReader getReader() throws IOException
    {
        if (capturingBufferedReader == null)
        {
            capturingBufferedReader = new CapturingBufferedReader(super.getReader());
        }
        return capturingBufferedReader;
    }

    /**
     * This is a BufferedReader that captures the input as it is read up to maxCaptureBytes
     */
    private class CapturingBufferedReader extends BufferedReader
    {
        private final BufferedReader delegate;

        private CapturingBufferedReader(final BufferedReader delegate)
        {
            super(delegate);
            this.delegate = delegate;
        }

        private void capture(char[] buf)
        {
            capture(buf, 0, buf.length);
        }

        private void capture(char[] buf, int off, int len)
        {
            // how much left do we have in the tank
            long roomLeft = maxCaptureBytes - writtenSoFar;
            if (roomLeft > 0)
            {
                len = (int) Math.min(roomLeft, len);
                writtenSoFar += len;
                try
                {
                    byteArrayWriter.write(buf, off, len);
                }
                catch (IOException ignore)
                {
                }
            }
        }

        public int read() throws IOException
        {
            int c = delegate.read();
            if (c != -1)
            {
                capture(new char[] { (char) c }, 0, 1);
            }
            return c;
        }

        public String readLine() throws IOException
        {
            String s = delegate.readLine();
            if (s != null && s.length() > 0)
            {
                capture(s.toCharArray());
            }
            return s;
        }

        public int read(final char[] cbuf) throws IOException
        {
            int totalread = delegate.read(cbuf);
            if (totalread > 0)
            {
                capture(cbuf, 0, totalread);
            }
            return totalread;
        }

        public int read(final char[] cbuf, final int off, final int len) throws IOException
        {
            int totalread = delegate.read(cbuf, off, len);
            if (totalread > 0)
            {
                capture(cbuf, off, totalread);
            }
            return totalread;
        }

        public long skip(final long n) throws IOException
        {
            return delegate.skip(n);
        }

        public boolean ready() throws IOException
        {
            return delegate.ready();
        }

        public boolean markSupported()
        {
            return delegate.markSupported();
        }

        public void mark(final int readAheadLimit) throws IOException
        {
            delegate.mark(readAheadLimit);
        }

        public void reset() throws IOException
        {
            delegate.reset();
        }

        public void close() throws IOException
        {
            delegate.close();
        }
    }

    /**
     * A delegate ServletInputStream that captures bytes as they are read from the underlying delegate stream.
     */
    private class CapturingInputStream extends ServletInputStream
    {
        private final ServletInputStream delegate;

        private CapturingInputStream(final ServletInputStream delegate)
        {
            this.delegate = delegate;
        }

        private void capture(byte[] buf, int off, int len)
        {
            // how much left do we have in the tank
            long roomLeft = maxCaptureBytes - writtenSoFar;
            if (roomLeft > 0)
            {
                len = (int) Math.min(roomLeft, len);
                writtenSoFar += len;
                byteArrayOutputStream.write(buf, off, len);
            }
        }

        public int read() throws IOException
        {
            int c = delegate.read();
            if (c > -1)
            {
                capture(new byte[] { (byte) c }, 0, 1);
            }
            return c;
        }

        public int read(final byte[] b) throws IOException
        {
            int totalread = delegate.read(b);
            if (totalread > 0)
            {
                capture(b, 0, totalread);
            }
            return totalread;
        }

        public int read(final byte[] b, final int off, final int len) throws IOException
        {
            int totalread = delegate.read(b, off, len);
            if (totalread > 0)
            {
                capture(b, off, totalread);
            }
            return totalread;
        }

        public int readLine(final byte[] b, int off, int len) throws IOException
        {
            int totalread = delegate.readLine(b, off, len);
            if (totalread > 0)
            {
                capture(b, off, totalread);
            }
            return totalread;
        }

        public long skip(final long n) throws IOException
        {
            return delegate.skip(n);
        }

        public int available() throws IOException
        {
            return delegate.available();
        }

        public void close() throws IOException
        {
            delegate.close();
        }

        public void mark(final int readlimit)
        {
            delegate.mark(readlimit);
        }

        public void reset() throws IOException
        {
            delegate.reset();
        }

        public boolean markSupported()
        {
            return delegate.markSupported();
        }
    }

}
