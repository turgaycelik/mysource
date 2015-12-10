package com.atlassian.jira.exception;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.JiraException;

/**
 * An exception that indicates errors encountered in when performing get related tasks in managers.
 *
 * @since v6.1
 */
@PublicApi
public class GetException extends JiraException
{
    public GetException()
    {
    }

    public GetException(final String s)
    {
        super(s);
    }

    public GetException(final Throwable throwable)
    {
        super(throwable);
    }

    public GetException(final String s, final Throwable throwable)
    {
        super(s, throwable);
    }
}
