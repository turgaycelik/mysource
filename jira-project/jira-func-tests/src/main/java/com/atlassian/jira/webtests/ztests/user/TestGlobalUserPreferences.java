package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test case to verify that the user counts are correct when updating global user preferences.
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestGlobalUserPreferences extends FuncTestCase
{
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestGlobalUserPreferences.xml");
    }

    //Update everyone to html
    public void testUpdateEmailMimeTypeToHtmlThenText()
    {
        navigation.gotoAdmin();
        //update the users to HTML
        tester.clickLink("user_defaults");
        tester.assertTextPresent("html");
        tester.clickLinkWithText("Apply");

        tester.assertTextPresent("receive 'text' email to receive 'html' email instead");
        tester.assertTextPresent("A total of 1 user");
        tester.submit("Update");

        //check that the users were updated.
        tester.clickLinkWithText("Apply");
        tester.assertTextPresent("A total of 0 users");
    }

    public void testUpdateEmailMimeTypeToTextThenHtml()
    {
        navigation.gotoAdmin();
        //update the users to HTML
        tester.clickLink("user_defaults");
        tester.clickLinkWithText("Edit default values");
        tester.selectOption("preference", "text");
        tester.submit("Update");

        tester.assertTextPresent("text");
        tester.clickLinkWithText("Apply");

        tester.assertTextPresent("receive 'html' email to receive 'text' email instead");
        tester.assertTextPresent("A total of 1 user");
        tester.submit("Update");

        //check that the users were updated.
        tester.clickLinkWithText("Apply");
        tester.assertTextPresent("A total of 0 users");
    }
}
