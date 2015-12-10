package com.atlassian.jira.util.collect;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;

/**
 * A version of {@link com.atlassian.jira.util.collect.EnclosedIterable} that is backed by a list
 *
 * @since 3.13
 */
public class MockCloseableIterable<T> implements EnclosedIterable<T>
{
    private final List<T> list;

    public MockCloseableIterable(final List<T> list)
    {
        this.list = Collections.unmodifiableList(list);
    }

    public <I> MockCloseableIterable(final List<I> list, final Function<I, T> transformer)
    {
        this.list = CollectionUtil.transform(list, transformer);
    }

    public void foreach(final Consumer<T> sink)
    {
        for (final T element : list)
        {
            sink.consume(element);
        }
    }

    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    public int size()
    {
        return list.size();
    }
}
