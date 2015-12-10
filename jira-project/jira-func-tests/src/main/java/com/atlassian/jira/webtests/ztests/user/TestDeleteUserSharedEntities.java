package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.plugins.ReferencePlugin;
import com.atlassian.jira.functest.framework.admin.user.DeleteUserPage;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.Arrays;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestDeleteUserSharedEntities extends FuncTestCase
{
    private static final SharedEntityInfo SYSTEM_PAGE = new SharedEntityInfo(10000L, "System Dashboard", null, true, TestSharingPermissionUtils.createPublicPermissions());
    private static final SharedEntityInfo DASHBOARD_2 = new SharedEntityInfo(10011L, "Dashboard 2", null, true, TestSharingPermissionUtils.createPublicPermissions());
    private static final SharedEntityInfo DASHBOARD_FOR_ADMIN = new SharedEntityInfo(10010L, "Dashboard for " + ADMIN_FULLNAME, "Copy of 'System Dashboard'", true, TestSharingPermissionUtils.createPublicPermissions());

    protected void setUpTest()
    {
        administration.restoreData("TestDeleteUserForSharedEntity.xml");
    }


    public void testDeleteUser()
    {
        DeleteUserPage deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters(FRED_USERNAME));
        assertThat(deleteUserPage.getNumberFromWarningFieldNamed(DeleteUserPage.SHARED_FILTERS), equalTo("3"));
        assertThat(deleteUserPage.getNumberFromWarningFieldNamedNoLink(DeleteUserPage.FAVORITED_FILTERS), equalTo("2"));
        assertThat(deleteUserPage.getNumberFromWarningFieldNamed(DeleteUserPage.SHARED_DASHBOARDS), equalTo("3"));
        assertThat(deleteUserPage.getNumberFromWarningFieldNamedNoLink(DeleteUserPage.FAVORITED_DASHBOARDS), equalTo("3"));
        deleteUserPage.clickDeleteUser();

        navigation.dashboard().navigateToPopular();

        SYSTEM_PAGE.setFavCount(1);
        DASHBOARD_2.setFavCount(1);
        DASHBOARD_FOR_ADMIN.setFavCount(1);

        assertions.getDashboardAssertions().assertDashboardPages(Arrays.asList(DASHBOARD_2, DASHBOARD_FOR_ADMIN, SYSTEM_PAGE), Dashboard.Table.POPULAR);

        navigation.manageFilters().popularFilters();

        text.assertTextPresent(new WebPageLocator(tester), "There are no filters in the system that you can view.");
    }

    public void testPreDeleteUserErrorMessages()
    {
        ReferencePlugin referencePlugin = administration.plugins().referencePlugin();
        if (referencePlugin.isInstalled() && referencePlugin.isEnabled())
        {
            String refUserName = "predeleteuser";
            navigation.login(ADMIN_USERNAME);
            administration.usersAndGroups().addUser(refUserName);
            DeleteUserPage deleteUserPage = navigation.gotoPageWithParams(DeleteUserPage.class, DeleteUserPage.generateDeleteQueryParameters(refUserName));
            assertThat(deleteUserPage.getUserDeletionError(), equalTo(deleteUserPage.getUserCannotBeDeleteMessage(refUserName)));
            assertThat(deleteUserPage.getNumberForPluginErrorNamed(Pattern.compile("(.*)Entity:(.*)")), equalTo("17"));
            navigation.logout();
        }
    }
}
