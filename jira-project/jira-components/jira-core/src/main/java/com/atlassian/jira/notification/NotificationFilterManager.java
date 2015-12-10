package com.atlassian.jira.notification;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.type.NotificationType;

/**
 * This allows notifications to filter before they are sent out.  Calls out to {@link NotificationFilter} plugin point
 * as part of this.
 *
 * @since v6.0
 */
public interface NotificationFilterManager
{
    /**
     * This will invoke plugins of {@link NotificationFilter} and recompute the possible recipients of the
     * notification.
     * <p/>
     * Adds are invoked for all plugins first and then removes.
     *
     * @param recipients the starting set of recipients
     * @param context a context object to pass to {@link NotificationFilter}s
     * @return the enhanced and filtered set of recipients
     */
    Iterable<NotificationRecipient> recomputeRecipients(Iterable<NotificationRecipient> recipients, NotificationFilterContext context);

    /**
     * This will invoke plugins of {@link NotificationFilter} and filter (only) a recipient of a notification
     *
     * @param recipient the target recipient
     * @param context a context object to pass to {@link NotificationFilter}s
     * @return whether the receipient should get the notification
     */
    boolean filtered(NotificationRecipient recipient, NotificationFilterContext context);


    /**
     * Creates a context for the specified reason
     *
     * @param reason the reason for the notification
     * @return a context
     */
    NotificationFilterContext makeContextFrom(NotificationReason reason);

    /**
     * Creates a context for the specified reason and for the issue
     *
     * @param reason the reason for the notification
     * @param issue the issue in play
     * @return a context
     */
    NotificationFilterContext makeContextFrom(NotificationReason reason, Issue issue);

    /**
     * Creates a context for the specified reason and issue event
     *
     * @param reason the reason for the notification
     * @param issueEvent the issue event in play
     * @return a context
     */
    NotificationFilterContext makeContextFrom(NotificationReason reason, IssueEvent issueEvent);

    /**
     * Creates a context with a new NotificationType
     *
     *
     *
     * @param copy copies state from a past one
     * @param notificationType the notification type in play
     * @return a context
     */
    NotificationFilterContext makeContextFrom(NotificationFilterContext copy, NotificationType notificationType);
}

