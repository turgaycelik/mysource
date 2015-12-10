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
 * Responsible for holding tests which verify that the Dashboards actions are not susceptible to
 * XSRF attacks.
 *
 * @since v6.1.1
 */
@WebTest ({ Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfManageDashboards extends FuncTestCase
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

    private Dashboard addSharedPublicDashboard()
    {
        return navigation.dashboard().addPage(Data.SHARED_PUBLIC_DASHBOARD, null);
    }

    protected void setUpTest()
    {
        backdoor.restoreBlankInstance();
    }

    public void testDasboardManagementOperations() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "Adding Dashboard",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.dashboard().navigateToMy();
                                tester.clickLink("create_page");
                                tester.setWorkingForm("add-dashboard");
                            }
                        },
                        new XsrfCheck.FormSubmissionWithId("add-dashboard-submit")
                ),
                new XsrfCheck(
                        "Restore Default Dashboard",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.dashboard().navigateToMy();
                                tester.clickLink("restore_defaults");
                                tester.setWorkingForm("jiraform");
                            }
                        },
                        new XsrfCheck.FormSubmissionWithId("restore_submit")
                )
        ).
        run(funcTestHelperFactory);
    }

    public void testDasboardOperations() throws Exception
    {
        addSharedPublicDashboard();
        new XsrfTestSuite(
                new XsrfCheck(
                        "Editing Dashboard",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.dashboard().navigateToMy();
                                tester.clickLink("edit_0");
                                tester.setWorkingForm("edit-dashboard");
                            }
                        },
                        new XsrfCheck.FormSubmission("update_submit")
                ),
                new XsrfCheck(
                        "Copy Dashboard",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.dashboard().navigateToMy();
                                tester.clickLink("clone_0");
                                tester.setWorkingForm("add-dashboard");
                                tester.setFormElement("portalPageName", "New Name");
                            }
                        },
                        new XsrfCheck.FormSubmissionWithId("add-dashboard-submit")
                ),
                new XsrfCheck(
                        "Delete Dashboard",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.dashboard().navigateToMy();
                                tester.clickLink("delete_0");
                                tester.setWorkingForm("delete-portal-page");
                            }
                        },
                        new XsrfCheck.FormSubmissionWithId("delete-portal-page-submit")
                )
        ).
        run(funcTestHelperFactory);
    }
}
