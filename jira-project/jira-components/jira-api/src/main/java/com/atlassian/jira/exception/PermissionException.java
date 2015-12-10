/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.exception;

import com.atlassian.jira.JiraException;

/**
 * Exception that is thrown when a permission is violated (usually in a Manager)
 *
 * ///CLOVER:OFF
 */
public class PermissionException extends JiraException
{
    public PermissionException()
    {
    }

    public PermissionException(String msg)
    {
        super(msg);
    }

    public PermissionException(Exception e)
    {
        super(e);
    }

    public PermissionException(String msg, Exception e)
    {
        super(msg, e);
    }
}
