package com.atlassian.jira.util.collect;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link java.util.Set} implementation that considers objects equal if an only if they are the same instance. The
 * {@link Object#equals(Object)} and {@link Object#hashCode()} methods of the stored objects are ingored.
 * <p/>
 * This implementation uses another Set to store {@link com.atlassian.jira.util.collect.IdentitySet.IdentityReference}
 * objects for each object in the set.
 */
public final class IdentitySet<T> extends AbstractSet<T> implements Set<T>
{
    /**
     * The set that we actually use to implement this class.
     */
    private final Set<IdentityReference<T>> delegateSet;

    /**
     * Create a new set using the passed set as the store. The passed set should not be used by the caller while the new
     * IdentitySet is in use.
     *
     * @param delegateSet the set actually store the objects.
     */
    public IdentitySet(Set<IdentityReference<T>> delegateSet)
    {
        this.delegateSet = notNull("delegateSet", delegateSet);
    }

    /**
     * Create a new identity set.
     *
     * @param <T> the objects to be stored in the identity set.
     * @return the new set.
     */
    public static <T> IdentitySet<T> newSet()
    {
        return new IdentitySet<T>(new HashSet<IdentityReference<T>>());
    }

    /**
     * Create a new identity set. The elements in the returned set will be stored in addition order.
     *
     * @param <T> the objects to be stored in the identity set.
     * @return the new set.
     */
    public static <T> IdentitySet<T> newListOrderedSet()
    {
        return new IdentitySet<T>(new LinkedHashSet<IdentityReference<T>>());
    }

    @Override
    public void clear()
    {
        delegateSet.clear();
    }

    public Iterator<T> iterator()
    {
        return new IdentityIterator<T>(delegateSet.iterator());
    }

    @Override
    public boolean add(final T t)
    {
        return delegateSet.add(new IdentityReference<T>(t));
    }

    @Override
    public boolean remove(final Object o)
    {
        return delegateSet.remove(new IdentityReference<Object>(o));
    }

    @Override
    public boolean containsAll(final Collection<?> c)
    {
        return delegateSet.containsAll(convert(c));
    }

    @Override
    public boolean addAll(final Collection<? extends T> c)
    {
        return delegateSet.addAll(convert(c));
    }

    @Override
    public boolean retainAll(final Collection<?> c)
    {
        return delegateSet.retainAll(convert(c));
    }

    @Override
    public boolean removeAll(final Collection<?> c)
    {
        return delegateSet.removeAll(convert(c));
    }

    public int size()
    {
        return delegateSet.size();
    }

    @Override
    public boolean isEmpty()
    {
        return delegateSet.isEmpty();
    }

    @Override
    public boolean contains(final Object o)
    {
        return delegateSet.contains(new IdentityReference<Object>(o));
    }

    @Override
    public String toString()
    {
        return delegateSet.toString();
    }

    private static <C> Collection<IdentityReference<C>> convert(final Collection<? extends C> collection)
    {
        if (collection == null)
        {
            return null;
        }
        else if (collection.isEmpty())
        {
            return Collections.emptyList();
        }
        else if (collection.size() == 1)
        {
            return Collections.singletonList(new IdentityReference<C>(collection.iterator().next()));
        }
        else
        {
            final List<IdentityReference<C>> list = new ArrayList<IdentityReference<C>>(collection.size());
            for (C c : collection)
            {
                list.add(new IdentityReference<C>(c));
            }
            return list;
        }
    }

    /**
     * Iterator for the set.
     */
    private static class IdentityIterator<T> implements Iterator<T>
    {
        private final Iterator<IdentityReference<T>> delegateIterator;

        public IdentityIterator(final Iterator<IdentityReference<T>> delegateIterator)
        {
            this.delegateIterator = delegateIterator;
        }

        public boolean hasNext()
        {
            return delegateIterator.hasNext();
        }

        public T next()
        {
            return delegateIterator.next().getValue();
        }

        public void remove()
        {
            delegateIterator.remove();
        }
    }

    /**
     * Stores a reference to any Object. Two references are considered equal only when they hold the exact same instance
     * of an object.
     */
    public static final class IdentityReference<T>
    {
        private final T value;

        public IdentityReference(final T value)
        {
            this.value = value;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final IdentityReference that = (IdentityReference) o;
            return value == that.value;
        }

        @Override
        public int hashCode()
        {
            return System.identityHashCode(value);
        }

        public T getValue()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return String.valueOf(value);
        }

        public static <T> IdentityReference<T> wrap(T value)
        {
            return new IdentityReference<T>(value);
        }
    }
}
