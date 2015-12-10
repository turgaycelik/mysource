/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification;

import com.atlassian.jira.event.issue.IssueEvent;

import java.util.List;
import java.util.Map;

/**
 * <p/>
 * Type of notification recipient; single user, group, assignee, etc.
 *
 * <p/>
 * Notification types are registered in notification-event-types.xml.
 */
public interface NotificationType
{
    /**
     * Who is to be notified of an event.
     *
     * @param event The event, eg. issue created
     * @param argument Configuration from notification scheme, eg. group name, custom field id.
     * @return A list of {@link NotificationRecipient}s.
     */
    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument);

    /**
     * Text to display for this type, on the "Add Notification" page.
     *
     * @return (Internationalized) text to display, eg. "Group".
     */
    public String getDisplayName();

    /**
     * Type identifier (currently unused).
     *
     * @return Eg. "group"
     */
    public String getType();

    /**
     * Validate the configuration (the argument in {@link #getRecipients(com.atlassian.jira.event.issue.IssueEvent, String)})
     * when it is entered in the "Add Notification" page.
     * For instance, a group notification type might check if the entered group exists.
     *
     * @param key Key of relevant value in parameters
     * @param parameters HTML form parameters (only the keyed entry is relevant).
     * @return Whether the entered value is valid for this type.
     */
    public boolean doValidation(String key, Map parameters);

    /**
     * Obtains the user-friendly display for the argument.  For example, returns a username for a key.
     *
     * @param argument Raw configuration value
     * @return Formatted configuration value.
     */
    public String getArgumentDisplay(String argument);

    /**
     * Obtains the raw parameter value to save for the given user entry user-friendly display value.  For example,
     * the user picker displays a username, but this needs to be stored in the configuration parameter as the user's
     * key, instead.
     *
     * @param displayValue Value entered into the form (username, for example)
     * @return raw configuration value.
     */
    public String getArgumentValue(String displayValue);
}
