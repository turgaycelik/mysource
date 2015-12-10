/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

/**
 * @deprecated Not used any more. Since v5.0.
 */
public class FieldLayoutStorageException extends Exception
{
    public FieldLayoutStorageException()
    {
    }

    public FieldLayoutStorageException(String string)
    {
        super(string);
    }

    public FieldLayoutStorageException(Throwable throwable)
    {
        super(throwable);
    }

    public FieldLayoutStorageException(String string, Throwable throwable)
    {
        super(string, throwable);
    }
}
