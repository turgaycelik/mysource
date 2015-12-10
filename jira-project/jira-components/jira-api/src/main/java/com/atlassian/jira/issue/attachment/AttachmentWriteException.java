package com.atlassian.jira.issue.attachment;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.fugue.Option;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.AttachmentException;

/**
 * Represents a case where writing attachment data has failed (e.g. there was a failure to write to the file system,
 * or there was an error sending the attachment to the remote attachment store).
 *
 * @since v6.3
 */
@ExperimentalApi
public class AttachmentWriteException extends AttachmentRuntimeException
{
    @Override
    protected Option<String> doGenerateMessage(final I18nHelper localisedMessages)
    {
        return Option.some(localisedMessages.getText("attachfile.error.save.to.store", this.getMessage()));
    }

    public AttachmentWriteException(final String message)
    {
        super(message);
    }

    public AttachmentWriteException(final Throwable cause)
    {
        super(cause);
    }

    public AttachmentWriteException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
