package com.atlassian.jira.util.collect;

import static com.atlassian.jira.util.collect.Transformed.entry;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

import com.atlassian.jira.util.Function;
import javax.annotation.Nonnull;

import net.jcip.annotations.Immutable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * Map implementation that decorates another {@link Map} whose value is an I and
 * uses a {@link Function} that converts that I into a V. The function is called
 * every time a value is requested, therefore certain operations are not
 * particularly performant, particularly the ones that require inspection of
 * values such as {@link #containsValue(Object)}.
 * <p>
 * This implementation is unmodifiable. This implementation is as thread-safe as
 * the underlying map.
 *
 * @param <K> the key type
 * @param <V> the final value type
 * @param <I> the value stored in the underlying map
 */
@Immutable
class TransformingMap<K, V, I> extends AbstractMap<K, V>
{
    private final Map<K, I> map;
    private final Function<I, V> transformer;

    TransformingMap(@Nonnull final Map<K, I> map, @Nonnull final Function<I, V> transformer)
    {
        this.map = notNull("map", map);
        this.transformer = notNull("transformer", transformer);
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return new TransformingSet<Entry<K, I>, Entry<K, V>>(map.entrySet(), new Function<Entry<K, I>, Entry<K, V>>()
        {
            public Entry<K, V> get(final Entry<K, I> entry)
            {
                return entry(entry, transformer);
            }
        });
    }

    @Override
    public V get(final Object key)
    {
        return transformer.get(map.get(key));
    }

    //
    // unsupported
    //

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V put(final K key, final V value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> t)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(final Object key)
    {
        throw new UnsupportedOperationException();
    }

    //
    // inner classes
    //

    static class DecoratedEntry<K, I, V> implements Map.Entry<K, V>
    {
        private final Entry<? extends K, ? extends I> entry;
        private final Function<I, V> transformer;
        private V value;

        DecoratedEntry(@Nonnull final Entry<? extends K, ? extends I> entry, @Nonnull final Function<I, V> transformer)
        {
            this.entry = notNull("entry", entry);
            this.transformer = notNull("transformer", transformer);
        }

        public K getKey()
        {
            return entry.getKey();
        }

        public V getValue()
        {
            if (value == null)
            {
                value = transformer.get(entry.getValue());
            }
            return value;
        }

        public V setValue(final V value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(final Object o)
        {
            if (!(o instanceof Map.Entry))
            {
                return false;
            }
            @SuppressWarnings("unchecked")
            final Map.Entry<K, V> e = (Map.Entry<K, V>) o;
            return eq(getKey(), e.getKey()) && eq(value, e.getValue());
        }

        @Override
        public int hashCode()
        {
            return ((getKey() == null) ? 0 : getKey().hashCode()) ^ ((value == null) ? 0 : value.hashCode());
        }

        private static boolean eq(final Object o1, final Object o2)
        {
            return (o1 == null ? o2 == null : o1.equals(o2));
        }
    }
}
