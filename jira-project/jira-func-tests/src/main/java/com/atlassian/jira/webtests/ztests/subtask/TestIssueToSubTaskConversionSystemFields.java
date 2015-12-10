package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;

/**
 * This test ensures that the correct systemfields are shown/hidden in the convert issue
 * to subtask wizard.
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.ISSUES, Category.SUB_TASKS })
public class TestIssueToSubTaskConversionSystemFields extends JIRAWebTest
{
    private static final String ISSUE_MKY_1 = "MKY-1";
    private static final String PARENT_ISSUE_MKY_3 = "MKY-3";
    private static final String ISSUE_HSP_1 = "HSP-1";
    private static final String PARENT_ISSUE_HSP_2 = "HSP-2";
    private static final String ISSUE_GOD_1 = "GOD-1";
    private static final String PARENT_ISSUE_GOD_2 = "GOD-2";

    public TestIssueToSubTaskConversionSystemFields(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueToSubtaskConversionSystemFields.xml");
    }

    /**
     * This tests the following:
     * || From || Value? || To || Field Update || Confirmation || Field
     * | H | (x) | H | not shown | not shown | Affects Version
     * | H | (x) | S | not shown | not shown | Assignee
     * | H | (x) | R | prompt | shown | Components
     * | S | (/) | H | not shown | shown | Description
     * | S | (/) | S | not shown | not shown | Due Date
     * | S | (/) | Sr | prompt | shown | Env
     * <p/>
     * Where:
     * X = non-existing field
     * H = not shown, optional field
     * S = shown, optional field
     * Sr = shown, optional field with changed renderer (text <-> wiki)
     * R = shown, required field
     */
    public void testConvertHiddenAndSomeShownScenarios()
    {
        gotoIssue(ISSUE_MKY_1);
        clickLink("issue-to-subtask");

        // assert 1st screen
        assertTextPresent("Step 1 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_MKY_1, 1);
        assertTextPresent("Convert Issue to Sub-task: " + ISSUE_MKY_1);

        // set a parent issue
        setFormElement("parentIssueKey", PARENT_ISSUE_MKY_3);
        submit("Next >>");

        // assert 2nd screen was skipped
        assertTextNotPresent("Step 2 of 4");
        // assert we're on the 3rd screen
        assertTextPresent("Step 3 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_MKY_1, 3);

        //check the correct fields are shown
        assertTextPresent("Component/s:");
        assertTextPresent("Environment:");
        //fields that shouldn't be there:
        assertTextNotPresent("Affects:");
        assertTextNotPresent("Assignee:");
        assertTextNotPresent("Description:");
        assertTextNotPresent("Due Date:");


        submit("Next >>");

        //assert we are still on the 3rd screen and an error is shown since components is a required field.
        // assert we're on the 3rd screen
        assertTextPresent("Step 3 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_MKY_1, 3);
        assertTextPresent("Component/s is required");

        //now set a component and continue.
        selectOption("components", "Component1");
        submit("Next >>");

        // assert we're on the 4th screen
        assertTextPresent("Step 4 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_MKY_1, 4);

        //assert all the correct fields are shown.
        final WebTable table = getDialog().getWebTableBySummaryOrId("convert_confirm_table");
        assertEquals("number of rows", 5, table.getRowCount());

        assertTrue(tableCellHasText(table, 1, 0, "Type"));
        assertTrue(tableCellHasText(table, 1, 1, "Bug"));
        assertTrue(tableCellHasText(table, 1, 2, "Sub-task"));
        assertTrue(tableCellHasText(table, 2, 0, "Component/s"));
        assertTrue(tableCellDoesNotHaveText(table, 2, 1, "Component1"));
        assertTrue(tableCellHasText(table, 2, 2, "Component1"));
        assertTrue(tableCellHasText(table, 3, 0, "Environment"));
        assertTrue(tableCellHasText(table, 3, 1, "A test value"));
        assertTrue(tableCellHasText(table, 3, 2, "A test value"));
        assertTrue(tableCellHasText(table, 4, 0, "Description"));
        assertTrue(tableCellHasText(table, 4, 1, "A test value"));
        assertTrue(tableCellDoesNotHaveText(table, 4, 2, "A test value"));

        //finish the wizard
        submit("Finish");

        text.assertTextPresent(new IdLocator(tester, "parent_issue_summary"), PARENT_ISSUE_MKY_3 + " A third monkey issue");
        text.assertTextPresent(new IdLocator(tester, "key-val"), ISSUE_MKY_1);
        text.assertTextPresent(new CssLocator(tester, "#content header h1"), "A monkey bug");
        text.assertTextPresent(new IdLocator(tester, "type-val"), "Sub-task");

    }

    /**
     * This tests the following:
     * || From || Value? || To || Field Update || Confirmation || Field
     * | S | (/) | R | not shown | not shown | Affects Vers
     * | S | (/) | Rr | prompt | shown | Description
     * | S | (x) | H | not shown | not shown | Due date
     * | S | (x) | R | prompt | shown | Component
     * | R | (/) | H | not shown | shown | Fix Version
     * | R | (/) | S | not shown | not shown | Priority
     * | R | (/) | Sr | prompt | shown | Environment
     * | R | (/) | R | not shown | not shown | Summary
     * <p/>
     * Where:
     * X = non-existing field
     * H = not shown, optional field
     * S = shown, optional field
     * Sr = shown, optional field with changed renderer (text <-> wiki)
     * R = shown, required field
     */
    public void testShownAndSomeRequiredScenarios()
    {
        gotoIssue(ISSUE_HSP_1);
        clickLink("issue-to-subtask");

        // assert 1st screen
        assertTextPresent("Step 1 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_HSP_1, 1);
        assertTextPresent("Convert Issue to Sub-task: " + ISSUE_HSP_1);

        // set a parent issue
        setFormElement("parentIssueKey", PARENT_ISSUE_HSP_2);
        submit("Next >>");

        // assert 2nd screen was skipped
        assertTextNotPresent("Step 2 of 4");
        // assert we're on the 3rd screen
        assertTextPresent("Step 3 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_HSP_1, 3);

        //check the correct fields are shown
        assertTextPresent("Component/s:");
        assertTextPresent("Environment:");
        assertTextPresent("Description:");
        //fields that shouldn't be there:
        assertTextNotPresent("Affects:");
        assertTextNotPresent("Due Date:");
        assertTextNotPresent("Priority:");
        assertTextNotPresent("Summary:");

        submit("Next >>");

        //assert we are still on the 3rd screen and an error is shown since components is a required field.
        // assert we're on the 3rd screen
        assertTextPresent("Step 3 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_HSP_1, 3);
        assertTextPresent("Component/s is required");

        //now set a component and continue.
        selectOption("components", "New Component 1");
        submit("Next >>");

        // assert we're on the 4th screen
        assertTextPresent("Step 4 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_HSP_1, 4);

        //assert all the correct fields are shown.
        final WebTable table = getDialog().getWebTableBySummaryOrId("convert_confirm_table");
        assertEquals("number of rows", 6, table.getRowCount());

        assertTrue(tableCellHasText(table, 1, 0, "Type"));
        assertTrue(tableCellHasText(table, 1, 1, "Bug"));
        assertTrue(tableCellHasText(table, 1, 2, "Sub-task"));
        assertTrue(tableCellHasText(table, 2, 0, "Component/s"));
        assertTrue(tableCellDoesNotHaveText(table, 2, 1, "New Component 1"));
        assertTrue(tableCellHasText(table, 2, 2, "New Component 1"));
        assertTrue(tableCellHasText(table, 3, 0, "Description"));
        assertTrue(tableCellHasText(table, 3, 1, "A test desc"));
        assertTrue(tableCellHasText(table, 3, 2, "A test desc"));
        assertTrue(tableCellHasText(table, 4, 0, "Environment"));
        assertTrue(tableCellHasText(table, 4, 1, "A test env"));
        assertTrue(tableCellHasText(table, 4, 2, "A test env"));
        assertTrue(tableCellHasText(table, 5, 0, "Fix Version/s"));
        assertTrue(tableCellHasText(table, 5, 1, "New Version 1"));
        assertTrue(tableCellDoesNotHaveText(table, 5, 2, "New Version 1"));

        //finish the wizard
        submit("Finish");

        text.assertTextPresent(new IdLocator(tester, "parent_issue_summary"), PARENT_ISSUE_HSP_2 + " A second issue");
        text.assertTextPresent(new IdLocator(tester, "key-val"), ISSUE_HSP_1);
        text.assertTextPresent(new CssLocator(tester, "#content header h1"), "A new issue");
        text.assertTextPresent(new IdLocator(tester, "type-val"), "Sub-task");
    }

    /**
     * This tests the following:
     * || From || Value? || To || Field Update || Confirmation || Field
     * | S | (x) | S | not shown | not shown | Environment
     * | R | (/) | Rr | prompt | shown | Description
     * <p/>
     * Where:
     * X = non-existing field
     * H = not shown, optional field
     * S = shown, optional field
     * Sr = shown, optional field with changed renderer (text <-> wiki)
     * R = shown, required field
     */
    public void testSomeMoreShownAndSomeRequiredScenarios()
    {
        gotoIssue(ISSUE_GOD_1);
        clickLink("issue-to-subtask");

        // assert 1st screen
        assertTextPresent("Step 1 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_GOD_1, 1);
        assertTextPresent("Convert Issue to Sub-task: " + ISSUE_GOD_1);

        // set a parent issue
        setFormElement("parentIssueKey", PARENT_ISSUE_GOD_2);
        submit("Next >>");

        // assert 2nd screen was skipped
        assertTextNotPresent("Step 2 of 4");
        // assert we're on the 3rd screen
        assertTextPresent("Step 3 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_GOD_1, 3);

        //check the correct fields are shown
        assertTextPresent("Description:");
        //fields that shouldn't be there:
        assertTextNotPresent("Component/s:");
        assertTextNotPresent("Environment:");
        assertTextNotPresent("Affects:");
        assertTextNotPresent("Due Date:");
        assertTextNotPresent("Priority:");
        assertTextNotPresent("Summary:");

        submit("Next >>");

        // assert we're on the 4th screen
        assertTextPresent("Step 4 of 4");
        assertSubTaskConversionPanelSteps(ISSUE_GOD_1, 4);

        //assert all the correct fields are shown.
        final WebTable table = getDialog().getWebTableBySummaryOrId("convert_confirm_table");
        assertEquals("number of rows", 3, table.getRowCount());

        assertTrue(tableCellHasText(table, 1, 0, "Type"));
        assertTrue(tableCellHasText(table, 1, 1, "Bug"));
        assertTrue(tableCellHasText(table, 1, 2, "Sub-task"));
        assertTrue(tableCellHasText(table, 2, 0, "Description"));
        assertTrue(tableCellHasText(table, 2, 1, "A test desc"));
        assertTrue(tableCellHasText(table, 2, 2, "A test desc"));

        //finish the wizard
        submit("Finish");

        text.assertTextPresent(new IdLocator(tester, "parent_issue_summary"), PARENT_ISSUE_GOD_2 + " The overlord");
        text.assertTextPresent(new IdLocator(tester, "key-val"), ISSUE_GOD_1);
        text.assertTextPresent(new CssLocator(tester, "#content header h1"), "A new issue");
        text.assertTextPresent(new IdLocator(tester, "type-val"), "Sub-task");
    }


}