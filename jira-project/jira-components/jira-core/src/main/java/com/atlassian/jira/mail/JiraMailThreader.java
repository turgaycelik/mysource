/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailThreader;

/**
 * Implementation of MailThreader that stores threads in an OfBiz-mediated database.
 */
public class JiraMailThreader implements MailThreader
{
    private Issue issue;
    private static final AtomicInteger SEQUENCE_ID_FACTORY = new AtomicInteger();

    /**
     * Constructor
     *
     * @param issue     the issue - the subject of the emails.
     */
    public JiraMailThreader(Issue issue)
    {
        this.issue = issue;
    }

    @Override
    public void threadEmail(Email email)
    {
        // As of JIRA v5.2 we set "In-Reply-To" even on the IssueCreated event as it is a fake message ID for threading.
        // So we can do away with the NotificationInstance table. JRADEV-14311
        if (email instanceof com.atlassian.jira.mail.Email)
        {
            com.atlassian.jira.mail.Email jiraEmail = (com.atlassian.jira.mail.Email) email;
            ComponentAccessor.getMailThreadManager().threadNotificationEmail(jiraEmail, issue);
        }
    }

    @Override
    public void storeSentEmail(Email email)
    {
        // JRADEV-14311 We don't store anything - we send a custom Message-ID that we can parse instead.
    }

    @Override
    public String getCustomMessageId(Email email)
    {
        return JiraMailUtils.getMessageId(issue, SEQUENCE_ID_FACTORY.incrementAndGet());
    }
}
