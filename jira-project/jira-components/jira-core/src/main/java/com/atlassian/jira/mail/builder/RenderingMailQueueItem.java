package com.atlassian.jira.mail.builder;

import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.SingleMailQueueItem;

import javax.mail.MessagingException;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0.8
 */
public class RenderingMailQueueItem extends SingleMailQueueItem
{
    private final EmailRenderer emailRenderer;
    public RenderingMailQueueItem(EmailRenderer emailRenderer)
    {
        super(emailRenderer.getEmail());
        this.emailRenderer = emailRenderer;
    }

    @Override
    public String getSubject()
    {
        return emailRenderer.getSubject();
    }

    @Override
    public void send() throws MailException
    {
        try
        {
            emailRenderer.render();
        }
        catch (MessagingException e)
        {
            throw new MailException(e);
        }
        super.send();
    }
}
