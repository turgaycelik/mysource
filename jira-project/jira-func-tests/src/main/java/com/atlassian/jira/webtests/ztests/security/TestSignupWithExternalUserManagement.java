package com.atlassian.jira.webtests.ztests.security;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.junit.Ignore;

/**
 * JRA-15966 When External User Management is enabled we should not let users auto signup with "public server mode".
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY })
public class TestSignupWithExternalUserManagement extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestSignupWithExternalUserManagement.xml");
    }

    public void testCanSignupWithExternalUserManagementOff() throws Exception
    {
        // Turn off External User Management
        navigation.gotoAdminSection("general_configuration");
        tester.clickLink("edit-app-properties");
        tester.checkCheckbox("externalUM", "false");
        tester.submit("Update");

        // Now Public Signup should work
        tester.clickLinkWithText("Log Out");
        tester.assertTextPresent("You are now logged out");
        tester.gotoPage("login.jsp");
        // look for the Public Signup text
        tester.assertTextPresent("Not a member?");
        tester.assertTextPresent("Signup");
        tester.assertTextNotPresent("to request an account.");
        // Signup new user
        tester.clickLink("signup");
        tester.setFormElement("username", "dude");
        tester.setFormElement("password", "dude");
        tester.setFormElement("confirm", "dude");
        tester.setFormElement("fullname", "Me");
        tester.setFormElement("email", "dude@example.com");
        tester.submit();
        tester.assertTextPresent("You have successfully signed up");
        // Now log in as new user to see it works
        tester.clickLinkWithText("Click here to log in");
        // Not logged in , so we can't Create Issue yet
        tester.assertLinkNotPresent("create_link");
        tester.gotoPage("login.jsp");
        tester.setFormElement("os_username", "dude");
        tester.setFormElement("os_password", "dude");
        tester.setWorkingForm("login-form");
        tester.submit();
        tester.assertLinkPresent("create_link");
    }

    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only")
    public void testCantSignupWithExternalUserManagementOn() throws Exception
    {
        String url;

        // Now Public Signup should NOT work
        tester.clickLinkWithText("Log Out");
        tester.assertTextPresent("You are now logged out");

        tester.gotoPage("login.jsp");
        // We should get "To request an account, please contact ....." (not signup)
        tester.assertTextPresent("Not a member?");
        tester.assertTextPresent("To request an account,");
        tester.assertTextNotPresent("Signup");

        // Make a failed login attempt, and we should now go to the "real" login page.
        tester.setFormElement("os_username", "dude");
        tester.setWorkingForm("login-form");
        tester.submit();
        tester.assertTextPresent("Welcome to Rasta JIRA");
        url = tester.getDialog().getResponse().getURL().getPath();
        assertTrue(url.endsWith("login.jsp"));
        // We should get "To request an account, please contact ....." (not signup)
        tester.assertTextPresent("To request an account,");
        tester.assertTextNotPresent("Signup");
    }

    @Ignore ("JRADEV-8029 Can no longer do this in a simple func test. Need to make User Directories read-only")
    public void testSignupPageBarfsWithExternalUserManagement() throws Exception
    {
        navigation.logout();
        // Go directly to the Signup page
        tester.gotoPage("secure/Signup!default.jspa");
        tester.assertTextPresent("It seems that you have tried to perform an operation that is not allowed in the current JIRA mode.");
        tester.assertTextNotPresent("To sign up for JIRA simply enter your details below.");

        // Turn off External User Management
        navigation.login(ADMIN_USERNAME);
        navigation.gotoAdminSection("general_configuration");
        tester.clickLink("edit-app-properties");
        tester.checkCheckbox("externalUM", "false");
        tester.submit("Update");

        // Try the Signup page again
        navigation.logout();
        // Go directly to the Signup page: this time it should work
        navigation.gotoPage("secure/Signup!default.jspa");
        tester.assertTextNotPresent("It seems that you have tried to perform an operation that is not allowed in the current JIRA mode.");
        tester.assertTextPresent("To sign up for JIRA simply enter your details below.");
    }

    public void testGeneralConfigurationChecksValidCombination() throws Exception
    {
        navigation.gotoAdminSection("general_configuration");
        tester.clickLink("edit-app-properties");
        // Private Mode and External UserManagement enabled.
        tester.selectOption("mode", "Private");
        tester.checkCheckbox("externalUM", "true");
        tester.submit("Update");
        // Should work
        tester.assertTextNotPresent("You cannot select Public Mode if External User Management is enabled.");

        tester.clickLink("edit-app-properties");
        // Public Mode and External UserManagement enabled.
        tester.selectOption("mode", "Public");
        tester.checkCheckbox("externalUM", "true");
        tester.submit("Update");
        // Should fail
        tester.assertTextPresent("You cannot select Public Mode if External User Management is enabled.");
    }
}
