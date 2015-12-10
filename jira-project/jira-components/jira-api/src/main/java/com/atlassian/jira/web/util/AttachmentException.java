package com.atlassian.jira.web.util;

/**
 * Represtents any File System errors that occur while trying to create an Issue Attachment.
 */
public class AttachmentException extends Exception
{
    public AttachmentException(String message)
    {
        super(message);
    }

    public AttachmentException(Throwable cause)
    {
        super(cause);
    }

    public AttachmentException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
