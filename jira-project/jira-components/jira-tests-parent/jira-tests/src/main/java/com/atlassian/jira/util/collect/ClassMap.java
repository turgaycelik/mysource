package com.atlassian.jira.util.collect;

import java.util.Collection;
import java.util.Map;

public interface ClassMap
{
    <T> T get(Class<T> klass);

    <T> void put(Class<T> klass, T instance);

    <T> void put(T instance);

    Collection<? extends Class<?>> keySet();

    Collection<?> values();

    public class Factory
    {
        public static ClassMap create(final Map<Class<?>, Object> delegate)
        {
            return new Impl(delegate);
        }
    }

    class Impl implements ClassMap
    {
        private final Map<Class<?>, Object> delegate;

        Impl(final Map<Class<?>, Object> delegate)
        {
            this.delegate = delegate;
        }

        public <T> void put(final T instance)
        {
            @SuppressWarnings("unchecked")
            final Class<T> klass = Class.class.cast(instance.getClass());
            put(klass, instance);
        }

        public <T> void put(final Class<T> klass, final T instance)
        {
            delegate.put(klass, instance);
        }

        public Collection<? extends Class<?>> keySet()
        {
            return delegate.keySet();
        }

        public Collection<? extends Object> values()
        {
            return delegate.values();
        }

        public <T> T get(final Class<T> klass)
        {
            return klass.cast(delegate.get(klass));
        }
    }
}
