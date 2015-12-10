package com.atlassian.jira.util.collect;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MultiMap<K, V, C extends Collection<V>> extends Map<K, C>
{
    /**
     * Get all elements in all Collections. This returns the same type as the mapped Collections, 
     * so if this {@link MultiMap} contains {@link List lists} then there will be the same number 
     * of elements as {@link #sizeAll()}, whereas if the {@link MultiMap} contains {@link Set sets}
     * then duplicates may be removed. If a different type of Collection
     * <p>
     * The returned Collection is immutable.
     * @return a Collection
     */
    C allValues();

    /**
     * The sum of sizes of all contained keys. May be different to calling {@link #size()} on the 
     * result of {@link #allValues()}
     * 
     * @return the total number of elements in all mapped collections.
     */
    int sizeAll();

    /**
     * Put a single value in to the appropriate mapped collection.
     *
     * @param key the Key
     * @param value the single value to add to the collection for this key
     * @return whether the Collection was modified.
     */
    boolean putSingle(final K key, final V value);

    /**
     * Does this {@link MultiMap} contain the value in any of its value collections.
     * 
     * @param value the single value to check for
     * @return true if the value is contained in any of the mapped collections
     */
    boolean contains(Object value);

    /**
     * Does this {@link MultiMap} contain the Collection as any of its value collections.
     * 
     * @param value the collection to check for
     * @return true if the value is contained in any of the mapped collections
     */
    boolean containsValue(Collection<?> value);

    /**
     * Copy all values to the supplied collection.
     * 
     * @param collection to copy all the values to.
     */
    void copyTo(Collection<V> collection);
}
