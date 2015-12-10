package com.atlassian.jira.webtest.webdriver.tests.security;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.pages.AddPermissionPage;
import com.atlassian.jira.pageobjects.pages.DeletePermissionPage;
import com.atlassian.jira.pageobjects.pages.EditPermissionsPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@WebTest({ com.atlassian.jira.functest.framework.suite.Category.WEBDRIVER_TEST })
public class TestAnonymousAccess extends BaseJiraWebTest
{

    private static final String TEST_PROJECT_KEY = "TST";
    private static final String TEST_PROJECT_NAME = TEST_PROJECT_KEY + " project";
    private static final String TEST_PROJECT_NAME_ISSUE_SUMMARY = TEST_PROJECT_NAME + " issue summary";
    private static final int DEFAULT_PERMISSION_SCHEMA = 0;
    private static final String JIRA_PERMISSION_BROWSE_PROJECTS = "BROWSE_PROJECTS";
    private static final String JIRA_GROUP_ANYONE = "Anyone";
    private static final String JIRA_PERMISSION_BROWSE_PROJECTS_LABEL = "Browse Projects";

    @BeforeClass
    public static void setUp()
    {
        jira.backdoor().project()
                .addProject(TEST_PROJECT_NAME, TEST_PROJECT_KEY, jira.getAdminCredentials().getUsername());
        grantAnonymousJiraPermissions();
    }

    @AfterClass
    public static void tearDown()
    {
        revokeAnonymousJiraPermissions();
        jira.backdoor().project().deleteProject(TEST_PROJECT_KEY);
    }

    @Test
    @LoginAs(anonymous = true)
    public void testAnonymousAccessOk() throws Exception
    {
        final IssueCreateResponse newIssue = jira.backdoor().issues()
                .createIssue(TEST_PROJECT_KEY, TEST_PROJECT_NAME_ISSUE_SUMMARY);

        final ViewIssuePage viewIssuePage = pageBinder.navigateToAndBind(ViewIssuePage.class, newIssue.key);

        assertThat(viewIssuePage.getSummary(), is(TEST_PROJECT_NAME_ISSUE_SUMMARY));
    }

    private static void grantAnonymousJiraPermissions()
    {
        final AddPermissionPage addPermissionPage = jira.quickLoginAsAdmin(AddPermissionPage.class,
                DEFAULT_PERMISSION_SCHEMA, JIRA_PERMISSION_BROWSE_PROJECTS);
        addPermissionPage.setGroup(JIRA_GROUP_ANYONE);
        addPermissionPage.add();
    }

    private static void revokeAnonymousJiraPermissions()
    {
        final EditPermissionsPage editPermissionsPage = jira.quickLoginAsAdmin(EditPermissionsPage.class,
                DEFAULT_PERMISSION_SCHEMA);
        final DeletePermissionPage deletePermissionPage = editPermissionsPage.deleteForGroup(
                JIRA_PERMISSION_BROWSE_PROJECTS_LABEL, JIRA_GROUP_ANYONE);
        deletePermissionPage.delete();
    }

}
