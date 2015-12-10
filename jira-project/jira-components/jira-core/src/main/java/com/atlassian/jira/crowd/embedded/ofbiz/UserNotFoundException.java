package com.atlassian.jira.crowd.embedded.ofbiz;

/**
 * Non stack trace filling exception used for flow control.
 *
 * @since v6.3
 */
public class UserNotFoundException extends com.atlassian.crowd.exception.UserNotFoundException
{
    public UserNotFoundException(final String userName)
    {
        super(userName);
    }

    public UserNotFoundException(final String userName, final Throwable t)
    {
        super(userName, t);
    }

    @Override
    public synchronized Throwable fillInStackTrace()
    {
        return this;
    }
}
