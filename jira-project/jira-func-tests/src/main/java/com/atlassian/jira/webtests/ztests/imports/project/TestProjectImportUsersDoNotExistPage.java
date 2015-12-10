package com.atlassian.jira.webtests.ztests.imports.project;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebTable;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

/**
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.PROJECT_IMPORT })
public class TestProjectImportUsersDoNotExistPage extends AbstractProjectImportTestCase
{

    private File tempFile;

    protected void setUpTest()
    {
        // We always need to restore the data and write it out to a tmp file whos path we know
        this.administration.restoreData("TestProjectImportUsersDoNotExist.xml");
        // We don't need to delete the file, as the export will handle overwrite. It might help us stay unique.
        tempFile = this.administration.exportDataToFile("TestProjectImportUsersDoNotExist_out.xml");
        File jiraImportDirectory = new File(administration.getJiraHomeDirectory(), "import");
        try
        {
            FileUtils.copyFileToDirectory(tempFile, jiraImportDirectory);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not copy file " + tempFile.getAbsolutePath() + " to the import directory in jira home " + jiraImportDirectory, e);
        }
    }

    protected void tearDownTest()
    {
//        tempFile.delete();
    }

    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only")
    public void testProjectImportMissingOptionalUsersExtMgmt() throws Exception
    {
        // Delete the Monkey project
        this.navigation.gotoAdminSection("view_projects");
        tester.clickLink("delete_project_10001");
        tester.submit("Delete");

        // Betty and Barney are missing - Betty is optional
        backdoor.usersAndGroups().addUser("barney");

        // Now toggle the allow external user management flag
        this.administration.generalConfiguration().setExternalUserManagement(true);

        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getAbsolutePath());
        tester.submit();

        advanceThroughWaitingPage();
        tester.assertTextPresent("Project Import: Select Project to Import");

        tester.selectOption("projectKey", "monkey");
        tester.submit("Next");

        advanceThroughWaitingPage();

        // There should be errors on the summary page
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are '1' user(s) referenced that are in use in the project and missing from the current system. External user management is enabled so the import is unable to create the user(s).");
        text.assertTextPresentHtmlEncoded("You may want to add the user(s) to the system before performing the import but the import can proceed without them. Click the 'View Details' link to see a full list of user(s) that are in use.");

        HttpUnitOptions.setScriptingEnabled(true);
        tester.clickLinkWithText("View Details");

        tester.assertTextPresent("Missing Optional Users");
        tester.assertTextPresent("1 optional user(s) are referenced in the data you are trying to import");
        tester.assertTextPresent("been enabled therefore this import can not create users");
        tester.assertTextNotPresent("To see all the users, use the export link.");
        // We should be showing betty
        // Assert the table 'usersdonotexist'
        final WebTable table = tester.getDialog().getWebTableBySummaryOrId("usersdonotexist");
        assertEquals(2, table.getRowCount());
        assertTrue(table.getTableCell(0, 0).asText().indexOf("Username") != -1);
        assertEquals("betty", table.getTableCell(1, 0).asText());
        assertEquals("", table.getTableCell(1, 1).asText());
        assertEquals("", table.getTableCell(1, 2).asText());

        // Check that cancel takes us back to the summary screen
        tester.clickLink("back-link");
        HttpUnitOptions.setScriptingEnabled(false);
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are '1' user(s) referenced that are in use in the project and missing from the current system. External user management is enabled so the import is unable to create the user(s).");
    }

    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test.")
    public void testProjectImportMissingMandatoryUsersExtMgmt() throws Exception
    {
        // Delete the Monkey project
        this.navigation.gotoAdminSection("view_projects");
        tester.clickLink("delete_project_10001");
        tester.submit("Delete");

        // Betty and Barney are missing - Betty is optional
        backdoor.usersAndGroups().addUser("betty");

        // Now toggle the allow external user management flag
        this.administration.generalConfiguration().setExternalUserManagement(true);

        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getAbsolutePath());
        tester.submit();

        advanceThroughWaitingPage();
        tester.assertTextPresent("Project Import: Select Project to Import");

        tester.selectOption("projectKey", "monkey");
        tester.submit("Next");

        advanceThroughWaitingPage();

        // There should be errors on the summary page
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are 1 required user(s) that are missing from the current system. External user management is enabled so the import is unable to create the user(s). You must add the user(s) to the system before the import can proceed. Click the 'View Details' link to see a full list of user(s) that are required.");

        HttpUnitOptions.setScriptingEnabled(true);
        tester.clickLinkWithText("View Details");

        tester.assertTextPresent("Missing Mandatory Users");
        tester.assertTextPresent("1 required user(s) are referenced in the data you are trying to import");
        tester.assertTextPresent("been enabled therefore this import can not create users");
        tester.assertTextNotPresent("To see all the users, use the export link.");

        // We should be showing fred and not admin
        // Assert the table 'usersdonotexist'
        final WebTable table = tester.getDialog().getWebTableBySummaryOrId("usersdonotexist");
        assertEquals(2, table.getRowCount());
        assertTrue(table.getTableCell(0, 0).asText().indexOf("Username") != -1);
        assertEquals("barney", table.getTableCell(1, 0).asText());
        assertEquals("", table.getTableCell(1, 1).asText());
        assertEquals("", table.getTableCell(1, 2).asText());

        // Check that back takes us back to the summary screen
        tester.clickLink("back-link");
        HttpUnitOptions.setScriptingEnabled(false);
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are 1 required user(s) that are missing from the current system. External user management is enabled so the import is unable to create the user(s). You must add the user(s) to the system before the import can proceed. Click the 'View Details' link to see a full list of user(s) that are required.");
    }

    public void testProjectImportMissingMandatoryUsersCantAutoCreate() throws Exception
    {
        // Delete the Monkey project
        this.navigation.gotoAdminSection("view_projects");
        tester.clickLink("delete_project_10001");
        tester.submit("Delete");

        // Betty and Barney are missing - Betty is optional
        backdoor.usersAndGroups().addUser("betty");

        // Ensure we have ext user management off.
        this.administration.generalConfiguration().setExternalUserManagement(false);

        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getAbsolutePath());
        tester.submit();

        advanceThroughWaitingPage();
        tester.assertTextPresent("Project Import: Select Project to Import");

        tester.selectOption("projectKey", "monkey");
        tester.submit("Next");

        advanceThroughWaitingPage();

        // There should be errors on the summary page
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are '1' required user(s) that JIRA can not automatically create.");

        HttpUnitOptions.setScriptingEnabled(true);
        tester.clickLinkWithText("View Details");

        assertions.getTextAssertions().assertTextPresentHtmlEncoded("There are 1 required user(s) that do not currently exist in JIRA and the import cannot create them. These user(s) need to exist for the import to proceed and we are unable to automatically create them because their details do not exist in the backup XML data.");
        tester.assertTextNotPresent("To see all the users, use the export link.");
        // We should be showing fred and not admin
        // Assert the table 'usersdonotexist'
        final WebTable table = tester.getDialog().getWebTableBySummaryOrId("usersdonotexist");
        assertEquals(2, table.getRowCount());
        assertTrue(table.getTableCell(0, 0).asText().indexOf("Username") != -1);
        assertEquals("barney", table.getTableCell(1, 0).asText());
        assertEquals("", table.getTableCell(1, 1).asText());
        assertEquals("", table.getTableCell(1, 2).asText());

        // Check that back takes us back to the summary screen
        tester.clickLink("back-link");
        HttpUnitOptions.setScriptingEnabled(false);
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are '1' required user(s) that JIRA can not automatically create.");
    }

    public void testProjectImportMissingOptionalUsersCantAutoCreate() throws Exception
    {
        // Delete the Monkey project
        this.navigation.gotoAdminSection("view_projects");
        tester.clickLink("delete_project_10001");
        tester.submit("Delete");

        // Betty and Barney are missing - Betty is optional
        backdoor.usersAndGroups().addUser("barney");

        // Ensure we have ext user management off.
        this.administration.generalConfiguration().setExternalUserManagement(false);

        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getAbsolutePath());
        tester.submit();

        advanceThroughWaitingPage();
        tester.assertTextPresent("Project Import: Select Project to Import");

        tester.selectOption("projectKey", "monkey");
        tester.submit("Next");

        advanceThroughWaitingPage();

        // There should be errors on the summary page
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are '1' user(s) referenced that JIRA can not automatically create. You may want to create these users before performing the import.");

        HttpUnitOptions.setScriptingEnabled(true);
        tester.clickLinkWithText("View Details");

        assertions.getTextAssertions().assertTextPresentHtmlEncoded("There are 1 optional user(s) that do not currently exist in JIRA and the import cannot create them. We are unable to automatically create them because their details do not exist in the backup XML data. The user(s) are not required to exist for the import to proceed, however you may wish to create them.");
        tester.assertTextNotPresent("To see all the users, use the export link.");
        // We should be showing fred and not admin
        // Assert the table 'usersdonotexist'
        final WebTable table = tester.getDialog().getWebTableBySummaryOrId("usersdonotexist");
        assertEquals(2, table.getRowCount());
        assertTrue(table.getTableCell(0, 0).asText().indexOf("Username") != -1);
        assertEquals("betty", table.getTableCell(1, 0).asText());
        assertEquals("", table.getTableCell(1, 1).asText());
        assertEquals("", table.getTableCell(1, 2).asText());

        // Check that back takes us back to the summary screen
        tester.clickLink("back-link");
        HttpUnitOptions.setScriptingEnabled(false);
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are '1' user(s) referenced that JIRA can not automatically create. You may want to create these users before performing the import.");
    }

    public void testProjectImportUsersToAutoCreate() throws Exception
    {
        // Delete the Monkey project
        this.navigation.gotoAdminSection("view_projects");
        tester.clickLink("delete_project_10001");
        tester.submit("Delete");

        // Betty and Barney are missing - and don't have details, so cant be created.
        backdoor.usersAndGroups().addUser("barney");
        backdoor.usersAndGroups().addUser("betty");
        // Delete Fred and Wilma, so we have 2 users we CAN crete.
        backdoor.usersAndGroups().deleteUser(FRED_USERNAME);
        backdoor.usersAndGroups().deleteUser("wilma");

        // Ensure we have ext user management off.
        this.administration.generalConfiguration().setExternalUserManagement(false);

        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getAbsolutePath());
        tester.submit();

        advanceThroughWaitingPage();
        tester.assertTextPresent("Project Import: Select Project to Import");

        tester.selectOption("projectKey", "monkey");
        tester.submit("Next");

        advanceThroughWaitingPage();

        // There should be errors on the summary page
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are '2' users that will be automatically created if the import continues.");

        HttpUnitOptions.setScriptingEnabled(true);
        tester.clickLinkWithText("View Details");

        assertions.getTextAssertions().assertTextPresentHtmlEncoded("There are 2 user(s) referenced in the data you are trying to import that do not currently exist in JIRA. The project import will automatically create these user(s).");
        tester.assertTextNotPresent("To see all the users, use the export link.");
        // We should be showing fred and not admin
        // Assert the table 'usersdonotexist'
        final WebTable table = tester.getDialog().getWebTableBySummaryOrId("usersdonotexist");
        assertEquals(3, table.getRowCount());
        assertTrue(table.getTableCell(0, 0).asText().indexOf("Username") != -1);
        assertEquals(FRED_USERNAME, table.getTableCell(1, 0).asText());
        assertEquals(FRED_FULLNAME, table.getTableCell(1, 1).asText());
        assertEquals("fred@example.com", table.getTableCell(1, 2).asText());
        assertEquals("wilma", table.getTableCell(2, 0).asText());
        assertEquals("Wilma O'Flinstone", table.getTableCell(2, 1).asText());
        assertEquals("wilma@example.com", table.getTableCell(2, 2).asText());

        // Check that back takes us back to the summary screen
        tester.clickLink("back-link");
        HttpUnitOptions.setScriptingEnabled(false);
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are '2' users that will be automatically created if the import continues.");
    }

    public void testProjectImportUsersToAutoCreateWithUserCountLimit() throws Exception
    {
        // Delete the Monkey project
        this.navigation.gotoAdminSection("view_projects");
        tester.clickLink("delete_project_10001");
        tester.submit("Delete");

        // Betty and Barney are missing - and don't have details, so cant be created.
        backdoor.usersAndGroups().addUser("barney");
        backdoor.usersAndGroups().addUser("betty");
        // Delete Fred and Wilma, so we have 2 users we CAN crete.
        backdoor.usersAndGroups().deleteUser(FRED_USERNAME);
        backdoor.usersAndGroups().deleteUser("wilma");

        // Ensure we have ext user management off.
        this.administration.generalConfiguration().setExternalUserManagement(false);

        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getAbsolutePath());
        tester.submit();

        advanceThroughWaitingPage();
        tester.assertTextPresent("Project Import: Select Project to Import");

        tester.selectOption("projectKey", "monkey");
        tester.submit("Next");

        advanceThroughWaitingPage();

        // There should be errors on the summary page
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are '2' users that will be automatically created if the import continues.");

        HttpUnitOptions.setScriptingEnabled(true);
        // Ask the request to limit the user list to 1 result.
        tester.gotoPage("/secure/admin/ProjectImportMissingUsersAutoCreate.jspa?userCountLimit=1");

        tester.assertTextPresent("There are 2 user(s) referenced in the data you are trying to import that do not currently exist in JIRA. The project import will automatically create these user(s).");
        tester.assertTextPresent("Displaying the first 1 users. To see all the users, use the export link.");
        // We should be showing fred and not admin
        // Assert the table 'usersdonotexist'
        final WebTable table = tester.getDialog().getWebTableBySummaryOrId("usersdonotexist");
        assertEquals(2, table.getRowCount());
        assertTrue(table.getTableCell(0, 0).asText().indexOf("Username") != -1);
        assertEquals(FRED_USERNAME, table.getTableCell(1, 0).asText());
        assertEquals(FRED_FULLNAME, table.getTableCell(1, 1).asText());
        assertEquals("fred@example.com", table.getTableCell(1, 2).asText());

        // Check that back takes us back to the summary screen
        tester.clickLink("back-link");
        HttpUnitOptions.setScriptingEnabled(false);
        tester.assertTextPresent("The results of automatic mapping are displayed below");
        text.assertTextPresentHtmlEncoded("There are '2' users that will be automatically created if the import continues.");
    }

    public void testXmlExport() throws IOException, SAXException, ParserConfigurationException, TransformerException
    {
        try
        {
            // Delete the Monkey project
            this.navigation.gotoAdminSection("view_projects");
            tester.clickLink("delete_project_10001");
            tester.submit("Delete");

            // Betty and Barney are missing - and don't have details, so cant be created.
            backdoor.usersAndGroups().addUser("barney");
            backdoor.usersAndGroups().addUser("betty");
            // Delete Fred and Wilma, so we have 2 users we CAN crete.
            backdoor.usersAndGroups().deleteUser(FRED_USERNAME);
            backdoor.usersAndGroups().deleteUser("wilma");

            // Ensure we have ext user management off.
            this.administration.generalConfiguration().setExternalUserManagement(false);

            // Lets try our import
            this.navigation.gotoAdminSection("project_import");

            // Get to the project select page
            tester.setWorkingForm("project-import");
            tester.assertTextPresent("Project Import: Select Backup File");
            tester.setFormElement("backupXmlPath", tempFile.getAbsolutePath());
            tester.submit();

            advanceThroughWaitingPage();
            tester.assertTextPresent("Project Import: Select Project to Import");

            tester.selectOption("projectKey", "monkey");
            tester.submit("Next");

            advanceThroughWaitingPage();

            // There should be errors on the summary page
            tester.assertTextPresent("The results of automatic mapping are displayed below");
            text.assertTextPresentHtmlEncoded("There are '2' users that will be automatically created if the import continues.");

            HttpUnitOptions.setScriptingEnabled(true);
            // Ask the request to limit the user list to 1 result.
            tester.gotoPage("/secure/admin/ProjectImportMissingUsersAutoCreate.jspa?userCountLimit=1");

            tester.assertTextPresent("There are 2 user(s) referenced in the data you are trying to import that do not currently exist in JIRA. The project import will automatically create these user(s).");
            tester.assertTextPresent("Displaying the first 1 users. To see all the users, use the export link.");
            // We should be showing fred and not admin
            // Assert the table 'usersdonotexist'
            final WebTable table = tester.getDialog().getWebTableBySummaryOrId("usersdonotexist");
            assertEquals(2, table.getRowCount());
            assertTrue(table.getTableCell(0, 0).asText().indexOf("Username") != -1);
            assertEquals(FRED_USERNAME, table.getTableCell(1, 0).asText());
            assertEquals(FRED_FULLNAME, table.getTableCell(1, 1).asText());
            assertEquals("fred@example.com", table.getTableCell(1, 2).asText());

            // Finally do the xmlExport
            tester.clickLinkWithText("Export to XML");

            String responseText = tester.getDialog().getResponseText();
            // Note that we include an apostrophe that needs to be escaped to test the XML escaping.
            String expected = "\n<users>\n"
                              + "        <user>\n"
                              + "            <name>fred</name>\n"
                              + "            <fullname>" + FRED_FULLNAME + "</fullname>\n"
                              + "            <email>fred@example.com</email>\n"
                              + "            <properties>\n"
                              + "                <property>\n"
                              + "                <key>bspropertykey</key>\n"
                              + "                    <value>&lt;/value&gt;</value>\n"
                              + "                </property>\n"
                              + "            </properties>\n"
                              + "        </user>\n"
                              + "        <user>\n"
                              + "            <name>wilma</name>\n"
                              + "            <fullname>Wilma O&apos;Flinstone</fullname>\n"
                              + "            <email>wilma@example.com</email>\n"
                              + "            <properties>\n"
                              + "            </properties>\n"
                              + "        </user>\n"
                              + "</users>";
            assertEquals(expected, responseText);
        }
        finally
        {
            // Go back to the world
            tester.gotoPage("/secure/admin/ProjectImportSelectBackup!default.jspa");
        }
    }
}
