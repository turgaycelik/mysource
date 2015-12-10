/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config.component;

import com.atlassian.util.profiling.object.ObjectProfiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class SwitchingInvocationHandler implements InvocationHandler
{
    private final Object enabled;
    private final Object disabled;
    private final InvocationSwitcher invocationSwitcher;

    protected SwitchingInvocationHandler(Object enabled, Object disabled, InvocationSwitcher invocationSwitcher)
    {
        this.enabled = enabled;
        this.disabled = disabled;
        this.invocationSwitcher = invocationSwitcher;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        return ObjectProfiler.profiledInvoke(method, getImplementingObject(), args);
    }

    /**
     * Cache the value of the object returned from the container
     */
    public Object getImplementingObject()
    {
        return isEnabled() ? enabled : disabled;
    }

    protected boolean isEnabled()
    {
        return invocationSwitcher.isEnabled();
    }
}
