/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.JiraException;

///CLOVER:OFF
@PublicApi
public class IndexException extends JiraException
{
    public IndexException()
    {
    }

    public IndexException(String msg)
    {
        super(msg);
    }

    public IndexException(Exception e)
    {
        super(e);
    }

    public IndexException(String msg, Exception e)
    {
        super(msg, e);
    }
}
