package com.atlassian.jira.external;

import com.atlassian.jira.JiraException;

public class ExternalException extends JiraException
{
    public ExternalException()
    {
    }

    public ExternalException(String s)
    {
        super(s);
    }

    public ExternalException(Throwable throwable)
    {
        super(throwable);
    }

    public ExternalException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}
