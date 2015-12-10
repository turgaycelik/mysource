package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * A FuncTest that covers JRA-14032
 *
 * Ensure that
 * .. you can't create subtasks for an issue that is closed.
 * .. you can't close a task/issue that has open subtasks. (sub task blocking condition has to be activated)
 * .. you can't convert a task to a sub-task and assign it to a parent that is closed
 * .. you can't change the parent of a subtask through the move operation to a parent that is closed.
 *
 * Overall we try to avoid to end up with a closed parent issue that has open subtasks.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestClosedParent extends FuncTestCase
{

    public void testCantCreateSubtaskWhenParentClosed()
    {
        administration.restoreData("TestCantCreateSubtaskWhenParentClosed.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        //Go to the parent
        navigation.issue().viewIssue("HSP-3");
        tester.assertLinkPresent("create-subtask");

        //Close the parent
        tester.clickLinkWithText("Close Issue");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");

        //Ensure we can't create a new subtask, when parent is closed.
        tester.assertLinkPresentWithText("Reopen Issue");
        tester.assertLinkNotPresent("create-subtask");
    }


    public void testCantCloseParentWithOpenSubTasks()
    {
        administration.restoreData("TestCantCreateSubtaskWhenParentClosed.xml");

        //Navigate to child
        navigation.issue().viewIssue("HSP-2");
        //Ensure child is open

        tester.assertTextPresent("Open");
        tester.assertTextPresent("The issue is open and ready for the assignee to start work on it.");

        //Navigate to parent
        navigation.issue().viewIssue("HSP-1");

        //Ensure parent is open
        tester.assertTextPresent("Open");
        tester.assertTextPresent("The issue is open and ready for the assignee to start work on it.");

        //Ensure we can't close the parent
        tester.assertLinkNotPresentWithText("Close Issue");

        //Navigate to child
        navigation.issue().viewIssue("HSP-2");

        tester.clickLinkWithText("Close Issue");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");

        navigation.issue().viewIssue("HSP-1");
        //Ensure we can close the parent
        tester.assertLinkPresentWithText("Close Issue");
    }


    public void testConvertIssueToSubtask()
    {
        administration.restoreData("TestConvertIssueToSubtask.xml");

        navigation.issue().viewIssue("HSP-3");
        tester.clickLinkWithText("Convert");

        //Step 1
        //Set the new parent for this issue
        tester.setWorkingForm("jiraform");
        tester.setFormElement("parentIssueKey", "HSP-1");
        tester.submit();

        tester.assertTextPresent("The issue HSP-1 is not editable.");
    }


    public void testMoveSubtaskToClosedParent()
    {
        administration.restoreData("TestMoveSubtaskToClosedParent.xml");
        navigation.issue().viewIssue("HSP-4");

        tester.clickLink("move-issue");

        tester.setWorkingForm("jiraform");
        tester.setFormElement("operation","move.subtask.parent.operation.name");
        tester.submit();

        tester.setWorkingForm("jiraform");
        tester.setFormElement("parentIssue", "HSP-1");

        tester.submit();

        tester.assertTextPresent("The new parent issue HSP-1 is not editable.");
    }



    public void testCantChangeAssigneeWhenIssueIsClosed()
    {
        administration.restoreData("TestCantChangeAssigneeWhenIssueIsClosed.xml");

        //Navigate to a closed Issue
        navigation.issue().viewIssue("HSP-1");

        tester.assertLinkNotPresentWithText("Assign");

         //Navigate to a closed subtask
        navigation.issue().viewIssue("HSP-2");

        tester.assertLinkNotPresentWithText("Assign");

        navigation.issue().viewIssue("HSP-1");
        tester.clickLinkWithText("Reopen Issue");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");
        navigation.issue().viewIssue("HSP-1");
        tester.assertLinkPresentWithText("Assign");


        navigation.issue().viewIssue("HSP-2");
        tester.clickLinkWithText("Reopen Issue");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");
        navigation.issue().viewIssue("HSP-2");
        tester.assertLinkPresentWithText("Assign");
    }


}
