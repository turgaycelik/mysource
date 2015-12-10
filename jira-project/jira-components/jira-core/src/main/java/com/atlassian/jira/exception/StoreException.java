/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.exception;

import com.atlassian.jira.JiraException;

///CLOVER:OFF
/**
 * @deprecated Please use {@link DataAccessException} instead.
 */ 
public class StoreException extends JiraException
{
    public StoreException()
    {
    }

    public StoreException(String msg)
    {
        super(msg);
    }

    public StoreException(Exception e)
    {
        super(e);
    }

    public StoreException(String msg, Exception e)
    {
        super(msg, e);
    }
}
