package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestShareUserDefault extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestShareUserDefaults.xml");
    }
    
    public void tearDownTest()
    {
        administration.restoreBlankInstance();
    }

    public void testDefaults()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        // Check the initial state for the defaults
        navigation.gotoAdminSection("user_defaults");
        Locator locator = new TableCellLocator(tester, "view_user_defaults", 3, 0);
        text.assertTextPresent(locator, "Default sharing for filters and dashboards");
        locator = new TableCellLocator(tester, "view_user_defaults", 3, 1);
        text.assertTextPresent(locator, "Private");

        // Check the initial state for the user
        tester.gotoPage("secure/ViewProfile.jspa");
        tester.clickLink("edit_prefs_lnk");
        tester.assertRadioOptionPresent("shareDefault", "true");
        tester.assertRadioOptionPresent("shareDefault", "false");
        tester.assertRadioOptionSelected("shareDefault", "true");

        // Change the default for sharing
        navigation.gotoAdminSection("user_defaults");
        tester.clickLinkWithText("Edit default values");
        tester.setFormElement("sharePublic", "false");
        tester.submit("Update");

        // Ensure the default has changed
        locator = new TableCellLocator(tester, "view_user_defaults", 3, 0);
        text.assertTextPresent(locator, "Default sharing for filters and dashboards");
        locator = new TableCellLocator(tester, "view_user_defaults", 3, 1);
        text.assertTextPresent(locator, "Public");

        // Ensure the user's setting hasn't changed
        tester.gotoPage("secure/ViewProfile.jspa");
        tester.clickLink("edit_prefs_lnk");
        tester.assertRadioOptionPresent("shareDefault", "true");
        tester.assertRadioOptionPresent("shareDefault", "false");
        tester.assertRadioOptionSelected("shareDefault", "false");
        // Change their share option
        tester.checkCheckbox("shareDefault", "true");
        tester.submit();

        // Ensure their share option has changed
        tester.gotoPage("secure/ViewProfile.jspa");
        tester.clickLink("edit_prefs_lnk");
        tester.assertRadioOptionPresent("shareDefault", "true");
        tester.assertRadioOptionPresent("shareDefault", "false");
        tester.assertRadioOptionSelected("shareDefault", "true");

        // Ensure changing their settings hasn't affected the global default
        navigation.gotoAdminSection("user_defaults");
        locator = new TableCellLocator(tester, "view_user_defaults", 3, 0);
        text.assertTextPresent(locator, "Default sharing for filters and dashboards");
        locator = new TableCellLocator(tester, "view_user_defaults", 3, 1);
        text.assertTextPresent(locator, "Public");
    }
}
