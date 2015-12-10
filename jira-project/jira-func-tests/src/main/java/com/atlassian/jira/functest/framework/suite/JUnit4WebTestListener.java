package com.atlassian.jira.functest.framework.suite;

import com.atlassian.jira.functest.framework.WebTestListener;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * JUnit4 test listener wrapping the web test listener.
 *
 * @since v4.4
 */
public class JUnit4WebTestListener extends RunListener
{

    private final WebTestListener listener;

    public JUnit4WebTestListener(WebTestListener listener)
    {
        this.listener = notNull(listener);
    }

    public WebTestListener webTestListener()
    {
        return listener;
    }

    @Override
    public void testStarted(Description description) throws Exception
    {
        listener.testStarted(new JUnit4WebTestDescription(description));
    }

    @Override
    public void testFinished(Description description) throws Exception
    {
        listener.testFinished(new JUnit4WebTestDescription(description));
    }

    @Override
    public void testFailure(Failure failure) throws Exception
    {
        listener.testFailure(new JUnit4WebTestDescription(failure.getDescription()), failure.getException());
    }

    @Override
    public void testAssumptionFailure(Failure failure)
    {
        listener.testError(new JUnit4WebTestDescription(failure.getDescription()), failure.getException());
    }

}
