package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.io.IOException;

@WebTest ({ Category.FUNC_TEST, Category.SUB_TASKS })
public class TestCreateSubTasks extends JIRAWebTest
{
    private static final String PROJECT_MONKEY_ID = "10001";

    public TestCreateSubTasks(String name)
    {
        super(name);
    }

    @Override
    public void tearDown()
    {
        getBackdoor().darkFeatures().disableForSite("jira.no.frother.reporter.field");
        super.tearDown();
    }

    public void testCreateSubTaskInJiraWithSingleSubTaskType()
    {
        // TestOneProjectWithOneIssueType.xml is set up with a single project that uses "Bugs Only" issue type scheme,
        // which has only a single "Bug" issue type.
        administration.restoreData("TestOneProjectWithOneIssueType.xml");

        // the number of projects present should not matter, so add a project too
        administration.project().addProject("neanderthal", "NEA", ADMIN_USERNAME);

        // Associate "Bugs & Subtasks" with homosapien project
        Long projectId = backdoor.project().getProjectId("HSP");
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);

        tester.checkCheckbox("createType", "chooseScheme");
        // Select 'Bugs & Sub-tasks' from select box 'schemeId'.
        tester.selectOption("schemeId", "Bugs & Sub-tasks");
        tester.submit();

        navigation.issue().createIssue("homosapien", "Bug", "First issue");
        navigation.issue().viewIssue("HSP-1");

        tester.clickLink("create-subtask");
        tester.assertTextNotPresent("Choose the project and issue type"); // step 1 skipped
        tester.assertTextPresent("Create Sub-Task");
        assertTextSequence(new String[] { "Project", "homosapien", "Issue Type", "Sub-task" });
        tester.assertFormElementPresent("summary");
    }

    public void testCreateSubTasks()
    {
        administration.restoreBlankInstance();
        getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
        getBackdoor().darkFeatures().enableForSite("jira.no.frother.reporter.field");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        administration.project().addProject(PROJECT_NEO, PROJECT_NEO_KEY, ADMIN_USERNAME);
        administration.subtasks().addSubTaskType(CUSTOM_SUB_TASK_TYPE_NAME, CUSTOM_SUB_TASK_TYPE_DESCRIPTION);

        resetFields();
        String issueKeyNormal = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test for sub tasks", "Minor", null, null, null, ADMIN_FULLNAME, "test environment 1", "test description for sub tasks", null, null, null);

        subTasksWithSubTasksEnabled(issueKeyNormal);
        subTasksCreateSubTaskWithCustonType(issueKeyNormal);
        subTasksWithCreatePermission(issueKeyNormal);
        subTaskWithNoSummary(issueKeyNormal);
        subTaskWithRequiredFields(issueKeyNormal);
        subTaskWithHiddenFields(issueKeyNormal);
        subTaskWithInvalidDueDate(issueKeyNormal);
        subTaskWithSchedulePermission(issueKeyNormal);
        subTaskWithAssignPermission(issueKeyNormal);
        subTaskWithModifyReporterPermission(issueKeyNormal);
        subTaskWithTimeTracking(issueKeyNormal);
        subTaskWithUnassignableUser(issueKeyNormal);
        subTaskMoveIssueWithSubTask(issueKeyNormal);
//        subTaskMoveSubTask(issueKeyNormal);
////            subTaskWithFieldSchemeRequired();
////            subTaskWithFieldSchemeHidden();
//            subTaskCreateSubTaskWithSecurity();

        deleteIssue(issueKeyNormal);

    }

    public void testCreateSubtaskSkipStep1OnlyOneProjectAndOneIssueType()
    {
        administration.restoreData("TestCreateSubtaskOneProjectOneSubtaskType.xml");
        assertRedirectAndFollow("/secure/CreateSubTaskIssue!default.jspa?parentIssueId=10000",
                ".*CreateSubTaskIssue\\.jspa\\?parentIssueId=10000&pid=" + PROJECT_MONKEY_ID + "&issuetype=4$");
        assertTextSequence(new String[] { "Create Sub-Task", "Project", "monkey", "Issue Type", "Sub-task", "Summary" });
    }

    public void testCreateIssueSkipStep1IssueTypeSchemeInfersOneProjectAndIssueType()
    {
        // check the preselection of the project in the form to "current project"
        administration.restoreData("TestCreateMonkeyHasOneIssueType.xml");

        // check we redirect to step 2 if we pass a pid url param for a project which has one issue type
        assertRedirectAndFollow("/secure/CreateSubTaskIssue!default.jspa?parentIssueId=10000",
                ".*CreateSubTaskIssue\\.jspa\\?parentIssueId=10000&pid=" + PROJECT_MONKEY_ID + "&issuetype=5$");
        assertTextSequence(new String[] { "Create Sub-Task", "Project", "monkey", "Issue Type", "Sub-task", "Summary" });

        // check that we do not redirect for homosapien issue since there are multiple issue types to choose from
        tester.gotoPage("/secure/CreateSubTaskIssue!default.jspa?parentIssueId=10001"); // HSP-1
        tester.assertFormPresent("subtask-create-start");

        // logout and then rerequest the url
        navigation.logout();
        tester.gotoPage("/secure/CreateSubTaskIssue!default.jspa?parentIssueId=10000"); // MKY-1
        tester.assertTextPresent("You are not logged in");
        tester.clickLinkWithText("Log In");
        tester.setFormElement("os_username", ADMIN_USERNAME);
        tester.setFormElement("os_password", ADMIN_USERNAME);
        tester.setWorkingForm("login-form");
        tester.submit();
        assertTextSequence(new String[] { "Create Sub-Task", "Project", "monkey", "Issue Type", "Sub-task", "Summary" });
    }

    /**
     * Tests if the 'Sub Task' link is available with 'Sub Tasks' enabled
     */
    private void subTasksWithSubTasksEnabled(String issueKey)
    {
        administration.subtasks().enable();
        log("Sub Task Create: Tests the availability of the 'Sub Task' Link with 'Sub Tasks' enabled");
        navigation.issue().gotoIssue(issueKey);
        tester.assertLinkPresent("create-subtask");

        administration.subtasks().disable();
        navigation.issue().gotoIssue(issueKey);
        tester.assertLinkNotPresent("create-subtask");
    }

    /**
     * Tests the ability to create a sub task using a custom-made sub task type
     */
    private void subTasksCreateSubTaskWithCustonType(String issueKey)
    {
        log("Sub Task Create: Tests the ability to create a sub task using a custom-made sub task type");
        createSubTaskStep1(issueKey, CUSTOM_SUB_TASK_TYPE_NAME);

        tester.setFormElement("summary", CUSTOM_SUB_TASK_SUMMARY);
        tester.submit();
        tester.assertTextPresent(CUSTOM_SUB_TASK_SUMMARY);
        tester.assertTextPresent("test for sub tasks");

        // All sub-tasks must be deleted before Sub-Tasks can be deactivated
        deleteCurrentIssue();
        administration.subtasks().disable();
        tester.assertTextPresent("Enable");
    }

    /**
     * Tests if the 'Create Sub Task' Link is available with the 'Create Issue' permission removed
     */
    private void subTasksWithCreatePermission(String issueKey)
    {
        log("Sub Task Create: Test availability of 'Create Sub Task' link with 'Create Issue' permission.");
        administration.subtasks().enable();
        removeGroupPermission(CREATE_ISSUE, Groups.USERS);
        navigation.issue().gotoIssue(issueKey);
        tester.assertLinkNotPresent("create-subtask");

        // Grant 'Create Issue' permission
        grantGroupPermission(CREATE_ISSUE, Groups.USERS);
        navigation.issue().gotoIssue(issueKey);
        tester.assertLinkPresent("create-subtask");
        administration.subtasks().disable();
    }


    private void subTaskWithNoSummary(String issueKey)
    {
        log("Sub Task Create: Adding sub task without summary");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);

        // Not setting summary

        // Need to set priority as this is automatically set
        tester.selectOption("priority", minorPriority);

        tester.submit();
        tester.assertTextPresent("Create Sub-Task");
        tester.assertTextPresent("You must specify a summary of the issue.");
        administration.subtasks().disable();
    }

    /**
     * Makes the fields Components,Affects Versions and Fixed Versions required.
     * Attempts to create a sub task with required fields not filled out and with an invalid assignee
     */
    private void subTaskWithRequiredFields(String issueKey)
    {
        // Set fields to be required
        setRequiredFields();

        log("Sub Task Create: Test the creation of a sub task using required fields");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);

        tester.setFormElement("summary", "This is a new summary");
        tester.setFormElement("reporter", "");

        tester.submit("Create");

        tester.assertTextPresent("Create Sub-Task");
        tester.assertTextPresent("Component/s is required");
        tester.assertTextPresent("Affects Version/s is required");
        tester.assertTextPresent("Fix Version/s is required");

        // Reset fields to be optional
        resetFields();
        administration.subtasks().disable();
    }

    /**
     * Makes the fields Components,Affects Versions and Fixed Versions hidden.
     */
    private void subTaskWithHiddenFields(String issueKey)
    {
        // Hide fields
        setHiddenFields(COMPONENTS_FIELD_ID);
        setHiddenFields(AFFECTS_VERSIONS_FIELD_ID);
        setHiddenFields(FIX_VERSIONS_FIELD_ID);

        log("Sub Task Create: Test the creation of a sub task using hidden fields");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);

        tester.assertTextPresent("Create Sub-Task");

        tester.assertLinkNotPresent("components");
        tester.assertLinkNotPresent("versions");
        tester.assertLinkNotPresent("fixVersions");

        // Reset fields to be optional
        resetFields();
        administration.subtasks().disable();
    }


    private void subTaskWithInvalidDueDate(String issueKey)
    {
        log("Sub Task Create: Creating sub task with invalid due date");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);

        tester.setFormElement("summary", "stuff");
        tester.setFormElement("duedate", "stuff");

        tester.submit("Create");

        tester.assertTextPresent("Create Sub-Task");

        tester.assertTextPresent("You did not enter a valid date. Please enter the date in the format &quot;d/MMM/yy&quot;");
        administration.subtasks().disable();
    }

    /**
     * Tests if the Due Date' field is available with the 'Schedule Issue' permission removed
     */
    private void subTaskWithSchedulePermission(String issueKey)
    {
        log("Sub Task Create: Test prescence of 'Due Date' field with 'Schedule Issue' permission.");
        removeGroupPermission(SCHEDULE_ISSUE, Groups.DEVELOPERS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        tester.assertTextNotPresent("Due Date");
        administration.subtasks().disable();

        // Grant Schedule Issue Permission
        grantGroupPermission(SCHEDULE_ISSUE, Groups.DEVELOPERS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        tester.assertTextPresent("Due Date");
        administration.subtasks().disable();
    }

    /**
     * Tests if the user is able to assign an issue with the 'Assign Issue' permission removed
     */
    private void subTaskWithAssignPermission(String issueKey)
    {
        log("Sub Task Create: Test ability to specify assignee with 'Assign Issue' permission.");
        removeGroupPermission(ASSIGN_ISSUE, Groups.DEVELOPERS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        tester.assertFormElementNotPresent("assignee");
        administration.subtasks().disable();

        // Grant Assign Issue Permission
        grantGroupPermission(ASSIGN_ISSUE, Groups.DEVELOPERS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        tester.assertFormElementPresent("assignee");
        administration.subtasks().disable();
    }

    /**
     * Tests if the 'Reporter' Option is available with the 'Modify Reporter' permission removed
     */
    private void subTaskWithModifyReporterPermission(String issueKey)
    {
        log("Sub Task Create: Test availability of Reporter with 'Modify Reporter' permission.");
        removeGroupPermission(MODIFY_REPORTER, Groups.ADMINISTRATORS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        tester.assertFormElementNotPresent("reporter");
        administration.subtasks().disable();

        // Grant Modify Reporter Permission
        grantGroupPermission(MODIFY_REPORTER, Groups.ADMINISTRATORS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        tester.assertFormElementPresent("reporter");
        administration.subtasks().disable();
    }

    /**
     * Tests if the 'Orignial Estimate' Link is available with Time Tracking activated
     */
    private void subTaskWithTimeTracking(String issueKey)
    {
        log("Sub task Create: Test availability of the original esitmate field with time tracking activated");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        tester.assertFormElementNotPresent("timetracking");
        administration.subtasks().disable();

        activateTimeTracking();
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        tester.assertFormElementPresent("timetracking");
        deactivateTimeTracking();
        administration.subtasks().disable();
    }

    /**
     * Tests if the user is able to assign an issue with the 'Assignable User' permission removed
     */
    private void subTaskWithUnassignableUser(String issueKey)
    {
        log("Sub Task Create: Attempt to set the assignee to be an unassignable user ...");

        // Remove assignable permission
        removeGroupPermission(ASSIGNABLE_USER, Groups.DEVELOPERS);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        tester.submit("Create");

        tester.assertTextPresent(DEFAULT_ASSIGNEE_ERROR_MESSAGE);

        //Restore permission
        grantGroupPermission(ASSIGNABLE_USER, Groups.DEVELOPERS);
        administration.subtasks().disable();
    }

    /**
     //     * Tests if a sub task has its security level automatically allocated
     //     */
//    public void subTaskCreateSubTaskWithSecurity()
//    {
//        log("Sub Task Create; Create a sub task from an issue with a security level");
//        grantGroupPermission(SET_ISSUE_SECURITY, JIRA_ADMIN);
//        createSubTaskStep1(PROJECT_HOMOSAP_KEY + "-9", SUB_TASK_DEFAULT_TYPE);
//        setFormElement("summary", SUB_TASK_SUMMARY);
//        submit();
//        assertTextPresent("test 9");
//        assertTextPresent(SUB_TASK_SUMMARY);
//        assertTextPresent(SECURITY_LEVEL_TWO_NAME);
//
//        // delete the sub-task
//        clickLink("delete-issue");
//        getDialog().setWorkingForm("delete_confirm_form");
//        submit();
//
//        administration.subtasks().disable();
//        setSecurityLevelToRequried();
//        createSubTaskStep1(PROJECT_HOMOSAP_KEY + "-9", SUB_TASK_DEFAULT_TYPE);
//        setFormElement("summary", SUB_TASK_SUMMARY);
//        submit();
//
//        assertTextPresent("Step 2 of 2");
//        assertTextPresent("Security Level is required. The &quot;Set Issue Security&quot; permission is required in order to set this field.");
////        assertTextPresent("test 9");
////        assertTextPresent(SUB_TASK_SUMMARY);
////        assertTextPresent(SECURITY_LEVEL_TWO_NAME);
//
//        // delete the issue
////        clickLink("delete-issue");
////        getDialog().setWorkingForm("delete_confirm_form");
////        submit();
//
//        setSecurityLevelToRequried();
//        removeGroupPermission(SET_ISSUE_SECURITY, JIRA_ADMIN);
//        administration.subtasks().disable();
//    }
//
    /**
     * Tests if the sub test is moved with the issue to a different project
     */
    private void subTaskMoveIssueWithSubTask(String issueKey)
    {
        log("Sub Task Move: Move issue with a sub task");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        tester.setFormElement("summary", SUB_TASK_SUMMARY);
        tester.submit();
        tester.assertTextPresent(SUB_TASK_SUMMARY);
        tester.assertTextPresent("test for sub tasks");

        // Move parent issue
        grantGroupPermission(MOVE_ISSUE, Groups.DEVELOPERS);
        gotoIssue(issueKey);
        tester.clickLink("move-issue");
        tester.selectOption("10000_1_pid", PROJECT_NEO);
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");

        // Check the result
        gotoIssue(issueKey);
        tester.clickLinkWithText(SUB_TASK_SUMMARY);
        tester.assertTextPresent(PROJECT_NEO);

        // restore to orignal settings
        deleteCurrentIssue();
        administration.subtasks().disable();

    }

    /**
     * Tests if a user can move a sub task to a different sub task type
     */
    private void subTaskMoveSubTask(String issueKey)
    {
        String subTaskKey;
        log("Sub Task Move; Move a sub task to a different sub task type.");
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        tester.setFormElement("summary", SUB_TASK_SUMMARY);
        tester.submit();
        tester.assertTextPresent(SUB_TASK_SUMMARY);
        tester.assertTextPresent("test for sub tasks");

        String text;

        try
        {
            text = tester.getDialog().getResponse().getText();
            int projectIdLocation = text.indexOf(PROJECT_NEO_KEY);
            int endOfIssueKey = text.indexOf("]", projectIdLocation);
            subTaskKey = text.substring(projectIdLocation, endOfIssueKey);
        }
        catch (IOException t)
        {
            fail("Unable to obtain sub-task key");
            return;
        }

        removeGroupPermission(MOVE_ISSUE, Groups.DEVELOPERS);
        gotoIssue(subTaskKey);
        tester.assertLinkNotPresent("move-issue");

        grantGroupPermission(MOVE_ISSUE, Groups.DEVELOPERS);
        gotoIssue(subTaskKey);
        tester.clickLink("move-issue");
        tester.selectOption("issuetype", CUSTOM_SUB_TASK_TYPE_NAME);
        tester.getDialog().setWorkingForm("jiraform");
        tester.submit();

        tester.assertTextPresent("Step 2 of 3");
        tester.getDialog().setWorkingForm("jiraform");
        tester.submit();

        tester.assertTextPresent("Step 4 of 4");
        tester.submit("Move");

        tester.assertTextPresent(CUSTOM_SUB_TASK_TYPE_NAME);
        tester.assertTextPresent("Details");

        // restore settings
        gotoIssue(subTaskKey);
        deleteCurrentIssue();
        administration.subtasks().disable();
    }

//    /**
//     * Tests that field layout schemes can be enforced on sub tasks with required fields
//     */
//    public void subTaskWithFieldSchemeRequired()
//    {
//        log("Sub Task Create: Enforce Sub Tasks on a field layout scheme");
//        administration.subtasks().enable();
//        associateFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"5");
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"6");
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"7");
//
//        gotoIssue(PROJECT_HOMOSAP_KEY + "-1");
//        clickLink("create-subtask");
//        assertTextPresent("Create Sub-Task Issue");
//        selectOption("issuetype", SUB_TASK_DEFAULT_TYPE);
//        submit();
//        setFormElement("summary","test summary");
//        submit();
//
//        assertTextPresent("Step 2 of 2");
//        assertTextPresent("Component/s is required");
//        assertTextPresent("Affects Version/s is required");
//        assertTextPresent("Fix Version/s is required");
//
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"5");
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"6");
//        setRequiredFieldsOnEnterprise(FIELD_SCHEME_NAME,"7");
//        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
//        administration.subtasks().disable();
//    }
//
//    /**
//     * Tests that field layout schemes can be enforced on sub tasks with hidden field
//     */
//    public void subTaskWithFieldSchemeHidden()
//    {
//        log("Sub Task Create: Enforce Sub Tasks on a field layout scheme");
//        administration.subtasks().enable();
//        associateFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
//        setHiddenFieldsOnEnterprise(FIELD_SCHEME_NAME,"5");
//        setHiddenFieldsOnEnterprise(FIELD_SCHEME_NAME,"6");
//        setHiddenFieldsOnEnterprise(FIELD_SCHEME_NAME,"7");
//
//        gotoIssue(PROJECT_HOMOSAP_KEY + "-1");
//        clickLink("create-subtask");
//        assertTextPresent("Create Sub-Task Issue");
//        selectOption("issuetype", SUB_TASK_DEFAULT_TYPE);
//        submit();
//
//        assertFormElementNotPresent("components");
//        assertFormElementNotPresent("versions");
//        assertFormElementNotPresent("fixVersions");
//
//        setShownFieldsOnEnterprise(FIELD_SCHEME_NAME,"5");
//        setShownFieldsOnEnterprise(FIELD_SCHEME_NAME,"6");
//        setShownFieldsOnEnterprise(FIELD_SCHEME_NAME,"7");
//        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
//        administration.subtasks().disable();
//    }
}
