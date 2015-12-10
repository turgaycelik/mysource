package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.atlassian.jira.webtests.Permissions;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Notification;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.NotifyClient;
import com.icegreen.greenmail.store.FolderException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static javax.ws.rs.core.Response.*;
import static org.apache.commons.lang.StringUtils.*;
import static org.hamcrest.core.IsEqual.*;
import static org.junit.Assert.*;

/**
 * Func tests for issue watching use cases.
 *
 * @since v5.2
 */
@WebTest({Category.FUNC_TEST, Category.REST, Category.EMAIL})
public final class TestIssueResourceNotify extends EmailFuncTestCase
{
    private static final String TEST_XML = "TestIssueResourceNotify.xml";

    private static final String HSP_1_KEY = "HSP-1";
    private static final String HSP_1_SUMMARY = "i think i'm being watched";

    public static final String NOTIFICATION_GROUP = "notification-group";

    public static final String JACK_EMAIL = "jack@example.com";

    private final Notification notification = new Notification().textBody("Text Body").htmlBody("<strong>Html Body</strong>");
    private NotifyClient notifyClient;

    public void testNotificationUsingNotificationScheme() throws Exception
    {
        testNotification(notification, true, JACK_EMAIL, FRED_EMAIL, "scheme@example.com");
    }

    public void ignored_testNotificationToEmail() throws Exception
    {
        testNotification(notification.toEmail(ADMIN_EMAIL), true, ADMIN_EMAIL);
    }

    public void testNotificationToUser() throws Exception
    {
        testNotification(notification.toUser(FRED_USERNAME), true, FRED_EMAIL);
    }

    public void testNotificationToUserAsText() throws Exception
    {
        backdoor.userProfile().changeUserNotificationType(FRED_USERNAME, "text");
        testNotification(notification.toUser(FRED_USERNAME), false, FRED_EMAIL);
    }

    public void testNotificationToGroup() throws Exception
    {
        testNotification(notification.toGroup(NOTIFICATION_GROUP), true, FRED_EMAIL);
    }

    public void testNotificationToReporter() throws Exception
    {
        testNotification(notification.toReporter(), true, JACK_EMAIL);
    }

    public void testNotificationToAssignee() throws Exception
    {
        testNotification(notification.toAssignee(), true, FRED_EMAIL);
    }

    public void testNotificationToWatchers() throws Exception
    {
        testNotification(notification.toWatchers(), true, JACK_EMAIL, FRED_EMAIL);
    }

    public void testNotificationToVoters() throws Exception
    {
        testNotification(notification.toVoters(), true, FRED_EMAIL);
    }

    public void testNotificationRestrictingByGroup() throws Exception
    {
        testNotification(notification.toWatchers().restrictToGroup(NOTIFICATION_GROUP), true, FRED_EMAIL);
    }

    public void testNotificationRestrictingByPermissionId() throws Exception
    {
        backdoor.usersAndGroups().removeUserFromGroup(FRED_USERNAME, JIRA_USERS_GROUP); // this removes the browse permission for the user
        testNotification(notification.toWatchers().restrictToPermission(Permissions.BROWSE), true, JACK_EMAIL);
    }

    public void testNotificationRestrictingByPermissionName() throws Exception
    {
        backdoor.usersAndGroups().removeUserFromGroup(FRED_USERNAME, JIRA_USERS_GROUP); // this removes the browse permission for the user
        testNotification(notification.toWatchers().restrictToPermission("BROWSE"), true, JACK_EMAIL);
    }

    public void testNotificationWithNonDefaultSubject() throws Exception
    {
        testNotification(notification.toUser(FRED_USERNAME).subject("A non default subject"), true, FRED_EMAIL);
    }

    private void testNotification(Notification notification, boolean html, String... mailboxes) throws Exception
    {
        final Response response = notifyClient.loginAs(ADMIN_USERNAME).postResponse(HSP_1_KEY, notification);
        assertThat(response.statusCode, equalTo(Status.NO_CONTENT.getStatusCode()));

        flushMailQueueAndWait(mailboxes.length);

        for (String mailbox : mailboxes)
        {
            checkMailBox(mailbox, notification, html);
        }
        assertThat(mailService.getReceivedMessages().length, equalTo(0)); // no more messages
    }

    public void testNotificationOnNonExistingIssue() throws Exception
    {
        final Response response = notifyClient.loginAs(ADMIN_USERNAME).postResponse("NO-1", notification);
        assertThat(response.statusCode, equalTo(Status.NOT_FOUND.getStatusCode()));
    }

    public void testNotificationWithNoBrowsePermissionOnIssue() throws Exception
    {
        final Response response = notifyClient.loginAs("luser").postResponse("MKY-1", notification);
        assertThat(response.statusCode, equalTo(Status.FORBIDDEN.getStatusCode()));
    }

    public void testNotificationWithNonExistingUser() throws Exception
    {
        final Response response = notifyClient.loginAs(ADMIN_USERNAME).postResponse("MKY-1", notification.toUser("a-funky-user-name"));
        assertThat(response.statusCode, equalTo(Status.BAD_REQUEST.getStatusCode()));
    }

    public void testNotificationWithNonExistingGroup() throws Exception
    {
        final Response response = notifyClient.loginAs(ADMIN_USERNAME).postResponse("MKY-1", notification.toGroup("a-funky-group-name"));
        assertThat(response.statusCode, equalTo(Status.BAD_REQUEST.getStatusCode()));
    }

    public void testNotificationWithNonExistingRestrictingGroup() throws Exception
    {
        final Response response = notifyClient.loginAs(ADMIN_USERNAME).postResponse("MKY-1", notification.restrictToGroup("a-funky-group-name"));
        assertThat(response.statusCode, equalTo(Status.BAD_REQUEST.getStatusCode()));
    }

    public void testNotificationWithNonExistingPermission() throws Exception
    {
        final Response response = notifyClient.loginAs(ADMIN_USERNAME).postResponse("MKY-1", notification.restrictToPermission("a-funky-permission-name"));
        assertThat(response.statusCode, equalTo(Status.BAD_REQUEST.getStatusCode()));
    }

    private void checkMailBox(String email, Notification notification, boolean html) throws FolderException, MessagingException
    {
        final MailBox mailBox = getMailBox(email);
        MimeMessage message = mailBox.awaitMessage();
        assertEquals(message.getSubject(), isNotBlank(notification.subject) ? DEFAULT_SUBJECT_PREFIX + " " + notification.subject : DEFAULT_SUBJECT_PREFIX + " (" + HSP_1_KEY + ") " + HSP_1_SUMMARY);
        assertMessageAndType(message, html ? notification.htmlBody : notification.textBody, html);

        mailBox.clear();
    }

    protected void restoreData()
    {
        administration.restoreData(TEST_XML);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        notifyClient = new NotifyClient(getEnvironmentData());
        restoreData();
        configureAndStartSmtpServer();
    }
}
