package com.atlassian.jira.webtest.webdriver.tests.permissions;

import com.atlassian.jira.pageobjects.project.ViewProjectsPage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.CreateUser;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.project.AddProjectPage;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.Matchers.containsString;

@WebTest(com.atlassian.jira.functest.framework.suite.Category.WEBDRIVER_TEST)
public class TestUserPermissions extends BaseJiraWebTest
{
    // we need user picker so let's have another user in JIRA
    private static final String TEST_ADMIN = "test-admin";

    private static final String TEST_PROJECT = "TSTPR";

    @BeforeClass
    public static void setUp()
    {
        jira.backdoor().project().addProject(TEST_PROJECT, TEST_PROJECT, jira.getAdminCredentials().getUsername());
    }

    @AfterClass
    public static void tearDown()
    {
        jira.backdoor().project().deleteProject(TEST_PROJECT);
    }

    @Test
    @CreateUser(username = TEST_ADMIN, password = TEST_ADMIN, admin = true)
    @LoginAs(admin = true, targetPage = AddProjectPage.class)
    public void testAdminHasUserPickerPermission()
    {
        final AddProjectPage addProjectPage = pageBinder.bind(AddProjectPage.class);

        assertTrue(addProjectPage.isLeadPickerPresent());
        assertThat(addProjectPage.getLeadPickerClassAttr(), containsString("single-user-picker"));

        jira.logout();
    }

    @Test
    @CreateUser(username = TEST_ADMIN, password = TEST_ADMIN, admin = true)
    @LoginAs(admin = true, targetPage = ViewProjectsPage.class)
    public void testAdministratorCollaboratorCanEditProject() throws Exception
    {
        final ViewProjectsPage viewProjectsPage = pageBinder.bind(ViewProjectsPage.class);

        assertTrue(viewProjectsPage.findProject(TEST_PROJECT).edit().isProjectKeyVisible().byDefaultTimeout());

        jira.logout();
    }
}
