package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests behaviour of the Screens page in case of invalid workflows.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS })
public class TestFieldScreensInvalidWorkflow extends FuncTestCase
{
    private static final String SCREEN_TABLE_LOCATOR = "//table[@class='gridBox']";

    public void testViewFieldScreensWithInvalidWorkflow()
    {
        administration.restoreData("InvalidWorkflow.xml");
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.viewFieldScreens().goTo();
        assertTransitionWithoutStepsNotVisible();
    }

    private void assertTransitionsWithStepsVisible()
    {
        assertions.getTextAssertions().assertTextPresent(xpath(SCREEN_TABLE_LOCATOR), "(Close Issue)");
        assertions.getTextAssertions().assertTextPresent(xpath(SCREEN_TABLE_LOCATOR), "(Resolve Issue)");
        assertions.getTextAssertions().assertTextPresent(xpath(SCREEN_TABLE_LOCATOR), "(This task is done!)");
        assertions.getTextAssertions().assertTextPresent(xpath(SCREEN_TABLE_LOCATOR), "(Reopen Issue)");
    }

    private void assertTransitionWithoutStepsNotVisible()
    {
        assertions.getTextAssertions().assertTextNotPresent(xpath(SCREEN_TABLE_LOCATOR), "(Try Again!)");
    }
}
