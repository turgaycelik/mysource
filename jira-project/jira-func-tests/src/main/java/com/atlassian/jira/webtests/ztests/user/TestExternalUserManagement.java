package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.junit.Ignore;
import org.xml.sax.SAXException;

/**
 * Test case to test that certain behaviour is disabled in JIRA if we enable external usermanagment.
 * The following actions should not be possible:
 * <ul>
 * <li>AddUser
 * <li>EditUserGroups
 * <li>GroupBrowser
 * <li>DeleteGroup
 * <li>DeleteUser
 * </ul>
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestExternalUserManagement extends JIRAWebTest
{
    private static final String USERNAME_JOHN = "john";
    private static final String NAME_JOHN_WAYNE = "John Wayne";
    private static final int GROUP_COL = 3;

    public TestExternalUserManagement(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only")
    public void testAddUser()
    {
        gotoAdmin();
        // First lets check everything is present as it should be.
        clickLink("user_browser");
        //check that the add user link is present
        assertLinkPresent("create_user");
        //Now lets add a user and make sure it works
        clickLink("create_user");
        setFormElement("username", USERNAME_JOHN);
        setFormElement("password", "some_pass");
        setFormElement("confirm", "some_pass");
        setFormElement("fullname", NAME_JOHN_WAYNE);
        setFormElement("email", "john@atlassian.com");
        submit("Create");

        assertTextPresent("User: " + NAME_JOHN_WAYNE);

        // Now check the case that all is not seen
        toggleExternalUserManagement(true);
        //check that the add user link is present
        assertLinkNotPresent("create_user");
        gotoPage("/secure/admin/user/AddUser!default.jspa");
        submit("Create");
        assertTextPresent("Cannot add user, as external user management is enabled, please contact your JIRA administrators.");
    }

    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only")
    public void testEditUserGroups() throws SAXException
    {
        gotoAdmin();
        //first check everything works fine.  We remove the admin user from the jira-developers group.
        clickLink("user_browser");
        WebTable userTable = getDialog().getResponse().getTableWithID("user_browser_table");
        assertTrue(tableCellHasText(userTable, 1, GROUP_COL, "jira-developers"));
        assertTextPresent("jira-developers");
        assertLinkPresentWithText("Groups");
        clickLink("editgroups_admin");
        selectMultiOptionByValue("groupsToLeave", "jira-developers");
        submit("leave");
        userTable = getDialog().getResponse().getTableWithID("user_browser_table");
        assertTrue(tableCellDoesNotHaveText(userTable, 1, GROUP_COL, "jira-developers"));

        toggleExternalUserManagement(true);

        gotoAdmin();
        //Now lets check the link isn't present, and we get an error message when submitting
        clickLink("user_browser");
        userTable = getDialog().getResponse().getTableWithID("user_browser_table");
        assertTrue(tableCellHasText(userTable, 1, GROUP_COL, "jira-users"));
        assertLinkNotPresent("editgroups_admin");
        gotoPage("/secure/admin/user/EditUserGroups!default.jspa?returnUrl=UserBrowser.jspa&name=admin");
        selectMultiOptionByValue("groupsToLeave", "jira-users");
        submit("leave");
        assertTextPresent("Cannot edit group memberships, as external user management is enabled, please contact your JIRA administrators.");
        //check that the user is still in the group.
        clickLink("user_browser");
        userTable = getDialog().getResponse().getTableWithID("user_browser_table");
        assertTrue(tableCellHasText(userTable, 1, GROUP_COL, "jira-users"));
    }

    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only"
            + "JRADEV-18305 Needs to be converted to WebDriver because the Multi User Picker can only be interacted with via JS")
    public void testBulkEditUserGroups() throws SAXException
    {
        gotoAdmin();
        //first check everything works fine.  We lets add fred to the admin group.
        clickLink("group_browser");
        WebTable userTable = getDialog().getResponse().getTableWithID("group_browser_table");
        assertTrue(tableCellHasText(userTable, 1, 1, "1"));
        clickLink("edit_members_of_jira-administrators");
        setFormElement("usersToAssignStr", FRED_USERNAME);
        submit("assign");
        clickLink("group_browser");
        userTable = getDialog().getResponse().getTableWithID("group_browser_table");
        assertTrue(tableCellHasText(userTable, 1, 1, "2"));

        toggleExternalUserManagement(true);

        gotoAdmin();
        //Now lets check the link isn't present, and we get an error message when submitting
        clickLink("group_browser");
        userTable = getDialog().getResponse().getTableWithID("group_browser_table");
        assertTrue(tableCellHasText(userTable, 2, 1, "1"));
        //lets try to assign fred to the developers group
        assertLinkNotPresent("edit_members_of_jira-developers");
        gotoPage("/secure/admin/user/BulkEditUserGroups!default.jspa?selectedGroupsStr=jira-developers");
        setFormElement("usersToAssignStr", FRED_USERNAME);
        submit("assign");
        // should fail with external mgmnt error
        assertTextPresent("Cannot edit group memberships, as external user management is enabled, please contact your JIRA administrators.");

        // Try to remove admin from dev
        gotoPage("/secure/admin/user/BulkEditUserGroups!default.jspa?selectedGroupsStr=jira-developers");
        selectOption("usersToUnassign", ADMIN_USERNAME);
        submit("unassign");
        // should fail with external mgmnt error
        assertTextPresent("Cannot edit group memberships, as external user management is enabled, please contact your JIRA administrators.");

        clickLink("group_browser");
        //check that the original table still contains the same number of users for developers.
        userTable = getDialog().getResponse().getTableWithID("group_browser_table");
        assertTrue(tableCellHasText(userTable, 2, 1, "1"));
    }

    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only")
    public void testGroupEditPage() throws SAXException
    {
        gotoAdmin();
        clickLink("group_browser");

        // Check the case where it is disabled and all should be seen
        // Just make sure all the stuff we should see is there
        assertTextPresent("You can also add and remove groups from here.");
        // Make sure we can see the add group box
        assertElementPresent("add-group");
        // Make sure we can see the bulk edit link
        assertLinkPresent("bulk_edit_groups");
        // Make sure the operations column in the group browser is not present
        WebTable groupBrowserTable = getDialog().getResponse().getTableWithID("group_browser_table");
        assertEquals(4, groupBrowserTable.getColumnCount());
        assertEquals(4, groupBrowserTable.getRowCount());
        setFormElement("addName", "test-new-group");
        submit("add_group");
        assertTextPresent("test-new-group");
        groupBrowserTable = getDialog().getResponse().getTableWithID("group_browser_table");
        assertEquals(5, groupBrowserTable.getRowCount());

        // Now check the case that all is not seen
        toggleExternalUserManagement(true);

        clickLink("group_browser");

        // Just make sure all the stuff we should not see is not there
        assertTextNotPresent("You can also add and remove groups from here.");
        // Make sure we can't see the add group box
        assertElementNotPresent("add-group");
        // Make sure we can't see the bulk edit link
        assertLinkNotPresent("bulk_edit_groups");
        gotoPage(page.addXsrfToken("/secure/admin/user/GroupBrowser!add.jspa?addName=another-test-group"));
        assertTextPresent("Cannot add groups, as external user management is enabled, please contact your JIRA administrators.");
        // Make sure the operations column in the group browser is not present
        groupBrowserTable = getDialog().getResponse().getTableWithID("group_browser_table");
        assertEquals(3, groupBrowserTable.getColumnCount());
        assertEquals(5, groupBrowserTable.getRowCount());
    }

    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only")
    public void testDeleteGroup() throws SAXException
    {
        //first test successful deletion:
        gotoAdmin();
        clickLink("group_browser");
        // Make sure the correct number of rows int the group browser is present
        WebTable groupBrowserTable = getDialog().getResponse().getTableWithID("group_browser_table");
        assertEquals(4, groupBrowserTable.getRowCount());
        assertLinkPresent("del_jira-users");
        clickLink("del_jira-users");
        submit("Delete");
        // Make sure the correct number of rows int the group browser is present
        groupBrowserTable = getDialog().getResponse().getTableWithID("group_browser_table");
        assertEquals(3, groupBrowserTable.getRowCount());

        toggleExternalUserManagement(true);

        gotoAdmin();
        clickLink("group_browser");
        // Make sure the correct number of rows int the group browser is present
        groupBrowserTable = getDialog().getResponse().getTableWithID("group_browser_table");
        assertEquals(3, groupBrowserTable.getRowCount());
        assertLinkNotPresent("del_jira-developers");
        //try to delete a group anyways
        gotoPage("/secure/admin/user/DeleteGroup!default.jspa?name=jira-developers");
        submit("Delete");
        assertTextPresent("Cannot delete group, as external user management is enabled, please contact your JIRA administrators.");
        //now lets check we still have the same number of groups.
        gotoAdmin();
        clickLink("group_browser");
        groupBrowserTable = getDialog().getResponse().getTableWithID("group_browser_table");
        assertEquals(3, groupBrowserTable.getRowCount());
    }

    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only")
    public void testDeleteUser() throws SAXException
    {
        //check everything works fine first.
        gotoAdmin();
        addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        clickLink("user_browser");
         // Make sure the correct number of rows int the group browser is present
        WebTable userBrowserTable = getDialog().getResponse().getTableWithID("user_browser_table");
        assertEquals(4, userBrowserTable.getRowCount());
        assertLinkPresent("deleteuser_link_fred");
        clickLink("deleteuser_link_fred");
        submit("Delete");
        userBrowserTable = getDialog().getResponse().getTableWithID("user_browser_table");
        assertEquals(3, userBrowserTable.getRowCount());

        toggleExternalUserManagement(true);
        //now check links aren't there and deleteing a user doesn't work
        gotoAdmin();
        clickLink("user_browser");
        assertLinkNotPresent("deleteuser_link_bob");
        gotoPage("/secure/admin/user/DeleteUser!default.jspa?returnUrl=UserBrowser.jspa&name=bob");
        submit("Delete");
        assertTextPresent("Cannot delete user, as external user management is enabled, please contact your JIRA administrators.");
        gotoAdmin();
        clickLink("user_browser");
        //check noone was deleted.
        userBrowserTable = getDialog().getResponse().getTableWithID("user_browser_table");
        assertEquals(3, userBrowserTable.getRowCount());

        
    }

    public void testSystemInfo()
    {
        gotoPage("/secure/admin/jira/ViewSystemInfo.jspa");
        assertTextPresent("External user management");
        assertTextPresentAfterText("OFF", "External user management");

        assertTextNotPresent("External user management is enabled. Showing records from local database tables only.");

        toggleExternalUserManagement(true);

        gotoPage("/secure/admin/jira/ViewSystemInfo.jspa");
        assertTextPresent("External user management");
        assertTextPresentAfterText("ON", "External user management");

        assertTextPresent("External user management is enabled. Showing records from local database tables only.");
    }

    public void testSignupLinkOnLoginPage()
    {
        try
        {
            toggleExternalUserManagement(true);
            logout();
            gotoPage("login.jsp");
            assertLinkNotPresent("signup");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

}
