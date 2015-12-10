package com.atlassian.jira.webtests.ztests.issue.move;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import com.meterware.httpunit.WebTable;

import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.MOVE_ISSUE })
public class TestMoveIssueForEnterprise extends JIRAWebTest
{
    private static final String MOVE_CONFIRM_TABLE = "move_confirm_table";
    private static final int CHANGE_TYPE_INDEX = 0;
    private static final int PREVIOUS_VALUE_INDEX = 1;
    private static final int NEW_VALUE_INDEX = 2;
    private static final String FIELD_STATUS = "Status:";
    private static final String SPAN_TAG_END = "</span>";

    public TestMoveIssueForEnterprise(String name)
    {
        super(name);
    }

    public void tearDown()
    {
        removeWorkflow();
        administration.restoreBlankInstance();
        super.tearDown();
    }

    public void testMoveIssueWithSubtaskFromIssueSecuritySchemeToNoScheme()
    {
        administration.restoreData("TestMoveIssueWithIssueSecurityLevel.xml");

        // Move issue with a subtask to a project that does not have an issue security scheme
        navigation.issue().gotoIssue("HSP-8");
        clickLink("move-issue");
        selectOption("10000_1_pid", "test");
        submit("Next");
        submit("Next");
        submit("Next");
        submit("Next");
        submit("Next");
        waitAndReloadBulkOperationProgressPage();

        // Verify that the issue no longer has a security level set
        assertTextNotPresent("Level 1");

        // Verify that the issues subtask is still visible
        assertTextPresent("subtask 1");
        clickLinkWithText("subtask 1");
        // Verify that the subtask also does not have any security level set
        assertTextNotPresent("Level 1");
    }

    public void testMoveIssueWithSubtaskFromIssueSecuritySchemeToSameScheme()
    {
        administration.restoreData("TestMoveIssueWithIssueSecurityLevel.xml");

        // Move issue with a subtask to a project that does not have an issue security scheme
        navigation.issue().gotoIssue("HSP-8");
        clickLink("move-issue");
        selectOption("10000_1_pid", "SameSchemeProject");
        submit("Next");
        submit("Next");
        submit("Next");
        submit("Next");
        submit("Next");
        waitAndReloadBulkOperationProgressPage();

        // Verify that the issue still has the same issue security level
        assertTextPresent("Level 1");

        // Verify that the issues subtask is still visible
        assertTextPresent("subtask 1");
        clickLinkWithText("subtask 1");
        // Verify that the subtask also has the same issue security level.
        assertTextPresent("Level 1");
    }

    public void testMoveIssueWithSubtaskFromIssueSecuritySchemeToDifferentScheme()
    {
        administration.restoreData("TestMoveIssueWithIssueSecurityLevel.xml");
        // Move issue with a subtask to a project that does not have an issue security scheme
        navigation.issue().gotoIssue("HSP-8");
        clickLink("move-issue");
        selectOption("10000_1_pid", PROJECT_MONKEY);
        submit("Next");
        submit("Next");
        selectOption("security", "Level 2");
        submit("Next");
        submit("Next");
        submit("Next");
        waitAndReloadBulkOperationProgressPage();

        // Verify that the issue has the new issue security level
        assertTextPresent("Level 2");

        // Verify that the issues subtask is still visible
        assertTextPresent("subtask 1");
        clickLinkWithText("subtask 1");
        // Verify that the subtask also has the new issue security level.
        assertTextPresent("Level 2");
    }

    public void testMoveIssueWithSubtaskFromNoSchemeToIssueSecurityScheme()
    {
        administration.restoreData("TestMoveIssueWithIssueSecurityLevel.xml");
           // Move issue with a subtask to a project that does not have an issue security scheme
        navigation.issue().gotoIssue("TST-1");
        clickLink("move-issue");
        selectOption("10010_1_pid", PROJECT_MONKEY);
        submit("Next");
        submit("Next");
        submit("Next");
        submit("Next");
        submit("Next");
        waitAndReloadBulkOperationProgressPage();
        // Verify that the issue still does not have any issue security level set
        assertTextNotPresent("Level 2");

        // Verify that the issues subtask is still visible
        assertTextPresent("subtask 2");
        clickLinkWithText("subtask 2");
        // Verify that the subtask also does not have any issue security level set
        assertTextNotPresent("Level 2");
    }

    public void testMoveIssueWithSubtaskFromNoSchemeToIssueSecuritySchemeRequired()
    {
        administration.restoreData("TestMoveIssueWithIssueSecurityLevel.xml");
           // Move issue with a subtask to a project that does not have an issue security scheme
        navigation.issue().gotoIssue("TST-1");
        clickLink("move-issue");
        selectOption("10010_1_pid", PROJECT_HOMOSAP);
        submit("Next");
        submit("Next");
        selectOption("security", "Level 1");
        submit("Next");
        submit("Next");
        submit("Next");
        waitAndReloadBulkOperationProgressPage();

        // Verify that the issue now has a issue security level set
        assertTextPresent("Level 1");

        // Verify that the issues subtask is still visible
        assertTextPresent("subtask 2");
        clickLinkWithText("subtask 2");
        // Verify that the subtask also now has a issue security level set
        assertTextPresent("Level 1");
    }

    public void testMoveIssueForEnterprise()
    {
        administration.restoreData("blankWithOldDefault.xml");
        createProjectIfAbsent(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
        createProjectIfAbsent(PROJECT_NEO, PROJECT_NEO_KEY, ADMIN_USERNAME);

        moveOperationToSameProjectAndType();
        moveIssueWithWorkflow();
        moveIssueWithMultipleWorkflowInScheme();
    }

    public void moveIssueWithMultipleWorkflowInScheme()
    {
        log("Move Operation: Moving issue to a different issueType with a different workflow that has different status.");

        createWorkflowWithDifferentWorkflows();

        // Create an issue that uses a issue type in the WORKFLOW_COPIED
        String issueKey = addIssue(PROJECT_NEO, PROJECT_NEO_KEY, "Bug", "test moving issue between workflows", "Minor", null, null, null, ADMIN_FULLNAME, "test environment 1", "An issue to move between workflow statuses", null, null, null);
        navigation.issue().gotoIssue(issueKey);

        //change the status to a status thats not in WORKFLOW_ADDED
        clickLinkWithText("Resolve Issue");
        setWorkingForm("issue-workflow-transition");
        submit("Transition");
        assertTextPresent("Resolved");

        //start moving
        clickLink("move-issue");
        selectOption("issuetype", "Improvement"); //set it to an issuetype thats in WORKFLOW_ADDED to not skip step 2
        submit();

        assertTextPresent("Move Issue: Select Status");
        //checks its got the correct statuses by choosing the status that was added in WORKFLOW_ADDED
        selectOption("beanTargetStatusId", STEP_NAME);
        submit();

        assertTextPresent("Move Issue: Update Fields");
        getDialog().setWorkingForm("jiraform");
        submit();

        //check correct changes are going to be made
        assertTextPresent("Move Issue: Confirm");
        getDialog().setWorkingForm("jiraform");
        confirmChangesToBeMade("Bug", "Improvement");
        confirmStatusChangesToBeMade("Resolved", STEP_NAME);
        confirmWorkflowMovement(WORKFLOW_COPIED, WORKFLOW_ADDED);
        submit("Move");

        assertTextPresentBeforeText(FIELD_STATUS, STEP_NAME);
        assertLinkPresentWithText(PROJECT_NEO);

        //reopen the issue to check if the WORKFLOW_ADDED operations are available
        clickLinkWithText(TRANSIION_NAME_REOPEN);
        setWorkingForm("issue-workflow-transition");
        submit("Transition");
        assertTextPresentBeforeText(FIELD_STATUS, "Open");

        //check that the workflow operation 'Approve Issue' is there
        assertLinkPresentWithText(TRANSIION_NAME_APPROVE);
        clickLinkWithText(TRANSIION_NAME_APPROVE);
        assertTextPresentBeforeText(FIELD_STATUS, STEP_NAME);

        navigation.issue().deleteIssue(issueKey);
    }

    private void confirmWorkflowMovement(String oldWorkflow, String newWorkFlow)
    {
        assertTextPresent("Move Issue: Confirm");
        assertTextPresentBeforeText("(" + oldWorkflow + ")" + SPAN_TAG_END, "(" + newWorkFlow + ")" + SPAN_TAG_END);
    }

    private void confirmChangesToBeMade(String oldValue, String newValue)
    {
        assertTextPresent("Move Issue: Confirm");
        assertTextPresentBeforeText(oldValue + SPAN_TAG_END, newValue + SPAN_TAG_END);
    }

    private void confirmStatusChangesToBeMade(String oldValue, String newValue)
    {
        assertTextPresent("Move Issue: Confirm");
        assertTextPresentBeforeText(oldValue, newValue);
    }

    private void createWorkflowWithDifferentWorkflows()
    {
        log("Creating workflow");
        addWorkFlowScheme(WORKFLOW_SCHEME, "Workflow scheme to test move issue");
        administration.workflows().goTo().copyWorkflow("jira", WORKFLOW_COPIED);
        addWorkFlow(WORKFLOW_ADDED, "this workflow has one status Open only");
        gotoWorkFlow();

        addLinkedStatus(STATUS_NAME, "The resolution of this issue has been approved");
        administration.workflows().goTo().workflowSteps(WORKFLOW_ADDED).add(STEP_NAME, STATUS_NAME);
        addTransition(WORKFLOW_ADDED, "Open", TRANSIION_NAME_APPROVE, "", STEP_NAME, null);
        addTransition(WORKFLOW_ADDED, STEP_NAME, TRANSIION_NAME_REOPEN, "", "Open", ASSIGN_FIELD_SCREEN);

        assignWorkflowScheme(10001L, "Bug", WORKFLOW_COPIED);
        assignWorkflowScheme(10001L, "New Feature", WORKFLOW_COPIED);
        assignWorkflowScheme(10001L, "Task", WORKFLOW_COPIED);
        assignWorkflowScheme(10001L, "Improvement", WORKFLOW_ADDED); //this issue type has a different workflow with different statuses

        administration.project().associateWorkflowScheme(PROJECT_NEO, WORKFLOW_SCHEME);
        waitForSuccessfulWorkflowSchemeMigration(PROJECT_NEO, WORKFLOW_SCHEME);
    }

    /**
     * Tests the error handling if the user attempts to move an issue to the same position
     */
    public void moveOperationToSameProjectAndType()
    {
        log("Move Operation: Test the error checking for moving an issue to the same project and issue type");
        String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test move issue", "Trivial", null, null, null, null, "test environment 5", "test description to be moved to another project", null, null, null);
        navigation.issue().gotoIssue(issueKey);

        clickLink("move-issue");
        assertTextPresent("Move Issue");
        setFormElement("issuetype", "1");

        submit();
        assertTextPresent("Step 1 of 4");
        assertTextPresent("You must select a different project or issue type to complete a move operation.");
    }

    public void moveIssueWithWorkflow()
    {
        log("Move Operation: Moving issue to project without the same status.");

        createWorkflow();

        performMoveIssue();

        removeWorkflow();

    }

    private void createProjectIfAbsent(final String name, final String key, final String lead)
    {
        administration.project().addProject(name, key, lead);
    }

    private void removeWorkflow()
    {
        log("Removing workflow");

        createProjectIfAbsent(PROJECT_NEO, PROJECT_NEO_KEY, ADMIN_USERNAME);
        administration.project().associateWorkflowScheme(PROJECT_NEO, "Default");

        deleteWorkFlowScheme("10000");
        deleteWorkFlow(WORKFLOW_COPIED);
        deleteWorkFlow(WORKFLOW_ADDED);
    }

    private void createWorkflow()
    {
        log("Creating workflow");
        addWorkFlowScheme(WORKFLOW_SCHEME, "Workflow scheme to test move issue");

        administration.workflows().goTo().copyWorkflow("jira", WORKFLOW_COPIED);
        addLinkedStatus(STATUS_NAME, "The resolution of this issue has been approved");
        administration.workflows().goTo().workflowSteps(WORKFLOW_COPIED).add(STEP_NAME, STATUS_NAME);
        addTransition(WORKFLOW_COPIED, "Resolved", TRANSIION_NAME_APPROVE, "", STEP_NAME, null);
        addTransition(WORKFLOW_COPIED, STEP_NAME, TRANSIION_NAME_REOPEN, "", "Open", ASSIGN_FIELD_SCREEN);
        addTransition(WORKFLOW_COPIED, STEP_NAME, TRANSIION_NAME_CLOSE, "", "Closed", null);

        assignWorkflowScheme(10000L, "Bug", WORKFLOW_COPIED);
        administration.project().associateWorkflowScheme(PROJECT_NEO, WORKFLOW_SCHEME);
        waitForSuccessfulWorkflowSchemeMigration(PROJECT_NEO, WORKFLOW_SCHEME);
    }

    private void performMoveIssue()
    {
        String issueKey = addIssue(PROJECT_NEO, PROJECT_NEO_KEY, "Bug", "test moving issue between workflows", "Minor", null, null, null, ADMIN_FULLNAME, "test environment 1", "An issue to move between workflow statuses", null, null, null);
        navigation.issue().gotoIssue(issueKey);

        clickLinkWithText("Resolve Issue");
        setWorkingForm("issue-workflow-transition");
        submit("Transition");
        assertTextPresent("Resolved");

        clickLinkWithText(TRANSIION_NAME_APPROVE);

        clickLink("move-issue");
        selectOption("pid", PROJECT_HOMOSAP);
        submit();

        assertTextPresent("Move Issue: Select Status");
        selectOption("beanTargetStatusId", "Open");
        submit();

        assertTextPresent("Move Issue: Update Fields");
        getDialog().setWorkingForm("jiraform");
        submit();

        checkMoveConfirmTable();

        getDialog().setWorkingForm("jiraform");
        submit("Move");

        assertTextPresent("Open");
        assertLinkPresentWithText(PROJECT_HOMOSAP);

        deleteIssue(issueKey);
    }

    private void checkMoveConfirmTable()
    {
        assertTextPresent("Move Issue: Confirm");
        try
        {
            WebTable fieldTable = getDialog().getResponse().getTableWithID(MOVE_CONFIRM_TABLE);
            // First row is a headings row so skip it
            for (int i = 1; i < fieldTable.getRowCount(); i++)
            {
                String field = fieldTable.getCellAsText(i, CHANGE_TYPE_INDEX);
//                log(field);
                if (field.indexOf("Status") > -1)
                {
                    String previousCell = fieldTable.getCellAsText(i, PREVIOUS_VALUE_INDEX);
                    String newCell = fieldTable.getCellAsText(i, NEW_VALUE_INDEX);
                    assertTrue(previousCell.indexOf("Approved") != -1);
                    assertTrue(newCell.indexOf("Open") != -1);
                    return;
                }
            }

            fail("Cannot find field with id '" + "Status" + "'.");
        }
        catch (SAXException e)
        {
            fail("Cannot find table with id '" + MOVE_CONFIRM_TABLE + "'.");
            e.printStackTrace();
        }
    }
}
