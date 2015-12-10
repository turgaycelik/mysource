package com.atlassian.jira.plugin.webresource;

import java.io.IOException;
import java.io.Writer;

/**
 * A writer that filters out superbatch web-resources. This is a bit of hack since we know that the WebResourceManager
 * will call writer.write() with a tag per line (which will include 'superbatch').
 *
 * @since v4.4
 */
public class SuperBatchFilteringWriter extends Writer
{
    private final StringBuffer buf;

    public SuperBatchFilteringWriter()
    {
        buf = new StringBuffer();
        lock = buf;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException
    {
        final StringBuilder string = new StringBuilder();
        string.append(cbuf, off, len);
        if (!string.toString().contains("/download/superbatch/"))
        {
            buf.append(cbuf, off, len);
        }
    }

    @Override
    public void flush() throws IOException
    {
    }

    @Override
    public void close() throws IOException
    {
    }

    @Override
    public String toString()
    {
        return buf.toString();
    }
}
