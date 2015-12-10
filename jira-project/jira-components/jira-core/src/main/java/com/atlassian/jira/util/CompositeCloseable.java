package com.atlassian.jira.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import static com.atlassian.jira.util.collect.CollectionUtil.foreach;

public class CompositeCloseable implements Closeable
{
    private final Collection<Closeable> closeables;

    public CompositeCloseable(@Nonnull final Closeable closeable, @Nonnull final Closeable closeable2)
    {
        this(Arrays.asList(closeable, closeable2));
    }

    public CompositeCloseable(final Collection<Closeable> closeables)
    {
        foreach(closeables, Sinks.<Closeable> nullChecker());
        this.closeables = Collections.unmodifiableCollection(closeables);
    }

    public void close()
    {
        foreach(closeables, CLOSE);
    }
}
