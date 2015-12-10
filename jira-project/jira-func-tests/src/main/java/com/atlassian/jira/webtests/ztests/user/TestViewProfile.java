package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests that the links on the view profile page works.
 *
 * @since v3.12
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestViewProfile extends FuncTestCase
{
    public static final String PROJECT_MONKEY = "monkey";
    public static final String PROJECT_HOMOSAP = "homosapien";


    protected void setUpTest()
    {
        administration.restoreData("TestViewProfile.xml");
    }

    /*
        Test links use url encoded userName
     */
    public void testIssueNavigatorLinkEncoding() throws Exception
    {
        tester.gotoPage("/secure/ViewProfile.jspa?name=monkey%2Bman");
        text.assertTextNotPresent(tester.getDialog().getResponseText(), "= \"monkey+man\"");
        text.assertTextPresent(tester.getDialog().getResponseText(), "+%3D+%22monkey%2Bman%22");
    }

    public void testEmailVisibility()
    {
        //first check the e-mail is shown
        tester.clickLink("view_profile");
        text.assertTextSequence(new WebPageLocator(tester), new String[] { "Full Name:", ADMIN_FULLNAME, "Email:", "admin@example.com", "Groups:" });
        tester.assertLinkPresentWithText("admin@example.com");

        //now hide e-mails
        navigation.gotoAdminSection("general_configuration");
        tester.clickLink("edit-app-properties");
        tester.checkCheckbox("emailVisibility", "hide");
        tester.submit("Update");

        //email header + link should no longer be shown.
        tester.clickLink("view_profile");
        text.assertTextSequence(new WebPageLocator(tester), new String[] { "Full Name:", ADMIN_FULLNAME, "Groups:" });
        tester.assertTextNotPresent("Email:");
        tester.assertLinkNotPresentWithText("admin@example.com");

        //now mask e-mails
        navigation.gotoAdminSection("general_configuration");
        tester.clickLink("edit-app-properties");
        tester.checkCheckbox("emailVisibility", "mask");
        tester.submit("Update");

        //email header should be shown with no link.
        tester.clickLink("view_profile");
        text.assertTextSequence(new WebPageLocator(tester), new String[] { "Full Name:", ADMIN_FULLNAME, "Email:", "admin at example dot com", "Groups:" });
        tester.assertLinkNotPresentWithText("admin@example.com");

        //now only show emails to logged in users
        navigation.gotoAdminSection("general_configuration");
        tester.clickLink("edit-app-properties");
        tester.checkCheckbox("emailVisibility", "user");
        tester.submit("Update");

        //as a logged in user the email should be present.
        tester.clickLink("view_profile");
        text.assertTextSequence(new WebPageLocator(tester), new String[] { "Full Name:", ADMIN_FULLNAME, "Email:", "admin@example.com", "Groups:" });
        tester.assertLinkPresentWithText("admin@example.com");
    }

    public void testAllLinksShow()
    {
        navigation.dashboard();

        tester.clickLink("view_profile");
        tester.assertTextPresent("User Profile");

        // Check link to administer the user
        tester.clickLink("admin_user");
        tester.assertTextPresent("jira-administrators");

        // Check link to view the user's project roles
        tester.clickLink("view_profile");
        tester.clickLink("view_project_roles");
        assertThat(locator.css(".aui-nav-breadcrumbs a").getNodes()[0].getNodeValue(), equalTo("Users"));
        assertThat(locator.css(".aui-nav-breadcrumbs a").getNodes()[1].getNodeValue(), equalTo(ADMIN_FULLNAME));
        assertThat(locator.css(".aui-page-header h2").getText(), equalTo("View Project Roles for User"));

        // User can change password
        tester.clickLink("view_profile");
        tester.clickLink("view_change_password");
        tester.assertLinkNotPresent("view_change_password");
    }

    public void testNotOwnProfile()
    {
        tester.gotoPage("/secure/ViewProfile.jspa?name=fred");

        // Only three links should be present
        tester.assertLinkPresent("view_profile");
        tester.assertLinkPresent("view_project_roles");
        tester.assertLinkPresent("admin_user");

        // This better not be shown
        tester.assertTextNotPresent("Reports");
        tester.assertLinkNotPresent("voted");
        tester.assertLinkNotPresent("watched");
        tester.assertLinkNotPresent("view_change_password");
        tester.assertLinkNotPresent("view_dashboard_config");
        tester.assertLinkNotPresent("view_manage_filters");
        tester.assertLinkNotPresent("edit_prefs_lnk");
        tester.assertLinkNotPresent("edit_profile_lnk");
    }

    public void testNotLoggedIn()
    {
        try
        {
            navigation.logout();
            tester.gotoPage("/secure/ViewProfile.jspa?name=fred");

            tester.assertTextPresent("You must log in to access this page.");
        }
        finally
        {
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testVotingDisabled()
    {
        setGlobalOption("voting", "false");
        navigation.dashboard();
        tester.clickLink("view_profile");
        tester.assertLinkNotPresent("voted");

        tester.assertLinkPresent("view_project_roles");
        tester.assertLinkPresent("edit_profile_lnk");
        tester.assertLinkPresent("view_change_password");
        tester.assertLinkPresent("admin_user");
        tester.assertLinkPresent("watched");
        tester.assertLinkPresent("edit_prefs_lnk");

    }

    public void testWatchingDisabled()
    {
        setGlobalOption("watching", "false");
        navigation.dashboard();
        tester.clickLink("view_profile");
        tester.assertLinkNotPresent("watched");

        tester.assertLinkPresent("view_project_roles");
        tester.assertLinkPresent("edit_profile_lnk");
        tester.assertLinkPresent("view_change_password");
        tester.assertLinkPresent("admin_user");
        tester.assertLinkPresent("voted");
        tester.assertLinkPresent("edit_prefs_lnk");

    }

    public void testUserNotAdmin()
    {
        try
        {
            navigation.login(FRED_USERNAME, FRED_PASSWORD);
            navigation.dashboard();
            tester.clickLink("view_profile");
            tester.assertLinkNotPresent("view_project_roles");
            tester.assertLinkNotPresent("admin_user");

            tester.assertLinkPresent("edit_profile_lnk");
            tester.assertLinkPresent("view_change_password");
            tester.assertLinkPresent("voted");
            tester.assertLinkPresent("watched");
            tester.assertLinkPresent("edit_prefs_lnk");

        }
        finally
        {
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testExternalUserManagementEnabled()
    {
        administration.generalConfiguration().setExternalUserManagement(true);

        navigation.dashboard();
        tester.clickLink("view_profile");
        // JRA-27280 Disallow user editing own profile if External User Management is ON
        tester.assertLinkNotPresent("edit_profile_lnk");
        // JRA-27280 Disallow user editing own profile if External User Management is ON
        tester.assertLinkNotPresent("view_change_password");

        tester.assertLinkPresent("view_project_roles");
        tester.assertLinkPresent("admin_user");
        tester.assertLinkPresent("voted");
        tester.assertLinkPresent("watched");
        tester.assertLinkPresent("edit_prefs_lnk");
    }

    public void testAssignedOpenIssuesReport()
    {
        // 2 projects with no issues, 2 users

        // first verify that the report isn't shown for both users
        tester.clickLink("view_profile");
        tester.assertTextPresent("User Profile: " + ADMIN_FULLNAME);
        tester.assertTextPresent("Assigned Open Issues per Project");
        tester.assertTextPresent("You have no open issues assigned to you");
        tester.gotoPage("secure/ViewProfile.jspa?name=fred");
        tester.assertTextPresent("User Profile: " + FRED_FULLNAME);
        tester.assertTextNotPresent("Assigned Open Issues per Project");

        // manually create an issue
        navigation.issue().createIssue("homosapien", "Bug", "First issue");

        // verify the report is shown for the one project
        tester.clickLink("view_profile");
        tester.assertTextPresent("User Profile: " + ADMIN_FULLNAME);
        tester.assertTextPresent("Assigned Open Issues per Project");
        tester.assertLinkPresentWithText("homosapien");
        tester.gotoPage("secure/ViewProfile.jspa?name=fred");
        tester.assertTextPresent("User Profile: " + FRED_FULLNAME);
        tester.assertTextNotPresent("Assigned Open Issues per Project");
        navigation.logout();
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        tester.gotoPage("secure/ViewProfile.jspa?name=admin");
        tester.assertTextPresent("User Profile: " + ADMIN_FULLNAME);
        tester.assertTextPresent("Assigned Open Issues per Project");
        tester.assertLinkPresentWithText("homosapien");

        // manually create a second issue in second project
        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.issue().createIssue("monkey", "Bug", "Second issue");

        // verify both projects are shown
        tester.clickLink("view_profile");
        tester.assertTextPresent("User Profile: " + ADMIN_FULLNAME);
        tester.assertTextPresent("Assigned Open Issues per Project");
        tester.assertLinkPresentWithText("homosapien");
        tester.assertLinkPresentWithText("monkey");
        text.assertTextSequence(new WebPageLocator(tester), new String[] { "1", "homosapien", "1", "monkey" });
        tester.gotoPage("secure/ViewProfile.jspa?name=fred");
        tester.assertTextPresent("User Profile: " + FRED_FULLNAME);
        tester.assertTextNotPresent("Assigned Open Issues per Project");
        navigation.logout();
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        tester.gotoPage("secure/ViewProfile.jspa?name=admin");
        tester.assertTextPresent("User Profile: " + ADMIN_FULLNAME);
        tester.assertTextPresent("Assigned Open Issues per Project");
        tester.assertLinkPresentWithText("homosapien");
        tester.assertLinkPresentWithText("monkey");
        text.assertTextSequence(new WebPageLocator(tester), new String[] { "1", "homosapien", "1", "monkey" });
    }

    public void testEditProfileFieldsExceed255() throws Exception
    {
        tester.gotoPage("secure/EditProfile!default.jspa?username=admin");

        final String fullname = StringUtils.repeat("ABCDEFGH", 32);
        final String email = StringUtils.repeat("x", 246) + "@email.com";

        tester.setFormElement("fullName", fullname);
        tester.setFormElement("email", email);
        tester.submit();

        tester.assertTextPresent("The full name must not exceed 255 characters in length.");
        tester.assertTextPresent("The email address must not exceed 255 characters in length.");

        tester.setFormElement("fullName", fullname.substring(0, 255));
        tester.setFormElement("email", email.substring(0, 255));
        tester.submit();

        tester.assertTextNotPresent("The full name must not exceed 255 characters in length.");
        tester.assertTextNotPresent("The email address must not exceed 255 characters in length.");
    }

    private void setGlobalOption(String optionName, String value)
    {
        navigation.gotoAdminSection("general_configuration");
        tester.clickLink("edit-app-properties");
        tester.setFormElement("title", "jWebTest JIRA installation");
        tester.checkCheckbox(optionName, value);
        tester.submit("Update");
    }

    public void testAdminLink()
    {
        tester.clickLink("view_profile");
        tester.clickLink("admin_user");
        assertions.assertNodeByIdHasText("username", ADMIN_USERNAME);
        
        tester.clickLink("view_profile");
        tester.clickLink("view_project_roles");
        assertions.assertNodeByIdHasText("return_link", ADMIN_FULLNAME);

        navigation.userProfile().gotoUserProfile(FRED_USERNAME);
        tester.clickLink("admin_user");
        assertions.assertNodeByIdHasText("username", FRED_USERNAME);

        navigation.userProfile().gotoUserProfile(FRED_USERNAME);
        tester.clickLink("view_project_roles");
        assertions.assertNodeByIdHasText("return_link", FRED_FULLNAME);
    }
}
