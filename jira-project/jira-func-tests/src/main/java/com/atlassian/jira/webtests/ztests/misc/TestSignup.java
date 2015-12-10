/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.model.JiraMode;
import org.apache.commons.lang.StringUtils;

/**
 * Tests the Signup action
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestSignup extends FuncTestCase
{
    @Override
    public void setUpTest()
    {
        administration.restoreBlankInstance();
        backdoor.generalConfiguration().setContactAdminFormOn();
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    public void testSignupLinkNotPresentIfJiraNotPublic() throws Exception
    {
        boolean enabled;
        // enable PUBLIC mode
        enablePublicMode(enabled = true);
        navigation.logout();

        assertMessageOnLoginPage(enabled);
        assertMessageOnBrowseProjects(enabled);
        assertMessageOnViewProjects(enabled);

        // disable PUBLIC mode
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        enablePublicMode(enabled = false);
        navigation.logout();

        assertMessageOnLoginPage(enabled);
        assertMessageOnBrowseProjects(enabled);
        assertMessageOnViewProjects(enabled);

        // enable PUBLIC mode
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        enablePublicMode(enabled = true);
        navigation.logout();

        assertMessageOnLoginPage(enabled);
        assertMessageOnBrowseProjects(enabled);
        assertMessageOnViewProjects(enabled);
    }

    public void testSignupLinkNotPresentIfJiraNotPublicEditIssue() throws Exception
    {
        administration.project().addProject("Test", "TST", ADMIN_USERNAME);
        final String issueKey = navigation.issue().createIssue("Test", FunctTestConstants.ISSUE_TYPE_BUG,
                "Nam lobortis; nulla et sollicitudin");
        final String issueId = navigation.issue().getId(issueKey);

        boolean enabled;
        // enable PUBLIC mode
        enablePublicMode(enabled = true);
        navigation.logout();

        assertMessageOnEditIssue(enabled, issueId);

        // disable PUBLIC mode
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        enablePublicMode(enabled = false);
        navigation.logout();

        assertMessageOnEditIssue(enabled, issueId);

        // enable PUBLIC mode
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        enablePublicMode(enabled = true);
        navigation.logout();

        assertMessageOnEditIssue(enabled, issueId);
    }

    private void assertMessageOnLoginPage(boolean publicModeEnabled)
    {
        navigation.gotoPage("login.jsp");
        if (publicModeEnabled)
        {
            // Not a member? Signup for an account.
            text.assertTextSequence(signUpHintLocator(), new String[] { "Not a member?", "Sign up", "for an account." });
            text.assertTextNotPresent(signUpHintLocator(), "to request an account.");
            assertions.link().assertLinkByIdHasExactText("signup", "Sign up");
        }
        else
        {
            // Not a member? Signup for an account.
            text.assertTextNotPresent(signUpHintLocator(), "for an account");
            // Not a member? Contact an Administrator to request an account.
            text.assertTextSequence(signUpHintLocator(),
                    new String[] { "Not a member?", "To request an account, please contact your", "JIRA administrators" });
            assertions.link().assertLinkPresentWithExactTextById(signUpHintSectionId(), "JIRA administrators");
        }
    }

    private Locator signUpHintLocator()
    {
        return locator.id(signUpHintSectionId());
    }

    private String signUpHintSectionId()
    {
        return "sign-up-hint";
    }

    private void assertMessageOnEditIssue(boolean enabled, String issueId)
    {
        tester.gotoPage("/secure/EditIssue!default.jspa?id=" + issueId);
        if (enabled)
        {
            assertions.forms().assertFormErrMsg(
                    "You are not logged in, and do not have the permissions required to act on the selected issue as a guest.");
            // Please login or signup for an account.
            assertions.forms().assertFormErrMsg("log in or sign up for an account.");
            assertions.forms().assertFormErrMsgContainsLink("log in");
            assertions.forms().assertFormErrMsgContainsLink("sign up");
        }
        else
        {
            // Not a member? Signup for an account.
            assertions.getJiraFormAssertions().assertNoFormErrMsg("for an account");
        }
    }

    private void assertMessageOnBrowseProjects(boolean enabled)
    {
        tester.gotoPage("/secure/project/BrowseProjects.jspa");
        if (enabled)
        {
            // To browse projects first login or signup for an account.
            assertions.forms().assertFormErrMsg("To browse projects, first log in or sign up for an account.");
            assertions.forms().assertFormErrMsgContainsLink("sign up");
        }
        else
        {
            assertions.getJiraFormAssertions().assertNoFormErrMsg("for an account.");
        }
    }

    private void assertMessageOnViewProjects(boolean publicModeEnabled)
    {
        tester.gotoPage("/secure/project/ViewProjects.jspa");
        if (publicModeEnabled)
        {
            // If you log in or signup for an account, you might be able to see more here.
            assertions.forms().assertFormWarningMessage("If you log in or sign up for an account, you might be able to see more here.");
            assertions.forms().assertFormWarningMessage("sign up");
        }
        else
        {
            assertions.forms().assertNoFormWarningMessage("for an account, you might be able to see more here.");
        }
    }

    private void enablePublicMode(final boolean enablePublicMode)
    {
        backdoor.generalConfiguration().setJiraMode(JiraMode.forPublicModeEnabledValue(enablePublicMode));
    }

    public void testEnableCaptcha()
    {
        toggleCaptcha(true);
        navigation.logout();
        // we need to request the captcha servlet to link a captcha with our session
        tester.beginAt("/captcha");
        tester.beginAt("/");

        tester.gotoPage("login.jsp");
        tester.clickLink("signup");
        //make sure the captcha element is present
        tester.assertFormElementPresent("captcha");
        tester.setFormElement("username", "test");
        tester.setFormElement("fullname", "test");
        tester.setFormElement("email", "test@test.com");
        tester.setFormElement("captcha", "");
        tester.submit();
        tester.assertTextPresent("Please enter the word as shown below");
    }

    public void testDisableCaptcha()
    {
        toggleCaptcha(false);
        navigation.logout();

        tester.gotoPage("login.jsp");
        tester.clickLink("signup");
        //make sure the captcha element is present
        tester.assertFormElementNotPresent("captcha");
        tester.setFormElement("username", "test");
        tester.setFormElement("fullname", "test");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.setFormElement("email", "test@test.com");
        tester.submit();
        tester.assertTextNotPresent("You must enter the text exactly as it appears in the picture.");
        tester.assertTextPresent("You have successfully signed up.");
    }

    public void testStayInTouchLink()
    {
        navigation.logout();
        tester.gotoPage("login.jsp");
        tester.clickLink("signup");
        tester.setFormElement("username", "test");
        tester.setFormElement("fullname", "test");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.setFormElement("email", "test@test.com");
        tester.submit();
        tester.assertTextPresent("Stay connected with Atlassian. Subscribe to");
        assertions.link().assertLinkWithExactTextAndUrlPresent("blogs, newsletters, forums and more",
                "http://www.atlassian.com/about/connected.jsp?s_kwcid=jira-stayintouch");
    }

    private void toggleCaptcha(boolean enableCaptcha)
    {
        backdoor.generalConfiguration().toggleCaptchaOnSignup(enableCaptcha);
    }

    private void prepareToSignUp()
    {
        navigation.logout();
        gotoSignupPage();
        assertions.assertNodeHasText(locator.css("h1"), "Sign up");
        tester.setWorkingForm("signup");
        tester.assertButtonPresent("signup-submit");
    }

    private void gotoSignupPage()
    {
        tester.gotoPage("http://localhost:8090/jira/secure/Signup!default.jspa");
    }

    // JRADEV-10699
    public void testMustBeLoggedOutToSignUp()
    {
        gotoSignupPage();
        assertions.assertNodeHasText(locator.css("h1"), "You're already logged in");
        tester.assertButtonNotPresent("signup-submit");
        text.assertTextSequence(locator.page(),
                new String[] { "You cannot sign up while logged in. Please", "log out", "first." });
        tester.assertLinkPresent("log_out");
    }

    public void testEmptyData()
    {
        prepareToSignUp();

        tester.setFormElement("username", "");
        tester.setFormElement("fullname", "");
        tester.setFormElement("email", "");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("You must specify a username.");
        tester.assertTextPresent("You must specify a password and a confirmation password.");
        tester.assertTextPresent("You must specify a full name.");
        tester.assertTextPresent("You must specify an email address.");
    }

    public void testNoPasswordSet()
    {
        prepareToSignUp();

        tester.setFormElement("username", "user");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user@email.com");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("You must specify a password and a confirmation password.");
        tester.assertTextNotPresent("You must specify a username.");
        tester.assertTextNotPresent("You must specify a full name.");
        tester.assertTextNotPresent("You must specify an email address.");

    }

    public void testSignUpDuplicateUser()
    {
        checkSuccessUserCreate();

        prepareToSignUp();

        tester.setFormElement("username", "user");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user@email.com");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("A user with that username already exists.");
    }

    public void testCreateUserSuccess()
    {
        checkSuccessUserCreate();
    }

    private void checkSuccessUserCreate()
    {
        prepareToSignUp();

        tester.setFormElement("username", "user");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "username@email.com");
        tester.submit();

        assertions.assertNodeHasText(locator.css("h1"), "Congratulations!");
        tester.assertTextPresent("You have successfully signed up. If you forget your password, you can have it emailed to you.");
    }

    public void testCreateUserInvalidEmail()
    {
        prepareToSignUp();

        tester.setFormElement("username", "User");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");

        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user.email.com");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("You must specify a valid email address.");
    }

    public void testCreateUserInvalidUsername()
    {
        prepareToSignUp();

        tester.setFormElement("username", "bad<username>");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");

        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user.email.com");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("The username must not contain &#39;&lt;&#39;, &#39;&gt;&#39; or &#39;&amp;&#39;.");
    }

    public void testCreateUserWIthLeadingOrTrailingSpaces()
    {
        prepareToSignUp();

        final String untrimmedUserName = "   andres  ";
        tester.setFormElement("username", untrimmedUserName);
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");

        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user@example.com");
        tester.submit();

        tester.assertTextPresent("You have successfully signed up.");

        // Try to login with the untrimmed username
        navigation.loginAttempt(untrimmedUserName, "password");
        tester.assertTextPresent("Sorry, your username and password are incorrect - please try again.");

        // Now use the trimmed user name
        navigation.loginAttempt(untrimmedUserName.trim(), "password");
        tester.assertTextNotPresent("Sorry, your username and password are incorrect - please try again.");
        assertions.getURLAssertions().assertCurrentURLEndsWith("Dashboard.jspa");
    }

    public void testCreateUserFieldsExceed255()
    {
        prepareToSignUp();

        final String username = StringUtils.repeat("abcdefgh", 32);
        final String fullname = StringUtils.repeat("ABCDEFGH", 32);
        final String email = StringUtils.repeat("x", 246) + "@email.com";

        tester.setFormElement("username", username);
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");

        tester.setFormElement("fullname", fullname);
        tester.setFormElement("email", email);
        tester.submit();

        tester.assertTextPresent("The username must not exceed 255 characters in length.");
        tester.assertTextPresent("The full name must not exceed 255 characters in length.");
        tester.assertTextPresent("The email address must not exceed 255 characters in length.");

        tester.setFormElement("username", username.substring(0, 255));
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");

        tester.setFormElement("fullname", fullname.substring(0, 255));
        tester.setFormElement("email", email.substring(0, 255));
        tester.submit();

        tester.assertTextNotPresent("The username must not exceed 255 characters in length.");
        tester.assertTextNotPresent("The full name must not exceed 255 characters in length.");
        tester.assertTextNotPresent("The email address must not exceed 255 characters in length.");

        navigation.login(username.substring(0, 255), "password");
        tester.assertTextPresent(fullname.substring(0, 255));
    }

    public void testCreateUserPassword()
    {
        prepareToSignUp();

        tester.setFormElement("username", "user");
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "");

        tester.setFormElement("fullname", "User Tested");
        tester.setFormElement("email", "user@email.com");
        tester.submit();

        tester.assertTextPresent("Sign up");
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("confirm", "confirm");
        tester.submit();
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("password", "abc");
        tester.setFormElement("confirm", "def");
        tester.submit();
        tester.assertTextPresent("Your password and confirmation password do not match.");

        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.submit();

        assertions.assertNodeHasText(locator.css("h1"), "Congratulations!");
        tester.assertTextPresent("You have successfully signed up. If you forget your password, you can have it emailed to you.");
    }
}
