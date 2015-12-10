package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.Arrays;

/**
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.PROJECTS})
public class TestUserRenameOnFilters extends FuncTestCase
{

    public static final String OWNER_XPATH = "//tr[@id='mf_%d']/td[2]/span[@data-filter-field='owner-full-name']";

    public void testFiltersShareWithRenamedUser()
    {
        administration.restoreData("TestSharedDashboards.xml");

        // Set up renamed users
        renameUser("anotherdev", "someotherdev");
        backdoor.usersAndGroups().addUser("anotherdev");
        // Delete user role members so that developers are the only ones with project roles
        backdoor.projectRole().deleteGroup("HSP", "Users", "jira-users");
        backdoor.projectRole().deleteGroup("MKY", "Users", "jira-users");

        navigation.login("someotherdev", "anotherdev");
        // Ensured filters shared with dev and dev roles are still visible
        navigation.manageFilters().popularFilters();
        // Shared with homosapiens Developers
        tester.assertElementPresent("mf_10004");
        // Shared with jira-developers group
        tester.assertElementPresent("mf_10006");

        navigation.login("anotherdev");
        // Check that none of the above filter share permissions were inherited by this recycled user
        tester.assertElementNotPresent("mf_10004");
        tester.assertElementNotPresent("mf_10006");
    }

    public void testRenamedUserRetainsFilterOwnership()
    {
        administration.restoreData("sharedfilters/TestBrowseFiltersShareType.xml");
        backdoor.issues().createIssue("MKY", "I'm just here for column order testing");

        // Setup a pre-existing column preference so it can be tested after rename
        navigation.login("developer");
        backdoor.columnControl().setLoggedInUserColumns(Arrays.asList("labels"));

        navigation.manageFilters().popularFilters();
        tester.clickLink("subscribe_c");
        tester.submit("Subscribe");

        // Setup renamed and recycled users and their dashboards
        navigation.login("admin");
        administration.sharedFilters().goTo();
        administration.sharedFilters().changeFilterOwner(10046, "developer"); // groupusersa
        renameUser("developer", "code monkey");
        backdoor.usersAndGroups().addUser("developer", "developer", "Dave Loper", "devdave@example.com");
        administration.sharedFilters().goTo();
        administration.sharedFilters().changeFilterOwner(10006, "developer"); // b

        // Check "favourite", "my" and "popular" filters for renamed user
        navigation.login("code monkey", "developer");
        navigation.manageFilters().favouriteFilters();
        text.assertTextPresent(locator.xpath(String.format(OWNER_XPATH, 10080)), "developer the great and wise");
        navigation.manageFilters().popularFilters();
        text.assertTextPresent(locator.xpath(String.format(OWNER_XPATH, 10080)), "developer the great and wise");

        // Check shared filters are still visible
        tester.assertElementPresent("mf_10054");
        navigation.manageFilters().myFilters();
        tester.assertElementPresent("mf_10080");
        tester.assertElementPresent("mf_10046");
        navigation.manageFilters().searchFilters();
        //tester.setFormElement("searchOwnerUserName", "code monkey");
        tester.submit("Search");
        text.assertTextPresent(locator.xpath(String.format(OWNER_XPATH, 10080)), "developer the great and wise");

        // Check subscription is still active
        navigation.manageFilters().manageSubscriptions(10007);
        text.assertTextPresent(locator.css("table > tbody > tr > td"), "developer the great and wise");

        // Check column preference is maintained
        assertEquals("Renamed user still have the same columns",
                "labels", backdoor.columnControl().getLoggedInUserColumns().get(0).value);

        // Check renamed user can edit / delete their own filter
        navigation.gotoPage("secure/EditFilter!default.jspa?filterId=10080");
        tester.setFormElement("filterName", "I renamed this");
        tester.submit();
        navigation.manageFilters().myFilters();
        text.assertTextPresent(locator.id("mf_10080"), "I renamed this");
        navigation.manageFilters().deleteFilter(10046);
        tester.submit();
        navigation.manageFilters().myFilters();
        tester.assertElementNotPresent("mf_10046");

        // Check the REST resource identifies the renamed user as owner
        navigation.gotoResource("../rest/api/2/filter/10080");
        tester.assertTextPresent("developer the great and wise");

        // Check "favourite", "my" and "popular" filters show correct owner for recycled user
        navigation.login("developer", "developer");
        navigation.manageFilters().popularFilters();
        text.assertTextPresent(locator.id("mf_10006"), "Dave Loper");
        navigation.manageFilters().favouriteFilters();
        tester.assertElementNotPresent("mf_10080");

        // Check filters shared with the old owner of the name are not visible
        tester.assertElementNotPresent("mf_10054");
        navigation.manageFilters().myFilters();
        tester.assertElementPresent("mf_10006");
        navigation.manageFilters().searchFilters();
        //tester.setFormElement("searchOwnerUserName", "developer");
        tester.submit("Search");
        text.assertTextPresent(locator.xpath(String.format(OWNER_XPATH, 10006)), "Dave Loper");

        // Check the REST resource identifies the recycled user as owner
        navigation.gotoResource("../rest/api/2/filter/10006");
        tester.assertTextPresent("Dave Loper");

        // Check ownership and searching on shared filters admin page
        navigation.login("admin");
        administration.sharedFilters().goTo();
        tester.setFormElement("searchName", "b");
        tester.submit();
        text.assertTextPresent(locator.xpath(String.format(OWNER_XPATH, 10006)), "Dave Loper");

        tester.setFormElement("searchName", "I renamed this");
        tester.submit();
        text.assertTextPresent(locator.xpath(String.format(OWNER_XPATH, 10080)), "developer the great and wise");
    }

    private void renameUser(String from, String to)
    {
        navigation.gotoPage(String.format("secure/admin/user/EditUser!default.jspa?editName=%s", from));
        tester.setFormElement("username", to);
        tester.submit("Update");
    }
}
