package com.atlassian.jira.webtests.ztests.issue.subtasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestMoveSubtask extends FuncTestCase
{
    // JRA-14232: when subtasks use different field configs the move link was invalid.
    public void testMoveSubtask() throws Exception
    {
        administration.restoreData("TestMoveSubtask.xml");

        navigation.issue().viewIssue("RAT-14");

        // Click Link 'Edit' (id='edit_issue').
        tester.clickLink("edit-issue");
        tester.clickLinkWithText("moving");

        tester.assertTextNotPresent("Sub-tasks cannot be moved independently of the parent issue.");
        tester.assertTextPresent("Move Sub-Task: Choose Operation");
        tester.assertTextPresent("Change issue type for this subtask");
    }

    //JRA-13011: sub-task components not reset when parent is moved.
    public void testMoveSubtaskWithComponents() throws Exception
    {
        administration.restoreData("TestMoveSubtaskWithComponent.xml");

        //Goto the first issue.
        navigation.issue().viewIssue("SRC-1");

        tester.clickLink("move-issue");

        //Select the project to move to.
        tester.selectOption("10010_1_pid", "TARGET");
        tester.submit("Next");
        tester.submit("Next");

        //Select the component for the parent issue.
        tester.selectOption("components_10010", "TGT2");
        tester.submit("Next");
        tester.submit("Next");

        //Move the issue.
        tester.assertTextPresent("Below is a summary of all issues that will be moved");
        assertions.getTextAssertions().assertTextSequence(new TableLocator(tester, "10010_1_components"), new String[]{"SRC1 [Project: SOURCE] TGT2"});
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();

        //Goto the sub issue.
        tester.clickLinkWithText("Source Subtask for Issue 1");

        //Make sure the sub-issue has no components.
        assertions.getTextAssertions().assertTextPresent(new IdLocator(tester, "components-val"), "None");
    }

    public void testMoveSubtasksAcrossProjectsWithDifferentRenderers()
    {
        administration.restoreData("TestMoveSubtaskAcrossProjectsWithDifferentRenderers.xml");

        navigation.issue().viewIssue("DTRP-2");
        assertions.getTextAssertions().assertTextPresent(new IdLocator(tester, "descriptionmodule"), "Subtask description.");

        navigation.issue().viewIssue("DTRP-1");
        assertions.getTextAssertions().assertTextPresent(new IdLocator(tester, "descriptionmodule"), "Task description.");

        tester.clickLink("move-issue");

        tester.selectOption("10030_3_pid", "Wiki Text Renderer Project");
        tester.submit("Next");
        tester.submit("Next");
        tester.checkCheckbox("retain_description");

        tester.assertTextPresent("Update fields for issues with current issue type(s)");
        tester.submit("Next");
        tester.submit("Next");

        tester.assertTextPresent("Below is a summary of all issues that will be moved");
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();

        navigation.issue().viewIssue("WTRP-1");
        assertions.getTextAssertions().assertTextPresent("Subtask with default renderer");
        // BUG: assertions.getTextAssertions().assertTextPresent(new IdLocator(tester, "descriptionmodule"), "Task description.");

        navigation.issue().viewIssue("WTRP-2");
        assertions.getTextAssertions().assertTextPresent(new IdLocator(tester, "descriptionmodule"), "Subtask description.");
    }
}
