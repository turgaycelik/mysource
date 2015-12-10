package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.Navigable;

/**
 * Represents 'Notification schemes' administration section of JIRA.
 *
 * @since v4.4
 */
public interface NotificationSchemes extends Navigable<NotificationSchemes>
{

    /**
     * Add notification scheme with given <tt>name</tt> and <tt>description</tt>.
     *
     * @param name scheme name
     * @param description scheme description
     * @return edit notifications object for the created scheme
     */
    EditNotifications addNotificationScheme(String name, String description);

    /**
     * Edit notifications of a scheme with given <tt>id</tt>.
     *
     * @param id ID of the scheme
     * @return edit notifications instance for given scheme
     */
    EditNotifications editNotifications(int id);

}
