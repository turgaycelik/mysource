package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.admin.user.DeleteUserPage;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import junit.framework.AssertionFailedError;
import org.junit.Ignore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Functional test case for user management pages.
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserManagement extends JIRAWebTest
{
    public static final String JIRA_DEVELOPERS_GROUP_NAME = "jira-developers";
    public static final String JIRA_ADMINISTRATORS_GROUP_NAME = "jira-administrators";
    public static final String ISO_8859_1_JAVA_CHARS = "!@?[]~'{};&abc123\u00a3 \u00a9 \u00e5 \u00eb \u00f8 \u00e2 \u00ee \u00f4 \u00fd \u00ff \u00fc";
    public static final String ISO_8859_1_HTML_CHARS = "!@?[]~'{};&amp;abc123&pound; &copy; &aring; &euml; &oslash; &acirc; &icirc; &ocirc; &yacute; &yuml; &uuml;";
    public static final String NON_ISO_8859_1_CHARACTERS = "\uFFFF???";
    private static final String DUPLICATE_GROUP_NAME = "duplicate_group";
    public static final String NON_ISO_8859_1_CHAR = "\u00e4";

    public TestUserManagement(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreBlankInstance();
    }

    public void testUserManagement()
    {
        navigation.gotoAdminSection("user_browser");
        if (tester.getDialog().isLinkPresentWithText(BOB_USERNAME))
        {
            deleteUser(BOB_USERNAME);
        }
        //the following tests are dependant on each other. (eg. all require user Bob created from createUser()) 
        createUser();
        createValidGroup();
        createInvalidUsers();
        createInvalidGroups();
        addUserToGroup();
        loginWithNewUser();
        removeUserFromGroup();
        setUserPassword();
        deleteUser();
        loginWithInvalidUser();
    }

    /**
     * Test that enabling the "external user management" hides all but the "Project Roles" operation.
     */
    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only")
    public void testUserBrowserOperationsVisibility()
    {
        toggleExternalUserManagement(false);
        //check that the operation links are visible
        clickOnAdminPanel("admin.usersgroups", "user_browser");
        assertLinkPresent("editgroups_admin");
        assertLinkPresent("editgroups_fred");
        assertLinkPresent("projectroles_link_admin");
        assertLinkPresent("projectroles_link_fred");
        assertLinkPresent("edituser_link_admin");
        assertLinkPresent("deleteuser_link_admin");
        assertLinkPresent("deleteuser_link_fred");

        //enable external user management
        toggleExternalUserManagement(true);
        //check that only the view project roles operation is available
        clickOnAdminPanel("admin.usersgroups", "user_browser");
        assertLinkNotPresent("editgroups_admin");
        assertLinkNotPresent("editgroups_fred");
        assertLinkPresent("projectroles_link_admin");
        assertLinkPresent("projectroles_link_fred");
        assertLinkNotPresent("edituser_link_admin");
        assertLinkNotPresent("deleteuser_link_admin");
        assertLinkNotPresent("deleteuser_link_fred");

        //disable external user management
        toggleExternalUserManagement(false);
        //check that the operation links are back to default
        clickOnAdminPanel("admin.usersgroups", "user_browser");
        assertLinkPresent("editgroups_admin");
        assertLinkPresent("editgroups_fred");
        assertLinkPresent("projectroles_link_admin");
        assertLinkPresent("projectroles_link_fred");
        assertLinkPresent("edituser_link_admin");
        assertLinkPresent("deleteuser_link_admin");
        assertLinkPresent("deleteuser_link_fred");
    }

    public void createInvalidUsers()
    {
        log("Testing User Creation Validation");
        addUser("", BOB_PASSWORD, "No Username", BOB_EMAIL);
        assertTextPresent("You must specify a username.");

        addUser(BOB_USERNAME, BOB_PASSWORD, "duplicate_user", BOB_EMAIL);
        addUser(BOB_USERNAME, BOB_PASSWORD, "duplicate_user", BOB_EMAIL);
        assertTextPresent("A user with that username already exists.");

        addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, "");
        assertTextPresent("You must specify an email address.");
        addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, "asf.com");
        assertTextPresent("You must specify a valid email address.");
    }

    public void createInvalidGroups()
    {
        log("Testing Group Creation Validation");
        //create group with already existing group name
        createGroup(DUPLICATE_GROUP_NAME);
        addGroup(DUPLICATE_GROUP_NAME);
        tester.assertTextPresent("A group or user with this name already exists.");

        removeGroup(DUPLICATE_GROUP_NAME);
    }

    private void addGroup(String groupName)
    {
        clickOnAdminPanel("admin.usersgroups", "group_browser");
        tester.setFormElement("addName", groupName);
        tester.submit();
    }

    public void createUser()
    {
        administration.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        tester.assertTextPresent("User: " + BOB_FULLNAME);
        assertTextPresentBeforeText("Username:", BOB_USERNAME);
        assertTextPresentBeforeText("Email", BOB_EMAIL);
    }

    public void createValidGroup()
    {
        administration.usersAndGroups().addGroup("Valid Group");
        administration.usersAndGroups().deleteGroup("Valid Group");
    }

    public void addUserToGroup()
    {
        administration.usersAndGroups().addUserToGroup(BOB_USERNAME, JIRA_DEVELOPERS_GROUP_NAME);
        administration.usersAndGroups().addUserToGroup(BOB_USERNAME, JIRA_ADMINISTRATORS_GROUP_NAME);
    }

    public void loginWithNewUser()
    {
        // log out from default login fired in setUp()
        navigation.logout();
        navigation.login(BOB_USERNAME, BOB_PASSWORD);
        assertRedirectPath(getEnvironmentData().getContext() + "/secure/Dashboard.jspa");
        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void removeUserFromGroup()
    {
        administration.usersAndGroups().removeUserFromGroup(BOB_USERNAME, JIRA_ADMINISTRATORS_GROUP_NAME);
    }

    public void setUserPassword()
    {
        String NEW_PASSWORD = "new";
        String DIFFERENT_PASSWORD = "diff";

        //check set user password validation
        navigateToUser(BOB_USERNAME);
        assertTextPresentBeforeText("User:", BOB_FULLNAME);
        tester.clickLinkWithText("Set Password");
        assertTextPresentBeforeText("Set Password:", BOB_FULLNAME);

        //error validation case 1 (empty input)
        tester.setFormElement("password", "");
        tester.setFormElement("confirm", "");
        tester.submit("Update");
        tester.assertTextPresent("You must specify a password");

        //error validation case 2 (only one of the fields entered)
        tester.setFormElement("password", "");
        tester.setFormElement("confirm", NEW_PASSWORD);
        tester.submit("Update");
        tester.assertTextPresent("You must specify a password");
        tester.setFormElement("password", NEW_PASSWORD);
        tester.setFormElement("confirm", "");
        tester.submit("Update");
        tester.assertTextPresent("The two passwords entered do not match.");

        //error validation case 3 (mismatching password)
        tester.setFormElement("password", NEW_PASSWORD);
        tester.setFormElement("confirm", DIFFERENT_PASSWORD);
        tester.submit("Update");
        tester.assertTextPresent("The two passwords entered do not match.");

        //successful validation and change
        tester.setFormElement("password", NEW_PASSWORD);
        tester.setFormElement("confirm", NEW_PASSWORD);
        tester.submit("Update");
        assertTextPresentBeforeText("Password for user " + BOB_USERNAME + " has successfully been set", BOB_FULLNAME);

        //check that the new password has been set for the user (ie. logout and login with the user)
        navigation.logout();
        navigation.loginAttempt(BOB_USERNAME, BOB_PASSWORD);
        tester.assertTextPresent("Sorry, your username and password are incorrect - please try again.");
        navigation.login(BOB_USERNAME, NEW_PASSWORD);

        assertEquals(BOB_FULLNAME, navigation.userProfile().userName());

        //log back in as admin for subsequent tests
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void deleteUser()
    {
        deleteUser(BOB_USERNAME);
        tester.assertTextPresent("UserBrowser");
        tester.assertTextPresent("Displaying users");

        // Restore user Bob for later use
        administration.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
    }

    public void testDeleteUserProjectLead()
    {
        administration.restoreData("TestUserManagement.xml");
        DeleteUserPage deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters("detkin"));
        assertThat(deleteUserPage.getUserDeletionError(), equalTo(deleteUserPage.getUserCannotBeDeleteMessage("detkin")));
        text.assertTextPresent(deleteUserPage.getProjectLink(), "Another Project");
        text.assertTextPresent(deleteUserPage.getProjectLink(), "Project 3");
    }
    public void testDeleteUserComponentLead()
    {
        administration.restoreData("TestUserManagementComponentLead.xml");
        navigateToUser("detkin");
        tester.clickLink("deleteuser_link");

        // there are six components in the imported XML file
        final int NUMBER_OF_COMPONENTS = 6;
        int count = 0;
        for (int i = 1; i <= NUMBER_OF_COMPONENTS; i++)
        {
            try
            {
                tester.assertLinkPresentWithText("comp " + i);
                count++;
            }
            catch (AssertionFailedError e)
            {
                // do nothing, not all components are shown and the order is not guaranteed
            }
        }

        // number of displayed components is same
        assertTrue(count == NUMBER_OF_COMPONENTS);

        tester.assertSubmitButtonPresent("Delete");
    }

    public void loginWithInvalidUser()
    {
        // log out from default login fired in setUp()
        navigation.logout();
        navigation.loginAttempt(BOB_USERNAME, null);
        tester.assertTextPresent("Sorry, your username and password are incorrect - please try again.");
    }
}
