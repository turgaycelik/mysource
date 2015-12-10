package com.atlassian.jira.util.map;

import java.io.Serializable;

/**
 * Used for keys or values in Maps that do not support null keys, or in caches
 * where it is needed to differentiate between a cached 'null' value and an
 * object not being in the cache.
 *
 * Although this class is marked as being Serializable, instances will only
 * truly be thus if the value they wrap is itself Serializable (or null).
 *
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class CacheObject<T> implements Serializable
{
    private final T value;

    /**
     * Returns a CacheObject&lt;T&gt; whose value is null.
     *
     * @return a CacheObject&lt;T&gt; whose value is null
     */
    public static <T> CacheObject<T> NULL()
    {
        //noinspection unchecked
        return NULL_INSTANCE;
    }

    /**
     * Wraps the given value in a CacheObject.
     *
     * @param object a &lt;T&gt;
     * @return a CacheObject that wraps the given value
     */
    public static <T> CacheObject<T> wrap(T object)
    {
        return object == null ? CacheObject.<T>NULL() : new CacheObject<T>(object);
    }

    /**
     * A CacheObject&lt;T&gt; whose value is null. Prefer {@link CacheObject#NULL()} to avoid generic-related warnings.
     */
    public static final CacheObject NULL_INSTANCE = new CacheObject<Object>(null);

    public CacheObject(final T value)
    {
        this.value = value;
    }

    public T getValue()
    {
        return value;
    }

    public boolean hasValue()
    {
        return value != null;
    }

    public boolean isNull()
    {
        return value == null;
    }

    // THis object can also be used as a key for maps that do not support null keys, for example ConcurrentHashMap
    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CacheObject))
        {
            return false;
        }

        final CacheObject<?> cacheObject = (CacheObject<?>) o;

        if (value != null ? !value.equals(cacheObject.value) : cacheObject.value != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return (value != null ? value.hashCode() : 0);
    }

    @Override
    public String toString()
    {
        return value != null ? "CacheObject{" + value + '}' : "CacheObject.NULL";
    }
}
