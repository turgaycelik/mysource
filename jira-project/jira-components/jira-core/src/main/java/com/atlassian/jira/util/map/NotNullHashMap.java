package com.atlassian.jira.util.map;

import java.util.HashMap;

/**
 * HashMap that quietly rejects any value that is null
 */
public class NotNullHashMap<K, V> extends HashMap<K, V>
{
    public V put(K key, V value)
    {
        if (value != null)
        {
            return super.put(key, value);
        }
        else
        {
            return super.get(key);
        }
    }
}
