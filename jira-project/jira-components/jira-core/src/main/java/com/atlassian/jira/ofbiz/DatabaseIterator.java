package com.atlassian.jira.ofbiz;

import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.collect.CloseableIterator;
import com.atlassian.jira.util.dbc.Assertions;
import org.ofbiz.core.entity.GenericValue;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * A database implementation of {@link com.atlassian.jira.util.collect.CloseableIterator}
 *
 * @since v3.13
 */
class DatabaseIterator<E> implements CloseableIterator<E>
{
    private final Resolver<GenericValue, E> resolver;
    private final OfBizListIterator listIterator;

    // if marked we're finished
    private final AtomicMarkableReference<E> next = new AtomicMarkableReference<E>(null, false);

    public DatabaseIterator(final Resolver<GenericValue, E> resolver, final OfBizListIterator listIterator)
    {
        Assertions.notNull("resolver", resolver);
        Assertions.notNull("listIterator", listIterator);
        this.resolver = resolver;
        this.listIterator = listIterator;
    }

    public boolean hasNext()
    {
        // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
        // if there are any results left in the iterator is to iterate over it until null is returned
        // (i.e. not use hasNext() method)
        // The documentation mentions efficiency only - but the functionality is totally broken when using
        // hsqldb JDBC drivers (hasNext() always returns true).
        // So listen to the OfBiz folk and iterate until null is returned.
        populateNextIfNull();
        final boolean[] finished = new boolean[1];
        final Object ref = next.get(finished);
        final boolean hasNext = (ref != null) || !finished[0];
        return hasNext;
    }

    public E next()
    {
        populateNextIfNull();
        if (next.isMarked())
        {
            throw new NoSuchElementException();
        }
        try
        {
            return next.getReference();
        }
        finally
        {
            next.set(null, false);
        }
    }

    private void pullNext()
    {
        final GenericValue gv = listIterator.next();
        // don't resolve if null
        final E ref = (gv == null) ? null : resolver.get(gv);
        // mark if null
        next.set(ref, ref == null);
    }

    private void populateNextIfNull()
    {
        if (!next.isMarked() && (next.getReference() == null))
        {
            pullNext();
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException("Cannot remove an from this Iterator");
    }

    public void close()
    {
        listIterator.close();
    }
}
