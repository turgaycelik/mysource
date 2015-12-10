package com.atlassian.jira.util.collect;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import com.atlassian.jira.util.Function;
import javax.annotation.Nonnull;

import net.jcip.annotations.Immutable;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * {@link List} implementation that decorates another {@link List} who contains values
 * of type I and uses a {@link Function} that converts those I into a V.
 * <p>
 * This implementation is unmodifiable. This implementation is as thread-safe as
 * the underlying {@link List}.
 *
 * @param <I> the value in the underlying iterator
 * @param <E> the value it is converted to
 */
@Immutable
class TransformingList<I, E> extends AbstractList<E>
{
    private final List<? extends I> list;
    private final Function<I, E> transformer;

    TransformingList(@Nonnull final List<? extends I> collection, @Nonnull final Function<I, E> transformer)
    {
        this.list = notNull("collection", collection);
        this.transformer = notNull("transformer", transformer);
    }

    @Override
    public Iterator<E> iterator()
    {
        return Transformed.iterator(new UnmodifiableIterator<I>(list.iterator()), transformer);
    }

    @Override
    public int size()
    {
        return list.size();
    }

    @Override
    public E get(final int index)
    {
        return transformer.get(list.get(index));
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
    public boolean add(final E o)
    {
        throw new UnsupportedOperationException();
    };

    @Override
    public boolean addAll(final Collection<? extends E> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }
}
