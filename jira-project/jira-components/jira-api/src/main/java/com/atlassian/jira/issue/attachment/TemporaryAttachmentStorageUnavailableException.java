package com.atlassian.jira.issue.attachment;

import com.atlassian.fugue.Option;
import com.atlassian.jira.util.I18nHelper;

/**
 * Represents a case where the storage subsystem for temporary attachments (e.g. temporarily file system) is unavailable.
 *
 * @since v6.3
 */
public class TemporaryAttachmentStorageUnavailableException extends AttachmentRuntimeException
{
    public TemporaryAttachmentStorageUnavailableException(final String message)
    {
        super(message);
    }

    public TemporaryAttachmentStorageUnavailableException(final Throwable cause)
    {
        super(cause);
    }

    public TemporaryAttachmentStorageUnavailableException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    @Override
    protected Option<String> doGenerateMessage(final I18nHelper localisedMessages)
    {
        return Option.some(localisedMessages.getText("attachfile.error.temp.writeerror", this.getMessage()));
    }
}
