package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for holding tests which verify that the Shared Filters Administration actions are not susceptible to
 * XSRF attacks.
 *
 * @since v6.0.2
 */
@WebTest ({ Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfAdminSharedFilters extends FuncTestCase
{
    private static class Data
    {
        private static final String SHARED_PUBLIC_FILTER_NAME = "Public Filter - Owner: Admin";
    }

    protected void setUpTest()
    {
        backdoor.restoreBlankInstance();
    }

    private String addFilterSharedWithAllJiraUsers()
    {
        return backdoor.filters().createFilter("", Data.SHARED_PUBLIC_FILTER_NAME, "admin", "jira-users");
    }

    public void testSharedFilterOperations() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "Delete Shared Filter",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                final String filterId = addFilterSharedWithAllJiraUsers();
                                administration.sharedFilters().goTo();
                                tester.clickLink("delete_" + filterId);
                                tester.setWorkingForm("delete-filter-confirm-form-" + filterId);
                            }
                        },
                        new XsrfCheck.FormSubmissionWithId("delete-filter-submit")
                )
        ).
        run(funcTestHelperFactory);
    }
}
