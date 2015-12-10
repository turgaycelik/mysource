package com.atlassian.jira.web.util;

import com.atlassian.jira.issue.attachment.AttachmentRuntimeException;
import com.atlassian.jira.util.I18nHelper;

/**
 * Generates a suitable message string for a given AttachmentException. In most cases the cause should be a subclass of
 * AttachmentRuntimeException. The exception's message is returned as a fallback.
 *
 * @since v6.3
 */
public class AttachmentExceptionMessages
{
    public static String get(AttachmentException e, I18nHelper localisedMessages)
    {
        Throwable cause = e.getCause();
        if (cause != null && cause instanceof AttachmentRuntimeException)
        {
            AttachmentRuntimeException attachmentExceptionCause = (AttachmentRuntimeException)cause;
            return attachmentExceptionCause.generateMessage(localisedMessages);
        }

        return e.getMessage();
    }
}
