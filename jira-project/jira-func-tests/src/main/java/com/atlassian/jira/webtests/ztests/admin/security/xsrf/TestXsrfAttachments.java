package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.ATTACHMENTS, Category.SECURITY })
public class TestXsrfAttachments extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testAttachmentAdministration() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("EditAttachmentSettings", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("attachments");
                    tester.clickLinkWithText("Edit Settings");
                    tester.checkCheckbox("attachmentPathOption", "DEFAULT");
                }
            }, new XsrfCheck.FormSubmission("Update"))
        ).run(funcTestHelperFactory);
    }
}
