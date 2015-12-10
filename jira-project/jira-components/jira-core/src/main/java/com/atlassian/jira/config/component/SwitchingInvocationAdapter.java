/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config.component;

import com.atlassian.jira.config.component.AbstractSwitchingInvocationAdaptor;
import com.atlassian.jira.config.component.InvocationSwitcher;

/**
 * The SwitchingInvocationAdapter returns a proxy that allows for dynamic determination of
 * which class implementation to be called on when invoking a specified method.
 */
public class SwitchingInvocationAdapter<T> extends AbstractSwitchingInvocationAdaptor<T>
{
    private final InvocationSwitcher invocationSwitcher;

    public SwitchingInvocationAdapter(final Class<T> interfaceClass, final Class<? extends T> enabledClass,
            final Class<? extends T> disabledClass, final InvocationSwitcher invocationSwitcher)
    {
        super(interfaceClass, enabledClass, disabledClass);
        this.invocationSwitcher = invocationSwitcher;
    }

    protected InvocationSwitcher getInvocationSwitcher()
    {
        return invocationSwitcher;
    }
}
