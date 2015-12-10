package com.atlassian.jira.functest.framework;

/**
 * Abstract, no-op implementation of {@link com.atlassian.jira.functest.framework.WebTestListener}. Subclasses
 * only need to override required methods.
 *
 * @since v4.4
 */
public abstract class AbstractWebTestListener implements WebTestListener
{
    @Override
    public void suiteStarted(WebTestDescription suiteDescription)
    {
    }

    @Override
    public void suiteFinished(WebTestDescription suiteDescription)
    {
    }

    @Override
    public void testStarted(WebTestDescription description)
    {
    }

    @Override
    public void testFinished(WebTestDescription description)
    {
    }

    @Override
    public void testError(WebTestDescription description, Throwable error)
    {
    }

    @Override
    public void testFailure(WebTestDescription description, Throwable failure)
    {
    }
}
