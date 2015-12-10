/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import com.atlassian.jira.JiraException;

public class IssueFieldUpdateException extends JiraException
{
    public IssueFieldUpdateException(Throwable throwable)
    {
        super(throwable);
    }

    public IssueFieldUpdateException()
    {
    }

    public IssueFieldUpdateException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public IssueFieldUpdateException(String s)
    {
        super(s);
    }
}
