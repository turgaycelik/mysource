package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * This is a test of the general admin UI for maintaining LDAP directories.
 *
 * This class does not test a large part of the functionality of setting defaults for and visibilty of fields
 * which is deeply embedded in javascipt that this form of test cannot exercise.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.LDAP, Category.TPM })
public class TestLdapDirectoryMaintenance extends AbstractTpmLdapTest
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testAddLdapDirectory()
    {
        navigation.gotoAdmin();
        addDirectory();

        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the new directory is added at the end
        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Ldap", getTypeDisplayName() + " (Read Only)").hasMoveUp(true).hasMoveDown(false).hasDisableEditSynchroniseOperations();

        // Open the edit screen and check the values to make sure they are stored/retrieved correctly.
        WebLink link = userDirectoryTable.getTableCell(2, 4).getLinkWith("edit");
        navigation.clickLink(link);

        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        tester.setWorkingForm("configure-ldap-form");

        assertEquals("First Ldap" ,tester.getDialog().getElement("configure-ldap-form-name").getAttribute("value"));

        assertEquals(getLdapServer() ,tester.getDialog().getElement("configure-ldap-form-hostname").getAttribute("value"));
        assertEquals("389" ,tester.getDialog().getElement("configure-ldap-form-port").getAttribute("value"));
        assertEquals(getBaseDn() ,tester.getDialog().getElement("configure-ldap-form-ldapBasedn").getAttribute("value"));
        assertEquals(getUserDn() ,tester.getDialog().getElement("configure-ldap-form-ldapUserdn").getAttribute("value"));

        tester.assertTextNotPresent(getPassword());
        assertThat(tester.getDialog().getElement("configure-ldap-form-ldapPassword").getAttribute("class"), containsString("password_value_placeholder"));

        assertEquals("inetorgperson" ,tester.getDialog().getElement("configure-ldap-form-ldapUserObjectclass").getAttribute("value"));
        assertEquals("(objectclass=inetorgperson)" ,tester.getDialog().getElement("configure-ldap-form-ldapUserFilter").getAttribute("value"));
        assertEquals("cn" ,tester.getDialog().getElement("configure-ldap-form-ldapUserUsername").getAttribute("value"));
        assertEquals("cn" ,tester.getDialog().getElement("configure-ldap-form-ldapUserUsernameRdn").getAttribute("value"));
        assertEquals("givenName" ,tester.getDialog().getElement("configure-ldap-form-ldapUserFirstname").getAttribute("value"));
        assertEquals("sn" ,tester.getDialog().getElement("configure-ldap-form-ldapUserLastname").getAttribute("value"));
        assertEquals("displayName" ,tester.getDialog().getElement("configure-ldap-form-ldapUserDisplayname").getAttribute("value"));
        assertEquals("mail" ,tester.getDialog().getElement("configure-ldap-form-ldapUserEmail").getAttribute("value"));
        assertEquals("memberOf" ,tester.getDialog().getElement("configure-ldap-form-ldapUserGroup").getAttribute("value"));
        assertEquals("userPassword" ,tester.getDialog().getElement("configure-ldap-form-ldapUserPassword").getAttribute("value"));
        assertEquals("groupOfUniqueNames" ,tester.getDialog().getElement("configure-ldap-form-ldapGroupObjectclass").getAttribute("value"));
        assertEquals("(objectclass=groupOfUniqueNames)" ,tester.getDialog().getElement("configure-ldap-form-ldapGroupFilter").getAttribute("value"));
        assertEquals("cn" ,tester.getDialog().getElement("configure-ldap-form-ldapGroupName").getAttribute("value"));
        assertEquals("description" ,tester.getDialog().getElement("configure-ldap-form-ldapGroupDescription").getAttribute("value"));
        assertEquals("uniqueMember" ,tester.getDialog().getElement("configure-ldap-form-ldapGroupUsernames").getAttribute("value"));

    }

    private void addDirectory()
    {
        addDirectory(false);
    }

    private void addDirectory(boolean writable)
    {
        // We go directly ot the add page, because we need JavaScript to drive the normal add UI.
        navigation.gotoPage("/plugins/servlet/embedded-crowd/configure/ldap/");
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        // Test for all mandatory fields
        tester.setWorkingForm("configure-ldap-form");
        tester.setFormElement("name", "");
        tester.setFormElement("port", "");
        tester.submit("test");

        // Check for the errors
        text.assertTextPresent("Name is a required field.");
        text.assertTextPresent("Hostname is a required field.");
        text.assertTextPresent("Port is a required field.");
// JRA-23819
//        text.assertTextPresent("Username is a required field.");
//        text.assertTextPresent("Password is a required field.");
        text.assertTextPresent("User object class is a required field.");
        text.assertTextPresent("User object filter is a required field.");
        text.assertTextPresent("User name attribute is a required field.");
        text.assertTextPresent("First name attribute is a required field.");
        text.assertTextPresent("Last name attribute is a required field.");
        text.assertTextPresent("User display name attribute is a required field.");
        text.assertTextPresent("User email attribute is a required field.");
        text.assertTextPresent("User group attribute is a required field.");
        text.assertTextPresent("User password attribute is a required field.");
        text.assertTextPresent("Group object class is a required field.");
        text.assertTextPresent("Group object filter is a required field.");
        text.assertTextPresent("Group name attribute is a required field.");
        text.assertTextPresent("Group description attribute is a required field.");
        text.assertTextPresent("Group members attribute is a required field.");

        tester.setWorkingForm("configure-ldap-form");
        tester.setFormElement("name", "First Ldap");
        if (isActiveDirectory())
        {
            tester.selectOption("type", "Microsoft Active Directory");
        }
        else
        {
            tester.selectOption("type", "OpenLDAP");
        }
        tester.setFormElement("hostname", getLdapServer());
        // For this test we can use AD read-only (no SSL)
        tester.setFormElement("port", "389");
        tester.setFormElement("ldapBasedn", getBaseDn());
        tester.setFormElement("ldapUserdn", getUserDn());
        tester.setFormElement("ldapPassword", getPassword());

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

        if (writable)
        {
            tester.setFormElement("ldapPermissionOption", "READ_WRITE");
        }

        tester.submit("test");

        tester.submit("save");

        // Now we are forced to the "Extended test" page
        assertExtendedTestPageAndReturnToDirectoryList();
    }

    public void testEditLdapDirectory()
    {
        navigation.gotoAdminSection("user_directories");
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");

        addDirectory();

        WebTable tblDirectoryList = new TableLocator(tester, "directory-list").getTable();
        WebLink link = tblDirectoryList.getTableCell(2, 4).getLinkWith("edit");
        navigation.clickLink(link);

        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        tester.setWorkingForm("configure-ldap-form");
        tester.setFormElement("name", "First Ldap X");
        tester.setFormElement("hostname", getLdapServer());
        // For this test we can use AD read-only (no SSL)
        tester.setFormElement("port", "389");
        tester.setFormElement("ldapBasedn", getBaseDn());
        tester.setFormElement("ldapUserdn", getUserDn());
        tester.setFormElement("ldapPassword", getPassword());

        tester.setFormElement("ldapUserObjectclass", "inetorgpersonX");
        tester.setFormElement("ldapUserFilter", "(objectclass=inetorgperson)X");
        tester.setFormElement("ldapUserUsername", "cnX");
        tester.setFormElement("ldapUserUsernameRdn", "cnX");
        tester.setFormElement("ldapUserFirstname", "givenNameX");
        tester.setFormElement("ldapUserLastname", "snX");
        tester.setFormElement("ldapUserDisplayname", "displayNameX");
        tester.setFormElement("ldapUserEmail", "mailX");
        tester.setFormElement("ldapUserGroup", "memberOfX");
        tester.setFormElement("ldapUserPassword", "userPasswordX");
        tester.setFormElement("ldapGroupObjectclass", "groupOfUniqueNamesX");
        tester.setFormElement("ldapGroupFilter", "(objectclass=groupOfUniqueNames)X");
        tester.setFormElement("ldapGroupName", "cnX");
        tester.setFormElement("ldapGroupDescription", "descriptionX");
        tester.setFormElement("ldapGroupUsernames", "uniqueMemberX");

        tester.submit("test");
        tester.submit("save");
        // Now we are forced to the "Extended test" page
        assertExtendedTestPageAndReturnToDirectoryList();

        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the new directory is added at the end
        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Ldap X", getTypeDisplayName() + " (Read Only)").hasMoveUp(true).hasMoveDown(false).hasDisableEditSynchroniseOperations();

        // Open the edit screen and check the values to make sure they are stored/retrieved correctly.
        tblDirectoryList = new TableLocator(tester, "directory-list").getTable();
        link = tblDirectoryList.getTableCell(2, 4).getLinkWith("edit");
        navigation.clickLink(link);

        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        tester.setWorkingForm("configure-ldap-form");

        assertEquals("First Ldap X" ,tester.getDialog().getElement("configure-ldap-form-name").getAttribute("value"));

        assertEquals(getLdapServer() ,tester.getDialog().getElement("configure-ldap-form-hostname").getAttribute("value"));
        assertEquals("389" ,tester.getDialog().getElement("configure-ldap-form-port").getAttribute("value"));
        assertEquals(getBaseDn() ,tester.getDialog().getElement("configure-ldap-form-ldapBasedn").getAttribute("value"));
        assertEquals(getUserDn() ,tester.getDialog().getElement("configure-ldap-form-ldapUserdn").getAttribute("value"));

        tester.assertTextNotPresent(getPassword());
        assertThat(tester.getDialog().getElement("configure-ldap-form-ldapPassword").getAttribute("class"), containsString("password_value_placeholder"));

        assertEquals("inetorgpersonX" ,tester.getDialog().getElement("configure-ldap-form-ldapUserObjectclass").getAttribute("value"));
        assertEquals("(objectclass=inetorgperson)X" ,tester.getDialog().getElement("configure-ldap-form-ldapUserFilter").getAttribute("value"));
        assertEquals("cnX" ,tester.getDialog().getElement("configure-ldap-form-ldapUserUsername").getAttribute("value"));
        assertEquals("cnX" ,tester.getDialog().getElement("configure-ldap-form-ldapUserUsernameRdn").getAttribute("value"));
        assertEquals("givenNameX" ,tester.getDialog().getElement("configure-ldap-form-ldapUserFirstname").getAttribute("value"));
        assertEquals("snX" ,tester.getDialog().getElement("configure-ldap-form-ldapUserLastname").getAttribute("value"));
        assertEquals("displayNameX" ,tester.getDialog().getElement("configure-ldap-form-ldapUserDisplayname").getAttribute("value"));
        assertEquals("mailX" ,tester.getDialog().getElement("configure-ldap-form-ldapUserEmail").getAttribute("value"));
        assertEquals("memberOfX" ,tester.getDialog().getElement("configure-ldap-form-ldapUserGroup").getAttribute("value"));
        assertEquals("userPasswordX" ,tester.getDialog().getElement("configure-ldap-form-ldapUserPassword").getAttribute("value"));
        assertEquals("groupOfUniqueNamesX" ,tester.getDialog().getElement("configure-ldap-form-ldapGroupObjectclass").getAttribute("value"));
        assertEquals("(objectclass=groupOfUniqueNames)X" ,tester.getDialog().getElement("configure-ldap-form-ldapGroupFilter").getAttribute("value"));
        assertEquals("cnX" ,tester.getDialog().getElement("configure-ldap-form-ldapGroupName").getAttribute("value"));
        assertEquals("descriptionX" ,tester.getDialog().getElement("configure-ldap-form-ldapGroupDescription").getAttribute("value"));
        assertEquals("uniqueMemberX" ,tester.getDialog().getElement("configure-ldap-form-ldapGroupUsernames").getAttribute("value"));



        // Check for mandatory fields

        // Change the contents and save
        tester.setFormElement("name", "");
        tester.setFormElement("hostname", "");
        tester.setFormElement("port", "");
        tester.setFormElement("ldapBasedn", "");
        tester.setFormElement("ldapUserdn", "");
        tester.setFormElement("ldapPassword", "");

        tester.setFormElement("ldapUserObjectclass", "");
        tester.setFormElement("ldapUserFilter", "");
        tester.setFormElement("ldapUserUsername", "");
        tester.setFormElement("ldapUserUsernameRdn", "");
        tester.setFormElement("ldapUserFirstname", "");
        tester.setFormElement("ldapUserLastname", "");
        tester.setFormElement("ldapUserDisplayname", "");
        tester.setFormElement("ldapUserEmail", "");
        tester.setFormElement("ldapUserGroup", "");
        tester.setFormElement("ldapUserPassword", "");
        tester.setFormElement("ldapGroupObjectclass", "");
        tester.setFormElement("ldapGroupFilter", "");
        tester.setFormElement("ldapGroupName", "");
        tester.setFormElement("ldapGroupDescription", "");
        tester.setFormElement("ldapGroupUsernames", "");
        tester.submit("test");

        // Check for the errors
        text.assertTextPresent("Name is a required field.");
        text.assertTextPresent("Hostname is a required field.");
        text.assertTextPresent("Port is a required field.");
// JRA-23819
//        text.assertTextPresent("Username is a required field.");
//        text.assertTextPresent("Password is a required field.");
        text.assertTextPresent("User object class is a required field.");
        text.assertTextPresent("User object filter is a required field.");
        text.assertTextPresent("User name attribute is a required field.");
        text.assertTextPresent("First name attribute is a required field.");
        text.assertTextPresent("Last name attribute is a required field.");
        text.assertTextPresent("User display name attribute is a required field.");
        text.assertTextPresent("User email attribute is a required field.");
        text.assertTextPresent("User group attribute is a required field.");
        text.assertTextPresent("User password attribute is a required field.");
        text.assertTextPresent("Group object class is a required field.");
        text.assertTextPresent("Group object filter is a required field.");
        text.assertTextPresent("Group name attribute is a required field.");
        text.assertTextPresent("Group description attribute is a required field.");
        text.assertTextPresent("Group members attribute is a required field.");

        tester.clickLink("configure-ldap-form-cancel");

        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the new directory is added at the end
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Ldap X", getTypeDisplayName() + " (Read Only)").hasMoveUp(true).hasMoveDown(false).hasDisableEditSynchroniseOperations();

        // Open the edit screen and check the values to make sure they are stored/retrieved correctly.
        tblDirectoryList = new TableLocator(tester, "directory-list").getTable();
        link = tblDirectoryList.getTableCell(2, 4).getLinkWith("edit");
        navigation.clickLink(link);

        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        tester.setWorkingForm("configure-ldap-form");

        tester.submit("test");
        tester.submit("save");
        // Now we are forced to the "Extended test" page
        assertExtendedTestPageAndReturnToDirectoryList();

        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the new directory is at the end
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Ldap X", getTypeDisplayName() + " (Read Only)").hasMoveUp(true).hasMoveDown(false).hasDisableEditSynchroniseOperations();

    }

    public void testDeleteLdapDirectory()
    {
        navigation.gotoAdminSection("user_directories");
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");

        addDirectory();

        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        WebLink link = userDirectoryTable.getTableCell(2, 4).getLinkWith("Disable");
        navigation.clickLink(link);

        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Ldap (inactive)", getTypeDisplayName() + " (Read Only)").hasMoveUp(true).hasMoveDown(false).hasEnableEditRemoveSynchroniseOperations();

        // Now Delete the directory
        link = userDirectoryTable.getTableCell(2, 4).getLinkWith("Remove");
        navigation.clickLink(link);

        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the new directory is added at the end
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(false).hasOnlyEditOperation();
        text.assertTextNotPresent(new IdLocator(tester, "embcwd"), "First Ldap");
    }

    public void testCantMoveLdapDirWhenAdminLost()
    {
        //NB: this test may fail if users for some reason already exist as synchronisation causes timing problems.
        navigation.gotoAdminSection("user_directories");
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");

        addDirectory(true);

        try
        {
            UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
            userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true);
            userDirectoryTable.assertRow(2).contains("10000", "First Ldap", getTypeDisplayName() + " (Read/Write)").hasMoveUp(true).hasMoveDown(false);

            //add user to directory1
            navigation.gotoAdminSection("user_browser");
            tester.clickLink("create_user");
            String username = "jra23861";
            String password = "password";
            tester.setFormElement("username", username);
            tester.setFormElement("fullname", username);
            tester.setFormElement("password", password);
            tester.setFormElement("confirm", password);
            tester.setFormElement("email", "jra23861@example.com");
            tester.submit("Create");

            //give user admin permissions
            navigation.gotoAdminSection("user_browser");
            tester.clickLink("editgroups_" + username);
            tester.selectOption("groupsToJoin", Groups.ADMINISTRATORS);
            tester.submit("join");

            try
            {
                //add user to directory2
                navigation.gotoAdminSection("user_browser");
                tester.clickLink("create_user");
                tester.setFormElement("username", username);
                tester.setFormElement("directoryId", "10000");
                tester.setFormElement("fullname", username);
                tester.setFormElement("password", password);
                tester.setFormElement("confirm", password);
                tester.setFormElement("email", "jra23861@example.com");
                tester.submit("Create");

                try
                {
                    navigation.logout();
                    navigation.login(username, password);

                    navigation.gotoAdminSection("user_directories");
                    text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");

                    userDirectoryTable = new UserDirectoryTable(this);

                    userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true);
                    userDirectoryTable.assertRow(2).contains("10000", "First Ldap", getTypeDisplayName() + " (Read/Write)").hasMoveUp(true).hasMoveDown(false);

                    WebLink link = userDirectoryTable.getTableCell(2, 3).getLinkWith("up");
                    navigation.clickLink(link);


                    tester.assertTextPresent("You cannot move the directory without losing your system admin privileges.");


                    userDirectoryTable = new UserDirectoryTable(this);

                    userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true);
                    userDirectoryTable.assertRow(2).contains("10000", "First Ldap", getTypeDisplayName() + " (Read/Write)").hasMoveUp(true).hasMoveDown(false);
                }
                finally
                {
                    navigation.logout();
                    navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

                    // remove user from dir 2
                    tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=" + username);
                    tester.clickLink("deleteuser_link");
                    tester.submit("Delete");
                }
            }
            finally
            {
               //remove user from dir 1
                tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=" + username);
                tester.clickLink("deleteuser_link");
                tester.submit("Delete");
            }
        }
        finally
        {
            // remove directory
            navigation.gotoAdminSection("user_directories");
            UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
            WebLink link = userDirectoryTable.getTableCell(2, 4).getLinkWith("Disable");
            navigation.clickLink(link);
            userDirectoryTable = new UserDirectoryTable(this);
            link = userDirectoryTable.getTableCell(2, 4).getLinkWith("Remove");
            navigation.clickLink(link);
        }
    }
}