package com.atlassian.jira.mail;

import com.atlassian.jira.notification.NotificationRecipient;

import java.util.Collection;

/**
 * This class takes a collection of {@link com.atlassian.jira.notification.NotificationRecipient} objects and
 * processes them by executing the {@link #processRecipient(com.atlassian.jira.notification.NotificationRecipient)} method.
 * <p/>
 * The design of this class is to process all recipients even if the processing causes any exceptions. If an exception
 * occurs, {@link #handleException(com.atlassian.jira.notification.NotificationRecipient, Exception)} method is invoked,
 * and processing continues.
 *
 * @since v3.12.3
 */
abstract class NotificationRecipientProcessor
{
    private final Collection<NotificationRecipient> recipients;

    NotificationRecipientProcessor(final Collection<NotificationRecipient> recipients)
    {
        this.recipients = recipients;
    }

    /**
     * Iterates through all recipients in the collection and performs
     * the {@link #processRecipient(com.atlassian.jira.notification.NotificationRecipient)} method on each recipient.
     * <p/>
     * If the {@link # processRecipient (com.atlassian.jira.notification.NotificationRecipient)} method throws an exception,
     * {@link #handleException(com.atlassian.jira.notification.NotificationRecipient, Exception)} is called.
     */
    public void process()
    {
        for (NotificationRecipient recipient : recipients)
        {
            try
            {
                processRecipient(recipient);
            }
            catch (Exception ex) // yes, we want to catch RuntimeException as well
            {
                handleException(recipient, ex);
            }
        }
    }

    /**
     * This method is called from the {@link #process()} method, which catches all exceptions
     * this method could throw.
     *
     * @param recipient notification recipient
     * @throws Exception if an error occurs
     */
    abstract void processRecipient(final NotificationRecipient recipient) throws Exception;

    /**
     * This method logs the exception caused in the execution of
     * the {@link #processRecipient(com.atlassian.jira.notification.NotificationRecipient)} method.
     *
     * @param recipient recipient that caused the exception
     * @param ex        exception
     */
    abstract void handleException(final NotificationRecipient recipient, Exception ex);
}
