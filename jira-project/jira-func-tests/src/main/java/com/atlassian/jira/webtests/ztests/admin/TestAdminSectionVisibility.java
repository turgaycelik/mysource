package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.junit.Ignore;

/**
 * Tests the admin/sys-admin visibility of pages in the admin section.
 *
 * @since v3.12
 */
@Ignore ("Disabled pending more investigation --lmiranda")
@WebTest ({Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestAdminSectionVisibility extends JIRAWebTest
{
    public TestAdminSectionVisibility(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    /** Check all the links that a SysAdmin should be able to use. */
    public void testSysAdminCanSeePages()
    {
        navigation.gotoAdmin();

        clickLink("backup_data");
        assertTextPresent("Backup JIRA data");

        clickLink("restore_data");
        assertTextPresent("Restore JIRA data from Backup");

        clickLink("jelly_runner");
        assertTextPresent("Either specify a path to a Jelly script file or paste");
        submit("Run now");
        assertTextPresent("Jelly script completed successfully.");

        clickLink("listeners");
        assertTextPresent("Listeners are used for performing certain actions");
        clickLinkWithText("Built-in Listeners");

        clickLink("services");
        assertTextPresent("Add a new service");

        clickLink("license_details");
        assertTextPresent("You can use the Update License form");

        clickLink("outgoing_mail");
        assertTextPresent("The table below shows the SMTP mail server");
        clickLinkWithText("Configure new SMTP mail server");
        assertTextPresent("Use this page to add a new SMTP mail server");

        clickLink("incoming_mail");
        assertTextPresent("The table below shows the POP / IMAP mail server");
        clickLinkWithText("Add POP / IMAP mail server");
        assertTextPresent("Use this page to add a new POP / IMAP mail server");

        clickLink("workflows");
        clickLinkWithText("import a workflow from XML");
        assertTextPresent("It is possible to import a predefined workflow");

        clickLink("attachments");
        assertTextPresent("To enable a user to attach files,");

        clickLinkWithText("Edit Configuration");
        clickLink("scheduler_details");
        assertTextPresent("This page shows you the properties of the JIRA internal scheduler");

        clickLink("integrity_checker");
        assertTextPresent("Select one or more integrity checks from");

        clickLink("ldap");
        assertTextPresent("This page helps you configure JIRA to authenticate");

        clickLink("logging_profiling");
        assertTextPresent("This page allows you to change the level at which JIRA logs");

        clickLink("view_projects");
        assertTextPresent("Administration");
        assertTextPresent("Below is the list of all 2  projects for this installation of JIRA.");
        assertHelpLinkWithStringInUrlPresent("Defining+a+Project");
    }

    public void testAdminCantSeeProtectedPages()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");
            String[] urlsToCheck = new String[] {
                    "/secure/admin/OutgoingMailServers.jspa",
                    "/secure/admin/IncomingMailServers.jspa",
                    "/secure/admin/AddSmtpMailServer!default.jspa",
                    "/secure/admin/XmlBackup!default.jspa",
                    "/secure/admin/XmlRestore!default.jspa",
                    "/secure/admin/views/ExternalImport.jspa",
                    "/secure/admin/views/BugzillaImport!default.jspa",
                    "/secure/admin/views/MantisImport!default.jspa",
                    "/secure/admin/views/FogBugzImport!default.jspa",
                    "/secure/admin/views/CsvImporter!default.jspa",
                    "/secure/admin/util/JellyRunner!default.jspa",
                    "/secure/admin/jira/IntegrityChecker!default.jspa",
                    "/secure/admin/jira/LDAPConfigurer!default.jspa",
                    "/secure/admin/jira/ViewLicense.jspa",
                    "/secure/admin/jira/ViewListeners!default.jspa",
                    "/secure/admin/jira/ViewLogging.jspa",
                    "/secure/admin/jira/ConfigureLogging!default.jspa?loggerName=root",
                    "/secure/admin/jira/SchedulerAdmin.jspa",
                    "/secure/admin/jira/ViewServices!default.jspa",
                    "/secure/admin/workflows/ImportWorkflowFromXml!default.jspa",
                    "/secure/project/EnterpriseSelectProjectRepository!default.jspa?projectId=10000"
            };

            checkUrlsForNoPerm(urlsToCheck);
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login("root", "root");
            restoreBlankInstance();
        }
    }

    /** Check all the links that an Admin should not be able to see. */
    public void testAdminCantSeeLinks()
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");
            navigation.gotoAdmin();

            assertLinkNotPresent("outgoing_mail");
            assertLinkNotPresent("incoming_mail");
            assertLinkNotPresent("backup_data");
            assertLinkNotPresent("restore_data");
            assertLinkNotPresent("jelly_runner");
            assertLinkNotPresent("integrity_checker");
            assertLinkNotPresent("ldap");
            assertLinkNotPresent("license_details");
            assertLinkNotPresent("listeners");
            assertLinkNotPresent("logging_profiling");
            assertLinkNotPresent("scheduler_details");
            assertLinkNotPresent("services");
            clickLink("workflows");
            assertLinkNotPresentWithText("import a workflow from XML");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login("root", "root");
            restoreBlankInstance();
        }
    }

    private void checkUrlsForNoPerm(String[] urls)
    {
        for (String url : urls)
        {
            gotoPage(url);
            assertTextPresent("emember my login on this computer");
        }
    }
}
