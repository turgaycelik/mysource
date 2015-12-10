/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config.component;

/**
 * The InvocationSwitcher is used in conjunction with the {@link SwitchingInvocationHandler}.
 * An InvocationSwitcher implementation provides the logic to determine which class implementation (registered with the
 * SwitchingInocationHandler) to invoke the specified method on.
 * */
public interface InvocationSwitcher
{
    boolean isEnabled();
}
