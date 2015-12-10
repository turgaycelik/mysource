package com.atlassian.jira.config.component;

import com.atlassian.jira.config.properties.ApplicationProperties;

import org.picocontainer.PicoContainer;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class AppPropertiesComponentAdaptor<T> extends AbstractSwitchingInvocationAdaptor<T>
{
    private final String appPropertiesKey;
    private InvocationSwitcher invocationSwitcher;
    private PicoContainer container;

    public AppPropertiesComponentAdaptor(final PicoContainer container, final Class<T> interfaceClass,
            final Class<? extends T> enabledClass, final Class<? extends T> disabledClass, final String appPropertiesKey)
    {
        super(interfaceClass, enabledClass, disabledClass);
        this.appPropertiesKey = appPropertiesKey;
        this.container = container;
    }

    protected InvocationSwitcher getInvocationSwitcher()
    {
        // Lazy load the switcher so that ApplicationProperties are also lazy loaded.
        if (invocationSwitcher == null)
        {
            invocationSwitcher = new AppPropertiesInvocationSwitcherImpl(getProperties(), appPropertiesKey);
        }

        return invocationSwitcher;
    }

    private ApplicationProperties getProperties()
    {
        // Lazy load the application properties
        return container.getComponent(ApplicationProperties.class);
    }
}
