/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.mail.server.MailServerManager;

import java.util.Set;

public class DefaultIssueMailQueueItemFactory implements IssueMailQueueItemFactory
{
    private final TemplateContextFactory templateContextFactory;
    private final JiraAuthenticationContext authenticationContext;
    private final MailingListCompiler mailingListCompiler;
    private final TemplateManager templateManager;
    private final VelocityTemplatingEngine templatingEngine;
    private final MailServerManager mailServerManager;
    private final PermissionManager permissionManager;

    public DefaultIssueMailQueueItemFactory(TemplateContextFactory templateContextFactory,
            JiraAuthenticationContext authenticationContext, MailingListCompiler mailingListCompiler,
            TemplateManager templateManager, VelocityTemplatingEngine templatingEngine,
            MailServerManager mailServerManager, PermissionManager permissionManager)
    {
        this.templateContextFactory = templateContextFactory;
        this.authenticationContext = authenticationContext;
        this.mailingListCompiler = mailingListCompiler;
        this.templateManager = templateManager;
        this.templatingEngine = templatingEngine;
        this.mailServerManager = mailServerManager;
        this.permissionManager = permissionManager;
    }

    public IssueMailQueueItem getIssueMailQueueItem(IssueEvent event, Long templateId,
            Set<NotificationRecipient> recipientList, String notificationType)
    {
        return new IssueMailQueueItem(templateContextFactory, event, templateId, recipientList, notificationType,
                authenticationContext, mailingListCompiler, templateManager, templatingEngine, mailServerManager,
                permissionManager);
    }
}
