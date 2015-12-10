/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class ProjectLead extends AbstractNotificationType
{
    private JiraAuthenticationContext jiraAuthenticationContext;

    public ProjectLead(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        final Project project = event.getIssue().getProjectObject();
        final String leadKey = project.getLeadUserKey();
        if (leadKey != null)
        {
            final ApplicationUser lead = ComponentAccessor.getUserManager().getUserByKey(leadKey);
            if (lead != null)
            {
                return asList(new NotificationRecipient(lead));
            }
        }
        return Collections.emptyList();
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.project.lead");
    }
}
