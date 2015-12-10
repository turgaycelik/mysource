package com.atlassian.jira.ofbiz;

import com.atlassian.jira.util.collect.CloseableIterator;

import java.util.NoSuchElementException;

/**
 * Used when size is zero.
 *
 * @since v3.13
 */
class EmptyIterator<E> implements CloseableIterator<E>
{
    public void close()
    {}

    public boolean hasNext()
    {
        return false;
    }

    public E next()
    {
        throw new NoSuchElementException();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}