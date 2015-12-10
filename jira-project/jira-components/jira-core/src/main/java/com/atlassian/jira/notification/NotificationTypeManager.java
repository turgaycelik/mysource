package com.atlassian.jira.notification;

import com.atlassian.jira.scheme.AbstractSchemeTypeManager;

import java.util.Map;

public class NotificationTypeManager extends AbstractSchemeTypeManager<NotificationType>
{
    private final String configFile;
    private volatile Map<String, NotificationType> schemeTypes;

    public NotificationTypeManager()
    {
        this("notification-event-types.xml");
    }

    public NotificationTypeManager(String configFile)
    {
        this.configFile = configFile;
    }

    public String getResourceName()
    {
        return configFile;
    }

    public Class<NotificationTypeManager> getTypeClass()
    {
        return NotificationTypeManager.class;
    }

    public NotificationType getNotificationType(String id)
    {
        return getTypes().get(id);
    }

    public Map<String, NotificationType> getSchemeTypes()
    {
        return schemeTypes;
    }

    public void setSchemeTypes(Map<String, NotificationType> schemeType)
    {
        schemeTypes = schemeType;
    }
}
