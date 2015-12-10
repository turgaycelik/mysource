package com.atlassian.jira.pageobjects.util;

import java.util.AbstractList;

public class IntegerRange
{

    public static Iterable<Integer> of(final int startAt, final int finishAt)
    {
        return create(startAt, finishAt);
    }

    public static Iterable<Integer> ofSize(final int size)
    {
        return create(0, size);
    }

    public static Iterable<Integer> ofSizeStartingAt(final int startAt, final int size)
    {
        return create(startAt, startAt + size);
    }

    private static Iterable<Integer> create(final int from, final int to)
    {
        if(to < from)
        {
            throw new IllegalStateException(String.format("Faulty range creation. Got: from=%d to=%d", from, to));
        }

        return new AbstractList<Integer>()
        {
            @Override
            public Integer get(final int index)
            {
                return from + index;
            }

            @Override
            public int size()
            {
                return to - from;
            }
        };
    }

}
