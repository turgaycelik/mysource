/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.exception;

import com.atlassian.jira.JiraException;

///CLOVER:OFF
public class AddException extends JiraException
{
    public AddException()
    {
    }

    public AddException(String msg)
    {
        super(msg);
    }

    public AddException(Exception e)
    {
        super(e);
    }

    public AddException(String msg, Exception e)
    {
        super(msg, e);
    }
}
