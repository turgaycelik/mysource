package com.atlassian.jira.web.debug;

import java.io.IOException;
import javax.servlet.ServletOutputStream;

/**
 * This should only ve used in dev mode.
 *
 * @since v6.0
 */
public class BreakpointReadyServletOutputStream extends ServletOutputStream
{
    private final ServletOutputStream delegate;

    public BreakpointReadyServletOutputStream(final ServletOutputStream delegate)
    {
        this.delegate = delegate;
    }

    /**
     * This is the guy that is always called at the very bottom of the calls
     *
     * @param b something to write
     */
    @Override
    public void write(final int b) throws IOException
    {
        delegate.write(b);
    }

    @Override
    public void write(final byte[] b) throws IOException
    {
        delegate.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException
    {
        delegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException
    {
        delegate.flush();
    }

    @Override
    public void close() throws IOException
    {
        delegate.close();
    }
}
