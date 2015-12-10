package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.DashboardAssertions;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.Arrays;
import java.util.List;

/**
 * Test to ensure that Manage Dashboard display only pages that you have permission to see.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS, Category.PERMISSIONS })
public class TestManageDashboardPagePermissions extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("DashboardPagePermissions.xml");
    }

    public void testCleanupOfPermissionsOnProjectDelete()
    {
        administration.project().deleteProject(10010);

        final DashboardAssertions dashboardAssertions = assertions.getDashboardAssertions();

        navigation.dashboard().navigateToPopular();
        List<SharedEntityInfo> list = Arrays.asList(
                globalPage(),
                privatePage(),
                sharedWithAdmins().setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions()),
                sharedWithDevelopers().setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions()),
                sharedWithJiraAdmin(),
                sharedWithJiraDeveloper(),
                sharedWithJiraUser(),
                sharedWithProjectAdmins(),
                sharedWithProjectDevelopers().setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions()),
                sharedWithProject(),
                systemPage());
        dashboardAssertions.assertDashboardPages(list, Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login("developer");

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                globalPage(),
                sharedWithJiraDeveloper(),
                sharedWithJiraUser(),
                sharedWithProject(),
                systemPage()), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login(FRED_USERNAME);

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                globalPage(),
                sharedWithJiraUser(),
                systemPage()), Dashboard.Table.POPULAR);
    }

    public void testCorrectDashboardsOnTabs()
    {
        final DashboardAssertions dashboardAssertions = assertions.getDashboardAssertions();

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                globalPage(),
                privatePage(),
                sharedWithAdmins(),
                sharedWithDevelopers(),
                sharedWithJiraAdmin(),
                sharedWithJiraDeveloper(),
                sharedWithJiraUser(),
                sharedWithProjectAdmins(),
                sharedWithProjectDevelopers(),
                sharedWithProject(),
                systemPage()), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login("developer");

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                globalPage(),
                sharedWithDevelopers(),
                sharedWithJiraDeveloper(),
                sharedWithJiraUser(),
                sharedWithProjectDevelopers(),
                sharedWithProject(),
                systemPage()), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login(FRED_USERNAME);

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                globalPage(),
                sharedWithJiraUser(),
                systemPage()), Dashboard.Table.POPULAR);
    }

    public void testCleanupOfPermissionsOnRoleDelete()
    {
        // delete Developers
        navigation.gotoAdmin();
        administration.roles().delete(10001);

        final DashboardAssertions dashboardAssertions = assertions.getDashboardAssertions();

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                globalPage(),
                privatePage(),
                sharedWithAdmins(),
                sharedWithDevelopers().setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions()),
                sharedWithJiraAdmin(),
                sharedWithJiraDeveloper(),
                sharedWithJiraUser(),
                sharedWithProjectAdmins(),
                sharedWithProjectDevelopers(),
                sharedWithProject(),
                systemPage()), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login("developer");

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                globalPage(),
                sharedWithJiraDeveloper(),
                sharedWithJiraUser(),
                sharedWithProject(),
                systemPage()), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login(FRED_USERNAME);

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                globalPage(),
                sharedWithJiraUser(),
                systemPage()), Dashboard.Table.POPULAR);
    }

    public void testCleanupOfPermissionsOnGroupDelete()
    {
        // delete jira-developers
        administration.usersAndGroups().deleteGroup("jira-developers");

        final DashboardAssertions dashboardAssertions = assertions.getDashboardAssertions();

        navigation.dashboard().navigateToPopular();
        List<SharedEntityInfo> list = Arrays.asList(
                globalPage(),
                privatePage(),
                sharedWithAdmins(),
                sharedWithDevelopers(),
                sharedWithJiraAdmin(),
                sharedWithJiraDeveloper().setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions()),
                sharedWithJiraUser(),
                sharedWithProjectAdmins(),
                sharedWithProjectDevelopers(),
                sharedWithProject(),
                systemPage()
        );
        dashboardAssertions.assertDashboardPages(list, Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login("developer");

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                globalPage(),
                sharedWithJiraUser(),
                systemPage()), Dashboard.Table.POPULAR);

        navigation.logout();
        navigation.login(FRED_USERNAME);

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertDashboardPages(Arrays.asList(
                globalPage(),
                sharedWithJiraUser(),
                systemPage()), Dashboard.Table.POPULAR);
    }

    private static SharedEntityInfo systemPage()
    {
        return new SharedEntityInfo(10000L, "System Dashboard", null, false, TestSharingPermissionUtils.createPublicPermissions());
    }

    private static SharedEntityInfo globalPage()
    {
        return new SharedEntityInfo(10011L, "Global", null, false, TestSharingPermissionUtils.createPublicPermissions());
    }

    private static SharedEntityInfo privatePage()
    {
        return new SharedEntityInfo(10010L, "Private", "Copy of 'System Dashboard'", false, TestSharingPermissionUtils.createPrivatePermissions());
    }

    private static SharedEntityInfo sharedWithAdmins()
    {
        return new SharedEntityInfo(10016L, "Shared with Admins", null, false, TestSharingPermissionUtils.createProjectPermissions(0, 0, "Dev Role Browse", "Administrators"));
    }

    private static SharedEntityInfo sharedWithDevelopers()
    {
        return new SharedEntityInfo(10015L, "Shared with Developers", null, false, TestSharingPermissionUtils.createProjectPermissions(0, 0, "Dev Role Browse", "Developers"));
    }

    private static SharedEntityInfo sharedWithJiraAdmin()
    {
        return new SharedEntityInfo(10014L, "Shared with jira-admin", null, false, TestSharingPermissionUtils.createGroupPermissions("jira-administrators"));
    }

    private static SharedEntityInfo sharedWithJiraDeveloper()
    {
        return new SharedEntityInfo(10013L, "Shared with jira-developer", null, false, TestSharingPermissionUtils.createGroupPermissions("jira-developers"));
    }

    private static SharedEntityInfo sharedWithJiraUser()
    {
        return new SharedEntityInfo(10012L, "Shared with jira-user", null, false, TestSharingPermissionUtils.createGroupPermissions("jira-user"));
    }

    private static SharedEntityInfo sharedWithProjectAdmins()
    {
        return new SharedEntityInfo(10019L, "Shared with proj Admins", null, false, TestSharingPermissionUtils.createProjectPermissions(0, 0, "Admin Role Browse", null));
    }

    private static SharedEntityInfo sharedWithProjectDevelopers()
    {
        return new SharedEntityInfo(10018L, "Shared with proj Developers", null, false, TestSharingPermissionUtils.createProjectPermissions(0, 0, "Dev Role Browse", null));
    }

    private static SharedEntityInfo sharedWithProject()
    {
        return new SharedEntityInfo(10017L, "Shared with Proj jira-dev", null, false, TestSharingPermissionUtils.createProjectPermissions(0, 0, "monkey", null));
    }
}