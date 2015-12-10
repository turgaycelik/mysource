package com.atlassian.jira.webtest.webdriver.tests.admin.users;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.CreateUser;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.pages.admin.user.EditUserDetailsPage;
import com.atlassian.jira.pageobjects.pages.admin.user.ViewUserPage;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@WebTest(com.atlassian.jira.functest.framework.suite.Category.WEBDRIVER_TEST)
public class TestLicensedUserManagement extends BaseJiraWebTest
{
    private static final String COLLBORATOR = "test-collaborator";

    @Test
    @CreateUser(username = COLLBORATOR, password = COLLBORATOR)
    @LoginAs(admin = true, targetPage = EditCollaboratorUserDetailsPage.class)
    public void testEditUserDetailsPage()
    {
        final EditUserDetailsPage page = pageBinder.bind(EditCollaboratorUserDetailsPage.class);

        assertThat(page.getFormCaption(), is("Edit Profile: Test User " + COLLBORATOR));
        assertThat(page.getFullNameLabel(), startsWith("Full Name"));
        assertThat(page.getCurrentUserFullName(), is("Test User " + COLLBORATOR));

        assertThat(page.getEmailLabel(), startsWith("Email"));
        assertThat(page.getCurrentUserEmail(), is(COLLBORATOR + "@example.com"));

        page.fillUserFullName("Test User Another Collaborator");
        page.fillUserEmail("Test-User-Another-Collaborator@example.com");
        final ViewUserPage viewUserPage = page.submit();

        assertThat(viewUserPage.getFullname(), is("Test User Another Collaborator"));
        assertThat(viewUserPage.getEmail(), is("Test-User-Another-Collaborator@example.com"));
    }

    public static class EditCollaboratorUserDetailsPage extends EditUserDetailsPage
    {
        public EditCollaboratorUserDetailsPage()
        {
            super(COLLBORATOR);
        }
    }
}
