package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

import java.net.URL;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestLoginActions extends FuncTestCase
{
    private static final String NOT_RECORDED = "Not recorded";
    private static final String AUTHENTICATION_ERR_MSG = "Sorry, your username and password are incorrect - please try again.";
    private static final String CAPTCHA_ERR_MSG = "Sorry, your userid is required to answer a CAPTCHA question correctly.";

    public void testLogin()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertRedirectPath(getEnvironmentData().getContext() + "/secure/Dashboard.jspa");
    }

    public void testLogInAgainGoesToLoginPage()
    {
        navigation.logout();
        tester.assertTextPresent("Log in again");
        tester.clickLinkWithText("Log in again");
        String url = tester.getDialog().getResponse().getURL().getPath();
        assertTrue("The URL '" + url + "' did not contain 'login.jsp'", url.contains("login.jsp"));
        assertFalse("The URL '" + url + "' contained 'Dashboard'", url.contains("Dashboard"));
    }

    public void testLoginPageWithNoPassword()
    {
        navigation.loginAttempt(null, null);
        tester.assertTextPresent(AUTHENTICATION_ERR_MSG);
    }

    public void testBadPasswordLogin()
    {
        navigation.loginAttempt(ADMIN_USERNAME, "bad password");
        tester.assertTextPresent(AUTHENTICATION_ERR_MSG);
    }

    public void testGoodPasswordNoPermission()
    {
        administration.restoreBlankInstance();

        // remove Fred from jira-users
        gotoUserBrowser();

        tester.clickLink(FRED_USERNAME);
        tester.clickLink("editgroups_link");
        tester.selectOption("groupsToLeave", "jira-users");
        tester.submit("leave");
        tester.selectOption("groupsToJoin", "jira-developers");
        tester.submit("join");
        navigation.logout();

        // test Fred can not log in
        navigation.loginAttempt(FRED_USERNAME, FRED_PASSWORD);
        tester.assertTextPresent("You do not have a permission to log in.");
    }

    public void testLastLoginInfoIsShown()
    {
        administration.restoreData("TestLastLogin.xml");

        gotoUserBrowser();
        TableLocator tableLocator = new TableLocator(tester, "user_browser_table");
        text.assertTextSequence(tableLocator, ADMIN_USERNAME, "Today", FRED_USERNAME, NOT_RECORDED, "nouserpermission", NOT_RECORDED);

        // now view the admin user.  
        assertViewUser(ADMIN_USERNAME, true, true, 1L);
        assertViewUser(FRED_USERNAME, false, false, null);
        assertViewUser("nouserpermission", false, false, null);

        // login as fred and watch 1 value go up
        loginAsAndThenAs(FRED_USERNAME, ADMIN_USERNAME);

        gotoUserBrowser();
        tableLocator = new TableLocator(tester, "user_browser_table");
        text.assertTextSequence(tableLocator, ADMIN_USERNAME, "Today", FRED_USERNAME, "Today", "nouserpermission", NOT_RECORDED);


        assertViewUser(FRED_USERNAME, true, false, 1L);
        assertViewUser("nouserpermission", false, false, null);

        // login as fred again and watch both values go up
        loginAsAndThenAs(FRED_USERNAME, ADMIN_USERNAME);

        gotoUserBrowser();
        tableLocator = new TableLocator(tester, "user_browser_table");
        text.assertTextSequence(tableLocator, ADMIN_USERNAME, "Today", FRED_USERNAME, "Today", "nouserpermission", NOT_RECORDED);

        assertViewUser(FRED_USERNAME, true, true, 2L);
        assertViewUser("nouserpermission", false, false, null);


        // login as nouserpermission and nothing should change since he is not allowed to login
        loginAsAndThenAs("nouserpermission", ADMIN_USERNAME);

        gotoUserBrowser();
        tableLocator = new TableLocator(tester, "user_browser_table");
        text.assertTextSequence(tableLocator, ADMIN_USERNAME, "Today", FRED_USERNAME, "Today", "nouserpermission", NOT_RECORDED);

        assertViewUser(FRED_USERNAME, true, true, 2L);
        assertViewUser("nouserpermission", false, false, null);
    }

    public void testCAPTCHALockedOut()
    {
        administration.restoreData("TestLastLogin.xml");

        setMaximumLoginAttempts("3");

        loginAsAndThenAs(FRED_USERNAME, "FAIL", ADMIN_USERNAME, false, AUTHENTICATION_ERR_MSG); // one failed attempt
        assertViewUserFailedAttempts(FRED_USERNAME, true, 1L, 1L);

        loginAsAndThenAs(FRED_USERNAME, "FAIL", ADMIN_USERNAME, false, AUTHENTICATION_ERR_MSG); // second failed attempt
        assertViewUserFailedAttempts(FRED_USERNAME, true, 2L, 2L);

        // we can now use the right password but its too late the CAPTCHA is shown and
        //
        // Unfortunately the nature of CAPTCHA is that it cant be tested via computers
        // so we cant test that we can pass the check!
        //
        loginAsAndThenAs(FRED_USERNAME, "FAIL", ADMIN_USERNAME, true, AUTHENTICATION_ERR_MSG); // third failed attempt
        assertViewUserFailedAttempts(FRED_USERNAME, true, 3L, 3L);

        resetUserLoginCount(FRED_USERNAME);

        assertViewUserFailedAttempts(FRED_USERNAME, true, 0L, 3L);

        loginAsAndThenAs(FRED_USERNAME, "FAIL", ADMIN_USERNAME, false, AUTHENTICATION_ERR_MSG); // one more failed attempt
        assertViewUserFailedAttempts(FRED_USERNAME, true, 1L, 4L);

        // and then succeed
        loginAsAndThenAs(FRED_USERNAME, FRED_USERNAME, ADMIN_USERNAME, false, null); // one failed attempt
        assertViewUserFailedAttempts(FRED_USERNAME, true, 0L, 4L);

        // ok but we can reset his password and the fail count should be reset to zero at this time
        navigation.login(ADMIN_USERNAME);
        navigation.gotoAdminSection("user_browser");
        tester.clickLinkWithText(FRED_USERNAME);
        tester.clickLinkWithText("Set Password");
        tester.setFormElement("password", FRED_USERNAME);
        tester.setFormElement("confirm", FRED_USERNAME);
        tester.submit("Update");

        // fred now has his login state reset because his password has been reset
        assertViewUser(FRED_USERNAME, true, false, 1L);
    }

    private void resetUserLoginCount(final String userName)
    {
        gotoUserBrowser();
        tester.clickLinkWithText("Reset Failed Login Count");
    }

    private void setMaximumLoginAttempts(final String howMany)
    {
        navigation.gotoAdminSection("general_configuration");
        tester.clickLink("edit-app-properties");
        tester.setFormElement("title", "jWebTest JIRA installation");
        tester.setFormElement("maximumAuthenticationAttemptsAllowed", howMany);
        tester.checkCheckbox("captcha", "false");
        tester.submit("Update");
    }

    private void gotoUserBrowser()
    {
        navigation.gotoAdminSection("user_browser");
    }

    private void loginAsAndThenAs(final String userName1, final String userName2)
    {
        loginAsAndThenAs(userName1, userName1, userName2, false, null);
    }

    private void loginAsAndThenAs(final String userName1, final String userName1password, final String userName2, final boolean requireCaptcha, final String errMsg)
    {
        navigation.logout();
        navigation.loginAttempt(userName1, userName1password);
        assertCAPTCHAShown(requireCaptcha);
        if (StringUtils.isNotBlank(errMsg))
        {
            tester.assertTextPresent(errMsg);
        }
        
        navigation.logout();
        navigation.loginAttempt(userName2, userName2);
    }

    private void assertViewUser(final String userName, final boolean lastLoginSet, final boolean prevLoginSet, final Long loginCount)
    {
        tester.gotoPage("secure/admin/user/ViewUser.jspa?name=" + userName);
        assertNotRecorded(lastLoginSet, new XPathLocator(tester, "//dd[@id='lastLogin']"));
        assertNotRecorded(prevLoginSet, new XPathLocator(tester, "//dd[@id='previousLogin']"));

        assertLoginNumber(loginCount, "loginCount");
    }

    private void assertViewUserFailedAttempts(final String userName, final boolean lastFailedLoginSet, final Long currentFailedCount, final Long totalFailedLoginCount)
    {
        tester.gotoPage("secure/admin/user/ViewUser.jspa?name=" + userName);
        assertNotRecorded(lastFailedLoginSet, new XPathLocator(tester, "//dd[@id='lastFailedLogin']"));

        assertLoginNumber(currentFailedCount, "currentFailedLoginCount");
        assertLoginNumber(totalFailedLoginCount, "totalFailedLoginCount");
    }

    private void assertLoginNumber(final Long currentFailedCount, final String fieldName)
    {
        final XPathLocator pathLocator = new XPathLocator(tester, "//dd[@id='" + fieldName + "']");
        if (currentFailedCount != null)
        {
            text.assertTextPresent(pathLocator, String.valueOf(currentFailedCount));
        }
        else
        {
            assertEquals("The " + fieldName + " should be Not Recorded", NOT_RECORDED, pathLocator.getText());
        }
    }

    private void assertCAPTCHAShown(final boolean requireCaptcha)
    {
        final boolean hasCaptcha = tester.getDialog().hasFormParameterNamed("os_captcha");
        assertEquals("CAPTCHA should " + (requireCaptcha ? "BE" : "NOT BE") + " present", requireCaptcha, hasCaptcha);
    }

    private void assertNotRecorded(final boolean lastLoginSet, final XPathLocator loc)
    {
        if (!lastLoginSet)
        {
            text.assertTextPresent(loc, NOT_RECORDED);
        }
        else
        {
            text.assertTextNotPresent(loc, NOT_RECORDED);
        }
    }

    private void assertRedirectPath(String s)
    {
        URL url = tester.getDialog().getResponse().getURL();
        assertEquals(s, url.getPath());
    }

}
