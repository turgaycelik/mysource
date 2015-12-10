package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static com.atlassian.jira.permission.ProjectPermissions.MOVE_ISSUES;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.PERMISSIONS, Category.SCHEMES })
public class TestPermissionSchemes extends JIRAWebTest
{
    public TestPermissionSchemes(String name)
    {
        super(name);
    }

    private static final String MOVE_TABLE_ID = "move_confirm_table";
    private static final int MOVE_TABLE_FIELD_NAME_COLUMN_INDEX = 0;
    private static final int MOVE_TABLE_OLD_VALUE_COLUMN_INDEX = 1;
    private static final int MOVE_TABLE_NEW_VALUE_COLUMN_INDEX = 2;

    public void testPermissionSchemes()
    {
        administration.restoreBlankInstance();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        administration.project().addProject(PROJECT_NEO, PROJECT_NEO_KEY, ADMIN_USERNAME);
        String issueKeyNormal = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test 1", "Minor", null, null, null, ADMIN_FULLNAME, "test environment 1", "test description for permission schemes", null, null, null);

        permissionSchemesCreateScheme();
        permissionSchemeAssociateScheme();
        permissionSchemeAddDuplicateScheme();
        permissionSchemeAddInvalidScheme();
        permissionSchemesMoveIssueToProjectWithAssignablePermission(issueKeyNormal);
        permissionSchemesMoveIssueToProjectWithAssignPermission();
        permissionSchemesMoveIssueWithSchedulePermission(issueKeyNormal);
        permissionSchemesMoveIssueToProjectWithCreatePermission(issueKeyNormal);

        permissionSchemeDeleteScheme();

        navigation.issue().deleteIssue(issueKeyNormal);
    }

    public void testProjectRolePermissionScheme()
    {
        logSection("Test to check that project role permission scheme works");
        administration.restoreData("TestSchemesProjectRoles.xml");
        gotoPermissionSchemes();
        tester.clickLinkWithText(DEFAULT_PERM_SCHEME);
        tester.assertTextPresent("Edit Permissions &mdash; " + DEFAULT_PERM_SCHEME);

        tester.clickLink("add_perm_" + MOVE_ISSUES.permissionKey());

        tester.assertTextPresent("Choose a project role");

        tester.checkCheckbox("type", "projectrole");
        tester.selectOption("projectrole", "test role");
        tester.submit();
        tester.assertTextPresent("(test role)");
    }

    public void permissionSchemesCreateScheme()
    {
        log("Permission Schemes: Create a new permission scheme");
        createPermissionScheme(PERM_SCHEME_NAME, PERM_SCHEME_DESC);
        tester.assertLinkPresentWithText(PERM_SCHEME_NAME);
        tester.assertTextPresent(PERM_SCHEME_DESC);
    }

    public void permissionSchemeDeleteScheme()
    {
        log("Permission Schemes:Delete a permission scheme");
        deletePermissionScheme(PERM_SCHEME_NAME);
        tester.assertLinkNotPresentWithText(PERM_SCHEME_NAME);
    }

    public void permissionSchemeAssociateScheme()
    {
        log("Permission Schemes: Associate a permission scheme with a project");
        associatePermSchemeToProject(PROJECT_NEO, PERM_SCHEME_NAME);

        assertThat(backdoor.project().getSchemes(PROJECT_NEO_KEY).permissionScheme.name, equalTo(PERM_SCHEME_NAME));

        associatePermSchemeToProject(PROJECT_NEO, DEFAULT_PERM_SCHEME);
    }

    /**
     * Create a scheme with a duplicate name
     */
    public void permissionSchemeAddDuplicateScheme()
    {
        log("Permission Schemes: Attempt to create a scheme with a duplicate name");
        createPermissionScheme(PERM_SCHEME_NAME, "");
        tester.assertTextPresent("Add Permission Scheme");
        tester.assertTextPresent("A Scheme with this name already exists.");
    }

    /**
     * Create a scheme with an invalid name
     */
    public void permissionSchemeAddInvalidScheme()
    {
        log("Permission Schemes: Attempt to create a scheme with an invalid name");
        createPermissionScheme("", "");
        tester.assertTextPresent("Add Permission Scheme");
        tester.assertTextPresent("Please specify a name for this Scheme.");
    }

    /**
     * Tests the ability to move an issue to a project WITHOUT the 'Assignable User' Permission
     */
    private void permissionSchemesMoveIssueToProjectWithAssignablePermission(String issueKey)
    {
        log("Move Operation: Moving issue to a project with 'Assign Issue' Permission.");
        associatePermSchemeToProject(PROJECT_NEO, PERM_SCHEME_NAME);
        setUnassignedIssuesOption(true);
        // Give jira-users 'Create' Permission
        grantGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        // give jira-developers 'Assignable Users' Permission
        grantGroupPermission(PERM_SCHEME_NAME, ASSIGNABLE_USER, Groups.DEVELOPERS);

        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("move-issue");
        tester.selectOption("pid", PROJECT_NEO);
        tester.submit();
        tester.assertTextPresent("Step 3 of 4");

        tester.assertTextNotPresent(DEFAULT_ASSIGNEE_ERROR_MESSAGE);

        removeGroupPermission(PERM_SCHEME_NAME, ASSIGNABLE_USER, Groups.DEVELOPERS);
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("move-issue");
        tester.selectOption("pid", PROJECT_NEO);
        tester.submit();
        tester.assertTextPresent("Step 3 of 4");
        tester.setWorkingForm("jiraform");
        tester.submit();

        tester.assertTextPresent(DEFAULT_ASSIGNEE_ERROR_MESSAGE);


        setUnassignedIssuesOption(false);
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("move-issue");
        tester.selectOption("pid", PROJECT_NEO);
        tester.submit();
        tester.assertTextPresent("Step 3 of 4");
        tester.assertTextNotPresent(DEFAULT_ASSIGNEE_ERROR_MESSAGE);

        removeGroupPermission(PERM_SCHEME_NAME,CREATE_ISSUE, Groups.USERS);
        associatePermSchemeToProject(PROJECT_NEO, DEFAULT_PERM_SCHEME);
    }

    /**
     * Test that assignee is autoassigned for move issue operation if user does not have 'Assign' permission
     */
    public void permissionSchemesMoveIssueToProjectWithAssignPermission()
    {
        log("Move Operation: Test that assignee is autoassigned if assignee does not have assign permission");
        associatePermSchemeToProject(PROJECT_NEO, PERM_SCHEME_NAME);
        // Give jira-users 'Create' Permission
        grantGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        // Give jira-admin 'Assign' Permission
        grantGroupPermission(PERM_SCHEME_NAME, ASSIGN_ISSUE, Groups.ADMINISTRATORS);
        // Give jira-users 'Assignable Users' Permission
        grantGroupPermission(PERM_SCHEME_NAME, ASSIGNABLE_USER, Groups.USERS);
        // Give jira-users 'Browse Project' Permission
        grantGroupPermission(PERM_SCHEME_NAME, BROWSE, Groups.USERS);
        grantGroupPermission(PERM_SCHEME_NAME, MOVE_ISSUE, Groups.USERS);

        administration.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        String issueKey;
        try
        {
            getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
            issueKey = addIssue(PROJECT_NEO, PROJECT_NEO_KEY, "Bug", "test 1", "Minor", null, null, null, BOB_FULLNAME, "Original Assignee - Bob\n New Assignee - " + ADMIN_FULLNAME, "This issue should be moved and auto-assigned to " + ADMIN_FULLNAME, null, null, null);
        }
        finally
        {
            getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        }

        administration.permissionSchemes().defaultScheme().removePermission(ASSIGN_ISSUE, Groups.DEVELOPERS);

        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("move-issue");
        tester.selectOption("pid", PROJECT_HOMOSAP);
        tester.submit();
        tester.assertTextPresent("Step 3 of 4");

        tester.setWorkingForm("jiraform");
        tester.submit();
        try
        {
            WebTable fieldTable = tester.getDialog().getResponse().getTableWithID(MOVE_TABLE_ID);
            // First row is a headings row so skip it
            for (int i = 1; i < fieldTable.getRowCount(); i++)
            {
                String field = fieldTable.getCellAsText(i, MOVE_TABLE_FIELD_NAME_COLUMN_INDEX);
                if (field.contains("Assignee"))
                {
                    String oldValue = fieldTable.getCellAsText(i, MOVE_TABLE_OLD_VALUE_COLUMN_INDEX);
                    String newValue = fieldTable.getCellAsText(i, MOVE_TABLE_NEW_VALUE_COLUMN_INDEX);
                    assertTrue(oldValue.contains(BOB_FULLNAME));
                    assertTrue(newValue.contains(ADMIN_FULLNAME));

                    administration.permissionSchemes().defaultScheme().grantPermissionToGroup(ASSIGN_ISSUE, Groups.DEVELOPERS);
                    removeGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
                    removeGroupPermission(PERM_SCHEME_NAME, ASSIGN_ISSUE, Groups.ADMINISTRATORS);
                    removeGroupPermission(PERM_SCHEME_NAME, ASSIGNABLE_USER, Groups.USERS);
                    removeGroupPermission(PERM_SCHEME_NAME, BROWSE, Groups.USERS);
                    removeGroupPermission(PERM_SCHEME_NAME, MOVE_ISSUE, Groups.USERS);
                    associatePermSchemeToProject(PROJECT_NEO, DEFAULT_PERM_SCHEME);
                    navigation.issue().deleteIssue(issueKey);
                    deleteUser(BOB_USERNAME);
                    return;
                }
            }
            fail("Cannot find field chamge for 'Assignee'");
        }
        catch (SAXException e)
        {
            fail("Cannot find table with id '" + MOVE_TABLE_ID + "'.");
            e.printStackTrace();
        }
    }

    /**
     * Test the abilty to move an issue with 'Schedule Issues' Permission and 'Due Date' Required
     */
    public void permissionSchemesMoveIssueWithSchedulePermission(String issueKey)
    {
        log("Move Operation: Moving issue to a project with 'Schedule Issue' Permission.");
        associatePermSchemeToProject(PROJECT_NEO, PERM_SCHEME_NAME);
        removeGroupPermission(PERM_SCHEME_NAME,SCHEDULE_ISSUE, Groups.DEVELOPERS);
        grantGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        setDueDateToRequried();

        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("move-issue");
        tester.selectOption("pid", PROJECT_NEO);
        tester.submit();

        tester.assertTextPresent("Step 3 of 4");

        tester.setWorkingForm("jiraform");
        tester.submit();
        tester.assertTextPresent("&quot;Due Date&quot; field is required and you do not have permission to Schedule Issues for project &quot;" + PROJECT_NEO + "&quot;.");

        // restore settings
        resetFields();
        grantGroupPermission(PERM_SCHEME_NAME, SCHEDULE_ISSUE, Groups.DEVELOPERS);
        removeGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        associatePermSchemeToProject(PROJECT_NEO, DEFAULT_PERM_SCHEME);
    }

    /**
     * Tests the ability to move an issue to a project WITHOUT the 'Create Issue' Permission
     */
    public void permissionSchemesMoveIssueToProjectWithCreatePermission(String issueKey)
    {
        log("Move Operation: Moving issue to a project with 'Create Issue' Permission.");
        associatePermSchemeToProject(PROJECT_NEO, PERM_SCHEME_NAME);

        grantGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("move-issue");
        tester.assertOptionsEqual("pid", new String[] {PROJECT_HOMOSAP, PROJECT_NEO, PROJECT_HOMOSAP, PROJECT_MONKEY, PROJECT_NEO});
        removeGroupPermission(PERM_SCHEME_NAME, CREATE_ISSUE, Groups.USERS);
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("move-issue");
        tester.assertOptionsEqual("pid", new String[] {PROJECT_HOMOSAP, PROJECT_HOMOSAP, PROJECT_MONKEY});

        associatePermSchemeToProject(PROJECT_NEO, DEFAULT_PERM_SCHEME);
    }
}

