package com.atlassian.jira.webtests.ztests.timetracking;

import java.util.List;

import javax.mail.internet.MimeMessage;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestWorklogNotifications extends EmailFuncTestCase
{
    public static final String EMAIL_ADDRESS = "admin@example.com";
    public static final String EMAIL_ADDRESS_USER_LISTENING_TO_ISSUE_UPDATED = "fred@example.com";

    public static final String TEXT_ON_WORKLOG_ADDED_MAIL = "added a worklog";
    public static final String TEXT_ON_WORKLOG_UPDATED_MAIL = "updated a worklog";
    public static final String TEXT_ON_WORKLOG_DELETED_MAIL = "deleted a worklog";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestWorklogNotificationsIncludingUserThatListensToIssueUpdated.xml");
        configureAndStartSmtpServer();
    }

    public void testAddingNewWorkLogSendsEmail() throws Exception
    {
        logSomeWork("TEST-1");

        assertOneEmailWasSent(EMAIL_ADDRESS, TEXT_ON_WORKLOG_ADDED_MAIL);
    }

    public void testUpdatingWorkLogSendsEmail() throws Exception
    {
        updateWork("TEST-1");

        assertOneEmailWasSent("admin@example.com", TEXT_ON_WORKLOG_UPDATED_MAIL);
    }

    public void testDeletingWorklogSendsEmail() throws Exception
    {
        deleteWork("TEST-1");

        assertOneEmailWasSent("admin@example.com", TEXT_ON_WORKLOG_DELETED_MAIL);
    }

    public void testUsersWhoOnlyListenToIssueUpdatedEventsDoNotReceiveAnyEmailsWhenLoggingWork() throws Exception
    {
        logSomeWork("TEST-1");

        assertNoEmailsWereSentTo(EMAIL_ADDRESS_USER_LISTENING_TO_ISSUE_UPDATED);
    }

    public void testUsersWhoOnlyListenToIssueUpdatedEventsDoNotReceiveAnyEmailsWhenUpdatingWork() throws Exception
    {
        updateWork("TEST-1");

        assertNoEmailsWereSentTo(EMAIL_ADDRESS_USER_LISTENING_TO_ISSUE_UPDATED);
    }

    public void testUsersWhoOnlyListenToIssueUpdatedEventsDoNotReceiveAnyEmailsWhenDeletingWork() throws Exception
    {
        deleteWork("TEST-1");

        assertNoEmailsWereSentTo(EMAIL_ADDRESS_USER_LISTENING_TO_ISSUE_UPDATED);
    }

    private void logSomeWork(String issueKey)
    {
        navigation.issue().logWork(issueKey, "1h");
    }

    private void updateWork(String issueKey)
    {
        tester.beginAt("/browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:worklog-tabpanel");
        tester.clickLink("edit_worklog_10000");
        tester.submit("Log");
    }

    private void deleteWork(String issueKey)
    {
        tester.beginAt("/browse/" + issueKey + "?page=com.atlassian.jira.plugin.system.issuetabpanels:worklog-tabpanel");
        tester.clickLink("delete_worklog_10000");
        tester.submit("Delete");
    }

    private void assertOneEmailWasSent(String emailAddress, String expectedText) throws Exception
    {
        flushMailQueueAndWait(1);
        List<MimeMessage> messagesForRecipient = getMessagesForRecipient(emailAddress);
        assertThat(messagesForRecipient.size(), is(1));
        assertEmailBodyContains(messagesForRecipient.get(0), expectedText);
    }

    private void assertNoEmailsWereSentTo(String emailAddress) throws Exception
    {
        flushMailQueueAndWait(1);
        List<MimeMessage> messagesForRecipient = getMessagesForRecipient(emailAddress);
        assertThat(messagesForRecipient.size(), is(0));
    }
}
