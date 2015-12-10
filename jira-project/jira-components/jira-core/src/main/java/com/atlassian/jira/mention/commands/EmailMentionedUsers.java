package com.atlassian.jira.mention.commands;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.MentionIssueEvent;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.mail.MentionMailQueueItem;
import com.atlassian.jira.mail.TemplateContextFactory;
import com.atlassian.jira.notification.JiraNotificationReason;
import com.atlassian.jira.notification.NotificationFilterManager;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.mail.server.MailServerManager;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Responsible for sending out a notification email to the users mentioned in an issue.
 *
 * @since v5.0
 */
@EventComponent
public class EmailMentionedUsers
{
    private Logger log = Logger.getLogger(EmailMentionedUsers.class);

    private final MailQueue mailQueue;
    private final RendererManager rendererManager;
    private final MailServerManager mailServerManager;
    private final NotificationFilterManager notificationFilterManager;

    public EmailMentionedUsers(final MailQueue mailQueue, RendererManager rendererManager, final MailServerManager mailServerManager, final NotificationFilterManager notificationFilterManager)
    {
        this.mailQueue = mailQueue;
        this.rendererManager = rendererManager;
        this.mailServerManager = mailServerManager;
        this.notificationFilterManager = notificationFilterManager;
    }

    @EventListener
    public void execute(final MentionIssueEvent mentionIssueEvent)
    {
        if (mailServerManager.isDefaultSMTPMailServerDefined())
        {
            final User from = mentionIssueEvent.getFromUser();

            for (User toUser : mentionIssueEvent.getToUsers())
            {
                if (toUser.getEmailAddress() == null)
                {
                    log.warn("User " + toUser.getName() + " does not have a registered email address. No mentioned notification will be sent.");
                    continue;
                }

                final NotificationRecipient recipient = new NotificationRecipient(toUser);

                if (shouldSendMention(mentionIssueEvent, recipient))
                {
                    Map<String, Object> params = Maps.newHashMap();
                    params.put("comment", mentionIssueEvent.getMentionText());
                    params.put("issue", mentionIssueEvent.getIssue());

                    IssueRenderContext issueRenderContext = mentionIssueEvent.getIssue().getIssueRenderContext();

                    MailQueueItem item = new MentionMailQueueItem(from, recipient, params, issueRenderContext,
                            rendererManager, mailQueue);
                    mailQueue.addItem(item);
                }
            }
        }
    }

    private boolean shouldSendMention(MentionIssueEvent mentionIssueEvent, NotificationRecipient recipient)
    {
        return userIsNotAlreadyReceivingAnEmail(mentionIssueEvent, recipient) && userIsNotFiltered(recipient);
    }

    private boolean userIsNotAlreadyReceivingAnEmail(MentionIssueEvent mentionIssueEvent, NotificationRecipient recipient)
    {
        return !mentionIssueEvent.getCurrentRecipients().contains(recipient);
    }

    private boolean userIsNotFiltered(NotificationRecipient recipient)
    {
        return !notificationFilterManager.filtered(recipient, notificationFilterManager.makeContextFrom(JiraNotificationReason.MENTIONED));
    }
}
