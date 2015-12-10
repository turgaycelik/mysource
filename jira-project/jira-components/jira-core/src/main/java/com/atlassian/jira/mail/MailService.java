package com.atlassian.jira.mail;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.notification.NotificationRecipient;

import java.util.Map;

/**
 * Provides mail-sending services.
 *
 * @since v5.0
 */
public interface MailService
{

    /**
     * Sends a Velocity-rendered email by adding the template and context to a
     * {@link com.atlassian.mail.queue.MailQueueItem} and adding that item to the queue. The final email will be
     * rendered as the {@link com.atlassian.mail.queue.MailQueue} is processed.
     *
     * @param replyTo  the user sending the email
     * @param recipient the recipient of the email
     * @param subjectTemplatePath the relative path to the Velocity template with the email's subject line
     * @param bodyTemplatePath the relative path to the Velocity template with the email's body
     * @param context the context map that will be used to render the templates
     */
    void sendRenderedMail(final User replyTo, NotificationRecipient recipient, String subjectTemplatePath,
            String bodyTemplatePath, Map<String, Object> context);
}
