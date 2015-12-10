package com.atlassian.jira.webtests.ztests.issue.move;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import net.sourceforge.jwebunit.ExpectedRow;
import net.sourceforge.jwebunit.ExpectedTable;

/**
 * Tests whether we prompt the user for a new Security Level correctly on various move operations.
 * <p>
 * These include:
 * <ul>
 * <li>Move</li>
 * <li>Bulk Move</li>
 * <li>Bulk Migrate</li>
 * <li>Convert Issue to Subtask and vice versa</li>
 * </ul>
 * </p>
 *
 * @see <a href="http://jira.atlassian.com/browse/JRA-14253">JRA-14253</a>
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.MOVE_ISSUE, Category.SECURITY, Category.USERS_AND_GROUPS })
public class TestPromptUserForSecurityLevelOnMove extends FuncTestCase
{

    protected void setUpTest()
    {
        administration.restoreData("TestPromptUserForSecurityLevelOnMove.xml");

    }

    public void testMoveIssueType_Issue_RequiresSecurity()
    {
        // This issue currently has no Security and the new Issue type requires Security to be set.

        // Goto Issue
        navigation.issueNavigator().displayAllIssues();
        navigation.issue().viewIssue("RAT-10");
        tester.clickLink("move-issue");
        tester.assertTextPresent("Choose the project and issue type to move to");

        // Move to Issue Type "New Feature" which requires a Security Level
        // Select 'New Feature' from drop-down 'issuetype'.
        tester.selectOption("issuetype", "New Feature");
        tester.submit("Next >>");
        tester.assertTextPresent("Update the fields of the issue to relate to the new project.");
        // Assert that the 'security' select box is present and has value 'Level Mouse' (this is the default).
        tester.assertOptionEquals("security", "Level Mouse");
    }

    public void testMoveIssueType_Issue_DoesntRequireSecurity()
    {
        // This issue currently has no Security but the new Issue type doesn't require Security to be set.

        // Goto Issue
        navigation.issueNavigator().displayAllIssues();
        navigation.issue().viewIssue("RAT-10");
        tester.clickLink("move-issue");
        tester.assertTextPresent("Choose the project and issue type to move to");

        // Move to Issue Type "Improvement" which doesn't require a Security Level
        // Select 'Improvement' from drop-down 'issuetype'.
        tester.selectOption("issuetype", "Improvement");
        tester.submit("Next >>");
        tester.assertTextPresent("Update the fields of the issue to relate to the new project.");
        // Assert that we don't prompt the user with a drop down for Security Level.
        tester.assertFormElementNotPresent("security");
        tester.assertTextNotPresent("Security Level");
    }

    public void testMoveIssueType_Subtask_RequiresSecurity()
    {
        // In this test, we move a subtask to a subtask type that has a field configuration that says
        // "requires security level".
        // Because it is a subtask, it should not prompt the user for Security Level.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().displayAllIssues();
        // Click Link 'RAT-14'.
        tester.clickLinkWithText("RAT-14");
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");
        tester.checkCheckbox("operation", "move.subtask.type.operation.name");
        // Click the "Next" submit button:
        tester.submit("Next >>");
        // Click the "Next >>" submit button:
        tester.submit("Next >>");
        tester.assertTextPresent("Update the fields of the sub-task to relate to the new sub-task type.");
        // Assert that we don't prompt the user with a drop down for Security Level.
        tester.assertFormElementNotPresent("security");
        tester.assertTextPresent("Security Level");
        tester.assertTextPresent("The security level of subtasks is inherited from parents.");
    }

    public void testBulkMoveIssueType_Issue_RequiresSecurity()
    {
        // This issue currently has no Security and the new Issue type requires Security to be set.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10050", "on");
        // Click the "Next" submit button:
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        // Click the "Next" submit button:
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types");
        // Select 'New Feature' from drop-down '10022_1_issuetype'.
        tester.selectOption("10022_1_issuetype", "New Feature");
        // Click the "Next" submit button:
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project 'Rattus' - Issue Type 'New Feature'");
        tester.assertTextPresent("Security Level");
        // Assert that the 'security' select box is present
        tester.assertFormElementPresent("security");

        // TODO: Really, this should show the default value, but this seems to be broken  see  	JRA-14291
//        // Assert that the 'security' select box is present and has value 'Level Mouse' (this is the default).
//        tester.assertOptionEquals("security", "Level Mouse");
    }

    public void testBulkMoveIssueType_Issue_DoesntRequireSecurity()
    {
        // This issue currently has no Security but the new Issue type doesn't require Security to be set.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10050", "on");
        // Click the "Next" submit button:
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        // Click the "Next" submit button:
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types");
        // Select 'Improvement' from drop-down '10022_1_issuetype'.
        tester.selectOption("10022_1_issuetype", "Improvement");
        // Click the "Next" submit button:
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project 'Rattus' - Issue Type 'Improvement'");
        // Assert that we don't prompt the user with a drop down for Security Level.
        tester.assertFormElementNotPresent("security");
        tester.assertTextNotPresent("Security Level");
    }

    public void testBulkMoveIssueType_Subtask_RequiresSecurity()
    {
        // In this test, we move a subtask to a subtask type that has a field configuration that says
        // "requires security level".
        // Because it is a subtask, it should not prompt the user for Security Level.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10060", "on");
        // Click the "Next" submit button:
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        // Click the "Next" submit button:
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types");
        // Select 'Secure Sub-task' from drop-down '10022_5_10022_issuetype'.
        tester.selectOption("10022_5_10022_issuetype", "Secure Sub-task");
        // Click the "Next" submit button:
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project 'Rattus' - Issue Type 'Secure Sub-task'");
//        // Assert that we don't prompt the user with a drop down for Security Level.
//        tester.assertFormElementNotPresent("security");
//        tester.assertTextNotPresent("Security Level");
        // Assert that we don't prompt the user with a drop down for Security Level.
        tester.assertFormElementNotPresent("security");
        tester.assertTextPresent("Security Level");
        tester.assertTextPresent("The security level of subtasks is inherited from parents.");
    }

    public void testMoveProject_Issue_RequiresSecurity()
    {
        // in this Test we move an issue which has a blank Security Level to a new Project where Security Level is a required field.
        // In this case, the move operation should ask for the new Security Level.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().displayAllIssues();
        // Select an issue with a subtask
        // Click Link 'RAT-13'.
        tester.clickLinkWithText("RAT-13");
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");
        // Select 'Canine' from drop-down 'pid'.
        tester.selectOption("10022_1_pid", "Canine");
        // Click the "Next >>" submit button:
        tester.submit("Next");
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project");
        tester.assertTextPresent("Security Level");
        tester.assertFormElementPresent("security");
        // Select 'Level Amber' from drop-down 'security'.
        tester.selectOption("security", "Level Amber");
        // Click the "Next >>" submit button:
        tester.submit("Next");
        tester.submit("Next");
        // Now we shouldn't need to ask questions about the subtask, so we should be on the Confirm screen.
        tester.assertTextPresent("Step 2 of 2");
    }

    public void testMoveProject_Issue_DoesntRequireSecurity()
    {
        // In this Test we move an issue which has a blank Security Level to a new Project where Security Level is not a required field.
        // In this case, the move operation should not ask for a new Security Level.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().displayAllIssues();
        // Click Link 'RAT-13'.
        tester.clickLinkWithText("RAT-13");
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");
        // Select 'Porcine' from drop-down 'pid'.
        tester.selectOption("10022_1_pid", "Porcine");
        // Click the "Next >>" submit button:
        tester.submit("Next");
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project");
        // We don't need to change the Security Level.
        tester.assertTextNotPresent("Security");
        tester.assertFormElementNotPresent("security");
    }

    public void testMoveProject_Issue_SecurityLevelInvalid()
    {
        // In this test we move an issue with a Security Level set to a new Project where that Security Level is
        // not allowed.
        // We should get prompted to change the Security Level for the parent issue, but not for its subtask.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().displayAllIssues();
        // Click Link 'RAT-12'.
        tester.clickLinkWithText("RAT-12");
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");
        // Select 'Canine' from drop-down 'pid'.
        tester.selectOption("10022_1_pid", "Canine");
        // Click the "Next >>" submit button:
        tester.submit("Next");
        tester.submit("Next");
        tester.assertTextPresent("Security Level");
        tester.assertFormElementPresent("security");
        // Select 'Level Green' from drop-down 'security'.
        tester.selectOption("security", "Level Green");
        // Click the "Next >>" submit button:
        tester.submit("Next");
        tester.submit("Next");
        tester.assertTextPresent("Confirm");
    }

    public void testMoveProject_Issue_SecurityLevelNotAvailableToUser()
    {
        // TODO: Uncomment this test when we fix JRA-14323

        // In this test we move an issue with a Security Level set to a new Project where that Security Level is
        // not available to the user.
        // We should get prompted to change the Security Level for the parent issue, but not for its subtask.

//        // Display all issues in the Issue Navigator.
//        navigation.issueNavigator().displayAllIssues();
//        // Go to RAT-20 issue
//        tester.clickLinkWithText("RAT-20");
//        // Click Link 'Move' (id='move_issue').
//        tester.clickLink("move-issue");
//        // Select 'Bovine' from select box 'pid'.
//        tester.selectOption("pid", "Bovine");
//        // The Bovine (COW) project uses the same Security Scheme as RAT (RatSecurityScheme)
//        // But admin user is in the Developer Project Role for Rattus, but not for Bovine, and therefore has no access
//        // to "Level KingRat" in the new Project.
//        tester.submit("Next >>");
//        // Assert that the value of select-one component 'security' = "10026". - this is Level Mouse - the default.
//        tester.assertFormElementEquals("security", "10026");
//
//        tester.submit("Next >>");
//        tester.assertTextPresent("Confirm the move with all of the details you have just configured.");
//        // Assert the table 'move_confirm_table'
//        final ExpectedTable expectedTable = new ExpectedTable();
//        expectedTable.appendRow(new ExpectedRow(new String[] {"", "Original Value (before move)", "New Value (after move)"}));
//        expectedTable.appendRow(new ExpectedRow(new String[] {"Project", "Rattus", "Bovine"}));
//        expectedTable.appendRow(new ExpectedRow(new String[] {"Type", "Bug", "Bug"}));
//        expectedTable.appendRow(new ExpectedRow(new String[] {"Security Level", "Level KingRat", "Level Mouse"}));
//        tester.assertTableEquals("move_confirm_table", expectedTable);
    }

    public void testMoveProject_Issue_SecurityLevelAvailableToUser()
    {
        // Do a Move where the Security Level value exists and is valid and available to the user in the new project.
        // Basically we want to run the testMoveProject_Issue_SecurityLevelNotAvailableToUser() test, but change the security first.

        // Go to RAT-20
        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().displayAllIssues();
        tester.clickLinkWithText("RAT-20");
        // Edit the issue and change the security level
        tester.clickLink("edit-issue");
        // Select 'Level Mouse' from select box 'security'.
        tester.selectOption("security", "Level Mouse");
        tester.submit("Update");

        // Now check that we don't prompt the user any more...
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");
        // Select 'Bovine' from select box 'pid'.
        tester.selectOption("10022_1_pid", "Bovine");
        // The Bovine (COW) project uses the same Security Scheme as RAT (RatSecurityScheme)
        tester.submit("Next");
        tester.submit("Next");
        // Assert that the value of select-one component 'security' = "10026". - this is Level Mouse - the default.
        tester.assertFormElementNotPresent("security");

        tester.submit("Next");
        tester.submit("Next");
        tester.assertTextPresent("Confirm");

        // Assert the table 'move_confirm_table'
        final ExpectedTable expectedTable = new ExpectedTable();
        expectedTable.appendRow(new ExpectedRow(new String[] {"Issue Targets", "Issue Targets"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"Target Project", "Bovine"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"Target Issue Type", "Bug"}));
        tester.assertTableEquals("move_confirm_table", expectedTable);
    }

    public void testBulkMoveProject_Issue_RequiresSecurity()
    {
        // In this Test we move an issue which has a blank Security Level to a new Project where Security Level is not a required field.
        // In this case, the move operation should not ask for a new Security Level.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().bulkEditAllIssues();
        // Choose RAT-13
        tester.checkCheckbox("bulkedit_10050", "on");
        tester.submit("Next");

        tester.assertTextPresent("Choose the operation you wish to perform");
        // Move
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types");
        // Select 'Canine' from select box '10022_1_pid'.
        tester.selectOption("10022_1_pid", "Canine");

        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types for Sub-Tasks");
        // Select 'Secure Sub-task' from select box '10022_5_10020_issuetype'.
        tester.selectOption("10022_5_10020_issuetype", "Secure Sub-task");
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project 'Canine' - Issue Type 'Bug'");
        tester.assertTextPresent("Security Level");
        // Assert that the select-one component 'security' is present.
        tester.assertFormElementPresent("security");

        // Select 'Level Red' from select box 'security'.
        tester.selectOption("security", "Level Red");
        tester.submit("Next");

        // Now we are on the page for Subtasks fields
        tester.assertTextPresent("Update Fields for Target Project 'Canine' - Issue Type 'Secure Sub-task'");
        tester.assertTextPresent("Fix Version/s");
        tester.assertTextPresent("Affects Version/s");
//        // Subtasks should not prompt for Security Level
//        tester.assertTextNotPresent("Security Level:");
//        tester.assertFormElementNotPresent("security");
        // Assert that we don't prompt the user with a drop down for Security Level.
        tester.assertFormElementNotPresent("security");
        tester.assertTextPresent("Security Level");
        tester.assertTextPresent("The security level of subtasks is inherited from parents.");
    }

    public void testBulkMoveProject_Issue_DoesntRequireSecurity()
    {
        // In this Test we move an issue which has a blank Security Level to a new Project where Security Level is a required field.
        // In this case, the move operation should ask for a new Security Level.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().bulkEditAllIssues();
        // Choose RAT-13
        tester.checkCheckbox("bulkedit_10050", "on");
        tester.submit("Next");

        tester.assertTextPresent("Choose the operation you wish to perform");
        // Move
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types");
        // Select 'Canine' from select box '10022_1_pid'.
        tester.selectOption("10022_1_pid", "Porcine");

        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types for Sub-Tasks");
        // Select 'Secure Sub-task' from select box '10022_5_10021_issuetype'.
        tester.selectOption("10022_5_10021_issuetype", "Secure Sub-task");
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project 'Porcine' - Issue Type 'Bug'");
        // Security is not required - so we shouldn't have a field for it.
        tester.assertTextNotPresent("Security Level");
        // Assert that the select-one component 'security' is present.
        tester.assertFormElementNotPresent("security");

        tester.submit("Next");

        // Now we are on the page for Subtasks fields
        tester.assertTextPresent("Update Fields for Target Project 'Porcine' - Issue Type 'Secure Sub-task'");
        tester.assertTextPresent("Fix Version/s");
        tester.assertTextPresent("Affects Version/s");
        // Subtasks should not prompt for Security Level
        tester.assertTextNotPresent("Security Level");
        tester.assertFormElementNotPresent("security");
    }

    public void testBulkMoveProject_Issue_SecurityLevelInvalid()
    {
        // In this test we move an issue with a Security Level set to a new Project where that Security Level is
        // not allowed.
        // We should get prompted to change the Security Level for the parent issue, but not for its subtask.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().bulkEditAllIssues();
        // Select RAT-12
        tester.checkCheckbox("bulkedit_10040", "on");
        tester.submit("Next");
        tester.assertTextPresent("Choose the operation you wish to perform");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types");
        // Select 'Canine' from select box '10022_1_pid'.
        tester.selectOption("10022_1_pid", "Canine");
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types for Sub-Tasks");
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project 'Canine' - Issue Type 'Bug'");
        tester.assertTextPresent("Security Level");
        // Assert that the select-one component 'security' is present.
        tester.assertFormElementPresent("security");
        // Select 'Level Green' from select box 'security'.
        tester.selectOption("security", "Level Green");
        tester.submit("Next");
        // Now we are on the fields screen for subtasks.
        tester.assertTextPresent("Update Fields for Target Project 'Canine' - Issue Type 'Sub-task'");
//        tester.assertTextNotPresent("Security Level:");
//        tester.assertFormElementNotPresent("security");
        // Assert that we don't prompt the user with a drop down for Security Level.
        tester.assertFormElementNotPresent("security");
        tester.assertTextPresent("Security Level");
        tester.assertTextPresent("The security level of subtasks is inherited from parents.");

    }

    public void testBulkMoveProject_Issue_SecurityLevelNotAvailableToUser()
    {
        // TODO: Uncomment this test when we fix JRA-14323

        // In this test we move an issue with a Security Level set to a new Project where that Security Level is
        // not available to the user.
        // We should get prompted to change the Security Level for the parent issue, but not for its subtask.

//        // Display all issues in the Issue Navigator.
//        navigation.issueNavigator().displayAllIssues();
//        // Click Link 'all 12 issue(s)' (id='bulkedit_all').
//        tester.clickLink("bulkedit_all");
//        tester.checkCheckbox("bulkedit_10100", "on");
//        tester.submit("Next");
//        tester.assertTextPresent("Choose the operation you wish to perform");
//        tester.checkCheckbox("operation", "bulk.move.operation.name");
//        tester.submit("Next");
//        tester.assertTextPresent("Select Projects and Issue Types");
//        // Select 'Bovine' from select box '10022_1_pid'.
//        tester.selectOption("10022_1_pid", "Bovine");
//        tester.submit("Next");
//        tester.assertTextPresent("Select Projects and Issue Types for Sub-Tasks");
//        // Select 'Sub-task' from select box '10022_6_10000_issuetype'.
//        tester.selectOption("10022_6_10000_issuetype", "Sub-task");
//        tester.submit("Next");
//        tester.assertTextPresent("Update Fields for Target Project 'Bovine' - Issue Type 'Bug'");
//        // We SHOULD see the Security Level Drop Down here.
//        tester.assertTextPresent("Security Level:");
//        // Assert that the select-one component 'security' is present.
//        tester.assertFormElementPresent("security");
//        tester.submit("Next");
//        tester.assertTextPresent("Update Fields for Target Project 'Bovine' - Issue Type 'Sub-task'");
//        // Update fields for subtask - should not prompt for Security Level.
//        tester.assertTextNotPresent("Security Level:");
//        tester.assertFormElementNotPresent("security");
    }

    public void testBulkMoveProject_Issue_SecurityLevelAvailableToUser()
    {
        // Do a Move where the Security Level value exists and is valid and available to the user in the new project.
        // Basically we want to run the testMoveProject_Issue_SecurityLevelNotAvailableToUser() test, but change the security first.

        // Go to RAT-20
        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().displayAllIssues();
        tester.clickLinkWithText("RAT-20");
        // Edit the issue and change the security level
        tester.clickLink("edit-issue");
        // Select 'Level Mouse' from select box 'security'.
        tester.selectOption("security", "Level Mouse");
        tester.submit("Update");
        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10100", "on");
        tester.submit("Next");
        tester.assertTextPresent("Choose the operation you wish to perform");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types");
        // Select 'Bovine' from select box '10022_1_pid'.
        tester.selectOption("10022_1_pid", "Bovine");
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types for Sub-Tasks");
        // Select 'Sub-task' from select box '10022_6_10000_issuetype'.
        tester.selectOption("10022_6_10000_issuetype", "Sub-task");
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project 'Bovine' - Issue Type 'Bug'");
        // We SHOULD NOT see the Security Level Drop Down here.
        tester.assertTextNotPresent("Security Level:");
        tester.assertFormElementNotPresent("security");
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project 'Bovine' - Issue Type 'Sub-task'");
        // Update fields for subtask - should not prompt for Security Level.
        tester.assertTextNotPresent("Security Level:");
        tester.assertFormElementNotPresent("security");
    }

    public void testBulkMigrate_SecurityNotRequired()
    {
        // We change the "Issue type scheme" of the project Rattus so that we start a Bulk Migration.
        // We migrate bug Issue type to an Issue type that does not require Security Level, and so there should be no
        // prompt to the user.
        // At the same time we migrate "Subtask" to "Secure Subtask". This subtask type will try to require a Security
        // field, but we test JIRA is smart enough not to prompt for the subtask - it will only get Security Level
        // from its parent.

        // Click Link 'ADMINISTRATION' (id='admin_link').
        Long projectId = backdoor.project().getProjectId("RAT");
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);

        tester.checkCheckbox("createType", "chooseScheme");
        // Select 'Bugless Scheme' from select box 'schemeId'.
        tester.selectOption("schemeId", "Bugless Scheme");
        tester.assertTextPresent("Select Issue Type Scheme for project Rattus");
        tester.submit(" OK ");
        tester.assertTextPresent("Issue Type Migration: Overview (Step 1 of 6)");
        tester.assertTextPresent("Some issues have issue types that are no longer applicable. You will need to move these issues to another issue type.");
        tester.submit("nextBtn");
        text.assertTextPresent(new WebPageLocator(tester), "Select a new issue type for issues with current issue type Bug in project Rattus");
        // Select 'Task' from select box 'issuetype'.
        tester.selectOption("issuetype", "Task");
        tester.submit("nextBtn");
        text.assertTextPresent(new WebPageLocator(tester), "Update fields for issues with current issue type Bug in project Rattus.");
        tester.assertTextPresent("All field values will be retained.");
        tester.submit("nextBtn");
        text.assertTextPresent(new WebPageLocator(tester), "Select a new issue type for issues with current issue type Sub-task in project Rattus");
        tester.submit("nextBtn");
        text.assertTextPresent(new WebPageLocator(tester), "Update fields for issues with current issue type Sub-task in project Rattus.");
        // Assert that we don't prompt the user with a drop down for Security Level.
        tester.assertFormElementNotPresent("security");
        tester.assertTextPresent("Security Level");
        tester.assertTextPresent("The security level of subtasks is inherited from parents.");
//        tester.assertTextPresent("All field values will be retained.");
    }

    public void testBulkMigrate_SecurityRequired()
    {
        // We change the "Issue type scheme" of the project Rattus so that we start a Bulk Migration.
        // We migrate bug Issue type to Issue type "New Feature" which requires Security Level, and so we should
        // prompt the user for a new value for the Security LEvel field for migrated issues.
        // At the same time we migrate "Subtask" to "Secure Subtask". This subtask type will try to require a Security
        // field, but we test JIRA is smart enough not to prompt for the subtask - it will only get Security Level
        // from its parent.

        // Click Link 'ADMINISTRATION' (id='admin_link').
        Long projectId = backdoor.project().getProjectId("RAT");
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);
        tester.checkCheckbox("createType", "chooseScheme");
        // Select 'Bugless Scheme' from select box 'schemeId'.
        tester.selectOption("schemeId", "Bugless Scheme");
        tester.submit(" OK ");
        tester.assertTextPresent("Issue Type Migration: Overview (Step 1 of 6)");
        tester.submit("nextBtn");
        text.assertTextPresent(new WebPageLocator(tester), "Select a new issue type for issues with current issue type Bug in project Rattus");
        // Select 'New Feature' from select box 'issuetype'.
        tester.selectOption("issuetype", "New Feature");
        tester.submit("nextBtn");
        text.assertTextPresent(new WebPageLocator(tester), "Update fields for issues with current issue type Bug in project Rattus.");
        tester.assertTextPresent("Security Level");
        // Assert that the select-one component 'security' is present.
        tester.assertFormElementPresent("security");
        // Select 'Level Mouse' from select box 'security'.
        tester.selectOption("security", "Level Mouse");
        tester.submit("nextBtn");
        text.assertTextPresent(new WebPageLocator(tester), "Select a new issue type for issues with current issue type Sub-task in project Rattus");
        tester.submit("nextBtn");
        text.assertTextPresent(new WebPageLocator(tester), "Update fields for issues with current issue type Sub-task in project Rattus.");
//        tester.assertTextPresent("All field values will be retained.");
        // Assert that we don't prompt the user with a drop down for Security Level.
        tester.assertFormElementNotPresent("security");
        tester.assertTextPresent("Security Level");
        tester.assertTextPresent("The security level of subtasks is inherited from parents.");
    }

    public void testConvertIssueToSubtask()
    {
        // We will take a standard issue with no security set and convert it to a subtask of type "Secure Subtask".
        // This subtask will try to make Security Level a required Field, but we shouldn't prompt the user on a subtask
        // ever.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().displayAllIssues();
        tester.clickLinkWithText("RAT-10");
        // Click Link 'Convert' (id='issue-to-subtask').
        tester.clickLink("issue-to-subtask");
        // Set parent to RAT-13
        tester.setFormElement("parentIssueKey", "RAT-13");
        tester.assertTextPresent("Convert Issue to Sub-task: RAT-10");
        // Select 'Secure Sub-task' from select box 'issuetype'.
        tester.selectOption("issuetype", "Secure Sub-task");
        tester.submit("Next >>");
        tester.assertTextPresent("Convert Issue to Sub-task: RAT-10");
        text.assertTextPresent(new WebPageLocator(tester), "Step 3 of 4: Update the fields of the issue to relate to the new issue type ...");
        tester.assertTextPresent("All fields will be updated automatically.");
    }

    public void testConvertSubtaskWithoutSecurityToIssueThatRequiresSecurity()
    {
        // We will take a subtask without security set and convert it to a standard issue of type "New Feature".
        // Security field is required, and the user should be prompted.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().displayAllIssues();
        tester.clickLinkWithText("RAT-14");
        // Click Link 'Convert' (id='subtask-to-issue').
        tester.clickLink("subtask-to-issue");
        tester.assertTextPresent("Convert Sub-task to Issue: RAT-14");
        // Select 'New Feature' from select box 'issuetype'.
        tester.selectOption("issuetype", "New Feature");
        tester.submit("Next >>");
        text.assertTextPresent(new WebPageLocator(tester), "Step 3 of 4: Update the fields of the issue to relate to the new issue type ...");
        tester.assertTextPresent("Security Level:");
        // Assert that the select-one component 'security' is present.
        tester.assertFormElementPresent("security");
    }

    public void testConvertSubtaskWithSecurityToIssueThatRequiresSecurity()
    {
        // We will take a subtask with security set and convert it to a standard issue of type "Task".
        // Security field is not required, and the user should not be prompted.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().displayAllIssues();
        tester.clickLinkWithText("RAT-14");
        // Click Link 'Convert' (id='subtask-to-issue').
        tester.clickLink("subtask-to-issue");
        tester.assertTextPresent("Convert Sub-task to Issue: RAT-14");
        // Select 'New Feature' from select box 'issuetype'.
        tester.selectOption("issuetype", "Task");
        tester.submit("Next >>");
        text.assertTextPresent(new WebPageLocator(tester), "Step 3 of 4: Update the fields of the issue to relate to the new issue type ...");
        tester.assertTextNotPresent("Security Level:");
        tester.assertFormElementNotPresent("security");
    }
}
