/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.EventUtils;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentAssignee extends AbstractNotificationType
{
    private static final Logger log = Logger.getLogger(CurrentAssignee.class);
    private JiraAuthenticationContext jiraAuthenticationContext;

    public CurrentAssignee(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        final List<NotificationRecipient> recipients;

        Issue issue = event.getIssue();
        if (issue != null)
        {
            recipients = getRecipientsForIssue(event, issue);
        }
        else
        {
            log.error("Error getting assignee notification recipients - no issue associated with event: " + event.getEventTypeId());
            recipients = Collections.emptyList();
        }
        return recipients;
    }

    /**
     * This is the notification strategy that is in place since JRA-6344 was addressed.  The previous assignee is always
     * notified if the assignee changes.
     *
     * @param event the issue event in play
     * @param issue the issue in play
     * @return a List of NotificationRecipients
     */
    List<NotificationRecipient> getRecipientsForIssue(IssueEvent event, Issue issue)
    {
        List<NotificationRecipient> recipients = new ArrayList<NotificationRecipient>();
        addPreviousAssignee(event, recipients);
        addCurrentAssignee(issue, recipients);
        return recipients;
    }

    /**
     * Adds the issues current assignee to the list if there is one
     *
     * @param issue the issue in play
     * @param recipients the list of possible recipients
     */
    private void addCurrentAssignee(final Issue issue, final List<NotificationRecipient> recipients)
    {
        final UserManager userManager = ComponentAccessor.getUserManager();
        final ApplicationUser u = userManager.getUserByKey(issue.getAssigneeId());
        if (u != null)
        {
            recipients.add(new NotificationRecipient(u));
        }
    }

    /**
     * Adds the previous assignee of an issue change if there is one
     * 
     * @param event the issue change event
     * @param recipients the list of possible recipients
     */
    private void addPreviousAssignee(final IssueEvent event, final List<NotificationRecipient> recipients)
    {
        final ApplicationUser previousAssignee = getPreviousAssignee(event);
        if (previousAssignee != null)
        {
            recipients.add(new NotificationRecipient(previousAssignee));
        }
    }

    /**
     * Designed to be overrriden for testing.  Gets the previous assignee by looking
     * in change history.
     *
     * TODO this could be improved by carrying the changes themselves in the IssueEvent instead of a change group GV and hence a DB interaction is required
     *
     * @param event the issue event in play
     * @return a previous assignee or null if there isnt one
     */
    ///CLOVER:OFF
    protected ApplicationUser getPreviousAssignee(final IssueEvent event)
    {
        return EventUtils.getPreviousAssignee(event);
    }
    ///CLOVER:ON

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.current.assignee");
    }
}
