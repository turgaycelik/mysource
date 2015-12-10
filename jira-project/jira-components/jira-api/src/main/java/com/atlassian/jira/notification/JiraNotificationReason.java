package com.atlassian.jira.notification;

/**
 * The reasons that JIRA can produce notification
 *
 * @since v6.0
 */
public enum JiraNotificationReason implements NotificationReason
{
    /**
     * And issue event has happened
     */
    ISSUE_EVENT,
    /**
     * The recipient has been @mentioned
     */
    MENTIONED,
    /**
     * The recipient has been shared with the user
     */
    SHARED,
    /**
     * An {@link AdhocNotificationService} call has been made
     */
    ADHOC_NOTIFICATION
}