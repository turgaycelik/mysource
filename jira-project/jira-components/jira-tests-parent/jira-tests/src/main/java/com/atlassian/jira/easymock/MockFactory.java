package com.atlassian.jira.easymock;

/**
 * Factory interface for mock creation.
 *
 * @since v4.4
 */
public interface MockFactory
{
    /**
     * Creates a mock with the given mock type and @Mock annotation.
     *
     * @param mock the @Mock annotation
     * @param mockClass the Class object for the mock
     * @return a mock
     */
    Object createMock(Mock mock, Class<?> mockClass);
}
