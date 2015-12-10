/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

import com.google.common.collect.Lists;

import org.apache.log4j.Logger;

public class CurrentReporter extends AbstractNotificationType
{
    private static final Logger LOG = Logger.getLogger(CurrentReporter.class);
    private JiraAuthenticationContext jiraAuthenticationContext;

    public CurrentReporter(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        Issue issue = event.getIssue();
        if (issue != null)
        {
            final ApplicationUser user = ComponentAccessor.getUserManager().getUserByKey(issue.getReporterId());
            String level = (String)event.getParams().get("level");
            if ( user != null &&
                (level == null || userInGroup(user, level)) )
            {
                return Lists.newArrayList(new NotificationRecipient(user));
            }
            else
            {
                //Guest reported or user not in the relevant level
                return Collections.emptyList();
            }
        }
        else
        {
            LOG.error("Error getting reporter notification recipients - no issue associated with event: " + event.getEventTypeId());
        }
        return Collections.emptyList();
    }

    private static boolean userInGroup(ApplicationUser user, String groupName)
    {
        return ComponentAccessor.getGroupManager().isUserInGroup(user.getUsername(), groupName);
    }


    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.reporter");
    }
}
