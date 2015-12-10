package com.atlassian.jira.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of CacheMap that allows null keys and values.
 *
 * <p> The data is stored in a {@link ConcurrentHashMap} giving it high throughput under load.
 * It uses a special token to transparently store null keys and values which are not natively supported by {@link ConcurrentHashMap}.
 *
 * @since v4.0
 */
@SuppressWarnings ("unchecked")
public class ConcurrentCacheMap<K, V> implements CacheMap<K, V>
{
    private static final Object NULL_TOKEN = new Object();
    private final ConcurrentHashMap map;

    public ConcurrentCacheMap()
    {
        map = new ConcurrentHashMap();
    }

    public V get(final K key)
    {
        return (V) convertTokenToNull(map.get(convertNullToToken(key)));
    }

    public void put(final K key, final V value)
    {
        map.put(convertNullToToken(key), convertNullToToken(value));
    }

    public void clear()
    {
        map.clear();
    }

    public void remove(final K key)
    {
        map.remove(convertNullToToken(key));
    }

    public Collection<V> copyOfValues()
    {
        ArrayList<V> values = new ArrayList<V>();
        Collection rawValues = map.values();

        for (final Object rawValue : rawValues)
        {
            values.add((V) convertTokenToNull(rawValue));
        }

        return Collections.unmodifiableCollection(values);
    }

    private static Object convertNullToToken(final Object obj)
    {
        return obj == null ? NULL_TOKEN : obj;
    }

    private static <T> T convertTokenToNull(final T obj)
    {
        return obj == NULL_TOKEN ? null : obj;
    }
}
