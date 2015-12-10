package com.atlassian.jira.functest.framework.navigator;

import net.sourceforge.jwebunit.WebTester;

/**
 * A way for specifying conditions on which to assert the state of the issue navigator results.
 *
 * @since v4.0
 */
public interface SearchResultsCondition
{
    /**
     * Executes assertions for this condition on the search results by inspecting the state of the latest response. It
     * is assumed that the search has already been executed, and we are currently on the page which should contain the
     * search results.
     *
     * @param tester the web tester
     */
    void assertCondition(WebTester tester);
}
