package com.atlassian.jira.crowd.embedded.ofbiz;

/**
 * Non stack trace filling exception used for flow control.
 *
 * @since v6.3
 */
public class GroupNotFoundException extends com.atlassian.crowd.exception.GroupNotFoundException
{
    public GroupNotFoundException(final String groupName)
    {
        super(groupName);
    }

    public GroupNotFoundException(final String groupName, final Throwable t)
    {
        super(groupName, t);
    }

    @Override
    public synchronized Throwable fillInStackTrace()
    {
        return this;
    }
}
