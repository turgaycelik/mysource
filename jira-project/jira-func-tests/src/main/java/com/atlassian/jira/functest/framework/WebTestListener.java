package com.atlassian.jira.functest.framework;

/**
 * An observer of a {@link junit.framework.Test}.
 *
 * @since v4.2
 */
public interface WebTestListener
{
    /**
     * Called when the whole web test suite is going to start.
     *
     * @param suiteDescription description of the whole suite
     */
    void suiteStarted(WebTestDescription suiteDescription);


    /**
     * Called when the whole web test suite is going finishing.
     *
     * @param suiteDescription description of the whole suite
     */
    void suiteFinished(WebTestDescription suiteDescription);

    /**
     * Called on start of a single web test.
     *
     * @param description test description
     */
    void testStarted(WebTestDescription description);

    /**
     * Called on finished web test.
     *
     * @param description test description
     */
    void testFinished(WebTestDescription description);

    /**
     * Called when test ends with an error
     *
     * @param description test description
     * @param error error
     */
    void testError(WebTestDescription description, Throwable error);

    /**
     * Called on test finished with an assertion failure.
     *
     * @param description test description
     * @param failure failure
     */
    void testFailure(WebTestDescription description, Throwable failure);
}
