package com.atlassian.jira.util.log;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.util.concurrent.atomic.AtomicLong;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An Log4J logger wrapper than will only log N times.  After maxTimes number of calls
 * the logger silently discards the logging output.
 * <p/>
 * The logging is done via a delegate Logger
 * <p/>
 * You might use it a bit like this
 * <pre>
 * private static final Logger log = Logger.getLogger(MyObject.class);
 * private final Logger oneShotParseErrorLog = new NShotLogger(log,1);
 * ...
 * ...
 * if (parseErrorOnSomethingThatWeOnlywanToReportOnce == true) {
 *      oneShotParseErrorLog.log("Things seem quite screwy in your config");
 * }
 * </pre>
 *
 * Note that the logger is not static.  If the object in question is a PICO managed singleton
 * then you will want to use a non static so that the logger will log again if the PICO world is
 * torn down and brought back up again.
 *
 * @since v3.13
 */
public class NShotLogger
{
    private final Logger delegateLogger;
    private final AtomicLong callCount;
    private final long maxTimes;

    /**
     * Creates a NShotLogger that will only output log data if it has been called maxTimes or less.
     *
     * @param delegateLogger the delegate Logger that will do the actual logging
     * @param maxTimes       the maximum number of times that the logger can output logging data.
     * @thows IllegalArgumentException if the delegateLogger is null
     * @thows IllegalArgumentException if maxTimes is <= 0
     */
    public NShotLogger(final Logger delegateLogger, final int maxTimes)
    {
        notNull("delegateLogger", delegateLogger);
        if (maxTimes <= 0)
        {
            throw new IllegalArgumentException("Come on now you have to provide a sensible maxTimes  ==> " + maxTimes);
        }
        this.delegateLogger = delegateLogger;
        this.maxTimes = maxTimes;
        callCount = new AtomicLong(0);
    }

    /**
     * @return the underlying delegate Logger
     */
    public Logger getDelegateLogger()
    {
        return delegateLogger;
    }

    public void debug(final Object o)
    {
        if (callCount.getAndIncrement() < maxTimes)
        {
            delegateLogger.debug(o);
        }
    }

    public void debug(final Object o, final Throwable throwable)
    {
        if (callCount.getAndIncrement() < maxTimes)
        {
            delegateLogger.debug(o, throwable);
        }
    }

    public void error(final Object o)
    {
        if (callCount.getAndIncrement() < maxTimes)
        {
            delegateLogger.error(o);
        }
    }

    public void error(final Object o, final Throwable throwable)
    {
        if (callCount.getAndIncrement() < maxTimes)
        {
            delegateLogger.error(o, throwable);
        }
    }

    public void fatal(final Object o)
    {
        if (callCount.getAndIncrement() < maxTimes)
        {
            delegateLogger.fatal(o);
        }
    }

    public void fatal(final Object o, final Throwable throwable)
    {
        if (callCount.getAndIncrement() < maxTimes)
        {
            delegateLogger.fatal(o, throwable);
        }
    }

    public void info(final Object o)
    {
        if (callCount.getAndIncrement() < maxTimes)
        {
            delegateLogger.info(o);
        }
    }

    public void info(final Object o, final Throwable throwable)
    {
        if (callCount.getAndIncrement() < maxTimes)
        {
            delegateLogger.info(o, throwable);
        }
    }

    public boolean isDebugEnabled()
    {
        return delegateLogger.isDebugEnabled();
    }

    public boolean isInfoEnabled()
    {
        return delegateLogger.isInfoEnabled();
    }

    public boolean isEnabledFor(final Priority priority)
    {
        return delegateLogger.isEnabledFor(priority);
    }

    public void warn(final Object o)
    {
        if (callCount.getAndIncrement() < maxTimes)
        {
            delegateLogger.warn(o);
        }
    }

    public void warn(final Object o, final Throwable throwable)
    {
        if (callCount.getAndIncrement() < maxTimes)
        {
            delegateLogger.warn(o, throwable);
        }
    }

}
