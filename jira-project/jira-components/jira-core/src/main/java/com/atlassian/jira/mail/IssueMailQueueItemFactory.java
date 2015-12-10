/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;

import java.util.Set;

public interface IssueMailQueueItemFactory
{
    public IssueMailQueueItem getIssueMailQueueItem(IssueEvent event, Long templateId, Set<NotificationRecipient> recipientList, String notificationType);
}
