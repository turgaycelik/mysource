package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.NavigationImpl;
import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.functest.framework.assertions.AssertionsImpl;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.ISSUES })
public class TestBulkMoveIssuesForEnterprise extends BulkChangeIssues
{
    public static final String CONFIRMATION_TEXT = "Please confirm that the correct changes have been entered.";
    private static final String SAME_FOR_ALL = "sameAsBulkEditBean";
    private static final String BULK_EDIT_KEY = "10000_1_";

    protected static final String TARGET_PROJECT_ID = BULK_EDIT_KEY + "pid";
    protected static final String TARGET_ISSUE_TYPE_ID = BULK_EDIT_KEY + "issuetype";

    protected static final String STD_ISSUE_SELECTION = "Select Projects and Issue Types";
    protected static final String ENT_SUB_TASK_SELECTION = " Move Issues: Select Projects and Issue Types for Sub-Tasks";
    protected static final String ENT_UPDATE_FIELDS_PROJECT = "Update Fields for Target Project '";
    protected static final String ENT_UPDATE_FIELDS_ISSUE_TYPE = "' - Issue Type '";
    protected static final String ENT_UPDATE_FIELDS_END = "'";

    public TestBulkMoveIssuesForEnterprise(String name)
    {
        super(name);
    }

    // JRA-17011: Move a task and change the assignee. The task does not have any children.
    public void testBulkMoveChangeAssigneeParent() throws Exception
    {
        restoreData("TestBulkMoveAssigneeChange.xml");

        final Navigation nav = new NavigationImpl(tester, getEnvironmentData());
        final IssueNavigatorNavigation navigatorNavigation = nav.issueNavigator();
        final Assertions assertions = new AssertionsImpl(tester, getEnvironmentData(), nav, locator);

        //Select the issue to move.
        startBulkMoveForIssue(navigatorNavigation, 10012);

        //Select the project to move the issue into.
        setTargetProject("Target");

        //Make sure that the assignee drop down is correct.
        assertTargetAssignees();
        next();

        //Check the confirmation screen.
        assertConfirmationScreen();
        next();

        waitAndReloadBulkOperationProgressPage();

        //Make sure the issue has the right assignee.
        nav.issue().viewIssue("SRC-5");
        assertions.getViewIssueAssertions().assertAssignee("Target User");
    }

    // JRA-17011: Move a task within the same project. Make sure we don't get any option to change the assignee.
    public void testBulkMoveChangeAssignee() throws Exception
    {
        restoreData("TestBulkMoveAssigneeChange.xml");

        final Navigation nav = new NavigationImpl(tester, getEnvironmentData());
        final IssueNavigatorNavigation navigatorNavigation = nav.issueNavigator();
        final Assertions assertions = new AssertionsImpl(tester, getEnvironmentData(), nav, locator);

        //Select the issue to move.
        startBulkMoveForIssue(navigatorNavigation, 10000);

        //Move the issue into the same project.
        setTargetProject("Source");

        //Make sure that the assignee drop down is not there.
        assertRetainAssignees();
        next();

        //Check the confirmation screen.
        assertConfirmationScreen();
        next();

        waitAndReloadBulkOperationProgressPage();

        //Make sure the issue has the right assignee.
        nav.issue().viewIssue("SRC-5");
        assertions.getViewIssueAssertions().assertAssignee("Source User");
    }

    // JRA-17011: Move a task and change the assignee. The task has a child that also needs to be migrated.
    public void testBulkMoveChangeAssigneeParentAndChild() throws Exception
    {
        restoreData("TestBulkMoveAssigneeChange.xml");

        final Navigation nav = new NavigationImpl(tester, getEnvironmentData());
        final IssueNavigatorNavigation navigatorNavigation = nav.issueNavigator();
        final Assertions assertions = new AssertionsImpl(tester, getEnvironmentData(), nav, locator);

        //Select the issue to move.
        startBulkMoveForIssue(navigatorNavigation, 10000);

        //Select the project to move into.
        setTargetProject("Target");

        //Check the sub-task project select.
        assertSelectProjectScreen();
        next();

        //Make sure that the assignee drop down is correct.
        assertTargetAssignees();
        next();

        //Make sure that the assignee drop down is correct for sub-tasks.
        assertTargetAssignees();
        next();

        //Check the confirmation screen.
        assertConfirmationScreen();
        next();

        waitAndReloadBulkOperationProgressPage();

        //Make sure the issues has the right assignee.
        nav.issue().viewIssue("SRC-1");
        assertions.getViewIssueAssertions().assertAssignee("Target User");

        nav.issue().viewIssue("SRC-2");
        assertions.getViewIssueAssertions().assertAssignee("Target User");
    }

    // JRA-17011: Move a task and change the assignee. Only the child needs to be migrated.
    public void testBulkMoveChangeAssigneeChildOnly() throws Exception
    {
        restoreData("TestBulkMoveAssigneeChange.xml");

        final Navigation nav = new NavigationImpl(tester, getEnvironmentData());
        final IssueNavigatorNavigation navigatorNavigation = nav.issueNavigator();
        final Assertions assertions = new AssertionsImpl(tester, getEnvironmentData(), nav, locator);

        //Select the issue to move.
        startBulkMoveForIssue(navigatorNavigation, 10010);

        //Select the project to move into.
        setTargetProject("Target");

        //Check the sub-task project select.
        assertSelectProjectScreen();
        next();

        //This should be retained.
        assertRetainAssignees();
        next();

        //Make sure that the assignee drop down is correct for sub-tasks.
        assertTargetAssignees();
        next();

        //Check the confirmation screen.
        assertConfirmationScreen();
        next();

        waitAndReloadBulkOperationProgressPage();

        //Make sure the migration works.
        nav.issue().viewIssue("SRC-3");
        assertions.getViewIssueAssertions().assertAssignee("Both User");

        nav.issue().viewIssue("SRC-4");
        assertions.getViewIssueAssertions().assertAssignee("Target User");
    }

    // JRA-17011: Move a task and change the assignee. The parent needs to be migrated while the child does not.
    public void testBulkMoveChangeAssigneeParentOnly() throws Exception
    {
        restoreData("TestBulkMoveAssigneeChange.xml");

        final Navigation nav = new NavigationImpl(tester, getEnvironmentData());
        final IssueNavigatorNavigation navigatorNavigation = nav.issueNavigator();
        final Assertions assertions = new AssertionsImpl(tester, getEnvironmentData(), nav, locator);

        //Select the issue to move.
        startBulkMoveForIssue(navigatorNavigation, 10013);

        //Select the project to move into.
        setTargetProject("Target");

        //Check the sub-task project select.
        assertSelectProjectScreen();
        next();

        //Make sure that the assignee drop down is correct.
        assertTargetAssignees();
        next();

        //Assignee should be retained for the sub-task.
        assertRetainAssignees();
        next();

        //Check the confirmation screen.
        assertConfirmationScreen();
        next();

        waitAndReloadBulkOperationProgressPage();

        //Make sure the move works.
        nav.issue().viewIssue("SRC-6");
        assertions.getViewIssueAssertions().assertAssignee("Target User");

        nav.issue().viewIssue("SRC-7");
        assertions.getViewIssueAssertions().assertAssignee("Both User");
    }

    // This test illustrates JRA-13937 "Bulk Move does not update the Security Level of subtasks".
    public void testBulkMoveSubTasks()
    {
        restoreData("TestBulkMoveSubTasks.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        //find all issues
        displayAllIssues();
        assertTextSequence(new String[] {"RAT-4", "No more milk", "Level Mouse"});
        assertTextSequence(new String[] {"RAT-3", "Get new milk bucket", "Level Mouse"});

        //bulk edit and move RAT-4 to project Bovine.
        bulkChangeIncludeAllPages();
        checkCheckbox("bulkedit_10033", "on");
        next();
        checkCheckbox("operation", "bulk.move.operation.name");
        next();
        assertTextSequence(new String[] {"Improvement", "Rattus"});
        selectOption("10022_4_pid", "Bovine");
        next();
        assertTextSequence(new String[] {"Sub-task", "Rattus"});
        next();
        assertTextSequence(new String[] {"Improvement", "Rattus"});
        assertTextPresent("Security Level");
        assertFormElementPresent("security");
        next();
        assertTextSequence(new String[] {"Sub-task", "Rattus"});
        // The security level of subtasks is inherited from parents.
        assertTextSequence(new String[] {"Security Level", "The security level of subtasks is inherited from parents."});
        next();
        assertTextPresent("Step 4 of 4: Confirmation");
        next();

        waitAndReloadBulkOperationProgressPage();

        //check that the security level has been removed, and that the original issues have become
        //COW issues.
        assertTextPresent("Issue Navigator");
        assertTextNotPresent("RAT-4");
        assertTextNotPresent("RAT-3");
        assertTextNotPresent("Level Mouse");
        assertTextSequence(new String[] {"COW-35", "No more milk"});
        assertTextSequence(new String[] {"COW-34", "Get new milk bucket"});
    }

    public void testBulkMoveIssuesForEnterprise()
    {
        restoreData("TestBulkMoveIssuesForEnterprise.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        log("Bulk Move - Tests for Enterprise Version");
        _testBulkMoveParentIssuesProjectAndIssueType();
        _testBulkMoveSubTaskAndItsParentSelectParents();
        _testMoveSessionTimeouts();
        // uses the blankprojects.xml backup
        _testMoveIssueTypeWithAndWithoutSubtasks();
    }

    public void _testMoveSessionTimeouts()
    {
        log("Bulk Move - Test that you get redirected to the session timeout page when jumping into the wizard");

        beginAt("secure/views/bulkedit/BulkMigrateDetails.jspa");
        verifyAtSessionTimeoutPage();
        beginAt("secure/views/bulkedit/BulkMigrateChooseSubTaskContext!default.jspa?subTaskPhase=false");
        verifyAtSessionTimeoutPage();
        beginAt("secure/views/bulkedit/BulkMigrateSetFields!default.jspa?subTaskPhase=false");
        verifyAtSessionTimeoutPage();
        beginAt("secure/views/bulkedit/BulkMigrateSetFields!default.jspa?subTaskPhase=true");
        verifyAtSessionTimeoutPage();
        beginAt("secure/views/bulkedit/BulkMigrateSetFields.jspa");
        verifyAtSessionTimeoutPage();
        beginAt("secure/views/bulkedit/BulkMigratePerform.jspa");
        verifyAtSessionTimeoutPage();
    }

    private void verifyAtSessionTimeoutPage()
    {
        assertTextPresent("Your session timed out while performing bulk operation on issues.");
    }

    // See JRA-9067
    public void _testMoveIssueTypeWithAndWithoutSubtasks()
    {
        log("Bulk Move - Move multiple issue types where at least one (but not all) issue types do not have sub tasks");
        getAdministration().restoreBlankInstance();
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);
        String issueKey1 = addIssue(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_BUG, "issueKey1", PRIORITY_MAJOR, null, null, null, null, "", "", null, null, null);
        String issueKey2 = addIssue(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_IMPROVEMENT, "issueKey2", PRIORITY_MAJOR, null, null, null, null, "", "", null, null, null);
        String subTaskKey1 = addSubTaskToIssue(issueKey1, ISSUE_TYPE_SUB_TASK, "subtask1", "");

        resetFields();
        displayAllIssues();
//        displayIssuesInProject("PROJECT_HOMOSAP");
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationBulkMove();
        assertTextPresent(STD_ISSUE_SELECTION);

        selectOption("10001_1_pid", PROJECT_HOMOSAP);
        selectOption("10001_1_issuetype", ISSUE_TYPE_BUG);

        selectOption("10001_4_pid", PROJECT_HOMOSAP);
        selectOption("10001_4_issuetype", ISSUE_TYPE_IMPROVEMENT);

        clickOnNext();
        clickOnNext();

        //retain all values
        assertTextPresent(ENT_UPDATE_FIELDS_PROJECT + PROJECT_HOMOSAP + ENT_UPDATE_FIELDS_ISSUE_TYPE + ISSUE_TYPE_BUG + ENT_UPDATE_FIELDS_END);
        clickOnNext();
        assertTextPresent(ENT_UPDATE_FIELDS_PROJECT + PROJECT_HOMOSAP + ENT_UPDATE_FIELDS_ISSUE_TYPE + ISSUE_TYPE_SUB_TASK + ENT_UPDATE_FIELDS_END);
        clickOnNext();
        assertTextPresent(ENT_UPDATE_FIELDS_PROJECT + PROJECT_HOMOSAP + ENT_UPDATE_FIELDS_ISSUE_TYPE + ISSUE_TYPE_IMPROVEMENT + ENT_UPDATE_FIELDS_END);
        clickOnNext();

        isStepConfirmation();
        clickOnNext();
        waitAndReloadBulkOperationProgressPage();

        gotoIssue(issueKey1);
        assertLinkPresentWithText(PROJECT_HOMOSAP);
        assertLinkNotPresentWithText(PROJECT_MONKEY);
        gotoIssue(issueKey2);
        assertLinkPresentWithText(PROJECT_HOMOSAP);
        assertLinkNotPresentWithText(PROJECT_MONKEY);
        gotoIssue(subTaskKey1);
        assertLinkPresentWithText(PROJECT_HOMOSAP);
        assertLinkNotPresentWithText(PROJECT_MONKEY);
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
    }

    public void _testBulkMoveParentIssuesProjectAndIssueType()
    {
        log("Bulk Move - Test bulk move Parent issues - Move the parents Project and Issue type");

        String issueKey1 = "NDT-1";
        String issueId1 = "10000";

        String issueKey2 = "HSP-1";
        String issueId2 = "10001";

        String issueKey3 = "HSP-2";
        String issueId3 = "10002";

        String issueKey4 = "NDT-2";
        String issueId4 = "10003";

        String issueKey5 = "MKY-1";
        String issueId5 = "10004";

        activateSubTasks();
        displayAllIssues();
        bulkChangeIncludeAllPages();
        isStepChooseIssues();
        selectCheckbox("bulkedit_" + issueId1);
        selectCheckbox("bulkedit_" + issueId2);
        assertCheckboxNotSelected("bulkedit_" + issueId3);
        assertCheckboxNotSelected("bulkedit_" + issueId4);
        selectCheckbox("bulkedit_" + issueId5);
        clickOnNext();
        chooseOperationBulkMove();
        assertTextPresent("Target Project");
        checkCheckbox(SAME_FOR_ALL, BULK_EDIT_KEY);
        selectOption(TARGET_PROJECT_ID, PROJECT_HOMOSAP);
        selectOption(TARGET_ISSUE_TYPE_ID, ISSUE_TYPE_IMPROVEMENT);
        clickOnNext();
        assertTextPresent(FIELDS_UPDATE_AUTO);
        clickOnNext();

        clickOnNext();
        waitAndReloadBulkOperationProgressPage();

        //check that the included issues with different projects moved
        assertLastChangeHistoryIs(issueKey1, "Project", PROJECT_NEO, PROJECT_HOMOSAP);
        assertLastChangeHistoryIs(issueKey5, "Project", PROJECT_MONKEY, PROJECT_HOMOSAP);
        //check that the parents selected has its issue type changed to Improvement
        assertLastChangeHistoryIs(issueKey1, "Type", ISSUE_TYPE_NEWFEATURE, ISSUE_TYPE_IMPROVEMENT);
        assertLastChangeHistoryIs(issueKey2, "Type", ISSUE_TYPE_BUG, ISSUE_TYPE_IMPROVEMENT);
        //check that the included issue with the same project did not make any changes
        assertLastChangeNotMadeToField(issueKey2, "Project");
        assertLastChangeNotMadeToField(issueKey5, "Type");
        //check excluded parents and all subtasks remain unchanged
        assertNoChangesForIssue(issueKey3);
        assertNoChangesForIssue(issueKey4);
    }

    /**
     * This tests bulk moving issues with sub-tasks. This verfies that even if the sub-tasks were selected bulk move will
     * unselect them
     */
    public void _testBulkMoveSubTaskAndItsParentSelectParents()
    {
        log("Bulk Move - Test bulk move subtask and its parent - Move the parents Project and Issue type");

        String issueKey1 = "NDT-3";
        String issueId1 = "10010";

        String subTaskKey1 = "NDT-4";
        String subTaskId1 = "10011";

        String subTaskKey2 = "NDT-5";
        String subTaskId2 = "10012";

        String issueKey2 = "HSP-3";
        String issueId2 = "10013";

        String issueKey3 = "HSP-4";
        String issueId3 = "10014";

        String subTaskKey3 = "HSP-5";
        String subTaskId3 = "10015";

        String issueKey4 = "NDT-6";
        String issueId4 = "10016";

        activateSubTasks();
        displayAllIssues();
        bulkChangeIncludeAllPages();

        isStepChooseIssues();
        selectCheckbox("bulkedit_" + issueId1);
        selectCheckbox("bulkedit_" + subTaskId1);
        selectCheckbox("bulkedit_" + subTaskId2);
        selectCheckbox("bulkedit_" + issueId2);
        assertCheckboxNotSelected("bulkedit_" + issueId3);
        assertCheckboxNotSelected("bulkedit_" + subTaskId3);
        assertCheckboxNotSelected("bulkedit_" + issueId4);
        clickOnNext();

        chooseOperationBulkMove();

        // Editing the parent
        assertTextPresent("Target Project");
        checkCheckbox(SAME_FOR_ALL, BULK_EDIT_KEY);
        selectOption(TARGET_PROJECT_ID, PROJECT_HOMOSAP);
        selectOption(TARGET_ISSUE_TYPE_ID, ISSUE_TYPE_IMPROVEMENT);
        clickOnNext();

        // Selecting context for sub-tasks
        assertTextPresent("Select Projects and Issue Types for Sub-Tasks");
        selectOption("10001_5_10000_issuetype", ISSUE_TYPE_SUB_TASK);
        clickOnNext();

        // No fields change required for parents
        assertTextPresent(FIELDS_UPDATE_AUTO);
        clickOnNext();

        // No fields change required for sub-tasks
        assertTextPresent(FIELDS_UPDATE_AUTO);
        clickOnNext();

        assertTextPresent(CONFIRMATION_TEXT);
        clickOnNext();

        waitAndReloadBulkOperationProgressPage();

        //check that the included issues with different projects moved
        assertLastChangeHistoryIs(issueKey1, "Project", PROJECT_NEO, PROJECT_HOMOSAP);
        assertLastChangeHistoryIs(subTaskKey1, "Project", PROJECT_NEO, PROJECT_HOMOSAP);
        assertLastChangeHistoryIs(subTaskKey2, "Project", PROJECT_NEO, PROJECT_HOMOSAP);
        //check that the parents selected has its issue type changed to Improvement
        assertLastChangeHistoryIs(issueKey1, "Type", ISSUE_TYPE_NEWFEATURE, ISSUE_TYPE_IMPROVEMENT);
        assertLastChangeHistoryIs(issueKey2, "Type", ISSUE_TYPE_BUG, ISSUE_TYPE_IMPROVEMENT);
        //check that the included issue with the same project did not make any changes
        assertLastChangeNotMadeToField(issueKey2, "Project");
        //check excluded parents and all subtasks remain unchanged
        assertNoChangesForIssue(issueKey3);
        assertNoChangesForIssue(subTaskKey3);
        assertNoChangesForIssue(issueKey4);
        deactivateSubTasks();
    }

    public void testBulkMoveIssuesWithSecurityLevels()
    {
        restoreData("TestBulkMoveIssuesForEnterpriseIssueLevelSecurity.xml");

        String issueKey1 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_BUG, "Summary for Bug", null, null, null, null, null, "", "", null, null, null);
        String issueKey2 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_NEWFEATURE, "Summary for New Feature", null, null, null, null, null, "", "", null, null, null);

        displayAllIssues();

        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationBulkMove();

        assertTextPresent("Select Projects and Issue Types");
        selectOption("10000_1_pid", "monkey");
        selectOption("10000_2_pid", "monkey");
        next();

        // Set the security Levels for the Issues
        selectOption("security", "Medium");
        next();
        selectOption("security", "High");
        next();

        // Assert that both the issues have their security level set on the confirmation page
        assertTextPresent("Medium");
        assertTextPresent("High");
        next();

        waitAndReloadBulkOperationProgressPage();

        // Check that the issues have the correct Security Level Set
        gotoIssue(issueKey1);
        assertTextPresent("Medium");
        gotoIssue(issueKey2);
        assertTextPresent("High");

        //assert the index reflects the bulk move (to project monkey) and retained the security information
        assertIndexedFieldCorrect("//item", EasyMap.build("key", "MKY-6", "security", "Medium"), null, "MKY-6");
        assertIndexedFieldCorrect("//item", EasyMap.build("key", "MKY-7", "security", "High"), null, "MKY-7");

    }

    public void testBulkMoveWithCustomFieldContextBehaviour()
    {
        restoreData("TestBulkMoveIssuesForEnterpriseCustomFieldContextBehaviour.xml");

        displayAllIssues();
        bulkChangeIncludeAllPages();

        // Select two Monkey issues to move.
        checkCheckbox("bulkedit_10011", "on");
        checkCheckbox("bulkedit_10000", "on");
        clickOnNext();

        // Choose the 'Bulk Move' operation
        checkCheckbox("operation", "bulk.move.operation.name");
        clickOnNext();

        // Choose The target project 'homosapien'
        selectOption("10001_1_pid", "homosapien");
        clickOnNext();

        // Select the target issue type
        selectOption("10001_5_10000_issuetype", "Sub-task");
        clickOnNext();

        // Choose the default status
        clickOnNext();

        // Select a value for the Man - Bug Custom field
        assertTextPresent("Man Bug Custom Field");
        selectOption("customfield_10002", "Yes");
        clickOnNext();

        // Assert that all other fields will remain the same
        assertTextPresent("All field values will be retained");
        clickOnNext();

        // Click next on the confirmation screen
        clickOnNext();

        waitAndReloadBulkOperationProgressPage();

        // go to the issue and confirm the fields have been updated
        displayAllIssues();
        clickLinkWithText("Another Monkey Bug!");

        text.assertTextPresent(new IdLocator(tester, "rowForcustomfield_10001"), "Monkey Option");
        text.assertTextPresent(new IdLocator(tester, "rowForcustomfield_10002"), "Yes");
    }

    public void testBulkMoveRetainWarning()
    {
        restoreData("TestBulkMoveRetainWarningTest.xml");

        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        chooseOperationBulkMove();
        assertTextPresent(STD_ISSUE_SELECTION);
        selectOption(TARGET_PROJECT_ID, PROJECT_MONKEY);
        navigation.clickOnNext();

        // JRA-8248: retain checkbox is no longer enabled for Component and Version fields; however there is no way to
        // check if a form control is disabled using JWebUnit, so just assert that the message we used to display is
        // no longer there
        assertTextNotPresent("Issues not in the project <strong>" + PROJECT_MONKEY + "</strong> will not retain values for <strong>Fix Version/s</strong>");
        assertTextNotPresent("Issues not in the project <strong>" + PROJECT_MONKEY + "</strong> will not retain values for <strong>Affects Version/s</strong>");
        assertTextNotPresent("Issues not in the project <strong>" + PROJECT_MONKEY + "</strong> will not retain values for <strong>Component/s</strong>");

        assertTextNotPresent("Issues not in the project <strong>" + PROJECT_MONKEY + "</strong> will not retain values for <strong>Description</strong>");

    }

    //test bulk moving issues and bulk changing the statuses to map to the new project
    public void testBulkMoveAndChangeStatus()
    {
        restoreData("TestBulkMoveMapWorkflows.xml");
        assertIndexedFieldCorrect("//item", EasyMap.build("key", "HSP-13", "type", "Bug", "status", "Totally Open", "summary", "bugs3"), EasyMap.build("key", "MKY-13"), "HSP-13");

        //let's move all homosapien issues to monkey and map "Totally Open" to "Open"
        showIssues("project=homosapien");
        bulkChangeIncludeAllPages();
        checkAllIssuesOnPage();
        moveIssuesToMonkey();

        assertStatusMapped("MKY-11"); //make sure that "totally open" is now "open"

        String movedIssueKey = getIssueKeyWithSummary("bugs3", "MKY");
        //it should now just be "open" in the index
        assertIndexedFieldCorrect("//item", EasyMap.build("key", movedIssueKey, "type", "Bug", "status", "Open", "summary", "bugs3"), EasyMap.build("key", "HSP-14"), movedIssueKey);
    }

    private void assertStatusMapped(String issue)
    {
        gotoIssue(issue);
        assertTextPresent("Open");
        assertTextNotPresent("Totally Open");
    }

    private void moveIssuesToMonkey()
    {
        next();
        checkCheckbox("operation", "bulk.move.operation.name");
        next();
        selectOption("10000_1_pid", "monkey");

        //accept all the default mappings (including the suggested "totally open" to "open"
        next();
        next();
        next();
        next();
        waitAndReloadBulkOperationProgressPage();
    }



    private void checkAllIssuesOnPage()
    {
        checkCheckbox("bulkedit_10022", "on");
        checkCheckbox("bulkedit_10023", "on");
        checkCheckbox("bulkedit_10024", "on");
        checkCheckbox("bulkedit_10025", "on");
    }

    private void next()
    {
        submit("Next");
    }

    private void startBulkMoveForIssue(final IssueNavigatorNavigation navigatorNavigation, long id)
    {
        navigatorNavigation.displayAllIssues();

        //Goto bulk edit.
        bulkChangeIncludeAllPages();

        assertTextSequence(new String[]{"Bulk Operation",  "Choose Issues"});
        //select the issue that we want.
        checkCheckbox("bulkedit_" + id, "on");
        next();

        assertTextSequence(new String[]{"Bulk Operation", "Choose Operation"});
        checkCheckbox("operation", "bulk.move.operation.name");
        next();
    }

    private void setTargetProject(final String option)
    {
        assertSelectProjectScreen();
        selectOption("10010_1_pid", option);
        next();
    }

    private void assertConfirmationScreen()
    {
        assertTextSequence(new String[]{"Move Issues", "Confirmation"});
    }

    private void assertSelectProjectScreen()
    {
        assertTextSequence(new String[]{"Move Issues", "Select Projects and Issue Types"});
    }

    private void assertRetainAssignees()
    {
        assertTextPresent("All field values will be retained");
    }

    private void assertTargetAssignees()
    {
        assertTextSequence(new String[]{"Move Issues", "Update Fields"});
        //Now check the options to ensure that they are the user's for the target project.

        // TODO - JRADEV-7741 - reinstate (as WebDriver test)
//        assertOptionsEqual("assignee", new String[]{"- Automatic -", "Both User", "Target User"});
    }
}
