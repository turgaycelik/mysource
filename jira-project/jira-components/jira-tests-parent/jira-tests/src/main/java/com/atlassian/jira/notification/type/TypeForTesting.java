/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;

import com.opensymphony.util.TextUtils;

import static java.util.Arrays.asList;

public class TypeForTesting extends AbstractNotificationType
{
    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        return asList(new NotificationRecipient(argument));
    }

    public String getDisplayName()
    {
        return "Test Type";
    }

    public String getType()
    {
        return "test";
    }

    public boolean doValidation(String key, Map parameters)
    {
        Object value = parameters.get(key);
        return (value != null && TextUtils.stringSet((String)value));
    }
}
