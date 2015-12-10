package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.google.common.collect.ImmutableList;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.EMAIL, Category.ISSUES })
public class TestBulkDeleteIssuesNotifications extends EmailFuncTestCase
{
    public void testBulkDeleteNoNoEmatifications() throws InterruptedException
    {
        administration.restoreData("TestBulkDeleteIssuesNotifications.xml");
        configureAndStartSmtpServer();
        bulkDeleteAllIssues(false, false);

        flushMailQueueAndWait(0);

        final MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(0, mimeMessages.length);
    }

    public void testBulkDeleteNotifications() throws InterruptedException, MessagingException
    {
        administration.restoreData("TestBulkDeleteIssuesNotifications.xml");
        configureAndStartSmtpServerWithNotify();
        bulkDeleteAllIssues(true, false);

        flushMailQueueAndWait(2);

        final MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(2, mimeMessages.length);
        assertRecipientsHaveMessages(ImmutableList.of("admin@example.com", "fred@example.com"));
    }
    

    public void testBulkDeleteSubtaskNoNotifications() throws InterruptedException
    {
        administration.restoreData("TestBulkDeleteIssuesNotificationsWithSubtasks.xml");
        configureAndStartSmtpServer();

        bulkDeleteAllIssues(false, true);

        flushMailQueueAndWait(0);

        final MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(0, mimeMessages.length);
    }

    public void testBulkDeleteSubtaskNotifications() throws InterruptedException, MessagingException
    {
        administration.restoreData("TestBulkDeleteIssuesNotificationsWithSubtasks.xml");
        configureAndStartSmtpServerWithNotify();

        bulkDeleteAllIssues(true, true);

        flushMailQueueAndWait(3);

        final MimeMessage[] mimeMessages = mailService.getReceivedMessages();
        assertEquals(3, mimeMessages.length);
        assertRecipientsHaveMessages(ImmutableList.of("admin@example.com", "fred@example.com"));

        List<String> subjects = new ArrayList<String>();
        List<MimeMessage> adminMessages = getMessagesForRecipient("admin@example.com");
        for (MimeMessage adminMessage : adminMessages)
        {
            subjects.add(adminMessage.getSubject());
        }

        assertEquals(2, subjects.size());
        System.out.println("subjects = " + subjects);
        assertTrue(subjects.contains("[JIRATEST] (HSP-2) This is my bug"));
        assertTrue(subjects.contains("[JIRATEST] (HSP-4) Subtask1"));

        List<MimeMessage> fredMessages = getMessagesForRecipient("fred@example.com");
        assertEmailSubjectEquals(fredMessages.get(0), "[JIRATEST] (HSP-3) This is fred's bug");
    }

    private void bulkDeleteAllIssues(boolean sendMail, boolean subtaskPresent)
    {
        //first lets try deleting issues without e-mail notifications
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        tester.checkCheckbox("bulkedit_10002", "on");
        tester.checkCheckbox("bulkedit_10001", "on");
        tester.submit("Next");
        tester.setFormElement("operation", "bulk.delete.operation.name");
        tester.submit("Next");
        //checkbox is checked by default.
        if (!sendMail)
        {
            tester.uncheckCheckbox("sendBulkNotification");
        }
        tester.submit("Next");
        if (!sendMail)
        {
            text.assertTextSequence(locator.page(), "Email notifications will", "NOT", "be sent for this update.");
        }
        else
        {
            tester.assertTextPresent("Email notifications will be sent for this update.");
        }
        if (subtaskPresent)
        {
            tester.assertTextPresent("Subtask1");
        }
        else
        {
            tester.assertTextNotPresent("Subtask1");
        }
        tester.submit("Confirm");
        waitAndReloadBulkOperationProgressPage();
        tester.assertElementNotPresent("issuetable");
    }

}