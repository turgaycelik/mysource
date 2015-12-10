package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * To be run against Active Directory or Open LDAP in TPM.
 * This test relies on TestTpmLdapSetup being run first in order to create the appropriate LDAP User Directory.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.LDAP, Category.TPM })
public class TestTpmLdap extends AbstractTpmLdapTest
{
    public void testAddAndDeleteUser() throws Exception
    {
        if (assertions.getUserAssertions().userExists("wilma"))
        {
            log("User wilma was found - attempting to clean up before running test.");
            deleteUser("wilma");
        }
        assertions.getUserAssertions().assertUserDoesNotExist("wilma");

        navigation.gotoAdminSection("user_browser");

        // Add a User
        tester.clickLink("create_user");
        tester.setFormElement("username", "wilma");
        tester.setFormElement("fullname", "Wilma Flintstone");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.setFormElement("email", "wilma@bedrock.com");
        tester.submit("Create");

        assertions.getUserAssertions().assertUserExists("wilma");
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", "LDAP Directory");

        // cleanup - delete the user
        deleteUser("wilma");
        assertions.getUserAssertions().assertUserDoesNotExist("wilma");
    }

    public void testAddAndDeleteGroup() throws Exception
    {
        assertions.getUserAssertions().assertGroupDoesNotExist("newGroup");

        navigation.gotoAdminSection("group_browser");

        tester.setFormElement("addName", "newGroup");
        tester.submit("add_group");
        tester.assertTextNotPresent("Error occurred adding group");

        assertions.getUserAssertions().assertGroupExists("newGroup");

        // cleanup - delete the user
        deleteGroup("newGroup");
        assertions.getUserAssertions().assertGroupDoesNotExist("newGroup");
    }

    public void testLoginAsLdapUser() throws Exception
    {
        setupUserWilma();

        navigation.logout();

        navigation.loginAttempt("wilma", "fail");
        tester.assertTextPresent("your username and password are incorrect");
        navigation.login("wilma", "password");

        // Check we are logged in as wilma
        // Click Link 'Wilma Flintstone' - View Profile
        tester.clickLink("header-details-user-fullname");
        assertions.assertNodeByIdHasText("up-user-title-name", "Wilma Flintstone");

        navigation.logout();

        navigation.login(ADMIN_USERNAME);

        // cleanup - delete the user
        deleteUser("wilma");
    }

    public void testEditUser() throws Exception
    {
        setupUserWilma();

        navigation.gotoAdminSection("user_browser");
        tester.setFormElement("userNameFilter", "wil");
        tester.submit("");
        // Click Link 'Edit'
        tester.clickLink("edituser_link_wilma");
        tester.setFormElement("fullName", "Betty Rubble");
        tester.setFormElement("email", "betty@example.com");
        tester.submit("Update");

        assertions.getUserAssertions().assertUserDetails("wilma", "Betty Rubble", "betty@example.com", "LDAP Directory");

        // cleanup - delete the user
        deleteUser("wilma");
    }

    public void testAddUserToGroup() throws Exception
    {
        setupUserWilma();
        // Add a new group - "cartoon-characters"
        addGroup("cartoon-characters");

        // Attempt to Add LDAP user to group
        assertions.getUserAssertions().assertUserDoesNotBelongToGroup("wilma", "cartoon-characters");
        gotoViewUser("wilma");
        // Click Link 'Edit Groups' (id='editgroups_link').
        tester.clickLink("editgroups_link");
        tester.selectOption("groupsToJoin", "cartoon-characters");
        tester.submit("join");
        assertions.getUserAssertions().assertUserBelongsToGroup("wilma", "cartoon-characters");

        // Attempt to Add Internal user to group
        assertions.getUserAssertions().assertUserDoesNotBelongToGroup(FRED_USERNAME, "cartoon-characters");
        gotoViewUser(FRED_USERNAME);
        // Click Link 'Edit Groups' (id='editgroups_link').
        tester.clickLink("editgroups_link");
        tester.selectOption("groupsToJoin", "cartoon-characters");
        tester.submit("join");
        assertions.getUserAssertions().assertUserBelongsToGroup(FRED_USERNAME, "cartoon-characters");

        // Attempt to Add LDAP user to group that is internal only (ATM)
        assertions.getUserAssertions().assertUserDoesNotBelongToGroup("wilma", "jira-developers");
        gotoViewUser("wilma");
        // Click Link 'Edit Groups' (id='editgroups_link').
        tester.clickLink("editgroups_link");
        tester.selectOption("groupsToJoin", "jira-developers");
        tester.submit("join");
        assertions.getUserAssertions().assertUserBelongsToGroup("wilma", "jira-developers");

        // cleanup - delete the user
        deleteUser("wilma");
        deleteGroup("cartoon-characters");
    }

    public void testDeleteUserDoesNotRetainGroupMembership() throws Exception
    {
        setupUserWilma();
        // Add a new group - "cartoon-characters"
        addGroup("cartoon-characters");

        // Attempt to Add LDAP user to group
        assertions.getUserAssertions().assertUserDoesNotBelongToGroup("wilma", "cartoon-characters");
        gotoViewUser("wilma");
        // Click Link 'Edit Groups' (id='editgroups_link').
        tester.clickLink("editgroups_link");
        tester.selectOption("groupsToJoin", "cartoon-characters");
        tester.submit("join");
        assertions.getUserAssertions().assertUserBelongsToGroup("wilma", "cartoon-characters");

        // cleanup - delete the user
        deleteUser("wilma");

        setupUserWilma();
        synchroniseDirectory(1);
        assertions.getUserAssertions().assertUserDoesNotBelongToGroup("wilma", "cartoon-characters");

        // cleanup - delete the user
        deleteUser("wilma");
        deleteGroup("cartoon-characters");
    }

    public void testDeleteUserWhenDirectoriesOverlap() throws Exception
    {
        setupUserWilma();
        // Add a new group - "cartoon-characters"
        addGroup("cartoon-characters");

        gotoViewUser("wilma");
        // Click Link 'Edit Groups' (id='editgroups_link').
        tester.clickLink("editgroups_link");
        tester.selectOption("groupsToJoin", "cartoon-characters");
        tester.submit("join");

        // Disable LDAP Directory and add user to Internal Directory
        navigation.gotoAdminSection("user_directories");
        tester.clickLinkWithText("Disable");
        assertions.getTextAssertions().assertTextPresent("LDAP Directory</span>  <em>(inactive)</em>");
        assertions.getUserAssertions().assertUserDoesNotExist("wilma");

        // Add user to Internal Directory and join cartoon-characters group
        setupUserWilma("JIRA Internal Directory");
        assertions.getUserAssertions().assertUserDoesNotBelongToGroup("wilma", "cartoon-characters");
        gotoViewUser("wilma");

        // Click Link 'Edit Groups' (id='editgroups_link').
        tester.clickLink("editgroups_link");
        tester.selectOption("groupsToJoin", "cartoon-characters");
        tester.submit("join");

        // Enable LDAP Directory once again
        navigation.gotoAdminSection("user_directories");
        tester.clickLinkWithText("Enable");
        assertions.getTextAssertions().assertTextNotPresent("LDAP Directory</span>  <em>(inactive)</em>");

        // Delete user from LDAP Directory
        deleteUser("wilma");
        assertions.getUserAssertions().assertUserExists("wilma");

        // User now exists in Internal Directory and belongs to cartoon-characters
        navigation.gotoAdminSection("user_directories");
        tester.clickLinkWithText("Disable");
        assertions.getTextAssertions().assertTextPresent("LDAP Directory</span>  <em>(inactive)</em>");

        assertions.getUserAssertions().assertUserBelongsToGroup("wilma", "cartoon-characters");

        // cleanup
        navigation.gotoAdminSection("user_directories");
        tester.clickLinkWithText("Enable");
        deleteUser("wilma");
        deleteGroup("cartoon-characters");
    }

    private void setupUserWilma()
    {
        setupUserWilma("LDAP Directory");
    }

    private void setupUserWilma(String expectedDirectory)
    {
        if (assertions.getUserAssertions().userExists("wilma"))
        {
            log("User wilma was found - attempting to clean up before running test.");
            deleteUser("wilma");
        }
        assertions.getUserAssertions().assertUserDoesNotExist("wilma");

        navigation.gotoAdminSection("user_browser");

        // Add a User
        tester.clickLink("create_user");
        tester.setFormElement("username", "wilma");
        tester.setFormElement("fullname", "Wilma Flintstone");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.setFormElement("email", "wilma@bedrock.com");
        tester.submit("Create");

        assertions.getUserAssertions().assertUserExists("wilma");
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", expectedDirectory);
    }

    private void deleteUser(final String username)
    {
        gotoViewUser(username);
        // Click Link 'Delete User' (id='deleteuser_link').
        tester.clickLink("deleteuser_link");
        tester.submit("Delete");
    }

    private void gotoViewUser(final String username)
    {
        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=" + username);
    }

    private void addGroup(final String name)
    {
        navigation.gotoAdminSection("group_browser");

        tester.setFormElement("addName", name);
        tester.submit("add_group");
        tester.assertTextNotPresent("Error occurred adding group");

        assertions.getUserAssertions().assertGroupExists(name);
    }

    private void deleteGroup(final String name)
    {
        navigation.gotoAdminSection("group_browser");
        tester.setFormElement("nameFilter", name);
        tester.submit("filter");
        tester.clickLink("del_" + name);
        tester.submit("Delete");
    }

}
