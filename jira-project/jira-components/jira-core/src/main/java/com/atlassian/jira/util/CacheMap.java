package com.atlassian.jira.util;

import java.util.Collection;

/**
 * An object that caches values that are mapped under keys.
 * A CacheMap cannot contain duplicate keys; each key can map to at most one value.
 * 
 * <p> The cache looks a lot like a {@link java.util.Map} and indeed implementations are likely to use a Map interally as
 *  the data store, however not all operations are implemented or exactly the same so this interface does not extend Map.
 *
 * @since v4.0
 */
public interface CacheMap<K, V>
{
    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this cache contains no mapping for the key.
     * 
     * @param key the key.
     * @return the value to which the specified key is mapped.
     */
    V get(K key);

    /**
     * Maps the specified key to the specified value in this cache.
     *
     * <p> The value can be retrieved by calling the <tt>get</tt> method
     * with a key that is equal to the original key.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    void put(K key, V value);

    /**
     * Removes all of the mappings from this cache.
     */
    void clear();

    /**
     * Removes the key (and its corresponding value) from this cache.
     * This method does nothing if the key is not in the cache.
     *
     * @param  key the key that needs to be removed
     */
    void remove(K key);

    /**
     * Returns a {@link Collection} view of the values contained in this cache.
     * 
     * <p> The collection is a copy of the values that were contained at the time that this operation was called.
     * Concurrent implementations of this interface will likely allow mutating operations to occur while the collection is 
     * being constructed, and which of these modifications are in the resulting collection will be timing and implementation dependant. 
     * 
     * @return a {@link Collection} view of the values contained in this cache.
     */
    Collection<V> copyOfValues();
}
