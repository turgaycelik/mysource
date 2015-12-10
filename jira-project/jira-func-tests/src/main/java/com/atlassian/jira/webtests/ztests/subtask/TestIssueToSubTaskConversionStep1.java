package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.SUB_TASKS })
public class TestIssueToSubTaskConversionStep1 extends JIRAWebTest
{
    private static final String ISSUE_WITHOUT_SUBTASK = "HSP-1";
    private static final String ISSUE_WITHOUT_SUBTASK_ID = "10000";
    private static final String ISSUE_WITH_SUBTASK = "HSP-2";
    private static final String ISSUE_WITH_SUBTASK_ID = "10001";
    private static final String SUBTASK = "HSP-3";
    private static final String SUBTASK_ID = "10002";
    private static final String INVALID_ISSUE_1 = "HSP-9";
    private static final String INVALID_ISSUE_ID_1 = "1)%20Please%20login%20again%20at%20https://attacker.com%20";
    private static final String INVALID_ISSUE_ID_2 = "10000a";
    private static final String ISSUE_FROM_OTHER_PROJECT = "MKY-1";

    private static final String ISSUE_TO_CONVERT_ID = "10020";
    private static final String ISSUE_TO_CONVERT_KEY = "MKY-2";
    private static final String PARENT_ISSUE = "MKY-3";
    private static final String SUBTASK_TYPE = "Sub-task";
    private static final String SUBTASK_TYPE_ID = "5";
    private static final String TASK_TYPE = "Task";
    private static final String TASK_TYPE_ID = "3";
    private static final String SUBTASK_TYPE_2 = "Sub-task 2";
    private static final String SUBTASK_TYPE_2_ID = "6";
    private static final String SUBTASK_TYPE_3 = "Sub-task 3";
    private static final String SUBTASK_TYPE_3_ID = "7";
    private static final String INVALID_TYPE_ID = "976";

    public TestIssueToSubTaskConversionStep1(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueToSubTaskConversion.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    /*
     * If you place historical parent key it should get translated to the current project key.
     */
    public void testParentIssueKeyIsTranslatedToCurrentKey()
    {
        Long projectId = backdoor.project().getProjectId("HSP");
        backdoor.project().addProjectKey(projectId, "OLD");

        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertTextPresent("Select Parent Issue");
        assertTextPresent("Begin typing to search for issues to link");
        assertTextPresent("Only non-sub-task issues from the same project (HSP) can be selected.");

        // Cannot be null
        setFormElement("parentIssueKey", "OLD-2");
        submit();
        assertTextPresentAfterText("HSP-2", "Parent Issue:");
    }

    /*
     * Tests that when subtasks are not enabled, issues cannot be converted to subtasks.
     */
    public void testIssueToSubTaskConversionWhenSubTaskNotEnabled()
    {
        gotoIssue(ISSUE_WITHOUT_SUBTASK);
        assertLinkPresent("issue-to-subtask");

        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertTextPresent("Step 1 of 4");

        deleteIssue(SUBTASK);
        // Make sure deactivateSubTasks() did not delete all the issues
        assertFalse(deactivateSubTasks());

        gotoIssue(ISSUE_WITHOUT_SUBTASK);
        assertLinkNotPresent("issue-to-subtask");

        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertTextPresent("Errors");
        assertTextPresent("Sub-tasks are disabled.");
        assertTextNotPresent("Step 1 of 4");
    }

    /*
     * Tests that subtasks cannot be converted to subtasks.
     */
    public void testIssueToSubTaskConversionOnSubtask()
    {
        gotoIssue(ISSUE_WITHOUT_SUBTASK);
        assertLinkPresent("issue-to-subtask");

        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertTextPresent("Step 1 of 4");

        gotoIssue(SUBTASK);
        assertLinkNotPresent("issue-to-subtask");

        gotoConvertIssue(SUBTASK_ID);
        assertTextPresent("Errors");
        assertTextPresent("Issue " + SUBTASK + " is already a sub-task.");
        assertTextNotPresent("Step 1 of 4");
    }

    /*
     * Tests that issues which have subtasks cannot be converted to subtasks.
     */
    public void testIssueToSubTaskConversionWhenIssueHasSubTasks()
    {
        gotoIssue(ISSUE_WITHOUT_SUBTASK);
        assertLinkPresent("issue-to-subtask");

        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertTextPresent("Step 1 of 4");

        gotoIssue(ISSUE_WITH_SUBTASK);
        assertLinkNotPresent("issue-to-subtask");

        gotoConvertIssue(ISSUE_WITH_SUBTASK_ID);
        assertTextPresent("Errors");
        assertTextPresent("Can not convert issue " + ISSUE_WITH_SUBTASK + " with sub-tasks to a sub-task.");
        assertTextNotPresent("Step 1 of 4");

        // Check Wizard pane shows correct link
        assertTextPresentBeforeText("Return to", ISSUE_WITH_SUBTASK);
        assertLinkPresentWithText(ISSUE_WITH_SUBTASK);
    }

    /*
     * Tests that convert issue to subtasks is only available to users with Permissions.EDIT_ISSUE
     */
    public void testIssueToSubTaskConversionEditPermission()
    {
        gotoIssue(ISSUE_WITHOUT_SUBTASK);
        assertLinkPresent("issue-to-subtask");

        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertTextPresent("Step 1 of 4");
        assertTextPresent("Select Parent Issue");
        assertTextPresent("Begin typing to search for issues to link");
        assertTextPresent("Only non-sub-task issues from the same project (HSP) can be selected.");

        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoIssue(ISSUE_WITHOUT_SUBTASK);
        assertLinkNotPresent("issue-to-subtask");

        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertTextPresent("Access Denied");
        assertTextNotPresent("Step 1 of 4");
    }

    /*
     * Tests that convert issue to subtasks is not available when subtasks are not in the issue types scheme
     */
    public void testIssueToSubTaskConversionWhenNotInIssueTypes()
    {
        gotoIssue(ISSUE_WITHOUT_SUBTASK);
        assertLinkPresent("issue-to-subtask");

        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertTextPresent("Step 1 of 4");

        // Delete all subtask issue types (and all subtask issues)
        deleteIssue(SUBTASK);
        gotoPage("secure/admin/DeleteIssueType!default.jspa?id=" + SUBTASK_TYPE_ID);
        assertTextPresent("Delete Issue Type: " + SUBTASK_TYPE);
        submit("Delete");
        gotoPage("secure/admin/DeleteIssueType!default.jspa?id=" + SUBTASK_TYPE_2_ID);
        assertTextPresent("Delete Issue Type: " + SUBTASK_TYPE_2);
        submit("Delete");
        gotoPage("secure/admin/DeleteIssueType!default.jspa?id=" + SUBTASK_TYPE_3_ID);
        assertTextPresent("Delete Issue Type: " + SUBTASK_TYPE_3);
        submit("Delete");

        gotoIssue(ISSUE_WITHOUT_SUBTASK);
        assertLinkNotPresent("issue-to-subtask");

        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertTextPresent("Errors");
        assertTextPresent("Project HSP associated with issue does not have any sub-task issue types available.");
        assertTextNotPresent("Step 1 of 4");
    }

    /*
     * Tests that an invalid issue id returns error
     */
    public void testIssueToSubTaskConversionIsProtectAgainstContentSpoofingThroughAnInvalidIssueId()
    {
        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertTextPresent("Step 1 of 4");

        gotoConvertIssue(INVALID_ISSUE_ID_1);
        assertTextPresent("Errors");
        assertTextPresent("Invalid issue id");
        assertTextNotPresent("attacker.com");
        assertTextNotPresent("Step 1 of 4");

        gotoConvertIssue(INVALID_ISSUE_ID_2);
        assertTextPresent("Errors");
        assertTextNotPresent(INVALID_ISSUE_ID_2);
        assertTextNotPresent("Step 1 of 4");

        // Check Wizard pane shows correct link
        assertTextPresentBeforeText("Return to", "Dashboard");
        assertLinkPresentWithText("Dashboard");
    }

    /*
     * Tests that the parent issue must be valid
     */
    public void testIssueToSubTaskConversionInvalidParentIssue()
    {
        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertTextPresent("Select Parent Issue");
        assertTextPresent("Begin typing to search for issues to link");
        assertTextPresent("Only non-sub-task issues from the same project (HSP) can be selected.");

        // Cannot be null
        setFormElement("parentIssueKey", "");
        submit();
        assertTextPresent("Parent issue key not specified.");

        // Must exist
        setFormElement("parentIssueKey", INVALID_ISSUE_1);
        submit();
        assertTextPresent("Parent issue with key " + INVALID_ISSUE_1 + " not found.");

        // Cannot be from another project
        setFormElement("parentIssueKey", ISSUE_FROM_OTHER_PROJECT);
        submit();
        assertTextPresent("Parent issue " + ISSUE_FROM_OTHER_PROJECT + " must be from the same project as issue " + ISSUE_WITHOUT_SUBTASK);

        // Cannot be itself
        setFormElement("parentIssueKey", ISSUE_WITHOUT_SUBTASK);
        submit();
        assertTextPresent("Issue " + ISSUE_WITHOUT_SUBTASK + " can not be parent of itself.");

        // Cannot be sub-task
        setFormElement("parentIssueKey", SUBTASK);
        submit();
        assertTextPresent("Parent issue " + SUBTASK + " can not be sub-task.");
    }

    /*
    * Tests that the new subtask type list contains only valid new subtask types
    */
    public void testIssueToSubTaskConversionSubTaskType()
    {
        // Project where SUBTASK_TYPE_2 exists
        gotoConvertIssue(ISSUE_WITHOUT_SUBTASK_ID);
        assertOptionsEqual("issuetype", new String[]{SUBTASK_TYPE, SUBTASK_TYPE_2, SUBTASK_TYPE_3});
        assertOptionValueNotPresent("issuetype", TASK_TYPE);

        // Project where SUBTASK_TYPE_2 doesn't exist
        gotoConvertIssue(ISSUE_TO_CONVERT_ID);
        assertOptionsEqual("issuetype", new String[]{SUBTASK_TYPE, SUBTASK_TYPE_3});
        assertOptionValueNotPresent("issuetype", SUBTASK_TYPE_2);
    }

    /*
     * Tests that the new subtask type must be valid
     */
    public void testIssueToSubTaskConversionInvalidSubTaskType()
    {
        // No such issue type
        gotoConvertIssueStep2(ISSUE_TO_CONVERT_ID, PARENT_ISSUE, INVALID_TYPE_ID);
        assertTextPresent("Selected issue type not found.");

        // Not applicable for the project
        gotoConvertIssueStep2(ISSUE_TO_CONVERT_ID, PARENT_ISSUE, SUBTASK_TYPE_2_ID);

        assertTextPresent("Issue type " + SUBTASK_TYPE_2 + " not applicable for this project");

        // Not a subtask type
        gotoConvertIssueStep2(ISSUE_TO_CONVERT_ID, PARENT_ISSUE, TASK_TYPE_ID);

        assertTextPresent("Issue type " + TASK_TYPE + " is not a sub-task");
    }

    /*
     * Tests that the panel shows the correct steps and progress for step 1
     */
    public void testIssueToSubTaskConversionPanelStep1()
    {
        gotoConvertIssue(ISSUE_TO_CONVERT_ID);
        assertTextPresent("Select Parent Issue");
        assertTextPresent("Begin typing to search for issues to link");
        assertTextPresent("Only non-sub-task issues from the same project (MKY) can be selected.");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT_KEY, 1);

    }

}
