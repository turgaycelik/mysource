/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;


public class PortletConfigurationException extends Exception
{
    public PortletConfigurationException()
    {
    }

    public PortletConfigurationException(String s)
    {
        super(s);
    }

    public PortletConfigurationException(Throwable throwable)
    {
        super(throwable);
    }

    public PortletConfigurationException(String s, Throwable throwable)
    {
        super(s, throwable);
    }
}
