package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.config.FuncProperties;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.LicenseKeys;
import com.google.common.collect.ImmutableMap;
import com.meterware.httpunit.WebTable;

import java.io.File;
import java.io.IOException;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.LICENSING, Category.DATABASE })
public class TestSetup extends FuncTestCase
{
    private static final String CONFIGURE_APP_PROPERTIES_TITLE = "Set Up Application Properties";
    private static final String SET_UP_LICENSE = "Adding Your License Key";
    private static final String SET_UP_ADMIN_ACCOUNT_TITLE = "Set Up Administrator Account";
    private static final String SET_UP_EMAIL_NOTIFICATIONS_TITLE = "Set Up Email Notifications";

    /**
     * Try to navigate to all Setup URLs to ensure Setup cannot be run again once it has already been run
     */
    public void testSetupCannotBeRunTwice()
    {
        administration.restoreBlankInstance();
        String[] actions = new String[] {
                "SetupDatabase.jspa", "SetupApplicationProperties.jspa", "SetupApplicationProperties!default.jspa",
                "SetupAdminAccount.jspa", "SetupAdminAccount!default.jspa",
                "SetupMailNotifications.jspa", "SetupMailNotifications!default.jspa",
                "SetupComplete.jspa", "SetupComplete!default.jspa",
                "SetupImport.jspa", "SetupImport!default.jspa" };

        String[] views = new String[] {
                "setup-db.jsp", "setup-application-properties.jsp", "setup-admin-account.jsp",
                "setup-mail-notifications.jsp", "setup-import.jsp" };

        for (String action : actions)
        {
            tester.gotoPage("/secure/" + action);
            assertSetupAlreadyLong();
        }

        for (String view : views)
        {
            tester.gotoPage("/views/" + view);
            assertSetupAlreadyShort();
        }
    }

    public void testMissingTitle() throws Exception
    {
        // Revert to not set up state
        gotoSetUpApplicationProperties();
        tester.submit();
        // We should not be allowed to continue
        tester.assertTextPresent(CONFIGURE_APP_PROPERTIES_TITLE);
        tester.assertTextPresent("You must specify a title.");
    }

    /**
     * Invalid base URLs should be rejected.
     */
    public void testInvalidBaseUrls() throws Exception
    {
        String[] invalidBaseUrls = {
                "",
                "http",
                "http://",
                "http://*&^%$#@",
                "http://example url.com:8090",
                "ldap://example.url.com:8090",
                "http://example.url.com:not_a_port",
                "http://example.url.com:8090/invalid path"
        };

        gotoSetUpApplicationProperties();
        for (String invalidBaseUrl : invalidBaseUrls)
        {
            tester.setFormElement("baseURL", invalidBaseUrl);
            tester.submit();
            tester.assertTextPresent("The URL specified is not valid.");
        }
    }

    public void testMissingLicense() throws Exception
    {
        // Revert to not set up state
        doConfigureApplicationPropertiesAndBundle();
        tester.setWorkingForm("setupLicenseForm");
        tester.submit();
        // We should not be allowed to continue
        tester.assertTextPresent(SET_UP_LICENSE);
    }

    public void testInvalidLicense() throws Exception
    {
        doConfigureApplicationPropertiesAndBundle();
        tester.setWorkingForm("setupLicenseForm");
        tester.setFormElement("setupLicenseKey", "blah");
        tester.submit();
        tester.assertTextPresent(SET_UP_LICENSE);
    }

    public void testInvalidSmtpPorts() throws Exception
    {
        doConfigureApplicationPropertiesAndBundle();
        doSetupLicense();
        doSetUpAdminAccount();

        //Lets try an invalid ports and make sure they don't work.
        tester.checkRadioOption("noemail","false");
        tester.setFormElement("serverName", "localhost");
        tester.setFormElement("port", "-1");
        tester.submit("finish");
        tester.assertTextPresent("SMTP port must be a number between 0 and 65535");

        tester.setFormElement("serverName", "localhost");
        tester.setFormElement("port", String.valueOf(0xFFFF + 1));
        tester.submit("finish");
        tester.assertTextPresent("SMTP port must be a number between 0 and 65535");
    }

    public void testSetupWithDefaulExctDirectories() throws IOException
    {
        // Revert to not set up state
        gotoSetUpApplicationProperties();
        // Fill in mandatory fields
        tester.setWorkingForm("jira-setupwizard");
        tester.setFormElement("title", "My JIRA");
        // Submit Step 'Configure Application Properties' with Default paths.
        tester.submit();
        //skip choosing bundle
        tester.gotoPage("/secure/SetupLicense!default.jspa");
        // Configure the license
        doSetupLicense();
        // Finish setup with standard values
        doSetUpAdminAccountAndEmailNotifications();

        // Now assert JIRA is setup as expected:

        // Attachments
        navigation.gotoAdminSection("attachments");
        assertions.assertNodeHasText(new CssLocator(tester, "h2"), "Attachments");
        // Assert the cells in table 'AttachmentSettings'.
        WebTable AttachmentSettings = tester.getDialog().getWebTableBySummaryOrId("table-AttachmentSettings");
        // Assert row 1: |Allow Attachments|ON|
        assertions.assertNodeHasText(new XPathLocator(tester, tableAttachmentSettingsRowHeading(1)), "Allow Attachments");
        assertions.assertNodeHasText(new XPathLocator(tester, tableAttachmentSettingsRowValue(1)), "ON");
        // Assert row 2: |Attachment Path|Default Directory [/home/mlassau/jira_homes/jira_trunk/data/attachments]|
        assertions.assertNodeHasText(new XPathLocator(tester, tableAttachmentSettingsRowHeading(2)), "Attachment Path");
        assertions.assertNodeHasText(new XPathLocator(tester, tableAttachmentSettingsRowValue(2)), "Default Directory [");

        // Indexes
        navigation.gotoAdminSection("indexing");
        tester.assertTextPresent("Re-Indexing");

        // Automated Backups
        administration.services().goTo();
        tester.assertTextPresent("Backup Service");
        tester.assertTextPresent("<strong>USE_DEFAULT_DIRECTORY:</strong> true");
        tester.assertTextNotPresent("DIR_NAME:");

        assertTimeTrackingActivationAndDefaultValues();
        assertIssueLinking();
    }

    private String tableAttachmentSettingsRowHeading(int rowNumber)
    {
        return "//table[@id='table-AttachmentSettings']/tbody/tr[" + rowNumber + "]/td/strong";
    }


    private String tableAttachmentSettingsRowValue(int rowNumber)
    {
        return "//table[@id='table-AttachmentSettings']/tbody/tr[" + rowNumber + "]/td";
    }

    public void testSetupImportMissingFilename() throws IOException
    {
        restoreEmptyInstance();
        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");

        // Use custom path
        tester.setFormElement("filename", "");
        tester.submit();

        // We should not be allowed to continue
        tester.assertTextPresent("Import Existing Data");
        tester.assertTextPresent("You must enter the location of an XML file.");
        tester.assertTextNotPresent("You must specify a location for index files");
    }

    public void testSetupImportInvalidLicense() throws IOException
    {
        restoreEmptyInstance();
        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");

        // Use custom path
        tester.setFormElement("filename", File.createTempFile("import", ".xml").getAbsolutePath());
        tester.setFormElement("license", "wrong");
        tester.submit();
        administration.waitForRestore();

        // We should not be allowed to continue
        tester.assertTextPresent("Import Existing Data");
        tester.assertTextPresent("Invalid JIRA license key specified.");
        tester.assertTextNotPresent("You must enter the location of an XML file.");
    }

    public void testSetupImportWithDodgyIndexPath() throws IOException
    {
        restoreEmptyInstance();

        //By creating a file for the index path, we'll force the failure of the index path directory creation
        File indexPath = File.createTempFile("testXmlImportWithInvalidIndexDirectory", null);
        indexPath.createNewFile();
        indexPath.deleteOnExit();

        File dataFile = administration.replaceTokensInFile("TestSetupInvalidIndexPath.xml", EasyMap.build("@@INDEX_PATH@@", indexPath.getAbsolutePath()));

        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");

        // Use default index path
        // We import an XML file with an old license (please do not update license in this file).
        // Now we prove that the optional license is actually used.
        tester.setFormElement("filename", dataFile.getAbsolutePath());
        tester.submit();
        administration.waitForRestore();

        text.assertTextPresent(new WebPageLocator(tester), "Cannot write to index directory. Check that the application server and JIRA have permissions to write to: " + indexPath.getAbsolutePath());
    }

    public void testSetupImportWithDodgyAttachmentPath() throws IOException
    {
        restoreEmptyInstance();

        //By creating a file for the index path, we'll force the failure of the index path directory creation
        File attachmentPath = File.createTempFile("testXmlImportWithInvalidAttachmentDirectory", null);
        attachmentPath.createNewFile();
        attachmentPath.deleteOnExit();

        File dataFile = administration.replaceTokensInFile("TestSetupInvalidAttachmentPath.xml", EasyMap.build("@@ATTACHMENT_PATH@@", attachmentPath.getAbsolutePath()));

        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");

        // Use default index path
        // We import an XML file with an old license (please do not update license in this file).
        // Now we prove that the optional license is actually used.
        tester.setFormElement("filename", dataFile.getAbsolutePath());
        tester.submit();
        administration.waitForRestore();

        text.assertTextPresent(new WebPageLocator(tester), "Cannot write to attachment directory. Check that the application server and JIRA have permissions to write to: " + attachmentPath.getAbsolutePath());
    }


    public void testSetupImportShouldDisplayAnErrorWhenAttemptingToDowngradeFromAnAllowedVersion() throws Exception
    {
        restoreEmptyInstance();
        File dataFile = administration.replaceTokensInFile("TestSetupDowngrade.xml", ImmutableMap.of("@@VERSION@@", "4.0"));

        // try to import the XML backup, which has a higher buildnumber
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.setFormElement("filename", dataFile.getAbsolutePath());
        tester.submit();
        administration.waitForRestore();

        // test that the attempted downgrade error message is shown and the user is given the option of ignoring the
        // error and downgrading anyway.
        text.assertTextPresent(locator.css("div.error"), "You are attempting to import data from JIRA X");
        text.assertTextPresent(locator.css("div.error"), "Click here to acknowledge this error and proceed anyway.");
        assertions.assertNodeExists(locator.css("a#acknowledgeDowngradeError"));
    }

    public void testSetupImportShouldAllowDowngradeOnceDowngradeErrorHasBeenAcknowledged() throws Exception
    {
        restoreEmptyInstance();
        File dataFile = administration.replaceTokensInFile("TestSetupDowngrade.xml", ImmutableMap.of("@@VERSION@@", "4.0"));

        // try to import the XML backup, which has a higher buildnumber
        tester.gotoPage("secure/SetupImport!default.jspa?downgradeAnyway=true"); // force downgradeAnyway=true
        tester.setFormElement("filename", dataFile.getAbsolutePath());
        tester.submit();
        administration.waitForRestore();

        // make sure import worked.
        tester.assertTextPresent("You have finished importing your existing data, JIRA is ready to use.  Please log in and get started.");
    }


    public void testSetupImportDefaultIndexDirectory() throws IOException
    {
        restoreEmptyInstance();
        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");

        // Use default index path
        // We import an XML file with an old license (please do not update license in this file).
        // Now we prove that the optional license is actually used.
        tester.setFormElement("filename", new File(environmentData.getXMLDataLocation(), "oldlicense.xml").getAbsolutePath());
        // need to set a new license, or we won't be allowed to log in.
        tester.setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        tester.submit();
        administration.waitForRestore();
        tester.assertTextPresent("You have finished importing your existing data, JIRA is ready to use.  Please log in and get started.");

        // Now assert JIRA is setup with the Default Index directory
        navigation.disableWebSudo();
        navigation.login(ADMIN_USERNAME);
        navigation.gotoAdminSection("indexing");
        tester.assertTextPresent("Re-Indexing");
    }

    public void testSetupImportDefaultsForSetupComplete() throws IOException
    {
        restoreEmptyInstance();
        // Go to SetupImport
        tester.gotoPage("secure/SetupImport!default.jspa");
        tester.assertTextPresent("Import Existing Data");

        // Use default index path
        // We import an XML file with an old license (please do not update license in this file).
        // Now we prove that the optional license is actually used.
        tester.setFormElement("filename", new File(environmentData.getXMLDataLocation(), "oldlicense.xml").getAbsolutePath());
        // need to set a new license, or we won't be allowed to log in.
        tester.setFormElement("license", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        tester.submit();
        administration.waitForRestore();
        tester.assertTextPresent("You have finished importing your existing data, JIRA is ready to use.  Please log in and get started.");

        // Now assert JIRA is setup with sub tasks disabled
        navigation.disableWebSudo();
        navigation.login(ADMIN_USERNAME);

        assertSubTasksDisabled();

        assertDefaultTextRendererIsSetForAllRenderableFields();
    }

    private void assertSubTasksDisabled()
    {
        assertFalse("Sub-tasks were enabled when they shouldn't have been", administration.subtasks().isEnabled());
    }

    private void assertDefaultTextRendererIsSetForAllRenderableFields()
    {
        final String[] renderableFields = { "Comment", "Description", "Environment" };
        for (String fieldName : renderableFields)
        {
            assertEquals("Default Text Renderer", administration.fieldConfigurations().defaultFieldConfiguration().getRenderer(fieldName));
        }
    }

    private void restoreEmptyInstance()
    {
        administration.restoreNotSetupInstance();
    }

    private void assertTimeTrackingActivationAndDefaultValues()
    {
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
        tester.assertTextPresent("The number of working hours per day is <b>8");
        tester.assertTextPresent("The number of working days per week is <b>5");
    }

    private void assertIssueLinking()
    {
        tester.gotoPage("secure/admin/ViewLinkTypes!default.jspa");
        WebPageLocator page = new WebPageLocator(tester);
        text.assertTextPresent(page,"Issue linking is currently ON.");
        text.assertTextSequence(page, new String[] { "Blocks", "blocks", "is blocked by" });
        text.assertTextSequence(page, new String[] { "Cloners", "clones", "is cloned by" });
        text.assertTextSequence(page, new String[] { "Duplicate", "duplicates", "is duplicated by" });
        text.assertTextSequence(page, new String[] { "Relates", "relates to", "relates to" });
    }

    private void assertSetupAlreadyLong()
    {
        tester.assertTextPresent("JIRA Setup has already completed");
        tester.assertTextPresent("It seems that you have tried to set up JIRA when this process has already been done.");
    }

    private void assertSetupAlreadyShort()
    {
        tester.assertTextPresent("JIRA has already been set up.");
    }

    private void doConfigureApplicationPropertiesAndBundle()
    {
        gotoSetUpApplicationProperties();

        // Fill in mandatory fields
        tester.setWorkingForm("jira-setupwizard");
        tester.setFormElement("title", "TestSetup JIRA");
        // Submit Step 'Set Up Application Properties' with Default paths.
        tester.submit();

        //skip choosing bundle
        tester.gotoPage("/secure/SetupLicense!default.jspa");
    }

    private void doSetUpAdminAccountAndEmailNotifications()
    {
        doSetUpAdminAccount();
        doSetUpEmailNotifications();
    }

    private void doSetUpEmailNotifications()
    {
        log("Noemail");
        tester.submit("finish");
        log("Noemail");
        // During SetupComplete, the user is automatically logged in
        // Assert that the user is logged in by checking if the profile link is present
        tester.assertLinkPresent("header-details-user-fullname");
        navigation.disableWebSudo();
    }

    private void doSetUpAdminAccount()
    {
        tester.assertTextPresent(SET_UP_ADMIN_ACCOUNT_TITLE);
        tester.setFormElement("username", ADMIN_USERNAME);
        tester.setFormElement("password", ADMIN_USERNAME);
        tester.setFormElement("confirm", ADMIN_USERNAME);
        tester.setFormElement("fullname", "Mary Magdelene");
        tester.setFormElement("email", "admin@example.com");
        tester.submit();
    }

    private void gotoSetUpApplicationProperties()
    {
        restoreEmptyInstance();
        tester.gotoPage("secure/SetupApplicationProperties.jspa");
        tester.assertTextPresent(CONFIGURE_APP_PROPERTIES_TITLE);
    }

    private void doSetupLicense()
    {
        tester.assertTextPresent(SET_UP_LICENSE);
        tester.setWorkingForm("setupLicenseForm");
        tester.setFormElement("setupLicenseKey", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        tester.submit();
        tester.assertTextPresent(SET_UP_ADMIN_ACCOUNT_TITLE);
    }

    private String getDowngradeAllowedVersion()
    {
        return FuncProperties.get("jira.downgrade.minimum.build.version");
    }

}
