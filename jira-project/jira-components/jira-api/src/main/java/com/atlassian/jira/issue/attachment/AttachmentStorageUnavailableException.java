package com.atlassian.jira.issue.attachment;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.fugue.Option;
import com.atlassian.jira.util.I18nHelper;

/**
 * Represents a case where the attachment storage subsystem is unavailable e.g. file system directories are not present.
 *
 * @since v6.3
 */
@ExperimentalApi
public class AttachmentStorageUnavailableException extends AttachmentRuntimeException
{
    public AttachmentStorageUnavailableException(final String message)
    {
        super(message);
    }

    public AttachmentStorageUnavailableException(final Throwable cause)
    {
        super(cause);
    }

    public AttachmentStorageUnavailableException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    @Override
    protected Option<String> doGenerateMessage(final I18nHelper localisedMessages)
    {
        return Option.some(localisedMessages.getText("attachfile.error.writeerror", this.getMessage()));
    }
}
