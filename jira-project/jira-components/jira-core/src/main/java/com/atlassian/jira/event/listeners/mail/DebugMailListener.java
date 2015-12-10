/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.listeners.mail;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.event.issue.DelegatingJiraIssueEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.JiraIssueEvent;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.queue.MailQueue;

/**
 * This listener is used to debug the MailListener.
 * <p>
 * Basically instead of actually sending an email, it will print the method call
 *
 * @see MailListener
 */
public class DebugMailListener extends MailListener
{
    public DebugMailListener(UserManager userManager, IssueEventBundleMailHandler issueEventBundleMailHandler, MailQueue mailQueueUserManager, IssueEventBundleFactory issueEventBundleFactory)
    {
        super(userManager, issueEventBundleMailHandler, mailQueueUserManager, issueEventBundleFactory);
    }

    @Override
    public boolean isInternal()
    {
        return false;
    }

    @Override
    protected void sendUserMail(UserEvent event, String subject, String subjectKey, String template)
    {
        logEvent(event);
        log("Subject: " + subject);
        log("Subject Key: " + subjectKey);
        log("Template: " + template);
    }

    @Override
    @EventListener
    public void handleIssueEventBundle(final IssueEventBundle bundle)
    {
        log("Issue Event Bundle received:");
        for (JiraIssueEvent event : bundle.getEvents())
        {
            if (event instanceof DelegatingJiraIssueEvent)
            {
                logEvent(((DelegatingJiraIssueEvent) event).asIssueEvent());
            }
        }
    }

    @Override
    protected void handleDefaultIssueEvent(final IssueEvent event)
    {
        logEvent(event);
    }

    /**
     * This is duplicated code from debugListener
     * @param event
     */
    private void logEvent(JiraEvent event)
    {
        try
        {
            if (event instanceof IssueEvent)
            {
                IssueEvent issueEvent = (IssueEvent) event;
                log("Issue: [#" + issueEvent.getIssue().getLong("id") + "] " + issueEvent.getIssue().getString("summary"));
                log("Comment: " + issueEvent.getComment());
                log("Change Group: " + issueEvent.getChangeLog());
                log("EventTypeId: " + issueEvent.getEventTypeId());
            }
            else if (event instanceof UserEvent)
            {
                UserEvent userEvent = (UserEvent) event;
                log("User: " + userEvent.getUser().getName() + " (" + userEvent.getUser().getEmailAddress() + ")");
            }

            log(" Time: " + event.getTime());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void log(String msg)
    {
        System.err.println("[DebugMailListener]: " + msg);
    }
}
