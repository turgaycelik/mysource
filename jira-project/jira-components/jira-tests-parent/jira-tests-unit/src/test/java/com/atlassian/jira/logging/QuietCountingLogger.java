package com.atlassian.jira.logging;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * A quiet logger that only counts the invocation of calls
 */
public class QuietCountingLogger extends Logger
{
    private int debugCalledCount = 0;
    private int infoCalledCount = 0;

    public static QuietCountingLogger create(String name)
    {
        // Modifying the logger name, to avoid hitting the cache.
        QuietCountingLogger quietCountingLogger = (QuietCountingLogger) LogManager.getLogger("QuietCountingLogger:" +
                name, new LoggerFactory()
        {
            @Override
            public QuietCountingLogger makeNewLoggerInstance(String name)
            {
                return new QuietCountingLogger(name);
            }
        });
        quietCountingLogger.resetCount();
        return quietCountingLogger;
    }

    /**
     * Always call this constructor from a {@link org.apache.log4j.spi.LoggerFactory}:
     * http://stackoverflow.com/questions/15480266/nullpointerexception-when-extending-log4j-logger-class
     */
    private QuietCountingLogger(final String s)
    {
        super(s);
        repository = LogManager.getLoggerRepository();
    }

    private void resetCount()
    {
        debugCalledCount = 0;
        infoCalledCount = 0;
    }

    @Override
    public void debug(final Object object, final Throwable throwable)
    {
        debugCalledCount++;
    }

    @Override
    public void debug(final Object object)
    {
        debugCalledCount++;
    }

    @Override
    public void info(final Object object)
    {
        infoCalledCount++;
    }

    @Override
    public void info(final Object object, final Throwable throwable)
    {
        infoCalledCount++;
    }

    public int getDebugCalledCount()
    {
        return debugCalledCount;
    }

    public int getInfoCalledCount()
    {
        return infoCalledCount;
    }

    public long getCount()
    {
        return getDebugCalledCount() + getInfoCalledCount();
    }
}
