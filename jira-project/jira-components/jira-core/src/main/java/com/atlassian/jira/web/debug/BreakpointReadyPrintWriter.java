package com.atlassian.jira.web.debug;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * This should only be used in debug mode
 *
 * @since v6.0
 */
public class BreakpointReadyPrintWriter extends PrintWriter
{
    public BreakpointReadyPrintWriter(final Writer out)
    {
        super(out);
    }

    @Override
    public void write(final int c)
    {
        super.write(c);
    }

    @Override
    public void write(final char[] buf, final int off, final int len)
    {
        super.write(buf, off, len);
    }

    @Override
    public void write(final char[] buf)
    {
        super.write(buf);
    }

    @Override
    public void write(final String s, final int off, final int len)
    {
        super.write(s, off, len);
    }

    @Override
    public void write(final String s)
    {
        super.write(s);
    }
}
