package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Function;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Adapter that provides a Map<K, K> where every key maps to itself.
 *
 * @param <K>
 */
class CollectionMap<K> extends AbstractMap<K, K>
{
    private final Collection<? extends K> collection;

    public CollectionMap(final Collection<? extends K> collection)
    {
        this.collection = collection;
    }

    @Override
    public Set<Entry<K, K>> entrySet()
    {
        return new LinkedHashSet<Entry<K, K>>(Transformed.collection(collection, new Function<K, Entry<K, K>>()
        {
            public Map.Entry<K, K> get(final K input)
            {
                return new SimpleEntry<K>(input);
            };
        }));
    }

    //
    // inner classes
    //    

    static class SimpleEntry<K> implements Map.Entry<K, K>
    {
        private final K value;

        SimpleEntry(final K value)
        {
            this.value = value;
        }

        public K getKey()
        {
            return value;
        }

        public K getValue()
        {
            return value;
        }

        public K setValue(final K value)
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
            final Map.Entry<K, K> e = (Map.Entry<K, K>) o;
            return eq(value, e.getKey()) && eq(value, e.getValue());
        }

        @Override
        public int hashCode()
        {
            return (value == null) ? 0 : value.hashCode() ^ value.hashCode();
        }

        private static boolean eq(final Object o1, final Object o2)
        {
            return (o1 == null ? o2 == null : o1.equals(o2));
        }
    }
}
