package com.atlassian.jira.functest.framework;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * JUnit3 test listener wrapping the web test listener.
 *
 * @since v4.4
 */
public class JUnit3WebTestListener implements TestListener
{

    private final WebTestListener listener;

    public JUnit3WebTestListener(WebTestListener listener)
    {
        this.listener = notNull(listener);
    }

    @Override
    public void startTest(Test test)
    {
        listener.testStarted(new JUnit3WebTestDescription(test));
    }

    @Override
    public void endTest(Test test)
    {
        listener.testFinished(new JUnit3WebTestDescription(test));
    }

    @Override
    public void addError(Test test, Throwable t)
    {
        listener.testError(new JUnit3WebTestDescription(test), t);
    }

    @Override
    public void addFailure(Test test, AssertionFailedError t)
    {
        listener.testFailure(new JUnit3WebTestDescription(test), t);
    }
}
