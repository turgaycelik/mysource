package com.atlassian.jira.util;

import com.atlassian.jira.component.ComponentAccessor;

import javax.annotation.Nonnull;

public class JiraComponentLocator implements ComponentLocator
{
    public <T> T getComponentInstanceOfType(final Class<T> type)
    {
        return ComponentAccessor.getComponentOfType(type);
    }

    public <T> T getComponent(final Class<T> type)
    {
        return getComponentInstanceOfType(type);
    }

    @Nonnull
    @Override
    public <T> com.google.common.base.Supplier<T> getComponentSupplier(final Class<T> type)
    {
        return new com.google.common.base.Supplier<T>()
        {
            @Override
            public T get()
            {
                return getComponent(type);
            }
        };
    }

    @Override
    public String toString()
    {
        return "Default Component locator";
    }
}
