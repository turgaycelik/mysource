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
public class TestXsrfAdminAssortedGlobalSettings extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testEasySettings() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("Time Tracking Activate", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("timetracking");
                }
            }, new XsrfCheck.FormSubmission("Activate"))
            ,
            new XsrfCheck("Time Tracking Deactivate", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("timetracking");
                }
            }, new XsrfCheck.FormSubmission("Deactivate"))
            ,
            new XsrfCheck("Look And Feel Update", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("lookandfeel");
                }
            }, new XsrfCheck.FormSubmission("logo-upload")),
            new XsrfCheck("Look And Feel Update", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("lookandfeel");
                }
            }, new XsrfCheck.FormSubmission("favicon-upload")),
            new XsrfCheck("General Configuration Update", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("general_configuration");
                    tester.clickLink("edit-app-properties");
                    tester.setFormElement("introduction", "Hello!");
                }
            }, new XsrfCheck.FormSubmission("Update"))

        ).run(funcTestHelperFactory);
    }

}