/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock.event;

import com.atlassian.jira.event.JiraListener;

import java.util.Map;

public class MockListener implements JiraListener
{
    private Map params;

    public void init(Map params)
    {
        this.params = params;
    }

    public String[] getAcceptedParams()
    {
        return new String[0];
    }

    public boolean isInternal()
    {
        return false;
    }

    public boolean isUnique()
    {
        return false;
    }

    public String getDescription()
    {
        return "A mock listener";
    }

    public Object getParam(String s)
    {
        return params.get(s);
    }
}
