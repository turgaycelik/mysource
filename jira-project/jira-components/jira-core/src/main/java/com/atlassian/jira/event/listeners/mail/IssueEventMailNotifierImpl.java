package com.atlassian.jira.event.listeners.mail;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.mail.IssueMailQueueItemFactory;
import com.atlassian.jira.notification.JiraNotificationReason;
import com.atlassian.jira.notification.NotificationFilterContext;
import com.atlassian.jira.notification.NotificationFilterManager;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.type.NotificationType;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;

import com.google.common.collect.Sets;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

public class IssueEventMailNotifierImpl implements IssueEventMailNotifier
{
    private static final Logger log = Logger.getLogger(IssueEventMailNotifierImpl.class);

    private final NotificationFilterManager notificationFilterManager;
    private final NotificationSchemeManager notificationSchemeManager;
    private final TemplateManager templateManager;
    private final IssueMailQueueItemFactory issueMailQueueItemFactory;
    private final MailQueue mailQueue;

    public IssueEventMailNotifierImpl(NotificationFilterManager notificationFilterManager,
            NotificationSchemeManager notificationSchemeManager,
            TemplateManager templateManager,
            IssueMailQueueItemFactory issueMailQueueItemFactory,
            MailQueue mailQueue)
    {
        this.notificationFilterManager = notificationFilterManager;
        this.notificationSchemeManager = notificationSchemeManager;
        this.templateManager = templateManager;
        this.issueMailQueueItemFactory = issueMailQueueItemFactory;
        this.mailQueue = mailQueue;
    }

    @Override
    @Nonnull
    public Set<NotificationRecipient> generateNotifications(@Nonnull List<SchemeEntity> schemeEntities, @Nonnull IssueEvent issueEvent, @Nonnull Set<NotificationRecipient> initialRecipientsToSkip)
    {
        Set<NotificationRecipient> notifiedRecipients = Sets.newHashSet();
        Set<NotificationRecipient> recipientsToSkip = Sets.newHashSet(initialRecipientsToSkip);
        NotificationFilterContext context = notificationFilterManager.makeContextFrom(JiraNotificationReason.ISSUE_EVENT, issueEvent);
        for (SchemeEntity schemeEntity : schemeEntities)
        {
            context = notificationFilterManager.makeContextFrom(context, NotificationType.from(schemeEntity.getType()));
            Set<NotificationRecipient> recipientsNotifiedForSchemeEntity = createMailItemsForSchemeEntity(schemeEntity, issueEvent, recipientsToSkip, context);

            notifiedRecipients.addAll(recipientsNotifiedForSchemeEntity);
            recipientsToSkip.addAll(recipientsNotifiedForSchemeEntity);
        }
        return notifiedRecipients;
    }

    private Set<NotificationRecipient> createMailItemsForSchemeEntity(
            @Nonnull SchemeEntity schemeEntity,
            @Nonnull IssueEvent issueEvent,
            @Nonnull Set<NotificationRecipient> recipientsToSkip,
            @Nonnull NotificationFilterContext context)
    {
        try
        {
            Set<NotificationRecipient> recipients = getRecipientsToNotify(schemeEntity, issueEvent, context, recipientsToSkip);
            if (!recipients.isEmpty())
            {
                createAndEnqueue(schemeEntity, issueEvent, recipients);
                return recipients;
            }
        }
        catch (GenericEntityException e)
        {
            log.error("There was an error accessing the notification scheme for the project: " + issueEvent.getProject().getKey() + ".", e);
        }
        return Collections.emptySet();
    }

    private Set<NotificationRecipient> getRecipientsToNotify(
            @Nonnull SchemeEntity schemeEntity,
            @Nonnull IssueEvent issueEvent,
            @Nonnull NotificationFilterContext context,
            final Set<NotificationRecipient> recipientsToSkip) throws GenericEntityException
    {
        Set<NotificationRecipient> recipients = notificationSchemeManager.getRecipients(issueEvent, schemeEntity);
        // call out to plugins to filter out any users that should added or not get notifications right now
        recipients = Sets.newHashSet(notificationFilterManager.recomputeRecipients(recipients, context));
        recipients.removeAll(recipientsToSkip);
        return recipients;
    }

    private void createAndEnqueue(@Nonnull SchemeEntity schemeEntity, @Nonnull IssueEvent issueEvent, Set<NotificationRecipient> recipients)
    {
        long templateId = templateManager.getTemplate(schemeEntity).getId();
        MailQueueItem item = issueMailQueueItemFactory.getIssueMailQueueItem(issueEvent, templateId, recipients, schemeEntity.getType());
        mailQueue.addItem(item);
    }
}
