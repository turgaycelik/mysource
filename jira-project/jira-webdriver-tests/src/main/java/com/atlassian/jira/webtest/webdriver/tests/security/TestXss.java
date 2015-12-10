package com.atlassian.jira.webtest.webdriver.tests.security;

import java.util.NoSuchElementException;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.CreateUser;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.pages.EditProfilePage;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.jira.pageobjects.pages.admin.EditApplicationPropertiesPage;
import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage;
import com.atlassian.jira.pageobjects.pages.admin.roles.UserRoleActorActionPage;
import com.atlassian.jira.pageobjects.pages.admin.user.AddUserPage;
import com.atlassian.jira.pageobjects.pages.admin.user.UserBrowserPage;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import static org.junit.Assert.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@WebTest({ com.atlassian.jira.functest.framework.suite.Category.WEBDRIVER_TEST })
public class TestXss extends BaseJiraWebTest
{
    private static final String DEVELOPER = "developer";

    @Test
    @CreateUser(username = DEVELOPER, password = DEVELOPER, developer = true)
    @LoginAs(user = DEVELOPER, password = DEVELOPER)
    public void testRoleActorActionsXSS() throws Exception
    {
        pageBinder.navigateToAndBind(UserRoleActorActionPage.class,
                "10002f843c%3Cscript%3Ealert%281%29%3C/script%3Ee156c7382b7");
        assertSourceNoXSS();
        pageBinder.navigateToAndBind(UserRoleActorActionPage.class,
                "10002&projectId=10010f843c%3Cscript%3Ealert%281%29%3C/script%3Ee156c7382b7");
        assertSourceNoXSS();
        pageBinder.navigateToAndBind(UserRoleActorActionPage.class,
                "100021442d<script>alert(1)</script>42df75ab185&projectId=10020");
        assertSourceNoXSS();
        pageBinder.navigateToAndBind(UserRoleActorActionPage.class,
                "10002f843c%3Cscript%3Ealert%281%29%3C/script%3Ee156c7382b7");
        assertSourceNoXSS();
        pageBinder.navigateToAndBind(UserRoleActorActionPage.class,
                "10002&projectId=10010b4927<script>alert(1)</script>fa5f1a0dfb");
        assertSourceNoXSS();
    }

    /**
     * Tests against XSS in EditProfile!default.jspa (JST-3617)
     */
    @Test
    @CreateUser(username = DEVELOPER, password = DEVELOPER, developer = true)
    @LoginAs(user = DEVELOPER, password = DEVELOPER, targetPage = ViewProfilePage.class)
    public void testEditProfileXSS()
    {
        final ViewProfilePage profilePage = pageBinder.bind(ViewProfilePage.class);
        final EditProfilePage editPage = profilePage.edit();
        editPage.setFullname("\"><script>alert(\"JST-3617\")</script>").setPassword(DEVELOPER);
        editPage.submit();
        assertSourceNoXSS();
    }

    /**
     * Tests against XSS in EditApplicationProperties.jspa (JST-3790)
     */
    @Test
    @CreateUser(username = DEVELOPER, password = DEVELOPER, developer = true)
    @LoginAs(sysadmin = true, targetPage = EditApplicationPropertiesPage.class)
    public void testEditApplicationPropertiesXSS()
    {
        EditApplicationPropertiesPage editPage = pageBinder.bind(EditApplicationPropertiesPage.class);
        final String originalEmailFrom = editPage.getEmailFromHeaderFormat();
        final String originalAppTitle = editPage.getApplicationTitle();

        try
        {
            editPage.setEmailFromHeaderFormat("\"><script>alert(3790)</script>");
            editPage.submit();
            pageBinder.bind(ViewGeneralConfigurationPage.class);
            assertSourceNoXSS();

            editPage = pageBinder.navigateToAndBind(EditApplicationPropertiesPage.class);
            editPage.setTitle("votest.jira.com'\"><script>alert(3790)</script>d5c2734e173b21b9c");
            editPage.submit();
            pageBinder.bind(ViewGeneralConfigurationPage.class);
            assertSourceNoXSS();

        }
        finally
        {
            editPage = pageBinder.navigateToAndBind(EditApplicationPropertiesPage.class);
            editPage.setTitle(originalAppTitle).setEmailFromHeaderFormat(originalEmailFrom);
            editPage.submit();
            pageBinder.bind(ViewGeneralConfigurationPage.class);
        }
    }

    /**
     * Tests against XSS in AddUser.jspa (JST-3797)
     */
    @Test
    @CreateUser(username = DEVELOPER, password = DEVELOPER, developer = true)
    @LoginAs(sysadmin = true, targetPage = AddUserPage.class)
    public void testAddUserXSS()
    {
        String testUserUsername = null;
        try
        {
            testUserUsername = "a" + System.currentTimeMillis();
            final AddUserPage addUserPage = pageBinder.bind(AddUserPage.class);
            final UserBrowserPage userBrowser = addUserPage.addUser(testUserUsername, "'\"><script>alert(1)</script>",
                    "'\"><script>alert(1)</script>", testUserUsername + "@example.com", false).createUser(
                    UserBrowserPage.class);
            try
            {
                userBrowser.findRow(testUserUsername);
            }
            catch (final NoSuchElementException e)
            {
                throw new AssertionError("User " + testUserUsername + " not found on browser page.");
            }
            assertSourceNoXSS();
        }
        finally
        {
            jira.backdoor().getTestkit().rawRestApiControl().rootResource().path("user")
                    .queryParam("username", testUserUsername).delete();
        }
    }

    private void assertSourceNoXSS()
    {
        assertThat(jira.getTester().getDriver().getDriver().getPageSource(), not(containsString("<script>alert")));
    }

}
