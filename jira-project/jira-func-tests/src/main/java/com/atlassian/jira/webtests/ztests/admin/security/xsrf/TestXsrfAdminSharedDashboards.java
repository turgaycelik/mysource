package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.dashboard.DashboardPageInfo;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Permissions;

import static com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils.createPublicPermissions;
import static com.google.common.collect.Iterables.getFirst;

/**
 * Responsible for holding tests which verify that the Shared Dashboards Administration actions are not susceptible to
 * XSRF attacks.
 *
 * @since v6.0.2
 */
@WebTest ({ Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfAdminSharedDashboards extends FuncTestCase
{
    private static class Data
    {
        private static final DashboardPageInfo SHARED_PUBLIC_DASHBOARD =
                new DashboardPageInfo
                        (
                                10014, "Public - Owner: Admin", null, true, createPublicPermissions(), ADMIN_USERNAME,
                                1, DashboardPageInfo.Operation.ALL
                        );
    }

    protected void setUpTest()
    {
        backdoor.restoreBlankInstance();
    }

    private Dashboard addSharedPublicDashboard()
    {
        return navigation.dashboard().addPage(Data.SHARED_PUBLIC_DASHBOARD, null);
    }

    public void testSharedDasboardOperations() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "Delete Shared Dashboard",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addSharedPublicDashboard();
                                backdoor.permissions().addGlobalPermission(Permissions.CREATE_SHARED_OBJECT, "jira-users");
                                final Long dashboardId = getFirst(backdoor.dashboard().getOwnedDashboard("admin"), null).getId();
                                administration.sharedDashboards().goTo();
                                tester.clickLink("delete_" + dashboardId);
                                tester.setWorkingForm("delete-portal-page");
                            }
                        },
                        new XsrfCheck.FormSubmissionWithId("delete-portal-page-submit")
                )
        ).
        run(funcTestHelperFactory);
    }
}
