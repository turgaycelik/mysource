package com.atlassian.jira.web.action.admin.mail;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Bases on the idea copied from org.openqa.jetty.log.LogStream (selenium-server package)
 */
public class LogPrintStream extends PrintStream
{
    private final Logger log;
    private final ExposingsInternalByteArrayOutputStream bout;
    private final Level level;

    public void flush()
    {
        super.flush();
        if (bout.size() > 0)
        {
            String s = new String(bout.getBuf(), 0, bout.size()).trim();
            if (s.length() > 0 && log != null)
            {
                final String processedLine = processLine(s);
                if (processedLine != null)
                {
                    log.log(level, processedLine);
                }
            }
        }
        bout.reset();
    }

    protected String processLine(String s)
    {
        return s;
    }

    public LogPrintStream(Logger log, Level level)
    {
        super(new ExposingsInternalByteArrayOutputStream(128), true);
        this.level = level;
        bout = (ExposingsInternalByteArrayOutputStream) this.out;
        this.log = log;
    }

    public void close()
    {
        flush();
        super.close();
    }

    public void println()
    {
        super.println();
        flush();
    }

    public void println(boolean arg0)
    {
        super.println(arg0);
        flush();
    }

    public void println(char arg0)
    {
        super.println(arg0);
        flush();
    }

    public void println(char[] arg0)
    {
        super.println(arg0);
        flush();
    }

    public void println(double arg0)
    {
        super.println(arg0);
        flush();
    }

    public void println(float arg0)
    {
        super.println(arg0);
        flush();
    }

    public void println(int arg0)
    {
        super.println(arg0);
        flush();
    }

    public void println(long arg0)
    {
        super.println(arg0);
        flush();
    }

    public void println(Object arg0)
    {
        super.println(arg0);
        flush();
    }

    public void println(String arg0)
    {
        super.println(arg0);
        flush();
    }

    public void write(byte[] arg0, int arg1, int arg2)
    {
        super.write(arg0, arg1, arg2);
        flush();
    }


    private static class ExposingsInternalByteArrayOutputStream extends ByteArrayOutputStream
    {
        public ExposingsInternalByteArrayOutputStream(int size)
        {
            super(size);
        }

        public byte[] getBuf()
        {
            return buf;
        }

        public int getCount()
        {
            return count;
        }
    }

}
