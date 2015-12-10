package com.atlassian.jira.issue.attachment;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Represents a case where there is an Attachment record in the database, but no underlying data. This is an inconsistency
 * in the attachment storage subsystem.
 *
 * @since v6.3
 */
@ExperimentalApi
public class NoAttachmentDataException extends AttachmentRuntimeException
{
    public NoAttachmentDataException(final String message)
    {
        super(message);
    }

    public NoAttachmentDataException(final Throwable cause)
    {
        super(cause);
    }

    public NoAttachmentDataException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
