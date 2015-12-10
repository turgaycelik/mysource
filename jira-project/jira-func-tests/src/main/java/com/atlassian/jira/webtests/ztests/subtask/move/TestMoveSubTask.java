package com.atlassian.jira.webtests.ztests.subtask.move;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestMoveSubTask extends JIRAWebTest
{
    private String parentIssue1;
    private String parentIssue2;
    private String parentIssue3;
    private String subtask;
    private String subtask2;
    private String subtask3;
    private static final String NEW_SUBTASK_TYPE = "newSubTaskType";
    private static final String STEP_ONE_TITLE = "Choose Operation";
    private static final String STEP_TWO_TITLE = "Operation Details";
    private static final String STEP_THREE_TITLE = "Update Fields";
    private static final String STEP_FOUR_TITLE = "Confirmation";
    private static final String SUBTASK_SUMMARY = "this subtask is moved";

    public TestMoveSubTask(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        getAdministration().restoreBlankInstance();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
        activateSubTasks();
    }

    public void tearDown()
    {
        deactivateSubTasks();
        getAdministration().restoreBlankInstance();
        super.tearDown();
    }

    public void testMoveSubTask()
    {
//        subTaskMoveSubTask();
//        subTaskMoveIssueWithSubTask();

        parentIssue1 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY,  "Bug", "control parent issue in project homosap", "Major", null, null, null, ADMIN_FULLNAME, "environment", "description", null, null, null);
        subtask = addSubTaskToIssue(parentIssue1, SUB_TASK_DEFAULT_TYPE, SUBTASK_SUMMARY, "description: Test subject");
        _testMoveSubTaskOperationListVisibility();
        _testSidePanelLinksForMoveSubTaskType();
//        _testWizardCancelForMoveSubTaskType(); //todo - test cant find any button with value: Cancel
        _testSuccessfulMoveOfSubTaskType();

        parentIssue2 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY,  "Bug", "control parent issue 2 in project homosap", "Major", null, null, null, ADMIN_FULLNAME, "environment", "description", null, null, null);
        parentIssue3 = addIssue(PROJECT_MONKEY, PROJECT_MONKEY_KEY,  "Bug", "control parent issue 3 in project monkey", "Major", null, null, null, ADMIN_FULLNAME, "environment", "description", null, null, null);
        subtask2 = addSubTaskToIssue(parentIssue1, SUB_TASK_DEFAULT_TYPE, "control subtask of parent 1 (homosap)", "description");
        subtask3 = addSubTaskToIssue(parentIssue3, SUB_TASK_DEFAULT_TYPE, "control subtask of parent 3 (monkey)", "description");
        _testSidePanelLinksForMoveSubTaskParent();
//        _testWizardCancelForMoveSubTaskParent();//todo - test cant find any button with value: Cancel
        _testValidateMoveSubTaskParent();
        _testSuccessfulMoveOfSubTaskParent();
        deleteSubTaskType(NEW_SUBTASK_TYPE);
    }

    private void _testMoveSubTaskOperationListVisibility()
    {
        //test no permissions
        log("Check move_issue link is hidden with no permission");
        removeGroupPermission(MOVE_ISSUE, Groups.DEVELOPERS);
        gotoIssue(subtask);
        assertLinkNotPresent("move-issue");
        grantGroupPermission(MOVE_ISSUE, Groups.DEVELOPERS);

        log("Check move subtask type operation cannot be perfomed");
        gotoIssue(subtask);
        clickLink("move-issue");
        //test operations canPerform()
        //initially theres is only one subtask - hence only move subtask type should be disabled
        assertRadioOptionNotPresent("operation", "move.subtask.type.operation.name");
        assertRadioOptionPresent("operation", "move.subtask.parent.operation.name");

        log("Check move subtask type operation can be performed");
        addNewSubTaskType();
        gotoIssue(subtask);
        clickLink("move-issue");
        assertRadioOptionPresent("operation", "move.subtask.type.operation.name");
        assertRadioOptionPresent("operation", "move.subtask.parent.operation.name");
    }

    private void _testSuccessfulMoveOfSubTaskType()
    {
        //make a successful change of issue type
        addNewSubTaskType();
        gotoStep1ofMoveSubTaskType();
        assertFormElementPresent("issuetype");
        selectOption("issuetype", NEW_SUBTASK_TYPE);
        submit("Next >>");
        submit("Next >>");
        assertTextPresentBeforeText("Type", SUB_TASK_DEFAULT_TYPE);
        assertTextPresentBeforeText(SUB_TASK_DEFAULT_TYPE, NEW_SUBTASK_TYPE);
        submit("Move");
        assertLastChangeHistoryIs(subtask, "Issue Type", SUB_TASK_DEFAULT_TYPE, NEW_SUBTASK_TYPE);
    }

    private void addNewSubTaskType()
    {
        administration.subtasks().addSubTaskType(NEW_SUBTASK_TYPE, "temporary subtask type");
    }

    private void _testSidePanelLinksForMoveSubTaskType()
    {
        //in step 2, go back to step 1
        gotoStep2ofMoveSubTaskType();
        assertTextPresent("Step 1 of 4");

        //in step 3, go back to step 1
        gotoStep3ofMoveSubTaskType();
        clickLinkWithText(STEP_ONE_TITLE);
        assertTextPresent("Step 1 of 4");

        //in step 3, go back to step 2
        gotoStep3ofMoveSubTaskType();
        clickLinkWithText(STEP_TWO_TITLE);
        assertTextPresent("Step 2 of 4");

        //in step 4, go back to step 1
        gotoStep4ofMoveSubTaskType();
        clickLinkWithText(STEP_ONE_TITLE);
        assertTextPresent("Step 1 of 4");

        //in step 4, go back to step 2
        gotoStep4ofMoveSubTaskType();
        clickLinkWithText(STEP_TWO_TITLE);
        assertTextPresent("Step 2 of 4");

        //in step 4, go back to step 3
        gotoStep4ofMoveSubTaskType();
        clickLinkWithText(STEP_THREE_TITLE);
        assertTextPresent("Step 3 of 4");
    }

    /**
     * this is a failing test, since it cannot click the 'Cancel' button
     */
    private void _testWizardCancelForMoveSubTaskType()
    {
        gotoMoveSubTaskChooseOperation();
        checkCancelRedirectsViewIssue();

        gotoStep1ofMoveSubTaskType();
        checkCancelRedirectsViewIssue();

        gotoStep2ofMoveSubTaskType();
        checkCancelRedirectsViewIssue();

        gotoStep3ofMoveSubTaskType();
        checkCancelRedirectsViewIssue();

        gotoStep4ofMoveSubTaskType();
        checkCancelRedirectsViewIssue();
    }

    private void checkCancelRedirectsViewIssue()
    {
        assertFormElementPresent("Cancel");
        clickAnyButtonWithValue("Cancel");
        assertTextNotPresent("Step ");
        assertLinkPresentWithText(subtask);
        assertLinkPresentWithText("move-issue");
    }

    private void gotoMoveSubTaskChooseOperation()
    {
        gotoIssue(subtask);
        clickLink("move-issue");
        assertTextPresent("Step 1 of 4");
    }

    private void gotoStep1ofMoveSubTaskType()
    {
        gotoMoveSubTaskChooseOperation();
        checkCheckbox("operation", "move.subtask.type.operation.name");
        submit("Next >>");
    }

    private void gotoStep2ofMoveSubTaskType()
    {
        gotoStep1ofMoveSubTaskType();
        assertLinkPresentWithText(STEP_ONE_TITLE);
        assertLinkNotPresentWithText(STEP_TWO_TITLE);
        assertLinkNotPresentWithText(STEP_THREE_TITLE);
        assertLinkNotPresentWithText(STEP_FOUR_TITLE);
        clickLinkWithText(STEP_ONE_TITLE);
    }

    private void gotoStep3ofMoveSubTaskType()
    {
        gotoStep1ofMoveSubTaskType();
        submit("Next >>");
        assertLinkPresentWithText(STEP_ONE_TITLE);
        assertLinkPresentWithText(STEP_TWO_TITLE);
        assertLinkNotPresentWithText(STEP_THREE_TITLE);
        assertLinkNotPresentWithText(STEP_FOUR_TITLE);
    }

    private void gotoStep4ofMoveSubTaskType()
    {
        gotoStep1ofMoveSubTaskType();
        submit("Next >>");
        submit("Next >>");
        assertLinkPresentWithText(STEP_ONE_TITLE);
        assertLinkPresentWithText(STEP_TWO_TITLE);
        assertLinkPresentWithText(STEP_THREE_TITLE);
        assertLinkNotPresentWithText(STEP_FOUR_TITLE);
    }

    private void gotoStep4ofMoveSubTaskParent()
    {
        gotoMoveSubTaskChooseOperation();
        checkCheckbox("operation", "move.subtask.parent.operation.name");
        submit("Next >>");
    }

    private void _testValidateMoveSubTaskParent()
    {
        gotoStep4ofMoveSubTaskParent();

        setFormElement("parentIssue", ""); //null input
        submit("Change Parent");
        assertTextPresent("Parent Issue is a required field");

        setFormElement("parentIssue", "dontExist"); //no such issue
        submit("Change Parent");
        assertTextPresent("The issue key &quot;dontExist&quot; does not exist");

        setFormElement("parentIssue", subtask); //itself
        submit("Change Parent");
        assertTextPresent("Issue cannot be its own parent");

        setFormElement("parentIssue", parentIssue1); //current parent
        submit("Change Parent");
        assertTextPresent("Already linked to this parent issue");

        setFormElement("parentIssue", parentIssue3); //parent, different project
        submit("Change Parent");
        assertTextPresent("Cannot link Parent issue from a different project");

        setFormElement("parentIssue", subtask2); //subtask, same project
        submit("Change Parent");
        assertTextPresent("Subtasks cannot be a parent issue");

        setFormElement("parentIssue", subtask3); //subtask, different project
        submit("Change Parent");
        assertTextPresent("Subtasks cannot be a parent issue");
    }

    /**
     * this is a failing test, since it cannot click the 'Cancel' button
     */
    private void _testWizardCancelForMoveSubTaskParent()
    {
        gotoMoveSubTaskChooseOperation();
        checkCancelRedirectsViewIssue();

        gotoStep4ofMoveSubTaskParent();
        checkCancelRedirectsViewIssue();
    }

    private void _testSuccessfulMoveOfSubTaskParent()
    {
        gotoStep4ofMoveSubTaskParent();
        setFormElement("parentIssue", parentIssue2);
        submit("Change Parent");
        assertTextNotPresent("Step 4 of 4");

        assertLastChangeHistoryIs(subtask, "Parent Issue", parentIssue1, parentIssue2);
    }

    private void _testSidePanelLinksForMoveSubTaskParent()
    {
        //in step 4, go back to step 1
        gotoStep4ofMoveSubTaskParent();
        assertLinkPresentWithText(STEP_ONE_TITLE);
        assertLinkNotPresentWithText(STEP_TWO_TITLE);
        assertLinkNotPresentWithText(STEP_THREE_TITLE);
        assertLinkNotPresentWithText(STEP_FOUR_TITLE);
        clickLinkWithText(STEP_ONE_TITLE);
        assertTextPresent("Step 1 of 4");
        assertLinkNotPresentWithText(STEP_ONE_TITLE);
        assertLinkNotPresentWithText(STEP_TWO_TITLE);
        assertLinkNotPresentWithText(STEP_THREE_TITLE);
        assertLinkNotPresentWithText(STEP_FOUR_TITLE);
    }

    /**
     * Checks that links are maintained between subtasks after moving to a new
     * parent.
     */
    public void testMoveParentSubTaskLinks()
    {

        String origParentName = "First Parent";
        String firstParent = addIssue(PROJECT_MONKEY, PROJECT_MONKEY_KEY, "Bug", origParentName, "Major", null, null, null, null, null, null, null, null, null);
        String newParent = addIssue(PROJECT_MONKEY, PROJECT_MONKEY_KEY, "Bug", "Second Parent", "Major", null, null, null, null, null, null, null, null, null);
        String childKey = addSubTaskToIssue(firstParent, "Sub-task", "subtask originally on First Parent", "sub bug");
        String linkageName = "linkoid";
        String linkLabel = "is linked to";
        createIssueLinkType(linkageName, linkLabel, linkLabel);
        linkIssueWithComment(childKey, linkLabel, firstParent, null, null);
        // this appears necessary because linkIssue turns the bugger off !?
        activateIssueLinking();

        gotoIssue(childKey);
        clickLink("move-issue");
        checkCheckbox("operation", "move.subtask.parent.operation.name");
        submit("Next >>");
        setFormElement("parentIssue", newParent);
        submit("Change Parent");

        assertTextSequence(new String[]{linkLabel, firstParent, origParentName});
    }
//    /**
//     * Tests if the sub test is moved with the issue to a different project
//     */
//    public void subTaskMoveIssueWithSubTask()
//    {
//        log("Sub Task Move: Move issue with a sub task");
//        createSubTaskStep1(PROJECT_HOMOSAP_KEY + "-5", SUB_TASK_DEFAULT_TYPE);
//        setFormElement("summary",SUB_TASK_SUMMARY);
//        submit();
//        assertTextPresent(SUB_TASK_SUMMARY);
//        assertTextPresent("test 5");
//
//        // Move parent issue
//        String projectId = getProjectId(PROJECT_HOMOSAP);
//
//        clickLink("admin_link");
//        gotoIssue(PROJECT_HOMOSAP_KEY + "-5");
//        clickLink("move-issue");
//        setFormElement("beanTargetPid", projectId);
//        submit();
//        getDialog().setWorkingForm("jiraform");
//        submit();
//        assertTextPresent("New Value (after move)");
//        submit("Move");
//        assertTextPresent("test 5");
//
//        // Check the result
//        gotoIssue(PROJECT_HOMOSAP_KEY + "-5");
//        clickLinkWithText(SUB_TASK_SUMMARY);
//        assertTextPresent(PROJECT_HOMOSAP);
//
//        // restore to orignal settings
//        clickLink("delete-issue");
//        getDialog().setWorkingForm("jiraform");
//        submit();
//        deactivateSubTasks();
//
//    }
//
//    /**
//     * Tests if a user can move a sub task to a different sub task type
//     */
//    public void subTaskMoveSubTask()
//    {
//        log("Sub Task Move; Move a sub task to a different sub task type.");
//
//        createSubTaskStep1(PROJECT_HOMOSAP_KEY + "-1", SUB_TASK_DEFAULT_TYPE);
//        setFormElement("summary",SUB_TASK_SUMMARY);
//        submit();
//        assertTextPresent(SUB_TASK_SUMMARY);
//        assertTextPresent("test 1");
//
//        clickLink("move-issue");
//        getDialog().setWorkingForm("jiraform");
//        submit();
//
//        assertTextPresent("Step 2 of 3");
//        getDialog().setWorkingForm("jiraform");
//        submit();
//
//        assertTextPresent("Step 4 of 4");
//        submit("Move");
//
//        assertTextPresent(CUSTOM_SUB_TASK_TYPE_NAME);
//        assertTextPresent("Details");
//
//        // restore settings
//        clickLink("delete-issue");
//        getDialog().setWorkingForm("jiraform");
//        submit();
//        deactivateSubTasks();
//    }

}
