package com.atlassian.jira.webtests.ztests.user;

import java.util.Collection;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.user.DeleteUserPage;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.webtests.Groups;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static com.atlassian.jira.permission.ProjectPermissions.ADD_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.ADMINISTER_PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissions.ASSIGN_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.BROWSE_PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissions.CLOSE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.CREATE_ATTACHMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.CREATE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_ALL_ATTACHMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_ALL_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_ALL_WORKLOGS;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_OWN_ATTACHMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_OWN_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_OWN_WORKLOGS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_ALL_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_ALL_WORKLOGS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_OWN_COMMENTS;
import static com.atlassian.jira.permission.ProjectPermissions.EDIT_OWN_WORKLOGS;
import static com.atlassian.jira.permission.ProjectPermissions.LINK_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.MANAGE_WATCHERS;
import static com.atlassian.jira.permission.ProjectPermissions.MOVE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.RESOLVE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.SCHEDULE_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.TRANSITION_ISSUES;
import static com.atlassian.jira.permission.ProjectPermissions.VIEW_DEV_TOOLS;
import static com.atlassian.jira.permission.ProjectPermissions.VIEW_READONLY_WORKFLOW;
import static com.atlassian.jira.permission.ProjectPermissions.WORK_ON_ISSUES;
import static com.atlassian.jira.webtests.WebTestCaseWrapper.logSection;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS, Category.USERS_AND_GROUPS })
public class TestDeleteUserAndPermissions extends FuncTestCase
{
    /**
     * This file import contains two projects. One project has a specific permission scheme that only allows user fred
     * to perform any instructions. Both fred and admin have public filters set up with each subscribing to each other's
     * filters. There are two issues created by fred, one in each of the two projects
     */
    private static final String TWO_PROJECTS_WITH_SUBSCRIPTIONS = "TestDeleteUserAndPermissions.xml";

    @Override
    protected void setUpTest()
    {
        administration.restoreData(TWO_PROJECTS_WITH_SUBSCRIPTIONS);
        administration.addGlobalPermission(BULK_CHANGE, Groups.USERS);
        backdoor.darkFeatures().enableForSite("jira.no.frother.reporter.field");
    }

    @Override
    protected void tearDownTest()
    {
        backdoor.darkFeatures().disableForSite("jira.no.frother.reporter.field");
        administration.removeGlobalPermission(BULK_CHANGE, Groups.USERS);
    }

    //------------------------------------------------------------------------------------------------------------ Tests

    public void testDeleteUserNotPossibleWithAssignedIssue()
    {
        logSection("Test Delete User Not Possible With Assigned Issue");

        log("Making sure that you can't see the other issue");
        navigation.issueNavigator().displayAllIssues();
        tester.assertTextPresent("Seen issue");
        tester.assertTextNotPresent("Unseen issue");

        log("Ensuring that you're unable to delete fred and that the correct number of issues are shown");
        DeleteUserPage deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters(FRED_USERNAME));
        assertThat(deleteUserPage.getUserDeletionError(), equalTo(deleteUserPage.getUserCannotBeDeleteMessage(FRED_USERNAME)));
        assertThat(deleteUserPage.getNumberFromErrorFieldNamed(DeleteUserPage.ASSIGNED_ISSUES), equalTo("1"));
        assertThat(deleteUserPage.getNumberFromErrorFieldNamed(DeleteUserPage.REPORTED_ISSUES), equalTo("2"));
        assertThat(deleteUserPage.getNumberFromWarningFieldNamed(DeleteUserPage.SHARED_FILTERS), equalTo("1"));
    }

    public void testDeleteUserRemoveFromPermissionAndNotificationSchemes()
    {
        logSection("Test delete user with shared filters");

        assertFredHasPermissionsAssigned();

        // assert Fred is in issue security scheme
        navigation.gotoAdmin();
        tester.clickLink("security_schemes");
        tester.clickLinkWithText("Test Issue Security Scheme");
        tester.assertTextPresent(FRED_USERNAME);

        // assert Fred is in Default Notification Scheme
        navigation.gotoAdmin();
        tester.clickLink("notification_schemes");
        tester.clickLinkWithText("Default Notification Scheme");
        tester.assertTextPresent(FRED_USERNAME);
        tester.assertLinkPresent("del_10050");

        // add permissions to admin so we can edit MKY-1
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLink("10000_edit");
        tester.clickLink("add_perm_" + EDIT_ISSUES.permissionKey());
        tester.checkCheckbox("type", "user");
        tester.setFormElement("user", ADMIN_USERNAME);
        tester.submit(" Add ");
        tester.clickLink("add_perm_" + BROWSE_PROJECTS.permissionKey());
        tester.checkCheckbox("type", "user");
        tester.setFormElement("user", ADMIN_USERNAME);
        tester.submit(" Add ");
        tester.clickLink("add_perm_" + ProjectPermissions.MODIFY_REPORTER.permissionKey());
        tester.checkCheckbox("type", "user");
        tester.setFormElement("user", ADMIN_USERNAME);
        tester.submit(" Add ");

        // remove fred from issue
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLink("edit-issue");
        tester.setFormElement("reporter", ADMIN_USERNAME);
        tester.submit("Update");
        navigation.issue().gotoIssue("MKY-1");
        tester.clickLink("edit-issue");
        tester.setFormElement("reporter", ADMIN_USERNAME);
        tester.selectOption("assignee", ADMIN_FULLNAME);
        tester.submit("Update");

        log("Deleting Fred");
        navigation.gotoAdmin();
        tester.clickLink("user_browser");
        tester.clickLink(FRED_USERNAME);
        tester.clickLink("deleteuser_link");
        tester.assertTextPresent("Delete User");
        tester.submit("Delete");
        assertions.assertNodeByIdExists("user_browser_table");
        tester.assertTextNotPresent(FRED_USERNAME);

        assertFredHasNoPermissionsAssigned();

        // assert Fred isn't in issue security scheme
        navigation.gotoAdmin();
        tester.clickLink("security_schemes");
        tester.clickLinkWithText("Test Issue Security Scheme");
        tester.assertTextNotPresent(FRED_USERNAME);

        // assert Fred isn't in Default Notification Scheme
        navigation.gotoAdmin();
        tester.clickLink("notification_schemes");
        tester.clickLinkWithText("Default Notification Scheme");
        tester.assertTextNotPresent(FRED_USERNAME);
        tester.assertLinkNotPresent("del_10050");
    }

    public void testCannotSelfDestruct()
    {
        administration.restoreBlankInstance();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        navigation.gotoAdmin();
        tester.clickLink("user_browser");

        DeleteUserPage deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParametersWithXsrf("admin", page));
        assertThat(deleteUserPage.getUserDeleteSelfError(), equalTo(DeleteUserPage.USER_CANNOT_DELETE_SELF));
        deleteUserPage.confirmNoDeleteButtonPresent();
    }

    public void testAdminCannotDeleteSysadmin()
    {
        administration.restoreBlankInstance();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        try
        {
            // create a sysadmin to attempt to delete (also not a project lead like "admin")
            administration.usersAndGroups().addUser("sysadmin2");
            administration.usersAndGroups().addUserToGroup("sysadmin2", "jira-administrators"); // should have system administrator permission now

            // create a normal admin (non system admin)
            administration.usersAndGroups().addUser("nonsystemadmin");
            administration.usersAndGroups().addGroup("nonsystemadmins");
            administration.usersAndGroups().addUserToGroup("nonsystemadmin", "nonsystemadmins");
            administration.addGlobalPermission(ADMINISTER, "nonsystemadmins");

            navigation.login("nonsystemadmin");

            navigation.gotoAdmin();
            tester.clickLink("user_browser");
            tester.assertLinkNotPresent("deleteuser_link_sysadmin2");
            // Hack the url
            DeleteUserPage deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParametersWithXsrf("sysadmin2", page));
            assertThat(deleteUserPage.getNonAdminDeletingSysAdminErrorMessage(), equalTo(DeleteUserPage.USER_CANNOT_DELETE_SYSADMIN));
            deleteUserPage.confirmNoDeleteButtonPresent();

            // try to hack the URL for getting to the form to delete the user
            deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParametersWithXsrf("sysadmin2", page) + "&confirm=true");
            assertThat(deleteUserPage.getNonAdminDeletingSysAdminErrorMessage(), equalTo(DeleteUserPage.USER_CANNOT_DELETE_SYSADMIN));

            navigation.gotoAdmin();
            tester.clickLink("user_browser");
            tester.assertLinkPresent("sysadmin2"); // check user is still in user list

        }
        finally
        {
            navigation.login(ADMIN_USERNAME);
        }
    }

    private void assertFredHasPermissionsAssigned()
    {
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLinkWithText("Default Permission Scheme");
        tester.assertTextNotPresent(FRED_USERNAME);
        tester.assertTextNotPresent("Fred");

        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLinkWithText("Fred's scheme");
        tester.assertLinkPresent("del_perm_" + ADMINISTER_PROJECTS.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + BROWSE_PROJECTS.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + CREATE_ISSUES.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + EDIT_ISSUES.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + SCHEDULE_ISSUES.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + MOVE_ISSUES.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + ProjectPermissions.ASSIGNABLE_USER.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + RESOLVE_ISSUES.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + CLOSE_ISSUES.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + ProjectPermissions.MODIFY_REPORTER.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + ADD_COMMENTS.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + DELETE_ALL_COMMENTS.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + DELETE_ISSUES.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + WORK_ON_ISSUES.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + LINK_ISSUES.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + CREATE_ATTACHMENTS.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + DELETE_ALL_ATTACHMENTS.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + VIEW_DEV_TOOLS.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + ProjectPermissions.VIEW_VOTERS_AND_WATCHERS.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + MANAGE_WATCHERS.permissionKey() + "_fred");
        tester.assertLinkPresent("del_perm_" + ProjectPermissions.SET_ISSUE_SECURITY.permissionKey() + "_fred");
    }

    private void assertFredHasNoPermissionsAssigned()
    {
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLinkWithText("Default Permission Scheme");
        tester.assertTextNotPresent(FRED_USERNAME);
        tester.assertTextNotPresent("Fred");

        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLinkWithText("Fred's scheme");
        tester.assertTextNotPresent(FRED_USERNAME);
        for (ProjectPermissionKey key : systemProjectPermissionKeys())
        {
            tester.assertLinkNotPresent("del_perm_" + key.permissionKey() + "_fred");
        }
    }

    private static Collection<ProjectPermissionKey> systemProjectPermissionKeys()
    {
        return asList(
            ADMINISTER_PROJECTS, BROWSE_PROJECTS, VIEW_DEV_TOOLS, VIEW_READONLY_WORKFLOW,
            CREATE_ISSUES, EDIT_ISSUES, TRANSITION_ISSUES, SCHEDULE_ISSUES,
            MOVE_ISSUES, ASSIGN_ISSUES, ProjectPermissions.ASSIGNABLE_USER, RESOLVE_ISSUES,
            CLOSE_ISSUES, ProjectPermissions.MODIFY_REPORTER, DELETE_ISSUES, LINK_ISSUES,
            ProjectPermissions.SET_ISSUE_SECURITY, ProjectPermissions.VIEW_VOTERS_AND_WATCHERS,
            MANAGE_WATCHERS, ADD_COMMENTS, EDIT_ALL_COMMENTS, EDIT_OWN_COMMENTS, DELETE_ALL_COMMENTS,
            DELETE_OWN_COMMENTS, CREATE_ATTACHMENTS, DELETE_ALL_ATTACHMENTS, DELETE_OWN_ATTACHMENTS,
            WORK_ON_ISSUES, EDIT_OWN_WORKLOGS, EDIT_ALL_WORKLOGS, DELETE_OWN_WORKLOGS, DELETE_ALL_WORKLOGS
        );
    }
}
