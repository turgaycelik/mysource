package com.atlassian.jira.issue.attachment;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.fugue.Option;
import com.atlassian.jira.util.I18nHelper;

/**
 * Base class for attachment exceptions that may come from asynchronous operations.
 *
 * @since v6.3
 */
@ExperimentalApi
public class AttachmentRuntimeException extends RuntimeException
{
    /**
     * Generate a localised message for this exception.
     * @param localisedMessages Internationalisation helper that can provide localised messages for given codes.
     * @return a localised message for this exception
     */
    public final String generateMessage(final I18nHelper localisedMessages)
    {
        Option<String> generatedMessage = doGenerateMessage(localisedMessages);
        return generatedMessage.getOrElse(this.getMessage());
    }

    /**
     * Generate a localised message for this exception.
     * @param localisedMessages Internationalisation helper that can provide localised messages for given codes.
     * @return a localised message for this exception. Option.none() to use the default (i.e. getMessage()).
     */
    protected Option<String> doGenerateMessage(final I18nHelper localisedMessages)
    {
        return Option.none();
    }

    public AttachmentRuntimeException(final String message)
    {
        super(message);
    }

    public AttachmentRuntimeException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public AttachmentRuntimeException(final Throwable cause)
    {
        super(cause);
    }
}
