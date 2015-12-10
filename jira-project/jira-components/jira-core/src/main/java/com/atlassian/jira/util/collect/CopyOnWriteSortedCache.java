package com.atlassian.jira.util.collect;

import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Supplier;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;

import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.locks.ReentrantLock;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Simple copy-on-write ordered cache with {@link Set} semantics.
 * This implementation supports add/remove and replace all of elements.
 *
 * Note that because this implementation uses JVM-local locks, it is unsuitable
 * for use in a clustered environment. It should therefore not be used by core
 * JIRA for data that needs to be consistent across the cluster.
 *
 * @param <T> the element type.
 */
public class CopyOnWriteSortedCache<T extends Comparable<T>> implements Iterable<T>
{

    private volatile SortedSet<T> set;
    private final ManagedLock lock = ManagedLocks.manage(new ReentrantLock());

    public CopyOnWriteSortedCache(final Iterable<T> elements)
    {
        this.set = ImmutableSortedSet.copyOf(notNull("elements", elements));
    }

    //
    // mutators
    //

    public T add(final T t)
    {
        notNull("element", t);
        return lock.withLock(new Supplier<T>()
        {
            public T get()
            {
                set = ImmutableSortedSet.<T> naturalOrder().addAll(set).add(t).build();
                return t;
            }
        });
    }

    public void remove(final T t)
    {
        notNull("element", t);
        lock.withLock(new Runnable()
        {
            public void run()
            {
                set = ImmutableSortedSet.<T> naturalOrder().addAll(Iterables.filter(set, new Predicate<T>()
                {
                    public boolean apply(final T input)
                    {
                        return !t.equals(input);
                    }
                })).build();
            }
        });
    }

    public void replaceAll(final Iterable<T> elements)
    {
        lock.withLock(new Runnable()
        {
            public void run()
            {
                set = ImmutableSortedSet.copyOf(elements);
            }
        });
    }

    //
    // j.u.c. views
    //

    /**
     * An unmodifiable {@link List} view of the underlying data that does not change.
     *
     * @return a {@link List} containing all the elements as at the time the call was made.
     */
    public List<T> asList()
    {
        return new SetBackedList<T>(set);
    }

    /**
     * An unmodifiable {@link SortedSet} view of the underlying data that does not change.
     *
     * @return a {@link SortedSet} containing all the elements as at the time the call was made.
     */
    public SortedSet<T> asSortedSet()
    {
        return set;
    }

    /*
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<T> iterator()
    {
        return set.iterator();
    }

    //
    // inner classes
    //

    /**
     * A List that delegates to an underlying set for its elements.
     * <p>
     * Does not implement the ListIterator as there is no efficient way
     * to traverse backwards and forwards through a {@link SortedSet}.
     *
     * @param <T> the element type
     */
    static class SetBackedList<T> extends AbstractSequentialList<T>
    {

        private final SortedSet<T> set;

        SetBackedList(final SortedSet<T> set)
        {
            this.set = set;
        }

        @Override
        public T get(final int index)
        {
            return Iterables.get(set, index);
        }

        @Override
        public int size()
        {
            return set.size();
        }

        @Override
        public Iterator<T> iterator()
        {
            return set.iterator();
        }

        @Override
        public ListIterator<T> listIterator()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListIterator<T> listIterator(final int index)
        {
            throw new UnsupportedOperationException();
        }
    }
}
