package com.atlassian.jira.bulkedit.operation;

public class BulkOperationException extends Exception
{
    public BulkOperationException(final String message)
    {
        super(message);
    }

    public BulkOperationException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public BulkOperationException(final Throwable cause)
    {
        super(cause);
    }
}
