/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.config.component;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * A simple handler that will throw an {@link UnsupportedOperationException} for all method calls.
 */
public class UnsupportedClassInvocationHandler implements InvocationHandler
{
    private static final Logger log = Logger.getLogger(UnsupportedClassInvocationHandler.class);

    private final String interfaceName;

    public UnsupportedClassInvocationHandler(String enterpriseClassName)
    {
        this.interfaceName = enterpriseClassName;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        log.error("Could not find implementation for interface " + interfaceName + ".  Please check license version and JIRA version for inconsistencies.");
        throw new UnsupportedOperationException("There is no implementation of " + interfaceName + " in this edition of JIRA.  This should not normally occur.  Please file a bug report");
    }
}
