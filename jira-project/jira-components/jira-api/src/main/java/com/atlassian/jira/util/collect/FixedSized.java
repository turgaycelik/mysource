package com.atlassian.jira.util.collect;

/**
 * Statically sized implementation of {@link com.atlassian.jira.util.collect.Sized}
 *
 * @since 6.3.6
 */
public class FixedSized implements Sized
{
    private final int size;

    public FixedSized(final int size)
    {
        this.size = size;
    }

    public int size()
    {
        return size;
    }

    public boolean isEmpty()
    {
        return size < 1;
    }
}
