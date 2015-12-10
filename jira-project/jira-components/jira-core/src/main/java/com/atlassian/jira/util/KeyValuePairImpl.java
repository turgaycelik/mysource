package com.atlassian.jira.util;

/**
 * @since v4.4
 */
public class KeyValuePairImpl<K, V> implements KeyValuePair<K, V>
{
    private final K key;
    private final V value;

    public KeyValuePairImpl(K key, V value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey()
    {
        return key;
    }

    @Override
    public V getValue()
    {
        return value;
    }
}
