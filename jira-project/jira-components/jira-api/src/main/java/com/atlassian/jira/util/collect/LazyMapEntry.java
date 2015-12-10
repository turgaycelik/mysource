package com.atlassian.jira.util.collect;

import java.util.Map;

/**
 * Lazily evaluates the value by delegating to the referenced map.
 */
class LazyMapEntry<K, V> implements Map.Entry<K, V>
{
    private final Map<K, V> map;
    private final K key;

    LazyMapEntry(final Map<K, V> map, final K key)
    {
        this.map = map;
        this.key = key;
    }

    public K getKey()
    {
        return key;
    }

    public V getValue()
    {
        return map.get(key);
    }

    public V setValue(final V value)
    {
        return map.put(key, value);
    };

    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof Map.Entry))
        {
            return false;
        }
        final V value = getValue();
        final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
        return eq(key, e.getKey()) && eq(value, e.getValue());
    }

    @Override
    public int hashCode()
    {
        final V value = getValue();
        return ((key == null) ? 0 : key.hashCode()) ^ ((value == null) ? 0 : value.hashCode());
    }

    private static boolean eq(final Object o1, final Object o2)
    {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128)
                .append("LazyMapEntry[key=").append(key)
                .append(",value=").append(getValue())
                .append(']').toString();
    }
}