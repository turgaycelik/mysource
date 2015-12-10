package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebLink;

/**
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.LDAP, Category.TPM })
public class TestDelegatingLdapDirectoryMaintenance extends AbstractTpmLdapTest
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testAddDelegatingLdapDirectory()
    {
        navigation.gotoAdmin();
        addDirectory();

        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the new directory is added at the end
        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Delegating Ldap", getTypeDisplayName()).hasMoveUp(true).hasMoveDown(false).hasDisableEditOperations();
    }

    private void addDirectory()
    {
        // We go directly ot the add page, because we need JavaScript to drive the normal add UI.
        navigation.gotoPage("/plugins/servlet/embedded-crowd/configure/delegatingldap/");
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        // Test for all mandatory fields
        tester.setWorkingForm("configure-delegating-ldap-form");
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
        text.assertTextPresent("User name attribute is a required field");

        tester.setWorkingForm("configure-delegating-ldap-form");
        tester.setFormElement("name", "First Delegating Ldap");
        tester.selectOption("type", getTypeDisplayName());
        tester.setFormElement("hostname", getLdapServer());
        // For this test we can use AD read-only (no SSL)
        tester.setFormElement("port", "389");
        tester.setFormElement("ldapUserdn",  getUserDn());
        tester.setFormElement("ldapPassword", getPassword());
        tester.setFormElement("ldapBasedn", getBaseDn());
        tester.setFormElement("ldapUserUsername", "cn");
        tester.submit("test");

        tester.submit("save");
    }

    public void testEditDelegatingLdapDirectory()
    {
        navigation.gotoAdminSection("user_directories");
        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");

        addDirectory();

        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        WebLink link = userDirectoryTable.getTableCell(2, 4).getLinkWith("edit");
        navigation.clickLink(link);

        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        // Change the contents and save
        tester.setFormElement("name", "First Delegating Ldap X");
        tester.setFormElement("hostname", getLdapServer());
        tester.setFormElement("ldapPassword", getPassword());
        tester.submit("test");

        tester.submit("save");

        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the new directory is added at the end
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Delegating Ldap X", getTypeDisplayName()).hasMoveUp(true).hasMoveDown(false).hasDisableEditOperations();

        // Check for mandatory fields
        link = userDirectoryTable.getTableCell(2, 4).getLinkWith("edit");
        navigation.clickLink(link);

        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");
        
        // Change the contents and save
        tester.setFormElement("name", "");
        tester.setFormElement("hostname", "");
        tester.setFormElement("port", "");
        tester.setFormElement("ldapUserdn", "");
        tester.setFormElement("ldapPassword", "");
        tester.setFormElement("ldapBasedn", "");
        tester.setFormElement("ldapUserUsername", "");
        tester.submit("test");

        // Check for the errors
        text.assertTextPresent("Name is a required field.");
        text.assertTextPresent("Hostname is a required field.");
        text.assertTextPresent("Port is a required field.");
// JRA-23819
//        text.assertTextPresent("Username is a required field.");
//        text.assertTextPresent("Password is a required field.");
        text.assertTextPresent("Base DN is a required field.");
        text.assertTextPresent("User name attribute is a required field");

        tester.clickLink("configure-delegating-ldap-form-cancel");

        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the directory is unchanged
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Delegating Ldap X", getTypeDisplayName()).hasMoveUp(true).hasMoveDown(false).hasDisableEditOperations();
    }

    public void testDeleteDelegatingLdapDirectory()
    {
        navigation.gotoAdminSection("user_directories");
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");

        addDirectory();

        // Disable the second directory
        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        WebLink link = userDirectoryTable.getTableCell(2, 4).getLinkWith("Disable");
        navigation.clickLink(link);

        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Delegating Ldap (inactive)", getTypeDisplayName()).hasMoveUp(true).hasMoveDown(false).hasEnableEditRemoveOperations();

        // Now Delete the directory
        link = userDirectoryTable.getTableCell(2, 4).getLinkWith("Remove");
        navigation.clickLink(link);

        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the new directory is added at the end
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(false).hasOnlyEditOperation();
        text.assertTextNotPresent(new IdLocator(tester, "embcwd"), "First Delegating Ldap");

    }

}