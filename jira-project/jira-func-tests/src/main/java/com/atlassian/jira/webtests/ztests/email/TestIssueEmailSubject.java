package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import javax.mail.internet.MimeMessage;
import java.util.List;

/**
 * @since v4.1
 */
@WebTest ({ Category.FUNC_TEST, Category.EMAIL })
public class TestIssueEmailSubject extends EmailFuncTestCase
{
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueNotificationsCurrentAssignee.xml");
        configureAndStartSmtpServerWithNotify();
        administration.generalConfiguration().setAllowUnassignedIssues(true);
    }

    private void assertEmailSubject(Runnable setup, String expectedSubjectHeading) throws Exception {
        setup.run();
        flushMailQueueAndWait(1);

        final List<MimeMessage> messagesForAdmin = getMessagesForRecipient("admin@example.com");
        assertEquals(1, messagesForAdmin.size());

        final MimeMessage message = messagesForAdmin.get(0);
        assertEmailSubjectEquals(message, expectedSubjectHeading);
    }

    public void testCreateIssue() throws Exception
    {
         assertEmailSubject(new Runnable() {
            public void run()
            {
                navigation.issue().createIssue("COW", null, "New issue");
            }
        }, "[JIRATEST] (COW-4) New issue");
    }

    public void testIssueUpdated() throws Exception
    {
        assertEmailSubject(new Runnable() {
            public void run()
            {
                navigation.issue().setDescription("COW-2", "Updated text");
            }
        }, "[JIRATEST] (COW-2) This cow has a calf");
    }

    public void testAssignIssue() throws Exception
    {
        assertEmailSubject(new Runnable() {
            public void run()
            {
                navigation.issue().unassignIssue("COW-2", "this is a comment");
            }
        }, "[JIRATEST] (COW-2) This cow has a calf");
    }

    public void testIssueResolved() throws Exception
    {
        assertEmailSubject(new Runnable() {
            public void run()
            {
                navigation.issue().resolveIssue("COW-2", "Fixed", "Yay!");
            }
        }, "[JIRATEST] (COW-2) This cow has a calf");
    }

    public void testIssueCommented() throws Exception
    {
        assertEmailSubject(new Runnable() {
            public void run()
            {
                navigation.issue().addComment("COW-2", "jaisodf", null);
            }
        }, "[JIRATEST] (COW-2) This cow has a calf");
    }

    public void testIssueDeleted() throws Exception
    {
        assertEmailSubject(new Runnable() {
            public void run()
            {
                navigation.issue().deleteIssue("COW-3");
            }
        }, "[JIRATEST] (COW-3) A calf is a tasty little renet factory");
    }
}
