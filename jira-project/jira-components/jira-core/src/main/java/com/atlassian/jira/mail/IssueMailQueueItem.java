/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailThreader;
import com.atlassian.mail.queue.AbstractMailQueueItem;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.template.TemplateSources.fragment;

public class IssueMailQueueItem extends AbstractMailQueueItem
{
    private static final Logger log = Logger.getLogger(IssueMailQueueItem.class);

    private final TemplateContextFactory templateContextFactory;
    private final JiraAuthenticationContext authenticationContext;
    private final MailingListCompiler mailingListCompiler;
    private final TemplateManager templateManager;
    private final VelocityTemplatingEngine templatingEngine;
    private final MailServerManager mailServerManager;
    private final PermissionManager permissionManager;

    private IssueEvent event;
    private Long templateId;
    private Set<NotificationRecipient> recipientList;
    private String notificationType;

    /**
     * Create an issue mail queue item.
     *
     *
     * @param templateContextFactory template context factory
     * @param event the event that is the subject of this mail item.
     * @param templateId the template ID for this mail item.
     * @param recipientList a list of recipients for this mail item.
     * @param notificationType notification type
     * @param authenticationContext authentication context
     * @param mailingListCompiler mailing list compiler
     * @param templatingEngine velocity templating engine
     * @param mailServerManager mail server manager
     * @param permissionManager permission manager
     *
     * @see IssueMailQueueItemFactory
     */
    public IssueMailQueueItem(final TemplateContextFactory templateContextFactory, final IssueEvent event,
            final Long templateId, final Set<NotificationRecipient> recipientList, final String notificationType,
            final JiraAuthenticationContext authenticationContext, final MailingListCompiler mailingListCompiler,
            final TemplateManager templateManager, final VelocityTemplatingEngine templatingEngine,
            final MailServerManager mailServerManager, final PermissionManager permissionManager)
    {
        this.templateContextFactory = templateContextFactory;
        this.event = event;
        this.templateId = templateId;
        this.recipientList = recipientList;
        this.notificationType = notificationType;
        this.authenticationContext = authenticationContext;
        this.mailingListCompiler = mailingListCompiler;
        this.templateManager = templateManager;
        this.templatingEngine = templatingEngine;
        this.mailServerManager = mailServerManager;
        this.permissionManager = permissionManager;
    }

    /**
     * This is the subject as displayed in the Mail Queue Admin page. The subject is displayed in the preference
     * language of the current user viewing items to be sent (i.e. different from items CURRENTLY being sent).
     * <p/>
     * The subject will be displayed in the preference language of the mail recipient once the mail is actually being
     * sent. When the mail is being sent, it is a SingleMailQueueItem.
     *
     * @return String   the subject as displayed on the mail queue admin page
     */
    @Override
    public String getSubject()
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        try
        {
            final String subjectTemplate = templateManager.getTemplateContent(templateId, "subject");
            final Map<String, Object> contextParams = getIssueContextParams(i18n.getLocale(), event);
            contextParams.put("eventTypeName", ((IssueTemplateContext) contextParams.get("context")).getEventTypeName(i18n));
            return templatingEngine.render(fragment(subjectTemplate)).applying(contextParams).asPlainText();
        }
        catch (Exception e)
        {
            log.error("Could not determine subject", e);
            return i18n.getText("bulk.bean.initialise.error");
        }
    }

    public void send() throws MailException
    {
        incrementSendCount();

        final Issue issue = event.getIssue();

        try
        {
            final SMTPMailServer smtp = mailServerManager.getDefaultSMTPMailServer();

            if (smtp == null)
            {
                log.warn("There is no default SMTP Mail server so mail cannot be sent");
            }
            else
            {
                if (issue == null)
                {
                    throw new MailException("Notification not sent; issue no longer exists [event=" + event + "]");
                }

                if (!recipientList.isEmpty())
                {
                    // Ensure that issue level security is respected
                    for (final Iterator iterator = recipientList.iterator(); iterator.hasNext();)
                    {
                        final NotificationRecipient recipient = (NotificationRecipient) iterator.next();
                        if (!permissionManager.hasPermission(Permissions.BROWSE, issue, recipient.getUserRecipient()))
                        {
                            iterator.remove();
                        }
                    }

                    final MailThreader threader = new JiraMailThreader(issue);
                    final Map<String, Object> contextParams = getIssueContextParams(authenticationContext.getLocale(), event);
                    mailingListCompiler.sendLists(recipientList, JiraMailUtils.getProjectEmailFromIssue(issue), JiraMailUtils.getFromNameForUser(event.getUser()), templateId,
                        (String) event.getParams().get("baseurl"), contextParams, threader);
                }
            }
        }
        catch (final MailException me)
        {
            throw me;
        }
        catch (final Exception ex)
        {
            log.error(ex.getMessage(), ex);
            throw new MailException(ex.getMessage(), ex);
        }
    }

    @Override
    public String toString()
    {
        final Issue issue = event.getIssue();
        final String issueString = new ToStringBuilder(issue).append("id", issue.getId()).append("summary", issue.getSummary()).append("key",
            issue.getKey()).append("created", issue.getCreated()).append("updated", issue.getUpdated()).append("assignee", issue.getAssignee()).append(
            "reporter", issue.getReporter()).toString();
        return new ToStringBuilder(this).append("issue", issueString).append("remoteUser", event.getUser()).append("notificationType",
            notificationType).append("eventTypeId", event.getEventTypeId().longValue()).append("templateId", templateId.longValue()).toString();
    }

    protected Map<String, Object> getIssueContextParams(final Locale locale, final IssueEvent iEvent) throws GenericEntityException
    {
        final Map<String, Object> contextParams = new HashMap<String, Object>();

        // NOTE: if adding a parameter here please update the doc online at
        // https://developer.atlassian.com/display/JIRADEV/Velocity+Context+for+Email+Templates

        final TemplateContext templateContext = templateContextFactory.getTemplateContext(locale, iEvent);
        contextParams.putAll(templateContext.getTemplateParams());

        return contextParams;
    }

    /**
     * Used in testing only
     *
     * @return recipientList   the set of recipients to recieve this email notification
     */
    public Set<NotificationRecipient> getRecipientList()
    {
        return recipientList;
    }
}
