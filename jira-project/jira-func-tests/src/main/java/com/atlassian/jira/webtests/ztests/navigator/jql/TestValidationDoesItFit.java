package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * JQL Func Test to ensure the IssueNavigator's behaviour when currently in Simple Mode and calling the IssueNavigator via URL
 * and the value for a field is invalid and requires to display a validation error message.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestValidationDoesItFit extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestValidationDoesItFit.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    /**
     * Test a saved filter that contains a function that will cause a validation error which is displayable in advanced
     * mode but not simple mode will be redirected back to advanced mode to show the error.
     * @throws Exception monkey
     */
    public void testFunctionValidationDoesntFit() throws Exception
    {
        // Sub-tasks are disabled so the filter is invalid. However, loading
        // a saved filter shouldn't produce errors until we modify it.
        issueTableAssertions.assertSearchWithError(10000L, "Function 'subTaskIssueTypes' is invalid as sub-tasks are currently disabled.");
        issueTableAssertions.assertSearchWithError(10000L, "issuetype in subTaskIssueTypes() ORDER BY key",
                "Function 'subTaskIssueTypes' is invalid as sub-tasks are currently disabled.");
    }

    // TODO: JRADEV-16612
//    public void testSimpleIssueNavigatorValidationDoesntFit() throws Exception
//    {
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "pid=1232133");
//        assertions.getIssueNavigatorAssertions().assertJqlErrors("A value with ID '1232133' does not exist for the field 'project'.");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "type=-3");
//        assertions.getIssueNavigatorAssertions().assertJqlErrors("Function 'subTaskIssueTypes' is invalid as sub-tasks are currently disabled.");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "type=2343");
//        assertions.getIssueNavigatorAssertions().assertJqlErrors("A value with ID '2343' does not exist for the field 'issuetype'.");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, true, "reporterSelect=specificuser", "reporter=sfdsf");
//        tester.assertTextPresent("Could not find username: sfdsf");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, true, "assigneeSelect=specificuser", "assignee=sfdsf");
//        tester.assertTextPresent("Could not find username: sfdsf");
//
//        navigation.logout();
//
//        tester.gotoPage("secure");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "reporterSelect=issue_current_user");
//        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults();
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "assigneeSelect=issue_current_user");
//        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults();
//
//        navigation.login(ADMIN_USERNAME);
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "status=78");
//        assertions.getIssueNavigatorAssertions().assertJqlErrors("A value with ID '78' does not exist for the field 'status'.");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "resolution=78");
//        assertions.getIssueNavigatorAssertions().assertJqlErrors("A value with ID '78' does not exist for the field 'resolution'.");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "priority=78");
//        assertions.getIssueNavigatorAssertions().assertJqlErrors("A value with ID '78' does not exist for the field 'priority'.");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, true, "customfield_10001=10003", "customfield_10001:1=10008");
//        assertions.getTextAssertions().assertTextPresentHtmlEncoded("The option 'Child Option 2' is an invalid option in the context 'Default Configuration for Cascading Select CF'");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "customfield_10016=23423423");
//        assertions.getIssueNavigatorAssertions().assertJqlErrors("A value with ID '23423423' does not exist for the field 'Project Picker CF'.");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "customfield_10017=23");
//        assertions.getTextAssertions().assertTextPresentHtmlEncoded("The option '23' for field 'Radio Buttons CF' does not exist.");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "customfield_10000=23");
//        assertions.getTextAssertions().assertTextPresentHtmlEncoded("The option '23' for field 'Select List CF' does not exist.");
//
//        executeIssueNavigatorURL(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, true, "customfield_10018=A Version that doesn't exist");
//        assertions.getIssueNavigatorAssertions().assertJqlErrors("The value 'A Version that doesn't exist' does not exist for the field 'Single Version Picker CF'.");
//    }



}
