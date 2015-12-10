/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.exception;

public class IssueNotFoundException extends NotFoundException
{
    public IssueNotFoundException(String string, Throwable throwable)
    {
        super(string, throwable);
    }

    public IssueNotFoundException()
    {
    }

    public IssueNotFoundException(String string)
    {
        super(string);
    }

    public IssueNotFoundException(Throwable throwable)
    {
        super(throwable);
    }
}
