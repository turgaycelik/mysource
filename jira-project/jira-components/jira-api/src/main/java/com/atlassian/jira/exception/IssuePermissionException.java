/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.exception;


public class IssuePermissionException extends RuntimeException
{
    public IssuePermissionException()
    {
    }

    public IssuePermissionException(String string)
    {
        super(string);
    }

    public IssuePermissionException(Throwable throwable)
    {
        super(throwable);
    }

    public IssuePermissionException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
