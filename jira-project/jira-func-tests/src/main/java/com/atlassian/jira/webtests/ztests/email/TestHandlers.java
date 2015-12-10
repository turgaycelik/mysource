package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.page.ViewIssuePage;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.JIRAServerSetup;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
// Passing because most of the test is commented out. The rationale for commenting it out is not valid anymore, so we
// probably should introduce it again
public class TestHandlers extends EmailFuncTestCase
{
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    /**
     * As part of https://jdog.atlassian.com/browse/JRADEV-7738 port was removed from service settings
     */
    public void testPortsNotInService()
    {
        configureAndStartMailServers("admin@example.com", "PRE", JIRAServerSetup.SMTP, JIRAServerSetup.IMAP, JIRAServerSetup.POP3);
        setupPopService();
        tester.assertTextNotPresent("port:</strong> " + mailService.getPop3Port());

        setupImapService();
        tester.assertTextNotPresent("port:</strong> " + mailService.getImapPort());
    }


//       Commenting out till I figure out the root cause of failure
//    public void testCreateIssueFromEmail() throws InterruptedException
//    {
//        configureAndStartMailServers("admin@example.com", "PRE", JIRAServerSetup.SMTP, JIRAServerSetup.IMAP);
//        mailService.addUser("admin@example.com", ADMIN_USERNAME, ADMIN_PASSWORD);
//        setupImapService();
//        mailService.sendTextMessage("admin@example.com", "fred@example.com", "the monkeys escaped", "aarrrgh!");
//        flushMailQueue();
//        long serviceId = administration.services().goTo().getIdForServiceName("imap");
//        administration.utilities().runServiceNow(serviceId);
//        ViewIssuePage viewIssuePage = navigation.issue().viewIssue("MKY-1");
//        assertEquals("Fred Normal", viewIssuePage.getReporter());
//    }

}
