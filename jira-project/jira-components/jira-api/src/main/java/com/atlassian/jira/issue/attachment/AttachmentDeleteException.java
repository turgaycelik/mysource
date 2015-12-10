package com.atlassian.jira.issue.attachment;

import com.atlassian.annotations.ExperimentalApi;

/**
 * Represents a case where there was an deleting reading the attachment (e.g. some I/O error reading from disk or from the
 * remote attachment store).
 *
 * @since v6.3
 */
@ExperimentalApi
public class AttachmentDeleteException extends AttachmentRuntimeException
{
    public AttachmentDeleteException(final String message)
    {
        super(message);
    }

    public AttachmentDeleteException(final Throwable cause)
    {
        super(cause);
    }

    public AttachmentDeleteException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
