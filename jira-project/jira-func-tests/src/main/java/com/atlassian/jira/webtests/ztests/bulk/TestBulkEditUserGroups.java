package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;
import org.junit.Ignore;

@Ignore ("JRADEV-18305 Needs to be converted to WebDriver because the Multi User Picker can only be interacted with via JS")
@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.USERS_AND_GROUPS })
public class TestBulkEditUserGroups extends JIRAWebTest
{
    private static final String BULK_EDIT_GROUP_MEMBERS_ERRORS_CONTAINER_LOCATOR = ".aui-message.error";
    /**
     * initial counts on the number of users per group according to 'TestBulkEditGroupMembers.xml'
     * there are admin, dev and user as 3 distinct users.
     * And an additional 401 users for testing the limits of how many users to display per group
     */
    private int adminsCount = 1;
    private int developersCount = 4;
    private int usersCount = 7;
    //two groups with more than 200+ users they share one common user (user199)
    private int group200Count = 200; //all usersX where X = {0, 1, 2 ... 199}
    private int group202Count = 202; //all usersY where Y = {199, 200, 201 ... 400}
    private static final String PERSONAL_LICENSE_LIMIT_ERROR =
            "Adding the user to the groups you have selected will grant the &#39;JIRA Users&#39; permission to the user in JIRA. "
            + "This will exceed the number of users allowed to use JIRA under your license. Please reduce the number of "
            + "users with the &#39;JIRA Users&#39;, &#39;JIRA Administrators&#39; or &#39;JIRA System Administrators&#39;"
            + " global permissions or consider upgrading your license.";

    //initialises the group counts - this must correspond to the above numbers
    public void resetGroupCounters()
    {
        adminsCount = 1;
        developersCount = 4;
        usersCount = 7;
        group200Count = 200;
        group202Count = 202;
    }

    private static final String PLEASE_REFRESH_MEMBERS_LIST = "Newly selected group(s) may have different members.";
    private static final String UNASSIGN = "unassign";
    private static final String ASSIGN = "assign";
    private static final String FIELD_USERS_TO_UNASSIGN = "usersToUnassign";
    private static final String FIELD_USERS_TO_ASSIGN = "usersToAssignStr";
    private static final String FIELD_SELECTED_GROUPS = "selectedGroupsStr";
    private static final String WARNING_TOO_MANY_USERS_TO_DISPLAY = "These group(s) have been limited to display the first 200 users only.";

    private static final String ERROR_SELECT_GROUPS = "Please select group(s) to edit";
    private static final String ERROR_SELECT_USERS_TO_REMOVE = "Please select users to remove from the selected group(s)";
    private static final String ERROR_SELECT_USERS_TO_ADD = "Please select users to add to all the selected group(s)";
    private static final String ERROR_CANNOT_ADD_USER_INVALID = "Cannot add user. &#39;invalid&#39; does not exist";
    private static final String ERROR_LEAVING_ALL_SYS_ADMIN_GROUPS = "You are trying to leave all of the system administration groups jira-administrators. You cannot delete your own system administration permission";
    private static final String ERROR_LEAVING_ALL_ADMIN_GROUPS = "You are trying to leave all of the administration groups jira-administrators. You cannot delete your own administration permission";
    private static final String ERROR_ADMIN_ALREADY_MEMBER_OF_ALL = "Cannot add user &#39;admin&#39;, user is already a member of all the selected group(s)";
    private static final String ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN = "Cannot add user &#39;admin&#39;, user is already a member of &#39;jira-administrators&#39;";

    public TestBulkEditUserGroups(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("TestBulkEditGroupMembers.xml");
        resetGroupCounters();
    }

    public void tearDown()
    {
        HttpUnitOptions.setScriptingEnabled(false);
        super.tearDown();
    }

    public void testBulkEditUserGroupsWithScriptingEnabled()
    {
        log("Testing Bulk Edit User Groups with Javascript ENABLED");
        HttpUnitOptions.setScriptingEnabled(true);
        runAllBulkEditUserGroupTests();
    }

    public void runAllBulkEditUserGroupTests()
    {
        _testBulkUnassignUsersFromGroups();
        _testBulkAssignUsersFromGroups();
        _testBulkEditUserGroupsListSize();
        _testBulkUnassignUsersFromGroupsValidation();
        _testBulkAssignUsersFromGroupsValidation();
    }

    //JRA-14495
    public void testBulkEditUserGroupsWithShortUsername()
    {
        HttpUnitOptions.setScriptingEnabled(true);
        getBackdoor().usersAndGroups().addUser("1");

        gotoBulkEditGroupMembers();

        //Remove 1 user from 1 selected group
        selectSingleGroupOnly("jira-users", 8);
        tester.setWorkingForm("jiraform");
        selectMultiOption(FIELD_USERS_TO_UNASSIGN, "1");
        tester.submit(UNASSIGN);
        tester.assertTextPresent("Selected 1 of 5 Groups");
        tester.assertTextPresent(7 + " Group Member(s)");
        //make sure the removed user is not on the list
        String[] options = tester.getDialog().getOptionValuesFor(FIELD_USERS_TO_UNASSIGN);
        for (String option : options)
        {
            assertFalse(option.equals("1______jira-users"));
        }
    }

    public void testBulkEditUserGroupsWithNoSysAdminPermRemoveLastAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            navigation.gotoAdminSection("group_browser");
            tester.clickLink("edit_members_of_jira-administrators");

            tester.selectOption("usersToUnassign", ADMIN_USERNAME);
            tester.submit("unassign");

            tester.assertTextPresent(ERROR_LEAVING_ALL_ADMIN_GROUPS);
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testBulkEditUserGroupsWithNoSysAdminPermRemoveAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            getBackdoor().usersAndGroups().addGroup("admin-group2");
            administration.addGlobalPermission(ADMINISTER, "admin-group2");
            getBackdoor().usersAndGroups().addUserToGroup(ADMIN_USERNAME, "admin-group2");

            navigation.gotoAdminSection("group_browser");
            tester.clickLink("edit_members_of_jira-administrators");

            tester.selectOption("usersToUnassign", ADMIN_USERNAME);
            tester.submit("unassign");

            tester.clickLink("view_profile");

            tester.assertTextNotPresent("jira-administrators");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testBulkEditUserGroupsInvalidGroups()
    {
        tester.gotoPage(page.addXsrfToken("/secure/admin/user/BulkEditUserGroups.jspa?selectedGroupsStr=invalid&assign=true&usersToAssignStr=admin"));
        text.assertTextPresent(locator.css(BULK_EDIT_GROUP_MEMBERS_ERRORS_CONTAINER_LOCATOR), "The group 'invalid' is not a valid group.");
        tester.gotoPage(page.addXsrfToken("/secure/admin/user/BulkEditUserGroups.jspa?selectedGroupsStr=invalid&unassign=true&usersToUnassign=admin"));
        text.assertTextPresent(locator.css(BULK_EDIT_GROUP_MEMBERS_ERRORS_CONTAINER_LOCATOR), "The group 'invalid' is not a valid group.");
    }

    public void testBulkEditUserGroupsWithNoSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.gotoAdminSection("group_browser");
            tester.clickLink("bulk_edit_groups");

            // All groups should be there excluding "jira-sys-admins"
            tester.assertOptionsEqual("selectedGroupsStr", new String[] { "jira-administrators", "jira-developers", "jira-users" });

            // Try to hack the url to add a user to the group that is not present
            tester.gotoPage(page.addXsrfToken("/secure/admin/user/BulkEditUserGroups.jspa?selectedGroupsStr=jira-sys-admins&assign=true&usersToAssignStr=fred"));
            text.assertTextPresent(locator.css(BULK_EDIT_GROUP_MEMBERS_ERRORS_CONTAINER_LOCATOR), "You cannot add users to groups which are not visible to you.");

            // Try to hack the url to remove a user from the group that is not present
            tester.gotoPage(page.addXsrfToken("/secure/admin/user/BulkEditUserGroups.jspa?selectedGroupsStr=jira-sys-admins&unassign=true&usersToUnassign=root"));
            text.assertTextPresent(locator.css(BULK_EDIT_GROUP_MEMBERS_ERRORS_CONTAINER_LOCATOR), "You can not remove a group from this user as it is not visible to you.");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testFredEditGroups()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.login(FRED_USERNAME, FRED_PASSWORD);
            // Try to hack the url to add a user to the group that is not present
            tester.gotoPage(page.addXsrfToken("/secure/admin/user/BulkEditUserGroups.jspa?selectedGroupsStr=jira-sys-admins&assign=true&usersToAssignStr=fred"));
            tester.assertTextPresent("my login on this computer");

            // Try to hack the url to remove a user from the group that is not present
            tester.gotoPage(page.addXsrfToken("/secure/admin/user/BulkEditUserGroups.jspa?selectedGroupsStr=jira-sys-admins&unassign=true&usersToUnassign=root"));
            tester.assertTextPresent("my login on this computer");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }

    }

    public void testBulkEditUserGroupsWithSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            navigation.gotoAdminSection("group_browser");
            tester.clickLink("edit_members_of_jira-sys-admins");

            // Should contain the jira-sys-admins group
            tester.assertOptionsEqual("selectedGroupsStr", new String[] { "jira-administrators","jira-developers", "jira-sys-admins", "jira-users"});
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testBulkEditUserGroupsHappyPathSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            navigation.logout();
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            getBackdoor().usersAndGroups().addGroup("sys-admin-group2");
            administration.addGlobalPermission(SYSTEM_ADMINISTER, "sys-admin-group2");
            getBackdoor().usersAndGroups().addUserToGroup(SYS_ADMIN_USERNAME, "sys-admin-group2");

            navigation.gotoAdminSection("group_browser");
            tester.clickLink("edit_members_of_jira-sys-admins");

            tester.selectOption("usersToUnassign", SYS_ADMIN_USERNAME);
            tester.submit("unassign");

            navigation.userProfile().gotoCurrentUserProfile();

            tester.assertTextNotPresent("jira-sys-admins");
            tester.assertTextPresent("sys-admin-group2");

            // Now go and rejoin the group just to prove it works
            navigation.gotoAdminSection("group_browser");
            tester.clickLink("edit_members_of_jira-sys-admins");

            tester.setFormElement("usersToAssignStr", SYS_ADMIN_USERNAME);
            tester.submit("assign");

            navigation.userProfile().gotoCurrentUserProfile();

            tester.assertTextPresent("jira-sys-admins");
            tester.assertTextPresent("sys-admin-group2");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }

    }

    public void testBulkEditUserGroupsHappyPathAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            getBackdoor().usersAndGroups().addGroup("admin-group2");
            administration.addGlobalPermission(ADMINISTER, "admin-group2");
            getBackdoor().usersAndGroups().addUserToGroup(ADMIN_USERNAME, "admin-group2");

            navigation.gotoAdminSection("group_browser");
            tester.clickLink("edit_members_of_jira-administrators");

            tester.selectOption("usersToUnassign", ADMIN_USERNAME);
            tester.submit("unassign");

            navigation.gotoAdminSection("user_browser");
            tester.clickLink("editgroups_admin");

            // Should be able to leave admin-group2 and should not be able to leave jira-administrators
            // as we have just removed the user from that group
            tester.assertOptionsEqual("groupsToLeave", new String[] { "admin-group2", "jira-developers", "jira-users" });

            // Now go and rejoin the group just to prove it works
            navigation.gotoAdminSection("group_browser");
            tester.clickLink("edit_members_of_jira-administrators");

            tester.setFormElement("usersToAssignStr", ADMIN_USERNAME);
            tester.submit("assign");

            navigation.gotoAdminSection("user_browser");
            tester.clickLink("editgroups_admin");

            // Now you should be able to leave both jira-administrators and admin-group2
            tester.assertOptionsEqual("groupsToLeave", new String[] { "admin-group2", "jira-administrators", "jira-developers", "jira-users" });
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testBulkEditGroupMembershipWithPersonalLicense()
    {
        //there's already a lot of users taking us over the personal license limit.
        // Lets switch to a personal license
        switchToPersonalLicense();
        //now lets add 1 more users. Bob should not be added to the jira-users group.
        getBackdoor().usersAndGroups().addUser(BOB_USERNAME);
        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=bob");
        tester.assertTextPresent("User: bob");
        tester.assertTextNotPresent("jira-users");

        //lets try to get bob to join one of the groups that will grant him log in permission
        // Click Link 'Edit Groups' (id='editgroups_link').
        tester.clickLink("editgroups_link");
        // Select 'jira-administrators' from select box 'groupsToJoin'.
        tester.selectOption("groupsToJoin", "jira-administrators");
        tester.submit("join");
        tester.assertTextPresent(PERSONAL_LICENSE_LIMIT_ERROR);
        // Select 'jira-users' from select box 'groupsToJoin'.
        tester.selectOption("groupsToJoin", "jira-users");
        tester.submit("join");
        tester.assertTextPresent(PERSONAL_LICENSE_LIMIT_ERROR);

        //now lets try to bulk edit group membership and add some more users to the each one of the groups granting log in
        //permission
        // Click Link 'Group Browser' (id='group_browser').
        tester.clickLink("group_browser");
        // Click Link 'Edit Members' (id='edit_members_of_jira-administrators').
        tester.clickLink("edit_members_of_jira-administrators");
        tester.setFormElement("usersToAssignStr", BOB_USERNAME);
        tester.submit("assign");
        tester.assertTextPresent(PERSONAL_LICENSE_LIMIT_ERROR);

        tester.clickLink("group_browser");
        tester.clickLink("edit_members_of_jira-users");
        tester.setFormElement("usersToAssignStr", BOB_USERNAME);
        tester.submit("assign");
        tester.assertTextPresent(PERSONAL_LICENSE_LIMIT_ERROR);
    }

    public void testBulkEditGroupMembershipWithPersonalLicenseAlreadyActive()
    {
        //there's already a lot of users taking us over the personal license limit.
        // Lets switch to a personal license
        switchToPersonalLicense();

        //go to a user that's already active and add him to the admins group.
        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=dev");
        assertTextPresent("User: developer");
        assertTextPresent("jira-users");
        assertTextNotPresent("jira-administrators");
        // Click Link 'Edit Groups' (id='editgroups_link').
        tester.clickLink("editgroups_link");

        // Select 'jira-administrators' from select box 'groupsToJoin'.
        tester.selectOption("groupsToJoin", "jira-administrators");
        tester.submit("join");
        tester.assertTextNotPresent(PERSONAL_LICENSE_LIMIT_ERROR);

        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=dev");
        tester.assertTextPresent("User: developer");
        tester.assertTextPresent("jira-users");
        tester.assertTextPresent("jira-administrators");

        //now lets try to bulk edit group membership and add some more users to the each one of the groups granting log in
        //permission
        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=dev00");
        assertTextPresent("User: developer");
        assertTextPresent("jira-users");
        assertTextNotPresent("jira-administrators");

        // Click Link 'Group Browser' (id='group_browser').
        tester.clickLink("group_browser");
        // Click Link 'Edit Members' (id='edit_members_of_jira-administrators').
        tester.clickLink("edit_members_of_jira-administrators");
        tester.setFormElement("usersToAssignStr", "dev00");
        tester.submit("assign");
        tester.assertTextNotPresent(PERSONAL_LICENSE_LIMIT_ERROR);

        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=dev00");
        tester.assertTextPresent("User: developer");
        tester.assertTextPresent("jira-users");
        tester.assertTextPresent("jira-administrators");
    }

    private void _testBulkUnassignUsersFromGroups()
    {
        gotoBulkEditGroupMembers();

        //Remove 1 user from 1 selected group
        selectDeveloperGroupOnly();
        tester.setWorkingForm("jiraform");
        selectMultiOption(FIELD_USERS_TO_UNASSIGN, "dev00");
        tester.submit(UNASSIGN); developersCount--;
        tester.assertTextPresent("Selected 1 of 5 Groups");
        tester.assertTextPresent(developersCount + " Group Member(s)");
        //make sure the removed user is not on the list
        String[] options = tester.getDialog().getOptionValuesFor(FIELD_USERS_TO_UNASSIGN);
        for (String option : options)
        {
            assertFalse(option.equals("dev00______jira-developers"));
        }

        //remove 2 users from 2 selected groups
        selectUsersAndDevelopersGroup();
        tester.setWorkingForm("jiraform");
        options = tester.getDialog().getOptionValuesFor(FIELD_USERS_TO_UNASSIGN);
        assertTrue(options.length == 7);
        selectMultiOptionByValue(FIELD_USERS_TO_UNASSIGN, "dev01");
        selectMultiOptionByValue(FIELD_USERS_TO_UNASSIGN, "user01______jira-users");
        tester.submit(UNASSIGN); developersCount--; usersCount--; usersCount--; //user is removed from both groups
        tester.assertTextPresent("Selected 2 of 5 Groups");
        //make sure both users are removed
        tester.setWorkingForm("jiraform");
        options = tester.getDialog().getOptionValuesFor(FIELD_USERS_TO_UNASSIGN);
        assertTrue(options.length == 5);
        for (String option : options)
        {
            assertFalse(option.equals("user01______jira-users"));
            assertFalse(option.equals("dev01"));
        }
    }

    private void _testBulkAssignUsersFromGroups()
    {
        gotoBulkEditGroupMembers();

        //Add one user to 1 group from 1 of the selected group
        selectDeveloperGroupOnly();
        tester.assertTextPresent(developersCount + " Group Member(s)");
        tester.setFormElement(FIELD_USERS_TO_ASSIGN, "dev00");
        tester.submit(ASSIGN); developersCount++;
        tester.assertTextPresent("Selected 1 of 5 Groups");
        tester.assertTextPresent(developersCount + " Group Member(s)");
        //make sure the added user is on the list
        tester.setWorkingForm("jiraform");
        String[] options = tester.getDialog().getOptionValuesFor(FIELD_USERS_TO_UNASSIGN);
        boolean found = false;
        for (String option : options)
        {
            if (option.equals("dev00______jira-developers"))
            { found = true; }
        }
        assertTrue(found);

        //Add 2 users to all of the 2 selected groups
        selectUsersAndDevelopersGroup();
        tester.setWorkingForm("jiraform");
        options = tester.getDialog().getOptionValuesFor(FIELD_USERS_TO_UNASSIGN);
        assertTrue(options.length == 5);
        tester.setFormElement(FIELD_USERS_TO_ASSIGN, "dev01, user00"); //dev01 is a member of no groups, user is a member of jira-users
        tester.submit(ASSIGN); developersCount++; developersCount++; usersCount++;
        tester.assertTextPresent("Selected 2 of 5 Groups");
        //make sure both users are added
        tester.setWorkingForm("jiraform");
        options = tester.getDialog().getOptionValuesFor(FIELD_USERS_TO_UNASSIGN);
        //added 2 users but list length goes up by 1 only
        //because dev is in jira-users originally, hence it is removed from jira-users section and displayed under All
        assertEquals(6, options.length);
        boolean found2 = false;
        found = false;
        for (String option : options)
        {
            if (option.equals("user00")) { found = true; }
            if (option.equals("dev01")) { found2 = true; }
        }
        assertTrue(found && found2);
    }

    private void _testBulkEditUserGroupsListSize()
    {
        //For these tests to pass, the jira.usermanagement.maxdisplaymembers must be set to 200 or left empty
        gotoBulkEditGroupMembers();

        //select group200 - there should be no warning about list size
        selectGroup200();
        tester.assertTextNotPresent(WARNING_TOO_MANY_USERS_TO_DISPLAY);

        //select group202 - there should be a warning about list size for group 202
        selectGroup202();
        tester.assertTextPresent("group202 have more than 200 users.");
        tester.assertTextPresent(WARNING_TOO_MANY_USERS_TO_DISPLAY);

        //select group202 and group200 - there should be a warning about list size for group 202 only
        selectGroup200And202();
        tester.assertTextPresent("group202 have more than 200 users.");
        tester.assertTextPresent(WARNING_TOO_MANY_USERS_TO_DISPLAY);
    }

    private void _testBulkUnassignUsersFromGroupsValidation()
    {
        //try unassiging a user without selecting a group
        gotoBulkEditGroupMembers();
        tester.submit(UNASSIGN);
        tester.assertTextPresent(ERROR_SELECT_GROUPS);

        //select groups and try to remove no users from them
        selectUsersAndDevelopersGroup();
        tester.submit(UNASSIGN);
        tester.assertTextPresent(ERROR_SELECT_USERS_TO_REMOVE);

        //select one group, select a user and select a new group that does not contain selected user then try unassign without refreshing
        selectUsersAndDevelopersGroup();
        selectMultiOptionByValue(FIELD_USERS_TO_UNASSIGN, "user00");
        //select a different group and dont refresh afterwards
        selectMultiOption(FIELD_SELECTED_GROUPS, getAdminOption());
        tester.assertTextPresent(PLEASE_REFRESH_MEMBERS_LIST);
        tester.submit(UNASSIGN);
        tester.assertTextPresent("Cannot remove user &#39;user00&#39; from group &#39;jira-administrators&#39; since user is not a member of &#39;jira-administrators&#39;");

        //select a group, select a user and select multiple new groups that does not contain selected user then try unassign without refreshing
        selectUsersAndDevelopersGroup();
        selectMultiOptionByValue(FIELD_USERS_TO_UNASSIGN, "user00");
        //select multiple different group and dont refresh afterwards
        selectMultiOption(FIELD_SELECTED_GROUPS, getDeveloperOption());
        selectMultiOption(FIELD_SELECTED_GROUPS, getAdminOption());
        tester.assertTextPresent(PLEASE_REFRESH_MEMBERS_LIST);
        tester.submit(UNASSIGN);
        assertTextPresent("Cannot remove user &#39;user00&#39; from selected group(s) since user is not a member of all the selected group(s)");

        //select multiple groups, select a user and select a new group that does not contain selected user then try unassign without refreshing
        selectUsersAndDevelopersGroup();
        selectMultiOptionByValue(FIELD_USERS_TO_UNASSIGN, "user______jira-users");
        //select a different group and dont refresh afterwards
        selectMultiOption(FIELD_SELECTED_GROUPS, getAdminOption());
        tester.assertTextPresent(PLEASE_REFRESH_MEMBERS_LIST);
        tester.submit(UNASSIGN);
        assertTextPresent("Cannot remove user &#39;user&#39; from group &#39;jira-users&#39; since the group was not selected. Please make sure to refresh after selecting new group(s)");

        //select admin group and remove admin
        selectAdminGroupOnly();
        tester.setWorkingForm("jiraform");
        selectMultiOption(FIELD_USERS_TO_UNASSIGN, ADMIN_USERNAME);
        tester.submit(UNASSIGN);
        tester.assertTextPresent(ERROR_LEAVING_ALL_SYS_ADMIN_GROUPS);

        //select all groups and try removing admin permission
        selectAllGroups();
        tester.setWorkingForm("jiraform");
        selectMultiOption(FIELD_USERS_TO_UNASSIGN, ADMIN_USERNAME);
        tester.submit(UNASSIGN);
        tester.assertTextPresent(ERROR_LEAVING_ALL_SYS_ADMIN_GROUPS);
    }

    private void _testBulkAssignUsersFromGroupsValidation()
    {
        //try assigning no users with no groups selected
        gotoBulkEditGroupMembers();
        tester.setFormElement(FIELD_USERS_TO_ASSIGN, "");
        tester.assertTextPresent("Selected 0 of 5 Groups");
        tester.submit(ASSIGN);
        tester.assertTextPresent(ERROR_SELECT_GROUPS);

        //try assigning users with no groups selected
        gotoBulkEditGroupMembers();
        tester.setFormElement(FIELD_USERS_TO_ASSIGN, "dev, user");
        tester.assertTextPresent("Selected 0 of 5 Groups");
        tester.submit(ASSIGN);
        tester.assertTextPresent(ERROR_SELECT_GROUPS);

        //select groups and add without selecting users
        selectUsersAndDevelopersGroup();
        tester.submit(ASSIGN);
        tester.assertTextPresent(ERROR_SELECT_USERS_TO_ADD);

        //select groups and add invalid user
        selectUsersAndDevelopersGroup();
        tester.setFormElement(FIELD_USERS_TO_ASSIGN, "invalid");
        tester.submit(ASSIGN);
        tester.assertTextPresent(ERROR_CANNOT_ADD_USER_INVALID);

        //add a existing member to a group
        selectAdminGroupOnly();
        tester.setFormElement(FIELD_USERS_TO_ASSIGN, ADMIN_USERNAME);
        tester.submit(ASSIGN);
        tester.assertTextPresent(ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN);

        //add a existing member to multiple groups
        selectUsersAndDevelopersGroup();
        tester.setFormElement(FIELD_USERS_TO_ASSIGN, ADMIN_USERNAME);
        tester.submit(ASSIGN);
        tester.assertTextPresent(ERROR_ADMIN_ALREADY_MEMBER_OF_ALL);

        //add a existing member and non existing member to a group
        selectAdminGroupOnly();
        tester.setFormElement(FIELD_USERS_TO_ASSIGN, "admin, dev");
        tester.submit(ASSIGN);
        tester.assertTextPresent(ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN);
        //assert that dev was not added in the process as there was error with adding admin
        tester.setWorkingForm("jiraform");
        String[] options = tester.getDialog().getOptionValuesFor(FIELD_USERS_TO_UNASSIGN);
        assertTrue(options.length == adminsCount);
        for (String option : options)
        {
            assertFalse(option.equals("dev"));
        }

        //add a existing member and non existing member to all groups
        selectAllGroups();
        tester.setFormElement(FIELD_USERS_TO_ASSIGN, "admin, dev"); //dev is a user and developer but not admin
        tester.submit(ASSIGN);
        tester.assertTextPresent(ERROR_ADMIN_ALREADY_MEMBER_OF_ALL);
        //assert that dev was not added in the process as there was error with adding admin
        tester.setWorkingForm("jiraform");
        options = tester.getDialog().getOptionValuesFor(FIELD_USERS_TO_UNASSIGN);
        boolean found = false;
        boolean found2 = false;
        for (String option : options)
        {
            assertFalse(option.equals("dev"));
            //assert that the developer is still a member of both users and developers
            if (option.equals("dev______jira-users")) { found = true; }
            if (option.equals("dev______jira-developers")) { found2 = true; }
        }
        assertTrue(found);
        assertTrue(found2);

        //-------------------------------------------------------------------------------------------------------Pruning
        //attempt to add various user members and test pruning a large set of usernames
        selectAdminGroupOnly();
        tester.setFormElement(FIELD_USERS_TO_ASSIGN, "user, admin, duplicate, invalid, dev, duplicate, duplicate, error, user");
        tester.submit(ASSIGN);
        tester.assertTextPresent(ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN);
        tester.assertTextPresent("Cannot add user. &#39;duplicate&#39; does not exist");
        tester.assertTextPresent("Cannot add user. &#39;invalid&#39; does not exist");
        tester.assertTextPresent("Cannot add user. &#39;duplicate&#39; does not exist");
        tester.assertTextPresent("Cannot add user. &#39;duplicate&#39; does not exist");
        tester.assertTextPresent("Cannot add user. &#39;error&#39; does not exist");
    }

    //----------------------------------------------------------------------------------------------------Helper Methods
    private void gotoBulkEditGroupMembers()
    {
        navigation.gotoAdminSection("group_browser");
        tester.clickLinkWithText("Bulk Edit Group Members");
        tester.assertTextPresent("This page allows you to edit the user memberships for each group.");
        tester.assertTextPresent("Selected 0 of 5 Groups");
        tester.assertTextPresent("No users in selected group(s)");
    }

    private void selectGroup200()
    {
        selectSingleGroupOnly(getGroup200Option(), group200Count);
    }

    private void selectGroup202()
    {
        selectSingleGroupOnly(getGroup202Option(), group202Count);
    }

    private void selectUserGroupOnly()
    {
        selectSingleGroupOnly(getUserOption(), usersCount);
    }

    private void selectDeveloperGroupOnly()
    {
        selectSingleGroupOnly(getDeveloperOption(), developersCount);
    }

    private void selectAdminGroupOnly()
    {
        selectSingleGroupOnly(getAdminOption(), adminsCount);
    }

    private void selectUsersAndDevelopersGroup()
    {
        selectTwoGroups(getUserOption(), getDeveloperOption());
    }

    private void selectGroup200And202()
    {
        selectTwoGroups(getGroup200Option(), getGroup202Option());
    }

    private void selectSingleGroupOnly(String group, int groupCount)
    {
        tester.setWorkingForm("jiraform");
        selectMultiOption(FIELD_SELECTED_GROUPS, group);
        tester.assertTextPresent(PLEASE_REFRESH_MEMBERS_LIST);
        refreshMembersList();
        tester.assertTextPresent("Selected 1 of 5 Groups");
        tester.assertTextPresent((groupCount <= 200 ? groupCount : 200) + " Group Member(s)");
    }

    private void selectTwoGroups(String group1, String group2)
    {
        tester.setWorkingForm("jiraform");
        selectMultiOption(FIELD_SELECTED_GROUPS, group1);
        selectMultiOption(FIELD_SELECTED_GROUPS, group2);
        tester.assertTextPresent(PLEASE_REFRESH_MEMBERS_LIST);
        refreshMembersList();
        tester.assertTextPresent("Selected 2 of 5 Groups");
    }

    private void selectAllGroups()
    {
        tester.setWorkingForm("jiraform");
        selectMultiOption(FIELD_SELECTED_GROUPS, getAdminOption());
        selectMultiOption(FIELD_SELECTED_GROUPS, getDeveloperOption());
        selectMultiOption(FIELD_SELECTED_GROUPS, getUserOption());
        tester.assertTextPresent(PLEASE_REFRESH_MEMBERS_LIST);
        refreshMembersList();
        tester.assertTextPresent("Selected 3 of 5 Groups");
    }

    private void refreshMembersList()
    {
        tester.clickLink("refresh-dependant-fields");
    }

    private String getAdminOption()     { return "jira-administrators"; }
    private String getDeveloperOption() { return "jira-developers"; }
    private String getUserOption()      { return "jira-users"; }
    private String getGroup200Option()  { return "group200"; }
    private String getGroup202Option()  { return "group202"; }
}
