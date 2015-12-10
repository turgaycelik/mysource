package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.SUB_TASKS })
public class TestIssueToSubTaskConversionStep2 extends JIRAWebTest
{
    private static final String ISSUE_TO_CONVERT_ID = "10050";
    private static final String ISSUE_TO_CONVERT_KEY = "MKY-4";
    private static final String ISSUE_TO_CONVERT_INVALID_STATUS_ID = "10051";
    private static final String ISSUE_TO_CONVERT_INVALID_STATUS_KEY = "MKY-5";
    private static final String PARENT_ISSUE = "MKY-3";
    private static final String SUBTASK_TYPE = "Sub-task";
    private static final String SUBTASK_TYPE_ID = "5";
    private static final String SUBTASK_TYPE_3 = "Sub-task 3";
    private static final String SUBTASK_TYPE_3_ID = "7";
    private static final String VALID_STATUS = "1";
    private static final String INVALID_STATUS = "3";
    private static final String NON_EXISTANT_STATUS = "932";


    public TestIssueToSubTaskConversionStep2(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueToSubTaskConversion.xml");
    }

    public void testSameWorkflow()
    {
        gotoConvertIssueStep2(ISSUE_TO_CONVERT_ID, PARENT_ISSUE, SUBTASK_TYPE_ID);
        assertTextPresent("Step 2 is not required.");

        // check pane
        assertThirdStepPaneWithout2nd(ISSUE_TO_CONVERT_KEY, PARENT_ISSUE, SUBTASK_TYPE, "Open");


        // Make sure Status is not displayed for update or on confirmation

        //assert other status not shown on screen
        assertTextNotPresent ("Closed");
        assertTextNotPresent ("Reopened");
        assertTextNotPresent ("Resolved");

        submit("Next >>");
        

        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT_KEY, 4);
        assertTextNotInTable("convert_confirm_table", "Status");

        submit("Finish");

        //check issue type
        Locator locator = new IdLocator(tester, "type-val");
        text.assertTextPresent(locator, SUBTASK_TYPE);
        // check that issue has correct status
        locator = new IdLocator(tester, "status-val");
        text.assertTextPresent(locator, "Open");

        // Check change history
        clickLinkWithText(CHANGE_HISTORY);
        assertTextPresentOnlyOnce("Status");
        assertTextSequence(new String[]{CHANGE_HISTORY, "Issue Type", "Bug", SUBTASK_TYPE});
        assertTextSequence(new String[]{CHANGE_HISTORY, "Parent", "MKY-3"});  
    }

    public void testDiffWorkflowValidStatus()
    {
        gotoConvertIssueStep2(ISSUE_TO_CONVERT_ID, PARENT_ISSUE, SUBTASK_TYPE_3_ID);
        assertTextPresent("Step 2 is not required.");

        // check pane
        assertThirdStepPaneWithout2nd(ISSUE_TO_CONVERT_KEY, PARENT_ISSUE, SUBTASK_TYPE_3, "Open");

        // Make sure Status is not displayed for update but is shown on confirm

        //assert other status not shown on screen
        assertTextNotPresent ("Closed");
        assertTextNotPresent ("Reopened");
        assertTextNotPresent ("Resolved");

        submit("Next >>");

        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT_KEY, 4);
        assertTextInTable("convert_confirm_table", "Status");
        assertTextSequence(new String[]{"Status", "(Workflow)", "Open", "status-inactive", "(jira)", "Open", "status-active", "(Subtask Workflow)"});

        submit("Finish");

        //check issue type
        Locator locator = new IdLocator(tester, "type-val");
        text.assertTextPresent(locator, SUBTASK_TYPE_3);
        // check that issue has correct status
        locator = new IdLocator(tester, "status-val");
        text.assertTextPresent(locator, "Open");


        // Check change history
        clickLinkWithText(CHANGE_HISTORY);
        assertTextPresentOnlyOnce("Status");
        assertTextSequence(new String[]{CHANGE_HISTORY, "Workflow", "jira", "Subtask Workflow"});
        assertTextSequence(new String[]{CHANGE_HISTORY, "Issue Type", "Bug", SUBTASK_TYPE_3});
        assertTextSequence(new String[]{CHANGE_HISTORY, "Parent", "MKY-3"});
    }

    public void testDiffWorkflowInvalidStatus()
    {
        gotoConvertIssueStep2(ISSUE_TO_CONVERT_INVALID_STATUS_ID, PARENT_ISSUE, SUBTASK_TYPE_3_ID);
        assertTextPresent("Step 2 of 4");

        // test that all shown statuses are valid
        assertOptionsEqual("targetStatusId", new String[]{"Open", "Resolved", "Reopened", "Closed"});
        assertOptionValueNotPresent("targetStatusId", "In Progress");

        //Check from and target workflow
        assertTextSequence(new String[]{"Select New Status", "In Progress", "Workflow", "jira", "Workflow", "Subtask Workflow"});

        // check pane
        assertSecondStepPane(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, PARENT_ISSUE, SUBTASK_TYPE_3);

        setFormElement("targetStatusId", "1");

        submit("Next >>");

        //assert other status not shown on screen
        assertTextNotPresent ("Closed");
        assertTextNotPresent ("Reopened");
        assertTextNotPresent ("Resolved");

        submit("Next >>");

        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, 4);
        assertTextInTable("convert_confirm_table", "Status");
        assertTextSequence(new String[]{"Status", "(Workflow)", "In Progress", "status-inactive", "(jira)", "Open", "status-active", "(Subtask Workflow)"});

        submit("Finish");

        //check issue type
        Locator locator = new IdLocator(tester, "type-val");
        text.assertTextPresent(locator, SUBTASK_TYPE_3);
        // check that issue has correct status
        locator = new IdLocator(tester, "status-val");
        text.assertTextPresent(locator, "Open");

        // Check change history
        clickLinkWithText(CHANGE_HISTORY);
        assertTextSequence(new String[]{CHANGE_HISTORY, "Workflow", "jira", "Subtask Workflow"});
        assertTextSequence(new String[]{CHANGE_HISTORY, "Status", "In Progress", "Open"});
        assertTextSequence(new String[]{CHANGE_HISTORY, "Issue Type", "Bug", SUBTASK_TYPE_3});   
        assertTextSequence(new String[]{CHANGE_HISTORY, "Parent", "MKY-3"});   
        
    }

    public void testBrowseBackOnPane()
    {
        //Test back to Step 1
        gotoConvertIssueStep3(ISSUE_TO_CONVERT_INVALID_STATUS_ID, PARENT_ISSUE, SUBTASK_TYPE_3_ID, "1");
        submit("Next >>");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, 4);

        clickLinkWithText("Select Parent and Sub-task Type");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, 1);
        assertTextPresent("Step 1 of 4");
        assertTextPresent(PARENT_ISSUE);
        getDialog().dumpResponse();
        assertTextPresent("option value=\"" + SUBTASK_TYPE_3_ID + "\" SELECTED");
        //assertOptionSelectedById("issuetype", SUBTASK_TYPE_3_ID);

        //Test back to Step 2
        gotoConvertIssueStep3(ISSUE_TO_CONVERT_INVALID_STATUS_ID, PARENT_ISSUE, SUBTASK_TYPE_3_ID, "1");
        submit("Next >>");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, 4);

        clickLinkWithText("Select New Status");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, 2);
        assertTextPresent("Step 2 of 4");      
        assertTextPresent("option value=\"" + "1" + "\" SELECTED");
        //assertOptionSelected("targetStatusId", "Open");

        //Test back to Step 3
        gotoConvertIssueStep3(ISSUE_TO_CONVERT_INVALID_STATUS_ID, PARENT_ISSUE, SUBTASK_TYPE_3_ID, "1");
        submit("Next >>");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, 4);

        clickLinkWithText("Update Fields");
        assertSubTaskConversionPanelSteps(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, 3);
        assertTextPresent("Step 3 of 4");

    }



    public void testResultsValidStatus()
    {
        gotoConvertIssueStep3(ISSUE_TO_CONVERT_INVALID_STATUS_ID, PARENT_ISSUE, SUBTASK_TYPE_3_ID, VALID_STATUS);
        assertTextPresent("Step 3 of 4");

        // check pane
        assertThirdStepPaneWith2nd(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, PARENT_ISSUE, SUBTASK_TYPE_3, "Open");

    }

    public void testResultsInvalidStatus()
    {
        gotoConvertIssueStep3(ISSUE_TO_CONVERT_INVALID_STATUS_ID, PARENT_ISSUE, SUBTASK_TYPE_3_ID, INVALID_STATUS);
        assertTextPresent("Step 2 of 4");
        assertTextPresent("Selected status (In Progress) is not valid for target workflow");

        // check pane
        assertSecondStepPane(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, PARENT_ISSUE, SUBTASK_TYPE_3);
    }

    public void testResultsNoStatus()
    {
        gotoConvertIssueStep3(ISSUE_TO_CONVERT_INVALID_STATUS_ID, PARENT_ISSUE, SUBTASK_TYPE_3_ID, "");
        assertTextPresent("Step 2 of 4");
        assertTextPresent("No status specified");

        // check pane
        assertSecondStepPane(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, PARENT_ISSUE, SUBTASK_TYPE_3);
    }
    public void testResultsNonExistantStatus()
    {
        gotoConvertIssueStep3(ISSUE_TO_CONVERT_INVALID_STATUS_ID, PARENT_ISSUE, SUBTASK_TYPE_3_ID, NON_EXISTANT_STATUS);
        assertTextPresent("Step 2 of 4");
        assertTextPresent("Status with id " + NON_EXISTANT_STATUS + " does not exist");

        // check pane
        assertSecondStepPane(ISSUE_TO_CONVERT_INVALID_STATUS_KEY, PARENT_ISSUE, SUBTASK_TYPE_3);
    }

    public void testResultsValidStatusForIssueThatDoesntNeedChanging()
    {
        gotoConvertIssueStep3(ISSUE_TO_CONVERT_ID, PARENT_ISSUE, SUBTASK_TYPE_ID, INVALID_STATUS);

        assertTextPresent("Status should not be changed for this conversion");
    }
    private void assertSecondStepPane(String key, String parent, String type)
    {
        assertSubTaskConversionPanelSteps(key, 2);
        assertLinkPresentWithText("Select Parent and Sub-task Type");
        assertLinkNotPresentWithText("Select New Status");
        assertLinkNotPresentWithText("Update Fields");
        assertTextSequence(new String[] {
            "Parent Issue:", "<strong>" + parent + "</strong>",
            "Sub-task Type:", "<strong>" + type + "</strong>",
            "Select New Status"
                });
    }

    private void assertThirdStepPaneWith2nd(String key, String parent, String type, String status)
    {
        assertSubTaskConversionPanelSteps(key, 3);
        assertLinkPresentWithText("Select Parent and Sub-task Type");
        assertLinkPresentWithText("Select New Status");
        assertLinkNotPresentWithText("Update Fields");
        assertTextSequence(new String[] {
            "Parent Issue:", "<strong>" + parent + "</strong>",
            "Sub-task Type:", "<strong>" + type + "</strong>",
            "Status:", "<strong>" + status + "</strong>"
        });

    }

        private void assertThirdStepPaneWithout2nd(String key, String parent, String type, String status)
    {
        assertSubTaskConversionPanelSteps(key, 3);
        assertLinkPresentWithText("Select Parent and Sub-task Type");
        assertLinkNotPresentWithText("Select New Status");
        assertLinkNotPresentWithText("Update Fields");
        assertTextSequence(new String[] {
            "Parent Issue:", "<strong>" + parent + "</strong>",
            "Sub-task Type:", "<strong>" + type + "</strong>",
            "Status:", "<strong>" + status + "</strong>"
        });

    }
}
