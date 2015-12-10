package com.atlassian.jira.util.collect;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import javax.annotation.Nonnull;

import java.util.Iterator;

/**
 * {@link EnclosedIterable} that takes a decorating function and applies it when returning in the {@link Iterator}.
 *
 * @since v3.13
 */
class TransformingEnclosedIterable<I, O> implements EnclosedIterable<O>
{
    private final EnclosedIterable<I> delegate;
    private final Function<I, O> transformer;

    TransformingEnclosedIterable(@Nonnull final EnclosedIterable<I> delegate, @Nonnull final Function<I, O> transformer)
    {
        this.delegate = notNull("delegate", delegate);
        this.transformer = notNull("decorator", transformer);
    }

    public void foreach(final Consumer<O> sink)
    {
        delegate.foreach(new Consumer<I>()
        {
            public void consume(final I element)
            {
                sink.consume(transformer.get(element));
            }
        });
    }

    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }

    public int size()
    {
        return delegate.size();
    }
}
