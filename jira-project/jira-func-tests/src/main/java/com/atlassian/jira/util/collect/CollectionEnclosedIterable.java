package com.atlassian.jira.util.collect;

import com.atlassian.jira.util.Consumer;
import javax.annotation.Nonnull;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Simple collection based {@link EnclosedIterable}.
 *
 * @param <T>
 */
public class CollectionEnclosedIterable<T> implements EnclosedIterable<T>
{
    /**
     * Create an {@link EnclosedIterable} from the supplied Collection. Does not copy the collection
     * so you should only use this if you are about to lose the reference or the collection is immutable.
     * @param <T> the collection type
     * @param collection
     * @return
     */
    public static <T> EnclosedIterable<T> from(final Collection<? extends T> collection)
    {
        return new CollectionEnclosedIterable<T>(collection);
    }

    public static <T> EnclosedIterable<T> copy(final Collection<? extends T> collection)
    {
        return new CollectionEnclosedIterable<T>(new ArrayList<T>(collection));
    }

    private final Collection<? extends T> collection;

    CollectionEnclosedIterable(@Nonnull final Collection<? extends T> collection)
    {
        this.collection = Assertions.notNull("collection", collection);
    }

    public void foreach(final Consumer<T> sink)
    {
        for (final T element : collection)
        {
            sink.consume(element);
        }
    };

    public int size()
    {
        return collection.size();
    }

    public boolean isEmpty()
    {
        return collection.isEmpty();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (!getClass().equals(obj.getClass()))
        {
            return false;
        }
        @SuppressWarnings("unchecked")
        final CollectionEnclosedIterable<T> other = (CollectionEnclosedIterable<T>) obj;
        return collection.equals(other.collection);
    }

    @Override
    public int hashCode()
    {
        return collection.hashCode();
    }
}
