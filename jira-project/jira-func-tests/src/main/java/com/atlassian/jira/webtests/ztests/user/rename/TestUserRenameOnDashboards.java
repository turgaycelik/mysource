package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;

/**
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.PROJECTS})
public class TestUserRenameOnDashboards extends FuncTestCase
{

    public static final String CHANGE_OWNERSHIP_OF_DEVELOPERS_DASHBOARD_URL = "secure/admin/dashboards/ChangeSharedDashboardOwner!default.jspa?dashboardId=10014&returnUrl=ViewSharedDashboards.jspa";
    public static final String CHANGE_OWNERSHIP_OF_FREDS_DASHBOARD_URL = "secure/admin/dashboards/ChangeSharedDashboardOwner!default.jspa?dashboardId=10019&returnUrl=ViewSharedDashboards.jspa";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestSharedDashboards.xml");
    }

    public void testDashboardsSharedWithRenamedUser()
    {
        // Set up renamed users
        renameUser("anotherdev", "someotherdev");
        backdoor.usersAndGroups().addUser("anotherdev");
        // Delete user role members so that developers are the only ones with project roles
        backdoor.projectRole().deleteGroup("HSP", "Users", "jira-users");
        backdoor.projectRole().deleteGroup("MKY", "Users", "jira-users");

        navigation.login("someotherdev", "anotherdev");
        // Ensure dashboards shared with dev and dev roles are still visible
        navigation.dashboard().navigateToPopular();
        // Shared with Homosapiens developers
        tester.assertElementPresent("pp_10016");
        // Shared with jira-developers group
        tester.assertElementPresent("pp_10015");

        navigation.login("anotherdev");
        // Check that none of the above dashboard share permissions were inherited by this recycled user
        navigation.dashboard().navigateToPopular();
        tester.assertElementNotPresent("pp_10016");
        tester.assertElementNotPresent("pp_10015");
    }

    public void testRenamedUserRetainsDashboardOwnership()
    {
        renameUser("developer", "code monkey");
        renameUser("fred", "developer");

        navigation.login("code monkey","developer");

        // Check "My dashboards" for renamed user stay the same
        navigation.dashboard().navigateToMy();
        tester.assertElementPresent("pp_10012");
        tester.assertElementPresent("pp_10016");

        // Check popular dashboards correctly reflect renamed user's ownership
        navigation.dashboard().navigateToPopular();
        text.assertTextPresent(locator.css("#pp_10013 > .cell-type-user"), "Developer");
        text.assertTextPresent(locator.css("#pp_10019 > .cell-type-user"), "Fred Normal");

        // Check favourite dashboards correctly reflect renamed user's ownership
        navigation.dashboard().navigateToFavourites();
        text.assertTextPresent(locator.css("#pp_10014 > .cell-type-user"), "Developer");

        navigation.login("developer", "fred");
        navigation.dashboard().navigateToFavourites();
        text.assertTextPresent(locator.css("#pp_10019 > .cell-type-user"), "Fred Normal");

        // Check searching by renamed user name finds that user's dashboards
        navigation.login("admin");
        navigation.dashboard().navigateToSearch();
        tester.setFormElement("searchOwnerUserName", "code monkey");
        tester.submit("Search");
        text.assertTextPresent(locator.css("#pp_10013 > .cell-type-user"), "Developer");
        text.assertTextPresent(locator.css("#pp_10014 > .cell-type-user"), "Developer");

        // Check searching by recycled user name finds that user's dashboards
        tester.setFormElement("searchOwnerUserName", "developer");
        tester.submit("Search");
        text.assertTextPresent(locator.css("#pp_10018 > .cell-type-user"), "Fred Normal");
        text.assertTextPresent(locator.css("#pp_10019 > .cell-type-user"), "Fred Normal");
        tester.submit("Search");

        // Make sure the search doesn't turn up dashboards visible to the previous owner of the name
        tester.assertElementNotPresent("pp_10013");
    }

    public void testOwnershipChangeToRenamedUser()
    {
        renameUser("developer", "code monkey");
        renameUser("fred", "developer");
        navigation.gotoPage(CHANGE_OWNERSHIP_OF_DEVELOPERS_DASHBOARD_URL);
        FormParameterUtil form = new FormParameterUtil(tester, "change-owner-form-10014", "ChangeOwner");
        form.addOptionToHtmlSelect("owner", new String[]{"developer","developer"});
        form.setFormElement("owner","developer");
        form.submitForm();
        navigation.gotoPage("secure/admin/dashboards/ViewSharedDashboards.jspa");
        text.assertTextPresent(locator.css("#pp_10014 > .cell-type-user"), "Fred Normal");

        navigation.gotoPage(CHANGE_OWNERSHIP_OF_FREDS_DASHBOARD_URL);
        form = new FormParameterUtil(tester, "change-owner-form-10019", "ChangeOwner");
        form.addOptionToHtmlSelect("owner", new String[]{"code monkey","code monkey"});
        form.setFormElement("owner", "code monkey");
        form.submitForm();
        navigation.gotoPage("secure/admin/dashboards/ViewSharedDashboards.jspa");
        text.assertTextPresent(locator.css("#pp_10019 > .cell-type-user"), "Developer");
    }

    private void renameUser(String from, String to)
    {
        navigation.gotoPage(String.format("secure/admin/user/EditUser!default.jspa?editName=%s", from));
        tester.setFormElement("username", to);
        tester.submit("Update");
    }
}
