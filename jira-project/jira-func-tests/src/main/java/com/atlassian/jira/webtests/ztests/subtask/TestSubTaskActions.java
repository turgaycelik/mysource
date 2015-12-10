package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestSubTaskActions extends FuncTestCase
{
    public static final String ADD_SUBTASK_LINK = "add-subtask-type";
    public static final String SUBTASK_ADMIN_SECTION = "subtasks";

    //JRADEV-9795
    public void testRedirectedTryingToAddSubtaskButDisabled()
    {
        backdoor.subtask().disable();

        //Should not see the add subtask type link.
        navigation.gotoAdminSection(SUBTASK_ADMIN_SECTION);
        tester.assertLinkNotPresent(ADD_SUBTASK_LINK);

        tester.gotoPage("/secure/admin/subtasks/AddNewSubTaskIssueType.jspa");

        //We should be redirected back to the manage subtask page where they can enable subtasks.
        assertions.getURLAssertions().assertCurrentURLEndsWith("/secure/admin/subtasks/ManageSubTasks.jspa");
    }

    public void testSubTaskActions()
    {
        administration.restoreBlankInstance();
        subTasksCreateSubTaskType();
        subTasksDeleteSubTaskType();
        subTaskCreateDuplicateSubTaskType();
        subTaskCreateInvalidSubTaskType();
    }

    /**
     * JRADEV-662
     */
    public void testSubTaskOperationsHaveTheRightIssueHeader()
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
        administration.attachments().enable();
        administration.subtasks().enable();

        administration.subtasks().addSubTaskType(CUSTOM_SUB_TASK_TYPE_NAME, CUSTOM_SUB_TASK_TYPE_DESCRIPTION);

        text.assertTextPresent(locator.page(), "Sub-Tasks");
        text.assertTextPresent(locator.page(), CUSTOM_SUB_TASK_TYPE_NAME);

        final String summary = "Parent Summary";
        final String childSummary = "Child Summary";

        final String parentIssueKey = navigation.issue().createIssue("homosapien", "Bug", summary);
        final String childIssueKey = navigation.issue().
                createSubTask(parentIssueKey, CUSTOM_SUB_TASK_TYPE_NAME, childSummary, "Sub Task Desc");

        navigation.issue().gotoIssue(childIssueKey);
        assertSubTaskIssueHeader(parentIssueKey, childIssueKey, summary, childSummary);

        tester.clickLink("attach-file");
        assertSubTaskIssueHeader(parentIssueKey, childIssueKey, summary, childSummary);

        navigation.issue().gotoIssue(childIssueKey);
        tester.clickLink("clone-issue");
        assertSubTaskIssueHeader(parentIssueKey, childIssueKey, summary, childSummary);

        navigation.issue().gotoIssue(childIssueKey);
        tester.clickLink("delete-issue");
        assertSubTaskIssueHeader(parentIssueKey, childIssueKey, summary, childSummary);

        navigation.issue().gotoIssue(childIssueKey);
        tester.clickLink("view-voters");
        assertSubTaskIssueHeader(parentIssueKey, childIssueKey, summary, childSummary);

        navigation.issue().gotoIssue(childIssueKey);
        tester.clickLink("manage-watchers");
        assertSubTaskIssueHeader(parentIssueKey, childIssueKey, summary, childSummary);

        navigation.issue().gotoIssue(childIssueKey);
        tester.clickLink("log-work");
        assertSubTaskIssueHeader(parentIssueKey, childIssueKey, summary, childSummary);
    }

    private void assertSubTaskIssueHeader(final String parentIssueKey, final String childIssueKey,
            final String parentSummary, final String childSummary)
    {
        text.assertTextSequence(locator.page(), "homosapien", parentIssueKey, parentSummary, childIssueKey, childSummary);
    }

    /**
     * Tests the ability to create a 'New Sub Task Issue Type'
     */
    private void subTasksCreateSubTaskType()
    {
        log("Sub Task Action: Tests the ability to create a sub task type");
        administration.subtasks().addSubTaskType(CUSTOM_SUB_TASK_TYPE_NAME, CUSTOM_SUB_TASK_TYPE_DESCRIPTION);
        administration.subtasks().enable();
        text.assertTextPresent(locator.page(), "Sub-Tasks");
        text.assertTextPresent(locator.page(), CUSTOM_SUB_TASK_TYPE_NAME);
        administration.subtasks().disable();
    }

    /**
     * Tests the ability to delete a 'New Sub Task Issue Type'
     */
    private void subTasksDeleteSubTaskType()
    {
        log("Sub Task Action: Tests the ability to create a sub task type");
        administration.subtasks().enable();

        administration.subtasks().deleteSubTaskType(CUSTOM_SUB_TASK_TYPE_NAME);

        navigation.gotoAdminSection(SUBTASK_ADMIN_SECTION);
        text.assertTextPresent(locator.page(), "Sub-Tasks");
        text.assertTextNotPresent(locator.page(), CUSTOM_SUB_TASK_TYPE_NAME);

        administration.subtasks().addSubTaskType(CUSTOM_SUB_TASK_TYPE_NAME, CUSTOM_SUB_TASK_TYPE_DESCRIPTION);
        administration.subtasks().disable();
    }

    /**
     * Ensures two sub task types cannot be given the same name
     */
    private void subTaskCreateDuplicateSubTaskType()
    {
        log("Sub Task Action: Ensures two sub task types cannot be given the same name");
        administration.subtasks().enable();

        gotoAddSubtask();
        tester.setFormElement("name", CUSTOM_SUB_TASK_TYPE_NAME);
        tester.setFormElement("description", CUSTOM_SUB_TASK_TYPE_DESCRIPTION);
        tester.submit("Add");
        text.assertTextPresent(locator.page(), "An issue type with this name already exists.");

        administration.subtasks().disable();
    }

    /**
     * Ensures a sub task types cannot be given no name
     */
    private void subTaskCreateInvalidSubTaskType()
    {
        log("Sub Task Action: Ensures a sub task types cannot be given no name");
        administration.subtasks().enable();

        gotoAddSubtask();
        tester.setFormElement("name", "");
        tester.setFormElement("description", CUSTOM_SUB_TASK_TYPE_DESCRIPTION);
        tester.submit("Add");
        text.assertTextPresent(locator.page(), "You must specify a name for this new sub-task issue type.");

        administration.subtasks().disable();
    }

    private void gotoAddSubtask()
    {
        navigation.gotoAdminSection(SUBTASK_ADMIN_SECTION);
        tester.clickLink(ADD_SUBTASK_LINK);
    }
}
