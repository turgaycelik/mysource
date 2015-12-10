package com.atlassian.jira.notification;

import java.util.List;

import com.atlassian.jira.event.issue.IssueEvent;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit test of {@link NotificationTypeManager}.
 *
 * @since 6.2
 */
public class TestNotificationTypeManager
{
    private NotificationTypeManager notificationTypeManager;

    @Before
    public void setUp()
    {
        notificationTypeManager = new NotificationTypeManager("test-notification-event-types.xml");
    }

    @Test
    public void testGetNotificationTypeFail()
    {
        assertNull(notificationTypeManager.getNotificationType(""));
    }

    @Test
    public void testGetNotificationType()
    {
        // Invoke
        final NotificationType nt = notificationTypeManager.getNotificationType("TEST_TYPE_1");

        // Check
        assertNotNull(nt);
        final IssueEvent event = new IssueEvent(null, null, null, null);
        final List<?> addresses = nt.getRecipients(event, "owen@atlassian.com");
        assertEquals(1, addresses.size());
    }
}
