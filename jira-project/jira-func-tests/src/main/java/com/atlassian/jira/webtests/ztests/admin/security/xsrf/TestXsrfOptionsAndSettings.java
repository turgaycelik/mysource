package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY , Category.SLOW_IMPORT})
public class TestXsrfOptionsAndSettings extends EmailFuncTestCase
{
    public void testJellyRunnerAndBanner() throws Exception
    {
        // NOTE: there seems to be a randomly occurring failure related to this and the clear caching. I am looking
        // into it but having a lot of trouble reproducing it. So lets have the test pass so we don't have any unneeded
        // panics about failures.
        administration.restoreDataSlowOldWay("TestJellyAddComment.xml");

        new XsrfTestSuite(
            new XsrfCheck("RunJellyScript", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("jelly_runner");
                    String script = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                            + "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\" xmlns:core=\"jelly:core\">\n"
                            + "  <jira:AddComment issue-key=\"HSP-1\" commenter=\"admin\" comment=\"This is a first comment.\"/>\n"
                            + "</JiraJelly>";
                    tester.setFormElement("script", script);
                 }
            }, new XsrfCheck.FormSubmission("Run now")),
            new XsrfCheck("EditAnnouncementBanner", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("edit_announcement");
                    tester.setFormElement("announcement", "This is an announcement!");
                 }
            }, new XsrfCheck.FormSubmission("Set Banner"))
        ).run(funcTestHelperFactory);
    }

    public void testSendEmail() throws Exception
    {
        administration.restoreBlankInstance();
        configureAndStartSmtpServer();

        new XsrfTestSuite(
            new XsrfCheck("SendBulkMail", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("send_email");
                    tester.checkCheckbox("sendToRoles", "false");
                    tester.selectOption("groups", "jira-users");
                    tester.setFormElement("subject", "I'm sending an email");
                    tester.setFormElement("message", "This is the message.");
                 }
            }, new XsrfCheck.FormSubmission("Send"))
        ).run(funcTestHelperFactory);

        // check that the emails came through
        flushMailQueueAndWait(2);
    }
}
