package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.WebTesterFactory;
import net.sourceforge.jwebunit.WebTester;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserRememberMeCookies extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
        for (int i = 0; i < 5; i++)
        {
            loginAs(ADMIN_USERNAME);
        }
    }

    public void testHasRememberMeCookies()
    {
        gotoRememberMeUserBrowserAdminSection();

        text.assertTextSequence(xpath("//form[@id='rememberme_cookies_form']//h2"), "Remember My Login", ADMIN_FULLNAME);

        assertEquals("There should be 5 rows of cookies", 5, xpath("//table[@id='rememberme_cookies_table']//tbody/tr").getNodes().length);

        tester.submit("Submit");

        assertHasNoRememberMeCookies();
    }

    public void testUserProfileRememberMeClear()
    {
        navigation.gotoPage("secure/ViewProfile.jspa");
        tester.clickLink("view_clear_rememberme");

        text.assertTextPresent("Remember My Login");
        tester.submit("Clear");
        text.assertTextPresent("Your remember my login tokens have successfully been cleared");

        gotoRememberMeUserBrowserAdminSection();
        assertHasNoRememberMeCookies();
    }


    public void testUserProfileRememberMeClear_NotLoggedIn()
    {
        navigation.logout();
        navigation.gotoPage("secure/ClearRememberMeCookies!default.jspa");
        text.assertTextPresent("You must be logged in to clear your remember my login tokens.");
    }

    public void testAllUsersRememberMeClear()
    {
        // direct page goto
        navigation.gotoPage("secure/admin/user/AllUsersRememberMeCookies!default.jspa");
        assertWeAreOnTheClearAllPage(5);

        // via the plugin point
        navigation.gotoAdminSection("rememberme");
        assertWeAreOnTheClearAllPage(5);

        tester.submit("Submit");
        text.assertTextPresent("All the tokens have been cleared.");
        assertWeAreOnTheClearAllPage(0);

        gotoRememberMeUserBrowserAdminSection();
        assertHasNoRememberMeCookies();
    }

    private void assertWeAreOnTheClearAllPage(final Integer numberOfTokens)
    {
        text.assertTextPresent("Remember My Login for All Users");
        text.assertTextPresent("to clear all of these tokens from this JIRA site.");
        text.assertTextPresent(xpath("//div[@class='form-body']/p/strong"), numberOfTokens.toString());
    }


    private void assertHasNoRememberMeCookies()
    {
        text.assertTextPresent(xpath("//form[@id='rememberme_cookies_form']"), "No login tokens have been set for: " + ADMIN_FULLNAME);
    }

    private void gotoRememberMeUserBrowserAdminSection()
    {
        navigation.gotoAdminSection("user_browser");
        tester.clickLink(ADMIN_USERNAME);
        tester.clickLink("rememberme_link");
    }

    private void loginAs(final String userName)
    {
        final WebTester tester = WebTesterFactory.createNewWebTester(environmentData);
        tester.beginAt("/login.jsp");
        tester.setFormElement("os_username", userName);
        tester.setFormElement("os_password", userName);
        tester.setFormElement("os_cookie", "true");
        tester.setWorkingForm("login-form");
        tester.submit();
        log.log("Started session for " + userName);
    }
}
