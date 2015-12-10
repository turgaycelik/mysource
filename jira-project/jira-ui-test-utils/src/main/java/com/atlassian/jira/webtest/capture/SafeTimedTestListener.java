package com.atlassian.jira.webtest.capture;

import com.atlassian.jira.functest.framework.WebTestDescription;
import org.apache.log4j.Logger;

/**
 * TimedTestListener that makes sure errors of a wrapped listener do not propagate up the stack.
 *
 * @since v4.2
 */
class SafeTimedTestListener implements TimedTestListener
{
    private static final Logger log = Logger.getLogger(SafeTimedTestListener.class);
    
    private final TimedTestListener delegate;

    SafeTimedTestListener(final TimedTestListener delegate) 
    {
        this.delegate = delegate;
    }

    public void start(final long clockMs)
    {
        try
        {
            delegate.start(clockMs);
        }
        catch (Exception e)
        {
            log.error("An error occurred while processing 'open' event.", e);
        }
    }

    public void startTest(final WebTestDescription test, final long clockMs)
    {
        try
        {
            delegate.startTest(test, clockMs);
        }
        catch (Exception e)
        {
            log.error("An error occurred while processing 'startTest' event.", e);
        }
    }

    public void addError(final WebTestDescription test, final Throwable t, final long clockMs)
    {
        try
        {
            delegate.addError(test, t, clockMs);
        }
        catch (Exception e)
        {
            log.error("An error occurred while processing 'addError' event.", e);
        }
    }

    public void addFailure(final WebTestDescription test, final Throwable t, final long clockMs)
    {
        try
        {
            delegate.addFailure(test, t, clockMs);
        }
        catch (Exception e)
        {
            log.error("An error occurred while processing 'addFailure' event.", e);
        }
    }

    public void endTest(final WebTestDescription test, final long clockMs)
    {
        try
        {
            delegate.endTest(test, clockMs);
        }
        catch (Exception e)
        {
            log.error("An error occurred while processing 'endTest' event.", e);
        }
    }

    public void close(final long clockMs)
    {
        try
        {
            delegate.close(clockMs);
        }
        catch (Exception e)
        {
            log.error("An error occurred while processing 'close' event.", e);
        }
    }
}
