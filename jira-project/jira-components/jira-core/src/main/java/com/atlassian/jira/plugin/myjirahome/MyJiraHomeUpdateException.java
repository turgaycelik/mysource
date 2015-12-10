package com.atlassian.jira.plugin.myjirahome;

/**
 * @since 5.1
 */
public class MyJiraHomeUpdateException extends RuntimeException
{
    public MyJiraHomeUpdateException()
    {
    }

    public MyJiraHomeUpdateException(String s)
    {
        super(s);
    }

    public MyJiraHomeUpdateException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public MyJiraHomeUpdateException(Throwable throwable)
    {
        super(throwable);
    }
}
