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
public class TestXsrfBulkOperations extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestXsrfBulkOperations.xml");
    }

    public void testBulkOperations() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("Bulk Delete Operation", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    tester.gotoPage("/issues/?jql=");
                    tester.gotoPage("/views/bulkedit/BulkEdit1!default.jspa?reset=true&tempMax=10000");
                    tester.checkCheckbox("bulkedit_10021", "on");
                    tester.submit("Next");
                    tester.checkCheckbox("operation", "bulk.delete.operation.name");
                    tester.submit("Next");

                }
            }, new XsrfCheck.FormSubmission("Confirm"))
        ).run(funcTestHelperFactory);
    }
}