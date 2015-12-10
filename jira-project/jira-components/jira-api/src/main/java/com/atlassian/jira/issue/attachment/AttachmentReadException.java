package com.atlassian.jira.issue.attachment;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Represents a case where there was an error reading the attachment (e.g. some I/O error reading from disk or from the
 * remote attachment store).
 *
 * @since v6.3
 */
@ExperimentalApi
public class AttachmentReadException extends AttachmentRuntimeException
{
    public AttachmentReadException(final String message)
    {
        super(message);
    }

    public AttachmentReadException(final Throwable cause)
    {
        super(cause);
    }

    public AttachmentReadException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
