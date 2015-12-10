package com.atlassian.jira.web.util;

/**
 * Exception when attachment exceeds setting limit size
 */
public class AttachmentExceedsLimitException extends AttachmentException
{
    public AttachmentExceedsLimitException(String message)
    {
        super(message);
    }

}
