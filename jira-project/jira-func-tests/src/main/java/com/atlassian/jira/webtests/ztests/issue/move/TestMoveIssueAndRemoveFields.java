package com.atlassian.jira.webtests.ztests.issue.move;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.table.StrictTextCell;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests moving an issue such that it ends up in a project or Issue Type with hidden fields.
 * <p>
 * This func test was written as part of the fix for JRA-13479.
 * When an issue was moved to an Issue Type or Project and afteer the move there are hidden fields that were not hidden
 * before the move, the values would remain in those fields.
 * </p>
 * <p>
 * The following cases are tested:
 * <ul>
 *   <li>Move an Issue to new Issue Type</li>
 *   <li>Move an Issue to new Project</li>
 *   <li>Bulk Move issues to new Issue Type</li>
 *   <li>Bulk Move issues to new Project</li>
 *   <li>Change a project's Issue Type scheme such that you have to migrate some issues to a new Issue Type.</li>
 * </ul>
 * </p>
 * @since v3.13
 * @see <a href="http://jira.atlassian.com/browse/JRA-13479">JRA-13479</a>
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.ISSUES, Category.MOVE_ISSUE })
public class TestMoveIssueAndRemoveFields extends JIRAWebTest
{
    public TestMoveIssueAndRemoveFields(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("TestMoveIssueAndRemoveFieldsEnterprise.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testMoveIssueType() throws SAXException
    {
        navigation.issue().gotoIssue("RAT-13");
        // Assert the Issue Key
        text.assertTextPresent(locator.id("key-val"), "RAT-13");

        // Assert Priority
        text.assertTextPresent(locator.id("priority-val"), "Minor");
        // Assert Affects Version
        text.assertTextPresent(locator.id("versions-val"), "v1.0");
        // Assert Fix Version/s
        text.assertTextPresent(locator.id("fixfor-val"), "v1.1");
        // Assert Security Level
        text.assertTextPresent(locator.id("security-val"), "Level KingRat");

        // Move the issue
        tester.clickLink("move-issue");
        assertTextPresent("Move Issue");
        assertTextPresent("Select Project and Issue Type");

        // Change the Issue Type to Task.
        // Task uses a different Field Configuration which hides fields including Security Level.
        tester.selectOption("10022_1_issuetype", "Task");
        tester.submit("Next");
        assertTextPresent("Move Issue");
        assertTextPresent("Update Fields for Target Project");

        // Next
        tester.submit("Next");
        assertTextPresent("Move Issue");
        assertTextPresent("Please confirm that the correct changes have been entered");
        // Assert the values will be changed as expected.
        WebTable checkedTable = getTable("move_confirm_table");
        assertTableHasMatchingRow(checkedTable, "Target Issue Type", "Task");
        assertTableHasMatchingRow(checkedTable, "Target Project", "Rattus");
        checkedTable = getTable("removed_fields_table");
        assertTableHasMatchingRow(checkedTable, "Fix Version/s");
        assertTableHasMatchingRow(checkedTable, "Affects Version/s");
        assertTableHasMatchingRow(checkedTable, "Security Level");
        assertTableHasMatchingRow(checkedTable, "Priority");

        // Commit the move.
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();
        // Assert the Issue Key
        text.assertTextPresent(locator.id("key-val"), "RAT-13");
        text.assertTextPresent(locator.id("assignee-val"), "Mahatma Gandhi");
        assertTrue(locator.id("priority-val").getNodes().length == 0);
        assertTrue(locator.id("fixfor-val").getNodes().length == 0);
        assertTrue(locator.id("versions-val").getNodes().length == 0);
        assertTrue(locator.id("security-val").getNodes().length == 0);

        Long projectId = backdoor.project().getProjectId("RAT");
        // But this is not a sufficient test. in the original issue the values were hidden but existed.
        // Change the Field Configuration to show these fields.
        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("Hidden Treasure FC Scheme"));

        tester.gotoPage("/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=" + projectId);

        tester.selectOption("schemeId", "System Default Field Configuration");
        tester.submit("Associate");

        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("System Default Field Configuration"));

        // now go back to the issue screen.
        navigation.issue().gotoIssue("RAT-13");
        // Assert the Issue Key
        text.assertTextPresent(locator.id("key-val"), "RAT-13");

        // Assert Priority missing
        text.assertTextPresent(locator.id("assignee-val"), "Mahatma Gandhi");

        // Priority and Security should not appear at all, as they are still blank
        assertTrue(locator.id("priority-val").getNodes().length == 0);
        assertTrue(locator.id("security-val").getNodes().length == 0);

        // Assert Affects Version is now blank
        text.assertTextPresent(locator.id("versions-val"), "None");

        // Assert Fix Version/s
        text.assertTextPresent(locator.id("fixfor-val"), "None");

        // But the subtask should always have the same Security Level as its parent
        navigation.issue().gotoIssue("RAT-14");
        // Assert the Issue Key
        text.assertTextPresent(locator.id("key-val"), "RAT-14");
        // Assert Priority
        text.assertTextPresent(locator.id("priority-val"), "Minor");
        // Assert Affects Version
        text.assertTextPresent(locator.id("versions-val"), "v1.0");
        // Assert Fix Version/s
        text.assertTextPresent(locator.id("fixfor-val"), "v1.1");
        // Assert Security Level is none
        assertTrue(locator.id("security-val").getNodes().length == 0);

        // go to Navigator to make sure that Lucene has the correct value as well"
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert subtask has changed appropriately "RAT-14"
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask_alternate.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasImageInContext(issueTable, 1, 5, "/images/icons/priorities/minor.png");
        assertEquals("", issueTable.getCellAsText(1, 11).trim());
        assertTableCellHasText("issuetable", 1, 13, "v1.1");
        assertTableCellHasText("issuetable", 1, 14, "v1.0");
        // Assert "RAT-13" is now task and has lost hidden fields.
        assertTableCellHasImageInContext(issueTable, 2, 0, "/images/icons/issuetypes/task.png");
        assertTableCellHasText("issuetable", 2, 1, "RAT-13");
        assertTableCellHasNoImage(issueTable, 2, 5);
        assertEquals("", issueTable.getCellAsText(2, 11).trim());
        assertEquals("", issueTable.getCellAsText(2, 12).trim());
        assertEquals("", issueTable.getCellAsText(2, 13).trim());
        assertEquals("", issueTable.getCellAsText(2, 14).trim());
        // Assert "RAT-12" has a value for the custom field
        assertTableCellHasText("issuetable", 3, 1, "RAT-12");
        assertEquals("asdf", issueTable.getCellAsText(3, 12).trim());
    }

    public void testMoveIssueTypeOnSubtask() throws SAXException
    {
        // If a subtask has a security level (because its parent does) and we move its issue type to one where the
        // security is hidden, then we want to actually KEEP the security level, even though it will be hidden from the
        // user.

        // go to subtask
        navigation.issue().gotoIssue("RAT-14");
        tester.clickLink("move-issue");
        tester.checkCheckbox("operation", "move.subtask.type.operation.name");
        tester.submit("Next >>");
        assertTextPresent("Step 2 of 4");
        assertTextPresent("Choose the sub-task type to move to ...");
        // there is only one alternative sub-task : the "Limited subtask" with hidden fields.
        tester.submit("Next >>");
        assertTextPresent("Step 3 of 4");
        assertTextPresent("Update the fields of the sub-task to relate to the new sub-task type.");

        // go to confirmation page
        tester.submit("Next >>");
        assertTextPresent("Step 4 of 4");
        assertTextPresent("Confirm the move with all of the details you have just configured.");
        WebTable table = getTable("move_confirm_table");
        assertTableHasMatchingRow(table, "Type", "Sub-task", "Limited Subtask");
        // Security should be special and not deleted.
        assertTextNotPresent("Security Level");
        // Other hidden fields should be deleted as usual.
        assertTableHasMatchingRow(table, "Affects Version/s", "v1.0", "");
        assertTableHasMatchingRow(table, "Fix Version/s", "v1.1", "");
        assertTableHasMatchingRow(table, "Priority", "Minor", "");

        // Confirm the move.
        tester.submit("Move");
        assertTextPresent("RAT-14");
        // Security field should not appear (although the value exists - it is still hidden)
        assertTextNotPresent("Security");
        // None of the hidden fields should appear
        text.assertTextNotPresent(locator.id("issuedetails"), "Random Shit");
        text.assertTextNotPresent(locator.id("issuedetails"), "Priority");
        text.assertTextNotPresent(locator.id("issuedetails"), "Fix Version");
        text.assertTextNotPresent(locator.id("issuedetails"), "Affects Version");

        // Now stop hiding fields so we can see if the Security Level remains correctly.
        changeProjectRatToDefaultFieldConfiguration();

        // now go back to the issue screen.
        navigation.issue().gotoIssue("RAT-14");
        // Assert the Issue Key
        text.assertTextPresent(locator.id("key-val"), "RAT-14");
        // Assert Affects Version
        text.assertTextPresent(locator.id("versions-val"), "None");
        // Assert Fix Version/s
        text.assertTextPresent(locator.id("fixfor-val"), "None");
        // Assert Security Level - this value existed even though it was hidden.
        text.assertTextPresent(locator.id("security-val"), "Level KingRat");

        // go to Navigator to make sure that Lucene has the correct value as well
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert "RAT-14"
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertEquals("Level KingRat", issueTable.getCellAsText(1, 11).trim());
        assertTrue(issueTable.getCellAsText(1, 12).trim().equals(""));
        assertTrue(issueTable.getCellAsText(1, 13).trim().equals(""));
    }

    public void testMoveProject() throws SAXException
    {
        navigation.issue().gotoIssue("RAT-13");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "RAT-13");
        // Assert Priority
        text.assertTextPresent(new IdLocator(tester, "priority-val"), "Minor");
        // Assert Affects Version
        text.assertTextPresent(new IdLocator(tester, "versions-val"), "v1.0");
        // Assert Fix Version/s
        text.assertTextPresent(new IdLocator(tester, "fixfor-val"), "v1.1");
        // Assert Security Level
        text.assertTextPresent(new IdLocator(tester, "security-val"), "Level KingRat");

        // Move the issue
        tester.clickLink("move-issue");
        assertTextPresent("Move Issue");
        assertTextPresent("Select Project and Issue Type");

        // Change the Issue Type to Task.
        // Task uses a different Field Configuration which hides fields including Security Level.
        tester.selectOption("10022_1_pid", "Porcine");
        tester.submit("Next");
        assertTextPresent("Move Issue");
        assertTextPresent("Select Projects and Issue Types for Sub-Tasks");

        // Next
        tester.submit("Next");
        assertTextPresent("Move Issue");
        assertTextPresent("Update Fields for Target Project");
        tester.submit("Next");
        tester.submit("Next");
        // Assert the values will be changed as expected.
        WebTable checkedTable = getTable("move_confirm_table");
        assertTableHasMatchingRow(checkedTable, "Target Issue Type", "Bug");
        assertTableHasMatchingRow(checkedTable, "Target Project", "Porcine");
        checkedTable = getTable("removed_fields_table");
        assertTableHasMatchingRow(checkedTable, "Fix Version/s");
        assertTableHasMatchingRow(checkedTable, "Affects Version/s");
        assertTableHasMatchingRow(checkedTable, "Security Level");
        assertTableHasMatchingRow(checkedTable, "Priority");

        // Commit the move.
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "PIG-15");
        // None of the hidden fields should appear
        assertTrue(new IdLocator(tester, "priority-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "fixfor-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "versions-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "security-val").getNodes().length == 0);

        // But this is not a sufficient test. in the original issue the values were hidden but existed.
        // Change the Field Configuration to show these fields.
        Long projectId = backdoor.project().getProjectId("PIG");
        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("Bug is Limited FC Scheme"));

        tester.gotoPage("/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=" + projectId);

        tester.selectOption("schemeId", "System Default Field Configuration");
        tester.submit("Associate");

        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("System Default Field Configuration"));

        // now go back to the issue screen.
        navigation.issue().gotoIssue("PIG-15");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "PIG-15");
        // Priority and Security should not appear at all, as they are still blank
        assertTrue(new IdLocator(tester, "priority-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "security-val").getNodes().length == 0);

        // Check that the subtask had its Security Level removed as well
        navigation.issue().gotoIssue("PIG-16");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "PIG-16");
        // Security should be set to blank in order to be the same as the parent
        assertTextNotPresent("Security");
        assertTextNotPresent("Level KingRat");

        // go to Navigator to make sure that Lucene has the correct value as well
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert subtask has changed appropriately "RAT-14" -> PIG-16
        assertTableCellHasImageInContext(issueTable, 4, 0, "/images/icons/issuetypes/subtask_alternate.png");
        assertTableCellHasText("issuetable", 4, 1, "PIG-16");
        assertTableCellHasImageInContext(issueTable, 4, 5, "/images/icons/priorities/minor.png");
        assertEquals("", issueTable.getCellAsText(4, 11).trim());
        assertEquals("", issueTable.getCellAsText(4, 12).trim());
        assertEquals("", issueTable.getCellAsText(4, 13).trim());
        // Assert "RAT-13" is now PIG-15 and has lost hidden fields.
        assertTableCellHasImageInContext(issueTable, 5, 0, "/images/icons/issuetypes/bug.png");
        assertTableCellHasText("issuetable", 5, 1, "PIG-15");
        assertTableCellHasNoImage(issueTable, 5, 5);
        assertEquals("", issueTable.getCellAsText(5, 11).trim());
        assertEquals("", issueTable.getCellAsText(5, 12).trim());
        assertEquals("", issueTable.getCellAsText(5, 13).trim());
    }

    public void testBulkMoveIssueType() throws SAXException
    {
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert current setup of "RAT-14"
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask_alternate.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasImageInContext(issueTable, 1, 5, "/images/icons/priorities/minor.png");
        assertTableCellHasText("issuetable", 1, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 1, 13, "v1.1");
        assertTableCellHasText("issuetable", 1, 14, "v1.0");
        // Assert current setup of "RAT-13"
        assertTableCellHasImageInContext(issueTable, 2, 0, "/images/icons/issuetypes/bug.png");
        assertTableCellHasText("issuetable", 2, 1, "RAT-13");
        assertTableCellHasImageInContext(issueTable, 2, 5, "/images/icons/priorities/minor.png");
        assertTableCellHasText("issuetable", 2, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 2, 13, "v1.1");
        assertTableCellHasText("issuetable", 2, 14, "v1.0");
        // Assert current setup of "RAT-12"
        assertTableCellHasImageInContext(issueTable, 3, 0, "/images/icons/issuetypes/bug.png");
        assertTableCellHasText("issuetable", 3, 1, "RAT-12");
        assertTableCellHasImageInContext(issueTable, 3, 5, "/images/icons/priorities/blocker.png");
        assertTableCellHasText("issuetable", 3, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 3, 13, "v0.9");
        assertTableCellHasText("issuetable", 3, 14, "v0.1");

        // Start a Bulk Move
        navigation.issueNavigator().bulkEditAllIssues();
        assertTextPresent("Step 1 of 4: Choose Issues");

        // Choose RAT-13 and RAT-12
        tester.checkCheckbox("bulkedit_10050", "on");
        tester.checkCheckbox("bulkedit_10040", "on");
        tester.submit("Next");
        assertTextPresent("Step 2 of 4: Choose Operation");
        assertTextPresent("Choose the operation you wish to perform on the selected <strong>2</strong> issue(s)");

        // Select the Move operation
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        assertTextPresent("Select Projects and Issue Types");

        // Move the Issue Type to Task
        tester.selectOption("10022_1_issuetype", "Task");
        tester.submit("Next");
        assertTextPresent("Move Issues");
        assertTextPresent("Update Fields for Target Project 'Rattus' - Issue Type 'Task'");

        tester.submit("Next");
        assertTextPresent("Confirmation");
        WebTable move_confirm_table = getTable("move_confirm_table");
        assertTableHasMatchingRow(move_confirm_table, "Target Project", "Rattus");
        assertTableHasMatchingRow(move_confirm_table, "Target Issue Type", "Task");
        // Check the removed Fields table.
        WebTable removed_fields_table = getTable("removed_fields_table");
        assertTableHasMatchingRow(removed_fields_table, "Priority");
        assertTableHasMatchingRow(removed_fields_table, "Random Shit");
        assertTableHasMatchingRow(removed_fields_table, "Affects Version/s");
        assertTableHasMatchingRow(removed_fields_table, "Fix Version/s");
        assertTableHasMatchingRow(removed_fields_table, "Security Level");

        // CONFIRM
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();
        // Now we are back on the navigator screen.
        issueTable = getTable("issuetable");
        // Assert subtask has changed appropriately "RAT-14"
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask_alternate.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasImageInContext(issueTable, 1, 5, "/images/icons/priorities/minor.png");
        assertEquals("", issueTable.getCellAsText(1, 11).trim());
        assertTableCellHasText("issuetable", 1, 13, "v1.1");
        assertTableCellHasText("issuetable", 1, 14, "v1.0");
        // Assert "RAT-13" is now task and has lost hidden fields.
        assertTableCellHasImageInContext(issueTable, 2, 0, "/images/icons/issuetypes/task.png");
        assertTableCellHasText("issuetable", 2, 1, "RAT-13");
        assertTableCellHasNoImage(issueTable, 2, 5);
        assertEquals("", issueTable.getCellAsText(2, 11).trim());
        assertEquals("", issueTable.getCellAsText(2, 13).trim());
        assertEquals("", issueTable.getCellAsText(2, 14).trim());
        // Assert "RAT-12"
        assertTableCellHasImageInContext(issueTable, 2, 0, "/images/icons/issuetypes/task.png");
        assertTableCellHasText("issuetable", 3, 1, "RAT-12");
        assertTableCellHasNoImage(issueTable, 3, 5);
        assertEquals("", issueTable.getCellAsText(3, 11).trim());
        assertEquals("", issueTable.getCellAsText(3, 13).trim());
        assertEquals("", issueTable.getCellAsText(3, 14).trim());


        Long projectId = backdoor.project().getProjectId("RAT");
        // Now the fields may be hidden, but actually exist.
        // Change the Field Configuration to show these fields.
        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("Hidden Treasure FC Scheme"));

        tester.gotoPage("/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=" + projectId);

        tester.selectOption("schemeId", "System Default Field Configuration");
        tester.submit("Associate");

        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("System Default Field Configuration"));

        // now go back to the Navigator and assert that the fields really are removed:
        navigation.issueNavigator().displayAllIssues();
        issueTable = getTable("issuetable");
        // Assert subtask has changed appropriately "RAT-14"
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask_alternate.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasImageInContext(issueTable, 1, 5, "/images/icons/priorities/minor.png");
        assertEquals("", issueTable.getCellAsText(1, 11).trim());
        assertTableCellHasText("issuetable", 1, 13, "v1.1");
        assertTableCellHasText("issuetable", 1, 14, "v1.0");
        // Assert "RAT-13" is now task and has lost hidden fields.
        assertTableCellHasImageInContext(issueTable, 2, 0, "/images/icons/issuetypes/task.png");
        assertTableCellHasText("issuetable", 2, 1, "RAT-13");
        assertTableCellHasNoImage(issueTable, 2, 5);
        assertEquals("", issueTable.getCellAsText(2, 11).trim());
        assertEquals("", issueTable.getCellAsText(2, 13).trim());
        assertEquals("", issueTable.getCellAsText(2, 14).trim());
        // Assert "RAT-12"
        assertTableCellHasImageInContext(issueTable, 2, 0, "/images/icons/issuetypes/task.png");
        assertTableCellHasText("issuetable", 3, 1, "RAT-12");
        assertTableCellHasNoImage(issueTable, 3, 5);
        assertEquals("", issueTable.getCellAsText(3, 11).trim());
        assertEquals("", issueTable.getCellAsText(3, 13).trim());
        assertEquals("", issueTable.getCellAsText(3, 14).trim());
    }

    public void testBulkMoveIssueTypeOnSubtask() throws SAXException
    {
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert current setup of "RAT-14"
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask_alternate.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasImageInContext(issueTable, 1, 5, "/images/icons/priorities/minor.png");
        assertTableCellHasText("issuetable", 1, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 1, 13, "v1.1");
        assertTableCellHasText("issuetable", 1, 14, "v1.0");
        // Assert current setup of "RAT-13"
        assertTableCellHasImageInContext(issueTable, 2, 0, "/images/icons/issuetypes/bug.png");
        assertTableCellHasText("issuetable", 2, 1, "RAT-13");
        assertTableCellHasImageInContext(issueTable, 2, 5, "/images/icons/priorities/minor.png");
        assertTableCellHasText("issuetable", 2, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 2, 13, "v1.1");
        assertTableCellHasText("issuetable", 2, 14, "v1.0");

        // Do a bulk edit
        navigation.issueNavigator().bulkEditAllIssues();
        assertTextPresent("Step 1 of 4: Choose Issues");
        // Choose RAT-14 which is a subtask of RAT-13
        checkCheckbox("bulkedit_10060", "on");
        tester.submit("Next");

        assertTextPresent("Step 2 of 4: Choose Operation");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");

        assertTextPresent("Step 3 of 4");
        assertTextPresent("Select Projects and Issue Types");
        // move to a subtask type with hidden fields.
        tester.selectOption("10022_5_10022_issuetype", "Limited Subtask");
        tester.submit("Next");

        assertTextPresent("Update Fields for Target Project 'Rattus' - Issue Type 'Limited Subtask'");
        tester.submit("Next");
        // Confirmation Screen
        assertTextPresent("Confirmation");
        assertTextPresent("Step 4 of 4");
        // Check the removed Fields table.
        WebTable removed_fields_table = getTable("removed_fields_table");
        assertTableHasMatchingRow(removed_fields_table, "Priority");
        assertTableHasMatchingRow(removed_fields_table, "Affects Version/s");
        assertTableHasMatchingRow(removed_fields_table, "Fix Version/s");
        // however Security Level should not be removed because we keep our parent's Security
        assertTextNotPresent("Security");

        // Confirm
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();
        // Now the Security Level for the subtask will be hidden. (but exist in DB)
        // Assert "RAT-14"
        issueTable = getTable("issuetable");
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasNoImage(issueTable, 1, 5);
        assertTableCellHasNoText(issueTable, 1, 11);
        assertTableCellHasNoText(issueTable, 1, 13);
        assertTableCellHasNoText(issueTable, 1, 14);
        // Assert "RAT-13"
        assertTableCellHasImageInContext(issueTable, 2, 0, "/images/icons/issuetypes/bug.png");
        assertTableCellHasText("issuetable", 2, 1, "RAT-13");
        assertTableCellHasImageInContext(issueTable, 2, 5, "/images/icons/priorities/minor.png");
        assertTableCellHasText("issuetable", 2, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 2, 13, "v1.1");
        assertTableCellHasText("issuetable", 2, 14, "v1.0");

        // now make the field visible again so we can check the actual value in DB
        changeProjectRatToDefaultFieldConfiguration();

        navigation.issueNavigator().displayAllIssues();
        // Assert "RAT-14"
        issueTable = getTable("issuetable");
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasNoImage(issueTable, 1, 5);
        assertTableCellHasText("issuetable", 1, 11, "Level KingRat");
        assertTableCellHasNoText(issueTable, 1, 13);
        assertTableCellHasNoText(issueTable, 1, 14);
        // Assert "RAT-13"
        assertTableCellHasImageInContext(issueTable, 2, 0, "/images/icons/issuetypes/bug.png");
        assertTableCellHasText("issuetable", 2, 1, "RAT-13");
        assertTableCellHasImageInContext(issueTable, 2, 5, "/images/icons/priorities/minor.png");
        assertTableCellHasText("issuetable", 2, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 2, 13, "v1.1");
        assertTableCellHasText("issuetable", 2, 14, "v1.0");
    }

    public void testBulkMoveProject() throws SAXException
    {
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert current setup of "RAT-14"
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask_alternate.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasImageInContext(issueTable, 1, 5, "/images/icons/priorities/minor.png");
        assertTableCellHasText("issuetable", 1, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 1, 13, "v1.1");
        assertTableCellHasText("issuetable", 1, 14, "v1.0");
        // Assert current setup of "RAT-13"
        assertTableCellHasImageInContext(issueTable, 2, 0, "/images/icons/issuetypes/bug.png");
        assertTableCellHasText("issuetable", 2, 1, "RAT-13");
        assertTableCellHasText("issuetable", 2, 2, "Uncle Matty");
        assertTableCellHasImageInContext(issueTable, 2, 5, "/images/icons/priorities/minor.png");
        assertTableCellHasText("issuetable", 2, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 2, 13, "v1.1");
        assertTableCellHasText("issuetable", 2, 14, "v1.0");
        // Assert current setup of "RAT-12"
        assertTableCellHasImageInContext(issueTable, 3, 0, "/images/icons/issuetypes/bug.png");
        assertTableCellHasText("issuetable", 3, 1, "RAT-12");
        assertTableCellHasText("issuetable", 3, 2, "One of them is \"Fatty\"");
        assertTableCellHasImageInContext(issueTable, 3, 5, "/images/icons/priorities/blocker.png");
        assertTableCellHasText("issuetable", 3, 11, "Level KingRat");
        assertTableCellHasText("issuetable", 3, 13, "v0.9");
        assertTableCellHasText("issuetable", 3, 14, "v0.1");

        // Start a Bulk Move
        navigation.issueNavigator().bulkEditAllIssues();
        assertTextPresent("Step 1 of 4: Choose Issues");

        // Choose RAT-13 and RAT-12
        tester.checkCheckbox("bulkedit_10040", "on");
        tester.checkCheckbox("bulkedit_10050", "on");
        tester.submit("Next");
        assertTextPresent("Step 2 of 4: Choose Operation");
        assertTextPresent("Choose the operation you wish to perform on the selected <strong>2</strong> issue(s)");

        // Select the Move operation
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        tester.assertTextPresent("Select Projects and Issue Types");

        // Move the project to Porcine
        tester.selectOption("10022_1_pid", "Porcine");
        tester.submit("Next");
        assertTextPresent("Move Issues");
        assertTextPresent("Select Projects and Issue Types for Sub-Tasks");

        // No changes to subtasks
        tester.submit("Next");
        assertTextPresent("Move Issues");
        assertTextPresent("Update Fields for Target Project 'Porcine' - Issue Type 'Bug'");

        // Not updating fields
        tester.submit("Next");
        assertTextPresent("Move Issues");
        assertTextPresent("Update Fields for Target Project 'Porcine' - Issue Type 'Sub-task'");

        // Click Next to go to confirmation screen:
        tester.submit("Next");
        assertTextPresent("Confirmation");
        WebTable move_confirm_table = getTable("move_confirm_table");
        assertTableHasMatchingRow(move_confirm_table, "Target Project", "Porcine");
        assertTableHasMatchingRow(move_confirm_table, "Target Issue Type", "Bug");
        // Check the removed Fields table.
        WebTable removed_fields_table = getTable("removed_fields_table");
        assertTableHasMatchingRow(removed_fields_table, "Priority");
        assertTableHasMatchingRow(removed_fields_table, "Random Shit");
        assertTableHasMatchingRow(removed_fields_table, "Affects Version/s");
        assertTableHasMatchingRow(removed_fields_table, "Fix Version/s");
        assertTableHasMatchingRow(removed_fields_table, "Security Level");

        // CONFIRM The whole operation.
        tester.submit("Next");
        waitAndReloadBulkOperationProgressPage();
        // Now we are back on the navigator screen.
        issueTable = getTable("issuetable");
        // ------------ Assert "RAT-13" is moved to PIG and has lost hidden fields.
        // The order of the table depends on the order that we moved the issues and gave them new keys, which is random.
        // So we must find the rownumber by matching the summary field.
        int rowNum = findIssueRowNum(issueTable, "Uncle Matty");
        assertTableCellHasImageInContext(issueTable, rowNum, 0, "/images/icons/issuetypes/bug.png");
        // Not sure exactly what the new key is so just make sure it starts with "PIG-1"
        assertTableCellHasText("issuetable", rowNum, 1, "PIG-1");
        assertTableCellHasText("issuetable", rowNum, 2, "Uncle Matty");
        // Assert priority null
        assertTableCellHasNoImage(issueTable, rowNum, 5);
        // security, Fix Version and Affects Version should be null
        assertEquals("", issueTable.getCellAsText(rowNum, 11).trim());
        assertEquals("", issueTable.getCellAsText(rowNum, 13).trim());
        assertEquals("", issueTable.getCellAsText(rowNum, 14).trim());
        // ------------ Assert "RAT-12" moved to PIG
        rowNum = findIssueRowNum(issueTable, "One of them is \"Fatty\"");
        assertTableCellHasImageInContext(issueTable, rowNum, 0, "/images/icons/issuetypes/bug.png");
        // Not sure exactly what the new key is so just make sure it starts with "PIG-1"
        assertTableCellHasText("issuetable", rowNum, 1, "PIG-1");
        assertTableCellHasText("issuetable", rowNum, 2, "One of them is \"Fatty\"");
        // Assert priority null
        assertTableCellHasNoImage(issueTable, rowNum, 5);
        // security, Fix Version and Affects Version should be null
        assertEquals("", issueTable.getCellAsText(rowNum, 11).trim());
        assertEquals("", issueTable.getCellAsText(rowNum, 13).trim());
        assertEquals("", issueTable.getCellAsText(rowNum, 14).trim());
        // ------------ Finally assert the subtask
        assertTableCellHasImageInContext(issueTable, 3, 0, "/images/icons/issuetypes/subtask_alternate.png");
        assertTableCellHasText("issuetable", 3, 1, "PIG-17");
        assertTableCellHasText("issuetable", 3, 2, "Get Uncle Matty a new Leg");
        // Assert priority
        assertTableCellHasImageInContext(issueTable, 3, 5, "/images/icons/priorities/minor.png");
        // security, Fix Version and Affects Version should be null
        assertEquals("", issueTable.getCellAsText(3, 11).trim());
        assertEquals("", issueTable.getCellAsText(3, 13).trim());
        assertEquals("", issueTable.getCellAsText(3, 14).trim());

        Long projectId = backdoor.project().getProjectId("PIG");

        // Now the fields may be hidden, but actually exist.
        // Change the Field Configuration to show these fields.
        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("Bug is Limited FC Scheme"));

        tester.gotoPage("/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=" + projectId);

        tester.selectOption("schemeId", "System Default Field Configuration");
        tester.submit("Associate");

        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("System Default Field Configuration"));

        // now go back to the Navigator and assert that the fields really are removed:
        navigation.issueNavigator().displayAllIssues();
        issueTable = getTable("issuetable");
        // ------------ Assert "RAT-13" is moved to PIG and has lost hidden fields.
        // The order of the table depends on the order that we moved the issues and gave them new keys, which is random.
        // So we must find the rownumber by matching the summary field.
        rowNum = findIssueRowNum(issueTable, "Uncle Matty");
        assertTableCellHasImageInContext(issueTable, rowNum, 0, "/images/icons/issuetypes/bug.png");
        // Not sure exactly what the new key is so just make sure it starts with "PIG-1"
        assertTableCellHasText("issuetable", rowNum, 1, "PIG-1");
        assertTableCellHasText("issuetable", rowNum, 2, "Uncle Matty");
        // Assert priority null
        assertTableCellHasNoImage(issueTable, rowNum, 5);
        // security, Fix Version and Affects Version should be null
        assertEquals("", issueTable.getCellAsText(rowNum, 11).trim());
        assertEquals("", issueTable.getCellAsText(rowNum, 13).trim());
        assertEquals("", issueTable.getCellAsText(rowNum, 14).trim());
        // ------------ Assert "RAT-12" moved to PIG
        rowNum = findIssueRowNum(issueTable, "One of them is \"Fatty\"");
        assertTableCellHasImageInContext(issueTable, rowNum, 0, "/images/icons/issuetypes/bug.png");
        // Not sure exactly what the new key is so just make sure it starts with "PIG-1"
        assertTableCellHasText("issuetable", rowNum, 1, "PIG-1");
        assertTableCellHasText("issuetable", rowNum, 2, "One of them is \"Fatty\"");
        // Assert priority null
        assertTableCellHasNoImage(issueTable, rowNum, 5);
        // security, Fix Version and Affects Version should be null
        assertEquals("", issueTable.getCellAsText(rowNum, 11).trim());
        assertEquals("", issueTable.getCellAsText(rowNum, 13).trim());
        assertEquals("", issueTable.getCellAsText(rowNum, 14).trim());
        // ------------ Finally assert the subtask
        assertTableCellHasImageInContext(issueTable, 3, 0, "/images/icons/issuetypes/subtask_alternate.png");
        assertTableCellHasText("issuetable", 3, 1, "PIG-17");
        assertTableCellHasText("issuetable", 3, 2, "Get Uncle Matty a new Leg");
        // Assert priority null
        assertTableCellHasImageInContext(issueTable, 3, 5, "/images/icons/priorities/minor.png");
        // security, Fix Version and Affects Version should be null
        assertEquals("", issueTable.getCellAsText(3, 11).trim());
        assertEquals("", issueTable.getCellAsText(3, 13).trim());
        assertEquals("", issueTable.getCellAsText(3, 14).trim());

        // Check the View Bug page so that we can check the values in the DB
        navigation.issue().gotoIssue("PIG-15");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "PIG-15");
        // Priority and Security should not appear at all, as they are still blank
        assertTrue(new IdLocator(tester, "priority-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "security-val").getNodes().length == 0);

        // Check that the subtask had its Security Level removed as well
        navigation.issue().gotoIssue("PIG-17");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "PIG-17");
        // Security should be set to blank in order to be the same as the parent
        assertTrue(new IdLocator(tester, "security-val").getNodes().length == 0);
        text.assertTextPresent(new IdLocator(tester, "priority-val"), "Minor");
    }

    private int findIssueRowNum(WebTable issueTable, String key)
    {
        // loop through the issue table until we find a row with the given key in the second column.
        for (int rowNum = 0; rowNum < issueTable.getRowCount(); rowNum++)
        {
            if (key.equals(issueTable.getCellAsText(rowNum, 2).trim()))
                return rowNum;
        }
        // None found
        return -1;
    }

    public void testBulkMigrate_AltersIssue() throws SAXException
    {
        Long projectId = backdoor.project().getProjectId("RAT");
        navigation.gotoAdmin();
        // Edit Project Rattus
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);
        assertTextPresent("Select Issue Type Scheme for project Rattus");

        // Change to "Bugless Scheme", which does not include "Bug" and will therefore force a migration.
        tester.checkCheckbox("createType", "chooseScheme");
        tester.selectOption("schemeId", "Bugless Scheme");
        tester.submit(" OK ");
        assertTextPresent("Issue Type Migration: Overview (Step 1 of 4)");

        tester.submit("nextBtn");
        assertTextPresent("Issue Type Migration: Select Issue Type (Step 2 of 4)");
        // Change to issue type Task
        tester.selectOption("issuetype", "Task");
        tester.submit("nextBtn");
        assertTextPresent("Issue Type Migration: Update Fields (Step 3 of 4)");
        // Next
        tester.submit("nextBtn");
        // We are on the Confirm Screen.
        assertTextPresent("Issue Type Migration: Confirmation (Step 4 of 4)");
        // Check the confirm table
        WebTable move_confirm_table = getTable("move_confirm_table");
        assertTableHasMatchingRow(move_confirm_table, "Target Project", "Rattus");
        assertTableHasMatchingRow(move_confirm_table, "Target Issue Type", "Task");
        // Check the removed Fields table.
        WebTable removed_fields_table = getTable("removed_fields_table");
        assertTableHasMatchingRow(removed_fields_table, "Priority");
        assertTableHasMatchingRow(removed_fields_table, "Random Shit");
        assertTableHasMatchingRow(removed_fields_table, "Affects Version/s");
        assertTableHasMatchingRow(removed_fields_table, "Fix Version/s");
        assertTableHasMatchingRow(removed_fields_table, "Security Level");

        // Confirm that we want to commit the changes
        tester.submit("nextBtn");

        if (tester.getDialog().getResponsePageTitle().contains("Bulk Operation"))
        {
            waitAndReloadBulkOperationProgressPage();
        }

        // we are back on the Project Edit screen.
        assertThat(backdoor.project().getSchemes(projectId).issueTypeScheme.name, equalTo("Bugless Scheme"));

        // Now the fields may be hidden, but actually exist.
        // Change the Field Configuration to show these fields.
        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("Hidden Treasure FC Scheme"));
        tester.gotoPage("/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=" + projectId);

        selectOption("schemeId", "System Default Field Configuration");
        submit("Associate");

        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("System Default Field Configuration"));

        // now go back to the Navigator and assert that the fields really are removed:
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert subtask "RAT-14" has lost Security Level field.
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask_alternate.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasImageInContext(issueTable, 1, 5, "/images/icons/priorities/minor.png");
        assertEquals("", issueTable.getCellAsText(1, 11).trim());
        assertTableCellHasText("issuetable", 1, 13, "v1.1");
        assertTableCellHasText("issuetable", 1, 14, "v1.0");
        // Assert "RAT-13" is now task and has lost hidden fields.
        assertTableCellHasImageInContext(issueTable, 2, 0, "/images/icons/issuetypes/task.png");
        assertTableCellHasText("issuetable", 2, 1, "RAT-13");
        assertTableCellHasNoImage(issueTable, 2, 5);
        assertEquals("", issueTable.getCellAsText(2, 11).trim());
        assertEquals("", issueTable.getCellAsText(2, 13).trim());
        assertEquals("", issueTable.getCellAsText(2, 14).trim());
        // Assert "RAT-12"
        assertTableCellHasImageInContext(issueTable, 3, 0, "/images/icons/issuetypes/task.png");
        assertTableCellHasText("issuetable", 3, 1, "RAT-12");
        assertTableCellHasNoImage(issueTable, 3, 5);
        assertEquals("", issueTable.getCellAsText(3, 11).trim());
        assertEquals("", issueTable.getCellAsText(3, 13).trim());
        assertEquals("", issueTable.getCellAsText(3, 14).trim());

        // Now check the DB as well:
        navigation.issue().gotoIssue("RAT-13");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "RAT-13");
        // Priority and Security should not appear at all, as they are still blank
        assertTrue(new IdLocator(tester, "priority-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "security-val").getNodes().length == 0);
        // Assert Affects Version is now blank
        text.assertTextPresent(new IdLocator(tester, "versions-val"), "None");
        text.assertTextPresent(new IdLocator(tester, "fixfor-val"), "None");

        // But the subtask should always have the same Security Level as its parent
        navigation.issue().gotoIssue("RAT-14");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "RAT-14");
        // Assert Priority
        text.assertTextPresent(new IdLocator(tester, "priority-val"), "Minor");
        // Assert Affects Version
        text.assertTextPresent(new IdLocator(tester, "versions-val"), "v1.0");
        text.assertTextPresent(new IdLocator(tester, "fixfor-val"), "v1.1");
        // Assert Security Level is none
        assertTrue(new IdLocator(tester, "security-val").getNodes().length == 0);
    }

    public void testBulkMigrate_AltersSubtask() throws SAXException
    {
        Long projectId = backdoor.project().getProjectId("RAT");
        // This test will Change the Issue Type scheme in such a way that a subtask will be forced to migrate to a new
        // issue type where this issue type has a hidden Security Level field. Now the Bulk Migrate should not try to
        // remove the Security Level of this subtask, because Security Levels must always be inherited from the parent.
        navigation.gotoAdmin();
        // Edit Project Rattus
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);

        assertTextPresent("Select Issue Type Scheme for project Rattus");

        // Change to "No Subtask Scheme", which does not include "Subtask" and will therefore force a migration.
        tester.checkCheckbox("createType", "chooseScheme");
        tester.selectOption("schemeId", "No Subtask Scheme");
        tester.submit(" OK ");

        assertTextPresent("Overview (Step 1 of 4)");
        tester.submit("nextBtn");
        assertTextPresent("Select Issue Type (Step 2 of 4)");
        // There is only one subtask Issue Type left that can be chosen.
        tester.submit("nextBtn");

        assertTextPresent("Update Fields (Step 3 of 4)");
        assertTextPresent("All field values will be retained.");
        tester.submit("nextBtn");
        // now we are on the confirnation page:
        assertTextPresent("Confirmation (Step 4 of 4)");
        // Check the confirm table
        WebTable move_confirm_table = getTable("move_confirm_table");
        assertTableHasMatchingRow(move_confirm_table, "Target Project", "Rattus");
        assertTableHasMatchingRow(move_confirm_table, "Target Issue Type", "Limited Subtask");
        // Check the removed Fields table.
        WebTable removed_fields_table = getTable("removed_fields_table");
        assertTableHasMatchingRow(removed_fields_table, "Priority");
        assertTableHasMatchingRow(removed_fields_table, "Affects Version/s");
        assertTableHasMatchingRow(removed_fields_table, "Fix Version/s");
        // WE SHOULD NOT TELL THE USER WE ARE GOING TO REMOVE SECURITY LEVEL
        assertTextNotPresent("Security Level");
        assertEquals("Too many rows in the removed_fields_table: Are we including Security Level?", 4, removed_fields_table.getRowCount());
        // Click the Finish button
        tester.submit("nextBtn");

        waitAndReloadBulkOperationProgressPage(true);

        // we are back on the Project Edit screen.
        assertThat(backdoor.project().getSchemes(projectId).issueTypeScheme.name, equalTo("No Subtask Scheme"));

        // Now the Security Level for RAT-14 will be hidden, but should actually exist.
        // Change the Field Configuration to show this field.
        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("Hidden Treasure FC Scheme"));

        tester.gotoPage("/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=" + projectId);
        tester.selectOption("schemeId", "System Default Field Configuration");
        tester.submit("Associate");

        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("System Default Field Configuration"));

        // now go back to the Navigator and assert that Security Level is still there, but other fields really are removed:
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert subtask "RAT-14" has NOT lost Security Level field.
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasNoImage(issueTable, 1, 5);
        assertTableCellHasText("issuetable", 1, 11, "Level KingRat");
        assertTableCellHasNoText(issueTable, 1, 12);
        assertTableCellHasNoText(issueTable, 1, 13);

        // Now check the DB as well:
        navigation.issue().gotoIssue("RAT-14");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "RAT-14");
        // Priority should not appear
        assertTrue(new IdLocator(tester, "priority-val").getNodes().length == 0);
        // Assert Affects Version is now blank
        text.assertTextPresent(new IdLocator(tester, "versions-val"), "None");
        text.assertTextPresent(new IdLocator(tester, "fixfor-val"), "None");
        // Security Level should be visible now.
        text.assertTextPresent(new IdLocator(tester, "security-val"), "Level KingRat");
    }

    public void testConvertSubtaskToIssue() throws SAXException
    {
        // Go to a subtask
        navigation.issue().gotoIssue("RAT-14");
        // Convert to issue
        tester.clickLink("subtask-to-issue");
        assertTextPresent("Convert Sub-task to Issue: RAT-14");

        // Choose Task because SecurityLevel is hidden
        tester.selectOption("issuetype", "Task");
        tester.submit("Next >>");
        assertTextPresent("Convert Sub-task to Issue: RAT-14");
        assertTextPresent("Step 3 of 4");
        assertTextPresent("Update the fields of the issue to relate to the new issue type ...");
        assertTextPresent("All fields will be updated automatically.");

        // Go to confirm screen
        tester.submit("Next >>");
        assertTextPresent("Confirm the conversion with all of the details you have just configured.");
        WebTable table = getTable("convert_confirm_table");
        assertTableHasMatchingRow(table, "Type", "Sub-task", "Task");
        assertTableHasMatchingRow(table, "Affects Version/s", "v1.0", "");
        assertTableHasMatchingRow(table, "Fix Version/s", "v1.1", "");
        assertTableHasMatchingRow(table, "Security Level", "Level KingRat", "");
        assertTableHasMatchingRow(table, "Priority", "Minor", "");

        // Confirm
        tester.submit("Finish");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "RAT-14");
        // None of the hidden fields should appear
        assertTrue(new IdLocator(tester, "security-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "fixfor-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "versions-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "priority-val").getNodes().length == 0);

        // But this is not a sufficient test. in the original issue the values were hidden but existed.
        // Change the Field Configuration to show these fields.
        changeProjectRatToDefaultFieldConfiguration();

        // now go back to the issue screen.
        navigation.issue().gotoIssue("RAT-14");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "RAT-14");
        // Priority and Security should not appear at all, as they are still blank
        assertTrue(new IdLocator(tester, "security-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "priority-val").getNodes().length == 0);
        // Assert Affects Version is now blank
        text.assertTextPresent(new IdLocator(tester, "versions-val"), "None");
        text.assertTextPresent(new IdLocator(tester, "fixfor-val"), "None");

        // go to Navigator to make sure that Lucene has the correct value as well
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert subtask has changed appropriately "RAT-14"
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/task.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasNoImage(issueTable, 1, 5);
        assertEquals("", issueTable.getCellAsText(1, 11).trim());
        assertTrue(issueTable.getCellAsText(1, 12).trim().equals(""));
        assertTrue(issueTable.getCellAsText(1, 13).trim().equals(""));
    }

    public void testConvertIssueToSubtaskOfIssueWithNoSecurity() throws SAXException
    {
        // In this Test we convert an issue to a subtask.
        // The original issue had a security level but its new parent does not, so the Security Level should be deleted.

        // Go to RAT 12
        navigation.issue().gotoIssue("RAT-12");
        assertTextPresent("Security Level");
        assertTextPresent("Level KingRat");

        // Convert to subtask
        tester.clickLink("issue-to-subtask");
        tester.setFormElement("parentIssueKey", "RAT-10");
        tester.selectOption("issuetype", "Limited Subtask");
        tester.submit("Next >>");
        assertTextPresent("Convert Issue to Sub-task: RAT-12");
        assertTextPresent("All fields will be updated automatically.");

        // go to confirmation page
        tester.submit("Next >>");
        WebTable table = getTable("convert_confirm_table");
        assertTableHasMatchingRow(table, "Type", "Bug", "Limited Subtask");
        assertTableHasMatchingRow(table, "Security Level", "Level KingRat", "None");
        assertTableHasMatchingRow(table, "Random Shit", "asdf", "");

        // Confirm
        tester.submit("Finish");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "RAT-12");
        // Security field should not appear
        assertTrue(new IdLocator(tester, "security-val").getNodes().length == 0);

        // But this is not a sufficient test. in the original issue the values were hidden but existed.
        // Change the Field Configuration to show these fields.
        changeProjectRatToDefaultFieldConfiguration();

        // now go back to the issue screen.
        navigation.issue().gotoIssue("RAT-12");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "RAT-12");
        // Security field should not appear
        assertTrue(new IdLocator(tester, "security-val").getNodes().length == 0);

        // go to Navigator to make sure that Lucene has the correct value as well
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert "RAT-12"
        assertTableCellHasImageInContext(issueTable, 3, 0, "/images/icons/issuetypes/subtask.png");
        assertTableCellHasText("issuetable", 3, 1, "RAT-12");
        assertEquals("", issueTable.getCellAsText(3, 11).trim());
        assertTrue(issueTable.getCellAsText(3, 12).trim().equals(""));
        assertTrue(issueTable.getCellAsText(3, 13).trim().equals(""));
    }

    public void testConvertIssueToSubtaskOfIssueWithSecurity() throws SAXException
    {
        // In this Test we convert an issue to a subtask.
        // The original issue had a security level "Level KingRat" but the new subtask Issue type will hide the
        // Security Level Field.
        // However the new parent HAS a security level, and by definition the subtask MUST have the same security level
        // as its parent. In this case it is CORRECT for the subtask to have a security level (equal to its parent)
        // even though it will not be visible to the users.
        // This is quite a complicated case, but is also not so likely to occur in the field (one would hope!).

        // Go to RAT 12
        navigation.issue().gotoIssue("RAT-12");
        assertTextPresent("Security Level");
        assertTextPresent("Level KingRat");

        // Convert to subtask
        tester.clickLink("issue-to-subtask");
        tester.setFormElement("parentIssueKey", "RAT-11");
        tester.selectOption("issuetype", "Limited Subtask");
        tester.submit("Next >>");
        assertTextPresent("Convert Issue to Sub-task: RAT-12");
        assertTextPresent("All fields will be updated automatically.");

        // go to confirmation page
        tester.submit("Next >>");
        WebTable table = getTable("convert_confirm_table");
        assertTableHasMatchingRow(table, "Type", "Bug", "Limited Subtask");
        // !!! We should take on the Security of the Parent even though Security is a hidden field !!!
        assertTableHasMatchingRow(table, "Security Level", "Level KingRat", "Level Mouse");
        // Other hidden fields should be deleted as usual.
        assertTableHasMatchingRow(table, "Affects Version/s", "v0.1", "");
        assertTableHasMatchingRow(table, "Fix Version/s", "v0.9", "");
        assertTableHasMatchingRow(table, "Priority", "Blocker", "");
        assertTableHasMatchingRow(table, "Random Shit", "asdf", "");
        // Assert Security Level doesn't appear twice
        assertEquals("Too many rows in summary table - is Security level listed twice?", 7, table.getRowCount());

        // Confirm
        tester.submit("Finish");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "RAT-12");
        // Security field should not appear (although the value exists - it is still hidden)
        assertTrue(new IdLocator(tester, "security-val").getNodes().length == 0);
        // None of the hidden fields should appear
        assertTrue(new IdLocator(tester, "priority-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "fixfor-val").getNodes().length == 0);
        assertTrue(new IdLocator(tester, "versions-val").getNodes().length == 0);
        assertTextNotPresent("Random Shit");

        // Now check that the security Level values is actually correct even though it was hidden.
        // Change the Field Configuration to show these fields.
        changeProjectRatToDefaultFieldConfiguration();

        // now go back to the issue screen.
        navigation.issue().gotoIssue("RAT-12");
        // Assert the Issue Key
        text.assertTextPresent(new IdLocator(tester, "key-val"), "RAT-12");
        // Assert Affects Version
        text.assertTextPresent(new IdLocator(tester, "versions-val"), "None");
        text.assertTextPresent(new IdLocator(tester, "fixfor-val"), "None");
        // Assert Security Level - this value existed even though it was hidden.
        text.assertTextPresent(new IdLocator(tester, "security-val"), "Level Mouse");

        // go to Navigator to make sure that Lucene has the correct value as well
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert "RAT-12"
        assertTableCellHasImageInContext(issueTable, 3, 0, "/images/icons/issuetypes/subtask.png");
        assertTableCellHasText("issuetable", 3, 1, "RAT-12");
        assertEquals("Level Mouse", issueTable.getCellAsText(3, 11).trim());
        assertTrue(issueTable.getCellAsText(3, 12).trim().equals(""));
        assertTrue(issueTable.getCellAsText(3, 13).trim().equals(""));
    }

    public void testMoveSubtaskToNewParentWithHiddenSecurity() throws SAXException
    {
        // When you move a subtask to a new Parent, it should take the Security Level of the new Parent, even if
        // Security Level is hidden on the parent (which implies the Parent's Security Level is "None").

        // First set up a parent with hidden Security Level
        navigation.issue().gotoIssue("RAT-10");
        tester.clickLink("move-issue");
        tester.selectOption("issuetype", "Task");
        tester.submit("Next >>");
        tester.submit("Next >>");
        tester.submit("Move");

        // Now go to a subtask
        navigation.issue().gotoIssue("RAT-14");
        assertTextPresent("RAT-14");
        assertTextPresent("Security Level");
        assertTextPresent("Level KingRat");
        // Move this subtask
        tester.clickLink("move-issue");
        assertTextPresent("Choose the operation you wish to perform");
        // Change Parent
        checkCheckbox("operation", "move.subtask.parent.operation.name");
        tester.submit("Next >>");
        assertTextPresent("Select a new parent issue for this subtask.");
        // Select new Parent
        tester.setFormElement("parentIssue", "RAT-10");
        tester.submit("Change Parent");

        // Now Security should be removed from the subtask
        assertTextPresent("RAT-14");
        assertTextNotPresent("Security Level");
        assertTextNotPresent("Level KingRat");

        // go to Navigator to make sure that Lucene has the correct value as well
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert "RAT-14"
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask_alternate.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasNoText(issueTable, 1, 11);
        assertTrue(issueTable.getCellAsText(1, 13).trim().equals("v1.1"));
        assertTrue(issueTable.getCellAsText(1, 14).trim().equals("v1.0"));
    }

    public void testMoveSubtaskWithHiddenSecurityToNewParent() throws SAXException
    {
        // When you move a subtask to a new Parent, it should take the Security Level of the new Parent, even if
        // Security Level is hidden on the subtask.

        // First we have to set a subtask to have hidden security.
        navigation.issue().gotoIssue("RAT-14");
        tester.clickLink("move-issue");
        tester.checkCheckbox("operation", "move.subtask.type.operation.name");
        tester.submit("Next >>");
        assertTextPresent("Choose the sub-task type to move to");
        tester.submit("Next >>");
        assertTextPresent("Update the fields of the sub-task to relate to the new sub-task type.");
        tester.submit("Next >>");
        assertTextPresent("Confirm the move with all of the details you have just configured.");
        tester.submit("Move");

        // Now move it to a new Parent. The current parent has Security Level KingRat, the new one Level Mouse.
        tester.clickLink("move-issue");
        assertTextPresent("Choose the operation you wish to perform");
        // Change Parent
        tester.checkCheckbox("operation", "move.subtask.parent.operation.name");
        tester.submit("Next >>");
        assertTextPresent("Select a new parent issue for this subtask.");
        // Select new Parent
        tester.setFormElement("parentIssue", "RAT-11");
        tester.submit("Change Parent");

        // Now Security will be hidden
        assertTextPresent("RAT-14");
        assertTextNotPresent("Security Level");
        assertTextNotPresent("Level Mouse");
        assertTextNotPresent("Level KingRat");

        // But if we make it visible ...
        changeProjectRatToDefaultFieldConfiguration();
        // ... we should now see the actual underlying value
        navigation.issue().gotoIssue("RAT-14");
        assertTextPresent("RAT-14");
        assertTextPresent("Security Level");
        assertTextPresent("Level Mouse");

        // go to Navigator to make sure that Lucene has the correct value as well
        navigation.issueNavigator().displayAllIssues();
        WebTable issueTable = getTable("issuetable");
        // Assert "RAT-14"
        assertTableCellHasImageInContext(issueTable, 1, 0, "/images/icons/issuetypes/subtask.png");
        assertTableCellHasText("issuetable", 1, 1, "RAT-14");
        assertTableCellHasText("issueTable", 1, 11, "Level Mouse");
        assertTrue(issueTable.getCellAsText(1, 12).trim().equals(""));
        assertTrue(issueTable.getCellAsText(1, 13).trim().equals(""));
    }

    private void assertTableCellHasNoText(final WebTable webTable, final int row, final int column)
    {
        String actual = webTable.getCellAsText(row, column).trim();
        assertEquals("Table cell at [" + row + ", " + column + "] was expected to have no text, but was '" + actual + "'",
                        "", actual);
    }

    private void changeProjectRatToDefaultFieldConfiguration()
    {
        Long projectId = backdoor.project().getProjectId("RAT");
        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("Hidden Treasure FC Scheme"));

        tester.gotoPage("/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=" + projectId);

        tester.selectOption("schemeId", "System Default Field Configuration");
        tester.submit("Associate");
        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo("System Default Field Configuration"));
    }

    private void assertTableHasMatchingRow(WebTable webtable, String col1)
    {
        assertTableHasMatchingRow(webtable, new Object[] {
                new StrictTextCell(col1)
        });
    }

    private void assertTableHasMatchingRow(WebTable webtable, String col1, String col2)
    {
        assertTableHasMatchingRow(webtable, new Object[] {
                new StrictTextCell(col1),
                new StrictTextCell(col2)
        });
    }

    private void assertTableHasMatchingRow(WebTable webtable, String col1, String col2, String col3)
    {
        assertTableHasMatchingRow(webtable, new Object[] {
                new StrictTextCell(col1),
                new StrictTextCell(col2),
                new StrictTextCell(col3)
        });
    }

    /**
     * This method will use the test properties context to make a safe check for an image.
     *
     * @param table WebTable
     * @param row   Index of the row
     * @param col   Index of the column
     * @param path  Image path relative to context.
     */
    protected void assertTableCellHasImageInContext(WebTable table, int row, int col, String path)
    {
        String fullPath = new LocalTestEnvironmentData().getContext() + path;
        assertTableCellHasImage(table, row, col, fullPath);
    }

    private WebTable getTable(String tableID) throws SAXException
    {
        return tester.getDialog().getResponse().getTableWithID(tableID);
    }
}
