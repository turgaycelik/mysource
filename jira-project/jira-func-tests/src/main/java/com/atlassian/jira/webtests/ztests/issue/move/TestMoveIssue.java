package com.atlassian.jira.webtests.ztests.issue.move;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

import org.xml.sax.SAXException;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.MOVE_ISSUE })
public class TestMoveIssue extends JIRAWebTest
{
    private static final String ISSUE_KEY_TO_MOVE = "HSP-1";
    private static final String ISSUE_KEY_NORMAL = "HSP-2";
    private static final String ANA_3 = "ANA-3";
    private static final String CURRENT_ISSUE_TYPE = "Current Issue Type";
    private static final String ANOTHER_TEST_PROJECT = "another test";
    private static final String NEW_ISSUE_TYPE = "New Issue Type";
    private static final String MORE_TESTS_ISSUE_TYPE = "more tests";
    private static final String ISSUETYPE_REQUEST_PARAM = "issuetype";
    private static final String PID_REQUEST_PARAM = "pid";
    private static final String JIRAFORM = "jiraform";
    private static final String MOVE_SUBMIT = "Move";
    private static final String RESULTING_ISSUE_TST_3 = "TST-3";
    private static final String TEST_PROJECT = "Test";
    private static final String MOVE_ISSUE_LINK = "move-issue";

    public TestMoveIssue(String name)
    {
        super(name);
    }

    public void testMoveIssue()
    {
        restoreData("TestMoveIssue.xml");

        moveOperationFunctionality(ISSUE_KEY_TO_MOVE);
        moveOperationWithMoveIssuesPermission(ISSUE_KEY_NORMAL);
        moveOperationWithInvalidDueDate(ISSUE_KEY_NORMAL);
        moveOperationWithDueDateRequired(ISSUE_KEY_NORMAL);
        moveOperationWithRequiredFields(ISSUE_KEY_NORMAL);
        deleteIssue(ISSUE_KEY_NORMAL);
        deleteIssue(ISSUE_KEY_TO_MOVE);
    }

    public void testMoveIssueIssueTypeAvailable()
    {
        log("Move Operation: Test the visibility of the Issue Type field on move.");
        restoreData("TestMoveIssueIssueTypeAvailable.xml");
        String projectId = getProjectId(TEST_PROJECT);

        //check that the issue has not been moved yet and the unmoved issue is indexed correctly.
        assertIndexedFieldCorrect("//item", EasyMap.build("key", ANA_3, "type", "another test"), EasyMap.build("key", "TST-3", "title", "[TST-3] Test"), ANA_3);

        gotoIssue(ANA_3);
        clickLink(MOVE_ISSUE_LINK);

        // Assert that the issue type is prompted on the move issue screen for prof and ent
        assertTextPresent(CURRENT_ISSUE_TYPE);
        assertTextPresent(ANOTHER_TEST_PROJECT);
        assertTextPresent(NEW_ISSUE_TYPE);

        selectOption(ISSUETYPE_REQUEST_PARAM, MORE_TESTS_ISSUE_TYPE);
        setFormElement(PID_REQUEST_PARAM, projectId);
        submit();

        getDialog().setWorkingForm(JIRAFORM);
        submit();

        // Assert that the issue type change values are shown on the move issue screen for prof and ent
        assertTextPresent(ANOTHER_TEST_PROJECT);
        assertTextPresent(MORE_TESTS_ISSUE_TYPE);
        submit(MOVE_SUBMIT);

        // Make sure the move worked.
        assertTextPresent(RESULTING_ISSUE_TST_3);

        //issue type should have been changed
        assertIndexedFieldCorrect("//item", EasyMap.build("key", RESULTING_ISSUE_TST_3, "type", MORE_TESTS_ISSUE_TYPE, "priority", "Major"), null, RESULTING_ISSUE_TST_3);
    }

    // this test only runs on enterprise edition since it tests component assignee's
    public void testMoveIssueAutomaticAssigneeWithComponents()
    {
        try
        {
            restoreData("TestMoveIssueAutomaticAssigneeWithComponents.xml");
            getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");

            String issueKeyToMove = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test move issue", "Trivial", new String[] { COMPONENT_NAME_ONE }, null, null, "user1", "test environment 5", "test description to be moved to another project", null, null, null);

            //index should not have moved the issue yet- there should be no component yet
            assertIndexedFieldCorrect("//item", EasyMap.build("key", "HSP-1"), EasyMap.build("key", "NDT-1", "component", "New Component 2"), issueKeyToMove);

            String projectId = getProjectId(PROJECT_NEO);

            // move the issue from project homosap to project_neo
            gotoIssue(issueKeyToMove);
            clickLink(MOVE_ISSUE_LINK);
            setFormElement(PID_REQUEST_PARAM, projectId);
            submit("Next >>");
            selectOption("components", COMPONENT_NAME_TWO);
            submit("Next >>");
            submit(MOVE_SUBMIT);
            assertTextPresent("user2");

            //index should have updated the issue- specifically, check that the component is being indexed correctly after a move
            assertIndexedFieldCorrect("//item", EasyMap.build("key", "NDT-1", "component", "New Component 2"), EasyMap.build("key", "HSP-1"), "NDT-1");
        }
        finally
        {
            deleteProject(PROJECT_HOMOSAP);
            deleteProject(PROJECT_NEO);
            deletePermissionScheme("test move perm scheme");
            removeGroupPermission("Default Permission Scheme", ASSIGNABLE_USER, Groups.USERS);
            deleteUser("user1");
            deleteUser("user2");
            getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        }
    }

    /** Tests if the move operation operates correctly */
    public void moveOperationFunctionality(String issueKey)
    {

        log("Move Operation: Test the functionality of the 'Move' operation");
        String projectId = getProjectId(PROJECT_NEO);
        gotoIssue(issueKey);
        clickLink(MOVE_ISSUE_LINK);
        setFormElement(PID_REQUEST_PARAM, projectId);
        submit();
        getDialog().setWorkingForm(JIRAFORM);
        submit();
        assertTextPresent("New Value (after move)");
        submit(MOVE_SUBMIT);
        assertTextNotPresent(PROJECT_HOMOSAP);
        assertTextPresent(PROJECT_NEO);
    }

    /** Tests if the 'Move' Link is available with the 'Move Issues' permission removed */
    public void moveOperationWithMoveIssuesPermission(String issueKey)
    {
        log("Move Operation: Test the availability of the 'Move' Link with 'Move Issues' Permission.");
        removeGroupPermission(MOVE_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        assertLinkNotPresentWithText(MOVE_SUBMIT);

        //Restore permission
        grantGroupPermission(MOVE_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        assertLinkPresentWithText(MOVE_SUBMIT);
    }

    /** Tests the error handling if an invalid 'Due Date' is selected. */
    public void moveOperationWithInvalidDueDate(String issueKey)
    {
        // Set 'Due Date to be required
        setDueDateToRequried();

        log("Move Operation: selecting invalid due date");
        String projectId = getProjectId(PROJECT_MONKEY);

        gotoIssue(issueKey);
        clickLink(MOVE_ISSUE_LINK);
        assertTextPresent("Move Issue");

        setFormElement(PID_REQUEST_PARAM, projectId);
        submit();

        setFormElement("duedate", "stuff");
        submit();

        assertTextPresent("Step 3 of 4");
        assertTextPresent("You did not enter a valid date. Please enter the date in the format &quot;d/MMM/yy&quot;");

        // reset fields to optional
        resetFields();
    }

    /** Tests if the 'Due Date' field is available with 'Due Date' set as required */
    public void moveOperationWithDueDateRequired(String issueKey)
    {
        // Set 'Due Date to be required
        setDueDateToRequried();
        log("Move Operation: testing the availabilty of the 'Due Date' field with 'Due Date' required");
        String projectId = getProjectId(PROJECT_MONKEY);

        gotoIssue(issueKey);
        clickLink(MOVE_ISSUE_LINK);
        assertTextPresent("Move Issue");

        setFormElement(PID_REQUEST_PARAM, projectId);

        submit();
        assertFormElementPresent("duedate");

        // reset fields to optional
        resetFields();
        gotoIssue(issueKey);
        clickLink(MOVE_ISSUE_LINK);
        assertTextPresent("Move Issue");

        setFormElement(PID_REQUEST_PARAM, projectId);

        submit();
        assertFormElementNotPresent("duedate");
    }

    /**
     * Tests the error handling if Components, Affects Versions and Fix Verions are set as required
     * and a project without these is selected
     */
    public void moveOperationWithRequiredFields(String issueKey)
    {
        // Set fields to be required
        setRequiredFields();

        log("Move Operation: Moving issue with required fields.");
        String projectId = getProjectId(PROJECT_NEO);

        gotoIssue(issueKey);

        clickLink(MOVE_ISSUE_LINK);
        assertTextPresent("Move Issue");

        setFormElement(PID_REQUEST_PARAM, projectId);
        submit();

        assertTextPresent("Step 3 of 4");

        setWorkingForm(JIRAFORM);
        submit();
        assertErrorMsgFieldRequired(COMPONENTS_FIELD_ID, PROJECT_NEO, "components");
        assertErrorMsgFieldRequired(FIX_VERSIONS_FIELD_ID, PROJECT_NEO, "versions");
        assertErrorMsgFieldRequired(AFFECTS_VERSIONS_FIELD_ID, PROJECT_NEO, "versions");

        // Reset fields to be optional
        resetFields();
    }

    /** Test mapping of workflows schemes that don't match when moving (enterprise only) */
    public void testMoveWithMappingStatus()
    {
        restoreData("TestBulkMoveMapWorkflows.xml");
        assertIndexedFieldCorrect("//item", EasyMap.build("key", "HSP-13", "summary", "bugs3", "status", "Totally Open"), null, "HSP-13");

        gotoIssue("HSP-13");
        assertTextPresent("Totally Open");
        clickLinkWithText("Move");
        selectOption("pid", "monkey"); //move it to monkey
        moveIssue();
        String movedIssueKey = getIssueKeyWithSummary("bugs3", "MKY");
        gotoIssue(movedIssueKey);
        assertTextNotPresent("Totally Open");
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Open", "summary", "bugs3", "key", movedIssueKey),
                EasyMap.build("status", "Totally Open", "key", "HSP-13"), movedIssueKey);
    }

    public void testMoveIssueWithinProject()
    {
        restoreData("TestMoveIssue.xml");
        //make sure moving within a project runs smoothly
        gotoIssue("HSP-1");
        clickLink("move-issue");
        selectOption("issuetype", "New Feature");
        submit("Next >>");
        submit("Next >>");
        submit("Move");

        //make sure that when a new issue is created, the key isn't unnecessarily incremented (JSP-12195)
        navigation.issue().goToCreateIssueForm(null,null);
        setFormElement("summary", "Test Bug 2");
        submit("Create");
        //New issue key is HSP-3 (after HSP-2 which is already present)
        assertLinkPresentWithText("HSP-3");
    }

    public void testMoveIssueWithSubtasksBetweenProjects()
    {
        restoreData("TestMoveIssueWithSubtasks.xml");

        //make sure moving an issue with subtasks between projects runs smoothly
        gotoIssue("HSP-3");
        clickLink("move-issue");
        tester.selectOption("10000_1_pid", "neanderthal");
        tester.selectOption("10000_1_issuetype", "Task");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();

        //make sure issue keys were generated correctly for issue & subtasks
        assertLinkPresentWithText("NDT-1");
        clickLinkWithText("Sub-task 1");
        assertLinkPresentWithText("NDT-2");
        assertLastChangeHistoryIs("NDT-2", "Key", "HSP-4", "NDT-2");
        assertLastChangeHistoryIs("NDT-2", "Project", "homosapien", "neanderthal");
        assertLastChangeNotMadeToField("NDT-2", "Workflow");
        assertLastChangeNotMadeToField("NDT-2", "Status");
        assertLastChangeNotMadeToField("NDT-2", "Issue Type");
        assertLastChangeNotMadeToField("NDT-2", "Assignee");
        clickLink("parent_issue_summary");
        clickLinkWithText("Sub-task 2");
        assertLinkPresentWithText("NDT-3");
        assertLastChangeHistoryIs("NDT-2", "Project", "homosapien", "neanderthal");
        assertLastChangeNotMadeToField("NDT-3", "Workflow");
        assertLastChangeNotMadeToField("NDT-3", "Status");
        assertLastChangeNotMadeToField("NDT-3", "Issue Type");
        assertLastChangeNotMadeToField("NDT-3", "Assignee");

        //make sure issue key counter was not incremented unnecessarily for issue's old project (JSP-12195)
        navigation.issue().goToCreateIssueForm("homosapien",null);
        setFormElement("summary", "Test Bug 3");
        submit("Create");
        assertLinkPresentWithText("HSP-6");

        //make sure issue key counter was not incremented unnecessarily for issue's new project (JSP-12195)
        navigation.issue().goToCreateIssueForm("neanderthal",null);
        setFormElement("summary", "Test Bug 4");
        submit("Create");
        assertLinkPresentWithText("NDT-4");
    }

    public void testMoveSubtaskWithDifferentWorkflowAndStatuses()
    {
        // same Issue Type, different Workflows, different statuses
        restoreData("jra-14416-workflows.xml");
        gotoIssue("AA-1");

        tester.clickLink("move-issue");
        tester.selectOption("10000_1_pid", "B");
        tester.selectOption("10000_1_issuetype", "Bug");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");

        assertTextPresent("Map Status for Target Project 'B'");
        assertTextPresent("Step 1 of 2");
        tester.selectOption("3", "Three");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();

        gotoIssue("BB-2");
        assertTextPresent("Three"); // check for correct new status
        assertTextPresent("Go To Fourth Step"); // ensure we moved to correct workflow

        assertLastChangeHistoryIs("BB-2", "Key", "AA-2", "BB-2");
        assertLastChangeHistoryIs("BB-2", "Project", "A", "B");
        assertLastChangeHistoryIs("BB-2", "Workflow", "classic default workflow", "B");
        assertLastChangeHistoryIs("BB-2", "Status", "In Progress", "Three");
        assertLastChangeNotMadeToField("BB-2", "Issue Type");
        assertLastChangeNotMadeToField("BB-2", "Assignee");
    }


    public void testMoveSubtaskWithDifferentWorkflows()
    {
        // same Issue Type, different Workflows, same statuses
        restoreData("jra-14416-statuses.xml");
        gotoIssue("AA-1");

        tester.clickLink("move-issue");
        tester.selectOption("10000_1_pid", "B");
        tester.selectOption("10000_1_issuetype", "Bug");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();

        gotoIssue("BB-2");
        assertTextPresent("Mark As Complete"); // check that we're on the correct new workflow
        assertTextPresent("Sub-task");
        assertTextPresent("In Progress");

        assertLastChangeHistoryIs("BB-2", "Key", "AA-2", "BB-2");
        assertLastChangeHistoryIs("BB-2", "Project", "A", "B");
        assertLastChangeHistoryIs("BB-2", "Workflow", "jira", "B");
        assertLastChangeNotMadeToField("BB-2", "Status");
        assertLastChangeNotMadeToField("BB-2", "Issue Type");
        assertLastChangeNotMadeToField("BB-2", "Assignee");
    }

    // the difference between this test and "testMoveIssueWithSubtasksBetweenProjects" is that this one
    // uses projects that are very different from one another. different issue types, different workflows, different
    // statuses. This means that, in order to "move subtasks" properly we need to migrate all of those things for the
    // subtasks.
    // This comes from JRA-14416
    public void testMoveIssueWithSubtaskIssueTypesBetweenProjects()
    {
        restoreData("jra-14416.xml");
        gotoIssue("AL-1");
        tester.clickLink("move-issue");
        tester.selectOption("10000_7_pid", "Baloney");
        tester.selectOption("10000_7_issuetype", "Issue Type Bentley");
        tester.submit("Next");

        assertTextPresent("Select Projects and Issue Types for Sub-Tasks");

        // Assert the table 'issuetypechoices'
        text.assertTextPresent("Subtask Apple");
        text.assertTextPresent("Subtask Asterisk");
        tester.selectOption("10000_6_10001_issuetype", "Subtask Bacon");
        tester.selectOption("10000_9_10001_issuetype", "Subtask Butter");
        tester.submit("Next");
        tester.submit("Next");

        try
        {
            getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
            assertTextPresent("Workflow Brown"); // assert that the correct destination workflow is being displayed
            tester.selectOption("10000", "B-Status-2");
            tester.submit("Next");
            tester.selectOption("assignee", "User B");
            tester.submit("Next");
            tester.submit("Next");
            tester.submit("Next");
            waitAndReloadBulkOperationProgressPage();
        }
        finally
        {
            getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        }

        gotoIssue("BA-2");
        assertTextPresent("Subtask Bacon");
        assertTextPresent("Goto Hajime"); // ensure we're on the correct workflow
        assertLastChangeHistoryIs("BA-2", "Key", "AL-2", "BA-2");
        assertLastChangeHistoryIs("BA-2", "Issue Type", "Subtask Apple", "Subtask Bacon");
        assertLastChangeHistoryIs("BA-2", "Project", "Alabaster", "Baloney");
        assertLastChangeHistoryIs("BA-2", "Fix Version/s", "1.5", "");
        assertLastChangeHistoryIs("BA-2", "Affects Version/s", "1.5", "");
        assertLastChangeHistoryIs("BA-2", "Assignee", "User A", "User B");
        assertLastChangeHistoryIs("BA-2", "Workflow", "Workflow Astaire", "Workflow Brown");
        assertLastChangeHistoryIs("BA-2", "Status", "A-Status-1", "B-Status-2");

        gotoIssue("BA-3");
        assertTextPresent("Subtask Butter");
        assertLastChangeHistoryIs("BA-3", "Key", "AL-3", "BA-3");
        assertLastChangeHistoryIs("BA-3", "Issue Type", "Subtask Asterisk", "Subtask Butter");
        assertLastChangeHistoryIs("BA-2", "Project", "Alabaster", "Baloney");

    }

    public void testMoveIssueWithSubtasksBetweenProjectsWithSecurityLevel() throws SAXException
    {
        //this data is good enough for this test.
        restoreData("TestReindexingSubtasks.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        //lets try moving to a project with no security level. Shouldn't ask for sec level to change
        //goto parent issue. Currently has Level Mouse assigned.
        gotoIssue("RAT-5");
        assertTextPresentAfterText("Level Mouse", "Security Level");

        //lets move it to the PIG project (no security level.
        clickLink("move-issue");
        selectOption("10022_1_pid", "Porcine");
        selectOption("10022_1_issuetype", "Bug");

        submit("Next");
        submit("Next");
        assertTextPresent("The value of this field must be changed to be valid in the target project");
        submit("Next");
        submit("Next");
        assertCollapsedTextSequence(new String[] { "Target Project", "Porcine",
                                                   "Target Issue Type", "Bug",
                                                   "Security Level", "None" });
        submit("Next");
        waitAndReloadBulkOperationProgressPage();
        assertTextPresent("Porcine");
        assertTextNotPresent("Rattus");
        assertTextNotPresent("RAT-");
        assertTextNotPresent("Level Mouse");
        //check the sub-tasks sec level
        gotoIssue("PIG-11");
        assertTextNotPresent("Security Level");

        //now lets go to the issue navigator, and make sure there's no RAT issues and no security leve set to 'Level Mouse'
        displayAllIssues();
        assertTextNotPresent("RAT-");
        assertTextNotPresent("Level Mouse");
        assertTableCellHasText("issuetable", 1, 1, "PIG-11");
        assertTableCellHasText("issuetable", 1, 11, "");
        assertTableCellHasText("issuetable", 2, 1, "PIG-10");
        assertTableCellHasText("issuetable", 2, 11, "");
        assertTableCellHasText("issuetable", 3, 1, "PIG-9");
        assertTableCellHasText("issuetable", 3, 11, "");

        //then lets try going to a project with a security level.
        gotoIssue("PIG-9");
        //lets move the issue to the DOG project
        clickLink("move-issue");
        selectOption("10021_1_pid", "Canine");
        selectOption("10021_1_issuetype", "Bug");
        submit("Next");
        submit("Next");
        //need to select a security level now.
        selectOption("security", "Level Green");
        submit("Next");
        submit("Next");
        assertCollapsedTextSequence(new String[] { "Target Project", "Canine",
                                                   "Target Issue Type", "Bug",
                                                   "Security Level", "Level Green" });
        submit("Next");
        waitAndReloadBulkOperationProgressPage();
        assertTextNotPresent("PIG");
        assertTextNotPresent("Porcine");
        assertTextPresent("Canine");
        assertTextPresentAfterText("Level Green", "Security Level:");
        //check the sub-tasks sec level
        gotoIssue("DOG-11");
        assertTextPresentAfterText("Level Green", "Security Level:");

        //check the issue navigator now. There should be no PIG issues and all DOG issues should have Level Green set
        displayAllIssues();
        assertTextNotInTable("issuetable", "PIG");
        assertTableCellHasText("issuetable", 1, 1, "DOG-11");
        assertTableCellHasText("issuetable", 1, 11, "Level Green");
        assertTableCellHasText("issuetable", 2, 1, "DOG-10");
        assertTableCellHasText("issuetable", 2, 11, "Level Green");
        assertTableCellHasText("issuetable", 3, 1, "DOG-9");
        assertTableCellHasText("issuetable", 3, 11, "Level Green");
    }

    //JRA-16007
    public void testMoveIssueWithoutVersionPermission() throws SAXException
    {
        restoreData("TestMoveIssueWithoutVersionPermission.xml");

        gotoIssue("HSP-1");

        //move the issue to the 'monkey' project (which will be selected by default)
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");
        tester.selectOption("pid", "monkey");
        tester.submit("Next >>");
        tester.submit("Next >>");

        //check the confirm screen shows that the versions will be set to nothing.
        int lastRow = getDialog().getWebTableBySummaryOrId("move_confirm_table").getRowCount() - 1;
        assertTableCellHasText("move_confirm_table", 1, 1, "homosapien");
        assertTableCellHasText("move_confirm_table", 1, 2, "monkey");
        assertTableCellHasText("move_confirm_table", lastRow, 1, "New Version 1");
        assertTableCellHasText("move_confirm_table", lastRow, 1, "New Version 4");
        assertTableCellHasNotText("move_confirm_table", lastRow, 2, "New Version 1");
        assertTableCellHasNotText("move_confirm_table", lastRow, 2, "New Version 4");

        tester.submit("Move");

        //check the moved issue doesn't show the new versions.
        assertTextPresent("MKY-1");
        assertTextPresent("Test issue 1");
        assertTextNotPresent("New Version 1");
        assertTextNotPresent("New Version 4");
    }

    //JRADEV-3273
    public void testMoveIssueWithRequiredLabels()
    {
        administration.restoreData("TestMoveLabels.xml");

        //Try moving an issue w/o labels. We should get prompted to updated
        //labels.  Validation should fail if there's no labels.
        getNavigation().issue().viewIssue("HSP-10");
        tester.clickLink("move-issue");
        tester.selectOption("pid", "monkey");
        tester.submit("Next >>");

        assertTextPresent("Move Issue: Update Fields");
        assertTextPresent("Labels");
        tester.submit("Next >>");

        assertTextPresent("Labels is required");

        //now try moving an issue that's already got some labels. Screen should not come up
        getNavigation().issue().viewIssue("HSP-9");
        tester.clickLink("move-issue");
        tester.selectOption("pid", "monkey");
        tester.submit("Next >>");

        assertTextPresent("Move Issue: Update Fields");
        // JRADEV-7741 - not sure why this would start failing... just getting build passing
        assertTextPresent("All fields will be updated automatically");
        tester.submit("Next >>");
        tester.submit("Move");

        tester.assertTextPresent("MKY-8");

        //now also try moving one with subtasks as well
        getNavigation().issue().viewIssue("HSP-8");
        tester.clickLink("move-issue");
        tester.selectOption("10000_1_pid", "monkey");
        tester.submit("Next");
        tester.submit("Next");

        assertTextPresent("Update Fields");
        assertTextPresent("All field values will be retained");
        tester.submit("Next");
        tester.submit("Next");

        assertTextPresent("Labels is required");

    }

    public void testMoveIssueWithSubtasksAndComponents()
    {
        administration.restoreData("JRA-17312.xml");

        getNavigation().issue().viewIssue("TWO-1");
        assertTextPresent("Date Custom Field:");
        tester.clickLink("move-issue");
        selectOption("10001_1_pid", "ONE");
        selectOption("10001_1_issuetype", "Bug");
        tester.submit("Next");
        tester.submit("Next");

        selectOption("components_10001", "comp-one");
        tester.submit("Next");
        selectOption("components_10001", "comp-one");
        tester.submit("Next");
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();

        getNavigation().issue().viewIssue("ONE-3");
        assertTextNotPresent("Date Custom Field:");
        assertTextSequence(new String [] {"Component/s", "comp-one"});
        assertTextPresent("Due:");

        getNavigation().issue().viewIssue("ONE-3");
        assertTextSequence(new String [] {"Component/s", "comp-one"});
        assertTextPresent("Due:");
    }

    public void testMoveIssueWithRequiredCustomFields()
    {
        administration.restoreData("JRA-12479.xml");

        getNavigation().issue().viewIssue("ONE-1");
        tester.clickLink("move-issue");
        tester.selectOption("pid", "TWO");
        tester.submit("Next >>");

        tester.setFormElement("customfield_10100", "Hello World!");
        tester.submit("Next >>");

        assertTextSequence(new String [] {"More Summary CF", "Hello World!"});
    }
    private void moveIssue()
    {
        submit("Next >>");
        submit("Next >>");
        submit("Next >>");
        submit("Move");
    }
}
