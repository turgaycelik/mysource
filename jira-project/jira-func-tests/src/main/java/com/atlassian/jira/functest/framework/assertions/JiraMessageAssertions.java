package com.atlassian.jira.functest.framework.assertions;

/**
 * Assertions for common messages displayed
 *
 * @since v4.3
 */
public interface JiraMessageAssertions
{
    /**
     * Assertion about expected title.
     *
     * @param expectedTitle expected title
     */
    void assertHasTitle(String expectedTitle);

    void assertHasMessage(String expectedMsg);


}
