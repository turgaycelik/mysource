package com.atlassian.jira.notification;

import com.atlassian.annotations.PublicSpi;

/**
 * A NotificationFilter allows a plugin to add and remove {@link NotificationRecipient} s to a notification event.
 * <p/>
 * {@link #addRecipient(NotificationFilterContext, Iterable)}  is always called first for all plugins and then {@link
 * #removeRecipient(NotificationRecipient, NotificationFilterContext)} is called after that to remove any recipients that are not
 * needed.
 * <p/>
 * You are very likely to be called multiple times during an issue event (depending on circumstances) and hence you need
 * to manager your side effects. The {@link NotificationFilterContext} object passed to you has a state map where you
 * can place stateful information and perhaps prevent multiple effects happening (if need be).
 *
 * @since v6.0
 */
@PublicSpi
public interface NotificationFilter
{

    /**
     * This called called to add possible new {@link NotificationRecipient}s to a notification event.  Its possible that
     * event is null in which case this means that there is no JIRA issue event in play but rather some other adhoc call
     * to the JIRA {@link AdhocNotificationService}.
     *
     *
     * @param context the context of this possible notification
     * @param intendedRecipients this list of recipients that has been build up to this point.
     * @return an iterable of new NotificationRecipients
     */
    Iterable<NotificationRecipient> addRecipient(NotificationFilterContext context, Iterable<NotificationRecipient> intendedRecipients);

    /**
     * This called called to remove {@link NotificationRecipient}s from a notification event.  Its possible that event
     * is null in which case this means that tyhere is no JIRA issue event in play but rather some other ahoc call to
     * the JIRA {@link AdhocNotificationService}.
     * <p/>
     * This is ALWAYS called after {@link #addRecipient(NotificationFilterContext, Iterable)}  has been called for all plugins.
     *
     * @param context the context of this possible notification
     * @return a boolean as to whether the user should get a notification
     */
    boolean removeRecipient(NotificationRecipient recipient, NotificationFilterContext context);
}

