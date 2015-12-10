package com.atlassian.jira.web.servlet;

/**
 * @since v3.13.5
 */
public class InvalidAttachmentPathException extends RuntimeException
{
    public InvalidAttachmentPathException()
    {
        super("Invalid attachment path");
    }
}
