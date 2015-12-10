package com.atlassian.jira.mock.servlet;

import java.io.IOException;
import java.io.StringWriter;
import javax.servlet.ServletOutputStream;

/**
 */
public class MockServletOutputStream extends ServletOutputStream
{
    private final StringWriter sw;

    public MockServletOutputStream(StringWriter sw)
    {
        this.sw = sw;
    }

    public void write(final int b) throws IOException
    {
        sw.write((char) b);
    }

    public String toString()
    {
        return sw.toString();
    }
}
