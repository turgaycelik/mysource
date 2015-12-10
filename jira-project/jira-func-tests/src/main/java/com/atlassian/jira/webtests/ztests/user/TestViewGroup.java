package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static com.atlassian.jira.permission.ProjectPermissions.ADMINISTER_PROJECTS;

/**
 * Test the view group page.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.FILTERS })
public class TestViewGroup extends FuncTestCase
{
    public static final int CREATE_SHARED_FILTER = 22;
    private static final String TEST_GROUP = "test_group";

    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    /**
     * Ensure that shared group filters are reported.
     */
    public void testGroupSharedFiltersReported()
    {
        gotoViewGroup("jira-users");

        //there should be no saved filters in this group now.
        assertions.assertNodeHasText(new CssLocator(tester, "table.aui td"), "There are no Saved Filters associated with this Group.");

        administration.addGlobalPermission(CREATE_SHARED_FILTER, "jira-users");
        long adminId = Long.parseLong(backdoor.filters().createFilter("", "AdministratorFilter", ADMIN_USERNAME, "jira-users"));
        long fredId = Long.parseLong(backdoor.filters().createFilter("", "FredFilter", FRED_USERNAME, "jira-users"));

        gotoViewGroup("jira-users");
        assertions.assertNodeHasText(new CssLocator(tester, "table.aui td"), "AdministratorFilter (Owner: " + ADMIN_FULLNAME + ")");
        assertions.assertNodeHasText(new CssLocator(tester, "table.aui td"), "FredFilter (Owner: " + FRED_FULLNAME + ")");

        navigation.manageFilters().deleteFilter((int) adminId);

        gotoViewGroup("jira-users");
        assertions.assertNodeDoesNotHaveText(new CssLocator(tester, "table.aui td"), "AdministratorFilter (Owner: " + ADMIN_FULLNAME + ")");
        assertions.assertNodeHasText(new CssLocator(tester, "table.aui td"), "FredFilter (Owner: " + FRED_FULLNAME + ")");

        navigation.logout();
        navigation.login(FRED_USERNAME);

        navigation.manageFilters().deleteFilter((int) fredId);

        navigation.logout();
        navigation.login(ADMIN_USERNAME);

        gotoViewGroup("jira-users");
        assertions.assertNodeDoesNotHaveText(new CssLocator(tester, "table.aui td"), "AdministratorFilter (Owner: " + ADMIN_FULLNAME + ")");
        assertions.assertNodeDoesNotHaveText(new CssLocator(tester, "table.aui td"), "FredFilter (Owner: " + FRED_FULLNAME + ")");

        assertions.assertNodeHasText(new CssLocator(tester, "table.aui td"), "There are no Saved Filters associated with this Group.");
    }

    /**
     * Test for JRA-15837
     */
    public void testViewSchemes()
    {
        // add a test_group
        navigation.gotoAdmin();
        tester.clickLink("group_browser");
        tester.setFormElement("addName", TEST_GROUP);
        tester.submit("add_group");
        tester.clickLinkWithText(TEST_GROUP);
        text.assertTextPresent(locator.page(), "There are no Permission Schemes associated with this Group.");
        text.assertTextPresent(locator.page(), "There are no Notification Schemes associated with this Group.");
        text.assertTextPresent(locator.page(), "There are no Saved Filters associated with this Group.");
        text.assertTextPresent(locator.page(), "There are no Issue Security Schemes associated with this Group.");

        // now add this group to various schemes and then check that its there

        // do permission schemes first
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLink("0_edit");
        tester.clickLink("add_perm_" + ADMINISTER_PROJECTS.permissionKey());
        tester.checkCheckbox("type", "group");
        tester.selectOption("group", TEST_GROUP);
        tester.submit(" Add ");

        // do notification schemes next
        navigation.gotoAdmin();
        tester.clickLink("notification_schemes");
        tester.clickLink("10000_edit");
        tester.clickLink("add_1");
        tester.checkCheckbox("type", "Group_Dropdown");
        tester.selectOption("Group_Dropdown", TEST_GROUP);
        tester.submit("Add");

        // do issue security next
        navigation.gotoAdmin();
        tester.clickLink("security_schemes");
        // Click Link 'Add Issue Security Scheme' (id='add_securityscheme').
        tester.clickLink("add_securityscheme");
        tester.setFormElement("name", "Test Issue Security Scheme");
        tester.submit("Add");
        tester.clickLinkWithText("Security Levels");
        tester.setFormElement("name", "Code Red");
        tester.submit("Add Security Level");
        tester.setFormElement("name", "");
        // Click Link 'Add' (id='add_Code Red').
        tester.clickLink("add_Code Red");
        tester.checkCheckbox("type", "group");
        // Select 'test_group' from select box 'group'.
        tester.selectOption("group", TEST_GROUP);
        tester.submit(" Add ");

        // now assert they are present
        gotoViewGroup(TEST_GROUP);
        text.assertTextPresent(locator.page(), "Default Permission Scheme");
        text.assertTextPresent(locator.page(), "Default Notification Scheme");
        text.assertTextPresent(locator.page(), "Test Issue Security Scheme");

        // now make sure its doesn't cache

        navigation.gotoAdmin();

        // Click Link 'Notification Schemes' (id='notification_schemes').
        tester.clickLink("notification_schemes");
        // Click Link 'Notifications' (id='10000_edit').
        tester.clickLink("10000_edit");
        // Click Link 'Delete' (id='del_10060').
        tester.clickLink("del_10160");
        tester.submit("Delete");

        // Click Link 'Permission Schemes' (id='permission_schemes').
        tester.clickLink("permission_schemes");
        // Click Link 'Permissions' (id='0_edit').
        tester.clickLink("0_edit");
        tester.clickLink("del_perm_" + ADMINISTER_PROJECTS.permissionKey() + "_test_group");
        tester.submit("Delete");

        // issue security schemes next
        // Click Link 'Issue Security Schemes' (id='security_schemes').
        tester.clickLink("security_schemes");
        // Click Link 'Delete' (id='del_Test Issue Security Scheme').
        tester.clickLink("del_Test Issue Security Scheme");
        tester.submit("Delete");

        // should now be removed and not cached
        gotoViewGroup(TEST_GROUP);
        text.assertTextPresent(locator.page(), "There are no Permission Schemes associated with this Group.");
        text.assertTextPresent(locator.page(), "There are no Notification Schemes associated with this Group.");
        text.assertTextPresent(locator.page(), "There are no Saved Filters associated with this Group.");
        text.assertTextPresent(locator.page(), "There are no Issue Security Schemes associated with this Group.");
    }

    private void gotoViewGroup(final String group)
    {
        tester.gotoPage("secure/admin/user/ViewGroup.jspa?name=" + group);
        assertions.assertNodeHasText(new CssLocator(tester, ".aui-page-header-main .aui-nav-breadcrumbs a"), "Groups");
        assertions.assertNodeHasText(new CssLocator(tester, ".aui-page-header-main h2"), group);
    }
}

