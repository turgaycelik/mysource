package com.atlassian.jira.issue.attachment;

/**
 * Represents a situation where attachments could not be cleaned up.
 *
 * @since v6.3
 */
public class AttachmentCleanupException extends AttachmentRuntimeException
{
    public AttachmentCleanupException(final String message)
    {
        super(message);
    }

    public AttachmentCleanupException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public AttachmentCleanupException(final Throwable cause)
    {
        super(cause);
    }
}
