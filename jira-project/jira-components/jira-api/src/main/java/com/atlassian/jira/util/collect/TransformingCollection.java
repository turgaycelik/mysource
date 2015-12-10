package com.atlassian.jira.util.collect;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import com.atlassian.jira.util.Function;
import javax.annotation.Nonnull;

import net.jcip.annotations.Immutable;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

@Immutable
class TransformingCollection<I, E> extends AbstractCollection<E>
{
    private final Collection<? extends I> collection;
    private final Function<I, E> transformer;

    TransformingCollection(@Nonnull final Collection<? extends I> collection, @Nonnull final Function<I, E> transformer)
    {
        this.collection = notNull("collection", collection);
        this.transformer = notNull("transformer", transformer);
    }

    @Override
    public Iterator<E> iterator()
    {
        return Transformed.iterator(new UnmodifiableIterator<I>(collection.iterator()), transformer);
    }

    @Override
    public int size()
    {
        return collection.size();
    }

    //
    // unsupported
    //

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException();
    }

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
    public boolean remove(final Object o)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> c)
    {
        throw new UnsupportedOperationException();
    }
}
