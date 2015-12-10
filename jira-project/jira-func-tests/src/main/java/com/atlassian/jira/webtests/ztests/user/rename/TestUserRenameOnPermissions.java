package com.atlassian.jira.webtests.ztests.user.rename;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.PermissionSchemes;
import com.atlassian.jira.functest.framework.admin.user.EditUserPage;
import com.atlassian.jira.functest.framework.page.ViewIssuePage;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.PERMISSIONS})
public class TestUserRenameOnPermissions extends FuncTestCase
{
    public void testSingleUserPermission()
    {
        administration.restoreData("user_rename_permissions.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        assertTrue(userCanEdit("betty"));

        // Remove existing permission - project role developer
        PermissionSchemes.PermissionScheme permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.removePermission(PermissionSchemes.Type.EDIT_ISSUES, "10001");

        assertFalse(userCanEdit("betty"));

        // Single User Permission for betty
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.grantPermissionToSingleUser(PermissionSchemes.Type.EDIT_ISSUES, "betty");
        assertTrue(userCanEdit("betty"));
        assertFalse(userCanEdit("bb"));

        // Rename user betty to betty2
        EditUserPage editUserPage = administration.usersAndGroups().gotoEditUser("betty");
        editUserPage.setUsername("betty2");
        editUserPage.submitUpdate();

        assertTrue(userCanEdit("betty2"));
        assertFalse(userCanEdit("bb"));

        permissionScheme = administration.permissionSchemes().defaultScheme();
        // remove by userkey
        permissionScheme.removePermission(PermissionSchemes.Type.EDIT_ISSUES, "bb");
        assertFalse(userCanEdit("betty2"));

        // Single User Permission for bob (recycled username)
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.grantPermissionToSingleUser(PermissionSchemes.Type.EDIT_ISSUES, "bb");
        assertTrue(userCanEdit("bb"));
        assertFalse(userCanEdit("betty2"));
    }

    public void testReporterPermission()
    {
        administration.restoreData("user_rename_permissions.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        // Remove existing permission - project role developer
        PermissionSchemes.PermissionScheme permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.removePermission(PermissionSchemes.Type.EDIT_ISSUES, "10001");

        assertFalse(userCanEditIssue("cat", "COW-1"));

        // Add reporter user permission
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.grantPermissionToReporter(PermissionSchemes.Type.EDIT_ISSUES);
        assertTrue(userCanEditIssue("cat", "COW-1"));
        assertFalse(userCanEditIssue("cc", "COW-1"));
        assertTrue(userCanEditIssue("cc", "COW-3"));
        assertFalse(userCanEditIssue("cat", "COW-3"));

        // Rename user cat to cat2
        EditUserPage editUserPage = administration.usersAndGroups().gotoEditUser("cat");
        editUserPage.setUsername("cat2");
        editUserPage.submitUpdate();

        // Check that cat2 can still edit
        assertTrue(userCanEditIssue("cat2", "COW-1"));
        assertFalse(userCanEditIssue("cc", "COW-1"));
        assertFalse(userCanEditIssue("cat2", "COW-3"));
    }

    public void testAssigneePermission()
    {
        administration.restoreData("user_rename_permissions.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        // Remove existing permission - project role developer
        PermissionSchemes.PermissionScheme permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.removePermission(PermissionSchemes.Type.EDIT_ISSUES, "10001");

        assertFalse(userCanEditIssue("betty", "COW-1"));

        // Add assignee user permission
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.grantPermissionToCurrentAssignee(PermissionSchemes.Type.EDIT_ISSUES);
        assertTrue(userCanEditIssue("betty", "COW-1"));
        assertFalse(userCanEditIssue("bb", "COW-1"));
        assertTrue(userCanEditIssue("bb", "COW-3"));
        assertFalse(userCanEditIssue("betty", "COW-3"));

        // Rename user betty to betty2
        EditUserPage editUserPage = administration.usersAndGroups().gotoEditUser("betty");
        editUserPage.setUsername("betty2");
        editUserPage.submitUpdate();

        // Check that betty2 can still edit
        assertTrue(userCanEditIssue("betty2", "COW-1"));
        assertFalse(userCanEditIssue("bb", "COW-1"));
        assertFalse(userCanEditIssue("betty2", "COW-3"));
    }

    public void testGroupPermission()
    {
        administration.restoreData("user_rename_permissions.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        // Remove existing permission - project role developer
        PermissionSchemes.PermissionScheme permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.removePermission(PermissionSchemes.Type.EDIT_ISSUES, "10001");

        assertFalse(userCanEditIssue("cat", "COW-1"));

        // Add group user permission
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.grantPermissionToGroup(PermissionSchemes.Type.EDIT_ISSUES, "jira-developers");
        assertTrue(userCanEditIssue("cat", "COW-1"));
        assertFalse(userCanEditIssue("cc", "COW-1"));

        // Rename user
        EditUserPage editUserPage = administration.usersAndGroups().gotoEditUser("cat");
        editUserPage.setUsername("cat2");
        editUserPage.submitUpdate();

        // Check that cat2 can still edit
        assertTrue(userCanEditIssue("cat2", "COW-1"));
        assertFalse(userCanEditIssue("cc", "COW-1"));

        // remove cat2 from jira-dev
        administration.usersAndGroups().removeUserFromGroup("cat2", "jira-developers");
        administration.usersAndGroups().addUserToGroup("cc", "jira-developers");
        assertFalse(userCanEditIssue("cat2", "COW-1"));
        assertTrue(userCanEditIssue("cc", "COW-1"));
    }

    public void testProjectLeadPermission()
    {
        administration.restoreData("user_rename_permissions.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        // Remove existing permission - project role developer
        PermissionSchemes.PermissionScheme permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.removePermission(PermissionSchemes.Type.EDIT_ISSUES, "10001");
        // set cat as project lead
        administration.project().setProjectLead("Bovine", "cat");

        assertFalse(userCanEditIssue("cat", "COW-1"));

        // Add project lead user permission
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.grantPermissionToProjectLead(PermissionSchemes.Type.EDIT_ISSUES);
        assertTrue(userCanEditIssue("cat", "COW-1"));
        assertFalse(userCanEditIssue("cc", "COW-1"));

        // Rename user
        EditUserPage editUserPage = administration.usersAndGroups().gotoEditUser("cat");
        editUserPage.setUsername("cat2");
        editUserPage.submitUpdate();

        // Check that cat2 can still edit
        assertTrue(userCanEditIssue("cat2", "COW-1"));
        assertFalse(userCanEditIssue("cc", "COW-1"));

        // set cc as project lead
        administration.project().setProjectLead("Bovine", "cc");
        assertFalse(userCanEditIssue("cat2", "COW-1"));
        assertTrue(userCanEditIssue("cc", "COW-1"));
    }

    public void testUserCFPermission()
    {
        administration.restoreData("user_rename_permissions.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        // Remove existing permission - project role developer
        PermissionSchemes.PermissionScheme permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.removePermission(PermissionSchemes.Type.EDIT_ISSUES, "10001");

        assertFalse(userCanEditIssue("cat", "COW-2"));
        assertFalse(userCanEditIssue("cc", "COW-1"));

        // Add User CF user permission
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.grantPermissionToUserCustomFieldValue(PermissionSchemes.Type.EDIT_ISSUES, "customfield_10300");
        assertTrue(userCanEditIssue("cat", "COW-2"));
        assertFalse(userCanEditIssue("cc", "COW-2"));
        assertTrue(userCanEditIssue("cc", "COW-1"));
        assertFalse(userCanEditIssue("cat", "COW-1"));

        // Rename user
        EditUserPage editUserPage = administration.usersAndGroups().gotoEditUser("cat");
        editUserPage.setUsername("cat2");
        editUserPage.submitUpdate();

        // Check that cat2 can still edit
        assertTrue(userCanEditIssue("cat2", "COW-2"));
        assertFalse(userCanEditIssue("cat2", "COW-1"));
    }

    public void testGroupCFPermission()
    {
        administration.restoreData("user_rename_permissions.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        // Remove existing permission - project role developer
        PermissionSchemes.PermissionScheme permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.removePermission(PermissionSchemes.Type.EDIT_ISSUES, "10001");

        assertFalse(userCanEditIssue("cat", "COW-5"));

        // Add group CF permission
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.grantPermissionToGroupCustomFieldValue(PermissionSchemes.Type.EDIT_ISSUES, "customfield_10400");
        assertTrue(userCanEditIssue("cat", "COW-5"));
        assertFalse(userCanEditIssue("cc", "COW-5"));

        // Rename user
        EditUserPage editUserPage = administration.usersAndGroups().gotoEditUser("cat");
        editUserPage.setUsername("cat2");
        editUserPage.submitUpdate();

        // Check that cat2 can still edit
        assertTrue(userCanEditIssue("cat2", "COW-5"));
        assertFalse(userCanEditIssue("cc", "COW-5"));

        // remove cat2 from jira-dev
        administration.usersAndGroups().removeUserFromGroup("cat2", "jira-developers");
        administration.usersAndGroups().addUserToGroup("cc", "jira-developers");
        assertFalse(userCanEditIssue("cat2", "COW-5"));
        assertTrue(userCanEditIssue("cc", "COW-5"));
    }

    public void testProjectRolePermission()
    {
        administration.restoreData("user_rename_permissions.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
        PermissionSchemes.PermissionScheme permissionScheme;

        // Default permission scheme is set up with project role developer permission based on group jira-dev
        assertTrue(userCanEditIssue("cat", "COW-1"));
        assertFalse(userCanEditIssue("cc", "COW-1"));

        // Rename user
        EditUserPage editUserPage = administration.usersAndGroups().gotoEditUser("cat");
        editUserPage.setUsername("cat2");
        editUserPage.submitUpdate();

        // Check that cat2 can still edit
        assertTrue(userCanEditIssue("cat2", "COW-1"));
        assertFalse(userCanEditIssue("cc", "COW-1"));

        // remove cat2 from jira-dev
        administration.usersAndGroups().removeUserFromGroup("cat2", "jira-developers");
        administration.usersAndGroups().addUserToGroup("cc", "jira-developers");
        assertFalse(userCanEditIssue("cat2", "COW-1"));
        assertTrue(userCanEditIssue("cc", "COW-1"));

        // Now lets swap permission to another role
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.removePermission(PermissionSchemes.Type.EDIT_ISSUES, "10001");
        // Add role permission
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.grantPermissionToProjectRole(PermissionSchemes.Type.EDIT_ISSUES, "10002");

        assertFalse(userCanEditIssue("cat2", "COW-1"));
        assertFalse(userCanEditIssue("cc", "COW-1"));


        // Swap to a user based role "Cats" which only includes cat
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.removePermission(PermissionSchemes.Type.EDIT_ISSUES, "10002");
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.grantPermissionToProjectRole(PermissionSchemes.Type.EDIT_ISSUES, "10003");

        assertTrue(userCanEditIssue("cat2", "COW-1"));
        assertFalse(userCanEditIssue("cc", "COW-1"));

        // Rename user
        editUserPage = administration.usersAndGroups().gotoEditUser("cat2");
        editUserPage.setUsername("cat");
        editUserPage.submitUpdate();

        assertTrue(userCanEditIssue("cat", "COW-1"));
        assertFalse(userCanEditIssue("cc", "COW-1"));


        // Swap to a user based role "Dogs" which only includes Candy
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.removePermission(PermissionSchemes.Type.EDIT_ISSUES, "10003");
        permissionScheme = administration.permissionSchemes().defaultScheme();
        permissionScheme.grantPermissionToProjectRole(PermissionSchemes.Type.EDIT_ISSUES, "10004");

        assertFalse(userCanEditIssue("cat", "COW-1"));
        assertTrue(userCanEditIssue("cc", "COW-1"));
    }

    private boolean userCanEdit(String username)
    {
        return userCanEditIssue(username, "COW-1");
    }

    private boolean userCanEditIssue(String username, String issueKey)
    {
        String password = username;
        if (username.equals("betty2"))
            password = "betty";
        if (username.equals("cat2"))
            password = "cat";
        try
        {
            navigation.login(username, password);
            ViewIssuePage viewIssuePage = navigation.issue().viewIssue(issueKey);
            return viewIssuePage.containsEditButton();
        }
        finally
        {
            navigation.login("admin");
        }
    }
}
