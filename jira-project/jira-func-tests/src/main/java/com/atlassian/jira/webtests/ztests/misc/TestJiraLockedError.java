package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Verify that going to the JiraLockedError page when JIRA is up and functional takes you to a useful page
 * instead of getting a useless 404 from the NoopServlet
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.SETUP })
public class TestJiraLockedError extends FuncTestCase
{
    // JRA-18822 show a better page when JIRA is actually functional
    public void testJiraLockedError() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoPage("/JiraLockedError");
        // check for the expected informative messages
        text.assertTextPresent(new XPathLocator(tester, "//h1"), "JIRA Startup Succeeded");
        text.assertTextPresent(locator.id("noerrors"), "No startup errors detected at present.");
        // make sure there is a link back to the dashboard
        tester.assertLinkPresent("context-path");
    }
}
