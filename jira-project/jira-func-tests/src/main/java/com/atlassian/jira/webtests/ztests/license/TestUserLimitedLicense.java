package com.atlassian.jira.webtests.ztests.license;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.jira.webtests.Permissions;

/**
 * Test that the user limited licenses such as Sprout Starter licences  and Commercial Limited work.
 *
 * Tests relating to the Admin Gadget have been moved to selenium -- see {@link  * com.atlassian.jira.webtest.selenium.admin.licenses.TestUserLimitedLicense}.
 */
@WebTest ({ Category.FUNC_TEST, Category.LICENSING })
public class TestUserLimitedLicense extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testCreateIssueOverLicenseLimit_Starter()
    {
        _testCreateIssueOverLicenseLimit(LicenseKeys.V2_STARTER);
    }

    public void testCreateIssueOverLicenseLimit_CommercialLimited()
    {
        _testCreateIssueOverLicenseLimit(LicenseKeys.V2_COMMERCIAL_LIMITED);
    }

    private void _testCreateIssueOverLicenseLimit(LicenseKeys.License license)
    {
        administration.switchToLicense(license);

        //lets try to create an issue and make sure everything works fine!
        navigation.issue().goToCreateIssueForm(null,null);
        tester.setFormElement("summary", "A little bug");
        tester.submit("Create");
        //check that we made it to the view issue page!
        tester.assertTextPresent("A little bug");
        tester.assertTextPresent("Details");

        //now lets add a user.  We should be at the user limit of the license and should still be able to create
        //issues!
        backdoor.usersAndGroups().addUser(BOB_USERNAME);
        navigation.issue().goToCreateIssueForm(null,null);
        tester.setFormElement("summary", "This is a big one!");
        tester.submit("Create");
        //check that we made it to the view issue page!
        tester.assertTextPresent("This is a big one!");
        tester.assertTextPresent("Details");

        //lets import data with one too many users!
        administration.restoreData("TestStarterLicenseTooManyUsers.xml");
        administration.switchToLicense(license);

        //make sure we're no longer in the admin section (where the create issue link is no longer displayed).
        if (tester.getDialog().isLinkPresent("leave_admin"))
        {
            tester.clickLink("leave_admin");
        }
        tester.clickLink("create_link");
        tester.assertTextPresent("You will not be able to create new issues because the user limit for your JIRA instance has been exceeded, please contact your JIRA administrators.");

        //try to hack the URL.
        String atlToken = page.getXsrfToken();
        tester.gotoPage("/secure/CreateIssueDetails.jspa?atl_token="+atlToken);
        tester.assertTextPresent("You have not selected a valid project to create an issue in.");
        tester.assertElementNotPresent("details-module");

        //remove one of the users from the jira-users group to reduce the number of active users.
        navigation.gotoAdmin();
        // Click Link 'User Browser' (id='user_browser').
        tester.clickLink("user_browser");
        // Click Link 'Groups' (id='editgroups_michael').
        tester.clickLink("editgroups_michael");
        // Select 'jira-users' from select box 'groupsToLeave'.
        tester.selectOption("groupsToLeave", "jira-users");
        tester.submit("leave");

        //check creating an issue works again!
        navigation.issue().goToCreateIssueForm(null,null);
        tester.setFormElement("summary", "Let's create another bug!");
        tester.submit("Create");
        //check that we made it to the view issue page!
        assertions.getTextAssertions().assertTextPresentHtmlEncoded("Let's create another bug!");
        tester.assertTextPresent("Details");
    }

    public void testAddUserOverLimitShowsWarning_Starter()
    {
        _testAddUserOverLimitShowsWarning(LicenseKeys.V2_STARTER);
    }

    public void testAddUserOverLimitShowsWarning_CommercialLimited()
    {
        _testAddUserOverLimitShowsWarning(LicenseKeys.V2_COMMERCIAL_LIMITED);
    }

    private void _testAddUserOverLimitShowsWarning(LicenseKeys.License license)
    {
        administration.switchToLicense(license);
        addUsersWhileUnderTheLimit("devman");
        addUsersWhileUnderTheLimit("prodman");
        addUsersWhileUnderTheLimit("fatman");


        // now we are at the limit, should not be able to create the next user
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("create_user");
        text.assertTextPresent(new WebPageLocator(tester), "Creating a new user will exceed the number of users allowed to use JIRA under your license.");
        tester.setFormElement("username", "barney");
        tester.setFormElement("password", "barney");
        tester.setFormElement("confirm", "barney");
        tester.setFormElement("fullname", "Barney");
        tester.setFormElement("email", "barney@example.com");
        tester.submit("Create");
        text.assertTextNotPresent(new WebPageLocator(tester), "jira-users");

        navigation.logout();
        navigation.gotoDashboard();
        navigation.loginAttempt("barney", "barney");
        tester.assertTextPresent("You do not have a permission to log in");
    }

    private void addUsersWhileUnderTheLimit(final String userName)
    {
        navigation.gotoAdminSection("user_browser");
        tester.clickLink("create_user");
        text.assertTextNotPresent(new WebPageLocator(tester), "Creating a new user will exceed the number of users allowed to use JIRA under your license.");
        tester.setFormElement("username", userName);
        tester.setFormElement("password", userName);
        tester.setFormElement("confirm", userName);
        tester.setFormElement("fullname", userName);
        tester.setFormElement("email", userName + "@example.com");
        tester.submit("Create");
        text.assertTextSequence(new WebPageLocator(tester), new String[] { "Groups:", "jira-users" });
    }

    public void testDeleteUserClearsActiveUserCount_Starter()
    {
        _testDeleteUserClearsActiveUserCount(LicenseKeys.V2_STARTER);
    }

    public void testDeleteUserClearsActiveUserCount_CommercialLimited()
    {
        _testDeleteUserClearsActiveUserCount(LicenseKeys.V2_COMMERCIAL_LIMITED);
    }

    private void _testDeleteUserClearsActiveUserCount(final LicenseKeys.License license)
    {
        administration.switchToLicense(license);

        //first check the current user limit.  should be 2
        navigation.gotoAdminSection("license_details");
        tester.assertTextPresent("2 currently active");

        //now lets delete an active user
        administration.usersAndGroups().deleteUser(FRED_USERNAME);

        //user limit should have decreased
        navigation.gotoAdminSection("license_details");
        tester.assertTextPresent("1 currently active");
    }

    public void testModifyGlobalPermissionsShowsWarning_Starter()
    {
        _testModifyGlobalPermissionsShowsWarning(LicenseKeys.V2_STARTER);
    }

    public void testModifyGlobalPermissionsShowsWarning_CommercialLimited()
    {
        _testModifyGlobalPermissionsShowsWarning(LicenseKeys.V2_COMMERCIAL_LIMITED);
    }

    private void _testModifyGlobalPermissionsShowsWarning(final LicenseKeys.License license)
    {
        //lets import data with one too many users!
        administration.restoreData("TestStarterLicenseGlobalPermissions.xml");
        administration.switchToLicense(license);

        navigation.gotoAdminSection("global_permissions");
        tester.assertTextPresent("Global Permissions");
        tester.assertTextPresent("These permissions apply to all projects. They are independent of project specific permissions.");
        // no warning should be displayed initially
        tester.assertTextNotPresent("You have exceeded the number of users allowed to use JIRA under");

        // now give the jira-developers group the USE permission - warning should be displayed
        administration.addGlobalPermission(Permissions.USE, "jira-developers");
        tester.assertTextPresent("You have exceeded the number of users allowed to use JIRA under");

        // remove the global permission - warning should disappear!
        administration.removeGlobalPermission(Permissions.USE, "jira-developers");
        tester.assertTextNotPresent("You have exceeded the number of users allowed to use JIRA under");
    }

    private void addMailServerConfig()
    {
        administration.mailServers().Smtp().goTo();
        tester.clickLinkWithText("Configure new SMTP mail server");
        tester.setFormElement("name", "mail");
        tester.setFormElement("description", "");
        tester.setFormElement("from", "mail_jira@example.com");
        tester.setFormElement("prefix", "JIRA:");
        tester.setFormElement("serverName", "localhost");
        tester.submit("Add");
    }

    public void testSignupOverLicenseLimit_Starter()
    {
        _testSignupOverLicenseLimit(LicenseKeys.V2_STARTER);
    }

    public void testSignupOverLicenseLimit_CommercialLimited()
    {
        _testSignupOverLicenseLimit(LicenseKeys.V2_COMMERCIAL_LIMITED);
    }

    private void _testSignupOverLicenseLimit(final LicenseKeys.License license)
    {
        administration.switchToLicense(license);
        // add 2 users to bring it up to 4 users in the system
        addUsersWhileUnderTheLimit("dudeman");
        addUsersWhileUnderTheLimit("prodman");

        // sign up a user when under limit
        navigation.logout();

        tester.gotoPage("login.jsp");
        tester.clickLink("signup");
        tester.setFormElement("username", "devman");
        tester.setFormElement("password", "devman");
        tester.setFormElement("confirm", "devman");
        tester.setFormElement("fullname", "Devman");
        tester.setFormElement("email", "devman@example.com");
        tester.submit();
        tester.assertTextPresent("You have successfully signed up. If you forget your password, you can have it emailed to you.");
        tester.clickLinkWithText("Click here to log in");

        navigation.login("devman", "devman");
        assertEquals("Devman", navigation.userProfile().userName());
        navigation.logout();

        // now sign up again - should be over limit
        tester.gotoPage("login.jsp");
        tester.clickLink("signup");
        tester.assertTextPresent("You cannot sign up at this time, as the user limit for JIRA has been exceeded.");
        text.assertTextPresent(new WebPageLocator(tester), "For further assistance, please contact your JIRA administrators.");

        // try to make the signup action execute manually
        tester.gotoPage("/secure/Signup.jspa");
        tester.assertTextPresent("You cannot sign up at this time, as the user limit for JIRA has been exceeded.");
        text.assertTextPresent(new WebPageLocator(tester), "For further assistance, please contact your JIRA administrators.");
    }

}