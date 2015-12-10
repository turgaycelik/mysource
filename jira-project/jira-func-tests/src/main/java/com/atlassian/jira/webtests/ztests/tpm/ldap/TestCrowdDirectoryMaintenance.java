package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebLink;

/**
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.LDAP, Category.TPM })
public class TestCrowdDirectoryMaintenance extends FuncTestCase
{
    private static final String JAACS_APPLICATION_NAME = "CousinMabel";
    private static final String JAACS_APPLICATION_CREDENTIAL = "secret";

    protected void setUpTest()
    {
        administration.restoreBlankInstance();
        setUpJaacsServer();
    }

    public void testAddCrowdDirectory()
    {
        addDirectory();

        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the new directory is added at the end
        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Crowd", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(false).hasDisableEditSynchroniseOperations();
    }

    private void addDirectory()
    {
        // We go directly to the add page, because we need JavaScript to drive the normal add UI.
        navigation.gotoPage("/plugins/servlet/embedded-crowd/configure/crowd/");
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        // Test for all mandatory fields
        tester.setWorkingForm("configure-crowd-form");
        tester.setFormElement("name", "");
        tester.submit("test");
        // Check for the errors
        text.assertTextPresent("Name is a required field.");
        text.assertTextPresent("Server URL is a required field.");
        text.assertTextPresent("Application name is a required field.");
        text.assertTextPresent("Application password is a required field.");

        tester.setWorkingForm("configure-crowd-form");
        tester.setFormElement("name", "First Crowd");
        tester.setFormElement("crowdServerUrl", tester.getTestContext().getBaseUrl());
        tester.setFormElement("applicationName", JAACS_APPLICATION_NAME);
        tester.setFormElement("applicationPassword", JAACS_APPLICATION_CREDENTIAL);

        tester.submit("test");

        tester.assertTextPresent("Connection test successful");

        // Change the url so we are not looping back on ourselves.  It can cause authentication infinite recursions.
        tester.setWorkingForm("configure-crowd-form");
        tester.setFormElement("crowdServerUrl", "http://dummmy.domain.priv/crowd");

        tester.submit("save");
    }

    public void testEditCrowdDirectory()
    {
        navigation.gotoAdminSection("user_directories");
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");

        addDirectory();

        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        WebLink link = userDirectoryTable.getTableCell(2, 4).getLinkWith("edit");
        navigation.clickLink(link);

        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        // Change the contents and save
        tester.setFormElement("name", "First Crowd X");
        tester.setFormElement("crowdServerUrl", tester.getTestContext().getBaseUrl());
        tester.setFormElement("applicationName", JAACS_APPLICATION_NAME);
        tester.setFormElement("applicationPassword", JAACS_APPLICATION_CREDENTIAL);
        tester.submit("test");

        // Change the url so we are not looping back on ourselves.  It can cause authentication infinite recursions.
        tester.setWorkingForm("configure-crowd-form");
        tester.setFormElement("crowdServerUrl", "http://dummmy.domain.priv/crowd");

        tester.submit("save");

        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the new directory is added at the end
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Crowd X", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(false).hasDisableEditSynchroniseOperations();

        // Check for mandatory fields
        link = userDirectoryTable.getTableCell(2, 4).getLinkWith("edit");
        navigation.clickLink(link);

        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        // Change the contents and save
        tester.setFormElement("name", "");
        tester.setFormElement("crowdServerUrl", "");
        tester.setFormElement("applicationName", "");
        tester.setFormElement("applicationPassword", "");
        tester.submit("test");

        // Check for the errors
        text.assertTextPresent("Name is a required field.");
        text.assertTextPresent("Server URL is a required field.");
        text.assertTextPresent("Application name is a required field.");

        tester.clickLink("configure-crowd-form-cancel");

        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        // assert the directory is unchanged
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Crowd X", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(false).hasDisableEditSynchroniseOperations();
    }

    public void testDeleteCrowdDirectory()
    {
        navigation.gotoAdminSection("user_directories");
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");

        addDirectory();

        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        WebLink link = userDirectoryTable.getTableCell(2, 4).getLinkWith("Disable");
        navigation.clickLink(link);

        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "First Crowd  (inactive)", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(false).hasEnableEditRemoveSynchroniseOperations();

        // Now Delete the directory
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "First Crowd");
        link = userDirectoryTable.getTableCell(2, 4).getLinkWith("Remove");
        navigation.clickLink(link);

        // assert we are back on the "View Directories" page.
        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(false).hasOnlyEditOperation();
        // assert the directory is gone
        text.assertTextNotPresent(new IdLocator(tester, "embcwd"), "First Crowd");

    }

    private void setUpJaacsServer()
    {
        navigation.gotoAdmin();

        navigation.gotoPage("secure/project/ConfigureCrowdServer.jspa");
        tester.clickLink("crowd-add-application");
        tester.setWorkingForm("edit-crowd-application");
        tester.setFormElement("name", JAACS_APPLICATION_NAME);
        tester.setFormElement("credential", JAACS_APPLICATION_CREDENTIAL);
        tester.clickButton("edit-crowd-application-submit");

}
}