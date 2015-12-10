package com.atlassian.jira.webtests.ztests.issue.move;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebTable;

/**
 * Tests for moving issues with custom fields between projects.
 * With global custom fields, and fields only in the original project.
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.COMMENTS, Category.MOVE_ISSUE, Category.TIME_TRACKING })
public class TestDeleteHiddenFieldOnMove extends FuncTestCase
{
    public void setUpTest()
    {
        administration.restoreData("TestDeleteHiddenFieldOnMove.xml");
    }

    public void testSingleMove()
    {
        navigation.issue().viewIssue("COW-13");
        // Click Link 'Move' (id='move_issue').
        tester.clickLink("move-issue");
        // Select 'Rattus' from select box 'pid'.
        tester.selectOption("10010_1_pid", "Rattus");
        tester.selectOption("10010_1_issuetype", "Bug");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        tester.submit("Next");
        // Assert the table 'move_confirm_table'
        WebTable move_confirm_table = tester.getDialog().getWebTableBySummaryOrId("move_confirm_table");
        assertions.getTableAssertions().assertTableContainsRowOnce(move_confirm_table, new String[] {"Target Project", "Rattus"});
        assertions.getTableAssertions().assertTableContainsRowOnce(move_confirm_table, new String[] {"Target Issue Type", "Bug"});
        WebTable removed_fields = tester.getDialog().getWebTableBySummaryOrId("removed_fields_table");
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] {"Fix Version/s"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] {"Stuff"});
        assertions.getTableAssertions().assertTableContainsRow(removed_fields, new String[] {"Milk"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] {"Priority"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] {"Resolution"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] {"Environment"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] {"Affects Version/s"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] {"Description"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] {"Milk2"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] {"Assignee"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] { "Reporter"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] {"Time Tracking"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields, new String[] {"Due Date"});

        // Commit the Move
        tester.submit("Next");

        // Now we are on the View Issue Page again - lets check the Change history.
        assertChangesAfterMove();
    }

    public void testBulkMove()
    {
        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10012", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        // Display all issues in the Issue Navigator.
        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10012", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        // Select 'Rattus' from select box '10010_1_pid'.
        tester.selectOption("10010_1_pid", "Rattus");
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types for Sub-Tasks");
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project 'Rattus' - Issue Type 'Bug'");
        tester.assertTextPresent("All field values will be retained.");
        tester.submit("Next");
        tester.assertTextPresent("Update Fields for Target Project 'Rattus' - Issue Type 'Sub-task'");
        tester.assertTextPresent("All field values will be retained.");
        tester.submit("Next");

        tester.assertTextPresent("Confirmation");
        // Assert the table 'removed_fields_table'
        WebTable removed_fields_table = tester.getDialog().getWebTableBySummaryOrId("removed_fields_table");
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Removed Fields"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Time Tracking"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Assignee"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Fix Version/s"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Resolution"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Reporter"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Affects Version/s"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Environment"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Description"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Priority"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Stuff"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Due Date"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Milk2"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Component/s"});
        assertions.getTableAssertions().assertTableContainsRowOnce(removed_fields_table, new String[] {"Milk"}, true);
        // Confirm the Bulk Move.
        tester.submit("Next");

        assertChangesAfterMove();

    }

    private void assertChangesAfterMove()
    {
        navigation.issue().viewIssue("RAT-15");

        // Now we are on the View Issue Page again - lets check the Change history.
        tester.clickLinkWithText(ISSUE_TAB_CHANGE_HISTORY);
        // Assert the table 'changehistory_10070'
        WebTable changehistory_10070 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10070");
        assertEquals("Expected table to have 17 rows, but found " + changehistory_10070.getRowCount(), 17, changehistory_10070.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Project", "Bovine [ 10010 ]", "Rattus [ 10000 ]"});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Key", "COW-13", "RAT-15"});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Assignee", "Daisy Doolittle [ daisy ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Fix Version/s", "1.1 [ 10002 ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Resolution", "Fixed [ 1 ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Reporter", "admin [ admin ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Affects Version/s", "1.0 [ 10000 ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Environment", "the environment", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Priority", "Major [ 3 ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Description", "Hello", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Stuff", "stuff value 1", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Due Date", "27/Oct/09", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Milk2", "milk value 2", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Component/s", "Horns [ 10001 ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Milk", "milk value 1", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Original Estimate", "1 week, 3 days [ 230400 ]", ""});
        // TODO: The Remaining Estimate is WRONG! -  	 JRA-15865
//        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10070, new String[] {"Remaining Estimate", "1 week, 1 day [ 172800 ]", ""});

        // Go to the subtask
        navigation.issue().viewIssue("RAT-16");
        // Assert the table 'changehistory_10071'
        WebTable changehistory_10071 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10071");
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Reporter", "admin [ admin ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Assignee", "Daisy Doolittle [ daisy ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Description", "Hello again", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Environment", "sub env", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Priority", "Major [ 3 ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Resolution", "Fixed [ 1 ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Due Date", "27/Oct/09", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Project", "Bovine [ 10010 ]", "Rattus [ 10000 ]"});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Key", "COW-14", "RAT-16"});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Fix Version/s", "2.0 [ 10001 ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Affects Version/s", "1.1 [ 10002 ]", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Stuff", "sub stuff 1", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Milk2", "sub milk 2", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Milk", "sub milk 1", ""});
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Component/s", "Horns [ 10001 ]", ""});
        // TODO: The Remaining Estimate and Original Estimate is WRONG! -  	 JRA-15865
//        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Original Estimate", "???", ""});
//        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10071, new String[] {"Remaining Estimate", "??", ""});
    }
}
