/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow;

public class WorkflowException extends RuntimeException
{
    public WorkflowException(String string)
    {
        super(string);
    }

    public WorkflowException(Throwable throwable)
    {
        super(throwable);
    }

    public WorkflowException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
