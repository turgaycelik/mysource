package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * This test ensures that an issue is inserted in the correct order in a subtask.
 * There are also a number of test cases to ensure canceling the subtask to issue conversion works.
 * <p/>
 * It also tests that the appropriate warning is shown if you do not have browse permission for an issue.
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.SUB_TASKS })
public class TestIssueToSubTaskConversionVariousOperations extends JIRAWebTest
{

    public TestIssueToSubTaskConversionVariousOperations(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueToSubtaskConversionVariousOperations.xml");
    }

    public void testAddNewSubtaskToExistingIssueWithSubtask()
    {
        gotoIssue("HSP-2");

        //check that the issue already has some subtasks
        assertTextPresent("Sub-Tasks");
        assertTextPresentBeforeText("1.", "My sub-task summary");
        assertTextPresentBeforeText("2.", "Second subtask");
        assertTextNotPresent("A new issue");

        // Now lets convert issue HSP-1 to be a subtask of HSP-2...
        gotoIssue("HSP-1");
        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", "HSP-2");
        submit("Next >>");
        selectOption("components", "New Component 1");
        submit("Next >>");
        submit("Finish");

        //go back to the original issue and ensure it has the correct order of subtasks.
        gotoIssue("HSP-2");
        assertTextPresentBeforeText("1.", "My sub-task summary");
        assertTextPresentBeforeText("2.", "Second subtask");
        assertTextPresentBeforeText("3.", "A new issue");

        //lets create a new subtask and make sure it goes at the very end
        clickLink("stqc_show");
        submit("Create");
        setFormElement("summary", "The new manually added subtask");
        setFormElement("description", "A test Desc");
        selectOption("components", "New Component 1");
        selectOption("versions", "New Version 1");
        submit("Create");

        gotoIssue("HSP-2");
        assertTextPresentBeforeText("1.", "My sub-task summary");
        assertTextPresentBeforeText("2.", "Second subtask");
        assertTextPresentBeforeText("3.", "A new issue");
        assertTextPresentBeforeText("4.", "The new manually added subtask");
    }

    public void testCancelFirstStep()
    {
        gotoIssue("HSP-1");

        //go to the first step of the wizard.
        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", "HSP-2");

        //cancel and check we get back to the issue screen
        gotoPage("/secure/ConvertIssue!cancel.jspa?id=10000");
        assertTextPresent("HSP-1");
        assertTextPresent("A new issue");
        assertTextPresentBeforeText("Type", "Bug");

        //start the convert issue operation again, and see if we can succeed
        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", "HSP-2");
        submit("Next >>");
        selectOption("components", "New Component 1");
        submit("Next >>");
        submit("Finish");

        text.assertTextPresent(new IdLocator(tester, "parent_issue_summary"), "HSP-2" + " A second issue");
        text.assertTextPresent(new IdLocator(tester, "key-val"), "HSP-1");
        text.assertTextPresent(new CssLocator(tester, "#content header h1"), "A new issue");
        text.assertTextPresent(new IdLocator(tester, "type-val"), "Sub-task");
    }

    public void testCancelThirdStep()
    {
        gotoIssue("HSP-1");

        //go to the first step of the wizard.
        clickLink("issue-to-subtask");
        assertSubTaskConversionPanelSteps("HSP-1", 1);
        setFormElement("parentIssueKey", "HSP-2");

        //go to the next step (step 3)
        submit("Next >>");
        assertSubTaskConversionPanelSteps("HSP-1", 3);

        //cancel and check we get back to the issue screen
        gotoPage("/secure/ConvertIssue!cancel.jspa?id=10000");
        assertTextPresent("HSP-1");
        assertTextPresent("A new issue");
        assertTextPresentBeforeText("Type", "Bug");

        //start the convert issue operation again, and see if we can succeed
        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", "HSP-2");
        submit("Next >>");
        selectOption("components", "New Component 1");
        submit("Next >>");
        submit("Finish");

        text.assertTextPresent(new IdLocator(tester, "parent_issue_summary"), "HSP-2" + " A second issue");
        text.assertTextPresent(new IdLocator(tester, "key-val"), "HSP-1");
        text.assertTextPresent(new CssLocator(tester, "#content header h1"), "A new issue");
        text.assertTextPresent(new IdLocator(tester, "type-val"), "Sub-task");
    }

    public void testCancelLastStep()
    {
        gotoIssue("HSP-1");

        //go to the first step of the wizard.
        clickLink("issue-to-subtask");
        assertSubTaskConversionPanelSteps("HSP-1", 1);
        setFormElement("parentIssueKey", "HSP-2");

        //go to the next step (step 3)
        submit("Next >>");
        assertSubTaskConversionPanelSteps("HSP-1", 3);

        //go to the last step
        selectOption("components", "New Component 1");
        submit("Next >>");
        assertSubTaskConversionPanelSteps("HSP-1", 4);

        //cancel and check we get back to the issue screen
        gotoPage("/secure/ConvertIssue!cancel.jspa?id=10000");
        assertTextPresent("HSP-1");
        assertTextPresent("A new issue");
        assertTextPresentBeforeText("Type", "Bug");

        //start the convert issue operation again, and see if we can succeed
        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", "HSP-2");
        submit("Next >>");
        selectOption("components", "New Component 1");
        submit("Next >>");
        submit("Finish");
        text.assertTextPresent(new IdLocator(tester, "parent_issue_summary"), "HSP-2" + " A second issue");
        text.assertTextPresent(new IdLocator(tester, "key-val"), "HSP-1");
        text.assertTextPresent(new CssLocator(tester, "#content header h1"), "A new issue");
        text.assertTextPresent(new IdLocator(tester, "type-val"), "Sub-task");

    }

    public void testConversionWithoutBrowsePermission()
    {
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);

        //lets go to the conversion operation of an issue I can't browse.
        gotoPage("/secure/ConvertIssue.jspa?id=10080");

        //choose a parent issue
        setFormElement("parentIssueKey", "CAT-3");
        submit("Next >>");
        submit("Next >>");
        submit("Finish");

        //now check that the correct warning is shown about not being able to browse the converted issue.
        assertTextPresent("You have successfully converted the issue (CAT-1), however you do not have the permission to view the converted issue");
    }


    public void testConversionWithHiddenField()
    {
        //first set an environment.
        gotoIssue("CAT-1");
        clickLink("edit-issue");
        setFormElement("environment", "Test environment");
        submit("Update");

        //now lets hide the environment.
        gotoAdmin();
        clickLink("field_configuration");        
        clickLinkWithText("Default Field Configuration");
        clickLink("hide_7");

        //lets convert the issue and ensure that the environment field is not part of the list.
        gotoIssue("CAT-1");
        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", "CAT-2");
        submit("Next >>");
        submit("Next >>");
        assertSubTaskConversionPanelSteps("CAT-1", 4);
        assertTextPresent("Environment");
    }
}
