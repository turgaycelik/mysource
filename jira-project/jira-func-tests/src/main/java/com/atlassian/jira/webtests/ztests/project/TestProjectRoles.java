package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.ProjectRoleClient;
import com.atlassian.jira.testkit.client.restclient.Response;

/**
 * Responsible for testing project roles.
 */
@WebTest ({ Category.FUNC_TEST, Category.PROJECTS, Category.ROLES })
public class TestProjectRoles extends FuncTestCase
{
    private static final String ROLE_DESC = "this is a test role";
    private static final String ROLE_NAME = "test role";
    private static final String ROLE_NAME_UPPER_CASE = "Test role";
    private static final String ROLE_UPDATED_NAME = "testing role";
    private static final String ROLE_UPDATED_DESC = "this is an updated description";
    private static final String EDIT_USER_ACTORS = "edit_10000_atlassian-user-role-actor";
    private static final String EDIT_GROUP_ACTORS = "edit_10000_atlassian-group-role-actor";
    private static final String DELETE_ROLE = "delete_test role";
    private static final String APOSTROPHE_ENTITY = "&#39;";

    public void testProjectRoleOperations()
    {
        administration.restoreBlankInstance();

        administration.roles().create(ROLE_NAME, ROLE_DESC);
        editProjectRole(ROLE_UPDATED_NAME);

        // JRA-13157 - checks that you can change the case of a role name, this would probably only error on a case-insensitive
        // database, but is a good test to have anyway.
        editProjectRole(ROLE_NAME_UPPER_CASE);

        ensureErrorForDuplicateRoleName();

        administration.roles().delete(ROLE_NAME);

        text.assertTextNotPresent(locator.id("project-role-" + ROLE_NAME), ROLE_NAME);
        text.assertTextNotPresent(locator.id("project-role-" + ROLE_NAME), ROLE_DESC);
    }

    public Response addGroupToProjectRole(String groupName, String projectKey, String roleName)
    {

        ProjectRoleClient projectRoleClient = new ProjectRoleClient(environmentData);
        return projectRoleClient.addActors(projectKey, roleName, new String[] { groupName }, null);
    }


    public Response addUserToProjectRole(String userName, String projectKey, String roleName)
    {
        ProjectRoleClient projectRoleClient = new ProjectRoleClient(environmentData);
        return projectRoleClient.addActors(projectKey, roleName, null, new String[] { userName });
    }


    public void testCreateIssueWithRolePermissions()
    {
        administration.restoreData("TestProjectRoles.xml");

        // Make sure we cannot create an issue
        navigation.gotoDashboard();
        tester.assertLinkNotPresent("create_link");

        // Assign the admin to the test role for Homosapien
        addUserToProjectRole(ADMIN_USERNAME, "HSP", ROLE_NAME);

        // Assert create link is now present
        navigation.gotoDashboard();
        tester.assertLinkPresent("create_link");
        tester.clickLink("create_link");

        // Assert Monkey Project option is not present since we only assigned the user to Homosapien
        tester.assertOptionsEqual("pid", new String[] { "homosapien" });

        // Assign the admin to the test role for Homosapien
        addUserToProjectRole(ADMIN_USERNAME, "MKY", ROLE_NAME);

        // Assert create link is now present
        tester.assertLinkPresent("create_link");
        tester.clickLink("create_link");

        // Assert Monkey Project option is NOW present since we only assigned the user to Homosapien
        tester.assertOptionsEqual("pid", new String[] { "homosapien", "monkey" });
    }

    public void testCreateIssueWithRolePermissionsForGroup()
    {
        administration.restoreData("TestProjectRoles.xml");

        // Make sure we cannot create an issue
        navigation.gotoDashboard();
        tester.assertLinkNotPresent("create_link");

        // Assign the admin to the test role for Homosapien
        addGroupToProjectRole("jira-administrators", "HSP", ROLE_NAME);


        // Assert create link is now present
        navigation.gotoDashboard();
        tester.assertLinkPresent("create_link");
        tester.clickLink("create_link");

        // Assert Monkey Project option is not present since we only assigned the user to Homosapien
        tester.assertOptionsEqual("pid", new String[] { "homosapien" });

        // Assign the admin to the test role for Homosapien
        addGroupToProjectRole("jira-administrators", "MKY", ROLE_NAME);

        // Assert create link is now present
        navigation.gotoDashboard();
        tester.assertLinkPresent("create_link");
        tester.clickLink("create_link");

        // Assert Monkey Project option is NOW present since we only assigned the user to Homosapien
        tester.assertOptionsEqual("pid", new String[] { "homosapien", "monkey" });
    }

    public void testAddDefaultUsersAndDefaultGroupsToRole()
    {
        administration.restoreData("TestProjectRoles.xml");
        addAdministratorToDefaultRole();

        // Assert that an error is thrown on the default screen for adding a user that does not exist
        navigation.gotoAdmin();
        tester.clickLink("project_role_browser");
        tester.clickLink("manage_test role");
        tester.clickLink(EDIT_USER_ACTORS);
        tester.setFormElement("userNames", "detkin");
        tester.submit("add");
        text.assertTextSequence(locator.css(".jiraform .aui-message.error"), "detkin", "could not be found");

        // Check adding a member that's already a member of the group throws an error
        navigation.gotoAdmin();
        tester.clickLink("project_role_browser");
        tester.clickLink("manage_test role");
        tester.clickLink(EDIT_USER_ACTORS);
        tester.setFormElement("userNames", ADMIN_USERNAME);
        tester.submit("add");
        text.assertTextSequence(locator.css(".jiraform .aui-message.error"), ADMIN_USERNAME, "already a member");

        // Assert that we can add default groups
        addGroupsToDefaultRole();

        // Test the deleting of a user from a default role
        deleteDefaultUsersForRole();

        // Test the deletion of a group from a default role
        deleteDefaultGroupsForRole();
    }

    public void testDeleteProjectRoleRemovesSchemeEntries()
    {
        administration.restoreData("TestProjectRoles.xml");
        navigation.gotoAdmin();
        tester.clickLink("project_role_browser");
        // delete the project role test_role
        deleteProjectRole(true);

        // now make sure that the test-role entry was deleted from the schemes
        tester.clickLink("notification_schemes");
        tester.clickLinkWithText("Notifications");
        tester.assertTextNotPresent(ROLE_NAME);

        administration.permissionSchemes().defaultScheme();
        tester.assertTextNotPresent(ROLE_NAME);

        administration.issueSecuritySchemes().getScheme("Default Issue Security Scheme");
        tester.assertTextNotPresent(ROLE_NAME);
    }

    public void testDeleteProjectRoleWorkflowConditions()
    {
        administration.restoreData("TestProjectRoles.xml");

        //check workflow condition is associated with project role
        administration.workflows().goTo().workflowSteps("Copy of jira workflow");
        tester.clickLinkWithText("Start Progress");
        tester.assertTextPresent("Only users in project role <b>test role</b> can execute this transition.");

        //delete test project role (being used by a workflow condition)
        tester.clickLink("project_role_browser");
        tester.setFormElement("name", "");
        tester.clickLink("delete_test role");
        tester.assertTextPresent("The following <strong>1</strong> workflow actions contain conditions that rely on the project role <strong>test role</strong>. If you delete this project role, these conditions will always fail.");
        tester.submit("Delete");

        //check workflow condition displays "missing project role" message correctly
        administration.workflows().goTo().workflowSteps("Copy of jira workflow");
        tester.clickLinkWithText("Start Progress");
        tester.assertTextPresent("Project Role (id=10000) is missing, now this condition will always fail.");

        //check workflow can still be updated
        //this gotoPage is the equivalent of pressing the "edit" link - don't want tester.clickLink() to conflict with other edit links
        tester.gotoPage("/secure/admin/workflows/EditWorkflowTransitionConditionParams!default.jspa?workflowStep=1&workflowTransition=4&count=2&workflowName=Copy+of+jira+workflow&workflowMode=live");
        tester.selectOption("jira.projectrole.id", "Users");
        tester.submit("Update");
        tester.assertTextPresent("Only users in project role <b>Users</b> can execute this transition.");
    }

    /**
     * Tests to see if the view usages screen contains all the associations it should
     */
    public void testViewUsages()
    {
        administration.restoreData("TestProjectRoleViewUsages.xml");
        gotoViewUsagesForUsersProjectRole();

        assertions.getLinkAssertions().assertLinkPresentWithExactTextById("relatednotificationschemes", "Default Notification Scheme");
        assertions.getLinkAssertions().assertLinkPresentWithExactTextById("relatednotificationschemes", "Other Notification Scheme");

        assertions.getLinkAssertions().assertLinkPresentWithExactTextById("relatedpermissionschemes", "Default Permission Scheme");
        assertions.getLinkAssertions().assertLinkPresentWithExactTextById("relatedpermissionschemes", "Other Permission Scheme");

        text.assertTextSequence(locator.table("relatedpermissionschemes"), new String[] {
                "Default Permission Scheme", "homosapien", "3 (View)", "monkey", "2 (View)", "test", "1 (View)",
                "Other Permission Scheme", "None", "None"
        });

        tester.assertLinkPresentWithText("test issue security scheme");

        tester.assertLinkPresentWithText("jira workflow");
        tester.assertLinkPresentWithText("Start Progress");
        tester.assertLinkPresentWithText("Stop Progress");

        // Now make sure all the links work
        tester.clickLinkWithText("Default Notification Scheme");
        tester.assertTextPresent("Edit Notifications &mdash; Default Notification Scheme");
        tester.assertLinkPresentWithText("Add notification");

        gotoViewUsagesForUsersProjectRole();
        tester.clickLinkWithText("Other Notification Scheme");
        tester.assertTextPresent("Edit Notifications &mdash; Other Notification Scheme");
        tester.assertLinkPresentWithText("Add notification");

        gotoViewUsagesForUsersProjectRole();
        tester.clickLinkWithText("Other Permission Scheme");
        tester.assertTextPresent("Edit Permissions &mdash; Other Permission Scheme");
        tester.assertLinkPresentWithText("Grant permission");

        gotoViewUsagesForUsersProjectRole();
        tester.clickLinkWithText("Default Permission Scheme");
        tester.assertTextPresent("Edit Permissions &mdash; Default Permission Scheme");
        tester.assertLinkPresentWithText("Grant permission");

        gotoViewUsagesForUsersProjectRole();
        tester.clickLinkWithText("test issue security scheme");
        tester.assertTextPresent("Edit Issue Security Levels");
        tester.assertTextPresent("test security level");

        gotoViewUsagesForUsersProjectRole();
        tester.clickLinkWithText("Start Progress");
        tester.assertTextPresent("Transition: Start Progress");

        gotoViewUsagesForUsersProjectRole();
        tester.clickLinkWithText("Stop Progress");
        tester.assertTextPresent("Transition: Stop Progress");

        // Test one of the project links
        gotoViewUsagesForUsersProjectRole();
        tester.clickLinkWithText("homosapien");
        assertions.assertNodeByIdHasText("project-config-header-name",  "homosapien");

        gotoViewUsagesForUsersProjectRole();
        tester.clickLink("view_project_role_actors_10000");
        tester.assertElementPresent("project-config-panel-people");
    }

    private void gotoViewUsagesForUsersProjectRole()
    {
        navigation.gotoAdmin();
        tester.clickLink("project_role_browser");

        // browse to the user role
        tester.clickLink("view_Users");
    }


    private void addGroupsToDefaultRole()
    {
        navigation.gotoAdmin();
        tester.clickLink("project_role_browser");
        tester.clickLink("manage_test role");
        tester.assertTextPresent("None selected");
        tester.clickLink(EDIT_GROUP_ACTORS);
        tester.assertTextPresent("Assign Default Groups to Project Role: test role");
        tester.setFormElement("groupNames", "jira-users, jira-developers, jira-administrators");
        tester.submit("add");
        tester.clickLink("return_link");
        tester.assertTextPresent("jira-administrators");
        tester.assertTextPresent("jira-users");
        tester.assertTextPresent("jira-developers");
        tester.clickLink("return_link");
        tester.assertTextPresent("Project Role Browser");
    }

    private void addAdministratorToDefaultRole()
    {
        navigation.gotoAdmin();
        tester.clickLink("project_role_browser");
        tester.clickLink("manage_test role");
        tester.assertTextPresent("None selected.");
        tester.clickLink(EDIT_USER_ACTORS);
        tester.assertTextPresent("Assign Default Users to Project Role: test role");
        tester.setFormElement("userNames", "admin, fred");
        tester.submit("add");
        tester.clickLink("return_link");
        tester.assertTextPresent(ADMIN_FULLNAME);
        tester.clickLink("return_link");
        tester.assertTextPresent("Project Role Browser");
    }

    private void deleteDefaultUsersForRole()
    {
        navigation.gotoAdmin();
        tester.clickLink("project_role_browser");
        tester.clickLink("manage_test role");
        tester.clickLink(EDIT_USER_ACTORS);
        tester.assertTextPresent("Assign Default Users to Project Role: test role");
        tester.checkCheckbox("removeusers_admin", ".");
        tester.checkCheckbox("removeusers_fred", ".");
        tester.submit("remove");
        tester.assertTextPresent("There are currently no users assigned to this project role.");
    }

    private void deleteDefaultGroupsForRole()
    {
        navigation.gotoAdmin();
        tester.clickLink("project_role_browser");
        tester.clickLink("manage_test role");
        deleteGroupsFromRoleForm("Assign Default Groups to Project Role: test role");
    }


    private void deleteGroupsFromRoleForm(String textPresent)
    {
        tester.clickLink(EDIT_GROUP_ACTORS);
        tester.assertTextPresent(textPresent);
        tester.checkCheckbox("removegroups_jira-users", ".");
        tester.checkCheckbox("removegroups_jira-developers", ".");
        tester.checkCheckbox("removegroups_jira-administrators", ".");
        tester.submit("remove");
        tester.assertTextPresent("There are currently no groups assigned to this project role");
    }



    private void deleteProjectRole(boolean checkAssociatedSchemes)
    {
        tester.clickLink(DELETE_ROLE);
        tester.assertTextPresent("Are you sure you would like to delete project role");
        tester.assertTextPresent("test role");
        if (checkAssociatedSchemes)
        {
            tester.assertLinkPresentWithText("Default Notification Scheme");
            tester.assertLinkPresentWithText("Default Permission Scheme");
            tester.assertLinkPresentWithText("Default Issue Security Scheme");

            // we need to show associated workflows as well, check they are present
            tester.assertTextPresent("Copy of jira workflow");
            // check the associated step:
            tester.assertLinkPresentWithText("Start Progress");
        }
        tester.submit("Delete");

        text.assertTextNotPresent(locator.id("project-role-" + ROLE_NAME), ROLE_NAME);
        text.assertTextNotPresent(locator.id("project-role-" + ROLE_NAME), ROLE_DESC);
    }

    private void ensureErrorForDuplicateRoleName()
    {
        tester.setFormElement("name", "test role");
        tester.setFormElement("description", "");
        tester.submit("Add Project Role");
        tester.assertTextPresent("A project role with name &#39;test role&#39; already exists.");
    }

    private void ensureErrorForDuplicateRoleNameOnEdit()
    {
        tester.clickLink("project_role_browser");
        tester.setFormElement("name", "anotherRole");
        tester.setFormElement("description", "test");
        tester.submit("Add Project Role");
        tester.clickLink("edit_anotherRole");
        tester.setFormElement("name", ROLE_NAME);
        tester.submit("Update");
        tester.assertTextPresent("A project role with name &#39;test role&#39; already exists.");
    }

    private void editProjectRole(String newRoleName)
    {
        administration.roles().edit("test role").setName(newRoleName);
        administration.roles().edit(newRoleName).setDescription(ROLE_UPDATED_DESC);

        tester.assertTextPresent(newRoleName);
        tester.assertTextPresent(ROLE_UPDATED_DESC);
        
        administration.roles().edit(newRoleName).setName(ROLE_NAME);
        administration.roles().edit(ROLE_NAME).setDescription(ROLE_DESC);
    }

}