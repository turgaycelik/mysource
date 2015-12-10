package com.atlassian.jira.functest.framework;

import com.atlassian.jira.webtests.util.EnvironmentAware;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Class to wrap a test so that a {@link WebTestListener} can listen to a
 * test.
 *
 * @since v4.2
 */
public class SuiteListenerWrapper implements Test, EnvironmentAware
{
    private final Test delegate;
    private final WebTestDescription suiteDescription;
    private final WebTestListener listener;
    private final TestListener junit3Delegate;

    public SuiteListenerWrapper(final Test delegate, final WebTestListener listener)
    {
        this.delegate = notNull(delegate);
        this.suiteDescription = new JUnit3WebTestDescription(delegate);
        this.listener = notNull(listener);
        this.junit3Delegate = new JUnit3WebTestListener(listener);
    }


    public Test delegate()
    {
        return delegate;
    }

    public int countTestCases()
    {
        return delegate.countTestCases();
    }

    public void run(final TestResult testResult)
    {
        listener.suiteStarted(suiteDescription);
        try
        {
            testResult.addListener(junit3Delegate);
            delegate.run(testResult);
        }
        finally
        {
            testResult.removeListener(junit3Delegate);
            listener.suiteFinished(suiteDescription);
        }
    }

    @Override
    public void setEnvironmentData(JIRAEnvironmentData environmentData)
    {
        if (delegate instanceof EnvironmentAware)
        {
            ((EnvironmentAware) delegate).setEnvironmentData(environmentData);
        }
    }
}
