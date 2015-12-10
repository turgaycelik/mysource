package com.atlassian.jira.webtest.capture;

import com.atlassian.jira.functest.framework.WebTestDescription;

/**
 * Observer of tests and when they occurred relative to the start of the test run.
 *
 * @since v4.2
 */
interface TimedTestListener
{
    void start(long clockMs);

    void startTest(WebTestDescription test, long clockMs);

    public void addError(WebTestDescription test, Throwable t, long clockMs);

    public void addFailure(WebTestDescription test, Throwable t, long clockMs);

    public void endTest(WebTestDescription test, long clockMs);

    void close(long clockMs);
}
