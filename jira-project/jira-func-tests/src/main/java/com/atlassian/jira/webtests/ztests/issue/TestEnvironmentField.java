package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test for JRA-16224
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS })
public class TestEnvironmentField extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestEnvironmentField.xml");
    }

    public void testEnvironmentFieldNeverHidden() throws Exception
    {
        assertEnvironmentFieldShownOnViewIssue(true);
        backdoor.screens().removeFieldFromScreen("Default Screen", "Environment");
        assertEnvironmentFieldShownOnViewIssue(false);
    }

    public void testHideShowToggleButton() throws Exception
    {
        // Go to HSP-1
        navigation.issue().viewIssue("HSP-1");
        // The environment value is only one line, so we don't need a toggle.
        text.assertTextNotPresent(new IdLocator(tester, "environment-val"), "Hide");
        tester.assertElementNotPresent("field-environment");

        // Go to HSP-2
        navigation.issue().viewIssue("HSP-2");
        // The environment value is longer than 2 lines, so we should see the Hide/Show toggle
        text.assertTextNotPresent(new IdLocator(tester, "environment-val"), "Hide");

        navigation.issue().setEnvironment("HSP-2", "A long description that is over 255 characters that will case teh twixie to appear. A long description that is over 255 characters that will case teh twixie to appear. A long description that is over 255 characters that will case teh twixie to appear. A long description that is over 255 characters that will case teh twixie to appear.");
        navigation.issue().viewIssue("HSP-2");
        // The environment value is longer than 2 lines, so we should see the Hide/Show toggle
        text.assertTextPresent(new IdLocator(tester, "environment-val"), "Hide");
        tester.assertElementPresent("field-environment");
        // Note that there exists a Selenium test to test the javascript works. See TestHideShowEnvironmentField
    }

    private void assertEnvironmentFieldShownOnViewIssue(final boolean shouldBeOnEdit)
    {
        // Go to View Issue and assert that the Environment Field is shown
        navigation.issue().viewIssue("HSP-1");
        // Click Link 'HSP-1' (id='issue_key_HSP-1').
        // Assert the table 'issueDetailsTable'

        Locator locator = new IdLocator(tester, "environment-val");
        text.assertTextPresent(locator, "this is the Environment Field");

        // Now assert whether environment is also on Edit screen
        // Click Link 'Edit' (id='edit_issue').
        tester.clickLink("edit-issue");
        if (shouldBeOnEdit)
        {
            tester.assertTextPresent("Environment");
        }
        else
        {
            tester.assertTextNotPresent("Environment");
        }
    }

}
