package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.assertions.TextAssertionsImpl;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

/**
 * Checks the set password and edit user details actions.
 *
 * @since v3.12
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestEditUserDetails extends FuncTestCase
{
    public void testAdminCannotSetSysAdminPassword()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            administration.usersAndGroups().gotoViewUser(SYS_ADMIN_USERNAME);
            tester.assertLinkNotPresentWithText("Set Password");
            tester.assertTextPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");

            // hack url
            navigation.gotoPage(page.addXsrfToken("/secure/admin/user/SetPassword.jspa?name=root&password=root&confirm=root"));
            tester.assertTextPresent("Error");
            assertions.getJiraFormAssertions().assertFormErrMsg("Must be a System Administrator to reset a System Administrator's password.");

        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAdminCannotEditSysAdminDetails()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            administration.usersAndGroups().gotoViewUser(SYS_ADMIN_USERNAME);
            tester.assertLinkNotPresentWithText("Edit Details");
            tester.assertTextPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");

            // hack url
            tester.gotoPage(page.addXsrfToken("/secure/admin/user/EditUser.jspa?editName=root&username=root&fullName=rooty&email=root@example.com"));
            tester.assertTextPresent("Error");
            tester.assertTextPresent("Only System Administrators can edit other System Administrators details.");

        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSysAdminCanEditSysAdmin()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            navigation.login(SYS_ADMIN_USERNAME);
            administration.usersAndGroups().addUser("anothersysadmin");
            administration.usersAndGroups().addUserToGroup("anothersysadmin", "jira-sys-admins");

            administration.usersAndGroups().gotoViewUser("anothersysadmin");
            tester.assertTextNotPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");
            tester.clickLinkWithText("Edit Details");
            tester.setFormElement("fullName", "Rooty");
            tester.submit("Update");
            tester.assertTextPresent("Rooty");

        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testSysAdminCanSetSysAdminPassword()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");
            navigation.login(SYS_ADMIN_USERNAME);

            administration.usersAndGroups().addUser("anothersysadmin", "something", "Another User", "another@example.com");
            administration.usersAndGroups().addUserToGroup("anothersysadmin", "jira-sys-admins");

            administration.usersAndGroups().gotoViewUser("anothersysadmin");
            tester.assertTextNotPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");
            tester.clickLinkWithText("Set Password");
            tester.setFormElement("password", "another");
            tester.setFormElement("confirm", "another");
            tester.submit("Update");

            navigation.login("anothersysadmin", "another");
            tester.assertTextNotPresent("Sorry, your username and password are incorrect - please try again.");

        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }


    public void testAdminCanEditNormalUser()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            administration.usersAndGroups().gotoViewUser(FRED_USERNAME);
            tester.assertTextNotPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");
            tester.clickLinkWithText("Edit Details");
            tester.setFormElement("fullName", "Freddy Kruger");
            tester.submit("Update");
            tester.assertTextPresent("Freddy Kruger");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAdminCanSetNormalUsersPassword()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            administration.usersAndGroups().gotoViewUser(FRED_USERNAME);
            tester.assertTextNotPresent("This user is a System Administrator. Your permission to modify the user is restricted because you do not have System Administrator permissions.");
            tester.clickLinkWithText("Set Password");
            tester.setFormElement("password", "another");
            tester.setFormElement("confirm", "another");
            tester.submit("Update");

            navigation.login(FRED_USERNAME, "another");
            tester.assertTextNotPresent("Sorry, your username and password are incorrect - please try again.");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testUserNameWithScriptTags()
    {
        try
        {
            administration.restoreData("TestWithSystemAdmin.xml");

            final String value = "\"xss user'bad";
            final String valueEncoded = "&quot;xss user&#39;bad";

            administration.usersAndGroups().addUser(value, "password", value, "email@email.com");
            administration.usersAndGroups().gotoViewUser(value);

            tester.clickLinkWithText("Set Password");
            tester.setFormElement("password", "another");
            tester.setFormElement("confirm", "another");
            tester.submit("Update");
            tester.assertTextPresent(valueEncoded);
            tester.assertTextNotPresent(value);

            navigation.login(value, "another");
            tester.assertTextNotPresent("Sorry, your username and password are incorrect - please try again.");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    // JRADEV-776: testing the Add User form in admin section. Similar tests could be done for sign up and edit profile.
    public void testFieldsExceed255()
    {
        try
        {
            administration.restoreBlankInstance();

            administration.usersAndGroups().addUserWithoutVerifyingResult(StringUtils.repeat("abcdefgh", 32), "password", StringUtils.repeat("ABCDEFGH", 32), StringUtils.repeat("x", 246) + "@email.com");
            tester.assertTextPresent("The username must not exceed 255 characters in length.");
            tester.assertTextPresent("The full name must not exceed 255 characters in length.");
            tester.assertTextPresent("The email address must not exceed 255 characters in length.");

            administration.usersAndGroups().addUserWithoutVerifyingResult(StringUtils.repeat("abcdefgh", 32).substring(0, 255), "password", StringUtils.repeat("ABCDEFGH", 32).substring(0, 255), (StringUtils.repeat("x", 246) + "@email.com").substring(0, 255));
            tester.assertTextNotPresent("The username must not exceed 255 characters in length.");
            tester.assertTextNotPresent("The full name must not exceed 255 characters in length.");
            tester.assertTextNotPresent("The email address must not exceed 255 characters in length.");

            navigation.logout();
            navigation.login(StringUtils.repeat("abcdefgh", 32).substring(0, 255), "password");
            tester.assertTextPresent(StringUtils.repeat("ABCDEFGH", 32).substring(0, 255));
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }

    public void testAutocompleteIsOff()
    {
        try
        {
            final TextAssertions text = new TextAssertionsImpl();

            administration.restoreData("TestWithSystemAdmin.xml");

            administration.usersAndGroups().gotoViewUser(FRED_USERNAME);
            tester.clickLinkWithText("Set Password");

            XPathLocator xpathPassword = new XPathLocator(tester, "//*[@name=\"password\"]");
            text.assertRegexMatch(xpathPassword.getHTML(), "autocomplete=[ ]*\"off\"[ ]*");

            XPathLocator xpathConfirm = new XPathLocator(tester, "//*[@name=\"confirm\"]");
            text.assertRegexMatch(xpathConfirm.getHTML(), "autocomplete=[ ]*\"off\"[ ]*");
        }
        finally
        {
            navigation.logout();
            // go back to sysadmin user
            navigation.login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            administration.restoreBlankInstance();
        }
    }
}
