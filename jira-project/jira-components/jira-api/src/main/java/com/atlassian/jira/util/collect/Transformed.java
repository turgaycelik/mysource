package com.atlassian.jira.util.collect;

import static com.atlassian.jira.util.collect.TransformingIterable.transformingIterable;

import com.atlassian.jira.util.Function;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static factory for creating transformed {@link Map}, {@link Set}, and {@link Iterator} instances.
 * Returned instances are unmodifiable unless otherwise noted.
 * <p>
 * All methods return implemetations that transform when iterating or retrieving elements.
 * This means that changes to the underlying collections will
 * be visible to the resulting collection (except for the {@link Set} of course).
 */
public class Transformed
{
    public static <K, I, V> Map<K, V> map(final Map<K, I> map, final Function<I, V> transformer)
    {
        return new TransformingMap<K, V, I>(map, transformer);
    }

    public static <K, I, V> Map.Entry<K, V> entry(final Map.Entry<? extends K, ? extends I> entry, final Function<I, V> transformer)
    {
        return new TransformingMap.DecoratedEntry<K, I, V>(entry, transformer);
    }

    public static <I, E> List<E> list(final List<I> set, final Function<I, E> transformer)
    {
        return new TransformingList<I, E>(set, transformer);
    }

    public static <I, E> Collection<E> collection(final Collection<? extends I> collection, final Function<I, E> transformer)
    {
        return new TransformingCollection<I, E>(collection, transformer);
    }

    public static <I, E> EnclosedIterable<E> enclosedIterable(final EnclosedIterable<I> iterable, final Function<I, E> transformer)
    {
        return new TransformingEnclosedIterable<I, E>(iterable, transformer);
    }

    public static <I, E> Iterable<E> iterable(final Iterable<I> iterable, final Function<I, E> transformer)
    {
        return TransformingIterable.transformingIterable(iterable, transformer);
    }

    /**
     * {@link Iterator} that transforms its values from one type to another using the supplied {@link Function}.
     * This iterator's {@link Iterator#remove()} method delegates to the supplied iterator.
     * 
     * @param <I>
     * @param <E>
     * @param set
     * @param transformer
     * @return
     */
    public static <I, E> Iterator<E> iterator(final Iterator<? extends I> set, final Function<I, E> transformer)
    {
        return new TransformingIterator<I, E>(set, transformer);
    }

    private Transformed()
    {
        throw new AssertionError();
    }
}
