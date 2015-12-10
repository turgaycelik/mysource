package com.atlassian.jira.util.collect;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import com.atlassian.jira.util.Function;
import javax.annotation.Nonnull;

import net.jcip.annotations.Immutable;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * {@link Set} implementation that decorates another {@link Set} who contains values
 * of type I and uses a {@link Function} that converts those I into a V.
 * <p>
 * This implementation is unmodifiable. This implementation is as thread-safe as
 * the underlying {@link Set}.
 *
 * @param <I> the value in the underlying iterator
 * @param <E> the value it is converted to
 */
@Immutable
class TransformingSet<I, E> extends AbstractSet<E>
{
    private final Set<I> set;
    private final Function<I, E> transformer;

    TransformingSet(@Nonnull final Set<I> set, @Nonnull final Function<I, E> transformer)
    {
        this.set = notNull("set", set);
        this.transformer = notNull("transformer", transformer);
    }

    @Override
    public Iterator<E> iterator()
    {
        return Transformed.iterator(new UnmodifiableIterator<I>(set.iterator()), transformer);
    }

    @Override
    public int size()
    {
        return set.size();
    }

    //
    // unsupported
    //

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
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }
}
