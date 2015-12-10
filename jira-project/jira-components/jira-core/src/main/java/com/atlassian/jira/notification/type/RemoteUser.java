/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class RemoteUser extends AbstractNotificationType
{
    private JiraAuthenticationContext jiraAuthenticationContext;

    public RemoteUser(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        if (event.getUser() == null)
        {
            //Guest user
            return Collections.emptyList();
        }
        else
        {
            return asList(new NotificationRecipient(event.getUser()));
        }
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.current.user");
    }
}
