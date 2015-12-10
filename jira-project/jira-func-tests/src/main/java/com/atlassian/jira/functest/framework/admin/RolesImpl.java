package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.jira.testkit.client.restclient.ProjectClient;
import com.atlassian.jira.testkit.client.restclient.ProjectRoleClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;

import java.util.List;

public class RolesImpl extends AbstractFuncTestUtil implements Roles
{
    private static final String DELETE_ROLE = "/secure/project/DeleteProjectRole!default.jspa?id=";
    private static final String ADD_ROLE = "/secure/project/UserRoleActorAction!addUsers.jspa?projectRoleId=";

    private RoleDetails roleDetails;

    public RolesImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
        roleDetails = new DefaultRoleDetails(tester);
    }

    public void delete(long roleId)
    {
        log("Deleting project role " + roleId);
        tester.gotoPage(DELETE_ROLE + roleId);
        tester.submit("Delete");            
    }

    public void delete(final String name)
    {
        log("Deleting project role:" + name);

        gotoProjectRolesScreen();

        tester.clickLink("delete_" + name);
        tester.submit("Delete");
    }

    private void gotoProjectRolesScreen()
    {
        // Only go to admin if we are not there.
        // We figure this out by checking whether the admin left-hand side menu is present.
        if (new IdLocator(tester, "adminMenu").getNodes().length == 0)
        { 
            getFuncTestHelperFactory().getNavigation().gotoAdmin();
        }
        tester.clickLink("project_role_browser");
    }

    public void create(String name, String description)
    {
        gotoProjectRolesScreen();

        tester.setFormElement("name", name);
        tester.setFormElement("description", description);
        tester.submit("Add Project Role");
        tester.assertTextPresent(name);
        tester.assertTextPresent(description);
    }

    public RoleDetails edit(String name)
    {
        gotoProjectRolesScreen();
        tester.clickLink("edit_" + name);
        return roleDetails;
    }

    private com.atlassian.jira.testkit.client.restclient.Project getProjectByName(String projectName)
    {
        ProjectClient projectClient = new ProjectClient(environmentData);

        final List<com.atlassian.jira.testkit.client.restclient.Project> projects = projectClient.getProjects();
        for (Project project : projects)
        {
            if (project.name.equals(projectName))
            {
                return project;
            }
        }
        return null;

    }

    private void addUserToProjectRole(String userName, String projectName, String roleName)
    {
        final Project projectByName = getProjectByName(projectName);

        if (projectByName == null)
        {
            Assert.fail("A project with the name '" + projectName + "' does not exist.");
        }

        ProjectRoleClient projectRoleClient = new ProjectRoleClient(environmentData);
        final Response response = projectRoleClient.addActors(projectByName.key, roleName, null, new String[] { userName });
    }

    public void addProjectRoleForUser(String projectName,String roleName, String userName)
    {
        addUserToProjectRole(userName, projectName, roleName);
    }
}