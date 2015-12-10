package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.PermissionSchemesControl;

import java.net.URL;

import static com.atlassian.jira.testkit.client.IssuesControl.HSP_PROJECT_ID;
import static com.atlassian.jira.webtests.Groups.DEVELOPERS;

/**
 * Tests the single-select control on the Assignee field.
 *
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS })
public class TestAssigneeField extends FuncTestCase
{
    private final long projectId = HSP_PROJECT_ID;
    private long issueId;
    private long permissionSchemeId;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreBlankInstance();

        backdoor.usersAndGroups().addUsers("user", "User ", 50);
        backdoor.usersAndGroups().addUsersWithGroup("d", "Dev ", 50, DEVELOPERS);

        PermissionSchemesControl permissionSchemes = backdoor.permissionSchemes();
        permissionSchemeId = permissionSchemes.copyDefaultScheme("Admin-Only Scheme");
        permissionSchemes.replaceGroupPermissions(permissionSchemeId, ASSIGNABLE_USER, DEVELOPERS);
        backdoor.project().setPermissionScheme(projectId, permissionSchemeId);

        IssueCreateResponse createResponse = backdoor.issues().createIssue(projectId, "Issue 1", "d5");
        String issueKey = createResponse.key();

        issueId = Long.parseLong(createResponse.id());

        // Assign the issue a few times to build up some User History for admin
        for (int i = 10; i < 20; i++)
        {
            backdoor.issues().assignIssue(issueKey, "d" + i);
        }

        log("Logging in d42 developer");
        navigation.login("d42", "d42");

        log("Ready for testing");
    }

    // JRADEV-6693
    public void testAssigneePickerVisibility() throws Exception
    {
        navigation.issue().gotoEditIssue(issueId);
        assertions.assertNodeExists("//select[@id='assignee']");

        backdoor.permissionSchemes().removeGroupPermission(permissionSchemeId, ASSIGN_ISSUE, DEVELOPERS);

        try
        {
            navigation.issue().gotoEditIssue(issueId);
            assertions.assertNodeDoesNotExist("//select[@id='assignee']");
        }
        finally
        {
            backdoor.permissionSchemes().addGroupPermission(permissionSchemeId, ASSIGN_ISSUE, DEVELOPERS);
        }
    }

    // JRADEV-7842
    public void testAssigneeNotAlteredIfNoPermission() throws Exception
    {
        backdoor.generalConfiguration().disallowUnassignedIssues();
        backdoor.permissionSchemes().removeGroupPermission(permissionSchemeId, ASSIGN_ISSUE, DEVELOPERS);

        try
        {
            navigation.issue().gotoEditIssue(issueId);

            tester.setWorkingForm("issue-edit");
            tester.submit();

            // Should be on View screen, not still on Edit screen (with an error message saying assignee is required)
            assertTrue(getUrl().getPath().endsWith("/browse/HSP-1"));
            // Assignee will be d19, as that is the last user the issue was assigned to in setup.
            assertions.assertNodeExists(assigneeLocator("d19"));
        }
        finally
        {
            backdoor.permissionSchemes().addGroupPermission(permissionSchemeId, ASSIGN_ISSUE, DEVELOPERS);
        }
    }

    // JRADEV-7842, JRA-27212
    public void testAssigneeSetToAutomaticIfNoPermission() throws Exception
    {
        // Ensure the default assignee is the project lead.
        backdoor.project().setProjectDefaultAssignee(projectId, true);

        // The initial project owner is admin.
        IssueCreateResponse response = backdoor.issues().createIssue(projectId, "Auto-assign to admin", null);
        navigation.issue().gotoIssue(response.key());
        assertions.assertNodeExists(assigneeLocator("admin"));

        // Set the project owner to someone else
        backdoor.project().setProjectLead(projectId, "d36");

        // Ensure the default assignee has changed.
        response = backdoor.issues().createIssue(projectId, "Should be auto-assigned to d36", null);
        navigation.issue().gotoIssue(response.key());
        assertions.assertNodeExists(assigneeLocator("d36"));
    }

    private String assigneeLocator(String user)
    {
        return "//*[@id='assignee-val']/*[@rel='" + user + "']";
    }

    private URL getUrl()
    {
        return tester.getTestContext().getWebClient().getCurrentPage().getURL();
    }
}
