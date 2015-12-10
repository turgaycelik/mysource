package com.atlassian.jira.util;


/**
 * Thrown when a timeout has been reached while trying to obtain a lock
 */
public class LockException extends Exception
{
    public LockException(String string)
    {
        super(string);
    }

    public LockException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
