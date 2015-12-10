/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.exception;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.JiraException;

///CLOVER:OFF
@PublicApi
public class RemoveException extends JiraException
{
    public RemoveException()
    {
    }

    public RemoveException(String msg)
    {
        super(msg);
    }

    public RemoveException(Exception e)
    {
        super(e);
    }

    public RemoveException(String msg, Exception e)
    {
        super(msg, e);
    }
}
