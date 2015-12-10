package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebLink;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * 
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.LDAP, Category.TPM })
public class TestBrowseUserDirectories extends FuncTestCase
{
    private static final String JAACS_APPLICATION_NAME = "CousinMabel";
    private static final String JAACS_APPLICATION_CREDENTIAL = "secret";

    protected void setUpTest()
    {
        administration.restoreBlankInstance();
        navigation.gotoAdmin();
        setUpJaacsServer();
    }

    public void testEmptyData()
    {
        navigation.gotoPage("/plugins/servlet/embedded-crowd/directories/list");
        tester.setWorkingForm("");
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "The table below shows the user directories currently configured for JIRA.");
        // This used to do tester.assertButtonPresent("new-directory");
        // But Func Tests don't like that it is not a Submit Button
        tester.assertTextPresent("<button id=\"new-directory\" class=\"aui-button\">Add Directory</button>");

        // The directory browser should show a single Internal directory
        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(false).hasOnlyEditOperation();
    }

    public void testMultipleDirectories() throws IOException, SAXException
    {
        log("Add a bunch of directories");
        addDirectories();

        log("Checking directories present in the correct order");
        // assert we are back on the "View Directories" page.
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "The table below shows the user directories currently configured for JIRA.");

        UserDirectoryTable userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(false).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(2).contains("10000", "CROWD1", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(true).hasDisableEditSynchroniseOperations();
        userDirectoryTable.assertRow(3).contains("10001", "CROWD2", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(true).hasDisableEditSynchroniseOperations();
        userDirectoryTable.assertRow(4).contains("10002", "CROWD3", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(false).hasDisableEditSynchroniseOperations();

        // test the reordering of the directories
        log("Moving directory up");
        log("Checking directories present in the correct order");
        WebLink link = userDirectoryTable.getTableCell(2, 3).getLinkWith("up");

        navigation.clickLink(link);
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("10000", "CROWD1", "Atlassian Crowd").hasMoveUp(false).hasMoveDown(true).hasDisableEditSynchroniseOperations();
        userDirectoryTable.assertRow(2).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(true).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(3).contains("10001", "CROWD2", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(true).hasDisableEditSynchroniseOperations();
        userDirectoryTable.assertRow(4).contains("10002", "CROWD3", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(false).hasDisableEditSynchroniseOperations();

        log("Moving directory down");
        log("Checking directories present in the correct order");
        link = userDirectoryTable.getTableCell(2, 3).getLinkWith("down");
        navigation.clickLink(link);
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("10000", "CROWD1", "Atlassian Crowd").hasMoveUp(false).hasMoveDown(true).hasDisableEditSynchroniseOperations();
        userDirectoryTable.assertRow(2).contains("10001", "CROWD2", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(true).hasDisableEditSynchroniseOperations();
        userDirectoryTable.assertRow(3).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(true).hasMoveDown(true).hasOnlyEditOperation();
        userDirectoryTable.assertRow(4).contains("10002", "CROWD3", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(false).hasDisableEditSynchroniseOperations();

        log("Moving directory up");
        log("Checking directories present in the correct order");
        link = userDirectoryTable.getTableCell(4, 3).getLinkWith("up");
        navigation.clickLink(link);
        userDirectoryTable = new UserDirectoryTable(this);
        userDirectoryTable.assertRow(1).contains("10000", "CROWD1", "Atlassian Crowd").hasMoveUp(false).hasMoveDown(true).hasDisableEditSynchroniseOperations();
        userDirectoryTable.assertRow(2).contains("10001", "CROWD2", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(true).hasDisableEditSynchroniseOperations();
        userDirectoryTable.assertRow(3).contains("10002", "CROWD3", "Atlassian Crowd").hasMoveUp(true).hasMoveDown(true).hasDisableEditSynchroniseOperations();
        userDirectoryTable.assertRow(4).contains("1", "JIRA Internal Directory", "Internal").hasMoveUp(true).hasMoveDown(false).hasOnlyEditOperation();

        // Move the Internal directory back to the top so we can be sure we can log back in.
        userDirectoryTable = new UserDirectoryTable(this);
        link = userDirectoryTable.getTableCell(4, 3).getLinkWith("up");
        navigation.clickLink(link);
        userDirectoryTable = new UserDirectoryTable(this);
        link = userDirectoryTable.getTableCell(3, 3).getLinkWith("up");
        navigation.clickLink(link);
        userDirectoryTable = new UserDirectoryTable(this);
        link = userDirectoryTable.getTableCell(2, 3).getLinkWith("up");
        navigation.clickLink(link);
    }

    private void addDirectories()
    {
        addCrowdDirectory("CROWD1", JAACS_APPLICATION_NAME, tester.getTestContext().getBaseUrl(), JAACS_APPLICATION_CREDENTIAL);
        addCrowdDirectory("CROWD2", JAACS_APPLICATION_NAME, tester.getTestContext().getBaseUrl(), JAACS_APPLICATION_CREDENTIAL);
        addCrowdDirectory("CROWD3", JAACS_APPLICATION_NAME, tester.getTestContext().getBaseUrl(), JAACS_APPLICATION_CREDENTIAL);
    }

    private void addCrowdDirectory(final String name, final String applicationName, final String serverUrl, final String password)
    {
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
        tester.setFormElement("name", name);
        tester.setFormElement("crowdServerUrl", serverUrl);
        tester.setFormElement("applicationName", applicationName);
        tester.setFormElement("applicationPassword", password);
        tester.submit("test");

        // Change the url so we are not looping back on ourselves.  It can cause authentication infinite recursions.
        tester.setWorkingForm("configure-crowd-form");
        tester.setFormElement("crowdServerUrl", "http://dummmy.domain.priv/crowd");

        tester.submit("save");
    }

    private void setUpJaacsServer()
    {
        navigation.gotoPage("secure/project/ConfigureCrowdServer.jspa");
        tester.clickLink("crowd-add-application");
        tester.setWorkingForm("edit-crowd-application");
        tester.setFormElement("name", JAACS_APPLICATION_NAME);
        tester.setFormElement("credential", JAACS_APPLICATION_CREDENTIAL);
        tester.clickButton("edit-crowd-application-submit");

    }

}