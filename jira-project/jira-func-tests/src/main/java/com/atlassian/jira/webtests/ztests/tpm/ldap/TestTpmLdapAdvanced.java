package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;

/**
 * To be run against Active Directory or Open LDAP in TPM.
 * This test relies on TestTpmLdapSetup being run first in order to create the appropriate LDAP User Directory.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.LDAP, Category.TPM })
public class TestTpmLdapAdvanced extends AbstractTpmLdapTest
{
    public void testDisabledDirectory() throws Exception
    {
        if (assertions.getUserAssertions().userExists("wilma"))
        {
            log("User wilma was found - attempting to clean up before running test.");
            deleteUser("wilma");
        }
        assertions.getUserAssertions().assertUserDoesNotExist("wilma");

        navigation.gotoAdminSection("user_browser");

        // Add a User to the LDAP directory
        tester.clickLink("create_user");
        tester.setFormElement("username", "wilma");
        tester.setFormElement("fullname", "Wilma Flintstone");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.setFormElement("email", "wilma@bedrock.com");
        tester.submit("Create");

        assertions.getUserAssertions().assertUserExists("wilma");
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", "LDAP Directory");

        // Now lets disable the LDAP directory
        navigation.gotoAdminSection("user_directories");
        tester.clickLinkWithText("Disable");
        assertions.getTextAssertions().assertTextPresent("LDAP Directory</span>  <em>(inactive)</em>");
        assertions.getUserAssertions().assertUserDoesNotExist("wilma");

        // Now re-enable
        navigation.gotoAdminSection("user_directories");
        tester.clickLinkWithText("Enable");
        assertions.getTextAssertions().assertTextNotPresent("LDAP Directory (inactive)");
        assertions.getUserAssertions().assertUserExists("wilma");

        // cleanup - delete the user
        deleteUser("wilma");
        assertions.getUserAssertions().assertUserDoesNotExist("wilma");
    }

    public void testSynchroniseDirectory() throws Exception
    {
        if (assertions.getUserAssertions().userExists("wilma"))
        {
            log("User wilma was found - attempting to clean up before running test.");
            deleteUser("wilma");
        }
        assertions.getUserAssertions().assertUserDoesNotExist("wilma");

        navigation.gotoAdminSection("user_directories");
        synchroniseDirectory(1);
        assertions.getTextAssertions().assertTextNotPresent("Never synchronised");

        // Add a second LDAP directory that actually points to the same LDAP tree.
        if (isActiveDirectory())
        {
            log("Attempting to add an Active Directory LDAP User Directory...");
            addActiveDirectory("LDAP Directory 2");
        }
        else
        {
            log("Attempting to add an Open LDAP User Directory...");
            addOpenLdap("LDAP Directory 2");
        }

        // Confirm the directory setup:
        WebTable table = assertions.getTableAssertions().getWebTable("directory-list");
        assertions.getTableAssertions().assertTableCellHasText(table, 1, 1, "LDAP Directory");
        assertions.getTableAssertions().assertTableCellHasText(table, 2, 1, "JIRA Internal Directory");
        assertions.getTableAssertions().assertTableCellHasText(table, 3, 1, "LDAP Directory 2");

        assertions.getTextAssertions().assertTextPresent("Never synchronised");
        // Do a Synchronise on LDAP 2:
        synchroniseDirectory(3);

        // Now add a new User - it will go to LDAP 1
        // Add a User to the LDAP directory
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("create_user");
        tester.setFormElement("username", "wilma");
        tester.selectOption("directoryId", "LDAP Directory");
        tester.setFormElement("fullname", "Wilma Flintstone");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.setFormElement("email", "wilma@bedrock.com");
        tester.submit("Create");

        assertions.getUserAssertions().assertUserExists("wilma");
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", "LDAP Directory");

        // Now lets disable the LDAP directory
        navigation.gotoAdminSection("user_directories");
        tester.clickLinkWithText("Disable");
        assertions.getTextAssertions().assertTextPresent("LDAP Directory</span>  <em>(inactive)</em>");

        // Wilma should no longer exist ... unless the automatic background synch has already kicked in.
        // Don't try to assert she doesn't exist lest we hit that race condition.

        // Synchronise LDAP 2 to find the newly added user
        synchroniseDirectory(3);
        // Check that Wilma is now found for LDAP 2
        assertions.getUserAssertions().assertUserExists("wilma");
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", "LDAP Directory 2");

        // Re-enable the LDAP directory
        navigation.gotoAdminSection("user_directories");
        tester.clickLinkWithText("Enable");
        assertions.getTextAssertions().assertTextNotPresent("LDAP Directory  <em>(inactive)</em>");
        // Wilma should be considered as belonging to LDAP 1 again
        assertions.getUserAssertions().assertUserDetails("wilma", "Wilma Flintstone", "wilma@bedrock.com", "LDAP Directory");

        // Cleanup - remove LDAP 2 directory
        navigation.gotoAdminSection("user_directories");
        WebTable tblDirectoryList = new TableLocator(tester, "directory-list").getTable();
        WebLink link = tblDirectoryList.getTableCell(3, 4).getLinkWith("Disable");
        navigation.clickLink(link);
        tblDirectoryList = new TableLocator(tester, "directory-list").getTable();
        link = tblDirectoryList.getTableCell(3, 4).getLinkWith("Remove");
        navigation.clickLink(link);

        // cleanup - delete the user
        deleteUser("wilma");
        assertions.getUserAssertions().assertUserDoesNotExist("wilma");
    }

    private void addActiveDirectory(String name)
    {
        navigation.gotoPage("/plugins/servlet/embedded-crowd/configure/ldap/");
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        tester.setWorkingForm("configure-ldap-form");
        // Set the required Simple fields
        tester.setFormElement("name", name);
        tester.selectOption("type", "Microsoft Active Directory");
        tester.setFormElement("hostname", getLdapServer());
        // AD will not allow mutating operations unless you use SSL
        tester.setFormElement("port", "636");
        tester.checkCheckbox("useSSL", "true");
        tester.setFormElement("ldapUserdn", getUserDn());
        tester.setFormElement("ldapPassword", getPassword());
        tester.setFormElement("ldapBasedn", getBaseDn());

        // Set the advanced fields manually - Func tests don't have javascript to do this for us
        tester.setFormElement("ldapUserObjectclass", "user");
        tester.setFormElement("ldapUserFilter", "(&(objectCategory=Person)(sAMAccountName=*))");
        tester.setFormElement("ldapUserUsername", "sAMAccountName");
        tester.setFormElement("ldapUserUsernameRdn", "cn");
        tester.setFormElement("ldapUserFirstname", "givenName");
        tester.setFormElement("ldapUserLastname", "sn");
        tester.setFormElement("ldapUserDisplayname", "displayName");
        tester.setFormElement("ldapUserEmail", "mail");
        tester.setFormElement("ldapUserGroup", "memberOf");
        tester.setFormElement("ldapUserPassword", "unicodePwd");
        tester.setFormElement("ldapGroupObjectclass", "group");
        tester.setFormElement("ldapGroupFilter", "(objectCategory=Group)");
        tester.setFormElement("ldapGroupName", "cn");
        tester.setFormElement("ldapGroupDescription", "description");
        tester.setFormElement("ldapGroupUsernames", "member");
        tester.setFormElement("ldapPermissionOption", "READ_WRITE");

        // Add the new Directory
        tester.submit("test");
        text.assertTextPresent("Connection test successful");

        tester.submit("save");

        // Get off the advanced test page
        assertExtendedTestPageAndReturnToDirectoryList();
    }

    private void addOpenLdap(String name)
    {
        navigation.gotoPage("/plugins/servlet/embedded-crowd/configure/ldap/");
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        tester.setWorkingForm("configure-ldap-form");
        // Set the required Simple fields
        tester.setFormElement("name", name);
        tester.selectOption("type", "OpenLDAP");
        // Allow flexibility of setting up localtest.properties to run test locally against crowd-op23
        tester.setFormElement("hostname", getLdapServer());
        tester.setFormElement("port", "389");
        tester.setFormElement("ldapUserdn", getUserDn());
        tester.setFormElement("ldapBasedn", getBaseDn());
        tester.setFormElement("ldapPassword", getPassword());
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
        tester.setFormElement("ldapUserPassword", "userPassword");
        tester.setFormElement("ldapGroupObjectclass", "groupOfUniqueNames");
        tester.setFormElement("ldapGroupFilter", "(objectclass=groupOfUniqueNames)");
        tester.setFormElement("ldapGroupName", "cn");
        tester.setFormElement("ldapGroupDescription", "description");
        tester.setFormElement("ldapGroupUsernames", "uniqueMember");
        tester.setFormElement("ldapPermissionOption", "READ_WRITE");

        // Add the new Directory
        tester.submit("test");
        text.assertTextPresent("Connection test successful");

        tester.submit("save");

        // Get off the advanced test page
        assertExtendedTestPageAndReturnToDirectoryList();
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
}
