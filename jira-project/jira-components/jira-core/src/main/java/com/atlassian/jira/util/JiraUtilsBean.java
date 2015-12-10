/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util;

/**
 * A bean version of JiraUtils that can be mocked out out
 * @see com.atlassian.jira.util.JiraUtils
 */
public class JiraUtilsBean
{
    public boolean isPublicMode()
    {
        return JiraUtils.isPublicMode();
    }

    public Object loadComponent(String className, Class callingClass) throws ClassNotFoundException
    {
        return JiraUtils.loadComponent(className, callingClass);
    }

    public Object loadComponent(Class componentClass)
    {
        return JiraUtils.loadComponent(componentClass);
    }
}
