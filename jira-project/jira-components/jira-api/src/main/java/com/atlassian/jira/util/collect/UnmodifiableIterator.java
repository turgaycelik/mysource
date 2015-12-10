package com.atlassian.jira.util.collect;

import java.util.Iterator;

/**
 * {@link Iterator} that does not allow item removal.
 *
 * @param <E>
 */
class UnmodifiableIterator<E> implements Iterator<E>
{
    private final Iterator<? extends E> iterator;

    UnmodifiableIterator(final Iterator<? extends E> iterator)
    {
        this.iterator = iterator;
    }

    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    public E next()
    {
        return iterator.next();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
