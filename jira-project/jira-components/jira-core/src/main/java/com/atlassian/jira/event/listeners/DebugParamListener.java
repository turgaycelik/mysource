/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.listeners;

public class DebugParamListener extends DebugListener
{
    public String[] getAcceptedParams()
    {
        return new String[] { "Param1", "Param2" };
    }
}
