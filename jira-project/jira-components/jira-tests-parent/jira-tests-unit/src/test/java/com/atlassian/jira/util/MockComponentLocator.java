package com.atlassian.jira.util;

import com.google.common.collect.Maps;

import java.util.Map;
import javax.annotation.Nonnull;

/**
 * @since v4.3
 */
public class MockComponentLocator implements ComponentLocator
{
    private final Map<Class<?>, Object> components = Maps.newHashMap();

    public <T> T getComponentInstanceOfType(final Class<T> type)
    {
        return type.cast(components.get(type));
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

    public <T> MockComponentLocator register(Class<T> clazz, T instance)
    {
        components.put(clazz, instance);
        return this;
    }
}
