package com.atlassian.jira.functest.framework.navigator;

import net.sourceforge.jwebunit.WebTester;
import org.jsoup.nodes.Document;

/**
 * Represents a condition in a Navigator Search.
 *
 * @since v3.13
 */
public interface NavigatorCondition
{

    /**
     * Get the condition as configured on the web page. The tester will be on the Navigator page when called.
     *
     * @param tester the tester pointed at the Navigator page.
     */
    void parseCondition(WebTester tester);

    /**
     * Assert that the condition is corrected configured for the passed tester. The tester must be on the Navigator
     * page when called.
     *
     * @param document the tester pointed at the Navigator page.
     */
    void assertSettings(Document document);

    /**
     * @return a copy of the condition.
     */
    NavigatorCondition copyCondition();

    /**
     * @return a clean copy of the condition.
     */
    NavigatorCondition copyConditionForParse();
}
