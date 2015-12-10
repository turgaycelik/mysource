package com.atlassian.jira.util.collect;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adaptor for turning an {@link Iterator} an into {@link Enumeration}.
 * 
 * @param <E> the type of element produced.
 */
public class IteratorEnumeration<E> implements Enumeration<E>
{
    public static <E> Enumeration<E> fromIterator(final Iterator<? extends E> iterator)
    {
        return new IteratorEnumeration<E>(iterator);
    }

    public static <E> Enumeration<E> fromIterable(final Iterable<? extends E> iterable)
    {
        return fromIterator(iterable.iterator());
    }

    private final Iterator<? extends E> iterator;

    IteratorEnumeration(final Iterator<? extends E> iterator)
    {
        this.iterator = iterator;
    }

    public boolean hasMoreElements()
    {
        return iterator.hasNext();
    }

    public E nextElement()
    {
        return iterator.next();
    }
}
