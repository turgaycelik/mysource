package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.ProjectClient;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Performs functional tests on Security Level field of subtasks.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY, Category.SUB_TASKS })
public class TestSecurityLevelOfSubtasks extends JIRAWebTest
{
    public TestSecurityLevelOfSubtasks(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestSecurityLevelOfSubtasks.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    public void testBulkEditWithSubtasksOnly()
    {
        // Go to issue navigator.
        showAllIssues();
        // Choose Bulk Operation
        bulkChangeIncludeAllPages();

        // Choose to operate on two subtasks - RAT-7 and RAT-9
        checkCheckbox("bulkedit_10051", "on");
        checkCheckbox("bulkedit_10032", "on");
        submit("Next");

        // Choose "Bulk Edit"
        assertCollapsedTextSequence(new String[] {
            "Step 2 of 4: Choose Operation",
            "Choose the operation you wish to perform on the selected 2 issue(s)."
        });
        checkCheckbox("operation", "bulk.edit.operation.name");
        submit("Next");

        // Operation Details
        assertCollapsedTextSequence(new String[] {
            "Step 3 of 4: Operation Details",
            "Choose the bulk action(s) you wish to perform on the selected 2 issue(s)."
        });
        // Check the table with editable fields:
        assertTableCellHasText("availableActionsTable", 1, 1, "Change Security Level");
        WebTable webTable = getWebTableWithID("availableActionsTable");
        // Assert we cannot set security level: read-only message.
        assertEquals("The security level of subtasks is inherited from parents.", webTable.getCellAsText(1,2).trim());
        // Change the priority
        checkCheckbox("actions", "priority");
        selectOption("priority", "Minor");
        // Change the assignee
        checkCheckbox("actions", "assignee");
        selectOption("assignee", "Henry Ford");
        submit("Next");

        // Confirmation Screen
        assertCollapsedTextSequence(new String[] {
            "Step 4 of 4: Confirmation",
            "Updated Fields",
            "Priority",
            "Minor",
            "Assignee",
            "Henry Ford",
            "The above table summarises the changes you are about to make"
        });
        // Check the issue table
        webTable = getWebTableWithID("issuetable");
        // Issues appear to be returned in a random order - so find the correct order.
        int rat9Row;
        int rat7Row;
        if (webTable.getCellAsText(1, 1).trim().equals("RAT-9"))
        {
            rat9Row = 1;
            rat7Row = 2;
        }
        else
        {
            rat9Row = 2;
            rat7Row = 1;
        }
        assertEquals("RAT-9", webTable.getCellAsText(rat9Row, 1).trim());
        // Show the old value for assignee
        assertEquals("Mark", webTable.getCellAsText(rat9Row, 3).trim());
        assertEquals("Level KingRat", webTable.getCellAsText(rat9Row, 11).trim());
        assertEquals("RAT-7", webTable.getCellAsText(rat7Row, 1).trim());
        assertEquals("Henry Ford", webTable.getCellAsText(rat7Row, 3).trim());
        assertEquals("Level Mouse", webTable.getCellAsText(rat7Row, 11).trim());
        submit("Confirm");
        waitAndReloadBulkOperationProgressPage();

        // Back to navigator
        // Check the issue table
        webTable = getWebTableWithID("issuetable");
        assertEquals("RAT-9", webTable.getCellAsText(1, 1).trim());
        // New Assignee
        assertEquals("Henry Ford", webTable.getCellAsText(1, 3).trim());
        assertEquals("Level KingRat", webTable.getCellAsText(1, 11).trim());
        assertEquals("RAT-7", webTable.getCellAsText(3, 1).trim());
        // New Assignee
        assertEquals("Henry Ford", webTable.getCellAsText(3, 3).trim());
        assertEquals("Level Mouse", webTable.getCellAsText(3, 11).trim());
    }

    /**
     * This test will bulk edit a "standard" issue as well as a subtask.
     * The parent of the subtask is not included in the bulk edit.
     */
    public void testBulkEditWithStandardIssueAndSubTask()
    {
        // Go to issue navigator
        showAllIssues();

        // Do Bulk Operation
        bulkChangeIncludeAllPages();

        // Select Issues
        checkCheckbox("bulkedit_10032", "on");
        checkCheckbox("bulkedit_10050", "on");
        submit("Next");

        // Choose "Bulk Edit" operation.
        assertCollapsedTextSequence(new String[] {
            "Step 2 of 4: Choose Operation",
            "Choose the operation you wish to perform on the selected 2 issue(s)."
        });
        checkCheckbox("operation", "bulk.edit.operation.name");
        submit("Next");

        // On the Edit field screen
        assertTextPresent("Change Security Level");
        // We should now have an editable drop-down for security level. This should affect the standard Issue and its
        // subtasks, but be ignored by the "orphan" subtask.
        checkCheckbox("actions", "security");
        selectOption("security", "None");
        submit("Next");

        // Confirmation Screen.
        assertTextPresent("Step 4 of 4: Confirmation");
        assertTextPresent("Updated Fields");
        assertTextPresent("None");
        assertTextPresent("The above table summarises the changes you are about to make to the following <strong>2</strong> issues. Do you wish to continue?");
        submit("Confirm");
        waitAndReloadBulkOperationProgressPage();

        // navigator      
        // Re-assert the Rat Issues
        // RAT-8 and its subtask will be changed to No security level.
        // Rat-7 which was selected in the Bulk Edit operation, should nevertheless still have a security level of
        // "Mouse", agreeing with its parent.
        WebTable issueTable = getWebTableWithID("issuetable");
        assertTableCellHasText("issuetable", 1, 1, "RAT-9");
        assertTableCellHasText("issuetable", 1, 2, "RAT-8");
        assertEquals("", issueTable.getCellAsText(1, 11).trim());
        assertTableCellHasText("issuetable", 2, 1, "RAT-8");
        assertEquals("", issueTable.getCellAsText(2, 11).trim());
        assertTableCellHasText("issuetable", 3, 1, "RAT-7");
        assertTableCellHasText("issuetable", 3, 2, "RAT-5");
        assertEquals("Level Mouse", issueTable.getCellAsText(3, 11).trim());
    }

    public void testEditParentIssue()
    {
        // Go to issue navigator
        showAllIssues();
        // go to issue RAT-8
        clickLinkWithText("RAT-8");
        assertTextPresent("A top level task.");
        // Edit the issue
        clickLink("edit-issue");
        assertTextPresent("Edit Issue");

        // Set security to none
        selectOption("security", "None");
        submit("Update");

        // View navigator
        navigation.issue().returnToSearch();
        // RAT-8 and its subtask will be changed to No security level.
        WebTable issueTable = getWebTableWithID("issuetable");
        assertTableCellHasText("issuetable", 1, 1, "RAT-9");
        assertTableCellHasText("issuetable", 1, 2, "RAT-8");
        assertEquals("", issueTable.getCellAsText(1, 11).trim());
        assertTableCellHasText("issuetable", 2, 1, "RAT-8");
        assertEquals("", issueTable.getCellAsText(2, 11).trim());

        // -- Now move it to level mouse. ----------------------------
        // go to issue RAT-8
        clickLinkWithText("RAT-8");
        assertTextPresent("A top level task.");
        // Edit the issue
        clickLink("edit-issue");
        assertTextPresent("Edit Issue");

        // Set security to Level KingRat
        selectOption("security", "Level KingRat");
        submit("Update");

        // View navigator
        navigation.issue().returnToSearch();
        // RAT-8 and its subtask will be changed to KingRat security level.
        issueTable = getWebTableWithID("issuetable");
        assertTableCellHasText("issuetable", 1, 1, "RAT-9");
        assertTableCellHasText("issuetable", 1, 2, "RAT-8");
        assertEquals("Level KingRat", issueTable.getCellAsText(1, 11).trim());
        assertTableCellHasText("issuetable", 2, 1, "RAT-8");
        assertEquals("Level KingRat", issueTable.getCellAsText(2, 11).trim());
    }

    public void testWorkflowTransitionParentIssue()
    {
        // Go to issue navigator
        showAllIssues();
        // Go to RAT-8
        clickLinkWithText("RAT-8");
        // Workflow transition "Resolve Issue"
        clickLink("action_id_5");
        setWorkingForm("issue-workflow-transition");
        assertTextPresent("Resolve Issue");
        assertTextPresent("Resolving an issue indicates that the developers are satisfied the issue is finished.");
        // Set Security level field to "none" as part of the workflow.
        selectOption("security", "None");
        submit("Transition");

        // We are on the issue view screen. Because the security level is none, the field is not even shown:
        assertTextNotPresent("Security Level");

        // return to navigator
        navigation.issue().returnToSearch();
        // Check security level of parent and subtask:
        WebTable issueTable = getWebTableWithID("issuetable");
        assertTableCellHasText("issuetable", 1, 1, "RAT-9");
        assertTableCellHasText("issuetable", 1, 2, "RAT-8");
        assertEquals("", issueTable.getCellAsText(1, 11).trim());
        assertTableCellHasText("issuetable", 2, 1, "RAT-8");
        assertEquals("", issueTable.getCellAsText(2, 11).trim());
    }

    public void testWorkflowTransitionSubtask()
    {
        // Go to issue navigator
        showAllIssues();
        // Go to RAT-9
        clickLinkWithText("RAT-9");
        // assert that the security level is KingRat
        assertTextPresent("Security Level:");
        assertTextPresent("Level KingRat");
        // Workflow transition "Resolve Issue"
        clickLink("action_id_5");
        setWorkingForm("issue-workflow-transition");
        assertTextPresent("Resolve Issue");
        assertTextPresent("Resolving an issue indicates that the developers are satisfied the issue is finished.");
        // Security Level field is inherited from parents for subtasks, so check that we have a read-only message.
        assertFormElementNotPresent("security");
        assertTextPresent("The security level of subtasks is inherited from parents.");
        submit("Transition");

        // We are on the issue view screen. Because the security level is none, the field is not even shown:
        // assert that the security level is still KingRat
        assertTextPresent("Security Level:");
        assertTextPresent("Level KingRat");

        // return to navigator
        navigation.issue().returnToSearch();
        // Check security level of parent and subtask:
        WebTable issueTable = getWebTableWithID("issuetable");
        assertTableCellHasText("issuetable", 1, 1, "RAT-9");
        assertTableCellHasText("issuetable", 1, 2, "RAT-8");
        assertEquals("Level KingRat", issueTable.getCellAsText(1, 11).trim());
        assertTableCellHasText("issuetable", 2, 1, "RAT-8");
        assertEquals("Level KingRat", issueTable.getCellAsText(2, 11).trim());
    }

    public void testChangeProjectsSecurityLevelScheme()
    {
        // Assert Precondition.
        assertPrecondition();

        Long projectId = backdoor.project().getProjectId("RAT");
        Long schemeId = backdoor.project().getSchemes(projectId).issueSecurityScheme.id;
        tester.gotoPage("/secure/project/SelectProjectIssueSecurityScheme!default.jspa?projectId=" + projectId + "&schemeId=" + schemeId);

        // Change Security Scheme to DogSecurityScheme
        selectOption("newSchemeId", "DogSecurityScheme");
        submit("Next >>");
        assertTextPresent("Associate Issue Security Scheme to Project");
        // Change Kingrat to red
        selectOption("level_10025", "Level Red");
        // Change mouse to green
        selectOption("level_10026", "Level Green");
        submit("Associate");

        assertThat(new ProjectClient(environmentData).get("RAT").name, equalTo("Rattus"));

        // go to navigator
        displayAllIssues();
        // Assert the security levels have moved as expected.
        assertTableCellHasText("issuetable", 1, 1, "RAT-9");
        assertTableCellHasText("issuetable", 1, 2, "RAT-8");
        assertTableCellHasText("issuetable", 1, 11, "Level Red");
        assertTableCellHasText("issuetable", 2, 1, "RAT-8");
        assertTableCellHasText("issuetable", 2, 11, "Level Red");
        assertTableCellHasText("issuetable", 3, 1, "RAT-7");
        assertTableCellHasText("issuetable", 3, 2, "RAT-5");
        assertTableCellHasText("issuetable", 3, 11, "Level Green");
        assertTableCellHasText("issuetable", 4, 1, "RAT-6");
        assertTableCellHasText("issuetable", 4, 2, "RAT-5");
        assertTableCellHasText("issuetable", 4, 11, "Level Green");
        assertTableCellHasText("issuetable", 5, 1, "RAT-5");
        assertTableCellHasText("issuetable", 5, 11, "Level Green");
    }

    private void assertPrecondition()
    {
        showAllIssues();
    }

    private void showAllIssues()
    {
        // Go to issue navigator
        displayAllIssues();

        // Assert Issue Set up
        // Rat Issues
        assertTableCellHasText("issuetable", 1, 1, "RAT-9");
        assertTableCellHasText("issuetable", 1, 2, "RAT-8");
        assertTableCellHasText("issuetable", 1, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 2, 1, "RAT-8");
        assertTableCellHasText("issuetable", 2, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 3, 1, "RAT-7");
        assertTableCellHasText("issuetable", 3, 2, "RAT-5");
        assertTableCellHasText("issuetable", 3, 11, "Level Mouse");
        assertTableCellHasText("issuetable", 4, 1, "RAT-6");
        assertTableCellHasText("issuetable", 4, 2, "RAT-5");
        assertTableCellHasText("issuetable", 4, 11, "Level Mouse");
        assertTableCellHasText("issuetable", 5, 1, "RAT-5");
        assertTableCellHasText("issuetable", 5, 11, "Level Mouse");

        // Cow Issues
        assertTableCellHasText("issuetable", 6, 1, "COW-37");
        assertTableCellHasText("issuetable", 6, 2, "COW-35");
        assertTableCellHasText("issuetable", 6, 2, "Lets get a third milk bucket");
        assertTableCellHasText("issuetable", 7, 11, "MyFriendsOnly");
        assertTableCellHasText("issuetable", 7, 1, "COW-36");
        assertTableCellHasText("issuetable", 7, 2, "COW-35");
        assertTableCellHasText("issuetable", 7, 2, "Get another milk bucket");
        assertTableCellHasText("issuetable", 7, 11, "MyFriendsOnly");
        assertTableCellHasText("issuetable", 8, 1, "COW-35");
        assertTableCellHasText("issuetable", 8, 2, "No more milk");
        assertTableCellHasText("issuetable", 8, 11, "MyFriendsOnly");
        assertTableCellHasText("issuetable", 9, 1, "COW-34");
        assertTableCellHasText("issuetable", 9, 2, "COW-35");
        assertTableCellHasText("issuetable", 9, 2, "Get new milk bucket");
        assertTableCellHasText("issuetable", 9, 11, "MyFriendsOnly");
    }
}
