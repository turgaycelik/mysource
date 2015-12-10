package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.jira.functest.framework.Dashboard.Table;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.sharing.GroupTestSharingPermission;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Test that a dashboard and its permissions are handled correctly when groups, users or project
 * are deleted.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS })
public class TestDashboardRelatedEntitiesDelete extends FuncTestCase
{
    private static final String GROUP_DELETE = "group_delete";
    private static final String GROUP_ADMIN = "jira-administrators";

    private static final String PROJECT_DELETE_NAME = "project_delete";
    private static final int PROJECT_DELETE_ID = 10010;

    private static final int ROLE_DELETE_ID = 10010;
    private static final String ROLE_DELETE_NAME = "role_delete";

    private static final int DASHBOARD_PROJECT_ID = 10013;
    
    private SharedEntityInfo dashboardCopy;
    private SharedEntityInfo roleDashboard;
    private SharedEntityInfo groupDashboard;
    private SharedEntityInfo projectDashboard;


    protected void setUpTest()
    {
        dashboardCopy = new SharedEntityInfo("Dashboard for " + ADMIN_FULLNAME, "Copy of 'System Dashboard'", true,
                TestSharingPermissionUtils.createPublicPermissions(), null, null);

        groupDashboard = new SharedEntityInfo("group_dashboard", null, true,
                new HashSet<GroupTestSharingPermission>(Arrays.asList(new GroupTestSharingPermission(GROUP_DELETE), new GroupTestSharingPermission(GROUP_ADMIN))),
                null, null);

        projectDashboard = new SharedEntityInfo("project_dashboard", null, true,
                TestSharingPermissionUtils.createProjectPermissions(PROJECT_DELETE_ID, -1, PROJECT_DELETE_NAME, null),
                null, null);

        roleDashboard = new SharedEntityInfo("role_dashboard", null, true,
                TestSharingPermissionUtils.createProjectPermissions(PROJECT_DELETE_ID, ROLE_DELETE_ID, PROJECT_DELETE_NAME, ROLE_DELETE_NAME),
                null, null);

        administration.restoreData("sharedpages/TestDashboardRelatedEntitiesDelete.xml");
        navigation.gotoAdmin();

    }

    /**
     * Test that the dashboard shares are cleaned up when a group is deleted.
     */
    public void testDeleteGroup()
    {
        administration.usersAndGroups().deleteGroup(GROUP_DELETE);

        //deleting the group should remove that share permission.
        groupDashboard.setSharingPermissions(TestSharingPermissionUtils.createGroupPermissions(GROUP_ADMIN));

        navigation.dashboard().navigateToMy();
        assertions.getDashboardAssertions().assertDashboardPages(Arrays.asList(dashboardCopy, groupDashboard, projectDashboard, roleDashboard), Table.OWNED);
    }

    /**
     * Make sure that deleting a role also deletes any related shares.
     */
    public void testDeleteRole()
    {
        administration.roles().delete(ROLE_DELETE_ID);

        //deleting the role should make this a private permission.
        roleDashboard.setSharingPermissions(TestSharingPermissionUtils.createPrivatePermissions());

        navigation.dashboard().navigateToMy();
        assertions.getDashboardAssertions().assertDashboardPages(Arrays.asList(dashboardCopy, groupDashboard, projectDashboard, roleDashboard), Table.OWNED);
    }
}
