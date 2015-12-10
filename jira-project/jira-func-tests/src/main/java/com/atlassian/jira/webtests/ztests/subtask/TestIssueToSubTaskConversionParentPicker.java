package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for verifying the step of picking the parent issue while the user is converting an issue to a sub-task.
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.SUB_TASKS })
public class TestIssueToSubTaskConversionParentPicker extends FuncTestCase
{
    private static final String PROJECT_ID = "10000";
    private static final String ISSUE = "HSP-1";
    private static final String SUBTASK = "HSP-3";
    private static final String PARENT_ISSUE = "HSP-5";
    private static final String ISSUE_FROM_OTHER_PROJECT = "MKY-1";
    private static final String SHOW_ALL_FILTER_ID = "10000";

    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueToSubTaskConversion.xml");
    }

    /*
     * Tests that the parent issue picker does not show the issue itself
     */
    public void testIssueToSubTaskConversionParentPickerSameIssue()
    {
        navigation.issueNavigator().loadFilter(Long.parseLong(SHOW_ALL_FILTER_ID));
        tester.assertLinkPresentWithText(ISSUE);

        tester.gotoPage("/secure/popups/IssuePicker.jspa?searchRequestId=" + SHOW_ALL_FILTER_ID +
                "&mode=search&formName=jiraform&linkFieldName=parentIssueKey&" +
                "currentIssue=" + ISSUE +
                "&singleSelectOnly=true&showSubTasks=false&showSubTasksParent=true&" +
                "selectedProjectId=" + PROJECT_ID);
        tester.assertLinkNotPresentWithText(ISSUE);
    }

    /*
     * Tests that the parent issue picker does not show sub-task issues
     */
    public void testIssueToSubTaskConversionParentPickerSubTask()
    {
        navigation.issueNavigator().loadFilter(Long.parseLong(SHOW_ALL_FILTER_ID));
        tester.assertLinkPresentWithText(SUBTASK);

        tester.gotoPage("/secure/popups/IssuePicker.jspa?searchRequestId=" + SHOW_ALL_FILTER_ID +
                "&mode=search&formName=jiraform&linkFieldName=parentIssueKey&" +
                "currentIssue=" + ISSUE +
                "&singleSelectOnly=true&showSubTasks=false&showSubTasksParent=true&" +
                "selectedProjectId=" + PROJECT_ID);
        tester.assertLinkNotPresentWithText(SUBTASK);
    }

    /*
     * Tests that the parent issue picker does not show the issues from other projects
     */
    public void testIssueToSubTaskConversionParentPickerOtherProject()
    {
        navigation.issueNavigator().loadFilter(Long.parseLong(SHOW_ALL_FILTER_ID));
        tester.assertLinkPresentWithText(ISSUE_FROM_OTHER_PROJECT);

        tester.gotoPage("/secure/popups/IssuePicker.jspa?searchRequestId=" + SHOW_ALL_FILTER_ID +
                "&mode=search&formName=jiraform&linkFieldName=parentIssueKey&" +
                "currentIssue=" + ISSUE +
                "&singleSelectOnly=true&showSubTasks=false&showSubTasksParent=true&" +
                "selectedProjectId=" + PROJECT_ID);
        tester.assertLinkNotPresentWithText(ISSUE_FROM_OTHER_PROJECT);
    }

    /*
     * Tests that the parent issue picker does not show issues that the user does not have permission to see
     */
    public void testIssueToSubTaskConversionParentPickerNoPermission()
    {
        tester.gotoPage("/secure/popups/IssuePicker.jspa?searchRequestId=" + SHOW_ALL_FILTER_ID +
                "&mode=search&formName=jiraform&linkFieldName=parentIssueKey&" +
                "currentIssue=" + ISSUE +
                "&singleSelectOnly=true&showSubTasks=false&showSubTasksParent=true&" +
                "selectedProjectId=" + PROJECT_ID);
        tester.submit();
        tester.assertLinkPresentWithText(PARENT_ISSUE);

        navigation.logout();
        navigation.login(FRED_USERNAME, FRED_PASSWORD);

        tester.gotoPage("/secure/popups/IssuePicker.jspa?searchRequestId=" + SHOW_ALL_FILTER_ID +
                "&mode=search&formName=jiraform&linkFieldName=parentIssueKey&" +
                "currentIssue=" + ISSUE +
                "&singleSelectOnly=true&showSubTasks=false&showSubTasksParent=true&" +
                "selectedProjectId=" + PROJECT_ID);
        tester.assertLinkNotPresentWithText(PARENT_ISSUE);
    }
}
