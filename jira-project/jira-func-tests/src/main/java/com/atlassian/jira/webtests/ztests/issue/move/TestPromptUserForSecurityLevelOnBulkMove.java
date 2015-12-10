package com.atlassian.jira.webtests.ztests.issue.move;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebTable;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * This test case was specifically written to test JRA-14350.
 * <p>
 * JRA-14350: Doing a Bulk Move can fail to update the security level of subtasks when the Parent Issue changes security.
 * </p>
 * <p>
 * The data in TestPromptUserForSecurityLevelOnMove did not replicate the defect.
 * </p>
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.MOVE_ISSUE, Category.SECURITY })
public class TestPromptUserForSecurityLevelOnBulkMove extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestPromptUserForSecurityLevelOnBulkMove.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testBulkMoveIssueType_Issue_RequiresSecurity()
    {
        // This issue currently has no Security and the new Issue type requires Security to be set.

        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().bulkEditAllIssues();
        // Select the "Parent" issue
        tester.checkCheckbox("bulkedit_10010", "on");
        tester.submit("Next");
        // Do a Move operation.
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        // Move to 'monkey' project
        // Select 'monkey' from select box '10000_1_pid'.
        tester.selectOption("10000_1_pid", "monkey");
        tester.submit("Next");
        // Leave the subtask issue type.
        tester.submit("Next");

        // the new project requires a non-null Security Level.
        // Select 'Developers' from select box 'security'.
        tester.selectOption("security", "Developers");
        tester.submit("Next");
        // now we are on the subtask screen.
        tester.assertTextPresent("Update Fields for Target Project 'monkey' - Issue Type 'Sub-task'");
        tester.assertTextPresent("The security level of subtasks is inherited from parents.");

        tester.submit("Next");
        tester.assertTextPresent("Confirmation");
        // TODO: Assert page data is correct here.

        // Hit the Confirm button to finish the wizard.
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();
        // Now we are back on the navigator. Assert the Security Levels
        // Assert the cells in table 'issuetable'.
        WebTable issuetable = tester.getDialog().getWebTableBySummaryOrId("issuetable");
        // Assert row 0: |T|Key|Summary|Assignee|Reporter|Pr|Status|Res|Created|Updated|Due|Security|
        assertEquals("cell(0, 0) in table 'issuetable' should be 'T'", "T", issuetable.getCellAsText(0, 0).trim());
        assertEquals("cell(0, 1) in table 'issuetable' should be 'Key'", "Key", issuetable.getCellAsText(0, 1).trim());
        assertEquals("cell(0, 2) in table 'issuetable' should be 'Summary'", "Summary", issuetable.getCellAsText(0, 2).trim());
        assertEquals("cell(0, 3) in table 'issuetable' should be 'Assignee'", "Assignee", issuetable.getCellAsText(0, 3).trim());
        assertEquals("cell(0, 4) in table 'issuetable' should be 'Reporter'", "Reporter", issuetable.getCellAsText(0, 4).trim());
        assertEquals("cell(0, 5) in table 'issuetable' should be 'P'", "P", issuetable.getCellAsText(0, 5).trim());
        assertEquals("cell(0, 6) in table 'issuetable' should be 'Status'", "Status", issuetable.getCellAsText(0, 6).trim());
        assertEquals("cell(0, 7) in table 'issuetable' should be 'Resolution'", "Resolution", issuetable.getCellAsText(0, 7).trim());
        assertEquals("cell(0, 8) in table 'issuetable' should be 'Created'", "Created", issuetable.getCellAsText(0, 8).trim());
        assertEquals("cell(0, 9) in table 'issuetable' should be 'Updated'", "Updated", issuetable.getCellAsText(0, 9).trim());
        assertEquals("cell(0, 10) in table 'issuetable' should be 'Due'", "Due", issuetable.getCellAsText(0, 10).trim());
        assertEquals("cell(0, 11) in table 'issuetable' should be 'Security'", "Security", issuetable.getCellAsText(0, 11).trim());
        // Assert row 1: ||MKY-10|MKY-9 Child|Administrator|Administrator||Open|UNRESOLVED|31/Jul/08|01/Aug/08|||
        assertEquals("cell(1, 1) in table 'issuetable' should be 'MKY-10'", "MKY-10", issuetable.getCellAsText(1, 1).trim());
        assertEquals("cell(1, 2) in table 'issuetable' should be 'MKY-9 Child'", "MKY-9\n                        Child", issuetable.getCellAsText(1, 2).trim());
        assertEquals("cell(1, 3) in table 'issuetable' should be '" + ADMIN_FULLNAME + "'", ADMIN_FULLNAME, issuetable.getCellAsText(1, 3).trim());
        assertEquals("cell(1, 4) in table 'issuetable' should be '" + ADMIN_FULLNAME + "'", ADMIN_FULLNAME, issuetable.getCellAsText(1, 4).trim());
        assertEquals("cell(1, 5) in table 'issuetable' should be ''", "", issuetable.getCellAsText(1, 5).trim());
        assertEquals("cell(1, 6) in table 'issuetable' should be 'Open'", "Open", issuetable.getCellAsText(1, 6).trim());
        assertEquals("cell(1, 7) in table 'issuetable' should be 'Unresolved'", "Unresolved", issuetable.getCellAsText(1, 7).trim());
        assertEquals("cell(1, 8) in table 'issuetable' should be '31/Jul/08'", "31/Jul/08", issuetable.getCellAsText(1, 8).trim());
        // Updated date will depend on when the test is run.
        //assertEquals("cell(1, 9) in table 'issuetable' should be '01/Aug/08'", "01/Aug/08", issuetable.getCellAsText(1, 9).trim());
        assertEquals("cell(1, 10) in table 'issuetable' should be ''", "", issuetable.getCellAsText(1, 10).trim());
        assertEquals("cell(1, 11) in table 'issuetable' should be 'Developers'", "Developers", issuetable.getCellAsText(1, 11).trim());
        // Assert row 2: ||MKY-9|Parent|Administrator|Administrator||Open|UNRESOLVED|31/Jul/08|01/Aug/08||Developers|
        assertEquals("cell(2, 1) in table 'issuetable' should be 'MKY-9'", "MKY-9", issuetable.getCellAsText(2, 1).trim());
        assertEquals("cell(2, 2) in table 'issuetable' should be 'Parent'", "Parent", issuetable.getCellAsText(2, 2).trim());
        assertEquals("cell(2, 3) in table 'issuetable' should be '" + ADMIN_FULLNAME + "'", ADMIN_FULLNAME, issuetable.getCellAsText(2, 3).trim());
        assertEquals("cell(2, 4) in table 'issuetable' should be '" + ADMIN_FULLNAME + "'", ADMIN_FULLNAME, issuetable.getCellAsText(2, 4).trim());
        assertEquals("cell(2, 5) in table 'issuetable' should be ''", "", issuetable.getCellAsText(2, 5).trim());
        assertEquals("cell(2, 6) in table 'issuetable' should be 'Open'", "Open", issuetable.getCellAsText(2, 6).trim());
        assertEquals("cell(2, 7) in table 'issuetable' should be 'Unresolved'", "Unresolved", issuetable.getCellAsText(2, 7).trim());
        assertEquals("cell(2, 8) in table 'issuetable' should be '31/Jul/08'", "31/Jul/08", issuetable.getCellAsText(2, 8).trim());
        // Updated date will depend on when the test is run.
        //assertEquals("cell(2, 9) in table 'issuetable' should be '01/Aug/08'", "01/Aug/08", issuetable.getCellAsText(2, 9).trim());
        assertEquals("cell(2, 10) in table 'issuetable' should be ''", "", issuetable.getCellAsText(2, 10).trim());
        assertEquals("cell(2, 11) in table 'issuetable' should be 'Developers'", "Developers", issuetable.getCellAsText(2, 11).trim());

        // Go to the Browse Issue page for the child so we can chekc the DB value as well
        tester.clickLinkWithText("MKY-10");
        tester.assertTextPresent("Security Level");
        tester.assertTextPresent("Developers");
    }
}
