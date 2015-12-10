package com.atlassian.jira.notification;

/**
 * Defines a notification event
 */
public interface NotificationEvent
{
    public String getId();

    public String getName();

    public String getDescription();
}
