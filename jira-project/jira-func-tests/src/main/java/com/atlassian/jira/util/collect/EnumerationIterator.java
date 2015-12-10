package com.atlassian.jira.util.collect;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adaptor for turning an {@link java.util.Enumeration} into an {@link java.util.Iterator}.
 * 
 * @param <E> the type of element produced.
 */
public class EnumerationIterator<E> implements Iterator<E>
{
    public static <E> Iterator<E> fromEnumeration(final Enumeration<? extends E> enumeration)
    {
        return new EnumerationIterator<E>(enumeration);
    }

    private final Enumeration<? extends E> enumeration;

    EnumerationIterator(final Enumeration<? extends E> enumeration)
    {
        this.enumeration = enumeration;
    }

    public boolean hasNext()
    {
        return enumeration.hasMoreElements();
    }

    public E next()
    {
        return enumeration.nextElement();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
