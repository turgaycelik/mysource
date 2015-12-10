package com.atlassian.jira.webtest.webdriver.tests.admin.users;

import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.pages.admin.user.AddUserPage;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.is;

@WebTest(com.atlassian.jira.functest.framework.suite.Category.WEBDRIVER_TEST)
public class TestAddUserPage extends BaseJiraWebTest
{
    @Test
    @LoginAs(admin = true, targetPage = AddUserPage.class)
    public void testErrorMessagesPresent() throws Exception
    {
        AddUserPage page = pageBinder.bind(AddUserPage.class);
        final String admin = jira.getAdminCredentials().getUsername();

        page = page.addUser(admin, "password", "confirmFail", "", "", false);
        page = page.createUserExpectingError();

        final Map<String, String> pageErrors = page.getPageErrors();

        assertThat(txtByKey(pageErrors, "user-create-username-error"), is("A user with that username already exists."));

        assertThat(txtByKey(pageErrors, "user-create-confirm-error"),
                is("Your password and confirmation password do not match."));

        assertThat(txtByKey(pageErrors, "user-create-fullname-error"), is("You must specify a full name."));
        assertThat(txtByKey(pageErrors, "user-create-email-error"), is("You must specify an email address."));
    }

    private String txtByKey(final Map<String, String> pageErrors, final String id)
    {
        return pageErrors.get(id);
    }
}
