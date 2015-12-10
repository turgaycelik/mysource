package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Supplier;
import com.google.common.collect.ImmutableMap;
import net.jcip.annotations.NotThreadSafe;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.collect.CollectionUtil.transformSet;

/**
 * A {@link Map} that is  backed by a {@link Master} map that contains {@link Supplier suppliers}
 * for the values that are called lazily when required. This can reduce the amount of memory
 * required if there are a number of entries that may or may not be required in the most case.
 * <p>
 * For example, when rendering Velocity templates, the context map is populated with a lot of
 * things that are only ever required occasionally.
 * <p>
 * Note: even though this class is effectively immutable and does not support direct mutation,
 * the memoization strategy is not thread-safe and should not be used if thread-safety is
 * required. Adding thread-safety to this class would be fairly trivial through.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
@NotThreadSafe
public class MemoizingMap<K, V> extends AbstractMap<K, V> implements Map<K, V>
{
    private final Map<K, Supplier<? extends V>> master;
    private final Map<K, V> cache = new HashMap<K, V>();

    MemoizingMap(final Map<K, Supplier<? extends V>> underlying)
    {
        this.master = underlying;
    }

    /**
     * {@inheritDoc}.
     * <p>
     * Implements the local caching of a value from the master.
     */
    @Override
    public V get(final Object key)
    {
        V value = cache.get(key);
        if (value == null)
        {
            final Supplier<? extends V> supplier = master.get(key);
            if (supplier == null)
            {
                return null;
            }
            value = supplier.get();
            @SuppressWarnings("unchecked")
            final K k = (K) key;
            cache.put(k, value);
        }
        return value;
    }

    @Override
    public Set<K> keySet()
    {
        return master.keySet();
    }

    @Override
    public boolean containsKey(final Object key)
    {
        return keySet().contains(key);
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return transformSet(keySet(), new EntryTransformer());
    }

    @Override
    public int size()
    {
        return master.size();
    }

    @Override
    public boolean isEmpty()
    {
        return master.isEmpty();
    }

    /*
     * short-cut fail, abstract map impl finds the entry in the entrySet iterator 
     * before discovering it can't remove it
     */
    @Override
    public V remove(final Object key)
    {
        throw new UnsupportedOperationException();
    }

    //
    // inner classes
    //

    /**
     * Transforms a Key into a {@link Entry} that lazily evaluates the value.
     */
    class EntryTransformer implements Function<K, Entry<K, V>>
    {
        public Entry<K, V> get(final K key)
        {
            return new LazyMapEntry<K, V>(MemoizingMap.this, key);
        }
    }

    /**
     * Master that individual Maps can be printed from.
     * @param <K> the key type
     * @param <V> the value type
     */
    public static class Master<K, V>
    {
        public static <K, V> Builder<K, V> builder()
        {
            return new Builder<K, V>();
        }

        private final Map<K, Supplier<? extends V>> suppliers;

        Master(final Map<K, Supplier<? extends V>> suppliers)
        {
            this.suppliers = suppliers;
        }

        /**
         * Make a composite {@link Master} of the other and this master.
         * 
         * @param other any values that may override elements in this Master.
         * @return a map instance that will be lazily populated with the values from the master.
         */
        public Master<K, V> combine(final Master<K, V> other)
        {
            return new Master<K, V>(CompositeMap.of(other.suppliers, suppliers));
        }

        /**
         * Make a composite caching map of the local and master maps.
         * 
         * @param localValues any values that may override elements in the Master.
         * @return a map instance that will be lazily populated with the values from the master.
         */
        public Map<K, V> toMap(final Map<K, ? extends V> localValues)
        {
            @SuppressWarnings("unchecked")
            final Map<K, V> map = (Map<K, V>) localValues;
            return CompositeMap.of(map, new MemoizingMap<K, V>(suppliers));
        }

        /**
         * Used to build a {@link Master} that individual local copies can then be copied from.
         *
         * @param <K>
         * @param <V>
         */
        public static class Builder<K, V>
        {
            private final Map<K, Supplier<? extends V>> suppliers = new HashMap<K, Supplier<? extends V>>();

            Builder()
            {}

            public Builder<K, V> addLazy(final K key, final Supplier<? extends V> value)
            {
                suppliers.put(key, value);
                return this;
            }

            public Builder<K, V> add(final K key, final V value)
            {
                suppliers.put(key, new ReferenceSupplier<V>(value));
                return this;
            }

            public Master<K, V> master()
            {
                return new Master<K, V>(ImmutableMap.copyOf(suppliers));
            }
        }
    }

    static class ReferenceSupplier<T> implements Supplier<T>
    {
        private final T ref;

        public ReferenceSupplier(final T ref)
        {
            this.ref = ref;
        }

        public T get()
        {
            return ref;
        }
    }
}
