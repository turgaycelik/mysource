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
public class TestXsrfAdminUserDefaults extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testAdminUserDefaults() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("Edit User Defaults", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdmin();
                    tester.clickLink("user_defaults");
                    tester.clickLinkWithText("Edit default values");
                }
            }, new XsrfCheck.FormSubmission("Update"))
            ,
            new XsrfCheck("Edit User Defaults Apply", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdmin();
                    tester.clickLink("user_defaults");
                    tester.clickLinkWithText("Apply");
                }
            }, new XsrfCheck.FormSubmission("Update"))

        ).run(funcTestHelperFactory);
    }

}