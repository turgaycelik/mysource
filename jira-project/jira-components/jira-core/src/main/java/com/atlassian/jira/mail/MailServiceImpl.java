package com.atlassian.jira.mail;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;

/**
 * Default implementation of {@link MailService}.
 *
 * @since v5.0
 */
public class MailServiceImpl implements MailService
{
    private final MailQueue mailQueue;

    public MailServiceImpl(MailQueue mailQueue)
    {
        this.mailQueue = mailQueue;
    }

    @Override
    public void sendRenderedMail(User replyTo, NotificationRecipient recipient, String subjectTemplatePath,
            String bodyTemplatePath, Map<String, Object> context)
    {
        final MailQueueItem item = new MailServiceQueueItemBuilder(replyTo, recipient, subjectTemplatePath,
                bodyTemplatePath, context).buildQueueItem();
        mailQueue.addItem(item);
    }
}
