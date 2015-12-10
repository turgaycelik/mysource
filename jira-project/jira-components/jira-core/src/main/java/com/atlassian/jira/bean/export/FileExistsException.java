/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bean.export;

public class FileExistsException extends Exception
{
    public FileExistsException()
    {
    }

    public FileExistsException(String message)
    {
        super(message);
    }
}
