package com.atlassian.jira.exception;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.JiraException;

/**
 * Thrown when there is a serious error with an update.
 *
 * @since v4.1
 */
@PublicApi
public class UpdateException extends JiraException
{
    public UpdateException()
    {
    }

    public UpdateException(String msg)
    {
        super(msg);
    }

    public UpdateException(Exception e)
    {
        super(e);
    }

    public UpdateException(String msg, Exception e)
    {
        super(msg, e);
    }
}
