package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestReindexingSubtasks extends JIRAWebTest
{
    public TestReindexingSubtasks(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestReindexingSubtasks.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    public void testEditSingleIssue() throws SAXException
    {
        assertPrecondition();
        assertTableCellHasText("issuetable", 1, 1, "RAT-7");
        assertTableCellHasText("issuetable", 1, 11, "Level Mouse");
        assertTableCellHasText("issuetable", 2, 1, "RAT-6");
        assertTableCellHasText("issuetable", 2, 11, "Level Mouse");
        assertTableCellHasText("issuetable", 3, 1, "RAT-5");
        assertTableCellHasText("issuetable", 3, 11, "Level Mouse");
        // Go to issue "RAT-5"
        clickLinkWithText("RAT-5");
        // Edit issue
        clickLink("edit-issue");
        // Assert current Security Level is "Level Mouse"
        assertTextSequence(new String[] { "Security Level", "Level Mouse", "Priority" });
        // Change security to "Level KingRat"
        selectOption("security", "Level KingRat");
        submit("Update");
        navigation.issue().returnToSearch();
        // Assert that all issues are set to "Level KingRat"
        assertTableCellHasText("issuetable", 1, 1, "RAT-7");
        assertTableCellHasText("issuetable", 1, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 2, 1, "RAT-6");
        assertTableCellHasText("issuetable", 2, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 3, 1, "RAT-5");
        assertTableCellHasText("issuetable", 3, 11, "Level KingRat");
        assertTextNotPresent("Level Mouse");
    }

    public void testMoveSingleIssue()
    {
        assertPrecondition();

        //now let's go to the parent issue and do a move asserting each screen along the way.
        gotoIssue("COW-35");
        clickLink("move-issue");
        assertTextPresent("Move Issue");
        selectOption("10000_4_pid", "Porcine");
        selectOption("10000_4_issuetype", "Bug");
        submit("Next");
        submit("Next");
        assertTextPresent("Update Fields");
        submit("Next");
        submit("Next");
        assertTextPresent("Below is a summary of all issues that will be moved");
        assertTextSequence(new String[] { "Project", "Porcine" });
        assertTextSequence(new String[] { "Type", "Bug" });
        assertTextSequence(new String[] { "Security Level", "None" });
        submit("Next");
        waitAndReloadBulkOperationProgressPage();

        displayAllIssues();
        //check the security level is gone
        assertTextNotPresent("MyFriendsOnly");
        assertTableCellHasText("issuetable", 4, 1, "PIG-12");
        assertTableCellHasText("issuetable", 4, 2, "PIG-9");
        assertTableCellHasText("issuetable", 4, 2, "Lets get a third milk bucket");
        assertTableCellHasText("issuetable", 5, 1, "PIG-11");
        assertTableCellHasText("issuetable", 5, 2, "PIG-9");
        assertTableCellHasText("issuetable", 5, 2, "Get another milk bucket");
        assertTableCellHasText("issuetable", 6, 1, "PIG-10");
        assertTableCellHasText("issuetable", 6, 2, "PIG-9");
        assertTableCellHasText("issuetable", 6, 2, "Get new milk bucket");
        assertTableCellHasText("issuetable", 7, 1, "PIG-9");
        assertTableCellHasText("issuetable", 7, 2, "No more milk");

        //now lets move the issue to yet another project with a security level this time..
        gotoIssue("PIG-9");
        clickLink("move-issue");
        assertTextPresent("Move Issue");
        selectOption("10021_1_pid", "Rattus");
        submit("Next");
        submit("Next");
        assertTextPresent("Update Fields");
        //set the security level.
        selectOption("security", "Level KingRat");
        submit("Next");
        submit("Next");
        assertTextSequence(new String[] { "Project", "Rattus" });
        assertTextSequence(new String[] { "Type", "Bug" });
        assertTextSequence(new String[] { "Security Level", "Level KingRat" });
        submit("Next");
        waitAndReloadBulkOperationProgressPage();
        assertTextPresent("Level KingRat");

        displayAllIssues();
        assertTableCellHasText("issuetable", 1, 1, "RAT-11");
        assertTableCellHasText("issuetable", 1, 2, "RAT-8");
        assertTableCellHasText("issuetable", 1, 2, "Lets get a third milk bucket");
        assertTableCellHasText("issuetable", 1, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 2, 1, "RAT-10");
        assertTableCellHasText("issuetable", 2, 2, "RAT-8");
        assertTableCellHasText("issuetable", 2, 2, "Get another milk bucket");
        assertTableCellHasText("issuetable", 2, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 3, 1, "RAT-9");
        assertTableCellHasText("issuetable", 3, 2, "RAT-8");
        assertTableCellHasText("issuetable", 3, 2, "Get new milk bucket");
        assertTableCellHasText("issuetable", 3, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 4, 1, "RAT-8");
        assertTableCellHasText("issuetable", 4, 2, "No more milk");
        assertTableCellHasText("issuetable", 4, 11, "Level KingRat");
    }

    public void testWorkflowTransitionSingleIssue() throws SAXException
    {
        assertPrecondition();
        // Open the "RAT-5" issue
        clickLinkWithText("RAT-5");
        assertTextPresent("Details");
        assertTextPresent("RAT-5");
        // Click the "Close issue" link
        clickLink("action_id_2");
        setWorkingForm("issue-workflow-transition");
        assertTextPresent("Close Issue");
        assertTextPresent("Closing an issue indicates that there is no more work to be done on it, and that it has been verified as complete.");
        // Change the security level to "Level KingRat" on our Workflow transition screen
        selectOption("security", "Level KingRat");
        submit("Transition");

        // Search for all issues again
        displayAllIssues();
        // Assert that all issues are set to "Level KingRat"
        assertTextNotPresent("Level Mouse");
        assertTableCellHasText("issuetable", 1, 1, "RAT-7");
        assertTableCellHasText("issuetable", 1, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 2, 1, "RAT-6");
        assertTableCellHasText("issuetable", 2, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 3, 1, "RAT-5");
        assertTableCellHasText("issuetable", 3, 11, "Level KingRat");

        // Open the "RAT-5" issue again
        clickLinkWithText("RAT-5");
        // Re-open the issue
        clickLink("action_id_3");
        setWorkingForm("issue-workflow-transition");
        // Now set security to None
        selectOption("security", "None");
        submit("Transition");
        displayAllIssues();
        // Assert that all issues are set to None
        assertTextNotPresent("Level KingRat");
        assertTextNotPresent("Level Mouse");
        WebTable issueTable = getIssuesTable();
        assertEquals("RAT-7", issueTable.getCellAsText(1, 1).trim());
        assertEquals("", issueTable.getCellAsText(1, 11).trim());
        assertEquals("RAT-6", issueTable.getCellAsText(2, 1).trim());
        assertEquals("", issueTable.getCellAsText(2, 11).trim());
        assertEquals("RAT-5", issueTable.getCellAsText(3, 1).trim());
        assertEquals("", issueTable.getCellAsText(3, 11).trim());
    }

    public void testBulkEditIssue()
    {
        assertPrecondition();

        //try bulk editing a single parent issue.
        bulkChangeIncludeAllPages();
        checkCheckbox("bulkedit_10033", "on");
        assertTextPresent("Step 1 of 4: Choose Issues");
        submit("Next");
        assertTextPresent("Step 2 of 4: Choose Operation");
        checkCheckbox("operation", "bulk.edit.operation.name");
        submit("Next");
        checkCheckbox("actions", "security");
        selectOption("security", "A");
        submit("Next");
        assertTextPresent("Step 4 of 4: Confirmation");
        assertTextSequence(new String[] { "Security Level", "A" });
        submit("Confirm");
        waitAndReloadBulkOperationProgressPage();

        //we should be on the issue navigator now.
        assertTextPresent("Issue Navigator");
        assertTextNotPresent("MyFriendsOnly");

        //now lets edit parent and one of the 4 subtasks
        bulkChangeIncludeAllPages();
        checkCheckbox("bulkedit_10033", "on");
        checkCheckbox("bulkedit_10041", "on");
        submit("Next");
        checkCheckbox("operation", "bulk.edit.operation.name");
        submit("Next");
        checkCheckbox("actions", "security");
        selectOption("security", "MyFriendsOnly");
        submit("Next");
        //check both issues are present, as well as the field that's about to change.
        assertTextPresent("No more milk");
        assertTextPresent("Lets get a third milk bucket");
        assertTextSequence(new String[] { "Security Level", "MyFriendsOnly" });
        submit("Confirm");
        waitAndReloadBulkOperationProgressPage();

        //now check that all the issues have the original security level again in the issue navigator.
        assertPrecondition();
    }

    public void testBulkMoveIssue() throws SAXException
    {
        assertPrecondition();

        // Do a Bulk Operation
        bulkChangeIncludeAllPages();

        // Assert we are at Step 1 of 4
        assertTextPresent("Step 1 of 4: Choose Issues");
        assertTextPresent("Step 1 of 4");
        // Select RAT-5 RAT-6 and COW-35
        checkCheckbox("bulkedit_10030", "on");
        checkCheckbox("bulkedit_10031", "on");
        checkCheckbox("bulkedit_10033", "on");
        submit("Next");

        // Assert we are at second step
        assertCollapsedTextSequence(new String[] {
                "Step 2 of 4: Choose Operation",
                "Choose the operation you wish to perform on the selected 3 issue(s)."
        });
        // Select "Bulk Move" as the operation.
        checkCheckbox("operation", "bulk.move.operation.name");
        submit("Next");

        // Assert step 3 - Parent Issues
        assertCollapsedTextSequence(new String[] {
                "Step 3 of 4",
                "Select Projects and Issue Types",
                "Please note that 1 sub-task issues were removed from the selection and do not appear in the table below. You are not allowed to bulk move sub-task issues together with their parent issue. In this case, you will only be asked to move the sub-task if you move the parent issue to a new project.",
                "The change will affect 1 issues with issue type(s) Improvement in project(s) Bovine.",
                "The change will affect 1 issues with issue type(s) Bug in project(s) Rattus."
        });
        // Change Bovine/Improvement to DOG/New Feature
        selectOption("10000_4_pid", "Canine");
        selectOption("10000_4_issuetype", "New Feature");
        // Change RAT to PIG
        selectOption("10022_1_pid", "Porcine");
        submit("Next");

        // Assert step 3 - Sub-Tasks
        assertCollapsedTextSequence(new String[] {
                "Step 3 of 4",
                "Select Projects and Issue Types for Sub-Tasks",
                "The table below lists all the sub-tasks that need to be moved to a new project. Please select the appropriate issue type for each of them",
                "The change will affect 3 issues with issue type(s) Sub-task in project(s) Bovine.",
                "The change will affect 2 issues with issue type(s) Sub-task in project(s) Rattus."
        });
        submit("Next");

        // Assert Update Fields for various combinations:
        assertCollapsedTextSequence(new String[] {
                "Step 3 of 4",
                "Update Fields for Target Project 'Canine' - Issue Type 'New Feature'",
                "Security Level"
        });
        // Choose "Level Red" security level
        selectOption("security", "Level Red");
        submit("Next");
        assertCollapsedTextSequence(new String[] {
                "Step 3 of 4",
                "Update Fields for Target Project 'Canine' - Issue Type 'Sub-task'",
                "Security Level",
                "The security level of subtasks is inherited from parents."
        });
        submit("Next");
        // Cannot choose security level as new Project (PIG) has no Security Level scheme.
        assertCollapsedTextSequence(new String[] {
                "Step 3 of 4",
                "Update Fields for Target Project 'Porcine' - Issue Type 'Bug'",
                "Security Level",
                "The value of this field must be changed to be valid in the target project, but you are not able to update this field in the target project. It will be set to the field's default value for the affected issues."
        });
        submit("Next");
        assertCollapsedTextSequence(new String[] {
                "Step 3 of 4",
                "Update Fields for Target Project 'Porcine' - Issue Type 'Sub-task'",
                "Security Level",
                "The value of this field must be changed to be valid in the target project, but you are not able to update this field in the target project. It will be set to the field's default value for the affected issues."
        });
        submit("Next");

        // Now we are finally on the summary screen.
        assertCollapsedTextSequence(new String[] {
                "Step 4 of 4",
                "Confirmation",
                "Below is a summary of all issues that will be moved. Please confirm that the correct changes have been entered. The bullet points below lists the different target projects and issue types the issues will be moved to. Click on a link below to go to that particular group of issues.",

                "Target Project",
                "Canine",
                "Target Issue Type",
                "New Feature",
                "Security Level",
                "Level Red",

                "Target Project",
                "Canine",
                "Target Issue Type",
                "Sub-task",
                "Security Level",
                "Level Red",

                "Target Project",
                "Porcine",
                "Target Issue Type",
                "Bug",
                "Security Level",
                "None",

                "Target Project",
                "Porcine",
                "Target Issue Type",
                "Sub-task",
                "Security Level",
                "None"
        });
        // Confirm change to get back to Issue Navigator
        submit("Next");
        waitAndReloadBulkOperationProgressPage();
        // Now test our Issue table.
        WebTable issueTable = getIssuesTable();
        assertEquals("PIG-11", issueTable.getCellAsText(1, 1).trim());
        assertEquals("", issueTable.getCellAsText(1, 11).trim());
        assertEquals("PIG-10", issueTable.getCellAsText(2, 1).trim());
        assertEquals("", issueTable.getCellAsText(2, 11).trim());
        assertEquals("PIG-9", issueTable.getCellAsText(3, 1).trim());
        assertEquals("", issueTable.getCellAsText(3, 11).trim());
        assertEquals("DOG-12", issueTable.getCellAsText(4, 1).trim());
        assertEquals("Level Red", issueTable.getCellAsText(4, 11).trim());
        assertEquals("DOG-11", issueTable.getCellAsText(5, 1).trim());
        assertEquals("Level Red", issueTable.getCellAsText(5, 11).trim());
        assertEquals("DOG-10", issueTable.getCellAsText(6, 1).trim());
        assertEquals("Level Red", issueTable.getCellAsText(6, 11).trim());
        assertEquals("DOG-9", issueTable.getCellAsText(7, 1).trim());
        assertEquals("Level Red", issueTable.getCellAsText(7, 11).trim());
    }

    /**
     * This test ensures that if a subtask as well as a parent (not the parent of the first subtask) and
     * subtask are selected, then the orphaned subtask will not be able change project (even if the
     * 'Use the above project and issue type pair for all other combinations.' checkbox is ticked)
     *
     * @throws SAXException SAXException
     */
    public void testBulkMoveWithOrphanedSubtasks() throws SAXException
    {
        assertPrecondition();

        //create another parent and subtask issue in the COW project.
        navigation.issue().goToCreateIssueForm(null,null);
        setFormElement("summary", "Another parent");
        submit("Create");
        clickLink("create-subtask");
        assertTextPresent("Create Sub-Task");
        submit("Create");
        assertTextPresent("Create Sub-Task");
        setFormElement("summary", "Orphan");
        submit("Create");

        displayAllIssues();
        // Do a Bulk Operation
        bulkChangeIncludeAllPages();
        //so lets select the 'Orphan' subtask and another subtask and parent (not linked to the 'Orpan')
        checkCheckbox("bulkedit_10051", "on");
        checkCheckbox("bulkedit_10033", "on");
        checkCheckbox("bulkedit_10040", "on");
        submit("Next");
        checkCheckbox("operation", "bulk.move.operation.name");
        submit("Next");

        // Assert step 3 - The orphan subtask should have its own box.
        assertCollapsedTextSequence(new String[] {
                "Step 3 of 4",
                "Select Projects and Issue Types",
                "Please note that 1 sub-task issues were removed from the selection and do not appear in the table below. You are not "
                + "allowed to bulk move sub-task issues together with their parent issue. In this case, you will only "
                + "be asked to move the sub-task if you move the parent issue to a new project.",
                "The change will affect 1 issues with issue type(s) Improvement in project(s) Bovine.",
                //check that the orphaned subtask is shown and displayes the target project.
                "Move",
                "To",
                "Bovine",
                "The change will affect 1 issues with issue type(s) Sub-task in project(s) Bovine."


        });

        // check the 'Use the above project and issue type pair for all other combinations.' This should be ignored by
        // the 'Orphan' subtask.
        checkCheckbox("sameAsBulkEditBean", "10000_4_");
        //lets move to RAT/New Feature
        selectOption("10000_4_pid", "Rattus");
        selectOption("10000_4_issuetype", "New Feature");
        submit("Next");

        // Step 3 (still ;)) - Need to move all the subtasks attached to the bovine parent issue.
        assertCollapsedTextSequence(new String[] {
                "Step 3 of 4",
                "Select Projects and Issue Types for Sub-Tasks",
                "The table below lists all the sub-tasks that need to be moved to a new project. Please select the appropriate issue type for each of them",
                //check that the orphaned subtask is shown and displayes the target project.
                "The change will affect 3 issues with issue type(s) Sub-task in project(s) Bovine.",
                "Move",
                "To",
                "Rattus"
        });
        submit("Next");

        // lets set the security level to kingrat
        assertCollapsedTextSequence(new String[] {
                "Step 3 of 4",
                "Update Fields for Target Project 'Rattus' - Issue Type 'New Feature'"
        });
        selectOption("security", "Level KingRat");
        submit("Next");

        //For subtasks, setting the security level shouldn't be possible.
        assertCollapsedTextSequence(new String[] {
                "Step 3 of 4",
                "Update Fields for Target Project 'Rattus' - Issue Type 'Sub-task'"
        });
        assertTextPresent("The security level of subtasks is inherited from parents.");
        submit("Next");

        //Now the orphaned subtask that remains in project Bovine - it's fields all stay the same.
        assertCollapsedTextSequence(new String[] {
                "Step 3 of 4",
                "Update Fields for Target Project 'Bovine' - Issue Type 'Sub-task'"
        });
        assertTextPresent("All field values will be retained.");
        submit("Next");

        //click next on the confirm screen.
        submit("Next");
        waitAndReloadBulkOperationProgressPage();

        WebTable issueTable = getIssuesTable();
        assertEquals("RAT-11", issueTable.getCellAsText(1, 1).trim());
        assertEquals("Level KingRat", issueTable.getCellAsText(1, 11).trim());
        assertEquals("RAT-10", issueTable.getCellAsText(2, 1).trim());
        assertEquals("Level KingRat", issueTable.getCellAsText(2, 11).trim());
        assertEquals("RAT-9", issueTable.getCellAsText(3, 1).trim());
        assertEquals("Level KingRat", issueTable.getCellAsText(3, 11).trim());
        assertEquals("RAT-8", issueTable.getCellAsText(4, 1).trim());
        assertEquals("Level KingRat", issueTable.getCellAsText(4, 11).trim());
        assertEquals("RAT-7", issueTable.getCellAsText(5, 1).trim());
        assertEquals("Level Mouse", issueTable.getCellAsText(5, 11).trim());
        assertEquals("RAT-6", issueTable.getCellAsText(6, 1).trim());
        assertEquals("Level Mouse", issueTable.getCellAsText(6, 11).trim());
        assertEquals("RAT-5", issueTable.getCellAsText(7, 1).trim());
        assertEquals("Level Mouse", issueTable.getCellAsText(7, 11).trim());
        assertEquals("COW-39", issueTable.getCellAsText(8, 1).trim());
        assertEquals("", issueTable.getCellAsText(8, 11).trim());
        assertEquals("COW-38", issueTable.getCellAsText(9, 1).trim());
        assertEquals("", issueTable.getCellAsText(9, 11).trim());
    }

    public void testBulkWorkflowTransitionIssue()
    {
        assertPrecondition();

        //bulk transition a single parent issue, setting the security level to nonen
        bulkChangeIncludeAllPages();
        assertTextPresent("Step 1 of 4: Choose Issues");
        checkCheckbox("bulkedit_10033", "on");
        submit("Next");
        assertTextPresent("Step 2 of 4: Choose Operation");
        checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        submit("Next");
        assertTextPresent("Step 3 of 4: Operation Details");
        checkCheckbox("wftransition", "Copy of jira_2_6");
        submit("Next");
        assertTextSequence(new String[] { "Workflow", "Copy of jira" });
        assertTextSequence(new String[] { "Selected Transition", "Close Issue" });
        assertTextSequence(new String[] { "Status Transition", "Open", "Closed" });
        //set the resolution to fixed, and the security level to 'None'.
        checkCheckbox("actions", "resolution");
        selectOption("resolution", "Fixed");
        checkCheckbox("actions", "security");
        selectOption("security", "None");
        submit("Next");
        assertTextSequence(new String[] { "Workflow", "Copy of jira" });
        assertTextSequence(new String[] { "Selected Transition", "Close Issue" });
        assertTextSequence(new String[] { "Status Transition", "Open", "Closed" });
        assertTextSequence(new String[] { "This change will affect", "1", "issues" });
        assertTextSequence(new String[] { "Security Level", "None" });
        assertTextSequence(new String[] { "Resolution", "Fixed" });
        assertTextPresent("COW-35");
        submit("Next");
        waitAndReloadBulkOperationProgressPage();

        //check that we are on the issue navigator and that the security level is no longer present.
        assertTextPresent("Issue Navigator");
        assertTextNotPresent("MyFriendsOnly");
        assertTableCellHasText("issuetable", 4, 1, "COW-37");
        assertTableCellHasText("issuetable", 4, 2, "COW-35");
        assertTableCellHasText("issuetable", 4, 2, "Lets get a third milk bucket");
        assertTableCellHasText("issuetable", 4, 6, "Open");
        assertTableCellHasText("issuetable", 5, 1, "COW-36");
        assertTableCellHasText("issuetable", 5, 2, "COW-35");
        assertTableCellHasText("issuetable", 5, 2, "Get another milk bucket");
        assertTableCellHasText("issuetable", 5, 6, "Open");
        assertTableCellHasText("issuetable", 6, 1, "COW-35");
        assertTableCellHasText("issuetable", 6, 2, "No more milk");
        assertTableCellHasText("issuetable", 6, 6, "Closed");
        assertTableCellHasText("issuetable", 7, 1, "COW-34");
        assertTableCellHasText("issuetable", 7, 2, "COW-35");
        assertTableCellHasText("issuetable", 7, 2, "Get new milk bucket");
        assertTableCellHasText("issuetable", 7, 6, "Open");

        //now lets do another transition and change the security level back.  This time select parent and subtask.
        //in the end this should really only do the transition for the parent.
        bulkChangeIncludeAllPages();
        checkCheckbox("bulkedit_10033", "on");
        checkCheckbox("bulkedit_10034", "on");
        submit("Next");
        checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        submit("Next");
        assertTextPresent("Step 3 of 4: Operation Details");
        assertTextSequence(new String[] { "Closed", "Reopened" });
        submit("Next");
        checkCheckbox("wftransition", "Copy of jira_3_4");
        submit("Next");
        assertTextSequence(new String[] { "Workflow", "Copy of jira" });
        assertTextSequence(new String[] { "Selected Transition", "Reopen Issue" });
        assertTextSequence(new String[] { "Status Transition", "Closed", "Reopened" });
        assertTextSequence(new String[] { "This change will affect", "1", "issues" });
        assertTextSequence(new String[] { "Security Level", "MyFriendsOnly" });
        checkCheckbox("actions", "security");
        selectOption("security", "MyFriendsOnly");
        submit("Next");
        assertTextSequence(new String[] { "Workflow", "Copy of jira" });
        assertTextSequence(new String[] { "Selected Transition", "Reopen Issue" });
        assertTextSequence(new String[] { "Status Transition", "Closed", "Reopened" });
        assertTextSequence(new String[] { "This change will affect", "1", "issues" });
        assertTextSequence(new String[] { "COW-35" });
        submit("Next");
        waitAndReloadBulkOperationProgressPage();

        assertTableCellHasText("issuetable", 4, 1, "COW-37");
        assertTableCellHasText("issuetable", 4, 2, "COW-35");
        assertTableCellHasText("issuetable", 4, 2, "Lets get a third milk bucket");
        assertTableCellHasText("issuetable", 4, 6, "Open");
        assertTableCellHasText("issuetable", 4, 11, "MyFriendsOnly");
        assertTableCellHasText("issuetable", 5, 1, "COW-36");
        assertTableCellHasText("issuetable", 5, 2, "COW-35");
        assertTableCellHasText("issuetable", 5, 2, "Get another milk bucket");
        assertTableCellHasText("issuetable", 5, 6, "Open");
        assertTableCellHasText("issuetable", 5, 11, "MyFriendsOnly");
        assertTableCellHasText("issuetable", 6, 1, "COW-35");
        assertTableCellHasText("issuetable", 6, 2, "No more milk");
        assertTableCellHasText("issuetable", 6, 6, "Reopened");
        assertTableCellHasText("issuetable", 6, 11, "MyFriendsOnly");
        assertTableCellHasText("issuetable", 7, 1, "COW-34");
        assertTableCellHasText("issuetable", 7, 2, "COW-35");
        assertTableCellHasText("issuetable", 7, 2, "Get new milk bucket");
        assertTableCellHasText("issuetable", 7, 6, "Open");
        assertTableCellHasText("issuetable", 7, 11, "MyFriendsOnly");
    }

    public void testConvertSingleIssueToSubtask()
    {
        assertPrecondition();

        //convert a subtask to a proper issue
        gotoIssue("COW-34");
        clickLink("subtask-to-issue");
        assertTextPresent("Convert Sub-task to Issue: COW-34");
        selectOption("issuetype", "New Feature");
        submit("Next >>");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextSequence(new String[] { "Type", "Sub-task", "New Feature" });
        submit("Finish");
        assertTextPresent("Get new milk bucket");

        assertTextNotPresent("COW-35");
        assertTextPresent("MyFriendsOnly");

        //let set the security leve to 'None'
        clickLink("edit-issue");
        selectOption("security", "None");
        submit("Update");
        navigation.issue().returnToSearch();
        //ensure that COW-34 is now an issue with no security level.
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
        assertTableCellHasText("issuetable", 7, 2, "Get new milk bucket");
        assertTableCellHasText("issuetable", 7, 11, "");

        //lets convert it back to a subtask of COW-35 and make sure there's a security level.
        gotoIssue("COW-34");
        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", "COW-35");
        submit("Next >>");
        assertTextPresent("Convert Issue to Sub-task: COW-34");
        assertTextPresent("All fields will be updated automatically.");
        submit("Next >>");
        assertTextSequence(new String[] { "Type", "New Feature", "Sub-task" });
        assertTextSequence(new String[] { "Security Level", "None", "MyFriendsOnly" });
        submit("Finish");
        assertTextPresent("COW-35");
        assertTextPresent("MyFriendsOnly");
        navigation.issue().returnToSearch();

        assertPrecondition();
    }

    private void assertPrecondition()
    {
        //first lets check we have an issue with subtasks and they all have a particular security level set.
        displayAllIssues();

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

    private WebTable getIssuesTable()
            throws SAXException
    {
        return getDialog().getResponse().getTableWithID("issuetable");
    }

}
