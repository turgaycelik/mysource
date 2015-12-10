package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS })
public class TestBulkMoveWithMultiContexts extends JIRAWebTest
{

    public TestBulkMoveWithMultiContexts(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("IssuesWithSubTasksWorkflowScheme.xml");
    }

    public void testBulkMoveWithMultiContexts() throws Exception
    {
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);
        displayAllIssues();
        bulkChangeIncludeAllPages();
        bulkChangeChooseIssuesAll();
        clickOnNext();

        checkCheckbox("operation", "bulk.move.operation.name");
        clickOnNext();

        assertTextPresent("Select Projects and Issue Types");
        selectOption("10000_1_pid", PROJECT_MONKEY);
        clickOnNext();

        assertTextPresent("Select Projects and Issue Types for Sub-Tasks");
        assertTextPresent("Super Sub Task");
        assertTextPresent("Mega Sub Task");
        selectOption("10000_6_10001_issuetype", ISSUE_TYPE_SUB_TASK);
        selectOption("10000_7_10001_issuetype", ISSUE_TYPE_SUB_TASK);
        clickOnNext();

        assertTextPresent("Map Status for Target Project 'monkey' - Issue Type 'Bug'");
        selectOption("10000", STATUS_OPEN);
        clickOnNext();

        assertTextPresent("All field values will be retained");
        clickOnNext();

        assertTextPresent("Map Status for Target Project 'monkey' - Issue Type 'Sub-task'");
        assertTextPresentBeforeText("Current Status", "Mega Open");
        assertTextPresentBeforeText("Current Status", "Super Open");
        clickOnNext();

        assertTextPresent("All field values will be retained");
        clickOnNext();

        assertTextPresent("Confirmation");
        clickOnNext();

        waitAndReloadBulkOperationProgressPage();

        // Check that everything has moved correctly
        assertTextPresent("Issue Navigator");
        assertTextPresent("MKY-1");
        clickLinkWithText("MKY-1");
        assertTextPresentBeforeText("Status", STATUS_OPEN);
        clickLinkWithText("Super Sub Task Issue");
        assertTextPresentBeforeText("Type", ISSUE_TYPE_SUB_TASK);
        assertTextPresentBeforeText("Status", STATUS_OPEN);
        assertTextPresent(PROJECT_MONKEY);
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
    }

    public void testBulkMoveIssueWithoutVersionPermission()
    {
        restoreData("TestMoveIssueWithoutVersionPermission.xml");
        gotoIssue("HSP-1");
        tester.assertTextPresent("Test issue 1");
        tester.assertTextPresent("New Version 1");
        tester.assertTextPresent("New Version 4");

        displayAllIssues();

        //do a bulk move of HSP-1 to the monkey project.
        bulkChangeIncludeAllPages();
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.move.operation.name");
        tester.submit("Next");
        tester.selectOption("10000_1_pid", "monkey");
        tester.submit("Next");

        assertTextSequence(new String[] { "Fix Version/s", "The value of this field must be changed to be valid in the target project, but you are not able to update this field in the target project. It will be set to the field's default value for the affected issues." });
        tester.submit("Next");
        tester.submit("Next");

        waitAndReloadBulkOperationProgressPage();

        tester.clickLinkWithText("MKY-1");
        tester.assertTextPresent("Test issue 1");
        tester.assertTextNotPresent("New Version 1");
        tester.assertTextNotPresent("New Version 4");
    }
}
