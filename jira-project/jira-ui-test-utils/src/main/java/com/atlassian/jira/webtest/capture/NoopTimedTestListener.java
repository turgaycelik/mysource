package com.atlassian.jira.webtest.capture;

import com.atlassian.jira.functest.framework.WebTestDescription;

/**
 * A {@link com.atlassian.jira.webtest.capture.TimedTestListener} that does nothing.
 *
 * @since v4.2
 */
class NoopTimedTestListener implements TimedTestListener
{
    public void start(final long clockMs)
    {
    }

    public void startTest(final WebTestDescription test, final long clockMs)
    {
    }

    public void addError(final WebTestDescription test, final Throwable t, final long clockMs)
    {
    }

    public void addFailure(final WebTestDescription test, final Throwable t, final long clockMs)
    {
    }

    public void endTest(final WebTestDescription test, final long clockMs)
    {
    }

    public void close(final long clockMs)
    {
    }
}
