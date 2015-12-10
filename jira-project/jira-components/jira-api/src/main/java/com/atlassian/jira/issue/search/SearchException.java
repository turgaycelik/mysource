/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.JiraException;

///CLOVER:OFF
@PublicApi
public class SearchException extends JiraException
{
    public SearchException()
    {
    }

    public SearchException(String msg)
    {
        super(msg);
    }

    public SearchException(Exception e)
    {
        super(e);
    }

    public SearchException(String msg, Exception e)
    {
        super(msg, e);
    }
}
