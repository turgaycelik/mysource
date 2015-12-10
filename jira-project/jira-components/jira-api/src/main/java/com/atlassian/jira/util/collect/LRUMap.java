package com.atlassian.jira.util.collect;

import net.jcip.annotations.NotThreadSafe;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.synchronizedMap;

/**
 * A subclass of {@link LinkedHashMap} that is access ordered AND constrained in size.
 * <p>
 * It is very important that any multi-threaded access to this class be externally synchronised
 * as even non mutative operations such as {@link #get(Object)} will cause internal modifications
 * to the map order.
 * 
 * @param <K> key type
 * @param <V> value type
 * @see LinkedHashMap
 *
 * @deprecated Since v6.2 Use {@link com.atlassian.cache.CacheFactory} instead and build a proper cache.
 */
@Deprecated
@NotThreadSafe
public class LRUMap<K, V> extends LinkedHashMap<K, V> implements Map<K, V>
{
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Get a map that is access ordered and has the specified capacity.
     * 
     * @param <K> the key type
     * @param <V> the value type
     * @param capacity the maximum capacity
     * @return an instance of LRUMap
     */
    public static <K, V> Map<K, V> newLRUMap(final int capacity)
    {
        return new LRUMap<K, V>(capacity);
    }

    /**
     *  Get a map that is access ordered and has the specified capacity that is thread-safe.
     *  
     * @param <K> the key type
     * @param <V> the value type
     * @param capacity the maximum capacity
     * @return an instance of LRUMap wrapped in a synchronized map.
     */
    public static <K, V> Map<K, V> synchronizedLRUMap(final int capacity)
    {
        return synchronizedMap(LRUMap.<K, V> newLRUMap(capacity));
    }

    private final int maxSize;

    LRUMap(final int capacity)
    {
        super(capacity, DEFAULT_LOAD_FACTOR, true);
        this.maxSize = capacity;
    }

    @Override
    protected final boolean removeEldestEntry(final Map.Entry<K, V> eldest)
    {
        return size() > maxSize;
    }
}
