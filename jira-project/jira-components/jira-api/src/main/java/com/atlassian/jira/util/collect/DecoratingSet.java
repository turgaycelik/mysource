package com.atlassian.jira.util.collect;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.util.Function;

import net.jcip.annotations.Immutable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * {@link Set} implementation that decorates the values of another {@link Set}
 * with a decorator {@link Function} that converts the values.
 * <p>
 * The decorator <strong>must not</strong> affect the equality/hashCode of the
 * underlying objects, otherwise it will break the {@link Set} contract in weird
 * and not so wonderful ways.
 * <p>
 * This implementation is unmodifiable. This implementation is as thread-safe as
 * the underlying {@link Set}.
 *
 * @param <E> the element Type
 */
@Immutable
class DecoratingSet<E> extends AbstractSet<E>
{
    private final Set<E> set;
    private final Function<E, E> transformer;

    DecoratingSet(@Nonnull final Set<E> set, @Nonnull final Function<E, E> transformer)
    {
        this.set = notNull("set", set);
        this.transformer = notNull("transformer", transformer);
    }

    @Override
    public Iterator<E> iterator()
    {
        return Transformed.iterator(new UnmodifiableIterator<E>(set.iterator()), transformer);
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
    }

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
