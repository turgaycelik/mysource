package com.atlassian.jira.util.collect;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import com.atlassian.jira.util.Function;
import javax.annotation.Nonnull;

import java.util.Iterator;

/**
 * {@link Iterator} implementation that decorates another {@link Iterator} who
 * contains values of type I and uses a {@link Function} that converts that I
 * into a V.
 * <p>
 * This implementation is unmodifiable.
 *
 * @param <I> the value in the underlying iterator
 * @param <E> the value it is converted to
 */
class TransformingIterator<I, E> implements Iterator<E>
{
    private final Iterator<? extends I> iterator;
    private final Function<I, E> decorator;

    TransformingIterator(@Nonnull final Iterator<? extends I> iterator, @Nonnull final Function<I, E> decorator)
    {
        this.iterator = notNull("iterator", iterator);
        this.decorator = notNull("decorator", decorator);
    }

    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    public E next()
    {
        return decorator.get(iterator.next());
    }

    public void remove()
    {
        iterator.remove();
    }
}
