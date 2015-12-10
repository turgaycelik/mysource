package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Supplier;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MultiMaps
{
    public static <K, V, C extends Collection<V>> MultiMap<K, V, C> create(final Map<K, C> basedOn, final Supplier<C> collectionFactory)
    {
        return new MMap<K, V, C>(basedOn, collectionFactory);
    }

    public static <K, V, C extends Collection<V>> MultiMap<K, V, C> create(final Supplier<C> collectionFactory)
    {
        return new MMap<K, V, C>(new HashMap<K, C>(), collectionFactory);
    }

    static class MMap<K, V, C extends Collection<V>> extends AbstractMap<K, C> implements MultiMap<K, V, C>
    {
        private final Map<K, C> delegate;
        private final Supplier<C> collectionFactory;

        public MMap(final Map<K, C> delegate, final Supplier<C> collectionFactory)
        {
            this.delegate = delegate;
            this.collectionFactory = collectionFactory;
        }

        public C getAll(final K key)
        {
            return delegate.get(key);
        }

        public C allValues()
        {
            final C result = collectionFactory.get();
            for (final C collection : values())
            {
                result.addAll(collection);
            }
            return result;
        }

        @Override
        public Set<Entry<K, C>> entrySet()
        {
            return delegate.entrySet();
        }

        @Override
        public C put(final K key, final C value)
        {
            return delegate.put(key, value);
        }

        @Override
        public boolean putSingle(final K key, final V value)
        {
            C collection = delegate.get(key);
            if (collection == null)
            {
                collection = collectionFactory.get();
                delegate.put(key, collection);
            }
            return collection.add(value);
        }

        @Override
        public void putAll(final Map<? extends K, ? extends C> t)
        {
            delegate.putAll(t);
        }

        @Override
        public Collection<C> values()
        {
            return delegate.values();
        }

        public <Val extends V> boolean contains(final Val value)
        {
            for (final C collection : values())
            {
                if (collection.contains(value))
                {
                    return true;
                }
            }
            return false;
        }

        public boolean containsValue(final Collection<?> value)
        {
            return super.containsValue(value);
        }

        public void copyTo(final Collection<V> copyTo)
        {
            for (final C collection : values())
            {
                copyTo.addAll(collection);
            }
        }

        public int sizeAll()
        {
            int result = 0;
            for (final C collection : values())
            {
                result += collection.size();
            }
            return result;
        }
    }
}
