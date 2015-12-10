package com.atlassian.jira.webtests.ztests.imports.project;

import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryField;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryList;
import com.atlassian.jira.functest.framework.changehistory.ChangeHistorySet;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.parser.comment.Comment;
import com.atlassian.jira.functest.framework.parser.issue.ViewIssueDetails;
import com.atlassian.jira.functest.framework.parser.worklog.Worklog;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.jira.testkit.client.restclient.ProjectClient;
import com.atlassian.jira.testkit.client.restclient.ProjectRole;
import com.atlassian.jira.testkit.client.restclient.ProjectRoleClient;
import com.atlassian.jira.testkit.client.restclient.Version;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Func tests the project import results screen and the errors leading up to that screen.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.PROJECT_IMPORT, Category.DATABASE })
public class TestProjectImportResults extends AbstractProjectImportTestCase
{
    private static final String CHANGE_HISTORY = "History";
    private static final String PAGE_USER_BROWSER = "/secure/admin/user/UserBrowser.jspa";

    public void testProjectNullAssigneeTypeUnassignedOn() throws Exception
    {
        _testProjectNullAssigneeType("TestProjectImportNoProjectWithUnassignedOn.xml", "Unassigned",
                Project.AssigneeType.UNASSIGNED);
    }

    public void testProjectNullAssigneeTypeUnassignedOff() throws Exception
    {
        _testProjectNullAssigneeType("TestProjectImportNoProjectWithUnassignedOff.xml", "Project Lead",
                Project.AssigneeType.PROJECT_LEAD);
    }

    // JRA-19699: testing what happens when "assigneetype" is not defined in the Project XML that you are trying to import
    private void _testProjectNullAssigneeType(final String currentSystemXML, final String defaultAssignee,
            final Project.AssigneeType defaultAssigneeCode) throws Exception
    {
        File tempFile = null;
        try
        {
            // do first import with blank instance (i.e. create project)
            tempFile = doProjectImport("TestProjectImportNoAssigneeType.xml", currentSystemXML);
            doImportAndAssertDefaultAssignee(defaultAssignee, defaultAssigneeCode);


            // do second import with existing project (i.e. update project details
            importToPreImportSummaryPage(tempFile);
            doImportAndAssertDefaultAssignee(defaultAssignee, defaultAssigneeCode);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    private void doImportAndAssertDefaultAssignee(final String defaultAssignee, final Project.AssigneeType defaultAssigneeType)
    {
        // Make sure we are ready to import
        tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
        tester.assertSubmitButtonPresent("Import");
        tester.submit("Import");
        advanceThroughWaitingPage();

        // because we currently allow Unassigned issues, we will default to Unassigned
        text.assertTextSequence(new IdLocator(tester, "systemfields"), "Default Assignee", defaultAssignee);

        assertThat(new ProjectClient(environmentData).get("MKY").assigneeType, equalTo(defaultAssigneeType));
    }

    public void testProjectNotEmptyWhenTryingToImport() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportResults.xml", "TestProjectImportResults2.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            // Now lets go add an issue to the monkey project
            this.navigation.issue().createIssue("monkey", "Bug", "I am breaking the project import");

            // Jump back to the import and have at it!
            tester.gotoPage("/secure/admin/ProjectImportSummary!default.jspa");
            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the select project page
            tester.assertTextPresent("Select Project to Import");
            text.assertTextPresentHtmlEncoded("The existing project with key 'MKY' contains '1' issues.");
            tester.assertTextPresent("The project import was aborted before it began because the project is no longer importable.");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testCustomFieldNoLongerExistsTryingToImport() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportResults.xml", "TestProjectImportResults2.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            // Now lets go and delete a required custom field
            tester.gotoPage("/secure/admin/DeleteCustomField!default.jspa?id=10030");
            tester.submit("Delete");

            // Jump back to the import and have at it!
            tester.gotoPage("/secure/admin/ProjectImportSummary!default.jspa");
            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the select project page
            tester.assertTextPresent("Project Import: Pre-Import Summary");
            tester.assertTextPresent("The data mappings have produced errors, you can not import this project until all errors have been resolved. See below for details.");
            tester.assertTextPresent("The project import was aborted before it began because the project import mappings are no longer valid.");
            text.assertTextPresentHtmlEncoded("The custom field 'Cascading Select CF' of type 'Select List (cascading)' is required for the import but does not exist in the current JIRA instance.");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testCreateUsers() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportStandardSimpleData.xml", "TestProjectImportStandardSimpleDataNoProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));
            assertThat(projectDetailsResults, containsString("Lead: " + FRED_FULLNAME));
            assertThat(projectDetailsResults, containsString("URL: http://monkeyproject.example.com"));
            assertThat(projectDetailsResults, containsString("Default Assignee: Project Lead"));
            assertThat(projectDetailsResults, containsString("Components: 3"));
            assertThat(projectDetailsResults, containsString("Versions: 3"));

            xPathLocator = new XPathLocator(tester, "//div[@id='customfields']/ul");
            String userRoleIssueResults = xPathLocator.getText();

            assertThat(userRoleIssueResults, containsString("Users: 2 out of 2"));
            assertThat(userRoleIssueResults, containsString("Administrators: 1 users, 1 groups"));
            assertThat(userRoleIssueResults, containsString("Developers: 1 users, 2 groups"));
            assertThat(userRoleIssueResults, containsString("Users: 1 users, 2 groups"));
            assertThat(userRoleIssueResults, containsString("Issues created: 2 out of 2"));

            // Check that user wilma was created correctly
            administration.usersAndGroups().gotoViewUser("wilma");
            tester.assertTextPresent("Wilma Flinstone");
            tester.assertTextPresent("wilma@example.com");
            tester.assertTextPresent("jira-users");
            tester.assertTextPresent("ice cream");
            tester.assertTextPresent("strawberry");
            tester.assertTextPresent("24");
            tester.assertTextPresent("4 x 6");

            // Check that user fred was created correctly
            administration.usersAndGroups().gotoViewUser(FRED_USERNAME);
            tester.assertTextPresent(FRED_FULLNAME);
            tester.assertTextPresent("fred@example.com");
            tester.assertTextPresent("jira-users");

        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
            this.navigation.logout();
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testIssueDataCommentAndWorklogVisiblity() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportStandardSimpleData.xml", "TestProjectImportStandardSimpleDataNoProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));
            assertThat(projectDetailsResults, containsString("Lead: " + FRED_FULLNAME));
            assertThat(projectDetailsResults, containsString("URL: http://monkeyproject.example.com"));
            assertThat(projectDetailsResults, containsString("Default Assignee: Project Lead"));
            assertThat(projectDetailsResults, containsString("Components: 3"));
            assertThat(projectDetailsResults, containsString("Versions: 3"));

            xPathLocator = new XPathLocator(tester, "//div[@id='customfields']/ul");
            String userRoleIssueResults = xPathLocator.getText();

            assertThat(userRoleIssueResults, containsString("Users: 2 out of 2"));
            assertThat(userRoleIssueResults, containsString("Administrators: 1 users, 1 groups"));
            assertThat(userRoleIssueResults, containsString("Developers: 1 users, 2 groups"));
            assertThat(userRoleIssueResults, containsString("Users: 1 users, 2 groups"));
            assertThat(userRoleIssueResults, containsString("Issues created: 2 out of 2"));

            // Lets login as a user who will not see some comments.  Need to set the password first
            navigateToUser("wilma");
            tester.clickLinkWithText("Set Password");
            tester.setFormElement("password", "wilma");
            tester.setFormElement("confirm", "wilma");
            tester.submit("Update");
            
            this.navigation.logout();
            this.navigation.login("wilma", "wilma");

            // Now lets go verify that the issues data look the way they should
            this.navigation.issue().viewIssue("MKY-1");

            final ViewIssueDetails details = this.parse.issue().parseViewIssuePage();
            assertEquals("MKY-1", details.getKey());
            assertEquals("Bug", details.getIssueType());
            assertEquals("In Progress", details.getStatus());
            assertEquals("Blocker", details.getPriority());
            assertEquals(FRED_FULLNAME, details.getAssignee());
            assertEquals("Wilma Flinstone", details.getReporter());

            // Assert the comments are correct and that we can only see one of them
            // We should already be on Comments.
            List comments = parse.issue().parseComments();
            assertEquals(1, comments.size());
            assertTrue(comments.contains(new Comment("I am a comment added by Wilma, that has been edited.", "Wilma Flinstone added a comment  - 12/Jun/08 3:03 PM - edited")));

            // Assert the worklogs are correct
            tester.clickLinkWithText("Work Log");
            List worklogs = parse.issue().parseWorklogs();
            assertEquals(1, worklogs.size());
            assertTrue(worklogs.contains(new Worklog("I am a worklog added by Mrs. Rubble.", "2 hours", "betty logged work  - 12/Jun/08 3:18 PM")));
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
            this.navigation.logout();
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testCanEditIssue() throws Exception
    {
        boolean isOracle = new EnvironmentUtils(tester, getEnvironmentData(), navigation).isOracle();

        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportStandardSimpleData.xml", "TestProjectImportStandardSimpleDataNoProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));

            if(isOracle)
            {
                //TODO: Remove this sleep hack once http://jira.atlassian.com/browse/JRA-20274 has been resolved
                Thread.sleep(2000);
            }

            // We want to go to the view Issue screen of the new issue.
            this.navigation.issue().viewIssue("MKY-1");
            // Now try to open for edit
            tester.clickLink("edit-issue");
            tester.setFormElement("summary", "This is the test bug with all the field values set, and I have edited it.");
            // This is a hack to get around the testing framework adding new lines to the text areas, hmmmm
            tester.setFormElement("description", "I am a description field");
            tester.setFormElement("environment", "I am environment field");
            tester.setFormElement("customfield_10011", "I am free text field value.");
            tester.setFormElement("customfield_10016", "fred");
            tester.submit("Update");
            // Test that the edit worked, and no data disappears.

            final ViewIssueDetails details = this.parse.issue().parseViewIssuePage();
            assertEquals("MKY-1", details.getKey());
            assertEquals("Bug", details.getIssueType());
            assertEquals("In Progress", details.getStatus());
            assertEquals("Blocker", details.getPriority());
            assertEquals(FRED_FULLNAME, details.getAssignee());
            assertEquals("Wilma Flinstone", details.getReporter());
            assertEquals("1", details.getVotes());
            // Make sure the voter is
            tester.clickLink("view-voters");
            tester.assertTextPresent("voter_link_admin");
            tester.clickLinkWithText("MKY-1");
            assertEquals("1", details.getWatchers());
            // Make sure the watch is wilma
            tester.clickLink("manage-watchers");
            tester.assertLinkPresent("watcher_link_wilma");
            tester.clickLinkWithText("MKY-1");

            assertTrue(details.getAvailableWorkflowActions().contains("Resolve Issue"));
            assertTrue(details.getAvailableWorkflowActions().contains("Close Issue"));
            assertEquals("monkey", details.getProjectName());
            assertEquals("This is the test bug with all the field values set, and I have edited it.", details.getSummary());
            assertEquals("I am a description field", details.getDescription());
            assertEquals("03/Jun/08 3:00 PM", details.getCreatedDate());
            // Updated date will change.
            //            assertEquals("06/Jun/08 2:09 PM", details.getUpdatedDate());
            assertEquals("09/Jun/08", details.getDueDate());
            assertTrue(details.getComponents().contains("First Test Component"));
            assertTrue(details.getComponents().contains("Second Test Component"));
            assertTrue(details.getAffectsVersions().contains("Cool Version"));
            assertTrue(details.getAffectsVersions().contains("Uncool Version"));
            assertTrue(details.getFixVersions().contains("Uncool Version"));
            assertTrue(details.getFixVersions().contains("Medium Cool Version"));
            assertEquals("2d", details.getOriginalEstimate());
            assertEquals("20h", details.getRemainingEstimate());
            assertEquals("1d 4h", details.getTimeSpent());
            assertEquals("I am environment field", details.getEnvironment());
            // Verify links
            tester.assertLinkPresentWithText("MKY-2");
            tester.assertLinkPresentWithText("HSP-1");
            // Verify custom fields
            assertTrue(details.customFieldValueContains("Cascading Select CF", "Parent Option 1"));
            assertTrue(details.customFieldValueContains("Cascading Select CF", "Child Option 1"));
            assertEquals("01/Jun/08 2:57 PM", details.getCustomFields().get("Date Time CF"));
            assertEquals("user-dudes", details.getCustomFields().get("Group Picker CF"));
            assertTrue(details.customFieldValueContains("Multi Checkboxes CF", "Multi Checkbox Option 2"));
            assertTrue(details.customFieldValueContains("Multi Checkboxes CF", "Multi Checkbox Option 1"));
            assertTrue(details.customFieldValueContains("Multi Select CF", "Multi Select Option 1"));
            assertTrue(details.customFieldValueContains("Multi Select CF", "Multi Select Option 2"));
            assertEquals("42.01", details.getCustomFields().get("Number Field CF"));
            assertEquals("Radio Option 1", details.getCustomFields().get("Radio Buttons CF"));
            assertEquals("Select List Option 1", details.getCustomFields().get("Select List CF"));
            assertEquals("I am text field 255 value.", details.getCustomFields().get("Text Field 255"));
            assertEquals(FRED_FULLNAME, details.getCustomFields().get("User Picker CF"));
            assertEquals("01/Apr/08", details.getCustomFields().get("Date Picker CF"));
            assertEquals("I am free text field value.", details.getCustomFields().get("Free Text Field CF"));
            assertTrue(details.customFieldValueContains("Multi Group Picker CF", "admin-dudes"));
            assertTrue(details.customFieldValueContains("Multi Group Picker CF", "dev-dudes"));
            assertEquals("homosapien", details.getCustomFields().get("Project Picker CF"));
            assertEquals(FRED_FULLNAME, details.getCustomFields().get("Multi User Picker CF"));
            assertEquals("Medium Cool Version", details.getCustomFields().get("Single Version Picker CF"));
            assertEquals("http://www.google.com", details.getCustomFields().get("URL Field CF"));
            assertTrue(details.customFieldValueContains("Version Picker CF", "Medium Cool Version"));
            assertTrue(details.customFieldValueContains("Version Picker CF", "Cool Version"));
            assertTrue(details.customFieldValueContains("Version Picker CF", "Uncool Version"));

            // Assert the comments are correct
            // We should already be on Comments.
            List comments = parse.issue().parseComments();
            assertEquals(4, comments.size());
            assertTrue(comments.contains(new Comment("I am a comment added by Wilma, that has been edited.", "Wilma Flinstone added a comment  - 12/Jun/08 3:03 PM - edited")));
            assertTrue(comments.contains(new Comment("I am a comment added by Adminstrator.", "Adminitrator added a comment  - 12/Jun/08 3:11 PM - Restricted to Developers")));
            assertTrue(comments.contains(new Comment("I am another Admin comment", "Adminitrator added a comment  - 12/Jun/08 3:15 PM - Restricted to jira-administrators")));
            assertTrue(comments.contains(new Comment("I am a comment added by betty.", "betty added a comment  - 12/Jun/08 3:16 PM - Restricted to jira-developers - edited")));

            // Assert the worklogs are correct
            tester.clickLinkWithText("Work Log");
            List worklogs = parse.issue().parseWorklogs();
            assertEquals(3, worklogs.size());
            assertTrue(worklogs.contains(new Worklog("I am Fred logging work, edited.", "1 day", FRED_FULLNAME + " logged work  - 12/Jun/08 3:13 PM - Restricted to Developers - edited")));
            assertTrue(worklogs.contains(new Worklog("I am admin worklog.", "2 hours", "Adminitrator logged work  - 12/Jun/08 3:15 PM - Restricted to jira-developers")));
            assertTrue(worklogs.contains(new Worklog("I am a worklog added by Mrs. Rubble.", "2 hours", "betty logged work  - 12/Jun/08 3:18 PM")));

            // Assert the change logs still contain old hisotry (a new value will be added
            tester.clickLinkWithText(CHANGE_HISTORY);
            final ChangeHistoryList list = parse.issue().parseChangeHistory();
            list.assertContainsChangeHistory(getExpectedChangeHistoryListForAllData());
            // Check that the SECOND last one is our marker, can't check this with the contains since its value is the date of the import
            final ChangeHistorySet changeHistorySet = (ChangeHistorySet) list.get(list.size() - 2);
            assertEquals("Adminitrator", changeHistorySet.getChangedBy());
            assertEquals("Project Import", ((ChangeHistoryField) changeHistorySet.getFieldChanges().iterator().next()).getFieldName());
            // Don't bother testing the new change log.
        }
        finally
        {
            if (tempFile != null)
            {
                //noinspection ResultOfMethodCallIgnored
                tempFile.delete();
            }
        }
    }

    public void testCustomFieldIssueTypeConstraints() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportCustomFieldConfig.xml", "TestProjectImportCustomFieldConfig2.xml");

            // Make sure we are ready to import
            text.assertTextPresentHtmlEncoded("The custom field 'Text Field 255' in the backup project is used by issue types 'Bug, Improvement' but the field with the same name in the current JIRA instance is not available to those issue types in this project.");
            tester.assertSubmitButtonNotPresent("Import");

            // Now go and fix the custom field config.
            // Click Link 'Custom Fields' (id='view_custom_fields').
            tester.clickLink("view_custom_fields");
            // Click Link 'Configure' (id='config_customfield_10008').
            tester.clickLink("config_customfield_10008");
            tester.clickLinkWithText("Edit Configuration");
            // Select 'Bug' from select box 'issuetypes'.
            tester.checkCheckbox("issuetypes", "1");
            tester.checkCheckbox("issuetypes", "4");
            tester.submit("Modify");

            // Now go back to the import and re-validate
            tester.gotoPage("/secure/admin/ProjectImportSummary!reMapAndValidate.jspa");
            advanceThroughWaitingPage();

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));

            // We want to go to the view Issue screen of the issue that should not have the custom field value showing.
            this.navigation.issue().viewIssue("MKY-2");
            tester.assertTextNotPresent("I am a hidden value that will only be shown when the configuration changes.");

            // Now enable the custom field for the New Feature issue type and make sure the data was imported
            this.navigation.gotoAdmin();
            // Click Link 'Custom Fields' (id='view_custom_fields').
            tester.clickLink("view_custom_fields");
            // Click Link 'Configure' (id='config_customfield_10008').
            tester.clickLink("config_customfield_10008");
            tester.clickLinkWithText("Edit Configuration");
            // Select 'Bug' from select box 'issuetypes'.
            tester.checkCheckbox("issuetypes", "1");
            tester.checkCheckbox("issuetypes", "4");
            tester.checkCheckbox("issuetypes", "2");
            tester.submit("Modify");

            // now see the value is on the issue
            this.navigation.issue().viewIssue("MKY-2");
            tester.assertTextPresent("I am a hidden value that will only be shown when the configuration changes.");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testCanTransitionIssue() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = doProjectImport("TestProjectImportStandardSimpleData.xml", "TestProjectImportStandardSimpleDataNoProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));

            // We want to go to the view Issue screen of the new issue.
            this.navigation.issue().viewIssue("MKY-1");
            tester.assertTextPresent("This is the test bug with all the field values set.");
            // Click Resolve.
            tester.clickLinkWithText("Resolve Issue");
            tester.assertTextPresent("Resolve Issue");
            tester.assertTextPresent("Resolving an issue indicates that the developers are satisfied the issue is finished.");
            tester.submit("Transition");
            // Check that the issue changed as expected.
            final ViewIssueDetails viewIssueDetails = this.parse.issue().parseViewIssuePage();
            assertEquals("Resolved", viewIssueDetails.getStatus());
            assertEquals("Fixed", viewIssueDetails.getResolution());
            assertTrue(viewIssueDetails.getAvailableWorkflowActions().contains("Close Issue"));
            assertTrue(viewIssueDetails.getAvailableWorkflowActions().contains("Reopen Issue"));
            assertEquals(2, viewIssueDetails.getAvailableWorkflowActions().size());
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testIssueDataSimpleAllData() throws Exception
    {
        // we want to test with both "old" and "new" attachment naming schemes to make sure the import
        // handles both
        _testSimpleImport("10000_bsattach.txt");
        _testSimpleImport("10000");
    }

    private void _testSimpleImport(final String attachmentFilename) throws Exception
    {
        File tempFile = null;

        // Fake up the attachments directory
        File attachmentsBackupDir = null;
        File projectDir = null;
        File issueDir = null;
        File attachmentFile = null;
        try
        {
            // Create a unique temp folder to use as our "attachments backup directory"
            attachmentsBackupDir = new File(new File(administration.getJiraHomeDirectory(), "import"), "attachments");
            attachmentsBackupDir.mkdir();
            projectDir = new File(attachmentsBackupDir, "MKY");
            projectDir.mkdir();
            issueDir = new File(projectDir, "MKY-1");
            issueDir.mkdir();
            attachmentFile = new File(issueDir, attachmentFilename);
            attachmentFile.createNewFile();

            tempFile = doProjectImport("TestProjectImportStandardSimpleData.xml", "TestProjectImportStandardSimpleDataNoProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();


            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));
            assertThat(projectDetailsResults, containsString("Lead: " + FRED_FULLNAME));
            assertThat(projectDetailsResults, containsString("URL: http://monkeyproject.example.com"));
            assertThat(projectDetailsResults, containsString("Default Assignee: Project Lead"));
            assertThat(projectDetailsResults, containsString("Components: 3"));
            assertThat(projectDetailsResults, containsString("Versions: 3"));

            xPathLocator = new XPathLocator(tester, "//div[@id='customfields']/ul");
            String userRoleIssueResults = xPathLocator.getText();

            assertThat(userRoleIssueResults, containsString("Users: 2 out of 2"));
            assertThat(userRoleIssueResults, containsString("Administrators: 1 users, 1 groups"));
            assertThat(userRoleIssueResults, containsString("Developers: 1 users, 2 groups"));
            assertThat(userRoleIssueResults, containsString("Users: 1 users, 2 groups"));
            assertThat(userRoleIssueResults, containsString("Issues created: 2 out of 2"));
            assertThat(userRoleIssueResults, containsString("Attachments: 1 out of 1"));

            // Make sure we can see the issues in the navigator
            this.navigation.issueNavigator().displayAllIssues();
            tester.assertTextPresent("MKY-1");
            tester.assertTextPresent("MKY-2");

            // Now lets go verify that the issues data look the way they should
            this.navigation.issue().viewIssue("MKY-1");

            final ViewIssueDetails details = this.parse.issue().parseViewIssuePage();
            assertEquals("MKY-1", details.getKey());
            assertEquals("Bug", details.getIssueType());
            assertEquals("In Progress", details.getStatus());
            assertEquals("Blocker", details.getPriority());
            assertEquals(FRED_FULLNAME, details.getAssignee());
            assertEquals("Wilma Flinstone", details.getReporter());
            assertEquals("1", details.getVotes());
            // Make sure the voter is
            tester.clickLink("view-voters");
            tester.assertTextPresent("voter_link_admin");
            tester.clickLinkWithText("MKY-1");
            assertEquals("1", details.getWatchers());
            // Make sure the watch is wilma
            tester.clickLink("manage-watchers");
            tester.assertLinkPresent("watcher_link_wilma");
            tester.clickLinkWithText("MKY-1");

            assertTrue(details.getAvailableWorkflowActions().contains("Resolve Issue"));
            assertTrue(details.getAvailableWorkflowActions().contains("Close Issue"));
            assertEquals("monkey", details.getProjectName());
            assertEquals("This is the test bug with all the field values set.", details.getSummary());
            assertEquals("I am a description field", details.getDescription());
            assertEquals("03/Jun/08 3:00 PM", details.getCreatedDate());
            assertEquals("06/Jun/08 2:09 PM", details.getUpdatedDate());
            assertEquals("09/Jun/08", details.getDueDate());
            assertEquals(null, details.getResolutionDate());
            assertTrue(details.getComponents().contains("First Test Component"));
            assertTrue(details.getComponents().contains("Second Test Component"));
            assertTrue(details.getAffectsVersions().contains("Cool Version"));
            assertTrue(details.getAffectsVersions().contains("Uncool Version"));
            assertTrue(details.getFixVersions().contains("Uncool Version"));
            assertTrue(details.getFixVersions().contains("Medium Cool Version"));
            assertEquals(3, details.getLabels().size());
            assertTrue(details.getLabels().contains("duderino"));
            assertTrue(details.getLabels().contains("fancy"));
            assertTrue(details.getLabels().contains("labelset"));
            assertEquals("2d", details.getOriginalEstimate());
            assertEquals("20h", details.getRemainingEstimate());
            assertEquals("1d 4h", details.getTimeSpent());
            assertTrue(details.getAttachments().contains("bsattach.txt"));
            assertEquals("I am environment field", details.getEnvironment());
            // Verify links
            tester.assertLinkPresentWithText("MKY-2");
            tester.assertLinkPresentWithText("HSP-1");
            tester.assertLinkPresentWithText("HSP-2");
            // Verify custom fields
            assertTrue(details.customFieldValueContains("Cascading Select CF", "Parent Option 1"));
            assertTrue(details.customFieldValueContains("Cascading Select CF", "Child Option 1"));
            assertEquals("01/Jun/08 2:57 PM", details.getCustomFields().get("Date Time CF"));
            assertEquals("user-dudes", details.getCustomFields().get("Group Picker CF"));
            assertTrue(details.customFieldValueContains("Labels CF", "duck duffy mickey mouse"));
            assertTrue(details.customFieldValueContains("Multi Checkboxes CF", "Multi Checkbox Option 2"));
            assertTrue(details.customFieldValueContains("Multi Checkboxes CF", "Multi Checkbox Option 1"));
            assertTrue(details.customFieldValueContains("Multi Select CF", "Multi Select Option 1"));
            assertTrue(details.customFieldValueContains("Multi Select CF", "Multi Select Option 2"));
            assertEquals("42.01", details.getCustomFields().get("Number Field CF"));
            assertEquals("Radio Option 1", details.getCustomFields().get("Radio Buttons CF"));
            assertEquals("Select List Option 1", details.getCustomFields().get("Select List CF"));
            assertEquals("I am text field 255 value.", details.getCustomFields().get("Text Field 255"));
            assertEquals(FRED_FULLNAME, details.getCustomFields().get("User Picker CF"));
            assertEquals("01/Apr/08", details.getCustomFields().get("Date Picker CF"));
            assertEquals("I am free text field value.", details.getCustomFields().get("Free Text Field CF"));
            assertTrue(details.customFieldValueContains("Multi Group Picker CF", "admin-dudes"));
            assertTrue(details.customFieldValueContains("Multi Group Picker CF", "dev-dudes"));
            assertEquals("homosapien", details.getCustomFields().get("Project Picker CF"));
            assertEquals("betty, " + FRED_FULLNAME, details.getCustomFields().get("Multi User Picker CF"));
            assertEquals("Medium Cool Version", details.getCustomFields().get("Single Version Picker CF"));
            assertEquals("http://www.google.com", details.getCustomFields().get("URL Field CF"));
            assertTrue(details.customFieldValueContains("Version Picker CF", "Medium Cool Version"));
            assertTrue(details.customFieldValueContains("Version Picker CF", "Cool Version"));
            assertTrue(details.customFieldValueContains("Version Picker CF", "Uncool Version"));

            // Assert the comments are correct
            // We should already be on Comments.
            List comments = parse.issue().parseComments();
            assertEquals(4, comments.size());
            assertTrue(comments.contains(new Comment("I am a comment added by Wilma, that has been edited.", "Wilma Flinstone added a comment  - 12/Jun/08 3:03 PM - edited")));
            assertTrue(comments.contains(new Comment("I am a comment added by Adminstrator.", "Adminitrator added a comment  - 12/Jun/08 3:11 PM - Restricted to Developers")));
            assertTrue(comments.contains(new Comment("I am another Admin comment", "Adminitrator added a comment  - 12/Jun/08 3:15 PM - Restricted to jira-administrators")));
            assertTrue(comments.contains(new Comment("I am a comment added by betty.", "betty added a comment  - 12/Jun/08 3:16 PM - Restricted to jira-developers - edited")));

            // Assert the worklogs are correct
            tester.clickLinkWithText("Work Log");
            List worklogs = parse.issue().parseWorklogs();
            assertEquals(3, worklogs.size());
            assertTrue(worklogs.contains(new Worklog("I am Fred logging work, edited.", "1 day", FRED_FULLNAME + " logged work  - 12/Jun/08 3:13 PM - Restricted to Developers - edited")));
            assertTrue(worklogs.contains(new Worklog("I am admin worklog.", "2 hours", "Adminitrator logged work  - 12/Jun/08 3:15 PM - Restricted to jira-developers")));
            assertTrue(worklogs.contains(new Worklog("I am a worklog added by Mrs. Rubble.", "2 hours", "betty logged work  - 12/Jun/08 3:18 PM")));

            // Assert the change logs are correct
            tester.clickLinkWithText(CHANGE_HISTORY);
            final ChangeHistoryList list = parse.issue().parseChangeHistory();
            list.assertContainsChangeHistory(getExpectedChangeHistoryListForAllData());
            // Check that the last one is our marker, can't check this with the contains since its value is the date of the import
            final ChangeHistorySet changeHistorySet = (ChangeHistorySet) list.get(list.size() - 1);
            assertEquals("Adminitrator", changeHistorySet.getChangedBy());
            assertEquals("Project Import", ((ChangeHistoryField) changeHistorySet.getFieldChanges().iterator().next()).getFieldName());

            // Go to the link that is outside the project and make sure its link is right on the issue and that we correctly
            // set the change history item on the external issue.
            navigation.issue().gotoIssue("HSP-1");
            tester.assertTextPresent("I am a homosap task");
            tester.assertLinkPresentWithText("MKY-1");
            final ChangeHistoryList hsp1ChangeHistoryList = parse.issue().parseChangeHistory();
            hsp1ChangeHistoryList.assertContainsChangeHistory(getExpectedHSP1ChangeHistory());
            // Now check the issue that never had a link to MKY-1 in its original project but does now after the import
            this.navigation.issue().viewIssue("HSP-2");
            final ChangeHistoryList hsp2ChangeHistoryList = parse.issue().parseChangeHistory();
            hsp2ChangeHistoryList.assertContainsChangeHistory(getExpectedHSP2ChangeHistory());
        }
        finally
        {
            if (attachmentFile != null)
            {
                attachmentFile.delete();
            }
            if (tempFile != null)
            {
                tempFile.delete();
            }
            if (issueDir != null)
            {
                issueDir.delete();
            }
            if (projectDir != null)
            {
                projectDir.delete();
            }
        }
    }

    public void testIssueDataSimpleAllDataResolutionDate() throws Exception
    {
        doProjectImport("TestProjectImportStandardSimpleDataResolutionDate.xml", "TestProjectImportStandardSimpleDataNoProject.xml");

        // Make sure we are ready to import
        tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
        tester.assertSubmitButtonPresent("Import");

        tester.submit("Import");
        advanceThroughWaitingPage();


        // We should end up on the results page
        XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
        String projectDetailsResults = xPathLocator.getText();
        assertThat(projectDetailsResults, containsString("Key: MKY"));
        assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));
        assertThat(projectDetailsResults, containsString("Lead: " + FRED_FULLNAME));
        assertThat(projectDetailsResults, containsString("URL: http://monkeyproject.example.com"));
        assertThat(projectDetailsResults, containsString("Default Assignee: Project Lead"));
        assertThat(projectDetailsResults, containsString("Components: 3"));
        assertThat(projectDetailsResults, containsString("Versions: 3"));

        xPathLocator = new XPathLocator(tester, "//div[@id='customfields']/ul");
        String userRoleIssueResults = xPathLocator.getText();

        assertThat(userRoleIssueResults, containsString("Users: 2 out of 2"));
        assertThat(userRoleIssueResults, containsString("Administrators: 1 users, 1 groups"));
        assertThat(userRoleIssueResults, containsString("Developers: 1 users, 2 groups"));
        assertThat(userRoleIssueResults, containsString("Users: 1 users, 2 groups"));
        assertThat(userRoleIssueResults, containsString("Issues created: 3 out of 3"));

        // Make sure we can see the issues in the navigator
        this.navigation.issueNavigator().displayAllIssues();
        tester.assertTextPresent("MKY-1");
        tester.assertTextPresent("MKY-2");
        tester.assertTextPresent("MKY-3");

        // Now lets go verify that the issues data look the way they should
        this.navigation.issue().viewIssue("MKY-1");

        final ViewIssueDetails details = this.parse.issue().parseViewIssuePage();

        assertEquals("monkey", details.getProjectName());
        assertEquals("This is the test bug with all the field values set.", details.getSummary());
        assertEquals("I am a description field", details.getDescription());
        assertEquals("03/Jun/08 3:00 PM", details.getCreatedDate());
        assertEquals("06/Jun/08 2:09 PM", details.getUpdatedDate());
        assertEquals("09/Jun/08", details.getDueDate());
        assertEquals(null, details.getResolutionDate());

        //now let's check the resolved issue shows the resolutiond date
        this.navigation.issue().viewIssue("MKY-3");

        final ViewIssueDetails resolvedDetails = this.parse.issue().parseViewIssuePage();
        assertEquals("This is the test bug with all the field values set and with resolution", resolvedDetails.getSummary());
        assertEquals("01/May/08 2:04 PM", resolvedDetails.getCreatedDate());
        assertEquals("01/Dec/08 2:05 PM", resolvedDetails.getUpdatedDate());
        assertEquals("09/Jun/08", resolvedDetails.getDueDate());
        assertEquals("19/Nov/08 2:04 PM", resolvedDetails.getResolutionDate());
    }

    public void testNonExistantCustomFieldValueIsIgnoredAndImportIsSuccessful() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportStandardNonExistantCustomFieldData.xml", "TestProjectImportStandardSimpleDataNoProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            // Assert the warning is present.
            text.assertTextPresentHtmlEncoded("The custom field 'Resolved date CF' will not be imported because the custom field type 'com.atlassian.jira.toolkit:resolveddate' is not installed.");

            tester.submit("Import");
            advanceThroughWaitingPage();


            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));
            assertThat(projectDetailsResults, containsString("Lead: " + FRED_FULLNAME));
            assertThat(projectDetailsResults, containsString("URL: http://monkeyproject.example.com"));
            assertThat(projectDetailsResults, containsString("Default Assignee: Project Lead"));
            assertThat(projectDetailsResults, containsString("Components: 3"));
            assertThat(projectDetailsResults, containsString("Versions: 3"));

            xPathLocator = new XPathLocator(tester, "//div[@id='customfields']/ul");
            String userRoleIssueResults = xPathLocator.getText();

            assertThat(userRoleIssueResults, containsString("Users: 2 out of 2"));
            assertThat(userRoleIssueResults, containsString("Administrators: 1 users, 1 groups"));
            assertThat(userRoleIssueResults, containsString("Developers: 1 users, 2 groups"));
            assertThat(userRoleIssueResults, containsString("Users: 1 users, 2 groups"));
            assertThat(userRoleIssueResults, containsString("Issues created: 2 out of 2"));

            // Make sure we can see the issues in the navigator
            this.navigation.issueNavigator().displayAllIssues();
            tester.assertTextPresent("MKY-1");
            tester.assertTextPresent("MKY-2");

            // Now lets go verify that the issues data look the way they should
            this.navigation.issue().viewIssue("MKY-1");

            final ViewIssueDetails details = this.parse.issue().parseViewIssuePage();
            assertEquals("MKY-1", details.getKey());
            assertEquals("Bug", details.getIssueType());
            assertEquals("Resolved", details.getStatus());
            assertEquals("Blocker", details.getPriority());
            assertEquals(FRED_FULLNAME, details.getAssignee());
            assertEquals("Wilma Flinstone", details.getReporter());
            assertEquals("1", details.getVotes());
            // Make sure the voter is
            tester.clickLink("view-voters");
            tester.assertTextPresent("voter_link_admin");
            tester.clickLinkWithText("MKY-1");
            assertEquals("1", details.getWatchers());
            // Make sure the watch is wilma
            tester.clickLink("manage-watchers");
            tester.assertLinkPresent("watcher_link_wilma");
            tester.clickLinkWithText("MKY-1");

            assertTrue(details.getAvailableWorkflowActions().contains("Reopen Issue"));
            assertTrue(details.getAvailableWorkflowActions().contains("Close Issue"));
            assertEquals("monkey", details.getProjectName());
            assertEquals("This is the test bug with all the field values set.", details.getSummary());
            assertEquals("I am a description field", details.getDescription());
            assertEquals("03/Jun/08 3:00 PM", details.getCreatedDate());
            assertEquals("06/Jun/08 2:09 PM", details.getUpdatedDate());
            assertEquals("09/Jun/08", details.getDueDate());
            assertTrue(details.getComponents().contains("First Test Component"));
            assertTrue(details.getComponents().contains("Second Test Component"));
            assertTrue(details.getAffectsVersions().contains("Cool Version"));
            assertTrue(details.getAffectsVersions().contains("Uncool Version"));
            assertTrue(details.getFixVersions().contains("Uncool Version"));
            assertTrue(details.getFixVersions().contains("Medium Cool Version"));
            assertEquals("2d", details.getOriginalEstimate());
            assertEquals("20h", details.getRemainingEstimate());
            assertEquals("1d 4h", details.getTimeSpent());
            assertEquals("I am environment field", details.getEnvironment());
            // Verify links
            tester.assertLinkPresentWithText("MKY-2");
            tester.assertLinkPresentWithText("HSP-1");
            // Verify custom fields
            assertTrue(details.customFieldValueContains("Cascading Select CF", "Parent Option 1"));
            assertTrue(details.customFieldValueContains("Cascading Select CF", "Child Option 1"));
            assertEquals("01/Jun/08 2:57 PM", details.getCustomFields().get("Date Time CF"));
            assertEquals("user-dudes", details.getCustomFields().get("Group Picker CF"));
            assertTrue(details.customFieldValueContains("Multi Checkboxes CF", "Multi Checkbox Option 2"));
            assertTrue(details.customFieldValueContains("Multi Checkboxes CF", "Multi Checkbox Option 1"));
            assertTrue(details.customFieldValueContains("Multi Select CF", "Multi Select Option 1"));
            assertTrue(details.customFieldValueContains("Multi Select CF", "Multi Select Option 2"));
            assertEquals("42.01", details.getCustomFields().get("Number Field CF"));
            assertEquals("Radio Option 1", details.getCustomFields().get("Radio Buttons CF"));
            assertEquals("Select List Option 1", details.getCustomFields().get("Select List CF"));
            assertEquals("I am text field 255 value.", details.getCustomFields().get("Text Field 255"));
            assertEquals(FRED_FULLNAME, details.getCustomFields().get("User Picker CF"));
            assertEquals("01/Apr/08", details.getCustomFields().get("Date Picker CF"));
            assertEquals("I am free text field value.", details.getCustomFields().get("Free Text Field CF"));
            assertTrue(details.customFieldValueContains("Multi Group Picker CF", "admin-dudes"));
            assertTrue(details.customFieldValueContains("Multi Group Picker CF", "dev-dudes"));
            assertEquals("homosapien", details.getCustomFields().get("Project Picker CF"));
            assertEquals("betty, " + FRED_FULLNAME, details.getCustomFields().get("Multi User Picker CF"));
            assertEquals("Medium Cool Version", details.getCustomFields().get("Single Version Picker CF"));
            assertEquals("http://www.google.com", details.getCustomFields().get("URL Field CF"));
            assertTrue(details.customFieldValueContains("Version Picker CF", "Medium Cool Version"));
            assertTrue(details.customFieldValueContains("Version Picker CF", "Cool Version"));
            assertTrue(details.customFieldValueContains("Version Picker CF", "Uncool Version"));
            // Make sure the non-standard custom field value is not there
            assertNull(details.getCustomFields().get("Resolved date CF"));
            //really make sure that the custom field isn't shown.  Can't check that the date doesn't appear, since
            // the issue header displays the resolution date now.  If the custom field got imported with any value however
            // it will be listed with the other custom fields, so asserting that the CF name doesn't appear should
            // be good enough.
            tester.assertTextNotPresent("Resolved date CF");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testIssueDataSimpleMinimalData() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportStandardSimpleData.xml", "TestProjectImportStandardSimpleDataNoProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();


            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));
            assertThat(projectDetailsResults, containsString("Lead: " + FRED_FULLNAME));
            assertThat(projectDetailsResults, containsString("URL: http://monkeyproject.example.com"));
            assertThat(projectDetailsResults, containsString("Default Assignee: Project Lead"));
            assertThat(projectDetailsResults, containsString("Components: 3"));
            assertThat(projectDetailsResults, containsString("Versions: 3"));

            xPathLocator = new XPathLocator(tester, "//div[@id='customfields']/ul");
            String userRoleIssueResults = xPathLocator.getText();

            assertThat(userRoleIssueResults, containsString("Users: 2 out of 2"));
            assertThat(userRoleIssueResults, containsString("Administrators: 1 users, 1 groups"));
            assertThat(userRoleIssueResults, containsString("Developers: 1 users, 2 groups"));
            assertThat(userRoleIssueResults, containsString("Users: 1 users, 2 groups"));
            assertThat(userRoleIssueResults, containsString("Issues created: 2 out of 2"));

            // Now lets go verify that the issues data look the way they should
            this.navigation.issue().viewIssue("MKY-2");

            final ViewIssueDetails details = this.parse.issue().parseViewIssuePage();
            assertEquals("MKY-2", details.getKey());
            assertEquals("New Feature", details.getIssueType());
            assertEquals("Open", details.getStatus());
            assertEquals("Critical", details.getPriority());
            assertEquals(FRED_FULLNAME, details.getAssignee());
            assertEquals("Adminitrator", details.getReporter());
            assertEquals("0", details.getVotes());
            assertEquals("0", details.getWatchers());
            assertTrue(details.getAvailableWorkflowActions().contains("Resolve Issue"));
            assertTrue(details.getAvailableWorkflowActions().contains("Close Issue"));
            assertEquals("monkey", details.getProjectName());
            assertEquals("I am a test issue with almost no field values set", details.getSummary());
            assertEquals("01/Jun/08 3:20 PM", details.getCreatedDate());
            assertEquals("02/Jun/08 3:53 PM", details.getUpdatedDate());
            assertEquals(null, details.getDueDate());
            assertEquals("Click to add description", details.getDescription());
            assertTrue(details.getComponents().contains("None"));
            assertTrue(details.getAffectsVersions().contains("None"));
            assertTrue(details.getFixVersions().contains("None"));
            assertEquals(null, details.getOriginalEstimate());
            assertEquals(null, details.getRemainingEstimate());
            assertEquals(null, details.getTimeSpent());
            assertEquals(0, details.getAttachments().size());
            assertEquals(null, details.getEnvironment());
            // Verify links
            tester.assertLinkPresentWithText("MKY-1");
            // Assert Labels CF not present
            assertEquals(1, details.getCustomFields().size());
            assertEquals("betty", details.getCustomFields().get("User Picker CF"));

            // Assert the comments are correct
            // We should already be on Comments.
            List comments = parse.issue().parseComments();
            assertEquals(0, comments.size());

            // Assert the worklogs are correct
            tester.clickLinkWithText("Work Log");
            List worklogs = parse.issue().parseWorklogs();
            assertEquals(0, worklogs.size());

            // Assert the change logs are correct
            tester.clickLinkWithText(CHANGE_HISTORY);
            final ChangeHistoryList list = parse.issue().parseChangeHistory();
            list.assertContainsChangeHistory(getExpectedChangeHistoryListForMinimal());
            // Check that the last one is our marker, can't check this with the contains since its value is the date of the import
            final ChangeHistorySet changeHistorySet = (ChangeHistorySet) list.get(list.size() - 1);
            assertEquals("Adminitrator", changeHistorySet.getChangedBy());
            assertEquals("Project Import", ((ChangeHistoryField) changeHistorySet.getFieldChanges().iterator().next()).getFieldName());
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    private ChangeHistoryList getExpectedChangeHistoryListForMinimal()
    {
        ChangeHistoryList expectedList = new ChangeHistoryList();
        expectedList.addChangeSet("Adminitrator")
                // This first one is a hack because of the impl of the change history parser
                .add("Field", "Original Value", "New Value")
                .add("Link", null, "This issue duplicates MKY-1 [ MKY-1 ]");
        expectedList.addChangeSet("Adminitrator")
                .add("User Picker CF", null, "betty");

        return expectedList;
    }

    private ChangeHistoryList getExpectedHSP1ChangeHistory()
    {
        ChangeHistoryList expectedList = new ChangeHistoryList();
        expectedList.addChangeSet(FRED_FULLNAME)
                // This first one is a hack because of the impl of the change history parser
                .add("Field", "Original Value", "New Value")
                .add("Link", null, "This issue duplicates MKY-1 [ MKY-1 ]");
        expectedList.addChangeSet("Adminitrator")
                .add("Link", "This issue duplicates MKY-1 [ MKY-1 ]", null);
        expectedList.addChangeSet("Adminitrator")
                .add("Reporter", FRED_FULLNAME + " [ fred ]", "Adminitrator [ admin ]");

        // This is the one that the import has added.
        expectedList.addChangeSet("Adminitrator")
                .add("Link", null, "This issue duplicates MKY-1 [ MKY-1 ]");
        return expectedList;
    }

    private ChangeHistoryList getExpectedHSP2ChangeHistory()
    {
        ChangeHistoryList expectedList = new ChangeHistoryList();
        // This is the one that the import has added.
        expectedList.addChangeSet("Adminitrator")
                // This first one is a hack because of the impl of the change history parser
                .add("Field", "Original Value", "New Value")
                .add("Link", null, "This issue is duplicated by MKY-1 [ MKY-1 ]");
        return expectedList;
    }

    private ChangeHistoryList getExpectedChangeHistoryListForAllData()
    {
        ChangeHistoryList expectedList = new ChangeHistoryList();
        expectedList.addChangeSet("Adminitrator")
                // This first one is a hack because of the impl of the change history parser
                .add("Field", "Original Value", "New Value")
                .add("Multi Checkboxes CF", "[Multi Checkbox Option 2, Multi Checkbox Option 1]", "[Multi Checkbox Option 1, Multi Checkbox Option 2]")
                .add("Version Picker CF", "Medium Cool Version, Uncool Version, Cool Version [ 10002, 10001, 10000 ]", "Cool Version, Uncool Version, Medium Cool Version [ 10000, 10001, 10002 ]")
                .add("Multi User Picker CF", "[admin, fred]", "[betty, fred]")
                .add("Multi Select CF ", "[Multi Select Option 2, Multi Select Option 1]", "[Multi Select Option 1, Multi Select Option 2]");
        expectedList.addChangeSet(FRED_FULLNAME)
                .add("Remaining Estimate", null, "2 days [ 172800 ]")
                .add("Original Estimate", null, "2 days [ 172800 ]")
                .add("Version Picker CF", "Medium Cool Version, Uncool Version, Cool Version [ 10002, 10001, 10000 ]", "Cool Version, Uncool Version, Medium Cool Version [ 10000, 10001, 10002 ]")
                .add("Multi Checkboxes CF", "[Multi Checkbox Option 2, Multi Checkbox Option 1]", "[Multi Checkbox Option 1, Multi Checkbox Option 2]")
                .add("Multi Select CF", "[Multi Select Option 2, Multi Select Option 1]", "[Multi Select Option 1, Multi Select Option 2]");
        expectedList.addChangeSet(FRED_FULLNAME)
                .add("Time Spent", null, "1 day [ 86400 ]")
                .add("Remaining Estimate", "2 days [ 172800 ]", "1 day [ 86400 ]");
        expectedList.addChangeSet("Adminitrator")
                .add("Time Spent", "1 day [ 86400 ]", "1 day, 2 hours [ 93600 ]")
                .add("Remaining Estimate", "1 day [ 86400 ]", "22 hours [ 79200 ]");
        expectedList.addChangeSet("betty")
                .add("Remaining Estimate", "22 hours [ 79200 ]", "20 hours [ 72000 ]")
                .add("Time Spent", "1 day, 2 hours [ 93600 ]", "1 day, 4 hours [ 100800 ]");
        expectedList.addChangeSet("Adminitrator")
                .add("Link", null, "This issue is duplicated by MKY-2 [ MKY-2 ]");
        expectedList.addChangeSet(FRED_FULLNAME)
                .add("Status", "Open [ 1 ]", "In Progress [ 3 ]");
        expectedList.addChangeSet(FRED_FULLNAME)
                .add("Version Picker CF", "Medium Cool Version, Uncool Version, Cool Version [ 10002, 10001, 10000 ]", "Cool Version, Uncool Version, Medium Cool Version [ 10000, 10001, 10002 ]")
                .add("Multi Checkboxes CF", "[Multi Checkbox Option 2, Multi Checkbox Option 1]", "[Multi Checkbox Option 1, Multi Checkbox Option 2]")
                .add("Due Date", "2008-07-02 00:00:00.0")
                .add("Multi Select CF", "[Multi Select Option 2, Multi Select Option 1]", "[Multi Select Option 1, Multi Select Option 2]");
        expectedList.addChangeSet(FRED_FULLNAME)
                .add("Link", null, "This issue is duplicated by HSP-1 [ HSP-1 ]");
        expectedList.addChangeSet("Adminitrator")
                .add("Multi Checkboxes CF", "[Multi Checkbox Option 2, Multi Checkbox Option 1]", "[Multi Checkbox Option 1, Multi Checkbox Option 2]")
                .add("Fix Version/s", null, "Uncool Version [ 10001 ]")
                .add("Version Picker CF", "Medium Cool Version, Cool Version, Uncool Version [ 10002, 10000, 10001 ]", "Cool Version, Uncool Version, Medium Cool Version [ 10000, 10001, 10002 ]")
                .add("Fix Version/s", null, "Medium Cool Version [ 10002 ]");
        expectedList.addChangeSet("Adminitrator")
                .add("Link", null, "This issue duplicates HSP-2 [ HSP-2 ]");

        return expectedList;
    }

    public void testCreateProject() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportStandardSimpleData.xml", "TestProjectImportStandardSimpleDataNoProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));

            // We want to test the link on the project Key works fine. It should take us to view project for Monkey
            tester.clickLinkWithText("MKY");
            assertions.assertNodeByIdHasText("project-config-header-name", "monkey");
            // Assert that the Project was created with all details
            ProjectClient pc = new ProjectClient(environmentData);

            final Project project = pc.get("MKY");
            assertEquals("monkey", project.name);
            assertEquals("This is a description for a monkey project.", project.description);
            assertEquals("http://monkeyproject.example.com", project.url);
            assertEquals(FRED_FULLNAME, project.lead.displayName);

            assertMonkeyProjectParts(false);

        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    private void assertMonkeyProjectParts(boolean testComponentLead)
    {
        ProjectClient pc = new ProjectClient(environmentData);

        final List<Component> components = pc.getComponents("MKY");

        assertEquals(3, components.size());
        assertEquals("First Test Component", components.get(0).name);
        if (testComponentLead)
        {
            assertEquals("wilma", components.get(0).lead.name);
        }
        assertEquals("Second Test Component", components.get(1).name);
        assertEquals("Third Test Component", components.get(2).name);


        final List<Version> versions = pc.getVersions("MKY");
        assertEquals(3, versions.size());
        assertEquals("Cool Version", versions.get(0).name);
        assertEquals("Uncool Version", versions.get(1).name);
        assertEquals("Medium Cool Version", versions.get(2).name);

        // Check the role memberships
        // Administrators
        ProjectRoleClient prc = new ProjectRoleClient(environmentData);
        final Map mky = prc.get("MKY");

        assertEquals(3, mky.size());

        // Admins
        ProjectRole projectRole = prc.get("MKY", "Administrators");
        assertEquals(2, projectRole.actors.size());
        assertEquals("admin-dudes", projectRole.actors.get(0).name);
        assertEquals("atlassian-group-role-actor", projectRole.actors.get(0).type);
        assertEquals("admin", projectRole.actors.get(1).name);
        assertEquals("atlassian-user-role-actor", projectRole.actors.get(1).type);

        // Developers
        projectRole = prc.get("MKY", "Developers");
        assertEquals(3, projectRole.actors.size());
        assertEquals("dev-dudes", projectRole.actors.get(0).name);
        assertEquals("atlassian-group-role-actor", projectRole.actors.get(0).type);
        assertEquals("fred", projectRole.actors.get(1).name);
        assertEquals("atlassian-user-role-actor", projectRole.actors.get(1).type);
        assertEquals("jira-developers", projectRole.actors.get(2).name);
        assertEquals("atlassian-group-role-actor", projectRole.actors.get(2).type);

        // Users
        projectRole = prc.get("MKY", "Users");
        assertEquals(3, projectRole.actors.size());
        assertEquals("jira-users", projectRole.actors.get(0).name);
        assertEquals("atlassian-group-role-actor", projectRole.actors.get(0).type);
        assertEquals("user-dudes", projectRole.actors.get(1).name);
        assertEquals("atlassian-group-role-actor", projectRole.actors.get(1).type);
        assertEquals("wilma", projectRole.actors.get(2).name);
        assertEquals("atlassian-user-role-actor", projectRole.actors.get(2).type);
    }

    public void testUpdateProjectOverwriteDetails() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImportUpdateProject("TestProjectImportStandardSimpleData.xml", "TestProjectImportStandardSimpleDataNoProject.xml", true);

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));

            // Assert that the Project was created with all details
            // We test the link in testImportAttachmentsFromOldPlace(). lets test the OK button also takes us to the View Project for Monkey
            tester.submit("OK");
            assertions.assertNodeByIdHasText("project-config-header-name", "monkey");

            // Assert Project propeties
            ProjectClient pc = new ProjectClient(environmentData);

            final Project project = pc.get("MKY");
            assertEquals("monkey", project.name);
            assertEquals("This is a description for a monkey project.", project.description);
            assertEquals("http://monkeyproject.example.com", project.url);
            assertEquals(FRED_FULLNAME, project.lead.displayName);


            assertMonkeyProjectParts(false);

        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testUpdateProjectOverwriteDetailsWithSenderAddressAndComponentLeads() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportEnterpriseSimpleData.xml", "TestProjectImportEnterpriseSimpleDataEmptyProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));
            assertThat(projectDetailsResults, containsString("Sender Address: jira-monkey-test@example.com"));

            // Assert that the Project was created with all details
            ProjectClient pc = new ProjectClient(environmentData);
            final Project mky = pc.get("MKY");
            assertEquals("MKY", mky.key);
            assertEquals("monkey", mky.name);
            assertEquals("This is a description for a monkey project.", mky.description);
            assertEquals("fred", mky.lead.name);
            assertThat(mky.email, equalTo("jira-monkey-test@example.com"));

            // Check that the component lead was included
            assertMonkeyProjectParts(true);

        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testUpdateProjectDoNotOverwriteDetails() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImportUpdateProject("TestProjectImportStandardSimpleData.xml", "TestProjectImportStandardSimpleDataNoProject.xml", false);

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: existing monk thing"));

            // Assert that the Project was created with all details
            ProjectClient pc = new ProjectClient(environmentData);

            final Project project = pc.get("MKY");
            assertEquals("King Monkey", project.name);
            assertEquals("existing monk thing", project.description);
            assertEquals("http://www.kingkong.net", project.url);
            assertEquals("John Smith", project.lead.displayName);


            final List<Component> components = pc.getComponents("MKY");

            assertEquals(3, components.size());
            assertEquals("First Test Component", components.get(0).name);
            assertEquals("Second Test Component", components.get(1).name);
            assertEquals("Third Test Component", components.get(2).name);


            final List<Version> versions = pc.getVersions("MKY");
            assertEquals(3, versions.size());
            assertEquals("Cool Version", versions.get(0).name);
            assertEquals("Uncool Version", versions.get(1).name);
            assertEquals("Medium Cool Version", versions.get(2).name);

            // Administrators
            ProjectRoleClient prc = new ProjectRoleClient(environmentData);
            final Map mky = prc.get("MKY");

            assertEquals(3, mky.size());

            // Admins
            ProjectRole projectRole = prc.get("MKY", "Administrators");
            assertEquals(1, projectRole.actors.size());
            assertEquals("jira-administrators", projectRole.actors.get(0).name);
            assertEquals("atlassian-group-role-actor", projectRole.actors.get(0).type);

            // Developers
            projectRole = prc.get("MKY", "Developers");
            assertEquals(1, projectRole.actors.size());
            assertEquals("jira-developers", projectRole.actors.get(0).name);
            assertEquals("atlassian-group-role-actor", projectRole.actors.get(0).type);

            // Users
            projectRole = prc.get("MKY", "Users");
            assertEquals(1, projectRole.actors.size());
            assertEquals("jira-users", projectRole.actors.get(0).name);
            assertEquals("atlassian-group-role-actor", projectRole.actors.get(0).type);
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testViewIssueIssueSecurityLevel() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportEnterpriseSimpleData.xml", "TestProjectImportEnterpriseSimpleDataEmptyProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));

            // Make sure we can see the issues in the navigator
            this.navigation.issueNavigator().displayAllIssues();
            tester.assertTextPresent("MKY-1");
            tester.assertTextPresent("MKY-2");

            // Now lets go verify that the issues data look the way they should
            this.navigation.issue().viewIssue("MKY-1");

            ViewIssueDetails details = this.parse.issue().parseViewIssuePage();
            assertEquals("MKY-1", details.getKey());
            assertEquals("Security Level 3", details.getSecurityLevel());

            this.navigation.issue().viewIssue("MKY-2");

            details = this.parse.issue().parseViewIssuePage();
            assertEquals("MKY-2", details.getKey());
            assertEquals("Security Level 2", details.getSecurityLevel());

            // Log out and make sure we can not see either issue
            this.navigation.logout();
            this.navigation.login("wilma", "wilma");
            this.navigation.issue().viewIssue("MKY-1");
            tester.assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
            this.navigation.issue().viewIssue("MKY-2");
            tester.assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");

            // Make sure we can not see the issues in the navigator as well
            this.navigation.issueNavigator().displayAllIssues();
            tester.assertTextNotPresent("MKY-1");
            tester.assertTextNotPresent("MKY-2");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
            this.navigation.logout();
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testIssueWorkflowActionsNonStandardWorkflow() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportEnterpriseCrazyWorkflowData.xml", "TestProjectImportEnterpriseCrazyWorkflowDataEmptyProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));

            // We want to go to the view Issue screen of the new issue.
            this.navigation.issue().viewIssue("MKY-1");
            tester.assertTextPresent("This is the test bug with all the field values set.");
            ViewIssueDetails viewIssueDetails = this.parse.issue().parseViewIssuePage();
            assertEquals("Crazy Open", viewIssueDetails.getStatus());
            assertTrue(viewIssueDetails.getAvailableWorkflowActions().contains("Be Dangerous"));
            assertTrue(viewIssueDetails.getAvailableWorkflowActions().contains("Be Crazy Done"));
            // Click Be Dangerous.
            tester.clickLinkWithText("Be Dangerous");
            // Check that the issue changed as expected.
            viewIssueDetails = this.parse.issue().parseViewIssuePage();
            assertEquals("Hecka Dangerous", viewIssueDetails.getStatus());
            assertTrue(viewIssueDetails.getAvailableWorkflowActions().contains("Be Dylan"));
            assertTrue(viewIssueDetails.getAvailableWorkflowActions().contains("Be Mark"));
            assertEquals(2, viewIssueDetails.getAvailableWorkflowActions().size());
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    public void testImportSubtask() throws Exception
    {
        File tempFile = null;

        try
        {
            tempFile = doProjectImport("TestProjectImportEnterpriseSimpleData.xml", "TestProjectImportEnterpriseSimpleDataEmptyProject.xml");

            // Make sure we are ready to import
            tester.assertTextPresent("The results of automatic mapping are displayed below. You will not be able to continue if any validation errors were raised");
            tester.assertSubmitButtonPresent("Import");

            tester.submit("Import");
            advanceThroughWaitingPage();

            // We should end up on the results page
            XPathLocator xPathLocator = new XPathLocator(tester, "//div[@id='systemfields']/ul");
            String projectDetailsResults = xPathLocator.getText();
            assertThat(projectDetailsResults, containsString("Key: MKY"));
            assertThat(projectDetailsResults, containsString("Description: This is a description for a monkey project."));

            // Make sure we can see the issues in the navigator
            this.navigation.issueNavigator().displayAllIssues();
            tester.assertTextPresent("MKY-1");
            tester.assertTextPresent("MKY-2");
            tester.assertTextPresent("MKY-3");

            // Now lets go verify that the issues data look the way they should
            this.navigation.issue().viewIssue("MKY-3");
            // assert some values that it was imported OK
            final ViewIssueDetails issueDetails = parse.issue().parseViewIssuePage();
            assertEquals("MKY-3", issueDetails.getKey());
            assertEquals("Sub-task 2", issueDetails.getIssueType());
            assertEquals("Gotta do some small stuff to get teh parent done.", issueDetails.getDescription());
            // It should mention that its parent is MKY-1
            tester.assertLinkPresentWithText("MKY-1");
            // and have a "convert to issue" action
            tester.clickLink("subtask-to-issue");
            tester.assertTextPresent("Convert Sub-task to Issue: MKY-3");
        }
        finally
        {
            if (tempFile != null)
            {
                tempFile.delete();
            }
        }
    }

    File doProjectImportUpdateProject(String backupFileName, String currentSystemXML, boolean overwrite)
    {
        File tempFile = importAndExportBackupAndSetupCurrentInstance(backupFileName, currentSystemXML);

        // Now set up an existing Monkey project
        long projectId = administration.project().addProject("King Monkey", "MKY", "john");
        administration.project().editProject(projectId, null, "existing monk thing", "http://www.kingkong.net");

        // Lets try our import
        this.navigation.gotoAdminSection("project_import");

        // Get to the project select page
        tester.setWorkingForm("project-import");
        tester.assertTextPresent("Project Import: Select Backup File");
        tester.setFormElement("backupXmlPath", tempFile.getAbsolutePath());
        tester.submit();

        advanceThroughWaitingPage();
        tester.assertTextPresent("Project Import: Select Project to Import");

        // Choose the MKY project
        tester.selectOption("projectKey", "monkey");
        if (overwrite)
        {
            tester.checkCheckbox("overwrite", "true");
        }
        else
        {
            tester.uncheckCheckbox("overwrite");
        }
        tester.submit("Next");
        advanceThroughWaitingPage();

        return tempFile;
    }

    public void navigateToUser(String username)
    {
        log("Navigating in UserBrowser to User " + username);
        tester.gotoPage(PAGE_USER_BROWSER);
        tester.clickLink(username);
    }

}
