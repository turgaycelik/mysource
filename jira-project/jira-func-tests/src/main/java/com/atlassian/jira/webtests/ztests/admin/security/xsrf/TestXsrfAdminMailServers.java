package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.2
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfAdminMailServers extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testAdminMailServer() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck("Add Mail Server", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        administration.mailServers().Smtp().goTo();
                        tester.gotoPage("AddSmtpMailServer!default.jspa");
                        tester.setFormElement("name", "name");
                        tester.setFormElement("description", "description");
                        tester.setFormElement("from", "brad@atlassian.com");
                        tester.setFormElement("prefix", "prefix");
                        tester.setFormElement("serverName", "server.example.com");
                    }
                }, new XsrfCheck.FormSubmission("Add"))
                ,
                new XsrfCheck("Edit Mail Server", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        administration.mailServers().Smtp().goTo();
                        tester.gotoPage("UpdateSmtpMailServer!default.jspa?id=10000");
                        tester.setFormElement("name", "nameX");
                    }
                }, new XsrfCheck.FormSubmission("Update"))
                ,
                new XsrfCheck("Send Test Mail Server", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        administration.mailServers().Smtp().goTo();
                        tester.gotoPage("SendTestMail!default.jspa");
                    }
                }, new XsrfCheck.FormSubmission("Send"))
                ,
                new XsrfCheck("Delete Mail Server", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        administration.mailServers().Smtp().goTo();
                        tester.gotoPage("DeleteMailServer!default.jspa?id=10000");
                    }
                }, new XsrfCheck.FormSubmission("Delete"))


        ).run(funcTestHelperFactory);
    }
}