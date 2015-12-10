package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;

import java.util.List;

import static com.atlassian.jira.webtests.ztests.tpm.ldap.AuditingClient.ViewResponse;
import static com.atlassian.jira.webtests.ztests.tpm.ldap.AuditingClient.ViewResponse.RecordResponse;
import static com.google.common.collect.Collections2.filter;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * To be run against Active Directory or Open LDAP in TPM.
 * This test relies on TestTpmLdapSetup being run first in order to create the appropriate Delegating LDAP User Directory.
 *
 * We need access to both the full access LDAP directory to setp up tests and the delegating LDAP directory to run them
 *
 * All normal user operations should be available.
 * 
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.LDAP, Category.TPM })
public class TestTpmDelegatingLdap extends AbstractTpmLdapTest
{
    public void testAddAndDeleteUser() throws Exception
    {
        disableLdapDirectory();
        addDelegatingLdapDirectory();

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
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", "Delegating LDAP Directory");

        // cleanup - delete the user
        deleteUser("wilma");
        assertions.getUserAssertions().assertUserDoesNotExist("wilma");

        deleteDelegatingLdapDirectory();
        enableLdapDirectory();
    }

    public void testAddAndDeleteGroup() throws Exception
    {
        disableLdapDirectory();
        addDelegatingLdapDirectory();

        assertions.getUserAssertions().assertGroupDoesNotExist("newGroup");

        navigation.gotoAdminSection("group_browser");

        tester.setFormElement("addName", "newGroup");
        tester.submit("add_group");
        tester.assertTextNotPresent("Error occurred adding group");

        assertions.getUserAssertions().assertGroupExists("newGroup");

        // cleanup - delete the user
        deleteGroup("newGroup");
        assertions.getUserAssertions().assertGroupDoesNotExist("newGroup");

        deleteDelegatingLdapDirectory();
        enableLdapDirectory();
    }

    public void testLoginAsInvalidLdapUser() throws Exception
    {
        navigation.gotoAdmin();

        if (assertions.getUserAssertions().userExists("wilma"))
        {
            log("User wilma was found - attempting to clean up before running test.");
            deleteUser("wilma");
        }
        assertions.getUserAssertions().assertUserDoesNotExist("wilma");

        disableLdapDirectory();
        addDelegatingLdapDirectory();

        setupUserWilma();
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", "Delegating LDAP Directory");

        navigation.logout();

        // Wilma exists locally but not in LDAP therefore she can't login
        navigation.loginAttempt("wilma", "password");
        tester.assertTextPresent("your username and password are incorrect");

        navigation.login(ADMIN_USERNAME);
        // cleanup - delete the user
        deleteUser("wilma");

        deleteDelegatingLdapDirectory();
        enableLdapDirectory();
    }

    public void testLoginAsValidLdapUser() throws Exception
    {
        // Before we start add Wilma to the real LDAP directory, so she can login
        setupUserWilma();
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", "LDAP Directory");

        disableLdapDirectory();
        addDelegatingLdapDirectory();

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

        deleteDelegatingLdapDirectory();
        enableLdapDirectory();

        // cleanup - delete the user from the real LDAP directory
        deleteUser("wilma");
    }

    public void testLoginAsValidLdapUserWithCopyOnLogin() throws Exception
    {
        // Before we start add Wilma to the real LDAP directory, so she can login
        setupUserWilma();
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", "LDAP Directory");

        disableLdapDirectory();
        addDelegatingLdapDirectoryWithCopyOnLogin();

        backdoor.getTestkit().auditing().clearAllRecords();
        navigation.logout();

        navigation.loginAttempt("wilma", "fail");
        tester.assertTextPresent("your username and password are incorrect");
        navigation.login("wilma", "password");

        // Check we are logged in as wilma
        // Click Link 'Wilma Flintstone' - View Profile
        tester.clickLink("header-details-user-fullname");
        assertions.assertNodeByIdHasText("up-user-title-name", "Wilma Flintstone");

        navigation.logout();

        assertCorrectAuditingLog();

        navigation.login(ADMIN_USERNAME);

        // cleanup - delete the user
        deleteUser("wilma");

        deleteDelegatingLdapDirectory();
        enableLdapDirectory();

        // cleanup - delete the user from the real LDAP directory
        deleteUser("wilma");
    }

    private void assertCorrectAuditingLog()
    {
        AuditingClient auditingClient = new AuditingClient(getEnvironmentData());
        final ViewResponse viewResponse = auditingClient.getViewResponse();
        List<RecordResponse> usersCreated = ImmutableList.copyOf(filter(viewResponse.getRecords(), new Predicate<RecordResponse>()
        {
            @Override
            public boolean apply(final RecordResponse recordResponse)
            {
                return recordResponse.getSummary().startsWith("User created");
            }

        }));
        assertThat(usersCreated.size(), equalTo(1));
        assertThat(usersCreated.get(0).getObjectItem().getObjectName(), startsWith("wilma"));
    }

    public void testEditUser() throws Exception
    {
        disableLdapDirectory();
        addDelegatingLdapDirectory();

        setupUserWilma();
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", "Delegating LDAP Directory");

        navigation.gotoAdminSection("user_browser");
        tester.setFormElement("userNameFilter", "wil");
        tester.submit("");
        // Click Link 'Edit'
        tester.clickLink("edituser_link_wilma");
        tester.setFormElement("fullName", "Betty Rubble");
        tester.setFormElement("email", "betty@example.com");
        tester.submit("Update");

        assertions.getUserAssertions().assertUserDetails("wilma", "Betty Rubble", "betty@example.com", "Delegating LDAP Directory");

        // cleanup - delete the user
        deleteUser("wilma");

        deleteDelegatingLdapDirectory();
        enableLdapDirectory();
    }

    public void testAddUserToGroup() throws Exception
    {
        disableLdapDirectory();
        addDelegatingLdapDirectory();

        setupUserWilma();
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", "Delegating LDAP Directory");
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

        deleteDelegatingLdapDirectory();
        enableLdapDirectory();
    }

    private void setupUserWilma()
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

    private void addDelegatingLdapDirectory() throws Exception
    {
        addDelegatingLdapDirectory(false);
    }

    private void addDelegatingLdapDirectoryWithCopyOnLogin() throws Exception
    {
        addDelegatingLdapDirectory(true);
    }

    private void addDelegatingLdapDirectory(final boolean createUserOnAuth)
    {
        navigation.gotoAdminSection("user_directories");
        tester.assertTextPresent("User Directories");

        // We run these tests against Active Directory and OpenLDAP, but we need different connection settings for each.
        if (isActiveDirectory())
        {
            log("Attempting to add a Delegating Active Directory LDAP User Directory...");
            addActiveDirectory(createUserOnAuth);
        }
        else
        {
            log("Attempting to add a Delegating Open LDAP User Directory...");
            addOpenLdap(createUserOnAuth);
        }

        WebTable table = assertions.getTableAssertions().getWebTable("directory-list");
        assertions.getTableAssertions().assertTableCellHasText(table, 3, 1, "Delegating LDAP Directory");
        // Move LDAP to top
        WebTable tblDirectoryList = new TableLocator(tester, "directory-list").getTable();
        WebLink link = tblDirectoryList.getTableCell(3, 3).getLinkWith("up");
        navigation.clickLink(link);

        tblDirectoryList = new TableLocator(tester, "directory-list").getTable();
        link = tblDirectoryList.getTableCell(2, 3).getLinkWith("up");
        navigation.clickLink(link);

        table = assertions.getTableAssertions().getWebTable("directory-list");
        assertions.getTableAssertions().assertTableCellHasText(table, 1, 1, "Delegating LDAP Directory");
    }

    private void addActiveDirectory(final boolean createUserOnAuth)
    {
        navigation.gotoPage("/plugins/servlet/embedded-crowd/configure/delegatingldap/");
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        tester.setWorkingForm("configure-delegating-ldap-form");
        // Set the required Simple fields
        tester.setFormElement("name", "Delegating LDAP Directory");
        tester.selectOption("type", "Microsoft Active Directory");
        tester.setFormElement("hostname", getLdapServer());
        // AD will not allow mutating operations unless you use SSL
        tester.setFormElement("port", "636");
        tester.checkCheckbox("useSSL", "true");
        tester.setFormElement("ldapUserdn", getUserDn());
        tester.setFormElement("ldapPassword", getPassword());
        tester.setFormElement("ldapBasedn", getBaseDn());
        tester.setFormElement("ldapUserUsername", "sAMAccountName");
        if (createUserOnAuth)
        {
            tester.checkCheckbox("createUserOnAuth", "true");
            tester.setFormElement("ldapAutoAddGroups", "jira-users");

            // Set the advanced fields manually - Func tests don't have javascript to do this for us
            tester.setFormElement("ldapUserObjectclass", "user");
            tester.setFormElement("ldapUserFilter", "(&(objectCategory=Person)(sAMAccountName=*))");
            tester.setFormElement("ldapUserUsernameRdn", "cn");
            tester.setFormElement("ldapUserFirstname", "givenName");
            tester.setFormElement("ldapUserLastname", "sn");
            tester.setFormElement("ldapUserDisplayname", "displayName");
            tester.setFormElement("ldapUserEmail", "mail");
            tester.setFormElement("ldapExternalId", "objectGUID");
        }

        // Add the new Directory
        tester.submit("test");
        text.assertTextPresent("Connection test successful");

        tester.submit("save");

        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        tester.assertTextPresent("JIRA Internal Directory");
        tester.assertTextPresent("Delegating LDAP Directory");
    }

    private void addOpenLdap(final boolean createUserOnAuth)
    {
        navigation.gotoPage("/plugins/servlet/embedded-crowd/configure/delegatingldap/");
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        tester.setWorkingForm("configure-delegating-ldap-form");
        // Set the required Simple fields
        tester.setFormElement("name", "Delegating LDAP Directory");
        tester.selectOption("type", "OpenLDAP");
        tester.setFormElement("hostname", getLdapServer());
        tester.setFormElement("port", "389");
        tester.setFormElement("ldapUserdn", getUserDn());
        tester.setFormElement("ldapBasedn", getBaseDn());
        tester.setFormElement("ldapPassword", getPassword());
        tester.setFormElement("ldapUserUsername", "cn");

        if (createUserOnAuth)
        {
            tester.checkCheckbox("createUserOnAuth", "true");
            tester.setFormElement("ldapAutoAddGroups", "jira-users");
            // Set the advanced fields manually - Func tests don't have javascript to do this for us
            tester.setFormElement("ldapUserObjectclass", "inetorgperson");
            tester.setFormElement("ldapUserFilter", "(objectclass=inetorgperson)");
            tester.setFormElement("ldapUserUsername", "cn");
            tester.setFormElement("ldapUserUsernameRdn", "cn");
            tester.setFormElement("ldapUserFirstname", "givenName");
            tester.setFormElement("ldapUserLastname", "sn");
            tester.setFormElement("ldapUserDisplayname", "displayName");
            tester.setFormElement("ldapUserEmail", "mail");
            tester.setFormElement("ldapUserGroup", "memberOf");
            tester.setFormElement("ldapExternalId", "entryUUID");
        }

        // Add the new Directory
        tester.submit("test");
        text.assertTextPresent("Connection test successful");

        tester.submit("save");

        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        tester.assertTextPresent("JIRA Internal Directory");
        tester.assertTextPresent("Delegating LDAP Directory");
    }

    private void deleteDelegatingLdapDirectory()
    {
        // Disable the directory
        navigation.gotoAdminSection("user_directories");
        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        WebLink link = userDirectoryTable.getTableCell(1, 4).getLinkWith("Disable");
        navigation.clickLink(link);

        WebTable table = assertions.getTableAssertions().getWebTable("directory-list");
        assertions.getTableAssertions().assertTableCellHasText(table, 1, 1, "Delegating LDAP Directory");

        // Now Delete the directory
        userDirectoryTable = new UserDirectoryTable(this);
        link = userDirectoryTable.getTableCell(1, 4).getLinkWith("Remove");
        navigation.clickLink(link);

        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        text.assertTextNotPresent(new IdLocator(tester, "embcwd"), "Delegating LDAP Directory");

    }

    private void disableLdapDirectory()
    {
        navigation.gotoAdminSection("user_directories");

        // First make sure You ar not logged in as admin via LDAP Directory
        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        assertTrue(userDirectoryTable.getTableCell(1, 1).asText().contains("LDAP Directory"));
        WebLink link = userDirectoryTable.getTableCell(1, 3).getLinkWith("Move Down");
        navigation.clickLink(link);

        navigation.logout();
        navigation.login(ADMIN_USERNAME);

        navigation.gotoAdminSection("user_directories");
        userDirectoryTable = new UserDirectoryTable(this);
        link = userDirectoryTable.getTableCell(2, 4).getLinkWith("Disable");
        navigation.clickLink(link);

        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(2).hasEnableEditRemoveSynchroniseOperations();
    }

    private void enableLdapDirectory()
    {
        navigation.gotoAdminSection("user_directories");
        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        assertTrue(userDirectoryTable.getTableCell(2, 1).asText().contains("LDAP Directory"));
        WebLink link = userDirectoryTable.getTableCell(2, 4).getLinkWith("Enable");
        navigation.clickLink(link);

        userDirectoryTable = new UserDirectoryTable(this);
        assertTrue(userDirectoryTable.getTableCell(2, 1).asText().contains("LDAP Directory"));
        link = userDirectoryTable.getTableCell(2, 3).getLinkWith("Move Up");
        navigation.clickLink(link);
    }


}