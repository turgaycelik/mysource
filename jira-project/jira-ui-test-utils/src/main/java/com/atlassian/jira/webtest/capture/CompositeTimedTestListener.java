package com.atlassian.jira.webtest.capture;

import com.atlassian.jira.functest.framework.WebTestDescription;
import com.atlassian.jira.util.collect.CollectionUtil;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Composite that allows multiple {@link com.atlassian.jira.webtest.capture.TimedTestListener} to look like
 * one.
 *
 * @since v4.2
 */
class CompositeTimedTestListener implements TimedTestListener
{
    private static final Logger log = Logger.getLogger(CompositeTimedTestListener.class);

    private final Collection<TimedTestListener> listeners;

    CompositeTimedTestListener(final Collection<? extends TimedTestListener> listeners) 
    {
        this.listeners = CollectionUtil.copyAsImmutableList(listeners);
    }

    public void start(final long clockMs)
    {
        for (TimedTestListener listener : listeners)
        {
            try
            {
                listener.start(clockMs);
            }
            catch (Exception e)
            {
                log.error("Error occured while processing 'open' command.", e);
            }
        }
    }

    public void startTest(final WebTestDescription test, final long clockMs)
    {
        for (TimedTestListener listener : listeners)
        {
            try
            {
                listener.startTest(test, clockMs);
            }
            catch (Exception e)
            {
                log.error("Error occured while processing 'startTest' command.", e);
            }
        }
    }

    public void addError(final WebTestDescription test, final Throwable t, final long clockMs)
    {
        for (TimedTestListener listener : listeners)
        {
            try
            {
                listener.addError(test, t, clockMs);
            }
            catch (Exception e)
            {
                log.error("Error occured while processing 'addError' command.", e);
            }
        }
    }

    public void addFailure(final WebTestDescription test, final Throwable t, final long clockMs)
    {
        for (TimedTestListener listener : listeners)
        {
            try
            {
                listener.addFailure(test, t, clockMs);
            }
            catch (Exception e)
            {
                log.error("Error occured while processing 'addFailure' command.", e);
            }
        }
    }

    public void endTest(final WebTestDescription test, final long clockMs)
    {
        for (TimedTestListener listener : listeners)
        {
            try
            {
                listener.endTest(test, clockMs);
            }
            catch (Exception e)
            {
                log.error("Error occured while processing 'endTest' command.", e);
            }
        }
    }

    public void close(final long clockMs)
    {
        for (TimedTestListener listener : listeners)
        {
            try
            {
                listener.close(clockMs);
            }
            catch (Exception e)
            {
                log.error("Error occured while processing 'close' command.", e);
            }
        }
    }
}
