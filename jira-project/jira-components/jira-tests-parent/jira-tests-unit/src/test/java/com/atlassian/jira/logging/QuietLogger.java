package com.atlassian.jira.logging;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * A logger that doesn't say much
 *
 * @since v3.13
 */
public class QuietLogger extends Logger
{
    public static QuietLogger create(String name)
    {
        // Modifying the logger name, to avoid hitting the cache.
        return (QuietLogger) LogManager.getLogger("QuietLogger:" + name, new LoggerFactory()
        {
            @Override
            public QuietLogger makeNewLoggerInstance(String name)
            {
                return new QuietLogger(name);
            }
        });
    }

    /**
     * Always call this constructor from a {@link org.apache.log4j.spi.LoggerFactory}:
     * http://stackoverflow.com/questions/15480266/nullpointerexception-when-extending-log4j-logger-class
     */
    private QuietLogger(String string)
    {
        super(string);
    }

    public void error(Object object, Throwable throwable)
    {
        //shhh
    }


    public void error(Object object)
    {
        // ssshhh
    }

    public void info(Object object)
    {
        // sssh
    }
}
