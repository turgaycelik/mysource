package com.atlassian.jira.notification;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.type.AbstractNotificationType;

import java.util.List;
import java.util.Map;

public class ErrorNotificationType extends AbstractNotificationType implements NotificationType
{
    private String displayName;

    public ErrorNotificationType(String displayName)
    {
        this.displayName = displayName;
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        return null;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getType()
    {
        return "error";
    }

    public boolean doValidation(String key, Map parameters)
    {
        return false;
    }
}
