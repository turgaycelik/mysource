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
public class TestXsrfAdminIssueLinking extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testEasySettings() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("Issue Linking Activate", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdmin();
                    tester.clickLink("linking");
                }
            }, new XsrfCheck.FormSubmission("Activate"))
            ,
            new XsrfCheck("Issue Linking Add", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdmin();
                    tester.clickLink("linking");
                    tester.setFormElement("name", "inout");
                    tester.setFormElement("outward", "out");
                    tester.setFormElement("inward", "in");
                }
            }, new XsrfCheck.FormSubmission("Add"))
            ,
            new XsrfCheck("Issue Linking Edit", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdmin();
                    tester.clickLink("linking");
                    tester.clickLink("edit_inout");
                }
            }, new XsrfCheck.FormSubmission("Update"))
            ,
            new XsrfCheck("Issue Linking Delete", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdmin();
                    tester.clickLink("linking");
                    tester.clickLinkWithText("Delete");
                }
            }, new XsrfCheck.FormSubmission("Delete"))
            ,
            new XsrfCheck("Issue Linking DeActivate", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdmin();
                    tester.clickLink("linking");
                }
            }, new XsrfCheck.FormSubmission("Deactivate"))

        ).run(funcTestHelperFactory);
    }

}