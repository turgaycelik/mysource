package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableList;

import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserProfile extends FuncTestCase
{
    private static final String XSS_ALERT_RAW = "\"alert('surprise!')";
    private static final String XSS_ALERT_ESCAPED = "&quot;alert(&#39;surprise!&#39;)";

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testXssInAvatarPicker()
    {
        administration.usersAndGroups().addUser(XSS_ALERT_RAW, "password", XSS_ALERT_RAW, "xss@xss.com");
        navigation.login(XSS_ALERT_RAW, "password");
        navigation.userProfile().gotoCurrentUserProfile();
        assertions.getTextAssertions().assertTextPresent(XSS_ALERT_ESCAPED);
        assertions.getTextAssertions().assertTextNotPresent(XSS_ALERT_RAW);
    }

    public void testUrlPropertyRendersAsLink()
    {
        final List<String> links = ImmutableList.of("http://www.atlassian.com/uniqueLinkForThisTest", "www.atlassian-func-tests.com");
        for (int i = 0; i < links.size(); i++)
        {
            final String link = links.get(i);
            addUserProperty("homePage" + i, link);
            navigation.userProfile().gotoCurrentUserProfile();
            assertions.getLinkAssertions().assertLinkLocationEndsWith(link, link);
        }
    }

    public void testBadUrlPropertyDoesNotRenderAsLink()
    {
        final String linkUrl = "htp:/bad.url.com";
        addUserProperty("homePage", linkUrl);
        navigation.userProfile().gotoCurrentUserProfile();

        tester.assertLinkNotPresentWithText(linkUrl);
    }

    public void testUserChangePassword()
    {
        try
        {
            navigation.userProfile().gotoCurrentUserProfile();
            tester.clickLinkWithText("Change Password");

            //no password entered
            tester.setFormElement("current", "");
            tester.setFormElement("password", "");
            tester.setFormElement("confirm", "");
            tester.submit();
            text.assertTextPresent(locator.page(), "The current password specified is incorrect.");
            text.assertTextPresent(locator.page(), "The new password cannot be blank.");

            //invalid current password & mismatching new passwords
            tester.setFormElement("current", "invalid");
            tester.setFormElement("password", "password");
            tester.setFormElement("confirm", "mismatch");
            tester.submit();
            text.assertTextPresent(locator.page(), "The current password specified is incorrect.");
            text.assertTextPresent(locator.page(), "The password and confirmation do not match.");

            //successfully set new password
            final String newPassword = "newpassword";
            tester.setFormElement("current", ADMIN_PASSWORD);
            tester.setFormElement("password", newPassword);
            tester.setFormElement("confirm", newPassword);
            tester.submit();
            text.assertTextPresent(locator.page(), "Your password has successfully been changed");

            //confirm by login in with the new password
            navigation.logout();
            navigation.loginAttempt(ADMIN_USERNAME, ADMIN_PASSWORD);//check old password dont work
            text.assertTextPresent(locator.page(), "Sorry, your username and password are incorrect - please try again.");
            navigation.login(ADMIN_USERNAME, newPassword);

            assertEquals(ADMIN_FULLNAME, navigation.userProfile().userName());
        }
        finally
        {
            administration.restoreBlankInstance();
        }
    }

    /**
     * A user with no predefined language gets the language options in the system's default language
     */
    public void testShowsLanguageListInDefaultLanguage()
    {
        administration.restoreData("TestUserProfileI18n.xml");

        administration.generalConfiguration().setJiraLocale("Deutsch (Deutschland)");

        navigation.userProfile().gotoCurrentUserProfile();

        // assert that the page defaults to German
        text.assertTextPresent(locator.id("up-p-locale"), "Deutsch (Deutschland) [Standard]");

        tester.clickLink("edit_prefs_lnk");

        // assert that the list of languages has German selected by default
        assertions.getJiraFormAssertions().assertSelectElementHasOptionSelected("userLocale", "Deutsch (Deutschland) [Standard]");
    }

    /**
     * A user with a language preference that is different from the system's language gets the list of languages in his
     * preferred language.
     */
    public void testShowsLanguageListInTheUsersLanguage()
    {
        administration.restoreData("TestUserProfileI18n.xml");

        // set the system locale to something other than English just to be different
        administration.generalConfiguration().setJiraLocale("Deutsch (Deutschland)");

        navigation.login(FRED_USERNAME);

        navigation.userProfile().gotoCurrentUserProfile();

        // assert that the page defaults to Spanish
        text.assertTextPresent(locator.id("up-p-locale"), "espa\u00f1ol (Espa\u00f1a)");

        tester.clickLink("edit_prefs_lnk");

        // assert that the list of languages has Spanish selected by default
        assertions.getJiraFormAssertions().assertSelectElementHasOptionSelected("userLocale", "espa\u00f1ol (Espa\u00f1a)");
    }

    public void testChangeUserProfileRequiresPassword()
    {
        final String newFullName = "New FullName";
        final String newEmail = "newemail@example.com";

        updateUserProfileForm(ADMIN_USERNAME, newFullName, newEmail, "wrongpassword");
        tester.assertTextPresent("The password you entered is incorrect.");

        updateUserProfileForm(ADMIN_USERNAME, newFullName, newEmail, "");
        tester.assertTextPresent("The password you entered is incorrect.");

        updateUserProfileForm(ADMIN_USERNAME, newFullName, newEmail, ADMIN_PASSWORD);
        text.assertTextSequence(locator.page(), "Profile", newFullName, "Full Name", newFullName, "Email", newEmail);
    }

    private void updateUserProfileForm(String userName, String fullName, String email, String password)
    {
        tester.gotoPage("secure/EditProfile!default.jspa?username=" + userName);
        tester.setFormElement("fullName", fullName);
        tester.setFormElement("email", email);
        tester.setFormElement("password", password);
        tester.submit();
    }

    private void addUserProperty(String key, String value)
    {
        tester.gotoPage("/secure/admin/user/EditUserProperties.jspa?name=admin");
        tester.setFormElement("key", key);
        tester.setFormElement("value", value);
        tester.submit();
    }
}
