/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.opensymphony.util.TextUtils;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class SingleEmailAddress extends AbstractNotificationType
{
    private JiraAuthenticationContext jiraAuthenticationContext;

    public SingleEmailAddress(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String emailAddress)
    {
        return asList(new NotificationRecipient(emailAddress));
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.single.email.address");
    }

    public String getType()
    {
        return "email";
    }

    public boolean doValidation(String key, Map parameters)
    {
        Object value = parameters.get(key);
        return (value != null && TextUtils.verifyEmail((String) value));
    }
}
