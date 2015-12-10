package com.atlassian.jira.external;


public class ExternalRuntimeException extends RuntimeException
{
    public ExternalRuntimeException()
    {
    }

    public ExternalRuntimeException(String s)
    {
        super(s);
    }

    public ExternalRuntimeException(Throwable throwable)
    {
        super(throwable);
    }

    public ExternalRuntimeException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}
