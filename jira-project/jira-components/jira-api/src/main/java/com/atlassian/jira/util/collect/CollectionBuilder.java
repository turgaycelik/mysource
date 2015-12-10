package com.atlassian.jira.util.collect;

import static com.atlassian.jira.util.collect.CollectionUtil.toList;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableSortedSet;
import net.jcip.annotations.NotThreadSafe;

import com.google.common.collect.Ordering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Convenience class for creating collections ({@link Set} and {@link List}) instances or
 * {@link EnclosedIterable enclosed iterables}.
 * <p>
 * The default methods {@link #asList()} and {@link #asSet()} and {@link #asSortedSet()} create immutable collections.
 *
 * @param <T> contained in the created collections.
 */
@NotThreadSafe
public final class CollectionBuilder<T>
{
    private static final Ordering<?> NATURAL_ORDER = new NaturalOrdering();

    public static <T> CollectionBuilder<T> newBuilder()
    {
        return new CollectionBuilder<T>(Collections.<T> emptyList());
    }

    public static <T> CollectionBuilder<T> newBuilder(final T... elements)
    {
        return new CollectionBuilder<T>(Arrays.asList(elements));
    }

    public static <T> List<T> list(final T... elements)
    {
        return unmodifiableList(Arrays.asList(elements));
    }

    static <T> Comparator<T> natural()
    {
        @SuppressWarnings("unchecked")
        final Comparator<T> result = (Comparator<T>) NATURAL_ORDER;
        return result;
    }

    private final List<T> elements = new LinkedList<T>();

    CollectionBuilder(final Collection<? extends T> initialElements)
    {
        elements.addAll(initialElements);
    }

    public CollectionBuilder<T> add(final T element)
    {
        elements.add(element);
        return this;
    }

    public <E extends T> CollectionBuilder<T> addAll(final E... elements)
    {
        this.elements.addAll(Arrays.asList(notNull("elements", elements)));
        return this;
    }

    public CollectionBuilder<T> addAll(final Collection<? extends T> elements)
    {
        this.elements.addAll(notNull("elements", elements));
        return this;
    }

    public CollectionBuilder<T> addAll(final Enumeration<? extends T> elements)
    {
        this.elements.addAll(toList(notNull("elements", elements)));
        return this;
    }

    public Collection<T> asCollection()
    {
        return asList();
    }

    public Collection<T> asMutableCollection()
    {
        return asMutableList();
    }

    public List<T> asArrayList()
    {
        return new ArrayList<T>(elements);
    }

    public List<T> asLinkedList()
    {
        return new LinkedList<T>(elements);
    }

    public List<T> asList()
    {
        return unmodifiableList(new ArrayList<T>(elements));
    }

    public List<T> asMutableList()
    {
        return asArrayList();
    }

    public Set<T> asHashSet()
    {
        return new HashSet<T>(elements);
    }

    public Set<T> asListOrderedSet()
    {
        return new LinkedHashSet<T>(elements);
    }

    public Set<T> asImmutableListOrderedSet()
    {
        return unmodifiableSet(new LinkedHashSet<T>(elements));
    }

    public Set<T> asSet()
    {
        return unmodifiableSet(new HashSet<T>(elements));
    }

    public Set<T> asMutableSet()
    {
        return asHashSet();
    }

    public SortedSet<T> asTreeSet()
    {
        return new TreeSet<T>(elements);
    }

    /**
     * Return a {@link SortedSet} of the elements of this builder in their natural order.
     * Note, will throw an exception if the elements are not comparable.
     *
     * @return an immutable sorted set.
     * @throws ClassCastException if the elements do not implement {@link Comparable}.
     */
    public SortedSet<T> asSortedSet()
    {
        return unmodifiableSortedSet(new TreeSet<T>(elements));
    }

    public SortedSet<T> asSortedSet(final Comparator<? super T> comparator)
    {
        final SortedSet<T> result = new TreeSet<T>(comparator);
        result.addAll(elements);
        return unmodifiableSortedSet(result);
    }

    public SortedSet<T> asMutableSortedSet()
    {
        return asTreeSet();
    }

    public EnclosedIterable<T> asEnclosedIterable()
    {
        return CollectionEnclosedIterable.copy(elements);
    }

    @SuppressWarnings("unchecked")
    static class NaturalOrdering extends Ordering<Comparable> implements Serializable
    {
        public int compare(final Comparable left, final Comparable right)
        {
            notNull("right", right); // left null is caught later
            if (left == right)
            {
                return 0;
            }

            return left.compareTo(right);
        }

        // preserving singleton-ness gives equals()/hashCode() for free
        private Object readResolve()
        {
            return NATURAL_ORDER;
        }

        @Override
        public String toString()
        {
            return "Ordering.natural()";
        }

        private static final long serialVersionUID = 0;
    }
}
