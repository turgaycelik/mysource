package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.functest.framework.admin.DefaultIssueEvents;
import com.atlassian.jira.functest.framework.admin.NotificationType;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.EmailFuncTestCase;
import com.google.common.collect.ImmutableMap;

import javax.mail.internet.MimeMessage;
import java.util.List;

/**
 * <p/>
 * Test that issue notifications are being sent on issue delete to particular subscribers.
 *
 * <p/>
 * See:<br/>
 * http://jira.atlassian.com/browse/JRA-21646<br/>
 * http://jira.atlassian.com/browse/JRA-24331
 *
 * @since 4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.EMAIL, Category.ISSUES })
public class TestIssueDeleteNotifications extends EmailFuncTestCase
{

    private static final String HENRY_FORD_EMAIL = "Henry.Ford@example.com";
    private static final String HENRY_FORD_USERNAME = "henry.ford";

    private static final String USER_CF_ID = "customfield_10000";
    private static final String MULTI_USER_CF_ID = "customfield_10001";
    private static final String GROUP_CF_ID = "customfield_10002";
    private static final String MULTI_GROUP_CF_ID = "customfield_10003";

    private static final String TEST_GROUP_ONE = "to-notify-1";
    private static final String TEST_GROUP_TWO = "to-notify-2";

    public void testAllWatchersNotifiedOfIssueDeletion() throws Exception
    {
        administration.restoreData("TestIssueNotificationsWithCustomFields.xml");
        configureAndStartSmtpServerWithNotify();

        administration.notificationSchemes().goTo().addNotificationScheme("Test scheme", "Test description")
                .addNotificationsForEvent(DefaultIssueEvents.ISSUE_DELETED.eventId(), NotificationType.ALL_WATCHERS);

        administration.project().associateNotificationScheme("COW", "Test scheme");
        final String issueKey = navigation.issue().createIssue("COW", "Bug", "Notification test");
        navigation.issue().addWatchers(issueKey, ADMIN_USERNAME, HENRY_FORD_USERNAME);
        navigation.issue().deleteIssue(issueKey);

        flushMailQueueAndWait(2);
        assertGotDeleteIssueMessages(issueKey, ADMIN_EMAIL, HENRY_FORD_EMAIL);
    }

    public void testUserFromUserCustomFieldNotifiedOfIssueDeletion() throws Exception
    {
        administration.restoreData("TestIssueNotificationsWithCustomFields.xml");
        configureAndStartSmtpServer();

        administration.notificationSchemes().goTo().addNotificationScheme("Test scheme", "Test description")
                .addNotificationsForEvent(DefaultIssueEvents.ISSUE_DELETED.eventId(),
                        NotificationType.USER_CUSTOM_FIELD_VALUE, USER_CF_ID);
        administration.project().associateNotificationScheme("COW", "Test scheme");

        final String issueKey = navigation.issue().createIssue("COW", "Bug", "Notification test",
                ImmutableMap.<String, String[]>of(USER_CF_ID, new String[] { BOB_USERNAME } ));
        navigation.issue().deleteIssue(issueKey);

        flushMailQueueAndWait(1);
        assertGotDeleteIssueMessages(issueKey, BOB_EMAIL);
    }

    public void testUsersFromMultiUserCustomFieldNotifiedOfIssueDeletion() throws Exception
    {
        administration.restoreData("TestIssueNotificationsWithCustomFields.xml");
        configureAndStartSmtpServer();

        administration.notificationSchemes().goTo().addNotificationScheme("Test scheme", "Test description")
                .addNotificationsForEvent(DefaultIssueEvents.ISSUE_DELETED.eventId(),
                        NotificationType.USER_CUSTOM_FIELD_VALUE, MULTI_USER_CF_ID);
        administration.project().associateNotificationScheme("COW", "Test scheme");

        final String issueKey = navigation.issue().createIssue("COW", "Bug", "Notification test",
                ImmutableMap.<String, String[]>of(MULTI_USER_CF_ID, new String[] { BOB_USERNAME + "," + FRED_USERNAME } ));
        navigation.issue().deleteIssue(issueKey);

        flushMailQueueAndWait(2);
        assertGotDeleteIssueMessages(issueKey, BOB_EMAIL, FRED_EMAIL);
    }

    public void testUsersFromGroupCustomFieldNotifiedOfIssueDeletion() throws Exception
    {
        administration.restoreData("TestIssueNotificationsWithCustomFields.xml");
        configureAndStartSmtpServer();

        administration.notificationSchemes().goTo().addNotificationScheme("Test scheme", "Test description")
                .addNotificationsForEvent(DefaultIssueEvents.ISSUE_DELETED.eventId(),
                        NotificationType.GROUP_CUSTOM_FIELD_VALUE, GROUP_CF_ID);
        administration.project().associateNotificationScheme("COW", "Test scheme");
        setUpGroups();

        final String issueKey = navigation.issue().createIssue("COW", "Bug", "Notification test",
                ImmutableMap.<String, String[]>of(GROUP_CF_ID, new String[] { TEST_GROUP_ONE } ));
        navigation.issue().deleteIssue(issueKey);

        flushMailQueueAndWait(2);
        assertGotDeleteIssueMessages(issueKey, BOB_EMAIL, FRED_EMAIL);
    }

    public void testUsersFromMultiGroupCustomFieldNotifiedOfIssueDeletion() throws Exception
    {
        administration.restoreData("TestIssueNotificationsWithCustomFields.xml");
        configureAndStartSmtpServer();

        administration.notificationSchemes().goTo().addNotificationScheme("Test scheme", "Test description")
                .addNotificationsForEvent(DefaultIssueEvents.ISSUE_DELETED.eventId(),
                        NotificationType.GROUP_CUSTOM_FIELD_VALUE, MULTI_GROUP_CF_ID);
        administration.project().associateNotificationScheme("COW", "Test scheme");
        setUpGroups();

        final String issueKey = navigation.issue().createIssue("COW", "Bug", "Notification test",
                ImmutableMap.<String, String[]>of(MULTI_GROUP_CF_ID, new String[] { TEST_GROUP_ONE, TEST_GROUP_TWO } ));
        navigation.issue().deleteIssue(issueKey);

        flushMailQueueAndWait(3);
        assertGotDeleteIssueMessages(issueKey, BOB_EMAIL, FRED_EMAIL, HENRY_FORD_EMAIL);
    }

    private void setUpGroups()
    {
        backdoor.usersAndGroups().addGroup(TEST_GROUP_ONE);
        backdoor.usersAndGroups().addUserToGroup(BOB_USERNAME, TEST_GROUP_ONE);
        backdoor.usersAndGroups().addUserToGroup(FRED_USERNAME, TEST_GROUP_ONE);
        backdoor.usersAndGroups().addGroup(TEST_GROUP_TWO);
        backdoor.usersAndGroups().addUserToGroup(BOB_USERNAME, TEST_GROUP_TWO);
        backdoor.usersAndGroups().addUserToGroup(HENRY_FORD_USERNAME, TEST_GROUP_TWO);
    }

    private void assertGotDeleteIssueMessages(String deletedIssueKey, String... emails) throws Exception
    {
        assertEquals(emails.length, mailService.getReceivedMessages().length);
        for (String username : emails)
        {
            final List<MimeMessage> received = getMessagesForRecipient(username);
            assertEquals("Unexpected number of messages for " + username, 1, received.size());
            final MimeMessage message = received.get(0);
            final String subject = message.getSubject();
            final String expectedPhrase = String.format("(%s)", deletedIssueKey);
            assertTrue("Subject '" + subject  + "' does not contain expected expected phrase: " + expectedPhrase,
                    subject.contains(expectedPhrase));

            assertEmailBodyContains(message, "deleted");
        }
    }


}
