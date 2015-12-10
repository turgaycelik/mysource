package com.atlassian.jira.notification.type;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;

import com.opensymphony.util.TextUtils;

import static java.util.Arrays.asList;

public class TypeForTesting2 extends AbstractNotificationType
{
    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        return asList(new NotificationRecipient("edwin@atlassian.com"),
                new NotificationRecipient("mike@atlassian.com"),
                new NotificationRecipient("owen@atlassian.com") );
    }

    public String getDisplayName()
    {
        return "Test Type2";
    }

    public String getType()
    {
        return "test2";
    }

    public boolean doValidation(String key, Map parameters)
    {
        final Object value = parameters.get(key);
        return (value != null && TextUtils.stringSet((String) value));
    }
}
