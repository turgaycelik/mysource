/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.exception;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.JiraException;

///CLOVER:OFF
@PublicApi
public class CreateException extends JiraException
{
    public CreateException()
    {
    }

    public CreateException(String msg)
    {
        super(msg);
    }

    public CreateException(Exception e)
    {
        super(e);
    }

    public CreateException(String msg, Exception e)
    {
        super(msg, e);
    }
}
