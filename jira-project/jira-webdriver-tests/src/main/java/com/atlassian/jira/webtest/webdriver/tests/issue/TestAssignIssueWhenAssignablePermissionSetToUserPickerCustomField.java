package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.dialogs.quickedit.EditIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests issue assigning scenarios when the project has a permission scheme that sets the "Assignable User" permission
 * to check for the value of the User Picker custom field.
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestAssignIssueWhenAssignablePermissionSetToUserPickerCustomField extends BaseJiraWebTest
{
    private static final String ISSUE_KEY = "TP-1";
    private static final String USER_NOT_SELECTED_ON_USER_PICKER = "test";
    private static final String USER_SELECTED_ON_USER_PICKER = "test2";

    // Scenario:
    //
    // Three users: "admin", "test", "test2"
    // An user picker custom field: "CF User Picker (multiple)"
    //
    // A single project: "TP"
    // "TP" has a permission scheme that defines the "Assignable user" to be decided by the value set on "CF User Picker (multiple)"
    //
    // A single issue "TP-1", that is unassigned and has a value of "test2" on the field "CF User Picker (multiple)"

    @Test
    @Restore ("xml/TestAssignIssueWhenAssignablePermissionSetToUserPickerCustomField.xml")
    public void issueCanNotBeAssignedToADifferentUserAsTheOneSpecifiedOnTheUserPickerCustomField() throws Exception
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue(ISSUE_KEY);
        viewIssuePage.execKeyboardShortcut("e");

        EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class).typeAssignee(USER_NOT_SELECTED_ON_USER_PICKER);
        editIssueDialog.submit();

        assertAssigneeFieldDisplaysFormError(editIssueDialog);
    }
    
    @Test
    @Restore ("xml/TestAssignIssueWhenAssignablePermissionSetToUserPickerCustomField.xml")
    public void issueCanBeAssignedToTheUserSpecifiedOnTheUserPickerCustomField()
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue(ISSUE_KEY);
        viewIssuePage.execKeyboardShortcut("e");

        EditIssueDialog editIssueDialog = pageBinder.bind(EditIssueDialog.class).typeAssignee(USER_SELECTED_ON_USER_PICKER);

        editIssueDialog.submitExpectingViewIssue(ISSUE_KEY);
    }

    private void assertAssigneeFieldDisplaysFormError(final EditIssueDialog editIssueDialog)
    {
        Map<String, String> formErrors = editIssueDialog.getFormErrors();
        assertThat(formErrors.get("assignee"), not(nullValue()));
    }
}
