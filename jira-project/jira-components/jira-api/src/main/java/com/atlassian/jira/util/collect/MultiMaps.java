package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Supplier;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * Static factory methods for creating and manipulating {@link MultiMap multi-maps}.
 */
public class MultiMaps
{
    public static <K, V, C extends Collection<V>> MultiMap<K, V, C> create(final Map<K, C> basedOn, final Supplier<C> collectionFactory)
    {
        return new MultiImpl<K, V, C>(basedOn, collectionFactory);
    }

    public static <K, V, C extends Collection<V>> MultiMap<K, V, C> create(final Supplier<C> collectionFactory)
    {
        return new MultiImpl<K, V, C>(new HashMap<K, C>(), collectionFactory);
    }

    public static <K, V> MultiMap<K, V, List<V>> createListMultiMap()
    {
        return create(new ListSupplier<V>());
    }

    public static <K, V> MultiMap<K, V, Set<V>> createSetMultiMap()
    {
        return create(new SetSupplier<V>());
    }

    public static <K, V> MultiMap<K, V, List<V>> unmodifiableListMultiMap(final MultiMap<K, V, List<V>> multiMap)
    {
        return new UnmodifiableMultiMap<K, V, List<V>>(multiMap, new Function<List<V>, List<V>>()
        {
            public List<V> get(final List<V> input)
            {
                return unmodifiableList(input);
            }
        });
    }

    public static <K, V> MultiMap<K, V, Set<V>> unmodifiableSetMultiMap(final MultiMap<K, V, Set<V>> multiMap)
    {
        return new UnmodifiableMultiMap<K, V, Set<V>>(multiMap, new Function<Set<V>, Set<V>>()
        {
            public Set<V> get(final Set<V> input)
            {
                return unmodifiableSet(input);
            }
        });
    }

    static class ListSupplier<V> implements Supplier<List<V>>
    {
        public List<V> get()
        {
            return new ArrayList<V>();
        }
    }

    static class SetSupplier<V> implements Supplier<Set<V>>
    {
        public Set<V> get()
        {
            return new HashSet<V>();
        }
    }

    static class MultiImpl<K, V, C extends Collection<V>> extends AbstractMap<K, C> implements MultiMap<K, V, C>
    {
        private final Map<K, C> delegate;
        private final Supplier<C> collectionFactory;

        public MultiImpl(final Map<K, C> delegate, final Supplier<C> collectionFactory)
        {
            this.delegate = delegate;
            this.collectionFactory = collectionFactory;
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

        public boolean contains(final Object value)
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

    static class UnmodifiableMultiMap<K, V, C extends Collection<V>> extends AbstractMap<K, C> implements MultiMap<K, V, C>
    {
        private final MultiMap<K, V, C> delegate;
        private final Function<C, C> unmodifiableTransformer;

        UnmodifiableMultiMap(final MultiMap<K, V, C> delegate, final Function<C, C> unmodifiableTransformer)
        {
            this.delegate = delegate;
            this.unmodifiableTransformer = unmodifiableTransformer;
        }

        public C allValues()
        {
            return unmodifiableTransformer.get(delegate.allValues());
        }

        @Override
        public Set<Entry<K, C>> entrySet()
        {
            final Set<Entry<K, C>> delegateSet = delegate.entrySet();
            return new DecoratingSet<Entry<K, C>>(delegateSet, new Function<Entry<K, C>, Entry<K, C>>()
            {
                public Map.Entry<K, C> get(final Map.Entry<K, C> input)
                {
                    return new Entry<K, C>()
                    {
                        public K getKey()
                        {
                            return input.getKey();
                        }

                        public C getValue()
                        {
                            return unmodifiableTransformer.get(input.getValue());
                        }

                        public C setValue(final C ignore)
                        {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public int hashCode()
                        {
                            return input.hashCode();
                        }

                        @Override
                        public boolean equals(final Object o)
                        {
                            return input.equals(o);
                        }
                    };
                }
            });
        }

        @Override
        public C put(final K key, final C value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean putSingle(final K key, final V value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(final Map<? extends K, ? extends C> t)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public C remove(final Object key)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<C> values()
        {
            return CollectionUtil.transform(delegate.values(), unmodifiableTransformer);
        }

        public boolean contains(final Object value)
        {
            return delegate.contains(value);
        }

        public boolean containsValue(final Collection<?> value)
        {
            return delegate.containsValue(value);
        }

        public void copyTo(final Collection<V> copyTo)
        {
            delegate.copyTo(copyTo);
        }

        public int sizeAll()
        {
            return delegate.sizeAll();
        }
    }
}
