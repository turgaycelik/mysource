package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.jira.testkit.client.restclient.ProjectClient;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Func test for EditProjectLeadAndDefaultAssignee action. Closely based off {@link TestEditProject}
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.PROJECTS })
public class TestEditProjectLeadAndDefaultAssignee extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestEditProject.xml");
    }

    public void testProjectDoesNotExistAdmin() throws Exception
    {
        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=999999");
        tester.assertTextPresent("There is not a project with the specified id. Perhaps it was deleted.");
    }

    public void testProjectDoesNotExistNonAdmin() throws Exception
    {
        // First try anonymous
        navigation.logout();
        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=999999");
        tester.assertTextPresent("You must log in to access this page.");

        // Now log in as non-admin
        navigation.login("gandhi");
        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=999999");
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

        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=10020");

        //lets change all fields
        FormParameterUtil formParameterUtil = new FormParameterUtil(tester, "project-edit-lead-and-default-assignee","Update");
        formParameterUtil.addOptionToHtmlSelect("lead", new String[]{ADMIN_USERNAME});
        formParameterUtil.setFormElement("lead", ADMIN_USERNAME);
        formParameterUtil.submitForm();

        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=10020");
        tester.setFormElement("lead", ADMIN_USERNAME);
        tester.selectOption("assigneeType", "Unassigned");
        tester.submit("Update");

        dog = pc.get("DOG");
        assertThat(dog.name, equalTo("Canine"));

        //now check that all information has been updated correctly.
        final Project newDog = pc.get("DOG");
        assertEquals("DOG", newDog.key);
        assertEquals("Canine", newDog.name);
        assertEquals("", newDog.description);
        assertEquals(null, newDog.url);
        assertEquals(ADMIN_USERNAME, newDog.lead.name);
        assertEquals(Project.AssigneeType.UNASSIGNED, newDog.assigneeType);

    }

    public void testEditProjectChangeNothing()
    {
        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=10020");
        tester.assertTextPresent("Edit Project Lead and Default Assignee for Project: Canine");

        //lets change all fields
        tester.setFormElement("lead", "murray");
        tester.selectOption("assigneeType", "Project Lead");
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
}
