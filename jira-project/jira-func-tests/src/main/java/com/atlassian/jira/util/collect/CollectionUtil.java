package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Functions;
import javax.annotation.Nonnull;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.collect.EnumerationIterator.fromEnumeration;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public final class CollectionUtil
{
    /**
     * For each element in the iterator, consume the contents.
     *
     * @param <T> the type of element to consume
     * @param iterator to iterate over the elements
     * @param consumer to consume the elements
     */
    public static <T> void foreach(@Nonnull final Iterator<? extends T> iterator, @Nonnull final Consumer<T> consumer)
    {
        while (iterator.hasNext())
        {
            consumer.consume(iterator.next());
        }
    }

    /**
     * For each element in the iterator, consume the contents.
     *
     * @param <T> the element type
     * @param iterable to iterate over the elements, may be null
     * @param consumer to consume the elements
     */
    public static <T> void foreach(final Iterable<T> iterable, @Nonnull final Consumer<T> consumer)
    {
        if (iterable != null)
        {
            foreach(iterable.iterator(), consumer);
        }
    }

    /**
     * Turn the iterator into a list.
     *
     * @param <T> the element type
     * @param iterator to iterate over the elements
     * @return an unmodifiable {@link List} of the elements in the iterator
     */
    public static <T> List<T> toList(@Nonnull final Iterator<? extends T> iterator)
    {
        final List<T> result = new ArrayList<T>();
        foreach(iterator, new Consumer<T>()
        {
            public void consume(final T element)
            {
                result.add(element);
            }
        });
        return unmodifiableList(result);
    }

    /**
     * Turn the iterable into a list.
     *
     * @param <T> the element type
     * @param iterable to iterate over the elements
     * @return an unmodifiable {@link List} of the elements in the iterator
     */
    public static <T> List<T> toList(@Nonnull final Iterable<? extends T> iterable)
    {
        return toList(iterable.iterator());
    }

    /**
     * Turn the enumeration into a list.
     *
     * @param <T> the element type
     * @param enumeration to enumerate over the elements
     * @return an unmodifiable {@link List} of the elements in the iterator
     */
    public static <T> List<T> toList(@Nonnull final Enumeration<? extends T> enumeration)
    {
        return toList(fromEnumeration(enumeration));
    }

    /**
     * Turn the iterable into a Set.
     *
     * @param <T> the element type
     * @param iterable to iterate over the elements
     * @return an unmodifiable {@link Set} of the elements in the iterator
     */
    public static <T> Set<T> toSet(@Nonnull final Iterable<? extends T> iterable)
    {
        return toSet(iterable.iterator());
    }

    /**
     * Turn the iterable into a Set.
     *
     * @param <T> the element type
     * @param iterator to iterate over the elements
     * @return an unmodifiable {@link Set} of the elements in the iterator
     */
    public static <T> Set<T> toSet(@Nonnull final Iterator<? extends T> iterator)
    {
        return ImmutableSet.<T> builder().addAll(iterator).build();
    }

    /**
     * Return a List that is transformed from elements of the input type to elements of the output type by a transformer function.
     *
     * @param <T> the input type
     * @param <R> the out type
     * @param iterator to iterate over the contents
     * @param transformer the function that performs the transformation
     * @return an unmodifiable List of the transformed type
     */
    public static <T, R> List<R> transform(@Nonnull final Iterator<? extends T> iterator, @Nonnull final Function<T, R> transformer)
    {
        return toList(transformIterator(iterator, transformer));
    }

    /**
     * Return a {@link List} that is transformed from elements of the input type
     * to elements of the output type by a transformer function.
     * <p>
     * Note, this performs a copy and applies the transform to all elements. If you want
     * a lazily applied function, see Transform
     *
     * @param <T> the input type
     * @param <R> the out type
     * @param iterable the contents
     * @param transformer the function that performs the transformation
     * @return an unmodifiable List of the transformed type
     */
    public static <T, R> List<R> transform(@Nonnull final Iterable<? extends T> iterable, @Nonnull final Function<T, R> transformer)
    {
        if (iterable == null)
        {
            return emptyList();
        }
        return transform(iterable.iterator(), transformer);
    }

    /**
     * Return an {@link Iterator} that is transformed from elements of the input
     * type to elements of the output type by a transformer function.
     *
     * @param <T> the input type
     * @param <R> the out type
     * @param iterator the contents
     * @param transformer the function that performs the transformation
     * @return an {@link Iterator} of the transformed type
     */
    public static <T, R> Iterator<R> transformIterator(@Nonnull final Iterator<? extends T> iterator, @Nonnull final Function<T, R> transformer)
    {
        return new TransformingIterator<T, R>(iterator, transformer);
    }

    /**
     * Return a {@link Set} that is transformed from elements of the input type
     * to elements of the output type by a transformer function.
     * <p>
     * Note, this performs a copy and applies the transform to all elements.
     *
     * @param <T> the input type
     * @param <R> the output type
     * @param iterable the contents
     * @param transformer the function that performs the transformation
     * @return an unmodifiable Set of the transformed type
     */
    public static <T, R> Set<R> transformSet(@Nonnull final Iterable<T> iterable, @Nonnull final Function<T, R> transformer)
    {
        return toSet(transformIterator(iterable.iterator(), transformer));
    }

    /**
     * Does the supplied {@link Iterator} contain anything that matches the predicate?
     *
     * @param <T> the element type
     * @param iterator containing elements
     * @param predicate the matcher
     * @return true if the predicate returns true for any elements.
     */
    public static <T> boolean contains(@Nonnull final Iterator<? extends T> iterator, @Nonnull final Predicate<T> predicate)
    {
        return filter(iterator, predicate).hasNext();
    }

    /**
     * Does the supplied {@link Iterable} contain anything that matches the predicate?
     *
     * @param <T> the element type
     * @param iterable containing elements
     * @param predicate the matcher
     * @return true if the predicate returns true for any elements.
     */
    public static <T> boolean contains(@Nonnull final Iterable<? extends T> iterable, @Nonnull final Predicate<T> predicate)
    {
        return contains(iterable.iterator(), predicate);
    }

    /**
     * Create a filtered {@link Iterator}.
     *
     * @param <T> the element type
     * @param iterator an iterator that only returns elements approved by the predicate
     * @param predicate for checking the elements
     * @return a filtered {@link Iterator}
     */
    public static <T> Iterator<T> filter(@Nonnull final Iterator<T> iterator, @Nonnull final Predicate<? super T> predicate)
    {
        return new FilteredIterator<T>(iterator, predicate);
    }

    /**
     * Create a filtered {@link Iterable}.
     *
     * @param <T> the element type
     * @param iterable an iterable whose iterators only returns elements approved by the predicate
     * @param predicate for checking the elements
     * @return a filtered {@link Iterable}
     */
    public static <T> Iterable<T> filter(@Nonnull final Iterable<T> iterable, @Nonnull final Predicate<? super T> predicate)
    {
        return new FilteredIterable<T>(iterable, predicate);
    }

    static class FilteredIterable<T> implements Iterable<T>
    {
        private final Iterable<T> delegate;
        private final Predicate<? super T> predicate;

        FilteredIterable(final Iterable<T> delegate, final Predicate<? super T> predicate)
        {
            this.delegate = notNull("delegate", delegate);
            this.predicate = notNull("predicate", predicate);
        }

        public Iterator<T> iterator()
        {
            return new FilteredIterator<T>(delegate.iterator(), predicate);
        }
    }

    public static <T, R> Iterable<R> transformAndFilter(final Iterable<T> iterable, final Function<T, R> transformer, final Predicate<R> predicate)
    {
        return filter(transform(iterable, transformer), predicate);
    }

    /**
     * Create a filtered {@link Collection}.
     *
     * @param <T> the element type
     * @param collection an iterable whose iterators only returns elements approved by the predicate
     * @param predicate for checking the elements
     * @return a filtered {@link Iterable}
     */
    public static <T> Collection<T> filter(@Nonnull final Collection<T> collection, @Nonnull final Predicate<? super T> predicate)
    {
        return Collections2.filter(collection, predicateAdapter(predicate));
    }

    /**
     * Filter a {@link Collection} for the specified subtype.
     *
     * @param <T> the incoming type
     * @param <R> the result type
     * @param iterable an iterable whose values are of the source type
     * @param subclass the result type, only return elements if they are of this type
     * @return a filtered {@link Collection} of the subtype
     */
    public static <T, R extends T> Collection<R> filterByType(@Nonnull final Iterable<T> iterable, @Nonnull final Class<R> subclass)
    {
        return transform(filter(iterable, Predicates.<T> isInstanceOf(subclass)), Functions.<T, R> downcast(subclass));
    }

    /**
     * Copy and sort the passed collection and return an unmodifiable {@link List} of the elements.
     * @param <T> the element type
     * @param collection the collection to copy
     * @param comparator for sorting
     * @return an unmodifiable {@link List} view of the elements.
     */
    public static <T> List<T> sort(@Nonnull final Collection<? extends T> collection, @Nonnull final Comparator<T> comparator)
    {
        notNull("collection", collection);
        notNull("comparator", comparator);
        final List<T> sorted = new ArrayList<T>(collection);
        if (sorted.size() > 1)
        {
            Collections.sort(sorted, comparator);
        }
        return unmodifiableList(sorted);
    }

    /**
     * Return an immutable list copy of the passed collection.
     *
     * @param copy the collection to copy.
     * @param <T> the type of elements for the returned collection.
     * @return an immutable list copy of the passed collection.
     */
    public static <T> List<T> copyAsImmutableList(@Nonnull final Collection<? extends T> copy)
    {
        notNull("copy", copy);
        if (copy.isEmpty())
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.unmodifiableList(new ArrayList<T>(copy));
        }
    }

    /**
     * Return an immutable set copy of the passed collection.
     *
     * @param copy the collection to copy.
     * @param <T> the type of elements for the returned collection.
     * @return an immutable set copy of the passed collection.
     */
    public static <T> Set<T> copyAsImmutableSet(@Nonnull final Collection<? extends T> copy)
    {
        notNull("copy", copy);
        if (copy.isEmpty())
        {
            return Collections.emptySet();
        }
        else
        {
            return Collections.unmodifiableSet(new HashSet<T>(copy));
        }
    }

    /**
     * Return an immutable copy of the passed map. The type of the reurned map is not gaurenteed to match the type
     * of the passed map. If this is important, than do it yourself.
     *
     * @param copy the map to copy.
     * @param <K> the type of key in the returned map.
     * @param <V> the type of value in the returned map.
     * @return the copied and immutable map.
     */
    public static <K, V> Map<K, V> copyAsImmutableMap(@Nonnull final Map<? extends K, ? extends V> copy)
    {
        notNull("copy", copy);
        if (copy.isEmpty())
        {
            return Collections.emptyMap();
        }
        else
        {
            return Collections.unmodifiableMap(new HashMap<K, V>(copy));
        }
    }

    /**
     * Return the first found element that the predicate matches.
     *
     * @param <T> the type of element to return
     * @param iterable that may contain the element to return
     * @param predicate to match the desired element
     * @return the first matched element or null if none found
     */
    public static <T> T findFirstMatch(@Nonnull final Iterable<? extends T> iterable, final Predicate<T> predicate)
    {
        notNull("iterable", iterable);
        notNull("predicate", predicate);
        for (final T t : iterable)
        {
            if (predicate.evaluate(t))
            {
                return t;
            }
        }
        return null;
    }

    /**
     * Returns the index of the first element that matches the predicate.
     *
     * @param iterable collection of elements
     * @param predicate to match the desired element
     * @param <T> the type of the elements
     * @return the 0-based index of the first element that matches the predicate or -1 if none found
     */
    public static <T> int indexOf(@Nonnull final Iterable<? extends T> iterable, @Nonnull final Predicate<? super T> predicate)
    {
        notNull("iterable", iterable);
        notNull("predicate", predicate);
        Iterator<? extends T> iterator = iterable.iterator();
        for (int i = 0; iterator.hasNext(); i++)
        {
            if (predicate.evaluate(iterator.next()))
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the first element of a an {@link Iterable} in iteration order, or null if empty.
     *
     * @param <T> the type
     * @param iterable the thing to get something from.
     * @return the first thing the iterator spits out. May
     */
    public static <T> T first(@Nonnull final Iterable<? extends T> iterable)
    {
        final Iterator<? extends T> iterator = iterable.iterator();
        return (iterator.hasNext()) ? iterator.next() : null;
    }

    /**
     * Take a map and eagerly transform all values into a new, immutable Map.
     *
     * @param map the original map
     * @param mapper the mapping function
     * @param <K> the key type
     * @param <R> the original value type
     * @param <S> the new value type
     * @return a new immutable map
     * @see com.atlassian.jira.util.collect.Transformed#map(java.util.Map, com.atlassian.jira.util.Function)  for a lazy version.
     */
    public static <K, R, S> Map<K, S> map(final Map<K, R> map, final Function<R, S> mapper)
    {
        final MapBuilder<K, S> builder = MapBuilder.newBuilder();
        for (final Map.Entry<K, R> entry : map.entrySet())
        {
            builder.add(entry.getKey(), mapper.get(entry.getValue()));
        }
        return builder.toMap();
    }

    static <T> com.google.common.base.Predicate<T> predicateAdapter(final Predicate<T> predicate)
    {
        return new GCollectPredicate<T>(predicate);
    }

    static class GCollectPredicate<T> implements com.google.common.base.Predicate<T>
    {
        private final Predicate<T> delegate;

        public GCollectPredicate(final Predicate<T> delegate)
        {
            this.delegate = delegate;
        }

        public boolean apply(final T input)
        {
            return delegate.evaluate(input);
        }
    }
}
