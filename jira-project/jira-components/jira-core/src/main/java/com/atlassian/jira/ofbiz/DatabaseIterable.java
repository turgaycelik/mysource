package com.atlassian.jira.ofbiz;

import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.collect.CloseableIterator;
import com.atlassian.jira.util.collect.EnclosedIterable;
import org.ofbiz.core.entity.GenericValue;

/**
 * A abstract implementation of {@link com.atlassian.jira.util.collect.EnclosedIterable} that defers to an {@link OfBizListIterator}.
 * <p/>
 * Note that the iteration order is up to the query that creates the {@link OfBizListIterator}.
 * <p/>
 * This implementation is unbounded.
 *
 * @since v3.13
 */
public abstract class DatabaseIterable<E> implements EnclosedIterable<E>
{
    // if size is unknown return -1
    private final int size;
    /**
     * Used to turn generic values into Domain objects
     */
    private final Resolver<GenericValue, E> resolver;

    public DatabaseIterable(final int size, final Resolver<GenericValue, E> resolver)
    {
        this.size = size;
        this.resolver = resolver;
    }

    CloseableIterator<E> iterator()
    {
        if (isEmpty())
        {
            return new EmptyIterator<E>();
        }
        return new DatabaseIterator<E>(resolver, createListIterator());
    }

    public final void foreach(final Consumer<E> consumer)
    {
        CloseableIterator.Functions.foreach(this.iterator(), consumer);
    }

    public final int size()
    {
        return size;
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Create a new iterator.
     *
     * @return an instance of OfBizListIterator
     */
    protected abstract OfBizListIterator createListIterator();
}