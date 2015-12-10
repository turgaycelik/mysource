package com.atlassian.jira.util.log;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.util.RealClock;

import com.google.common.annotations.VisibleForTesting;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
* A logger for use where there is the chance for large numbers of repetitive errors.
* This Logger will output only a limited number of stacktraces and then will only output error messages for
* {@link #warn(Object s, Throwable t)} and {@link #error(Object s, Throwable t)}
*
* @since v6.3
*/
public class RateLimitingLogger
{
    private static final int MAX_STACK_TRACES = 25;
    private static final int MAX_IDLE_TIME = 5;
    private static final AtomicBoolean warningSuppressedMessageWritten = new AtomicBoolean(false);
    private static final AtomicLong lastWarning = new AtomicLong(0);
    private static final AtomicInteger warningsLogged = new AtomicInteger(0);
    private final int maxStackTraces;
    private final long resetAfterMinutes;

    private final Clock clock;

    private final Logger delegate;

    private final String name;

    /**
     * Create a new logger with defaults for number of stacktraces (25) and time to reset (5 minutes).
     * @param clazz Class  for logger name
     */
    public RateLimitingLogger(final Class clazz)
    {
        this(clazz, MAX_STACK_TRACES, MAX_IDLE_TIME);
    }

    /**
     * Create a new logger
     * @param clazz Class  for logger name
     * @param maxStackTraces Maximum number of stacktraces to print before starting suppression.
     * @param resetAfterMinutes Number of minutes where the logger is idle to cause a reset to recommence printing stacktraces.
     */
    public RateLimitingLogger(final Class clazz, int maxStackTraces, int resetAfterMinutes)
    {
        //Have to use RealClock.getInstance() because typically use of RateLimitingLogger is in static initializer and
        //components are not set up yet when these are run
        this(Logger.getLogger(clazz), maxStackTraces, resetAfterMinutes, RealClock.getInstance());
    }

    /**
     * Create a new logger with a specific delegate.
     * @param delegate Class  for logger name
     * @param maxStackTraces Maximum number of stacktraces to print before starting suppression.
     * @param resetAfterMinutes Number of minutes where the logger is idle to cause a reset to recommence printing stacktraces.
     * @param clock the clock to use for retrieving system time.
     */
    @VisibleForTesting
    RateLimitingLogger(final Logger delegate, int maxStackTraces, int resetAfterMinutes, Clock clock)
    {
        this.delegate = delegate;
        this.name = delegate.getName();
        this.maxStackTraces = maxStackTraces;
        this.resetAfterMinutes = TimeUnit.MINUTES.toMillis(resetAfterMinutes);
        this.clock = clock;
    }

    public void trace(final Object message) {
        delegate.trace(message);
    }

    public void trace(final Object message, final Throwable t)
    {
        delegate.trace(message, t);
    }

    public boolean isTraceEnabled()
    {
        return delegate.isTraceEnabled();
    }

    public void debug(final Object message)
    {
        delegate.debug(message);
    }

    public void debug(final Object message, final Throwable t)
    {
        delegate.debug(message, t);
    }

    public void error(final Object message)
    {
        delegate.error(message);
    }

    public void error(final Object message, final Throwable t)
    {
        if (wantFullStackTrace())
        {
            delegate.error(message, t);
        }
        else
        {
            delegate.error(message);
        }
    }

    public void fatal(final Object message)
    {
        delegate.fatal(message);
    }

    public void fatal(final Object message, final Throwable t)
    {
        delegate.fatal(message, t);
    }

    public boolean isDebugEnabled()
    {
        return delegate.isDebugEnabled();
    }

    public boolean isEnabledFor(final Priority level)
    {
        return delegate.isEnabledFor(level);
    }

    public boolean isInfoEnabled()
    {
        return delegate.isInfoEnabled();
    }

    public void info(final Object message)
    {
        delegate.info(message);
    }

    public void info(final Object message, final Throwable t)
    {
        delegate.info(message, t);
    }

    public void warn(final Object message)
    {
        delegate.warn(message);
    }

    public void warn(final Object message, final Throwable t)
    {
        if (wantFullStackTrace())
        {
            delegate.warn(message, t);
        }
        else
        {
            delegate.warn(message);
        }
    }

    private boolean wantFullStackTrace()
    {
        if (isDebugEnabled())
        {
            return true;
        }

        // Check if the last full warning is too long ago and if so reset all and start again
        long systemTime = clock.getCurrentDate().getTime();
        if (systemTime - lastWarning.get() > resetAfterMinutes)
        {
            warningsLogged.set(0);
            warningSuppressedMessageWritten.set(false);
        }
        lastWarning.set(systemTime);

        if (warningsLogged.incrementAndGet() <= maxStackTraces)
        {
            return true;
        }
        if (warningSuppressedMessageWritten.compareAndSet(false, true))
        {
            delegate.warn("*******************************************************************************************************************");
            delegate.warn("Further indexing error stacktraces suppressed.");
            delegate.warn("To enable full stacktraces set logger level for '" + name + "' to 'DEBUG' ");
            delegate.warn("*******************************************************************************************************************");
        }
        return false;
    }

    @VisibleForTesting
    static void reset()
    {
        lastWarning.set(0);
    }
}
