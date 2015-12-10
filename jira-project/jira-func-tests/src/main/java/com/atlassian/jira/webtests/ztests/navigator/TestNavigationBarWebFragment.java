package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.log4j.Logger;

import static com.atlassian.jira.permission.ProjectPermissions.ADMINISTER_PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissions.BROWSE_PROJECTS;
import static com.atlassian.jira.permission.ProjectPermissions.CREATE_ISSUES;

/**
 * Simple test case that checks the links on the top system navigation bar
 * is visible with correct permissions.
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR })
public class TestNavigationBarWebFragment extends FuncTestCase
{
    public static final Logger log = Logger.getLogger(TestNavigationBarWebFragment.class);

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestNavigationBarWebFragment.xml");
    }

    @Override
    public void tearDownTest()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.restoreBlankInstance();
        super.tearDownTest();
    }

    public void testNavigationBarWebFragment()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        tester.assertLinkPresent("home_link"); //always visible

        _checkBrowseAndFindIssueLinksVisiblity();
        _checkCreateIssueLinkVisiblity();
        _checkAdminLinkVisiblityToProjectAdmin();
        _checkAdminLinkVisiblityToSystemAdmin();
    }

    public void _checkBrowseAndFindIssueLinksVisiblity()
    {
        //check we have the browse/find link to start with.
        tester.assertLinkPresent("browse_link");
        tester.assertLinkPresent("find_link");

        //remove the browse project permission and assert links are not available
        removeBrowsePermission();
        tester.assertLinkNotPresent("find_link");

        //add the browse permission back and check its displayed correctly.
        addBrowsePermission();
        navigation.gotoDashboard();
        tester.assertLinkPresent("find_link");
        tester.assertLinkPresent("browse_link");
        tester.assertLinkPresentWithText("Projects");

        navigation.gotoDashboard();
        tester.assertLinkPresent("browse_link");
        tester.assertLinkPresentWithText("Projects");
    }

    public void _checkCreateIssueLinkVisiblity()
    {
        //make sure we're no longer in the admin section (where the create issue link is no longer displayed).
        if (tester.getDialog().isLinkPresent("leave_admin"))
        {
            tester.clickLink("leave_admin");
        }
        //check we have the create issue link to start with.
        tester.assertLinkPresent("create_link");

        //remove the permission and assert link is not present
        removeCreatePermission();
        //make sure we're no longer in the admin section (where the create issue link is no longer displayed).
        if (tester.getDialog().isLinkPresent("leave_admin"))
        {
            tester.clickLink("leave_admin");
        }
        tester.assertLinkNotPresent("create_link");

        //readd the permission and assert its back
        addCreatePermission();
        //make sure we're no longer in the admin section (where the create issue link is no longer displayed).
        if (tester.getDialog().isLinkPresent("leave_admin"))
        {
            tester.clickLink("leave_admin");
        }
        tester.assertLinkPresent("create_link");
    }

    public void _checkAdminLinkVisiblityToProjectAdmin()
    {
        navigation.login("project_admin", "project_admin");
        assertTrue(administration.link().isPresent());

        //login as admin and remove the project admin permission from user: project_admin
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        removeProjectAdminPermission();
        navigation.gotoDashboard();
        assertTrue(administration.link().isPresent());

        //log back in as the project_admin, and assert admin link is not available
        navigation.logout();//must explicitly logout to invalidate session (SessionKeys.USER_PROJECT_ADMIN)
        navigation.login("project_admin", "project_admin");
        assertFalse(administration.link().isPresent());

        //login as admin and add the project admin permission for user: project_admin
        navigation.logout();//not neccessary but safe to logout here also
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        addProjectAdminPermission();
        navigation.gotoDashboard();
        assertTrue(administration.link().isPresent());

        //log back in as project_admin and assert link is back
        navigation.logout();//must explicitly logout to create new session (SessionKeys.USER_PROJECT_ADMIN)
        navigation.login("project_admin", "project_admin");
        assertTrue(administration.link().isPresent());
    }

    public void _checkAdminLinkVisiblityToSystemAdmin()
    {
        navigation.login("system_admin", "system_admin");
        assertTrue(administration.link().isPresent());

        //login as admin and remove the system_admin from the administrators group
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.usersAndGroups().removeUserFromGroup("system_admin", "jira-administrators");
        navigation.gotoDashboard();
        assertTrue(administration.link().isPresent());

        //log back in as the system_admin, and assert admin link is not available
        navigation.logout();//must explicitly logout to invalidate session (SessionKeys.USER_PROJECT_ADMIN)
        navigation.login("system_admin", "system_admin");
        assertFalse(administration.link().isPresent());

        //login as admin and add the system_admin permission for user: system_admin
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.usersAndGroups().addUserToGroup("system_admin", "jira-administrators");
        navigation.gotoDashboard();
        assertTrue(administration.link().isPresent());

        //log back in as project_admin and assert link is back
        navigation.login("system_admin", "system_admin");
        assertTrue(administration.link().isPresent());
    }

    public void removeBrowsePermission()
    {
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLink("0_edit");
        tester.clickLink("del_perm_" + BROWSE_PROJECTS.permissionKey() + "_10000");
        tester.submit("Delete");
    }

    public void removeCreatePermission()
    {
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLink("0_edit");
        tester.clickLink("del_perm_" + CREATE_ISSUES.permissionKey() + "_10000");
        tester.submit("Delete");
    }

    public void removeProjectAdminPermission()
    {
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLink("0_edit");
        tester.clickLink("del_perm_" + ADMINISTER_PROJECTS.permissionKey() + "_jira-developers");
        tester.submit("Delete");
    }

    public void addBrowsePermission()
    {
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLink("0_edit");
        tester.clickLink("add_perm_" + BROWSE_PROJECTS.permissionKey());
        tester.checkCheckbox("type", "group");
        tester.selectOption("group", "jira-users");
        tester.submit(" Add ");
    }

    public void addCreatePermission()
    {
        navigation.gotoAdmin();
        tester.clickLink("permission_schemes");
        tester.clickLink("0_edit");
        tester.clickLink("add_perm_" + CREATE_ISSUES.permissionKey());
        tester.checkCheckbox("type", "group");
        tester.selectOption("group", "jira-users");
        tester.submit(" Add ");
    }

    public void addProjectAdminPermission()
    {
        navigation.gotoAdminSection("permission_schemes");
        tester.clickLink("0_edit");
        tester.clickLink("add_perm_" + ADMINISTER_PROJECTS.permissionKey());
        tester.checkCheckbox("type", "group");
        tester.selectOption("group", "jira-developers");
        tester.submit(" Add ");
    }
}
