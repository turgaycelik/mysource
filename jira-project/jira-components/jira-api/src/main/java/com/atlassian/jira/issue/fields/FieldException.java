/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;


public class FieldException extends Exception
{
    public FieldException()
    {
    }

    public FieldException(String string)
    {
        super(string);
    }

    public FieldException(Throwable throwable)
    {
        super(throwable);
    }

    public FieldException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
