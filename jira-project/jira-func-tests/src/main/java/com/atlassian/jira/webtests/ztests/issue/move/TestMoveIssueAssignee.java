package com.atlassian.jira.webtests.ztests.issue.move;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.ExpectedRow;
import net.sourceforge.jwebunit.ExpectedTable;
import net.sourceforge.jwebunit.WebTester;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Tests that the assignee field is set appropriately on Move Issue.
 * In particular, if the assignee is not valid in the new Project.
 */
@WebTest({Category.FUNC_TEST, Category.MOVE_ISSUE })
public class TestMoveIssueAssignee extends FuncTestCase
{
    protected void setUpTest()
    {
        this.administration.restoreData("TestMoveIssueAssignee.xml");
    }

    public void testMoveSingleIssueSameWorkflow() throws Exception
    {
        // Simple case - assignee is valid in new Project, so leave alone
        navigation.issue().viewIssue("RAT-24");
        tester.clickLink("move-issue");
        tester.selectOption("pid", "Porcine");
        assertions.getTextAssertions().assertTextPresent("Step 1 of 4");
        tester.submit("Next >>");
        assertions.getTextAssertions().assertTextPresent("Step 3 of 4");
        tester.submit("Next >>");
        assertions.getTextAssertions().assertTextPresent("Step 4 of 4");
        tester.submit("Move");
        assertions.getViewIssueAssertions().assertAssignee("Mahatma Gandhi");

        // Interesting case - assignee is NOT valid in new Project, so we reassign automatically
        navigation.issue().viewIssue("RAT-23");
        tester.clickLink("move-issue");
        tester.selectOption("pid", "Canine");
        assertions.getTextAssertions().assertTextPresent("Step 1 of 4");
        tester.submit("Next >>");
        assertions.getTextAssertions().assertTextPresent("Step 3 of 4");
        tester.submit("Next >>");
        assertions.getTextAssertions().assertTextPresent("Step 4 of 4");
        // Assert the Confirmation table
        WebTable move_confirm_table = tester.getDialog().getWebTableBySummaryOrId("move_confirm_table");
        // Assert row 4: |Assignee|Mahatma Gandhi|Murray|
        assertEquals("Cell (4, 0) in table 'move_confirm_table' should be 'Assignee'.", "Assignee", move_confirm_table.getCellAsText(4, 0).trim());
        assertEquals("Cell (4, 1) in table 'move_confirm_table' should be 'Mahatma Gandhi'.", "Mahatma Gandhi", move_confirm_table.getCellAsText(4, 1).trim());
        assertEquals("Cell (4, 2) in table 'move_confirm_table' should be 'Murray'.", "Murray", move_confirm_table.getCellAsText(4, 2).trim());
        tester.submit("Move");
        assertions.getViewIssueAssertions().assertAssignee("Murray");

        // Stupid case - assignee is NOT valid in new Project, but automatic assignee is not valid either
        navigation.issue().viewIssue("RAT-22");
        tester.clickLink("move-issue");
        tester.selectOption("pid", "Bovine");
        assertions.getTextAssertions().assertTextPresent("Step 1 of 4");
        tester.submit("Next >>");
        assertions.getTextAssertions().assertTextPresent("Step 3 of 4");
        tester.submit("Next >>");
        assertions.getTextAssertions().assertTextPresent("Step 3 of 4");
        tester.assertTextPresent("The default assignee does NOT have ASSIGNABLE permission");
    }
}
