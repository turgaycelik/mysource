package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.jira.testkit.client.restclient.ProjectClient;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.PROJECTS })
public class TestEditProject extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestEditProject.xml");
    }

    public void testProjectDoesNotExistAdmin() throws Exception
    {
        tester.gotoPage("secure/project/EditProject!default.jspa?pid=999999");
        tester.assertTextPresent("There is not a project with the specified id. Perhaps it was deleted.");
    }

    public void testProjectDoesNotExistNonAdmin() throws Exception
    {
        // First try anonymous
        navigation.logout();
        tester.gotoPage("secure/project/EditProject!default.jspa?pid=999999");
        tester.assertTextPresent("You must log in to access this page.");

        // Now log in as non-admin
        navigation.login("gandhi");
        tester.gotoPage("secure/project/EditProject!default.jspa?pid=999999");
        tester.assertTextPresent("Access Denied");
        tester.assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
        tester.assertTextNotPresent("You cannot view this URL as a guest.");
    }

    public void testEditProjectSuccess()
    {
        ProjectClient pc = new ProjectClient(environmentData);

        Project dog = pc.get("DOG");
        assertEquals("DOG", dog.key);
        assertEquals("Canine", dog.name);
        assertEquals("", dog.description);
        assertEquals(null, dog.url);
        assertEquals("murray", dog.lead.name);
        assertEquals(Project.AssigneeType.PROJECT_LEAD, dog.assigneeType);

        tester.gotoPage("/secure/project/EditProject!default.jspa?pid=" + dog.id);
        tester.assertTextPresent("Edit Project: Canine");

        //lets change all fields
        tester.setFormElement("name", "Dogs");
        tester.setFormElement("url", "http://www.dogs.com");
        tester.setFormElement("description", "This is the dog project.");
        tester.submit("Update");

        dog = pc.get("DOG");
        assertThat(dog.name, equalTo("Dogs"));

        //now check that all information has been updated correctly.
        final Project newDog = pc.get("DOG");
        assertEquals("DOG", newDog.key);
        assertEquals("Dogs", newDog.name);
        assertEquals("This is the dog project.", newDog.description);
        assertEquals("http://www.dogs.com", newDog.url);
        assertEquals("murray", newDog.lead.name);
        assertEquals(Project.AssigneeType.PROJECT_LEAD, newDog.assigneeType);

    }

    public void testEditProjectNameExists()
    {
        Long projectId = backdoor.project().getProjectId("DOG");
        tester.gotoPage("/secure/project/EditProject!default.jspa?pid=" + projectId);
        tester.assertTextPresent("Edit Project: Canine");
        tester.setFormElement("name", "Bovine");
        tester.submit("Update");

        tester.assertTextPresent("A project with that name already exists.");
    }

    public void testEditProjectValidation()
    {
        Long projectId = backdoor.project().getProjectId("DOG");
        tester.gotoPage("/secure/project/EditProject!default.jspa?pid=" + projectId);
        tester.assertTextPresent("Edit Project: Canine");
        tester.setFormElement("name", "");
        tester.setFormElement("url", "badURL");
        tester.submit("Update");

        tester.assertTextPresent("You must specify a valid project name.");
        tester.assertTextPresent("The URL specified is not valid - it must start with http://");

        tester.assertTextPresent("Edit Project: Canine");
        tester.setFormElement("name", "");
        tester.setFormElement("url", "badURL");
        tester.submit("Update");
    }

    public void testEditProjectNoAccess()
    {
        navigation.logout();
        navigation.login("cow", "cow");

        //try to create a project without the right permissions.
        tester.gotoPage("secure/admin/EditProject.jspa?pid=10020&name=newproject&lead=admin&atl_token=" + page.getXsrfToken());
        tester.assertTextPresent("Welcome to Dev JIRA");
        tester.assertTextNotPresent("Edit Project: Canine");
        tester.assertTextNotPresent("Use this page to update your project details.");
    }

    public void testEditProjectDoesntExist()
    {
        tester.gotoPage("secure/admin/EditProject.jspa?pid=10025&name=newproject&lead=admin&atl_token=" + page.getXsrfToken());
        tester.assertTextPresent("There is not a project with the specified id. Perhaps it was deleted.");
    }

    public void testEditProjectChangeNothing()
    {
        Long projectId = backdoor.project().getProjectId("DOG");
        tester.gotoPage("/secure/project/EditProject!default.jspa?pid=" + projectId);
        tester.assertTextPresent("Edit Project: Canine");

        //lets change all fields
        tester.setFormElement("name", "Canine");
        tester.setFormElement("url", "");
        tester.setFormElement("description", "");
        tester.submit("Update");

        //verify all the project data is untouched.
        //now check that all information has been updated correctly.
        ProjectClient pc = new ProjectClient(environmentData);

        final Project dog = pc.get("DOG");
        assertEquals("DOG", dog.key);
        assertEquals("Canine", dog.name);
        assertEquals("", dog.description);
        assertEquals(null, dog.url);
        assertEquals("murray", dog.lead.name);
        assertEquals(Project.AssigneeType.PROJECT_LEAD, dog.assigneeType);


    }

    public void testEditWithFieldsExceedingLimits() throws Exception
    {
        ProjectClient pc = new ProjectClient(environmentData);

        final Project dog = pc.get("DOG");
        assertEquals("DOG", dog.key);
        assertEquals("Canine", dog.name);
        assertEquals("", dog.description);
        assertEquals(null, dog.url);
        assertEquals("murray", dog.lead.name);
        assertEquals(Project.AssigneeType.PROJECT_LEAD, dog.assigneeType);

        Long projectId = backdoor.project().getProjectId("DOG");
        tester.gotoPage("/secure/project/EditProject!default.jspa?pid=" + projectId);

        tester.assertTextPresent("Edit Project: Canine");

        tester.setFormElement("name", StringUtils.repeat("N", 81));
        tester.setFormElement("url", StringUtils.repeat("U", 256));
        tester.submit("Update");

        tester.assertTextPresent("The project name must not exceed 80 characters in length");
        tester.assertTextPresent("The URL must not exceed 255 characters in length");
    }

    public void testEditProjectWithoutBrowseProjectPermission() throws Exception
    {
        Long projectId = backdoor.project().getProjectId("VG");
        tester.gotoPage("/secure/project/EditProject!default.jspa?pid=" + projectId);
        tester.setFormElement("name", "Vegans");
        tester.submit("Update");

        //verify the project name has been updated.
        ProjectClient pc = new ProjectClient(environmentData);

        final Project dog = pc.get("VG");
        assertEquals("VG", dog.key);
        assertEquals("Vegans", dog.name);

    }
}
