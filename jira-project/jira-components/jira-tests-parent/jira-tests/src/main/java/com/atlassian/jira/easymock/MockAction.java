package com.atlassian.jira.easymock;

/**
 * Interface for mock actions.
 *
 * @since v4.4
 */
interface MockAction
{
    /**
     * Performs an action on the given mock object.
     *
     * @param mock the @Mock annotation
     * @param mockClass the class/interface that is being mocked
     * @param mockObject the mock object
     */
    void doWithMock(Mock mock, Class<?> mockClass, Object mockObject);
}
