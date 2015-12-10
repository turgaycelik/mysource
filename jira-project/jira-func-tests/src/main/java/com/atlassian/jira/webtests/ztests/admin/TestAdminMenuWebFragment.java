package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * This test asserts that the links in the admin menu is available/unavailable under various conditions.
 *
 * To test just the admin menu, this test case only uses users with permission to goto the administration section
 *
 * To keep it simple, the test xml file only changed the group settings (and not any roles)
 */
@WebTest ({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestAdminMenuWebFragment extends FuncTestCase
{
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestWebFragment.xml");
    }

    public void tearDownTest()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.restoreBlankInstance();
        super.tearDownTest();
    }

    public void testAdminMenuWebFragment()
    {
        _testSystemAdminCanSeeAllAdminSections();
        _testProjectAdminCanSeeProjectSectionOnly();
        _testOtherUsersCannotSeeAdminSections();
    }

    /**
     * Test that all administrative sections are AVAILABLE when the user IS a system administrator
     */
    public void _testSystemAdminCanSeeAllAdminSections()
    {
        //assert system administrators can see all the sections
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.gotoAdmin();
        assertAdminLinksAreVisible();
    }

    /**
     * Test that the system administrator can only see the project section
     */
    public void _testProjectAdminCanSeeProjectSectionOnly()
    {
        //assert project administrator can only see the project section
        navigation.login("project_admin", "project_admin");
        navigation.gotoAdmin();
        assertAdminLinksAreNotVisible();
    }

    /**
     * Test that all other users (including not logged in) can only see project section with restriction message
     */
    public void _testOtherUsersCannotSeeAdminSections()
    {
        //users should not be able to view the Admin link - so go there directly
        navigation.login("user", "user");
        tester.gotoPage("/secure/project/ViewProjects.jspa");
        tester.assertTextPresent("You do not have the permissions to administer any projects, or there are none created.");
        assertAdminLinksAreNotVisible();

        //non-logged-in users should not be able to view the Admin link - so go there directly
        navigation.logout();
        tester.gotoPage("/secure/project/ViewProjects.jspa");
        assertions.assertNodeHasText(new CssLocator(tester, ".aui-message.warning"),"If you log in or sign up for an account, you might be able to see more here.");
        assertions.assertNodeByIdDoesNotExist("adminMenu"); // Users that are not logged in shouldn't see any admin menu
    }

    public void assertAdminLinksAreVisible()
    {
        //Now assert all the links are available
        tester.assertLinkPresent("view_projects");//always visible
        tester.assertLinkPresent("user_browser");
        tester.assertLinkPresent("group_browser");
        tester.assertLinkPresent("project_role_browser");
        tester.assertLinkPresent("attachments");
        tester.assertLinkPresent("edit_default_dashboard");
        tester.assertLinkPresent("general_configuration");
        tester.assertLinkPresent("global_permissions");
        tester.assertLinkPresent("linking");
        tester.assertLinkPresent("lookandfeel");
        tester.assertLinkPresent("outgoing_mail");
        tester.assertLinkPresent("incoming_mail");
        tester.assertLinkPresent("timetracking");
        tester.assertLinkPresent("user_defaults");
        tester.assertLinkPresent("permission_schemes");
        tester.assertLinkPresent("scheme_tools");
        tester.assertLinkPresent("view_custom_fields");
        tester.assertLinkPresent("field_configuration");
        tester.assertLinkPresent("field_screens");
        tester.assertLinkPresent("issue_types");
        tester.assertLinkPresent("priorities");
        tester.assertLinkPresent("resolutions");
        tester.assertLinkPresent("statuses");
        tester.assertLinkPresent("backup_data");
        tester.assertLinkPresent("restore_data");
        tester.assertLinkPresent("jelly_runner");
        tester.assertLinkPresent("send_email");
        tester.assertLinkPresent("edit_announcement");
        tester.assertLinkPresent("indexing");
        tester.assertLinkNotPresent("issue_caching"); //this is never displayed
        tester.assertLinkPresent("integrity_checker");
        tester.assertLinkPresent("license_details");
        tester.assertLinkPresent("listeners");
        tester.assertLinkPresent("logging_profiling");
        tester.assertLinkPresent("mail_queue");
        tester.assertLinkPresent("upm-admin-link");
        tester.assertLinkPresent("scheduler_details");
        tester.assertLinkPresent("services");
        tester.assertLinkPresent("system_info");

        tester.assertLinkPresent("view_categories");
        tester.assertLinkPresent("security_schemes");
        tester.assertLinkPresent("workflow_schemes");
        tester.assertLinkPresent("field_configuration");
        tester.assertLinkPresent("issue_fields");
        tester.assertLinkPresent("issue_type_screen_scheme");

        tester.assertLinkPresent("eventtypes");
        tester.assertLinkPresent("subtasks");
        tester.assertLinkPresent("workflows");
        tester.assertLinkPresent("field_screen_scheme");
    }

    public void assertAdminLinksAreNotVisible()
    {
        //Now assert all the links are available
        tester.assertLinkNotPresent("user_browser");
        tester.assertLinkNotPresent("group_browser");
        tester.assertLinkNotPresent("project_role_browser");
        tester.assertLinkNotPresent("attachments");
        tester.assertLinkNotPresent("edit_default_dashboard");
        tester.assertLinkNotPresent("general_configuration");
        tester.assertLinkNotPresent("global_permissions");
        tester.assertLinkNotPresent("linking");
        tester.assertLinkNotPresent("lookandfeel");
        tester.assertLinkNotPresent("outgoing_mail");
        tester.assertLinkNotPresent("incoming_mail");
        tester.assertLinkNotPresent("timetracking");
        tester.assertLinkNotPresent("user_defaults");
        tester.assertLinkNotPresent("permission_schemes");
        tester.assertLinkNotPresent("scheme_tools");
        tester.assertLinkNotPresent("view_custom_fields");
        tester.assertLinkNotPresent("field_configuration");
        tester.assertLinkNotPresent("field_screens");
        tester.assertLinkNotPresent("issue_types");
        tester.assertLinkNotPresent("priorities");
        tester.assertLinkNotPresent("resolutions");
        tester.assertLinkNotPresent("statuses");
        tester.assertLinkNotPresent("backup_data");
        tester.assertLinkNotPresent("restore_data");
        tester.assertLinkNotPresent("jelly_runner");
        tester.assertLinkNotPresent("send_email");
        tester.assertLinkNotPresent("edit_announcement");
        tester.assertLinkNotPresent("indexing");
        tester.assertLinkNotPresent("issue_caching"); //this is never displayed
        tester.assertLinkNotPresent("integrity_checker");
        tester.assertLinkNotPresent("license_details");
        tester.assertLinkNotPresent("listeners");
        tester.assertLinkNotPresent("logging_profiling");
        tester.assertLinkNotPresent("mail_queue");
        tester.assertLinkNotPresent("plugins");
        tester.assertLinkNotPresent("scheduler_details");
        tester.assertLinkNotPresent("services");
        tester.assertLinkNotPresent("system_info");

        tester.assertLinkNotPresent("view_categories");
        tester.assertLinkNotPresent("security_schemes");
        tester.assertLinkNotPresent("workflow_schemes");
        tester.assertLinkNotPresent("field_configuration");
        tester.assertLinkNotPresent("issue_fields");
        tester.assertLinkNotPresent("issue_type_screen_scheme");

        tester.assertLinkNotPresent("eventtypes");
        tester.assertLinkNotPresent("subtasks");
        tester.assertLinkNotPresent("workflows");
        tester.assertLinkNotPresent("field_screen_scheme");
    }
}
