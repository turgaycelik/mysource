package com.atlassian.jira.exception;


/**
 * The expected resource was not found. Corresponds to a 404 HTTP
 * response.
 */
public class NotFoundException extends RuntimeException
{
    public NotFoundException()
    {
    }

    public NotFoundException(String string)
    {
        super(string);
    }

    public NotFoundException(Throwable throwable)
    {
        super(throwable);
    }

    public NotFoundException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
