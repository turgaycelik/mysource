/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;


public class ColumnLayoutException extends Exception
{
    public ColumnLayoutException()
    {
    }

    public ColumnLayoutException(String string)
    {
        super(string);
    }

    public ColumnLayoutException(Throwable throwable)
    {
        super(throwable);
    }

    public ColumnLayoutException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
