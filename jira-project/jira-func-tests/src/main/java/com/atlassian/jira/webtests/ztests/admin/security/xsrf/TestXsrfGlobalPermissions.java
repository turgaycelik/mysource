package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfGlobalPermissions extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testGlobalPermissionsAdministration() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("AddGlobalPermission", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("global_permissions");
                    tester.setWorkingForm("jiraform");
                    tester.selectOption("globalPermType", "Browse Users");
                    tester.selectOption("groupName", "jira-users");

                }
            }, new XsrfCheck.FormSubmission("Add")),
            new XsrfCheck("RemoveGlobalPermission", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("global_permissions");
                    tester.clickLink("del_USER_PICKER_jira-users");
                }
            }, new XsrfCheck.FormSubmission("Delete"))
        ).run(funcTestHelperFactory);
    }
}
