package com.atlassian.jira.webtests.ztests.admin.issuetypes;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 *
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.ISSUES, Category.SCHEMES })
public class TestIssueTypeSchemeMigration extends JIRAWebTest
{
    public TestIssueTypeSchemeMigration(String name)
    {
        super(name);
    }

    public void testI18n()
    {
        administration.restoreI18nData("TestIssueTypeSchemeMigrationGerman.xml");
        final String baseUrl = getEnvironmentData().getBaseUrl().toString();
        log("Setting baseurl to '" + baseUrl + "'");
        navigation.gotoAdminSection("general_configuration");
        assertions.assertNodeByIdHasText("edit-app-properties", "Einstellungen bearbeiten");
        tester.clickLink("edit-app-properties");
        tester.setFormElement("baseURL", baseUrl);
        tester.submit("Aktualisieren");

        Long projectId = backdoor.project().getProjectId(PROJECT_MONKEY_KEY);
        tester.gotoPage("secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);

        tester.checkCheckbox("createType", "chooseScheme");
        // Select 'monkey' from select box 'schemeId'.
        tester.selectOption("schemeId", "monkey");
        tester.submit(" OK ");

        tester.submit("nextBtn");
        assertCurrentStep(2);

        tester.submit("previousBtn");
        assertCurrentStep(1);

        tester.submit("nextBtn");
        tester.submit("nextBtn");
        tester.submit("nextBtn");
        assertCurrentStep(4);

        XPathLocator finishLocator = locator.xpath("//*[@id=\"nextButton\" and @value=\"Fertigstellen\"]");
        assertNotNull(finishLocator.getNode());

        tester.submit("nextBtn");

        administration.generalConfiguration().setJiraLocaleToSystemDefault();
    }

    private void assertCurrentStep(int step)
    {
        XPathLocator locator = new XPathLocator(tester, "//*[@id=\"currentStep\" and @value=" + step + " ]");
        assertNotNull(locator.getNode());
    }

    public void testIssueTypeOptionsCorrectForIssueTypeMapping()
    {
        // NOTE: this data has a bunch of useless issue types so that we get id's of larger than 10
        // so that when they are ordered issue type with id 11 will come before issue type with id 2
        // this will exercise the problem area that we have fixed.
        administration.restoreData("TestIssueTypeSchemeMigration.xml");

        // We want to migrate the homosapien project to the Issue Type Scheme to Move to scheme
        // this will force us to be prompted with a option group of possible top-level issue
        // types for where we are going and a option group of possible sub-task issue types
        // for where we are going. We want to verify that this does not get reordered.

        Long projectId = backdoor.project().getProjectId(PROJECT_HOMOSAP_KEY);
        tester.gotoPage("secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);

        tester.selectOption("schemeId", "Issue Type Scheme to Move to");
        tester.submit(" OK ");

        // Make sure the correct issue types are on the summary page
        assertTextPresent("Issue Type Migration");

        text.assertTextPresent(locator.id("summary_table"), "New Feature");
        text.assertTextPresent(locator.id("summary_table"), "Sub-task ORIG");

        tester.submit("nextBtn");

        // We should be presented with the sub task issue type to map
        text.assertTextPresent(locator.css(".jiraform .instructions"), "Sub-task ORIG");

        // Make sure the correct options of subtask issue types for where we are going are available and that the
        // top-level issue types are not.
        tester.assertOptionsEqual("issuetype", new String[] { "Another Subtask", "Sub-task DIFFERENT" });

        // Move on to the next mapping, the mapping of the top-level issue type
        tester.submit("nextBtn");
        tester.submit("nextBtn");

        text.assertTextPresent(locator.css(".jiraform .instructions"), "New Feature");
        tester.assertOptionsEqual("issuetype", new String[] { "Improvement", "Bug" });
    }

    public void testIssueTypeSchemeMigrationNoSubtasksMultiProjects() throws SAXException
    {
        administration.restoreData("TestIssueTypeSchemeMigrationNoSubtasks.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        //check pre-conditions: This isn't a 100% but good enough for the purpose of this test.
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getDialog().getResponse().getTableWithID("issuetable");
        assertTableCellHasImage(issueTable, 1, 0, "issuetypes/subtask_alternate.png");
        assertTableCellHasImage(issueTable, 2, 0, "issuetypes/newfeature.png");
        assertTableCellHasImage(issueTable, 3, 0, "issuetypes/genericissue.png");
        assertTableCellHasImage(issueTable, 4, 0, "issuetypes/bug.png");
        navigation.issue().gotoIssue("HSP-4");
        assertTextPresentAfterText("Another Subtask", "Type:");
        navigation.issue().gotoIssue("HSP-3");
        assertTextPresentAfterText("New Feature", "Type:");
        navigation.issue().gotoIssue("HSP-2");
        assertTextPresentAfterText("Sub-task ORIG", "Type:");
        navigation.issue().gotoIssue("HSP-1");
        assertTextPresentAfterText("Bug", "Type:");

        //first test that migrating to multiple projects does not work if a project has subtasks and the scheme
        //doesn't have subtask types.
        navigation.gotoAdminSection("issue_type_schemes");
        tester.clickLinkWithText("Issue Type Schemes");
        tester.clickLink("associate_10030");
        selectMultiOptionByValue("projects", "10000");
        selectMultiOptionByValue("projects", "10001");
        tester.submit("Associate");
        //ensure we get an error!
        assertTextPresent("There are 2 sub-tasks that will be affected by this change. You must have at least one valid sub-task issue type.");

        //then test the successfulMultiproject migration to a scheme with subtasks.
        navigation.gotoAdminSection("issue_type_schemes");
        tester.clickLinkWithText("Issue Type Schemes");
        tester.clickLink("associate_10020");
        selectMultiOptionByValue("projects", "10000");
        selectMultiOptionByValue("projects", "10001");
        tester.submit("Associate");

        //check if the correct project/issue types to be modifed are shown
        assertCollapsedTextSequence(new String[] {
                "Issue Type Migration: Overview (Step 1 of 6)",
                "homosapien", "New Feature", "1",
                "homosapien", "Sub-task ORIG", "1"
        });
        tester.submit("nextBtn");


        assertCollapsedTextSequence(new String[] {
                "Issue Type Migration: Select Issue Type (Step 2 of 6)",
                "Select a new issue type for issues with current issue type ",
                "Sub-task ORIG",
                " in project ",
                "homosapien"
        });
        //lets select a different subtask issue type.
        tester.selectOption("issuetype", "Sub-task DIFFERENT");
        tester.submit("nextBtn");

        //no field should need to be mapped
        assertCollapsedTextSequence(new String[] {
                "Issue Type Migration: Update Fields (Step 3 of 6)",
                "Update fields for issues with current issue type ",
                "Sub-task ORIG",
                " in project ",
                "homosapien."
        });
        assertTextPresent("All field values will be retained.");
        tester.submit("nextBtn");

        //no field should need to be mapped
        assertCollapsedTextSequence(new String[] {
                "Issue Type Migration: Select Issue Type (Step 4 of 6)",
                "Select a new issue type for issues with current issue type ",
                "New Feature",
                " in project ",
                "homosapien",
        });
        //lets select a new issue type.
        tester.selectOption("issuetype", "Bug");
        tester.submit("nextBtn");

        //check if any of the fields for the new feature type should be mapped
        assertCollapsedTextSequence(new String[] {
                "Issue Type Migration: Update Fields (Step 5 of 6)",
                "Update fields for issues with current issue type ",
                "New Feature",
                " in project ",
                "homosapien."
        });
        assertTextPresent("All field values will be retained.");
        tester.submit("nextBtn");

        //submit past the confirmation screen.
        tester.submit("nextBtn");

        waitAndReloadBulkOperationProgressPage(true);

        //check the correct issue types are shown in the issue navigator.
        navigation.issueNavigator().displayAllIssues();
        issueTable = getDialog().getResponse().getTableWithID("issuetable");
        assertTableCellHasImage(issueTable, 1, 0, "issuetypes/subtask_alternate.png");
        assertTableCellHasImage(issueTable, 2, 0, "issuetypes/bug.png");
        assertTableCellHasImage(issueTable, 3, 0, "issuetypes/genericissue.png");
        assertTableCellHasImage(issueTable, 4, 0, "issuetypes/bug.png");
        navigation.issue().gotoIssue("HSP-4");
        assertTextPresentAfterText("Another Subtask", "Type:");
        navigation.issue().gotoIssue("HSP-3");
        assertTextPresentAfterText("Bug", "Type:");
        navigation.issue().gotoIssue("HSP-2");
        assertTextPresentAfterText("Sub-task DIFFERENT", "Type:");
        navigation.issue().gotoIssue("HSP-1");
        assertTextPresentAfterText("Bug", "Type:");
    }

    public void testIssueTypeSchemeMigrationMultiProjectsSecurityLevels()
    {
        administration.restoreData("TestIssueTypeSchemeMigrationSecurityLevel.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        assertPrecondition();

        //first test that migrating to multiple projects does not work if a project has subtasks and the scheme
        //doesn't have subtask types.
        navigation.gotoAdminSection("issue_type_schemes");
        tester.clickLinkWithText("Issue Type Schemes");
        tester.clickLink("associate_10060");
        selectMultiOptionByValue("projects", "10000");
        selectMultiOptionByValue("projects", "10020");
        selectMultiOptionByValue("projects", "10021");
        selectMultiOptionByValue("projects", "10022");
        tester.submit("Associate");
        assertTextPresent("There are 5 sub-tasks that will be affected by this change. You must have at least one valid sub-task issue type.");

        assertPrecondition();

        //now lets move to an issue type scheme with different issue types.
        navigation.gotoAdminSection("issue_type_schemes");
        tester.clickLinkWithText("Issue Type Schemes");
        tester.clickLink("associate_10061");
        selectMultiOptionByValue("projects", "10000");
        selectMultiOptionByValue("projects", "10020");
        selectMultiOptionByValue("projects", "10021");
        selectMultiOptionByValue("projects", "10022");
        tester.submit("Associate");
        assertCollapsedTextSequence(new String[] {
                "Issue Type Migration: Overview (Step 1 of 6)",
                "Bovine", "Sub-task", "3",
                "Rattus", "Sub-task", "2"
        });
        tester.submit("nextBtn");

        assertCollapsedTextSequence(new String[] {
                "Select a new issue type for issues with current issue type ",
                "Sub-task",
                "in project ",
                "Bovine"
        });
        tester.submit("nextBtn");

        assertCollapsedTextSequence(new String[] {
                "Issue Type Migration: Update Fields (Step 3 of 6)",
                "All field values will be retained."
        });
        tester.submit("nextBtn");

        assertCollapsedTextSequence(new String[] {
                "Issue Type Migration: Select Issue Type (Step 4 of 6)",
                "Select a new issue type for issues with current issue type ",
                "Sub-task",
                "in project",
                "Rattus"
        });
        tester.submit("nextBtn");

        assertCollapsedTextSequence(new String[] {
                "Issue Type Migration: Update Fields (Step 5 of 6)",
                "All field values will be retained."
        });
        tester.submit("nextBtn");
        tester.submit("nextBtn");

        //check issues to see they've still got the security level set.
        assertPrecondition();

        //now go to an issue and make sure it's got the right type and level.
        navigation.issue().gotoIssue("RAT-7");
        assertTextPresentAfterText("Level Mouse", "Security");
        assertTextPresentAfterText("DIFFERENT SUBTASK", "Type");

        navigation.issue().gotoIssue("COW-36");
        assertTextPresentAfterText("MyFriendsOnly", "Security");
        assertTextPresentAfterText("DIFFERENT SUBTASK", "Type");
    }

    // added to validate JRA-16052
    public void testIssueTypeMigrationWithNumericCustomField()
    {
        administration.restoreData("TestIssueTypeMigrationWithNumericCustomField.xml");

        navigation.gotoAdminSection("issue_type_schemes");
        tester.clickLinkWithText("Issue Type Schemes");

        tester.clickLink("associate_10001");
        tester.selectOption("projects", "homosapien");
        tester.submit("Associate");
        tester.submit("nextBtn");
        tester.submit("nextBtn");

        tester.setFormElement("customfield_10000", "LOREM_IPSUM");
        tester.submit("nextBtn");

        // verify that we are still on step 3 and have an error message in the form.
        assertTextPresent("&#39;LOREM_IPSUM&#39; is an invalid number");
        assertTextPresent("Issue Type Migration: Update Fields (Step 3 of 4)");

        // now make sure the next step works when we use a number.
        tester.setFormElement("customfield_10000", "857");
        tester.submit("nextBtn");

        assertTextPresent("Issue Type Migration: Confirmation (Step 4 of 4)");
        assertTextPresent("857");

    }

    // This is here to test the bug in JRA-10244, the problem that subtasks whose parents
    // issue type is obsolete in the destination context are removed from the list of issues
    // to be operated on.
    public void testIssueTypeSchemeMigrationWithSubtasks() throws SAXException
    {
        administration.restoreData("TestIssueTypeSchemeMigrationWithSubtasks.xml");


        Long projectId = backdoor.project().getProjectId(PROJECT_HOMOSAP_KEY);
        tester.gotoPage("secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);

        tester.selectOption("schemeId", "Issue Type Scheme to Move to");
        tester.submit(" OK ");

        // Make sure the correct issue types are on the summary page
        assertTextPresent("Issue Type Migration");

        // Before we fixed the bug the sub task would not have been here
        WebTable summaryTable = getDialog().getResponse().getTableWithID("summary_table");
        assertTrue(tableCellHasText(summaryTable, 1, 1, "Bug"));
        assertTrue(tableCellHasText(summaryTable, 2, 1, "Sub-task ORIG"));

        tester.submit("nextBtn");

        // We should be presented with the bug issue type map
        assertTextPresent("Issue Type Migration");
        tester.selectOption("issuetype", "New Feature");
        tester.submit("nextBtn");
        tester.submit("nextBtn");

        // We should be presented with the sub task issue type to map
        assertTextPresent("Issue Type Migration");
        tester.selectOption("issuetype", "Sub-task DIFFERENT");
        tester.submit("nextBtn");
        tester.submit("nextBtn");

        // Complete the operation
        tester.submit("nextBtn");

        // Browse to the issues and confirm that everything has been changed correctly
        navigation.issue().gotoIssue("HSP-1");
        assertTextPresent("New Feature");
        navigation.issue().gotoIssue("HSP-2");
        assertTextPresent("Sub-task DIFFERENT");
    }

    private void assertPrecondition()
    {
        //first lets check we have an issue with subtasks and they all have a particular security level set.
        navigation.issueNavigator().displayAllIssues();

        // Rat Issues
        assertTableCellHasText("issuetable", 1, 1, "RAT-7");
        assertTableCellHasText("issuetable", 1, 2, "RAT-5");
        assertTableCellHasText("issuetable", 1, 11, "Level Mouse");
        assertTableCellHasText("issuetable", 2, 1, "RAT-6");
        assertTableCellHasText("issuetable", 2, 2, "RAT-5");
        assertTableCellHasText("issuetable", 2, 11, "Level Mouse");
        assertTableCellHasText("issuetable", 3, 1, "RAT-5");
        assertTableCellHasText("issuetable", 3, 11, "Level Mouse");

        // Cow Issues
        assertTableCellHasText("issuetable", 4, 1, "COW-37");
        assertTableCellHasText("issuetable", 4, 2, "COW-35");
        assertTableCellHasText("issuetable", 4, 2, "Lets get a third milk bucket");
        assertTableCellHasText("issuetable", 4, 11, "MyFriendsOnly");
        assertTableCellHasText("issuetable", 5, 1, "COW-36");
        assertTableCellHasText("issuetable", 5, 2, "COW-35");
        assertTableCellHasText("issuetable", 5, 2, "Get another milk bucket");
        assertTableCellHasText("issuetable", 5, 11, "MyFriendsOnly");
        assertTableCellHasText("issuetable", 6, 1, "COW-35");
        assertTableCellHasText("issuetable", 6, 2, "No more milk");
        assertTableCellHasText("issuetable", 6, 11, "MyFriendsOnly");
        assertTableCellHasText("issuetable", 7, 1, "COW-34");
        assertTableCellHasText("issuetable", 7, 2, "COW-35");
        assertTableCellHasText("issuetable", 7, 2, "Get new milk bucket");
        assertTableCellHasText("issuetable", 7, 11, "MyFriendsOnly");
    }
}
