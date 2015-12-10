package com.atlassian.jira.functest.framework.admin;

/**
 * Represents functionality of the 'Edit notifications' screen.
 *
 * @since v4.4
 */
public interface EditNotifications
{

    /**
     * ID of associated notification scheme.
     *
     * @return ID of the notification scheme
     */
    int notificationSchemeId();

    /**
     * Add simple (parameterless) notification type for given event ID.
     *
     * @param eventId ID of the event to add to
     * @param notificationType notification type to add
     * @return this instance
     *
     * @see NotificationType
     * @see DefaultIssueEvents
     */
    EditNotifications addNotificationsForEvent(int eventId, NotificationType notificationType);


    /**
     * Add parameterized notification type for given event ID.
     *
     * @param eventId ID of the event to add to
     * @param notificationType notification types to add
     * @param paramValue form parameter value associated with given notification type
     * @return this instance
     *
     * @see NotificationType
     * @see DefaultIssueEvents
     */
    EditNotifications addNotificationsForEvent(int eventId, NotificationType notificationType, String paramValue);
}
