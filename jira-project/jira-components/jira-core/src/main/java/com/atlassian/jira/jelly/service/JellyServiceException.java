package com.atlassian.jira.jelly.service;

import org.apache.commons.jelly.JellyException;

/**
 * Represents a problem with Jelly script loading, parsing or execution.
 */
public class JellyServiceException extends JellyException
{
    public JellyServiceException()
    {
        super();
    }

    public JellyServiceException(Throwable cause)
    {
        super(cause);
    }

    public JellyServiceException(String message)
    {
        super(message);
    }

    public JellyServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
