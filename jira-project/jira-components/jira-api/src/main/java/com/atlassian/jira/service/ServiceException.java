/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.JiraException;

///CLOVER:OFF
@PublicApi
public class ServiceException extends JiraException
{
    public ServiceException()
    {
        super();
    }

    public ServiceException(Exception e)
    {
        super(e);
    }

    public ServiceException(String msg)
    {
        super(msg);
    }

    public ServiceException(String msg, Exception e)
    {
        super(msg, e);
    }
}
