package com.atlassian.jira.issue.attachment;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Represents a case where there was an error moving the attachment.
 *
 * @since v6.3
 */
@ExperimentalApi
public class AttachmentMoveException extends AttachmentRuntimeException
{
    public AttachmentMoveException(final String message)
    {
        super(message);
    }

    public AttachmentMoveException(final Throwable cause)
    {
        super(cause);
    }

    public AttachmentMoveException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
