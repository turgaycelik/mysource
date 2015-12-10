package com.atlassian.jira.auditing.handlers;

import com.atlassian.jira.auditing.RecordRequest;
import com.atlassian.jira.event.notification.NotificationAddedEvent;
import com.atlassian.jira.event.notification.NotificationDeletedEvent;

/**
 *
 * @since v6.2
 */
public interface NotificationChangeHandler
{
    RecordRequest onNotificationAddedEvent(NotificationAddedEvent event);

    RecordRequest onNotificationDeletedEvent(NotificationDeletedEvent event);
}
