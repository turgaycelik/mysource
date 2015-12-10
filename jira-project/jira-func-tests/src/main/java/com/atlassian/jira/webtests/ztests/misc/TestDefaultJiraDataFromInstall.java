package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.SystemTenantOnly;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.table.ImageCell;
import com.atlassian.jira.webtests.table.LinkCell;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

/**
 * This functional test should run before all other functional tests. It's designed to check the initial setup of JIRA,
 * prior to any of the XML imports that occur in the other test classes.
 */
@SystemTenantOnly
@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.IMPORT_EXPORT, Category.SETUP, Category.ISSUE_TYPES, Category.REFERENCE_PLUGIN })
public class TestDefaultJiraDataFromInstall extends JIRAWebTest
{
    private static final String MANAGE_WATCHERS = "Manage Watchers";

    public TestDefaultJiraDataFromInstall(String name)
    {
        super(name);
    }

    public void setUp()
    {
        //overrides super.setUp() because it calls the init() method - we only want to call init once
    }

    public void testDefaultJiraData() throws SAXException
    {
        //set the base url (required for JIRAWebTest.jiraSetup())
        getTestContext().setBaseUrl(getEnvironmentData().getBaseUrl().toExternalForm());

        //ensure rhino is disabled - usually done in init(), but calls to jiraSetup() will cause HttpUnit to die
        //in a messy fashion if you haven't disabled scripting
        HttpUnitOptions.setScriptingEnabled(false);

        //if JIRA has not been setup for the first time, set it up fresh and run the tests

        if (!isJiraSetup())
        {
            init();

            _testEnterpriseDefaultJiraPermissions();
            _testEnterpriseDefaultNotificationSchemes();
            _testEnterpriseDefaultScreens();
            _testEnterpriseDefaultScreenSchemes();
            _testEnterpriseDefaultIssueTypeScreenSchemes();
            _testEnterpriseDefaultEventTypes();
            _testEnterpriseDefaultFieldsVisibility();
            _testDefaultTimeTrackingOptions();
            _testDefaultSubTaskOptions();
            _testWikiRendererAsDefaultForAllRenderableFields();

        }
        //if JIRA is setup, you probably need to promote this to the top of the list in AcceptanceTestHarness
        else
        {
            fail("TestDefaultJiraDataFromInstall needs to be prior to being setup");
        }
    }

    private void _testWikiRendererAsDefaultForAllRenderableFields()
    {
        final String[] renderableFields = {"Comment", "Description", "Environment", "Log Work"};
        for (String fieldName : renderableFields)
        {
            assertEquals("Wiki Style Renderer", administration.fieldConfigurations().defaultFieldConfiguration().getRenderer(fieldName));
        }
    }

    private void _testDefaultSubTaskOptions()
    {
        // assert Sub-tasks are enabled
        navigation.gotoAdminSection("subtasks");

        text.assertTextSequence(new WebPageLocator(tester),
                "Sub-Tasks are currently turned", "ON",
                "You can manage your sub-tasks as part of standard issue types");

        assertions.getLinkAssertions().assertLinkPresentWithExactText("//td[@class='jiraformbody']", "Disable");

        // assert the Sub-task issue types have the default Sub-task type
        WebTable subTaskTypesTable = tester.getDialog().getWebTableBySummaryOrId("sub-task-list");

        assertions.getTableAssertions().assertTableContainsRowOnce(subTaskTypesTable,
                new Object[] {"Sub-task", "The sub-task of the issue", new ImageCell(ISSUE_IMAGE_SUB_TASK), new LinkCell("EditSubTaskIssueTypes!default.jspa?id=5", "Edit")});

        // assert Sub Task appears as part of the Default Issue Type Scheme
        navigation.gotoAdminSection("issue_type_schemes");
        text.assertTextPresent(new XPathLocator(tester, "//table[@id='issuetypeschemes']//ul/li"), "Sub-task");
    }

    private void _testDefaultTimeTrackingOptions()
    {
        navigation.gotoAdminSection("timetracking");

        // time tracking should be ON by default
        tester.assertElementPresent("deactivate_submit");

        // assert default options for Time Tracking
        text.assertTextSequence(new WebPageLocator(tester),
                "To change these values deactivate and then reactivate Time Tracking",
                "The number of working hours per day is", "8",
                "The number of working days per week is", "5",
                "Time estimates will be displayed in the following format:", "pretty (e.g. 4 days, 4 hours, 30 minutes)",
                "The current default unit for time tracking is", "minute",
                "Copying of comments to work description is currently", "enabled");

        text.assertTextNotPresent(new WebPageLocator(tester), "Legacy mode is currently");
    }

    public void _testStandardDefaultFieldsVisibility()
    {
        gotoFieldConfigurationDefault();
        // Affects Version/s
        assertField("Affects Version/s", 0, false);
        // Assignee
        assertField("Assignee", 1, false);
        // Attachment
        assertField("Attachment", 2, false);
        // Component/s
        assertField("Component/s", 4, false);
        // Description
        assertField("Description", 5, false);
        // Due Date
        assertField("Due Date", 6, false);
        // Environment
        assertField("Environment", 7, false);
        // Fix Version/s
        assertField("Fix Version/s", 8, false);
        // Priority
        assertField("Priority", 10, false);
        // Reporter
        assertField("Reporter", 11, false);
        // Resolution
        assertField("Resolution", 12, false);
        // Security Level
        assertTextNotPresent("Security Level");
    }

    public void _testProfessionalDefaultFieldsVisibility()
    {
        gotoFieldConfigurationDefault();
        // Affects Version/s
        assertField("Affects Version/s", 0, false);
        // Assignee
        assertField("Assignee", 1, false);
        // Attachment
        assertField("Attachment", 2, false);
        // Component/s
        assertField("Component/s", 4, false);
        // Description
        assertField("Description", 5, false);
        // Due Date
        assertField("Due Date", 6, false);
        // Environment
        assertField("Environment", 7, false);
        // Fix Version/s
        assertField("Fix Version/s", 8, false);
        // Priority
        assertField("Priority", 10, false);
        // Reporter
        assertField("Reporter", 11, false);
        // Resolution
        assertField("Resolution", 12, false);
    }

    public void _testEnterpriseDefaultFieldsVisibility()
    {
        gotoFieldConfigurationDefault();
        // Affects Version/s
        assertField("Affects Version/s", 0, false);
        // Assignee
        assertField("Assignee", 1, false);
        // Attachment
        assertField("Attachment", 2, false);
        // Component/s
        assertField("Component/s", 4, false);
        // Description
        assertField("Description", 5, false);
        // Due Date
        assertField("Due Date", 6, false);
        // Environment
        assertField("Environment", 7, false);
        // Fix Version/s
        assertField("Fix Version/s", 8, false);
        // Priority
        assertField("Priority", 10, false);
        // Reporter
        assertField("Reporter", 11, false);
        // Resolution
        assertField("Resolution", 12, false);
        // Security Level
        assertField("Security Level", 13, false);
    }

    private void assertField(String field, int id, boolean hide)
    {
        assertTextPresent(field);
        if (hide)
        {
            assertLinkPresent("hide_" + id);
        }
    }

    public void _testEnterpriseDefaultJiraPermissions() throws SAXException
    {
        gotoPermissionSchemes();

        final XPathLocator nameLocator = new XPathLocator(tester, "//*[@href=\"EditPermissions!default.jspa?schemeId=0\"]");
        text.assertTextPresent(nameLocator, "Default Permission Scheme");

        final String[] strings = { "Default Permission Scheme", "This is the default Permission Scheme. Any new projects that are created will be assigned this scheme." };
        text.assertTextSequence(tester.getDialog().getResponseText(), strings);

        clickLinkWithText("Default Permission Scheme");

        WebTable projectPermissionsTable = getDialog().getResponse().getTableWithID("edit_project_permissions");

        log("Checking project permissions table row count");
        assertEquals(7, projectPermissionsTable.getRowCount()); //4 + 1 header row + 1 from reference plugin + 1 from the func-test plugin

        //check header on first table
        assertTableRowEquals(projectPermissionsTable, 0, new String[] {"Project Permissions", "Users / Groups / Project Roles", "Operations"});

        assertTrue(tableCellHasText(projectPermissionsTable, 1, 0, "Administer Projects"));
        assertAdminRole(projectPermissionsTable, 1);

        assertTrue(tableCellHasText(projectPermissionsTable, 2, 0, "Browse Projects"));
        assertUserRole(projectPermissionsTable, 2);

        assertTrue(tableCellHasText(projectPermissionsTable, 3, 0, "View Development Tools"));
        assertDevRole(projectPermissionsTable, 3);

        assertTrue(tableCellHasText(projectPermissionsTable, 4, 0, "View Read-Only Workflow"));
        assertUserRole(projectPermissionsTable, 4);


        WebTable issuePermissionsTable = getDialog().getResponse().getTableWithID("edit_issue_permissions");

        log("Checking issue permissions table row count");
        assertEquals(14, issuePermissionsTable.getRowCount());

        int currentRow = 0; // row index, 0 will be skipped (it's header)

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Assignable User"));
        assertDevRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Assign Issues"));
        assertDevRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Close Issues"));
        assertDevRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Create Issues"));
        assertUserRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Delete Issues"));
        assertAdminRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Edit Issues"));
        assertDevRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Link Issues"));
        assertUserRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Modify Reporter"));
        assertAdminRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Move Issues"));
        assertDevRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Resolve Issues"));
        assertDevRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Schedule Issues"));
        assertDevRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Set Issue Security"));
        assertNoRole(issuePermissionsTable, currentRow);

        assertTrue(tableCellHasText(issuePermissionsTable, ++currentRow, 0, "Transition Issues"));
        assertDevRole(issuePermissionsTable, currentRow);


        WebTable commentPermissionsTable = getDialog().getResponse().getTableWithID("edit_comments_permissions");

        log("Checking comments permissions table row count");
        assertEquals(6, commentPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(commentPermissionsTable, 1, 0, "Add Comments"));
        assertUserRole(commentPermissionsTable, 1);

        assertTrue(tableCellHasText(commentPermissionsTable, 2, 0, "Delete All Comments"));
        assertAdminRole(commentPermissionsTable, 2);

        assertTrue(tableCellHasText(commentPermissionsTable, 3, 0, "Delete Own Comments"));
        assertUserRole(commentPermissionsTable, 3);

        assertTrue(tableCellHasText(commentPermissionsTable, 4, 0, "Edit All Comments"));
        assertDevRole(commentPermissionsTable, 4);

        assertTrue(tableCellHasText(commentPermissionsTable, 5, 0, "Edit Own Comments"));
        assertUserRole(commentPermissionsTable, 5);


        WebTable timetrackingPermissionsTable = getDialog().getResponse().getTableWithID("edit_timetracking_permissions");

        log("Checking time tracking permissions table row count");
        assertEquals(6, timetrackingPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(timetrackingPermissionsTable, 1, 0, "Delete All Worklogs"));
        assertAdminRole(timetrackingPermissionsTable, 1);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 2, 0, "Delete Own Worklogs"));
        assertUserRole(timetrackingPermissionsTable, 2);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 3, 0, "Edit All Worklogs"));
        assertDevRole(timetrackingPermissionsTable, 3);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 4, 0, "Edit Own Worklogs"));
        assertUserRole(timetrackingPermissionsTable, 4);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 5, 0, "Work On Issues"));
        assertDevRole(timetrackingPermissionsTable, 5);


        WebTable attachmentsPermissionsTable = getDialog().getResponse().getTableWithID("edit_attachments_permissions");

        log("Checking attachments permissions table row count");
        assertEquals(4, attachmentsPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(attachmentsPermissionsTable, 1, 0, "Create Attachments"));
        assertUserRole(attachmentsPermissionsTable, 1);

        assertTrue(tableCellHasText(attachmentsPermissionsTable, 2, 0, "Delete All Attachments"));
        assertAdminRole(attachmentsPermissionsTable, 2);

        assertTrue(tableCellHasText(attachmentsPermissionsTable, 3, 0, "Delete Own Attachments"));
        assertUserRole(attachmentsPermissionsTable, 3);


        WebTable votersAndWatchersPermissionsTable = getDialog().getResponse().getTableWithID("edit_votersandwatchers_permissions");

        log("Checking voters & watchers permissions table row count");
        assertEquals(3, votersAndWatchersPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(votersAndWatchersPermissionsTable, 1, 0, MANAGE_WATCHERS));
        assertAdminRole(votersAndWatchersPermissionsTable, 1);

        assertTrue(tableCellHasText(votersAndWatchersPermissionsTable, 2, 0, "View Voters and Watchers"));
        assertDevRole(votersAndWatchersPermissionsTable, 2);

        log("Checking Global Permissions");
        clickOnAdminPanel("admin.globalsettings", "global_permissions");
        WebTable globalPermissionsTable = getDialog().getResponse().getTableWithID("global_perms");
        assertTrue(tableCellHasText(globalPermissionsTable, 1, 0, "JIRA System Administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 1, 1, "jira-administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 2, 0, "JIRA Administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 2, 1, "jira-administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 3, 0, "JIRA Users"));
        assertTrue(tableCellHasText(globalPermissionsTable, 3, 1, "jira-users"));
        assertTrue(tableCellHasText(globalPermissionsTable, 4, 0, "Browse Users"));
        assertTrue(tableCellHasText(globalPermissionsTable, 4, 1, "jira-developers"));
        assertTrue(tableCellHasText(globalPermissionsTable, 5, 0, "Create Shared Objects"));
        assertTrue(tableCellHasText(globalPermissionsTable, 5, 1, "jira-users"));
        assertTrue(tableCellHasText(globalPermissionsTable, 6, 0, "Manage Group Filter Subscriptions"));
        assertTrue(tableCellHasText(globalPermissionsTable, 6, 1, "jira-developers"));
        assertTrue(tableCellHasText(globalPermissionsTable, 7, 0, "Bulk Change"));
        assertTrue(tableCellHasText(globalPermissionsTable, 7, 1, "jira-users"));

    }

    private void _testEnterpriseDefaultNotificationSchemes()
    {
        navigation.gotoAdmin();
        tester.clickLink("notification_schemes");

        final String atl_token = page.getXsrfToken();
        final XPathLocator nameLocator = new XPathLocator(tester, "//*[@href=\"EditNotifications!default.jspa?atl_token=" + atl_token + "&schemeId=10000\"]");
        text.assertTextPresent(nameLocator, "Default Notification Scheme");
    }

    private void _testEnterpriseDefaultScreens()
    {
        String[] strings = { "Default Screen", "Allows to update all system fields.",
                             "Resolve Issue Screen", "Allows to set resolution, change fix versions and assign an issue.",
                             "Workflow Screen", "This screen is used in the workflow and enables you to assign issues"};

        navigation.gotoAdmin();
        tester.clickLink("field_screens");

        text.assertTextSequence(tester.getDialog().getResponseText(), strings);
        // Check assign issue screen was replaced by the workflow screen correctly
        text.assertTextNotPresent(tester.getDialog().getResponseText(), "Assign Issue Screen");
        text.assertTextNotPresent(tester.getDialog().getResponseText(), "Allows to assign an issue.");
    }

    private void _testEnterpriseDefaultScreenSchemes()
    {
        String[] strings = { "Default Screen Scheme", "Default Screen Scheme" };

        navigation.gotoAdmin();
        tester.clickLink("field_screen_scheme");

        text.assertTextSequence(tester.getDialog().getResponseText(), strings);
    }

    private void _testEnterpriseDefaultIssueTypeScreenSchemes()
    {
        String[] strings = { "Default Issue Type Screen Scheme", "The default issue type screen scheme" };

        navigation.gotoAdmin();
        tester.clickLink("issue_type_screen_scheme");

        text.assertTextSequence(tester.getDialog().getResponseText(), strings);
    }

    private void _testEnterpriseDefaultEventTypes()
    {
        String[] strings = { "Issue Created", "This is the &#39;issue created&#39; event.",
                             "Issue Updated", "This is the &#39;issue updated&#39; event.",
                             "Issue Assigned", "This is the &#39;issue assigned&#39; event.",
                             "Issue Resolved", "This is the &#39;issue resolved&#39; event.",
                             "Issue Closed", "This is the &#39;issue closed&#39; event.",
                             "Issue Commented", "This is the &#39;issue commented&#39; event.",
                             "Issue Comment Edited", "This is the &#39;issue comment edited&#39; event.",
                             "Issue Reopened", "This is the &#39;issue reopened&#39; event.",
                             "Issue Deleted", "This is the &#39;issue deleted&#39; event.",
                             "Issue Moved", "This is the &#39;issue moved&#39; event.",
                             "Work Logged On Issue", "This is the &#39;work logged on issue&#39; event.",
                             "Work Started On Issue", "This is the &#39;work started on issue&#39; event.",
                             "Work Stopped On Issue", "This is the &#39;work stopped on issue&#39; event.",
                             "Issue Worklog Updated", "This is the &#39;issue worklog updated&#39; event.",
                             "Issue Worklog Deleted", "This is the &#39;issue worklog deleted&#39; event.",
                             "Generic Event", "This is the &#39;generic event&#39; event.",
                           };

        navigation.gotoAdmin();
        tester.clickLink("eventtypes");

        text.assertTextSequence(tester.getDialog().getResponseText(), strings);
    }

    public void _testProfessionalDefaultJiraPermissions() throws SAXException
    {
        gotoPermissionSchemes();
        clickLinkWithText("Default Permission Scheme");


        WebTable projectPermissionsTable = getDialog().getResponse().getTableWithID("edit_project_permissions");

        log("Checking project permissions table row count");
        assertEquals(4, projectPermissionsTable.getRowCount()); //3 + 1 header row

        //check header on first table
        assertTableRowEquals(projectPermissionsTable, 0, new String[] {"Project Permissions", "Groups / Project Roles", "Operations"});

        assertTrue(tableCellHasText(projectPermissionsTable, 1, 0, "Administer Projects"));
        assertAdminRole(projectPermissionsTable, 1);

        assertTrue(tableCellHasText(projectPermissionsTable, 2, 0, "Browse Projects"));
        assertUserRole(projectPermissionsTable, 2);

        assertTrue(tableCellHasText(projectPermissionsTable, 3, 0, "View Issue Source Tab"));
        assertDevRole(projectPermissionsTable, 3);


        WebTable issuePermissionsTable = getDialog().getResponse().getTableWithID("edit_issue_permissions");

        log("Checking issue permissions table row count");
        assertEquals(12, issuePermissionsTable.getRowCount());

        assertTrue(tableCellHasText(issuePermissionsTable, 1, 0, "Create Issues"));
        assertUserRole(issuePermissionsTable, 1);

        assertTrue(tableCellHasText(issuePermissionsTable, 2, 0, "Edit Issues"));
        assertDevRole(issuePermissionsTable, 2);

        assertTrue(tableCellHasText(issuePermissionsTable, 3, 0, "Schedule Issues"));
        assertDevRole(issuePermissionsTable, 3);

        assertTrue(tableCellHasText(issuePermissionsTable, 4, 0, "Move Issues"));
        assertDevRole(issuePermissionsTable, 4);

        assertTrue(tableCellHasText(issuePermissionsTable, 5, 0, "Assign Issues"));
        assertDevRole(issuePermissionsTable, 5);

        assertTrue(tableCellHasText(issuePermissionsTable, 6, 0, "Assignable User"));
        assertDevRole(issuePermissionsTable, 6);

        assertTrue(tableCellHasText(issuePermissionsTable, 7, 0, "Resolve Issues"));
        assertDevRole(issuePermissionsTable, 7);

        assertTrue(tableCellHasText(issuePermissionsTable, 8, 0, "Close Issues"));
        assertDevRole(issuePermissionsTable, 8);

        assertTrue(tableCellHasText(issuePermissionsTable, 9, 0, "Modify Reporter"));
        assertAdminRole(issuePermissionsTable, 9);

        assertTrue(tableCellHasText(issuePermissionsTable, 10, 0, "Delete Issues"));
        assertAdminRole(issuePermissionsTable, 10);

        assertTrue(tableCellHasText(issuePermissionsTable, 11, 0, "Link Issues"));
        assertDevRole(issuePermissionsTable, 11);


        WebTable commentPermissionsTable = getDialog().getResponse().getTableWithID("edit_comments_permissions");

        log("Checking comments permissions table row count");
        assertEquals(6, commentPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(commentPermissionsTable, 1, 0, "Add Comments"));
        assertUserRole(commentPermissionsTable, 1);

        assertTrue(tableCellHasText(commentPermissionsTable, 2, 0, "Edit All Comments"));
        assertDevRole(commentPermissionsTable, 2);

        assertTrue(tableCellHasText(commentPermissionsTable, 3, 0, "Edit Own Comments"));
        assertUserRole(commentPermissionsTable, 3);

        assertTrue(tableCellHasText(commentPermissionsTable, 4, 0, "Delete All Comments"));
        assertAdminRole(commentPermissionsTable, 4);

        assertTrue(tableCellHasText(commentPermissionsTable, 5, 0, "Delete Own Comments"));
        assertUserRole(commentPermissionsTable, 5);


        WebTable timetrackingPermissionsTable = getDialog().getResponse().getTableWithID("edit_timetracking_permissions");

        log("Checking time tracking permissions table row count");
        assertEquals(6, timetrackingPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(timetrackingPermissionsTable, 1, 0, "Work On Issues"));
        assertDevRole(timetrackingPermissionsTable, 1);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 2, 0, "Edit Own Worklogs"));
        assertUserRole(timetrackingPermissionsTable, 2);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 3, 0, "Edit All Worklogs"));
        assertDevRole(timetrackingPermissionsTable, 3);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 4, 0, "Delete Own Worklogs"));
        assertUserRole(timetrackingPermissionsTable, 4);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 5, 0, "Delete All Worklogs"));
        assertAdminRole(timetrackingPermissionsTable, 5);


        WebTable attachmentsPermissionsTable = getDialog().getResponse().getTableWithID("edit_attachments_permissions");

        log("Checking attachments permissions table row count");
        assertEquals(4, attachmentsPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(attachmentsPermissionsTable, 1, 0, "Create Attachments"));
        assertUserRole(attachmentsPermissionsTable, 1);

        assertTrue(tableCellHasText(attachmentsPermissionsTable, 2, 0, "Delete All Attachments"));
        assertAdminRole(attachmentsPermissionsTable, 2);

        assertTrue(tableCellHasText(attachmentsPermissionsTable, 3, 0, "Delete Own Attachments"));
        assertUserRole(attachmentsPermissionsTable, 3);


        WebTable votersAndWatchersPermissionsTable = getDialog().getResponse().getTableWithID("edit_votersandwatchers_permissions");

        log("Checking voters & watchers permissions table row count");
        assertEquals(3, votersAndWatchersPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(votersAndWatchersPermissionsTable, 1, 0, "View Voters and Watchers"));
        assertDevRole(votersAndWatchersPermissionsTable, 1);

        assertTrue(tableCellHasText(votersAndWatchersPermissionsTable, 2, 0, MANAGE_WATCHERS));
        assertAdminRole(votersAndWatchersPermissionsTable, 2);

        log("Checking Global Permissions");
        clickOnAdminPanel("admin.globalsettings", "global_permissions");
        WebTable globalPermissionsTable = getDialog().getResponse().getTableWithID("global_perms");
        assertTrue(tableCellHasText(globalPermissionsTable, 1, 0, "JIRA System Administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 1, 1, "jira-administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 2, 0, "JIRA Administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 2, 1, "jira-administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 3, 0, "JIRA Users"));
        assertTrue(tableCellHasText(globalPermissionsTable, 3, 1, "jira-users"));
        assertTrue(tableCellHasText(globalPermissionsTable, 4, 0, "Browse Users"));
        assertTrue(tableCellHasText(globalPermissionsTable, 4, 1, "jira-developers"));
        assertTrue(tableCellHasText(globalPermissionsTable, 5, 0, "Create Shared Objects"));
        assertTrue(tableCellHasText(globalPermissionsTable, 5, 1, "jira-users"));
        assertTrue(tableCellHasText(globalPermissionsTable, 6, 0, "Manage Group Filter Subscriptions"));
        assertTrue(tableCellHasText(globalPermissionsTable, 6, 1, "jira-developers"));
        assertTrue(tableCellHasText(globalPermissionsTable, 7, 0, "Bulk Change"));
        assertTrue(tableCellHasText(globalPermissionsTable, 7, 1, "jira-users"));

    }

    public void _testStandardDefaultJiraPermissions() throws SAXException
    {
        gotoPermissionSchemes();
        clickLinkWithText("Default Permission Scheme");


        WebTable projectPermissionsTable = getDialog().getResponse().getTableWithID("edit_project_permissions");

        log("Checking project permissions table row count");
        assertEquals(4, projectPermissionsTable.getRowCount()); //3 + 1 header row

        //check header on first table
        assertTableRowEquals(projectPermissionsTable, 0, new String[] {"Project Permissions", "Groups / Project Roles", "Operations"});

        assertTrue(tableCellHasText(projectPermissionsTable, 1, 0, "Administer Projects"));
        assertAdminRole(projectPermissionsTable, 1);

        assertTrue(tableCellHasText(projectPermissionsTable, 2, 0, "Browse Projects"));
        assertUserRole(projectPermissionsTable, 2);

        assertTrue(tableCellHasText(projectPermissionsTable, 3, 0, "View Issue Source Tab"));
        assertDevRole(projectPermissionsTable, 3);


        WebTable issuePermissionsTable = getDialog().getResponse().getTableWithID("edit_issue_permissions");

        log("Checking issue permissions table row count");
        assertEquals(12, issuePermissionsTable.getRowCount());

        assertTrue(tableCellHasText(issuePermissionsTable, 1, 0, "Create Issues"));
        assertUserRole(issuePermissionsTable, 1);

        assertTrue(tableCellHasText(issuePermissionsTable, 2, 0, "Edit Issues"));
        assertDevRole(issuePermissionsTable, 2);

        assertTrue(tableCellHasText(issuePermissionsTable, 3, 0, "Schedule Issues"));
        assertDevRole(issuePermissionsTable, 3);

        assertTrue(tableCellHasText(issuePermissionsTable, 4, 0, "Move Issues"));
        assertDevRole(issuePermissionsTable, 4);

        assertTrue(tableCellHasText(issuePermissionsTable, 5, 0, "Assign Issues"));
        assertDevRole(issuePermissionsTable, 5);

        assertTrue(tableCellHasText(issuePermissionsTable, 6, 0, "Assignable User"));
        assertDevRole(issuePermissionsTable, 6);

        assertTrue(tableCellHasText(issuePermissionsTable, 7, 0, "Resolve Issues"));
        assertDevRole(issuePermissionsTable, 7);

        assertTrue(tableCellHasText(issuePermissionsTable, 8, 0, "Close Issues"));
        assertDevRole(issuePermissionsTable, 8);

        assertTrue(tableCellHasText(issuePermissionsTable, 9, 0, "Modify Reporter"));
        assertAdminRole(issuePermissionsTable, 9);

        assertTrue(tableCellHasText(issuePermissionsTable, 10, 0, "Delete Issues"));
        assertAdminRole(issuePermissionsTable, 10);

        assertTrue(tableCellHasText(issuePermissionsTable, 11, 0, "Link Issues"));
        assertDevRole(issuePermissionsTable, 11);


        WebTable commentPermissionsTable = getDialog().getResponse().getTableWithID("edit_comments_permissions");

        log("Checking comments permissions table row count");
        assertEquals(6, commentPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(commentPermissionsTable, 1, 0, "Add Comments"));
        assertUserRole(commentPermissionsTable, 1);

        assertTrue(tableCellHasText(commentPermissionsTable, 2, 0, "Edit All Comments"));
        assertDevRole(commentPermissionsTable, 2);

        assertTrue(tableCellHasText(commentPermissionsTable, 3, 0, "Edit Own Comments"));
        assertUserRole(commentPermissionsTable, 3);

        assertTrue(tableCellHasText(commentPermissionsTable, 4, 0, "Delete All Comments"));
        assertAdminRole(commentPermissionsTable, 4);

        assertTrue(tableCellHasText(commentPermissionsTable, 5, 0, "Delete Own Comments"));
        assertUserRole(commentPermissionsTable, 5);


        WebTable timetrackingPermissionsTable = getDialog().getResponse().getTableWithID("edit_timetracking_permissions");

        log("Checking time tracking permissions table row count");
        assertEquals(6, timetrackingPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(timetrackingPermissionsTable, 1, 0, "Work On Issues"));
        assertDevRole(timetrackingPermissionsTable, 1);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 2, 0, "Edit Own Worklogs"));
        assertUserRole(timetrackingPermissionsTable, 2);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 3, 0, "Edit All Worklogs"));
        assertDevRole(timetrackingPermissionsTable, 3);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 4, 0, "Delete Own Worklogs"));
        assertUserRole(timetrackingPermissionsTable, 4);
        assertTrue(tableCellHasText(timetrackingPermissionsTable, 5, 0, "Delete All Worklogs"));
        assertAdminRole(timetrackingPermissionsTable, 5);

        WebTable attachmentsPermissionsTable = getDialog().getResponse().getTableWithID("edit_attachments_permissions");

        log("Checking attachments permissions table row count");
        assertEquals(4, attachmentsPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(attachmentsPermissionsTable, 1, 0, "Create Attachments"));
        assertUserRole(attachmentsPermissionsTable, 1);

        assertTrue(tableCellHasText(attachmentsPermissionsTable, 2, 0, "Delete All Attachments"));
        assertAdminRole(attachmentsPermissionsTable, 2);

        assertTrue(tableCellHasText(attachmentsPermissionsTable, 3, 0, "Delete Own Attachments"));
        assertUserRole(attachmentsPermissionsTable, 3);


        WebTable votersAndWatchersPermissionsTable = getDialog().getResponse().getTableWithID("edit_votersandwatchers_permissions");

        log("Checking voters & watchers permissions table row count");
        assertEquals(3, votersAndWatchersPermissionsTable.getRowCount());

        assertTrue(tableCellHasText(votersAndWatchersPermissionsTable, 1, 0, "View Voters and Watchers"));
        assertDevRole(votersAndWatchersPermissionsTable, 1);

        assertTrue(tableCellHasText(votersAndWatchersPermissionsTable, 2, 0, MANAGE_WATCHERS));
        assertAdminRole(votersAndWatchersPermissionsTable, 2);

        log("Checking Global Permissions");
        clickOnAdminPanel("admin.globalsettings", "global_permissions");
        WebTable globalPermissionsTable = getDialog().getResponse().getTableWithID("global_perms");
        assertTrue(tableCellHasText(globalPermissionsTable, 1, 0, "JIRA System Administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 1, 1, "jira-administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 2, 0, "JIRA Administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 2, 1, "jira-administrators"));
        assertTrue(tableCellHasText(globalPermissionsTable, 3, 0, "JIRA Users"));
        assertTrue(tableCellHasText(globalPermissionsTable, 3, 1, "jira-users"));
        assertTrue(tableCellHasText(globalPermissionsTable, 4, 0, "Browse Users"));
        assertTrue(tableCellHasText(globalPermissionsTable, 4, 1, "jira-developers"));
        assertTrue(tableCellHasText(globalPermissionsTable, 5, 0, "Bulk Change"));
        assertTrue(tableCellHasText(globalPermissionsTable, 5, 1, "jira-users"));
    }

    private void assertAdminRole(WebTable permissionsTable, int row)
    {
        assertTrue(tableCellHasText(permissionsTable, row, 1, "(Administrators)"));
    }

    private void assertUserRole(WebTable permissionsTable, int row)
    {
        assertTrue(tableCellHasText(permissionsTable, row, 1, "(Users)"));
    }

    private void assertDevRole(WebTable permissionsTable, int row)
    {
        assertTrue(tableCellHasText(permissionsTable, row, 1, "(Developers)"));
    }

    private void assertNoRole(WebTable permissionsTable, int row)
    {
        assertTrue(tableCellDoesNotHaveText(permissionsTable, row, 1, "(Administrators)"));
        assertTrue(tableCellDoesNotHaveText(permissionsTable, row, 1, "(Users)"));
        assertTrue(tableCellDoesNotHaveText(permissionsTable, row, 1, "(Developers)"));
    }
}
